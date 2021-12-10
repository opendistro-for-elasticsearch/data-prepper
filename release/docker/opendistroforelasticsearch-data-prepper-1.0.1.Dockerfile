FROM amazoncorretto:15-al2-full
ARG PIPELINE_FILEPATH
ARG CONFIG_FILEPATH
ARG JAR_FILE

ENV DATA_PREPPER_PATH /usr/share/data-prepper
ENV ENV_CONFIG_FILEPATH=$CONFIG_FILEPATH
ENV ENV_PIPELINE_FILEPATH=$PIPELINE_FILEPATH

# Update all packages
RUN yum update -y && yum clean all

RUN mkdir -p $DATA_PREPPER_PATH
RUN mkdir -p /var/log/data-prepper
COPY $JAR_FILE /usr/share/data-prepper/data-prepper.jar

WORKDIR $DATA_PREPPER_PATH
CMD java $JAVA_OPTS -jar data-prepper.jar ${ENV_PIPELINE_FILEPATH} ${ENV_CONFIG_FILEPATH}
