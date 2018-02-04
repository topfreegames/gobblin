FROM maven:3-jdk-8-alpine

ENV RELEASE_VERSION "0.8.0"

RUN mkdir -p /opt/gobblin/
WORKDIR /opt/gobblin/


RUN apk add --update curl && \
  rm -rf /var/cache/apk/* && \
  curl -LO https://github.com/linkedin/gobblin/releases/download/gobblin_0.8.0/gobblin-distribution-0.8.0.tar.gz && \
  tar -xf gobblin-distribution-$RELEASE_VERSION.tar.gz && \
  rm gobblin-distribution-$RELEASE_VERSION.tar.gz && \
  curl -L http://central.maven.org/maven2/com/amazonaws/aws-java-sdk/1.11.86/aws-java-sdk-1.11.86.jar > /opt/gobblin/gobblin-dist/lib/aws-java-sdk-1.11.86.jar && \
  curl -L http://central.maven.org/maven2/com/tfgco/eventsgateway/1.0.0/eventsgateway-gobblin-1.0.0.jasr > /opt/gobblin/gobblin-dist/lib/eventsgateway-gobblin-1.0.0.jar && \
  curl -L http://central.maven.org/maven2/com/tfgco/avro/1.0.0/avro-1.0.0.jar > /opt/gobblin/gobblin-dist/lib/avro-1.0.0.jar && \
  apk del curl

RUN mkdir source
COPY . source
WORKDIR source
RUN mvn -Dmaven.test.skip=true package && cp ./target/*.jar /opt/gobblin/gobblin-dist/lib/ && cd .. && rm -rf source
RUN mkdir -p /etc/opt/job-conf
RUN mkdir -p /home/gobblin/work-dir

VOLUME /home/gobblin/work-dir
VOLUME /etc/opt/job-conf

ENV GOBBLIN_WORK_DIR /home/gobblin/work-dir
ENV GOBBLIN_JOB_CONFIG_DIR /etc/opt/job-conf
ENV LOG_LEVEL WARN

ADD log4j-config.xml /home/gobblin/log4j-config.xml
COPY gobblin-standalone.properties /opt/gobblin/gobblin-dist/conf/gobblin-standalone.properties
COPY events.pull /etc/opt/job-conf/events.pull
CMD ["java", "-cp", "/opt/gobblin/gobblin-dist/lib/*", "-Dlog4j.configuration=file:/home/gobblin/log4j-config.xml", "-Dgobblin.logs.dir=/var/log/gobblin", "-Dorg.quartz.properties=/opt/gobblin/gobblin-dist/conf/quartz.properties", "gobblin.scheduler.SchedulerDaemon", "/opt/gobblin/gobblin-dist/conf/gobblin-standalone.properties"]