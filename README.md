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
instead, you can use `sbt ci` alias which runs scalafmt and scalafix checker against `src/{main, test, it}` then runs both cases.

## build and run

after running `sbt pack`, you will see there is an executable shell located in `target/pack/bin/xurl`. you can easily run the application by just calling this file.

## endpoints

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

## todo

- basen character uniqueness tests
- better logging
- tracing & metrics
- ~~healthcheck endpoint~~ [PR#2](https://github.com/brsyuksel/xurl/pull/2)
- pagination
- clear error messages
