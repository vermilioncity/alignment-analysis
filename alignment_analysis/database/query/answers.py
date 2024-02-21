from sqlalchemy import func, and_, Float, cast, or_
from alignment_analysis.database.models import (Response,
                                                OptionAlignment, Question,
                                                Alignment, Option,
                                                Respondent, Team,
                                                Location, respondent_team)
from collections import defaultdict

from alignment_analysis import db

from alignment_analysis.database.query.utils import (get_model, team_ids,
                                                     limit_tree_hierarchy)
from alignment_analysis.database.query.teams import get_team_hierarchy


def filter_answers(query, args):
    joins = defaultdict(list)
    filters = []

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


def answers_by_question(question):

    if len(question) != 1:
        raise Exception('Too many questions')
    else:
        question = question[0]

    raw_score = func.max(func.abs(Alignment.raw_score))
    alignment = func.array_agg(Alignment.name)

    query = db.session.query(Question.id.label('question_id'),
                             Question.name.label('question_name'),
                             Option.id.label('option_id'),
                             Option.name.label('option_name'),
                             raw_score.label('raw_score'),
                             alignment.label('alignment')) \
                      .join(Option) \
                      .join(OptionAlignment) \
                      .join(Alignment) \
                      .filter(Option.id == question) \
                      .group_by(Question.id,
                                Question.name,
                                Option.id,
                                Option.name) \
                      .cte()

    sum_ = func.sum(query.c.raw_score)

    query = db.session.query(query.c.question_name,
                             query.c.question_id,
                             query.c.option_id,
                             query.c.option_name,
                             query.c.alignment,
                             sum_.label('sum')) \
                      .join(Response,
                            and_(Response.question_id == query.c.question_id,
                                 Response.option_id == query.c.option_id)) \
                      .join(Respondent,
                            Respondent.id == Response.respondent_id) \
                      .group_by(query.c.question_name,
                                query.c.question_id,
                                query.c.option_id,
                                query.c.option_name,
                                query.c.alignment)

    return query


def calculate_percent(query):

    pct = (func.sum('sum') * 1.0) / func.count(query.c.option_id)

    percent = db.session.query(query.c.question_id,
                               pct.label('percent')) \
                        .group_by(query.c.question_id) \
                        .subquery()

    return percent


def get_answers(args):

    question = args.pop('question')
    query = answers_by_question(question)

    if args:
        query = filter_answers(query, args)

    query = query.cte()

    percent = calculate_percent(query)

    query = db.session.query(*query.c, percent.c.percent) \
                      .join(percent,
                            percent.c.question_id == query.c.question_id)

    results = [row._asdict() for row in query.all()]

    return results
