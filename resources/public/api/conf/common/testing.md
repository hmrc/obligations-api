You can use the sandbox environment to <a href="/api-documentation/docs/testing">test this API</a>. You can use
the <a href="/api-documentation/docs/api/service/api-platform-test-user/1.0">Create Test User API</a> or it's frontend
service to create test users.

It may not be possible to test all scenarios in the sandbox. You can test some scenarios by passing the
Gov-Test-Scenario header. Documentation for each endpoint includes a **Test data** section, which explains the scenarios
that you can simulate using the Gov-Test-Scenario header.

If you have a specific testing need that is not supported in the sandbox, contact <a href="/developer/support">our
support team</a>.

Some APIs may be marked \[test only\]. This means that they are not available for use in production and may change.

### Dynamic

Some endpoints support DYNAMIC gov test scenarios.
The response is dynamic based on the request parameters:

- Retrieve Income Tax (Self Assessment) Income and Expenditure Obligations
- Retrieve Income Tax (Self Assessment) End of Period Statement Obligations
- Retrieve Income Tax (Self Assessment) Final Declaration Obligations

You can use the Sandbox environment to [test this API](/api-documentation/docs/testing). 
You can test different scenarios in the Sandbox by passing the Gov-Test-Scenario header. The Sandbox also allows you to perform 
[stateful and dynamic testing](https://developer.service.hmrc.gov.uk/guides/income-tax-mtd-end-to-end-service-guide/documentation/how-to-integrate.html#sandbox-testing) 
for some APIs. The ‘Test data’ section under each endpoint explains the test scenarios that can be simulated.

If you have a specific testing need that is not supported in the Sandbox, 
[contact our Software Developers Support Team](https://developer.service.hmrc.gov.uk/developer/support). 
Some endpoints may be marked ‘[test only]’, which means that they are not available for use in Production and may change.