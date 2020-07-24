import pandas as pd


def create_score_column(answers):
    """Create two score columns--a raw score simply summing across the dimension, and a z-scored score.

    Args:
        answers (DataFrame): a long table of respondent answers and associated alignment for each answer

    Returns:
        DataFrame: original dataframe with a raw score and  adjusted score column

    """

    answers['Raw Sum'] = answers.groupby(['Dimension', 'Name'])['Score'].transform('sum')
    mean = answers.groupby(['Dimension'])['Raw Sum'].transform('mean')
    std = answers.groupby(['Dimension'])['Raw Sum'].transform('std')
    answers['Adjusted Sum'] = ((answers['Raw Sum'] - mean) / std)

    return answers


def assign_alignment(answers, score_column='Raw Sum'):
    """Assign people to a four-dimensional label based on their score.

    Args:
        answers (DataFrame): Individual scored answers to each question
        score_column (str): Name of score column to use (Raw Sum or Adjusted Sum)

    Returns:
        DataFrame: Long table of person, summed scores, and alignment group

    """

    alignment_scores = answers[['Name', 'Dimension', score_column]].drop_duplicates().copy(deep=True)

    alignment_scores = pd.pivot_table(data=alignment_scores, index='Name', values=score_column,
                                      columns='Dimension', aggfunc='sum') \
                         .reset_index()

    return alignment_scores
