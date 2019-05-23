FROM openjdk:8-jdk
MAINTAINER Billy Brawner <billy@wbrawner.com>
ADD . /usr/share/budget/budget-api
WORKDIR /usr/share/budget/budget-api
RUN /usr/share/budget/budget-api/mvnw -Dmaven.test.skip=true package
ENTRYPOINT ["/usr/bin/java", "-jar", "/usr/share/budget/budget-api/target/budget-api.jar"]
