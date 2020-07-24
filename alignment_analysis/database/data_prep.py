import pandas as pd
import numpy as np


def _convert_email_to_name(answers):
    answers['name'] = answers['name'].str.replace('@(nielsen|gmail)\.com', '') \
                                     .str.replace('(\.ap)|(\.consultant)', '') \
                                     .str.replace('\.\w\.', ' ') \
                                     .str.replace('.', ' ') \
                                     .str.replace('jhal', 'Jonathan Hal') \
                                     .str.title()

    return answers


def _remap_responses(answers):
    answers['name'] = np.where(answers['Email Address'].isna(), answers['What is your Nielsen email?'],
                               answers['Email Address'])

    answers['Which is your favorite Slack Channel?'] = np.where(answers['Which is your favorite Slack Channel?'] ==
                                                                'Channels where people tend to get into (fun) arguments',
                                                                'MA-Pulse',
                                                                answers['Which is your favorite Slack Channel?'])

    return answers


def clean_responses_file(path):

    answers = pd.read_csv(path)

    answers = _remap_responses(answers)
    answers = _convert_email_to_name(answers)

    answers = answers.drop(['Timestamp', 'Score', 'Email Address',
                            'Which Nielsen location do you sit in? ',
                            'What is your Nielsen email?'], axis=1) \
                     .melt(id_vars=['name'], var_name='question', value_name='option') \
                     .dropna(subset=['name'])

    return answers
