FROM ibm-semeru-runtimes:open-21-jdk AS builder
MAINTAINER William Brawner <me@wbrawner.com>

COPY --chown=1000:1000 . /home/ubuntu/src
WORKDIR /home/ubuntu/src
RUN /home/ubuntu/src/gradlew --console=plain --no-daemon shadowJar

FROM ibm-semeru-runtimes:open-21-jre
EXPOSE 8080
COPY --from=builder --chown=1000:1000 /home/ubuntu/src/app/build/libs/twigs.jar twigs.jar
USER ubuntu
CMD /opt/java/openjdk/bin/java $JVM_ARGS -jar /twigs.jar
