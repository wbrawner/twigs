FROM ghcr.io/graalvm/graalvm-ce:ol9-java17-22.3.0-b2 as builder
MAINTAINER William Brawner <me@wbrawner.com>

RUN groupadd --system --gid 1000 gradle \
    && useradd --system --gid gradle --uid 1000 --shell /bin/bash --create-home gradle

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN /home/gradle/src/gradlew --console=plain --no-daemon nativeCompile

FROM quay.io/centos/centos:stream9
EXPOSE 8080
RUN groupadd --system --gid 1000 twigs \
    && useradd --system --gid twigs --uid 1000 --create-home twigs
COPY --from=builder --chown=twigs:twigs /home/gradle/src/app/build/native/nativeCompile/twigs /usr/local/bin/twigs
USER twigs
CMD /usr/local/bin/twigs
