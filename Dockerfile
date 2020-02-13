FROM openjdk:11-jdk as builder
MAINTAINER William Brawner <me@wbrawner.com>

RUN groupadd --system --gid 1000 gradle \
    && useradd --system --gid gradle --uid 1000 --shell /bin/bash --create-home gradle

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN /home/gradle/src/gradlew bootJar

FROM openjdk:11-jdk-slim
EXPOSE 8080
COPY --from=builder /home/gradle/src/build/libs/budget-server-*.jar budget-api.jar
ENTRYPOINT ["/usr/local/openjdk-11/bin/java", "-Xmx256M", "-jar", "/budget-api.jar"]
