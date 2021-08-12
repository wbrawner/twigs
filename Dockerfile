FROM openjdk:14-jdk as builder
MAINTAINER William Brawner <me@wbrawner.com>

FROM adoptopenjdk:openj9
EXPOSE 8080
COPY app/build/libs/twigs.jar twigs.jar
CMD /opt/java/openjdk/bin/java $JVM_ARGS -jar /twigs.jar
