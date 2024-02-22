FROM openjdk:11 as builder

RUN apt-get update && apt-get install -y wget
RUN wget https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein -O /usr/local/bin/lein && chmod a+x /usr/local/bin/lein

RUN apt-get install -y nodejs npm

WORKDIR /app
COPY alignment-analysis /app/alignment-analysis
RUN cd alignment-analysis && npx shadow-cljs compile app

FROM python:3.8

RUN apt-get update && pip install --upgrade pip

WORKDIR /app

COPY requirements.txt /app/requirements.txt

RUN pip install -r requirements.txt

COPY . /app

COPY --from=builder /app/alignment-analysis/resources/public/ /app/alignment_analysis/static/

CMD ["python", "-m", "alignment_analysis.manage", "--init_db", "run", "--host", "0.0.0.0", "--port", "5000"]