from sqlalchemy import or_, func, column, case
from alignment_analysis.database.models import (Respondent, Team, Response,
                                                OptionAlignment, Dimension, Alignment, Option)
from alignment_analysis.utils import get_args


def get_team_hierarchy(team, subteam):
    """Self-joins the teams table to generate a ragged hierachy of departments -> teams -> subteams

    Args:
        team (flask_sqlalchemy.model.DefaultMeta): aliased Team model
        subteam (flask_sqlalchemy.model.DefaultMeta): aliased Team model

    Returns:
        flask_sqlalchemy.BaseQuery: Self-joining query comprising department/team/subteam long table hierarchy

    """

    query = Team.query.outerjoin(team, Team.id == team.c.parent_id) \
                      .outerjoin(subteam, team.c.id == subteam.c.parent_id) \
                      .filter(Team.level == 1, or_(team.c.level == 2, team.c.level.is_(None)),
                              or_(subteam.c.level == 3, subteam.c.level.is_(None))) \
                      .with_entities(Team.id.label('department_id'),
                                     Team.name.label('department_name'),
                                     team.c.id.label('team_id'),
                                     team.c.name.label('team_name'),
                                     subteam.c.id.label('subteam_id'), subteam.c.name.label('subteam_name'))

    return query


def jsonify_teams(query):
    """Converts a long hierarchy table into a JSON of department -> teams -> subteams

    Args:
        query (flask_sqlalchemy.BaseQuery): Self-joining query comprising department/team/subteam long table hierarchy

    Returns:
        flask_sqlalchemy.BaseQuery: Query returning one JSON blob per department

    """

    query = query.from_self('department_id', 'department_name',
                            'team_id', 'team_name',
                            func.json_build_object('id', column('subteam_id'), 'name', column('subteam_name')).label(
                                'subteams'))

    query = query.from_self('department_id', 'department_name',
                            func.json_build_object('id', column('team_id'),
                                                   'name', column('team_name'),
                                                   'subteams', func.array_agg(column('subteams'))).label('teams')) \
                 .group_by('department_id', 'department_name', 'team_id', 'team_name')

    blob = func.json_build_object('id', column('department_id'),
                                  'name', column('department_name'),
                                  'teams', func.array_agg(column('teams')))

    query = query.from_self(func.json_strip_nulls(blob).label('departments')) \
                 .group_by('department_id', 'department_name')

    return query


def denullify_json(results):
    """Removes nested empty key/value pairs, e.g., {"subteams": [{}]}"

    Args:
        results (list): JSON that includes empty key/value pairs

    Returns:
        list: JSON with no empty key/value pairs

    """

    for i, department in enumerate(results):
        if department['teams'] == [{'subteams': [{}]}]:
            del results[i]['teams']
            continue

        for j, team in enumerate(department['teams']):
            if not team['subteams'][0]:
                del team['subteams']

    return results


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
        flask_sqlalchemy.BaseQuery: Respondent ID/name, dimension name, score, and option_id

    """

    query = query.join(Response) \
                 .join(OptionAlignment, Response.option_id == OptionAlignment.option_id) \
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
    return func.sum(case([(column('dimension') == dimension, column('adjusted_score'))], else_=0)).label(dimension)


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
