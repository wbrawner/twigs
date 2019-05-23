FROM openjdk:8-jdk
MAINTAINER Billy Brawner <billy@wbrawner.com>
ADD . /usr/share/budget/budget-api
WORKDIR /usr/share/budget/budget-api
ENTRYPOINT ["/usr/share/budget/budget-api/mvnw", "spring-boot:run"]
