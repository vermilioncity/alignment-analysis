from collections import defaultdict

from sqlalchemy import or_, and_

from alignment_analysis.database.models import (Respondent, Team,
                                                Location, respondent_team)
from alignment_analysis.database.query.utils import (get_model, team_ids,
                                                     limit_tree_hierarchy)
from alignment_analysis.database.query.teams import get_team_hierarchy
from alignment_analysis import db


def filter_locations(query, args):
    joins = defaultdict(list)
    filters = []

    search_term = args.pop('search', None)

    if search_term:
        query = query.filter(Location.name.ilike(f'%%{search_term}%%'))

    for model_name, values in args.items():
        model = get_model(model_name)

        if model == Team:
            team_query = get_team_hierarchy().subquery(with_labels=True)
            id_cols = team_ids(team_query)

            cond1 = Location.id == Respondent.location_id
            joins[Respondent].append(cond1)

            cond2 = Respondent.id == respondent_team.c.respondent_id
            joins[respondent_team].append(cond2)

            cond3 = or_(*[c == respondent_team.c.team_id for c in id_cols])
            joins[team_query].append(cond3)

            exprs = limit_tree_hierarchy(id_cols)
            cond4 = or_(*(c.in_(values) for c in exprs))
            filters.append(cond4)

        elif model == Location:
            cond1 = Location.id.in_(values)
            filters.append(cond1)

        elif model == Respondent:
            cond1 = Location.id == Respondent.location_id
            joins[Respondent].append(cond1)

            cond2 = Respondent.id.in_(values)
            filters.append(cond2)

    for model, conds in joins.items():
        query = query.join(model, and_(*conds))

    query = query.filter(and_(*filters)) \
                 .distinct()

    return query


def get_locations(args):
    query = db.session.query(Location.id,
                             Location.name)

    if args:
        query = filter_locations(query, args)

    if not query.count():
        return []

    return [{'id': r.id, 'name': r.name} for r in query]
