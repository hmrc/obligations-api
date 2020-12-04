Obligations API
========================
The Obligations API allows software packages to:


- retrieve obligations for a user's business income sources
- retrieve the crystallisation obligations for a user's Income Tax account
- retrieve the End of Period Statement obligations for a user's business income sources


## Requirements
- Scala 2.12.x
- Java 8
- sbt 1.3.13
- [Service Manager](https://github.com/hmrc/service-manager)

## Run Microservice
To run the microservice from console, use `sbt run`

To start the service manager profile: `sm --start MTDFB_OBLIGATIONS`
 
## Run Tests
```
sbt test
sbt it:test
```

## Documentation
To view documentation locally ensure the Obligations API is running, and run api-documentation-frontend:
`./run_local_with_dependencies.sh`

Then go to http://localhost:9680/api-documentation/docs/api/preview and use this port and version:
`http://localhost:7793/api/conf/1.0/application.raml`

## API Reference / Documentation 
Available on the [HMRC Developer Hub](https://developer.service.hmrc.gov.uk/api-documentation)

## Reporting Issues
You can create a GitHub issue [here](https://github.com/hmrc/individuals-expenses-api/issues)

## License
This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
