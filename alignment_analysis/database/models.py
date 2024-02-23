from sqlalchemy import Column, Integer, ForeignKey, String
from sqlalchemy.orm import relationship
from alignment_analysis import db


respondent_team = db.Table('respondent_team',
                           db.Column('respondent_id',
                                     Integer, ForeignKey('respondent.id')),
                           db.Column('team_id',
                                     Integer, ForeignKey('team.id')))


class Respondent(db.Model):
    __tablename__ = 'respondent'

    id = Column(Integer, primary_key=True)
    name = Column(String(30))
    location_id = Column(Integer, ForeignKey('location.id'), nullable=False)
    teams = relationship("Team", secondary=respondent_team)

    def __repr__(self):
        return '<Respondent %r>' % self.name


class Team(db.Model):
    __tablename__ = 'team'

    id = Column(Integer, primary_key=True)
    name = Column(String(30), unique=True, nullable=False)
    parent_id = Column(Integer, ForeignKey('team.id'), nullable=True)
    respondents = relationship("Respondent",
                               secondary=respondent_team)

    def __repr__(self):
        return '<Team %r>' % self.name


class Location(db.Model):
    __tablename__ = 'location'

    id = Column(Integer, primary_key=True)
    name = Column(String(20), unique=True, nullable=False)

    def __repr__(self):
        return '<Location %r>' % self.name


class Question(db.Model):
    __tablename__ = 'question'

    id = Column(Integer, primary_key=True)
    name = Column(String(140), unique=True, nullable=False)

    def __repr__(self):
        return '<Question %r>' % self.name


class Option(db.Model):
    __tablename__ = 'option'

    id = Column(Integer, primary_key=True)
    name = Column(String(80), unique=True, nullable=False)
    question_id = Column(Integer, ForeignKey('question.id'), nullable=False)

    def __repr__(self):
        return '<Option %r>' % self.name


class Alignment(db.Model):
    __tablename__ = 'alignment'

    id = Column(Integer, primary_key=True)
    name = Column(String(20), unique=True, nullable=False)
    raw_score = Column(Integer, nullable=False)
    binary_score = Column(Integer, nullable=False)
    dimension_id = Column(Integer, ForeignKey('dimension.id'), nullable=False)


class Dimension(db.Model):
    __tablename__ = 'dimension'

    id = Column(Integer, primary_key=True)
    name = Column(String(20), nullable=False)


class OptionAlignment(db.Model):
    __tablename__ = 'option_alignment'

    id = Column(Integer, primary_key=True)
    question_id = Column(Integer, ForeignKey('question.id'), nullable=False)
    option_id = Column(Integer, ForeignKey('option.id'), nullable=False)
    alignment_id = Column(Integer, ForeignKey('alignment.id'), nullable=False)
    dimension_id = Column(Integer, ForeignKey('dimension.id'), nullable=False)

    def __repr__(self):
        return '<Option Alignment %r, %r>' % (self.alignment_id,
                                              self.option_id)


class Response(db.Model):
    __tablename__ = 'response'

    id = Column(Integer, primary_key=True)
    question_id = Column(Integer, ForeignKey('question.id'), nullable=False)
    respondent_id = Column(Integer, ForeignKey('respondent.id'), nullable=False)
    option_id = Column(Integer, ForeignKey('option.id'), nullable=False)

    def __repr__(self):
        return '<Response: Respondent %r, Answer %r>' % (self.respondent_id,
                                                         self.option_id)
