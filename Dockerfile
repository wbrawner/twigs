FROM openjdk:8-jdk as builder
MAINTAINER Billy Brawner <billy@wbrawner.com>

RUN groupadd --system --gid 1000 maven \
    && useradd --system --gid maven --uid 1000 --shell /bin/bash --create-home maven \
    && mkdir /home/maven/.m2 \
    && chown --recursive maven:maven /home/maven \
    && ln -s /home/maven/.m2 /root/.m2

COPY --chown=maven:maven . /home/maven/src
WORKDIR /home/maven/src
RUN /home/maven/src/mvnw -DskipTests package

FROM openjdk:8-jdk-slim
EXPOSE 8080
COPY --from=builder /home/maven/src/target/budget-server-0.0.1-SNAPSHOT.jar budget-server.jar
ENTRYPOINT ["/usr/local/openjdk-8/bin/java", "-Xmx3g", "-jar", "/budget-server.jar"]


