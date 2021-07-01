# Twigs Server

This is the backend application that powers the [Android](../../../twigs-android), [iOS](../../../twigs-ios), and [web](../../../twigs-web) applications for Twigs, a personal finance/budgeting app. None of these apps are complete, so expect bugs, and they are all in various stages of development, so expect some feature disparity between platforms.

## Prerequisites

- Go 1.16 or newer
- PostgreSQL 13 or newer
- (optional) Docker

## Running

Prior to running the app, make sure you have a PostgreSQL server running, with a database and user ready to go. To avoid the hassle of figuring out how to get it installed locally, using Docker is recommended, and a sample `docker-compose.yml` file is included in the root of the repository. If you already have a PostgreSQL server running, you can run the app from the command line:

    go run twigs.go

## Configuration

Some parameters of Twigs can be configured via environment variables:

Environment Variable|Default Value|Note
:---:|:---:|:---
`TWIGS_PORT`|`8080`|Port for web server to listen on
`TWIGS_DB_HOST`|`localhost`|PostgreSQL server host
`TWIGS_DB_PORT`|`5432`|PostgreSQL server port
`TWIGS_DB_NAME`|`twigs`|PostgreSQL database name
`TWIGS_DB_USER`|`twigs`|PostgreSQL database user
`TWIGS_DB_PASS`|`twigs`|PostgreSQL database password
`TWIGS_PW_SALT`||Salt to use for password, generated if empty or null

## Building

    go build

