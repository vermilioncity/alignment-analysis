from pathlib import Path

from flask import current_app as app, send_from_directory, request, jsonify
from sqlalchemy import func, alias, union_all, select, or_

from alignment_analysis import db
from alignment_analysis.database.models import (Respondent, Team, Location)
from alignment_analysis.database.query import (get_respondents,
                                               get_team_hierarchy,
                                               get_aligned_respondent_responses,
                                               jsonify_teams, denullify_teams,
                                               standardize_scores)
from alignment_analysis.database.query_utils import apply_filter, to_dict
from alignment_analysis.utils import get_args

STATIC_PATH = str(Path(__file__).parents[0] / 'static')

@app.route('/')
def index():
    return app.send_static_file('index.html')


@app.route('/respondents', methods=['GET'])
def respondents():
    query = get_respondents(request.args)

    return jsonify([{'id': r.id, 'name': r.name} for r in query])


@app.route('/teams', methods=['GET'])
def teams():

    team = alias(Team)
    subteam = alias(Team)

    query = db.session.query(team.c.id.label('id_1'),
                             team.c.name.label('name_1')) \
                      .filter(team.c.parent_id.is_(None))

    teams = get_team_hierarchy(query, team, subteam)

    search_term = request.args.get('search')

    if search_term:
        teams = teams.subquery(with_labels=True)
        name_cols = [c for c in teams.c if 'name' in c.name]
        teams = db.session.query(*(c.label(c.name) for c in teams.c)) \
                          .filter(or_(*(c.ilike(f'%%{search_term}%%')
                                        for c in name_cols)))

    if not teams.count():
        return jsonify([])

    teams = jsonify_teams(teams)
    teams = to_dict(teams)
    teams = denullify_teams(teams)

    return jsonify(teams)


@app.route('/locations', methods=['GET'])
def locations():

    query = Location.query

    if request.args:
        args = get_args(request.args)
        query = apply_filter(query, args, 'location')

    return jsonify([{'id': r.id, 'name': r.name} for r in query])


@app.route('/zscores', methods=['GET'])
def zscores():

    if request.args:
        query = get_respondents(request.args)
    else:
        query = Respondent.query

    query = get_aligned_respondent_responses(query)
    query = standardize_scores(query, ['id', 'name'])

    results = [row._asdict() for row in query.all()]

    return jsonify([{'id': r['id'],
                     'name': r['name'],
                     'evil_vs_good': float(r['evil_vs_good']),
                     'chaotic_vs_lawful': float(r['chaotic_vs_lawful'])}
                    for r in results])


@app.route('/correlation', methods=['GET'])
def correlation():

    import pandas as pd
    from alignment_analysis import db
    df = pd.read_sql('query.statement', con=db.engine)


@app.route('/css/<path:path>')
def send_css(path):
    return send_from_directory('css/', path)


@app.route('/data/<path:path>')
def send_data(path):
    assert False, "hello!"
    return send_from_directory('data/', path)


@app.route('/js/compiled/<path:path>')
def send_js(path):
    return send_from_directory('{}/js/compiled/'.format(STATIC_PATH), path)

