FROM ruby:2.5.0-stretch

RUN mkdir -p /opt
WORKDIR /opt

RUN apt-get update
RUN apt-get install gnupg curl openssl bash wget -y

ENV DOCKERIZE_VERSION v0.6.0
RUN wget https://github.com/jwilder/dockerize/releases/download/$DOCKERIZE_VERSION/dockerize-alpine-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
    && tar -C /usr/local/bin -xzvf dockerize-alpine-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
    && rm dockerize-alpine-linux-amd64-$DOCKERIZE_VERSION.tar.gz

RUN gem install stomp

COPY run.rb .
COPY start.sh .
RUN chmod u+x start.sh

ENTRYPOINT ["/opt/start.sh", "run.rb"]
CMD ["run.rb"]