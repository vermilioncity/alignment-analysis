from flask import current_app as app, send_from_directory, request, jsonify
from alignment_analysis import db
from alignment_analysis.database.models import (Respondent, Team, Location)
from alignment_analysis.database.query import (get_respondents, get_team_hierarchy,
                                               get_aligned_respondent_responses,
                                               jsonify_teams, denullify_json,
                                               standardize_scores)
from alignment_analysis.database.query_utils import apply_filter
from sqlalchemy import func, alias, union_all
from alignment_analysis.utils import get_args


@app.route('/')
def index():
    return app.send_static_file('templates/index.html')


@app.route('/respondents', methods=['GET'])
def respondents():
    query = get_respondents(request.args)

    return jsonify([{'id': r.id, 'name': r.name} for r in query])


@app.route('/teams', methods=['GET'])
def teams():

    subteam = alias(Team, name='subteam')
    team = alias(Team, name='team')

    query = get_team_hierarchy(team, subteam)

    request_args = dict(request.args)
    flatten = request_args.pop('flatten', None)

    if request_args:
        args = get_args(request_args)
        filter_query = apply_filter(Team.query, args, 'team').distinct().subquery()
        query = query.join(filter_query, func.coalesce(subteam.c.id, team.c.id, Team.id) == filter_query.c.id)

    if not flatten:
        department = query.cte(name="temp_table")
        team = alias(department, name='team')
        subteam = alias(department, name='subteam')
        t = db.session.query(union_all(department.select([department.c.team_id.label('id'), department.c.subteam_name.label('name')]),
                                       team.select([team.c.team_id.label('id'), team.c.subteam_name.label('name')]),
                                       subteam.select([subteam.c.team_id.label('id'), subteam.c.subteam_name.label('name')])))
        #query = query.from_self(column('department_id').label('id'))

    else:
        query = jsonify_teams(query)
        results = denullify_json([c[0] for c in query.all()])

    return jsonify(results)


@app.route('/locations', methods=['GET'])
def locations():

    query = Location.query

    if request.args:
        args = get_args(request.args)
        query = apply_filter(query, args, 'location')

    return jsonify([{'id': r.id, 'name': r.name} for r in query])


@app.route('/z_scores', methods=['GET'])
def z_scores():

    if request.args:
        query = get_respondents(request.args)
    else:
        query = Respondent.query

    query = get_aligned_respondent_responses(query)
    query = standardize_scores(query, ['id', 'name'])

    results = [row._asdict() for row in query.all()]

    return jsonify([{'id': r['id'], 'name': r['name'],
                     'Evil vs. Good': float(r['Evil vs. Good']),
                     'Chaotic vs. Lawful': float(r['Chaotic vs. Lawful'])} for r in results])


@app.route('/correlation', methods=['GET'])
def correlation():

    import pandas as pd
    from alignment_analysis import db
    df = pd.read_sql('query.statement', con=db.engine)


@app.route('/css/<path:path>')
def send_css(path):
    return send_from_directory('css/', path)


@app.route('/dist/<path:path>')
def send_js(path):
    return send_from_directory('dist/', path)


@app.route('/data/<path:path>')
def send_data(path):
    return send_from_directory('data/', path)
