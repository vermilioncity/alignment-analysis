from flask.cli import FlaskGroup, click, with_appcontext

import os

from alignment_analysis import db, create_app
from alignment_analysis.database.etl.init_db import populate_db


def create_cli_app(info):
    return create_app()


@click.group(cls=FlaskGroup, create_app=create_cli_app)
@click.option('--debug', is_flag=True, default=False)
@click.option('--init_db', is_flag=True, default=False)
@with_appcontext
def cli(debug, init_db):

    if debug:
        os.environ['FLASK_DEBUG'] = '1'
        os.environ['FLASK_ENV'] = 'development'

    if init_db:
        db.drop_all()
        populate_db(db)


if __name__ == '__main__':
    cli()
