Obligations API
========================
The Obligations API allows software packages to:

- retrieve obligations for a user's business income sources
- retrieve the crystallisation obligations for a user's Income Tax account
- retrieve the End of Period Statement obligations for a user's business income sources

## Requirements

- Scala 2.13.x
- Java 11
- sbt > 1.9.x
- [Service Manager 2](https://github.com/hmrc/sm2)

## Running the microservice

Run from the console using: `sbt run` (starts on port 7793 by default)

Start the service manager profile: `sm2 --start MTDFB_OBLIGATIONS`

## Run Tests

Run unit tests: `sbt test`

Run integration tests: `sbt it/test`

## Viewing Open API Spec (OAS) docs

To view documentation locally ensure the Obligations API is running, and run api-documentation-frontend:
`./run_local_with_dependencies.sh`
Then go to http://localhost:9680/api-documentation/docs/openapi/preview and use this port and version:
`http://localhost:7793/api/conf/3.0/application.yaml`

## Changelog

You can see our changelog [here](https://github.com/hmrc/income-tax-mtd-changelog/wiki)

## Support and Reporting Issues

You can create a GitHub issue [here](https://github.com/hmrc/income-tax-mtd-changelog/issues)

## API Reference / Documentation

Available on
the [HMRC Developer Hub](https://developer.service.hmrc.gov.uk/api-documentation/docs/api/service/obligations-api/2.0)

## License

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
