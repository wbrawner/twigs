FROM ibm-semeru-runtimes:open-21-jdk as builder
MAINTAINER William Brawner <me@wbrawner.com>

RUN groupadd --system --gid 1000 gradle \
    && useradd --system --gid gradle --uid 1000 --shell /bin/bash --create-home gradle

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN /home/gradle/src/gradlew --console=plain --no-daemon shadowJar

FROM ibm-semeru-runtimes:open-21-jre
EXPOSE 8080
RUN groupadd --system --gid 1000 twigs \
    && useradd --system --gid twigs --uid 1000 --create-home twigs
COPY --from=builder --chown=twigs:twigs /home/gradle/src/app/build/libs/twigs.jar twigs.jar
USER twigs
CMD /opt/java/openjdk/bin/java $JVM_ARGS -jar /twigs.jar
