FROM ${images.owner}/spring-activemq-demo-base:${project.version}

RUN mkdir -p /opt
WORKDIR /opt

RUN curl -s -o activemq.tar.gz http://apache.mirror.rafal.ca/activemq/${amq.version}/apache-activemq-${amq.version}-bin.tar.gz
RUN curl -s -o activemq.asc https://www.apache.org/dist/activemq/${amq.version}/apache-activemq-${amq.version}-bin.tar.gz.asc
RUN curl -s -o KEYS https://www.apache.org/dist/activemq/KEYS
RUN gpg --import KEYS && gpg --verify activemq.asc activemq.tar.gz && rm activemq.asc KEYS
RUN tar xzf activemq.tar.gz && rm activemq.tar.gz
RUN mv apache-* activemq

WORKDIR /opt/activemq/lib
COPY mssql-jdbc.jar .
COPY commons-dbcp.jar .
COPY commons-pool.jar .
RUN curl -s -o postgresql.jar https://jdbc.postgresql.org/download/postgresql-9.4.1212.jre6.jar

WORKDIR /opt/activemq

COPY start.sh .
RUN chmod u+x start.sh

ENTRYPOINT ["/opt/activemq/start.sh"]
CMD ["console"]



