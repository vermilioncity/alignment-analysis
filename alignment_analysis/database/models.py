from sqlalchemy import Table, Column, Integer, String, MetaData, ForeignKey

metadata = MetaData()

respondents = Table('respondents', metadata,
                    Column('id', Integer, primary_key=True),
                    Column('name', String(30)),
                    Column('location_id', Integer, ForeignKey('locations.id'), nullable=False),
                    Column('team_id', Integer, ForeignKey('teams.id'), nullable=False)
                    )

teams = Table('teams', metadata,
              Column('id', Integer, primary_key=True),
              Column('name', String(30), unique=True, nullable=False),
              Column('level', Integer, nullable=False),
              Column('parent_id', Integer, ForeignKey('teams.id'), nullable=True))

locations = Table('locations', metadata,
                  Column('id', Integer, primary_key=True),
                  Column('name', String(20), unique=True, nullable=False))

questions = Table('questions', metadata,
                  Column('id', Integer, primary_key=True),
                  Column('name', String(140), unique=True, nullable=False))

options = Table('options', metadata,
                Column('id', Integer, primary_key=True),
                Column('name', String(80), unique=True, nullable=False),
                Column('question_id', Integer, ForeignKey('questions.id'), nullable=False))

alignments = Table('alignments', metadata,
                   Column('id', Integer, primary_key=True),
                   Column('name', String(20), unique=True, nullable=False),
                   Column('raw_score', Integer, nullable=False),
                   Column('binary_score', Integer, nullable=False),
                   Column('dimension_id', Integer, ForeignKey('dimensions.id'), nullable=False))

dimensions = Table('dimensions', metadata,
                   Column('id', Integer, primary_key=True),
                   Column('name', String(20), unique=True, nullable=False))

option_alignments = Table('option_alignments', metadata,
                          Column('question_id', Integer, ForeignKey('questions.id'), nullable=False),
                          Column('option_id', Integer, ForeignKey('options.id'), nullable=False),
                          Column('alignment_id', Integer, ForeignKey('alignments.id'), nullable=False),
                          Column('dimension_id', Integer, ForeignKey('dimensions.id'), nullable=False))

responses = Table('responses', metadata,
                  Column('id', Integer, primary_key=True),
                  Column('question_id', Integer, ForeignKey('questions.id'), nullable=False),
                  Column('respondent_id', Integer, ForeignKey('respondents.id'), nullable=False),
                  Column('option_id', Integer, ForeignKey('options.id'), nullable=False))
