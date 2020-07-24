from alignment_analysis.database import models
from alignment_analysis.database.models import Respondent


def _is_existing_join(query, table):
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

    return model.__table__.name


def get_model(field):
    """Get model from class name.

    Args:
        field (str): Name of class

    Returns:
        flask_sqlalchemy.model.DefaultMeta: SQLAlchemy model

    """

    return getattr(models, field.title())


def apply_filter(query, args, model_name):
    """Apply filters to a query based on a dictionary.

    Args:
        query (flask_sqlalchemy.BaseQuery):
        args (dict): Dictionary of filter parameters (e.g., {'location': [4, 5]})
        model_name (str): Name of model associated with endpoint

    Returns:
        flask_sqlalchemy.BaseQuery: Query filtered to parameters

    """

    if args:
        for field, values in args.items():
            if model_name == field:
                model = get_model(model_name)
            else:
                model = get_model(field)

            query = query.filter(getattr(model, 'id').in_(values))

            if model_name not in args.keys():
                if not _is_existing_join(query, 'respondents') and _get_table_name(model) != 'respondents':
                    query = query.join(Respondent)

                query = query.join(model)

    return query
