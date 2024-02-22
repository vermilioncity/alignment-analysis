from sqlalchemy import func, column, case
import pandas as pd

from alignment_analysis.database.models import (Respondent, Response,
                                                OptionAlignment, Dimension,
                                                Alignment, Option)
from alignment_analysis import db
from alignment_analysis.database.query.respondents import filter_respondents


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
    label = dimension.lower() \
                     .replace('.', '') \
                     .replace(' ', '_')

    return func.sum(case((column('dimension') == dimension, column('adjusted_score')), else_=0)) \
               .label(label)


def standardize_scores(session, query):
    """ Recalibrate scores to be calibrated against others' answers."""

    query = query.add_columns(func.sum(Alignment.raw_score)
                                  .over(partition_by=[Dimension.name,
                                                      Respondent.id])
                                  .label('raw_sum'),
                              func.avg(Alignment.binary_score)
                                  .over(partition_by=[Dimension.name,
                                                      Option.question_id])
                                  .label('mean')) \
                .subquery()

    std = func.nullif(func.sqrt(column('mean') * (1 - column('mean'))), 0)
    adjusted_score = ((column('binary_score') - column('mean')) / std)
    query = session.query(query.c.dimension,
                            query.c.raw_score,
                            query.c.binary_score,
                            query.c.question_id,
                            query.c.option_id,
                            adjusted_score.label('adjusted_score'),
                            query.c.id,
                            query.c.name)

    return query


def sum_standardized_scores(query):

    query = query.subquery()

    query = db.session.query(query.c.id,
                             query.c.name) \
                      .add_columns(_sum_case_when('Chaotic vs. Lawful'),
                                   _sum_case_when('Evil vs. Good')) \
                      .group_by(query.c.id, query.c.name)

    return query


def calculate_correlations(query):
    corr = pd.DataFrame(query)
    corr['adjusted_score'] = corr['adjusted_score'].astype(float)
    corr = corr.pivot_table(index='question_id',
                            columns=['id', 'name'],
                            values='adjusted_score') \
               .corr() \
               .rename_axis(['id1', 'name1'], axis=1) \
               .stack() \
               .stack() \
               .reset_index()

    corr = corr.groupby(['id', 'name'])[['id1', 'name1', 0]].agg(list) \
               .reset_index() \
               .sort_values(['name'])

    results = []
    for row in corr.itertuples():
        result = {"id": row[1], "name": row[2], "scores": []}
        for id_, name, score in zip(row[3], row[4], row[5]):
            result['scores'].append({"id": id_, "name": name, "score": score})

        results.append(result)

    return results


def get_scores(args):

    query = db.session.query(Respondent)

    query = get_aligned_respondent_responses(query)
    query = standardize_scores(db.session, query)
    query = sum_standardized_scores(query)

    if args:
        query = query.subquery()
        query = db.session.query(*query.c) \
                          .join(Respondent, query.c.id == Respondent.id)
        query = filter_respondents(query, args)

    results = [row._asdict() for row in query.all()]

    return [{'id': r['id'],
             'name': r['name'],
             'evil_vs_good': float(r['evil_vs_good']),
             'chaotic_vs_lawful': float(r['chaotic_vs_lawful'])}
             for r in results]


def get_correlations(args):

    query = db.session.query(Respondent)

    query = get_aligned_respondent_responses(query)
    query = standardize_scores(db.session, query)

    if args:
        query = query.subquery()
        query = db.session.query(*query.c) \
                          .join(Respondent, query.c.id == Respondent.id)
        query = filter_respondents(query, args)

    results = calculate_correlations(query)

    return results

