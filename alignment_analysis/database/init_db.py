import json

import pandas as pd
from flask import current_app
import os

from alignment_analysis.database.data_prep import clean_responses_file

from sqlalchemy import create_engine


def get_engine(username=None, password=None, service=None, port=None, db_name=None):
    username = username or os.getenv('POSTGRES_USER'),
    password = password or os.getenv('POSTGRES_PASSWORD'),
    service = service or os.getenv('POSTGRES_SERVICE'),
    port = port or os.getenv('POSTGRES_PORT'),
    db_name = db_name or os.getenv('POSTGRES_DB')

    return create_engine(f'postgresql+psycopg2://{username}:{password}@{service}:{port}/{db_name}')


def _get_instance(session, model, **kwargs):
    return session.query(model).filter_by(**kwargs).first()


def _add_if_new(session, model, **kwargs):
    instance = _get_instance(session, model, **kwargs)
    if instance is None:
        instance = model(**kwargs)
        session.add(instance)
        session.flush()

    return instance


def load_dimension(session):

    from alignment_analysis.database.models import Dimension

    for dim in ('Chaotic vs. Lawful', 'Evil vs. Good'):
        dimension = Dimension(name=dim)
        session.add(dimension)

    session.commit()


def load_alignment(session):
    from alignment_analysis.database.models import (Dimension, Alignment)

    for dim in ('Chaotic vs. Lawful', 'Evil vs. Good'):
        dimension = _get_instance(session, Dimension, name=dim)
        dim_list = dim.split(' vs. ')

        session.add_all([Alignment(name=dim_list[0], binary_score=0, raw_score=-1, dimension_id=dimension.id),
                         Alignment(name=dim_list[1], binary_score=1, raw_score=1, dimension_id=dimension.id)])

    session.commit()


def load_questions_options(app, session, filepath):
    from alignment_analysis.database.models import (Question, Option, Alignment, OptionAlignment)

    with app.open_resource(filepath) as f:
        question_bank = pd.read_csv(f)

    for row in question_bank.itertuples():
        question = _add_if_new(session, Question, name=row.question)
        alignment = _get_instance(session, Alignment, name=row.alignment)
        option = _add_if_new(session, Option, name=row.option, question_id=question.id)
        option_alignment = OptionAlignment(question_id=question.id, option_id=option.id,
                                           alignment_id=alignment.id, dimension_id=alignment.dimension_id)
        session.add(option_alignment)

    session.commit()


def load_teams(app, session, filepath):

    from alignment_analysis.database.models import Team

    with app.open_resource(filepath) as f:
        teams = json.load(f)

    for team in teams:
        name = team['team']
        parent_name = team.get('parent')
        if parent_name:
            parent = _get_instance(session, Team, name=parent_name)

            assert parent, f'{parent_name} does not exist'

            session.add(Team(name=name, parent_id=parent.id))

        else:
            session.add(Team(name=name))

        session.flush()

    session.commit()


def load_locations(app, session, filepath):
    from alignment_analysis.database.models import Location

    with app.open_resource(filepath) as f:
        locations = json.load(f)

    for location in locations['locations']:
        session.add(Location(name=location))

    session.commit()


def load_respondents(app, session, filepath):

    from alignment_analysis.database.models import Location, Respondent, Team

    with app.open_resource(filepath) as f:
        respondents = json.load(f)

    for respondent in respondents:
        name = respondent['name']
        teams = respondent['teams']
        location_name = respondent['location']

        location = _get_instance(session, Location, name=location_name)

        assert location, f'{location} does not exist'

        r = Respondent(name=name, location_id=location.id)
        session.add(r)

        session.flush()

        for team in teams:
            t = _get_instance(session, Team, name=team)
            r.teams.append(t)

    session.commit()


def load_responses(session, responses):

    from alignment_analysis.database.models import Response, Respondent, Option

    for response in responses.itertuples():
        respondent = _get_instance(session, Respondent, name=response.name)
        option = _get_instance(session, Option, name=response.option)

        assert respondent, f'{response.name} does not exist'
        assert option, f'{response.option} does not exist'

        session.add(Response(respondent_id=respondent.id, question_id=option.question_id, option_id=option.id))

    session.commit()


def populate_db(db):

    from alignment_analysis.database import models

    db.create_all()

    root_data_dir = os.path.join(current_app.root_path, 'database', 'data')

    load_dimension(db.session)
    load_alignment(db.session)

    load_teams(current_app, db.session, os.path.join(root_data_dir, 'teams.json'))
    load_locations(current_app, db.session, os.path.join(root_data_dir, 'locations.json'))
    load_respondents(current_app, db.session, os.path.join(root_data_dir, 'respondents.json'))
    load_questions_options(current_app, db.session, os.path.join(root_data_dir, 'question_bank.csv'))

    responses = clean_responses_file(os.path.join(root_data_dir, 'responses.csv'))
    load_responses(db.session, responses)


