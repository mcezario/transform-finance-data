# Getting Started
This application exposes a Rest endpoint that is responsible for downloading a ZIP file that contains multiple CSV files in it from an AWS S3 bucket, parsing the CSV files, transforming the content, and then finally, outputting the transformed content as the endpoint's response in json format.

### Guides
The following guides show how to use call the endpoint correctly:

* Start the application by running this command: `./mvnw spring-boot:run`
* Once the application starts, [call](http://localhost:8080/) the endpoint responsible transforming the content hosted in AWS S3

### Tests

* Tests can be run by executing this command: `./mvnw test`