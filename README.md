# Twigs Server

This is the backend application that powers the [Android](https://github.com/wbrawner/twigs-android), [iOS](https://github.com/wbrawner/twigs-ios), and [web](https://github.com/wbrawner/twigs-web) applications for Twigs, a personal finance/budgeting app. 

## Prerequisites

- JDK 14 or newer
- MySQL 5.7 or newer
- (optional) Docker

## Running

Prior to running the app, make sure you have a MySQL server running, with a database and user ready to go. To avoid the hassle of figuring out how to get it installed locally, using Docker is recommended, and a sample `docker-compose.yml` file is included in the root of the repository. If you already have a MySQL server running, you can run the app from the command line with gradle:

    ./gradlew bootRun

By default, twigs will try to connect to the `budget` database on `localhost`, using `budget` as the user and password. To change these values, you can modify the `api/src/main/resources/application.properties` file (but don't commit it!), or better yet, set the appropriate environment variables using the uppercase names and replacing the `.`s with `_`s. For example, to change the `spring.datasource.username` property (the database username), you could set the value in an environment variable called `SPRING_DATASOURCE_USERNAME`.

## Building

Building the app is also handled with gradle:

    ./gradlew bootJar

