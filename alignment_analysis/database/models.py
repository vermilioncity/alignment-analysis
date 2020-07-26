from sqlalchemy import Column, Integer, ForeignKey, String
from sqlalchemy.ext.associationproxy import association_proxy
from sqlalchemy.orm import relationship
from alignment_analysis import db


class Respondent(db.Model):
    __tablename__ = 'respondents'

    id = Column(Integer, primary_key=True)
    name = Column(String(30))
    location_id = Column(Integer, ForeignKey('locations.id'), nullable=False)
    team_id = Column(Integer, ForeignKey('teams.id'), nullable=False)
    _location = relationship("Location", back_populates="_respondents")
    _teams = relationship("Team", back_populates="_respondents")

    location = association_proxy('_location', 'name')
    teams = association_proxy('_teams', 'name')

    def __repr__(self):
        return '<Respondent %r>' % self.name


class Team(db.Model):
    __tablename__ = 'teams'

    id = Column(Integer, primary_key=True)
    name = Column(String(30), unique=True, nullable=False)
    level = Column(Integer, nullable=False)
    parent_id = Column(Integer, ForeignKey('teams.id'), nullable=True)
    _respondents = relationship("Respondent", back_populates="_teams")

    respondents = association_proxy('_respondents', 'name')

    def __repr__(self):
        return '<Team %r>' % self.name


class Location(db.Model):
    __tablename__ = 'locations'

    id = Column(Integer, primary_key=True)
    name = Column(String(20), unique=True, nullable=False)
    _respondents = relationship("Respondent", back_populates="_location")

    respondents = association_proxy('_respondents', 'name')

    def __repr__(self):
        return '<Location %r>' % self.name


class Question(db.Model):
    __tablename__ = 'questions'

    id = Column(Integer, primary_key=True)
    name = Column(String(140), unique=True, nullable=False)

    _options = relationship("Option", back_populates="_question")
    options = association_proxy('_option', 'name')

    def __repr__(self):
        return '<Question %r>' % self.name


class Option(db.Model):
    __tablename__ = 'options'

    id = Column(Integer, primary_key=True)
    name = Column(String(80), unique=True, nullable=False)
    question_id = Column(Integer, ForeignKey('questions.id'), nullable=False)
    _question = relationship("Question", back_populates="_options")

    question = association_proxy('_question', 'name')

    def __repr__(self):
        return '<Option %r>' % self.name


class Alignment(db.Model):
    __tablename__ = 'alignments'

    id = Column(Integer, primary_key=True)
    name = Column(String(20), unique=True, nullable=False)
    raw_score = Column(Integer, nullable=False)
    binary_score = Column(Integer, nullable=False)
    dimension_id = Column(Integer, ForeignKey('dimensions.id'), nullable=False)


class Dimension(db.Model):
    __tablename__ = 'dimensions'

    id = Column(Integer, primary_key=True)
    name = Column(String(20), nullable=False)


class OptionAlignment(db.Model):
    __tablename__ = 'option_alignments'

    id = Column(Integer, primary_key=True)
    question_id = Column(Integer, ForeignKey('questions.id'), nullable=False)
    option_id = Column(Integer, ForeignKey('options.id'), nullable=False)
    alignment_id = Column(Integer, ForeignKey('alignments.id'), nullable=False)
    dimension_id = Column(Integer, ForeignKey('dimensions.id'), nullable=False)

    def __repr__(self):
        return '<Option Alignment %r, %r>' % (self.alignment_id,
                                              self.option_id)


class Response(db.Model):
    __tablename__ = 'responses'

    id = Column(Integer, primary_key=True)
    question_id = Column(Integer, ForeignKey('questions.id'), nullable=False)
    respondent_id = Column(Integer, ForeignKey('respondents.id'), nullable=False)
    option_id = Column(Integer, ForeignKey('options.id'), nullable=False)

    def __repr__(self):
        return '<Response: Respondent %r, Answer %r>' % (self.respondent_id,
                                                         self.option_id)
