FROM openjdk:14-jdk as builder
MAINTAINER William Brawner <me@wbrawner.com>

RUN groupadd --system --gid 1000 gradle \
    && useradd --system --gid gradle --uid 1000 --shell /bin/bash --create-home gradle

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN /home/gradle/src/gradlew --console=plain --no-daemon bootJar

FROM adoptopenjdk:openj9
EXPOSE 8080
COPY --from=builder /home/gradle/src/api/build/libs/api-0.0.1-SNAPSHOT.jar twigs-api.jar
CMD /opt/java/openjdk/bin/java $JVM_ARGS -jar /twigs-api.jar
