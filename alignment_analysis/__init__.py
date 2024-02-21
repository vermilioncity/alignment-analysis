import os

from flask import Flask
from flask_sqlalchemy import SQLAlchemy

db = SQLAlchemy()


def create_app():
    app = Flask(__name__, static_url_path='/static')
    app.config.from_mapping(DATA_DIR=os.path.join('alignment_analysis/database'))
    app.config['JSON_SORT_KEYS'] = False

    #'postgresql+psycopg2://{username}:{password}@{service}:{port}/{db_name}'
    app.config.from_mapping(
        SQLALCHEMY_DATABASE_URI='postgresql+psycopg2://postgres:postgres@aa_db:5432/postgres',
        DATA_DIR=os.path.join('alignment_analysis/database')
    )

    db.init_app(app)

    @app.shell_context_processor
    def shell_context():
        return {'app': app, 'db': db}

    with app.app_context():
        import alignment_analysis.database.models as models

        from alignment_analysis.views import index

        return app


