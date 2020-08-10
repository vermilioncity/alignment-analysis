from sqlalchemy import or_, func, column, case, select
from alignment_analysis.database.models import (Respondent, Team, Response,
                                                OptionAlignment, Dimension,
                                                Alignment, Option)
from alignment_analysis.utils import get_args
from sqlalchemy import alias


def get_team_hierarchy(query, team, subteam):
    """Recursively child teams on parent id until there are
    no more children left to add.

    Args:
        query: Current query
        team: Aliased Team model
        subteam: Aliased Team model

    Returns:
        query: Query with children added

    """

    query = query.outerjoin(subteam, team.c.id == subteam.c.parent_id)

    null_count = query.filter(subteam.c.name.is_(None)).count()

    if query.count() == null_count:
        return query

    query = query.add_columns(subteam.c.id, subteam.c.name)

    return get_team_hierarchy(query, subteam,
                              alias(Team))


def jsonify_teams(query):
    """Converts a table to JSON format.  This is soooo ugly.

    Args:
        query (flask_sqlalchemy.BaseQuery): Self-joining query

    Returns:
        flask_sqlalchemy.BaseQuery: Query returning one JSON blob
        per top-level team

    """

    json_col = None
    num_joins = int(len(query.first())/2)

    for i in range(num_joins, 1, -1):
        ids = [column(f'team_{j}_id') for j in range(1, i)]
        names = [column(f'team_{j}_name') for j in range(1, i)]

        json_cols = ['id', column(f'team_{i}_id'),
                     'name', column(f'team_{i}_name')]
        if json_col is not None:
            json_cols.extend(['subteams', column('subteams')])

        json_col = func.json_strip_nulls(func.json_build_object(*json_cols))
        query = query.from_self(func.array_agg(json_col).label('subteams'),
                                *(ids+names)) \
                     .group_by(*(ids+names))

    query = query.from_self(column('team_1_id').label('id'),
                            column('team_1_name').label('name'),
                            column('subteams'))

    return query


def denullify_teams(subteams):
    """Recursively removes nested empty values in JSON,
    like {"subteams": {"subteams": [{}]}, etc.

    Args:
        subteams (dict): Dictionary of team metadata

    Returns:
        subteams (dict): Dictionary of metadata with nested
            data removed.
    """

    for subteam in subteams:

        if 'subteams' not in subteam:
            break

        else:
            for subsubteam in subteam['subteams']:
                if 'id' not in subsubteam:
                    del subteam['subteams']
                else:
                    denullify_teams(subteam['subteams'])

    return subteams


def get_respondents(args):
    """Queries the Respondents table, applying filters as needed.

    Args:
        args (dict): Dictionary of parameters (e.g., {'location': [4, 5]})

    Returns:
        flask_sqlalchemy.BaseQuery: Respondents, optionally filtered

    """

    query = Respondent.query
    args = get_args(args)

    for field, values in args.items():
        field = 'id' if field == 'respondent' else f'{field}_id'
        query = query.filter(getattr(Respondent, field).in_(values))

    query = query.with_entities(Respondent.id, Respondent.name)

    return query


def get_aligned_respondent_responses(query):
    """Joins respondents to their responses and alignments.

    Args:
        query (flask_sqlalchemy.BaseQuery): Respondents query

    Returns:
        flask_sqlalchemy.BaseQuery: Respondent ID/name, dimension name,
        score, and option_id

    """

    query = query.join(Response) \
                 .join(OptionAlignment,
                       Response.option_id == OptionAlignment.option_id) \
                 .join(Option) \
                 .join(Alignment) \
                 .join(Dimension) \
                 .with_entities(Respondent.id.label('id'),
                                Respondent.name.label('name'),
                                Dimension.name.label('dimension'),
                                Alignment.raw_score.label('raw_score'),
                                Alignment.binary_score.label('binary_score'),
                                Option.question_id.label('question_id'),
                                OptionAlignment.option_id.label('option_id'))

    return query


def _sum_case_when(dimension):
    return func.sum(case([(column('dimension') == dimension,
                           column('adjusted_score'))], else_=0)) \
               .label(dimension)


def standardize_scores(query, grouping_cols):
    """ Recalibrate scores to be calibrated against others' answers.

    Args:
        query (flask_sqlalchemy.BaseQuery):
        grouping_cols (list): List of columns to group by

    Returns:

    """

    query = query.add_columns(func.sum(Alignment.raw_score)
                                  .over(partition_by=[Dimension.name, Respondent.id])
                                  .label('raw_sum'),
                              func.avg(Alignment.binary_score)
                                  .over(partition_by=[Dimension.name, Option.question_id])
                                  .label('mean'))

    std = func.nullif(func.sqrt(column('mean') * (1 - column('mean'))), 0)
    query = query.from_self('dimension',
                            'raw_score', 'binary_score',
                            'question_id', 'option_id',
                            ((column('binary_score') - column('mean')) / std).label('adjusted_score'),
                            *grouping_cols)

    query = query.from_self(*grouping_cols) \
                 .add_columns(_sum_case_when('Chaotic vs. Lawful'), _sum_case_when('Evil vs. Good')) \
                 .group_by(*grouping_cols)

    return query
