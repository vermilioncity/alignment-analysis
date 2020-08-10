FROM python:3.7

WORKDIR /src

COPY requirements.txt .
RUN pip install -r requirements.txt

COPY alignment_analysis alignment_analysis/

CMD ["python", "-m", "alignment_analysis.manage", "--init_db", "--debug", "run", "--host", "0.0.0.0", "--port", "5000"]