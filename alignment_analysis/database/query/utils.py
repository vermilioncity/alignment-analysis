from sqlalchemy import case

from alignment_analysis.database import models


def is_existing_join(query, table):
    """Checks if a table is already joined to a query.

    Args:
        query (flask_sqlalchemy.BaseQuery): A query
        table (str): Name of a table to join

    Returns:
        flask_sqlalchemy.BaseQuery: Query joined to table

    """

    for mapper in query._join_entities:
        if _get_table_name(mapper.entity) == table:
            return True

    return False


def _get_table_name(model):
    """Gets table name of model

    Args:
        model (flask_sqlalchemy.model.DefaultMeta): SQLAlchemy model

    Returns:
        str: Table name of model

    """

    return model.__tablename__


def get_model(field):
    """Get model from class name.

    Args:
        field (str): Name of class

    Returns:
        flask_sqlalchemy.model.DefaultMeta: SQLAlchemy model

    """

    return getattr(models, field.title())


def team_ids(hierarchy):
    return [c for c in hierarchy.c if 'id' in c.name]


def to_dict(query):
    return [row._asdict() for row in query.all()]


def limit_tree_hierarchy(id_cols):
    """ Ensures someone who does not belong to a subteam is not
    included in subteam results """

    exprs = []
    for c in id_cols:
        expr = case((models.respondent_team.c.team_id < c, None), else_=c)
        exprs.append(expr)

    return exprs
