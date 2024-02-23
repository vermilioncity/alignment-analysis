from collections import defaultdict

from sqlalchemy import alias, column, func, or_, and_

from alignment_analysis.database.models import (Team, Respondent,
                                                Location, respondent_team)
from alignment_analysis import db
from alignment_analysis.database.query.utils import (to_dict,
                                                     get_model,
                                                     team_ids)


def _get_team_hierarchy(query, team, subteam, count=2):
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

    query = query.add_columns(subteam.c.id.label(f'id_{count}'),
                              subteam.c.name.label(f'name_{count}'))

    return _get_team_hierarchy(query, subteam,
                               alias(Team), count+1)


def get_team_hierarchy():
    team = alias(Team)
    subteam = alias(Team)

    query = db.session.query(team.c.id.label('id_1'),
                             team.c.name.label('name_1')) \
                      .filter(team.c.parent_id.is_(None))

    teams = _get_team_hierarchy(query, team, subteam)

    return teams


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
        ids = [column(f'id_{j}') for j in range(1, i)]
        names = [column(f'name_{j}') for j in range(1, i)]

        json_cols = ['id', column(f'id_{i}'),
                     'name', column(f'name_{i}')]

        if json_col is not None:
            json_cols.extend(['subteams', column('subteams')])

        json_col = func.json_strip_nulls(func.json_build_object(*json_cols))

        query = db.session.query() \
                          .select_from(query.subquery()) \
                          .add_columns(func.array_agg(json_col).label('subteams'),
                                       *(ids+names)) \
                          .group_by(*(ids+names))

    query = db.session.query() \
                      .select_from(query.subquery()) \
                      .add_columns(column('id_1').label('id'),
                                   column('name_1').label('name'),
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


def filter_teams(team, args):

    joins = defaultdict(list)
    filters = []

    search_term = args.pop('search', None)

    if search_term:
        team = team.subquery(with_labels=True)
        name_cols = [c for c in team.c if 'name' in c.name]
        team = db.session.query(*(c.label(c.name) for c in team.c)) \
                         .filter(or_(*(c.ilike(f'%%{search_term}%%')
                                     for c in name_cols)))

    team = team.subquery(with_labels=True)

    for model_name, values in args.items():
        model = get_model(model_name)

        if model == Team:
            cond1 = or_(*(c.in_(values) for c in team_ids(team)))
            filters.append(cond1)

        elif model == Location:
            cond1 = or_(*[c == respondent_team.c.team_id
                          for c in team_ids(team)])
            joins[respondent_team].append(cond1)

            cond2 = Respondent.id == respondent_team.c.respondent_id
            joins[Respondent].append(cond2)

            cond3 = Respondent.location_id.in_(values)
            filters.append(cond3)

        elif model == Respondent:
            cond1 = or_(*[c == respondent_team.c.team_id
                          for c in team_ids(team)])
            joins[respondent_team].append(cond1)

            cond2 = Respondent.id == respondent_team.c.respondent_id
            joins[Respondent].append(cond2)

            cond3 = Respondent.id.in_(values)
            filters.append(cond3)

    query = db.session.query(*team.c) \
                      .filter(and_(*filters)) \
                      .distinct()

    for model, conds in joins.items():
        query = query.join(model, or_(*conds))

    return query


def get_teams(args):

    team = get_team_hierarchy()

    if args:
        team = filter_teams(team, args)

    if not team.count():
        return []

    team = jsonify_teams(team)
    team = to_dict(team)
    team = denullify_teams(team)

    return team
