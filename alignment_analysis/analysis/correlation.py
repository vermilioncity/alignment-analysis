import pandas as pd


def create_correlation(answers):
    """Creates a correlation table across answers to identify who answers most similarly to whom.

    Args:
        answers (DataFrame): a long table of respondent answers and associated alignment for each answer

    Returns:
        DataFrame: Correlation of answers between all individuals

    """

    answers['Score'] = 1

    correlation = pd.pivot_table(answers, index='Answer ID', columns='Name', values='Score', fill_value=0) \
                    .corr() \
                    .reset_index() \
                    .melt(id_vars='Name', var_name='Comparison', value_name='Score')

    return correlation