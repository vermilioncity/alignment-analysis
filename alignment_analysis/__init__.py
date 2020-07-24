import os

from flask import Flask


def create_app():
    app = Flask(__name__, static_url_path='/static/')
    app.config.from_mapping(DATA_DIR=os.path.join('alignment_analysis/database'))

    @app.shell_context_processor
    def shell_context():
        return {'app': app}

    with app.app_context():
        import alignment_analysis.database.models as models

        import alignment_analysis.views

        return app


