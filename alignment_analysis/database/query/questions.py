from alignment_analysis.database.models import Question

from alignment_analysis import db


def get_questions(args):

    query = db.session.query(Question.id, Question.name)

    search_term = args.pop('search', None)

    if search_term:
        query = query.filter(Question.name.ilike(f'%%{search_term}%%'))

    return [{'id': r.id, 'name': r.name} for r in query]
