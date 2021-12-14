# xurl

yet another link shortener service but purely functional.

## tech stack

| library     | version |
|-------------|---------|
| cats        | 2.7.0   |
| cats-effect | 3.3.0   |
| skunk       | 0.2.2   |
| redis4cats  | 1.0.0   |
| http4s      | 0.23.7  |
| circe       | 0.14.1  |
| weaver      | 0.7.9   |

## testing

before running tests, make sure your containers running:
`docker-compose up -d`

use `sbt test` command to run unit tests, `sbt it:test` for integration tests.
instead, you can use `sbt ci` alias which runs scalafmt checker agains `src/{main, test, it}` then runs both cases.

## todo

- better logging
- tracing & metrics
- healthcheck endpoint
- pagination
- clear error messages
