Obligations API
========================
The Obligations API allows software packages to:

- retrieve obligations for a user's business income sources
- retrieve the crystallisation obligations for a user's Income Tax account
- retrieve the End of Period Statement obligations for a user's business income sources

## Requirements

- Scala 2.12.x
- Java 8
- sbt > 1.3.7
- [Service Manager](https://github.com/hmrc/service-manager)

## Running the microservice
Run from the console using: `sbt run` (starts on port 7793 by default)

Start the service manager profile: `sm --start MTDFB_OBLIGATIONS`
 
## Run Tests
Run unit tests: `sbt test`

Run integration tests: `sbt it:test`

## To view the RAML
To view documentation locally ensure the Obligations API is running, and run api-documentation-frontend:

```
./run_local_with_dependencies.sh
```

Then go to http://localhost:9680/api-documentation/docs/api/preview and use this port and version:

```
http://localhost:7793/api/conf/1.0/application.raml
```

## Changelog

You can see our changelog [here](https://github.com/hmrc/income-tax-mtd-changelog/wiki)

## Support and Reporting Issues

You can create a GitHub issue [here](https://github.com/hmrc/income-tax-mtd-changelog/issues)

## API Reference / Documentation 
Available on the [HMRC Developer Hub](https://https://developer.service.hmrc.gov.uk/api-documentation/docs/api/service/obligations-api/1.0)


## License
This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
