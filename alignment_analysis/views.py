from pathlib import Path

from flask import current_app as app, send_from_directory, request, jsonify

from alignment_analysis.database.query import (get_respondents,
                                               get_teams,
                                               get_locations,
                                               get_scores,
                                               get_correlations,
                                               get_answers,
                                               get_questions)


STATIC_PATH = str(Path(__file__).parents[0] / 'static')


@app.route('/')
def index():
    return app.send_static_file('index.html')


@app.route('/respondents', methods=['GET'])
def respondents():
    args = request.args.to_dict(flat=False)

    results = get_respondents(args)

    return jsonify(results)


@app.route('/teams', methods=['GET'])
def teams():

    args = request.args.to_dict(flat=False)

    results = get_teams(args)

    return jsonify(results)


@app.route('/locations', methods=['GET'])
def locations():

    args = request.args.to_dict(flat=False)
    results = get_locations(args)

    return jsonify(results)


@app.route('/zscores', methods=['GET'])
def zscores():

    args = request.args.to_dict(flat=False)

    results = get_scores(args)

    return jsonify(results)


@app.route('/correlation', methods=['GET'])
def correlation():

    args = request.args.to_dict(flat=False)

    results = get_correlations(args)

    return jsonify(results)


@app.route('/answers', methods=['GET'])
def answers():

    args = request.args.to_dict(flat=False)

    results = get_answers(args)

    return jsonify(results)


@app.route('/questions', methods=['GET'])
def questions():

    args = request.args.to_dict(flat=False)

    results = get_questions(args)

    return jsonify(results)


@app.route('/css/<path:path>')
def send_css(path):
    return send_from_directory('css/', path)


@app.route('/data/<path:path>')
def send_data(path):
    return send_from_directory('data/', path)


@app.route('/js/compiled/<path:path>')
def send_js(path):
    return send_from_directory('{}/js/compiled/'.format(STATIC_PATH), path)

