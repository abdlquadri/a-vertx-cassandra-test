FROM java:8-jre

ENV VERTICLE_FILE build/libs/pastes-3.3.3-fat.jar

# Set the location of the verticles
ENV VERTICLE_HOME /usr/verticles

EXPOSE 8080

COPY $VERTICLE_FILE $VERTICLE_HOME/
COPY config/config_docker.json $VERTICLE_HOME/

WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["java -jar pastes-3.3.3-fat.jar -conf config_docker.json"]