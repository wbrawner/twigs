# Twigs

Twigs is a personal finance application tailored to individuals and small groups that want robust budgeting features
without needing to pay a monthly subscription.

## Prerequisites

- JDK 17 or newer
- PostgreSQL 13 or newer
- (optional) Docker

## Running

Prior to running the app, make sure you have a PostgreSQL server running, with a database and user ready to go. To avoid
the hassle of figuring out how to get it installed locally, using Docker is recommended, and a
sample `docker-compose.yml` file is included in the root of the repository. If you already have a PostgreSQL server
running, you can run the app from the command line with gradle:

    ./gradlew run

## Configuration

Some parameters of Twigs can be configured via environment variables:

| Environment Variable | Default Value | Note                                                    |
|:--------------------:|:-------------:|:--------------------------------------------------------|
|     `TWIGS_PORT`     |    `8080`     | Port for web server to listen on                        |
|   `TWIGS_DB_HOST`    |  `localhost`  | PostgreSQL server host                                  |
|   `TWIGS_DB_PORT`    |    `5432`     | PostgreSQL server port                                  |
|   `TWIGS_DB_NAME`    |    `twigs`    | PostgreSQL database name                                |
|   `TWIGS_DB_USER`    |    `twigs`    | PostgreSQL database user                                |
|   `TWIGS_DB_PASS`    |    `twigs`    | PostgreSQL database password                            |
|   `TWIGS_PW_SALT`    |               | Salt to use for password, generated if empty or null    |
|  `TWIGS_SMTP_FROM`   |               | From email address for automated emails sent from Twigs |
|  `TWIGS_SMTP_HOST`   |               | SMTP server host for sending emails                     |
|  `TWIGS_SMTP_PORT`   |               | SMTP server port for sending emails                     |     
|  `TWIGS_SMTP_USER`   |               | SMTP server username for sending emails                 |
|  `TWIGS_SMTP_PASS`   |               | SMTP server password for sending emails                 |

## Building

Building the app is also handled with gradle:

    ./gradlew shadowJar

## License

Copyright (C) 2019-2024 William Brawner

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
