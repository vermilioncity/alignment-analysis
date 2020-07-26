import os

from flask import Flask
from flask_sqlalchemy import SQLAlchemy

db = SQLAlchemy()


def create_app():
    app = Flask(__name__, static_url_path='/static/')
    app.config.from_mapping(DATA_DIR=os.path.join('alignment_analysis/database'))

    app.config.from_mapping(
        SQLALCHEMY_DATABASE_URI='postgresql+psycopg2://{username}:{password}@{service}:{port}/{db_name}'.format(
                                username=os.getenv('POSTGRES_USER'),
                                password=os.getenv('POSTGRES_PASSWORD'),
                                service=os.getenv('POSTGRES_SERVICE'),
                                port=os.getenv('POSTGRES_PORT'),
                                db_name=os.getenv('POSTGRES_DB')),
        DATA_DIR=os.path.join('alignment_analysis/database')
    )

    db.init_app(app)

    @app.shell_context_processor
    def shell_context():
        return {'app': app, 'db': db}

    with app.app_context():
        import alignment_analysis.database.models as models

        import alignment_analysis.views

        return app


