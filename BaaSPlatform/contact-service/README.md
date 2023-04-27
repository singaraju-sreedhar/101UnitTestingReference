# Contact Service APIs
## Prerequisites
 In order to build the project, you will have to install the following:

* [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html) and [Maven](https://maven.apache.org/download.cgi
* IDE With [lombok enable](https://github.com/101digital/spring-boot-template/wiki/FakeJWT-locally/IDE-Setting)
* [Familiar with git command](https://git-school.github.io/visualizing-git/)
* [Familiar with maven command](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html)
## Need to read before start
See [101digital best practice](https://github.com/101digital/spring-boot-template/wiki)

## Prepare
* [Maven setting](https://github.com/101digital/spring-boot-template/wiki/maven-setup) in ~/.m2/setting.xml or C:\Users\<LoginUser>\.m2\setting.xml
* [Jenkins setting](https://github.com/101digital/spring-boot-template/wiki/Jenkin-setup)

## Local config
This configuration use for local development only.[SHOUD NOT] commit to git using [.gitignore](https://git-scm.com/docs/gitignore)

* Spring boot config ./config/application.yml
```yaml
server.port: 8074
security.jwt.enable: false
logging.level:
  io.marketplace: TRACE
```
[more..](https://github.com/101digital/spring-boot-template/wiki)

* Fake JWT for local environment  ./src/main/java/io/marketplace/services/service_name/config/LocalAutoConfiguration.java [LocalAutoConfiguration.java](https://github.com/101digital/spring-boot-template/wiki/FakeJWT-locally)

## Run application locally
```bash
mvn spring-boot:run
```
Verify following url:
* http://localhost:8074/swagger-ui.htm
* http://localhost:8074/actuator
* http://localhost:8074/actuator/prometheus

## Update Service Name following 101 naming convention
<i>Replace all service_name to *realname* </i>
* [change api-docs setting ](https://github.com/101digital/spring-boot-template/wiki/Api-Doc-Setting)


## Do deployment
Notes: In 101digital sanbox#eks. service port is 8080 and management port is 8081
```bash
git checkout -b develop
git push origin develop
```
* Verify using 101digital Jenkins https://jenkins.101digital.io/
* Verify api doc portal https://101digital.oneapi.world/apidocs/

## Start implement spring-boot rest api with API-FIRST approarch
* Change API spec file: src/main/resource/META-INF/api.yml
* Run spring boot again with maven `maven spring-boot:run`
* Execute api using swagger-ui: http://localhost:8074/swagger-ui.htm
* Implement api delegate: src/main/java/io/marketplace/services/service_name/api/delegate/
* Implement service layer: src/main/java/io/marketplace/services/service_name/services/

Notes: Move all business to service layer and keep in mind that api/delegate cani be changed anytime
### Changlenge :
* Reusable Data Object
* Java type mapping: `integer -> Long` `number --> BigDecimal`
 
[Read more about API-FIRST](https://github.com/101digital/spring-boot-template/wiki) <br/>
https://reflectoring.io/spring-boot-openapi/

## Mandatory for 101 developer
### Using 101 logging framework
** Important**:
Using
```java
import io.marketplace.commons.logging.Logger;
import io.marketplace.commons.logging.LoggerFactory;
```
instead of other logging framework.
```java
//-- SLf4j
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//---Log4J ---------
import org.apache.log4j.Logger;
//--------------

//-- Apache commong loging
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//-- JAVA common logging
import java.util.logging.*
```
*[marketplace-logging-common](https://github.com/101digital/marketplace-commons/tree/master/marketplace-logging-common)
### Error handling
* All error code in application should [list here](https://docs.google.com/spreadsheets/d/162QSo5D8EVtvUk4U61-egRsURTZKkAQh0IVKgWkBb70/edit#gid=0) 
* https://github.com/101digital/spring-boot-template/wiki#error-handling
* Use exception pattern to handle failt respons [marketplace-exception-common](https://github.com/101digital/marketplace-commons/tree/master/marketplace-exception-common)

### Changing the service name
* once the spring boot application is clone into a new service, it contains the default service name which is service_name. shell script has been created to convert the default name to the actual service name. Please run the https://github.com/101digital/spring-boot-template/blob/master/change-service-name.sh in git bash terminal.
* steps to run the shell script. Assume the new service name is example-demo-service
* run : sh change-service-name.sh example-demo (excluding the -service)
* this will replace the service_name (file name, folder name, file contents) with actual service name
* resolve any compilation errors (if your new service has multiple worlds with - , this will give compilation errors as - not allowed in the package name)
* run rm -f .git/index
