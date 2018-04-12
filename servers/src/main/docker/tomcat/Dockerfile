FROM ${images.owner}/spring-activemq-demo-base:${project.version}

USER root

ENV JPDA_ADDRESS=8000
ENV JPDA_TRANSPORT=dt_socket
ENV JPDA_SUSPEND=y

RUN mkdir -p /opt
WORKDIR /opt
RUN curl -s -o tomcat.tar.gz ${tomcat.download.url}

RUN tar xvzf tomcat.tar.gz && rm tomcat.tar.gz
RUN mv apache-* tomcat
RUN rm -Rf /opt/tomcat/webapps/*

WORKDIR /opt/tomcat/

COPY start.sh .
RUN chmod u+x start.sh

COPY logging.properties /opt/tomcat/conf/

ENTRYPOINT ["/opt/tomcat/start.sh"]
CMD ["run"]