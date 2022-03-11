# xurl

![ci tests](https://github.com/brsyuksel/xurl/actions/workflows/ci.yml/badge.svg)

[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)

![Cats Friendly Badge](https://typelevel.org/cats/img/cats-badge-tiny.png) 

yet another link shortener service but purely functional.

## tech stack

| library     | version  |
|-------------|----------|
| cats        | 2.7.0    |
| cats-effect | 3.3.7    |
| skunk       | 0.3.1    |
| redis4cats  | 1.1.1    |
| http4s      | 0.23.10  |
| circe       | 0.14.1   |
| weaver      | 0.7.11   |

## testing

before running tests, make sure your containers running:
`docker-compose up -d`

use `sbt test` command to run unit tests, `sbt it:test` for integration tests.
instead, you can use `sbt ci` alias which runs scalafmt and scalafix checker against `src/{main, test, it}` then runs both cases.

## build and run

after running `sbt pack`, you will see there is an executable shell located in `target/pack/bin/xurl`. you can easily run the application by just calling this file.

## endpoints

- `GET /_health`

Returns the statuses of storage and cache services. Example response:

```json
{
    "storage": true,
    "cache": true
}
```

- `GET /api/v1/urls`

Returns all stored urls in database. Example response:

```json
[
    {
        "code": "Fb",
        "address": "https://httpbin.org/get?from=xurl2",
        "hit": 0,
        "created_at": "2021-12-14T16:03:00.968752"
    }
]
```

- `GET /api/v1/urls/<code>`

Returns the detail for a given code:

```json
{
    "code": "Fb",
    "address": "https://httpbin.org/get?from=xurl2",
    "hit": 0,
    "created_at": "2021-12-14T16:03:00.968752"
}
```

- `POST /v1/api/urls`

Shortens the given url and stores in db.

Example Request:
```json
{
    "url": "https://httpbin.org/get?from=xurl2"
}
```

Example Response:
```json
{
    "code": "Fb",
    "address": "https://httpbin.org/get?from=xurl2",
    "hit": 0,
    "created_at": "2021-12-14T16:03:00.968752"
}
```

- `GET /<code>`

Redirects the user to associated url for a given code.

- `GET /_prometheus/metrics`

Exposes Prometheus-compatible JVM and server' metrics

## grafana dashboard

you can access the grafana dashboard of the xurl instance by visiting `http://localhost:3000`.

the grafana' default credentials have been used so you can log in with **admin** for username and **admin** for password.

the dashboard name is `xurl`, you can also take a look community-driven `postgres` and `redis` dashboards to see metrics of them. 

## docker container

after building a docker image for the project by using a command like `docker build . -t xurl:latest`, you should create a container using the host network since the configuration is not updated:
```
docker run --rm --network host xurl:latest
```

> graalvm native image causes a bug on the metrics endpoint that are used by prometheus, the relevant endpoint somehow returns 500 for requests.

## todo

- basen character uniqueness tests
- better logging
- tracing
- ~~metrics~~ [PR#13](https://github.com/brsyuksel/xurl/pull/13) [PR#17](https://github.com/brsyuksel/xurl/pull/17) [PR#18](https://github.com/brsyuksel/xurl/pull/18)
- ~~healthcheck endpoint~~ [PR#2](https://github.com/brsyuksel/xurl/pull/2)
- pagination
- clear error messages
- ~~github actions for ci~~ [PR#9](https://github.com/brsyuksel/xurl/pull/9)
- scala 3
- ~~graalvm native image~~ [PR#30](https://github.com/brsyuksel/xurl/pull/30)
- [exp] buld native image with musl
- ~~load testing~~ [PR#32](https://github.com/brsyuksel/xurl/pull/32)
