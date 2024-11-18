# wso2-api-revisioner

### Recommendations for building
- Java 1.8+
- Maven 3.5.4+

### Tested APIM Versions
- 4.3.0

### Use case

Today I came to you with a scenario where I needed to achieve the following objective.

- Had a gateway named 'ABC' in Dev_3XX environment.
- Deployed APIs to the 'ABC' gateway in that environment.
- Migrated the WSO2 API Manager from 3.X.X to 4.X.X.
- Introduced a new gateway 'XYZ' for the 4.X.X server running on Dev_4XX environment.
- Deployed APIs to the 'XYZ' gateway in that environment.
- Needed to deprecate the 'ABC' gateway in that environment.

This would be an easy step if you have a couple of APIs where you can easily undeploy the APIs for the 'ABC' environment. However, it won't be practical to undeploy if you have a large number of APIs (e.g. 50) deployed to that environment. Therefore, I came up with a simple tool undeploy all the APIs in a respective environment. Let's talk a bit about the tool.

### Tool directory structure:

- <TOOL_HOME>/wso2-api-revision-undeployer-tool-0.0.1.jar
- <TOOL_HOME>/resources/integration.properties

### Build to tool:

- Download and extract the tool.
- Go to the wso2-api-revision-undeployer-master directory (e.g. <SRC_HOME>).
- Run the following command.
`mvn clean install`
- The built jar file will be available in the <SRC_HOME>/target directory.
- Create a directory structure similar to the tool directory structure described above.

The following are the properties that you need to configure in the integration.properties file.

application.host=<PUBLISHER_URL>
application.transport.port=<PUBLISHER_PORT>
#### A client key to generate a token
application.client.key=<CLIENT_KEY>
#### Respective client secret to generate a token
application.client.secret=<CLIENT_SECRET>
application.username=<ADMIN_USERNAME>
application.password=<ADMIN_PASSWORD>
#### Should be higher that the number of APIs you have
application.api.limit=<API_LIMIT>
#### compatible for WSO2 APIM 4.2.0, 4.3.0, 4.4.0
application.publisher.context=api/am/publisher/v4/apis
#### compatible for WSO2 APIM 4.1.0
application.publisher.context=api/am/publisher/v3/apis
#### compatible for WSO2 APIM 4.0.0
application.publisher.context=api/am/publisher/v2/apis
#### e.g. XYZ
application.new.gw.name=<NEW_GW_NAME>
#### e.g. ABC
application.old.gw.name=<OLD_GW_NAME>

Add both the APIM gateway configurations to the <APIM_HOME>/repository/conf/deployment.toml file.

`[[apim.gateway.environment]]
name = "ABC"
type = "hybrid"
gateway_type = "Regular"
provider = "wso2"
display_in_api_console = true
description = "Dev_3XX GW"
show_as_token_endpoint_url = true
service_url = "https://localhost:${mgt.transport.https.port}/services/"
username= "${admin.username}"
password= "${admin.password}"
ws_endpoint = "ws://localhost:9099"
wss_endpoint = "wss://localhost:8099"
http_endpoint = "http://localhost:${http.nio.port}"
https_endpoint = "https://localhost:${https.nio.port}"
websub_event_receiver_http_endpoint = "http://localhost:9021"
websub_event_receiver_https_endpoint = "https://localhost:8021"`

`[[apim.gateway.environment]]
name = "XYZ"
type = "hybrid"
gateway_type = "Regular"
provider = "wso2"
display_in_api_console = true
description = "Dev_4XX"
show_as_token_endpoint_url = true
service_url = "https://localhost:${mgt.transport.https.port}/services/"
username= "${admin.username}"
password= "${admin.password}"
ws_endpoint = "ws://localhost:9099"
wss_endpoint = "wss://localhost:8099"
http_endpoint = "http://localhost:${http.nio.port}"
https_endpoint = "https://localhost:${https.nio.port}"
websub_event_receiver_http_endpoint = "http://localhost:9021"
websub_event_receiver_https_endpoint = "https://localhost:8021"`

`[apim.sync_runtime_artifacts.gateway]
gateway_labels =["ABC", "XYZ"]`

Now let's restart the APIM server. Once the APIM is successfully restarted, let's run the APIM tool as follows.

- java -jar wso2-api-revision-undeployer-tool-0.0.1.jar

Now log into the publisher and check whether the revisions related to the deprecated gateway are now undeployed.

Once confirmed, you can remove the [[apim.gateway.environment]] configuration for the deprecated gateway (ABC) and remove 'ABC' from the [apim.sync_runtime_artifacts.gateway] configuration.

If you have two deprecated gateways, run the tool one deprecated gateway at a time.