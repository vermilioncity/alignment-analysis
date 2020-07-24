FROM node:lts-alpine3.11

RUN apk add --no-cache bash

WORKDIR /node
COPY package.json package-lock.json ./

RUN npm install webpack -g
RUN npm install webpack-cli -g
RUN npm install --no-progress --ignore-optional

WORKDIR /node/client

COPY . .

CMD ["npm", "run-script", "watch"]