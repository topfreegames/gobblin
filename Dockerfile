FROM anapsix/alpine-java:8_jdk

RUN mkdir -p /opt/gobblin/
WORKDIR /opt/gobblin/


RUN apk add --update curl snappy maven && \
  rm -rf /var/cache/apk/* && \
  curl -LO https://github.com/topfreegames/incubator-gobblin/releases/download/gobblin_0.11.0/gobblin-distribution-0.11.0.tar.gz && \
  tar -xf gobblin-distribution-0.11.0.tar.gz && \
  rm gobblin-distribution-0.11.0.tar.gz && \
  curl -L http://central.maven.org/maven2/com/amazonaws/aws-java-sdk/1.11.86/aws-java-sdk-1.11.86.jar > /opt/gobblin/gobblin-dist/lib/aws-java-sdk-1.11.86.jar && \
  curl -L http://central.maven.org/maven2/org/apache/hadoop/hadoop-aws/2.6.0/hadoop-aws-2.6.0.jar > /opt/gobblin/gobblin-dist/lib/hadoop-aws-2.6.0.jar && \
  apk del curl

RUN mkdir source
COPY . source
WORKDIR source
RUN mvn -T 4 -Dmaven.test.skip=true assembly:single && cp ./target/*.jar /opt/gobblin/gobblin-dist/lib/ && cd .. && rm -rf source
RUN mkdir -p /etc/opt/job-conf
RUN mkdir -p /home/gobblin/work-dir

VOLUME /home/gobblin/work-dir
VOLUME /etc/opt/job-conf

ENV GOBBLIN_WORK_DIR /home/gobblin/work-dir
ENV GOBBLIN_JOB_CONFIG_DIR /etc/opt/job-conf
ENV LOG_LEVEL WARN

WORKDIR /opt/gobblin/
ADD log4j-config.xml /home/gobblin/log4j-config.xml
COPY gobblin-standalone.properties /opt/gobblin/gobblin-dist/conf/gobblin-standalone.properties
COPY events.pull /etc/opt/job-conf/events.pull
COPY push.pull /etc/opt/job-conf/push.pull

CMD ["java", "-cp", "/opt/gobblin/gobblin-dist/lib/*", "-Dlog4j.configuration=file:/home/gobblin/log4j-config.xml", "-Dgobblin.logs.dir=/var/log/gobblin", "-Dorg.quartz.properties=/opt/gobblin/gobblin-dist/conf/quartz.properties", "gobblin.scheduler.SchedulerDaemon", "/opt/gobblin/gobblin-dist/conf/gobblin-standalone.properties"]
