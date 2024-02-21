FROM openjdk:11

RUN apt-get update && apt-get install -y python3 python3-pip

RUN wget https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein -O /usr/local/bin/lein
RUN chmod a+x /usr/local/bin/lein

WORKDIR /app

COPY . /app

# i have no idea why leinengen wants this
RUN git config --global user.email "rbruehlman@gmail.com"
RUN git config --global user.name "Rebecca Bruehlman"
RUN git config --global tag.gpgSign false

RUN cd alignment-analysis && lein release app && cd ..

RUN ln -s alignment-analysis/resources/public alignment_analysis/static

RUN pip intall -r requirements.txt

CMD ["python", "-m", "alignment_analysis.manage", "--init_db", "run", "--host", "0.0.0.0", "--port", "5000"]