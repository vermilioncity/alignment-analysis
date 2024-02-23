from collections import defaultdict

from sqlalchemy import or_, and_

from alignment_analysis.database.models import (Respondent, Team,
                                                Location, respondent_team)
from alignment_analysis.database.query.utils import (get_model,
                                                     limit_tree_hierarchy,
                                                     team_ids)
from alignment_analysis.database.query.teams import get_team_hierarchy
from alignment_analysis import db


def filter_respondents(query, args):
    joins = defaultdict(list)
    filters = []

    search_term = args.pop('search', None)

    if search_term:
        query = query.filter(Respondent.name.ilike(f'%%{search_term}%%'))

    for model_name, values in args.items():
        model = get_model(model_name)

        if model == Team:
            team_query = get_team_hierarchy().subquery(with_labels=True)
            id_cols = team_ids(team_query)

            cond1 = Respondent.id == respondent_team.c.respondent_id
            joins[respondent_team].append(cond1)

            cond2 = or_(*[c == respondent_team.c.team_id for c in id_cols])
            joins[team_query].append(cond2)

            exprs = limit_tree_hierarchy(id_cols)
            cond3 = or_(*(c.in_(values) for c in exprs))
            filters.append(cond3)

        elif model == Location:
            cond1 = Respondent.location_id.in_(values)
            filters.append(cond1)

        elif model == Respondent:
            cond1 = Respondent.id.in_(values)
            filters.append(cond1)

    for model, conds in joins.items():
        query = query.join(model, and_(*conds))

    query = query.filter(and_(*filters)) \
                 .distinct()

    return query


def get_respondents(args):
    query = db.session.query(Respondent.id,
                             Respondent.name)

    if args:
        query = filter_respondents(query, args)

    if not query.count():
        return []
    else:
        return [{'id': r.id, 'name': r.name} for r in query]
