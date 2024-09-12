import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MainTests {

    @Test
    public void convertQueuesToEndpointsTest() throws JsonProcessingException {
        String body = """
                {
                  "routesList":[
                    {
                      "uuid": "32354541274",
                      "name": "Elend",
                      "rule": "emxSourceSystem==\\"cars\\" && emxSourceEnvironment==\\"stage\\" && emxDatatype==\\"vendor\\"",
                      "description": "",
                      "enabled": true,
                      "queues": ["emx-to-scms-stage", "emx-trash", "emx-to-archive-core", "emx-to-emx-healthcheck", "emx-to-cfis-dev:skim", "scms#test", "emx-to-cfis-test;"],
                      "createdDate": "2023-03-28",
                      "modifiedDate": "2023-03-28"
                    }
                  ]
                }
                """;
        var routes = Main.convertQueuesToEndpoints(body);
        var expectedQueues = List.of("scms#stage", "emx-core-trash#dev", "emx-core-archive#dev", "emx-core-healthcheck#dev", "cfis#dev%3Askim", "scms#test", "cfis#test%3B");
        assertThat(routes).singleElement().satisfies(route -> {
            assertThat(route.queues()).hasSize(7);
            assertThat(route.queues()).isEqualTo(expectedQueues);
            assertThat(route.name()).isEqualTo("Elend");
            assertThat(route.uuid()).isEqualTo("32354541274");
            assertThat(route.description()).isEmpty();
            assertThat(route.enabled()).isTrue();
            assertThat(route.rule()).contains("emxSourceEnvironment");
            assertThat(route.createdDate()).isEqualTo("2023-03-28");
            assertThat(route.modifiedDate()).isEqualTo("2023-03-28");
        });
    }

    @Test
    public void convertEndpointToQueuesTest() throws IOException {
        String body = """
                {
                  "routesList":[
                    {
                      "uuid": "32354541274",
                      "name": "Elend",
                      "rule": "emxSourceSystem==\\"cars\\" && emxSourceEnvironment==\\"stage\\" && emxDatatype==\\"vendor\\"",
                      "description": "",
                      "enabled": true,
                      "queues": ["scms#stage", "emx-core-trash#dev", "emx-core-archive#dev", "emx-core-healthcheck#dev", "cfis#dev%3Askim", "emx-to-scms-test", "cfis#test%3B"],
                      "createdDate": "2023-03-28",
                      "modifiedDate": "2023-03-28"
                    }
                  ]
                }
                """;
        var routes = Main.convertEndpointsToQueues(body.getBytes(StandardCharsets.UTF_8));
        var expectedQueues = List.of("emx-to-scms-stage", "emx-trash", "emx-to-archive-core", "emx-to-emx-healthcheck", "emx-to-cfis-dev:skim", "emx-to-scms-test", "emx-to-cfis-test;");
        assertThat(routes).singleElement().satisfies(route -> {
            assertThat(route.queues()).hasSize(7);
            assertThat(route.queues()).isEqualTo(expectedQueues);
            assertThat(route.name()).isEqualTo("Elend");
            assertThat(route.uuid()).isEqualTo("32354541274");
            assertThat(route.description()).isEmpty();
            assertThat(route.enabled()).isTrue();
            assertThat(route.rule()).contains("emxSourceEnvironment");
            assertThat(route.createdDate()).isEqualTo("2023-03-28");
            assertThat(route.modifiedDate()).isEqualTo("2023-03-28");
        });
    }

    @Test
    public void convertRulesToEndpointParts() throws JsonProcessingException {
        String body = """
                {
                  "routesList":[
                    {
                      "uuid": "32354541274",
                      "name": "Elend",
                      "rule": "emxSourceSystem==\\"cars\\" && emxSourceEnvironment==\\"stage\\" && emxDatatype==\\"vendor\\"",
                      "description": "",
                      "enabled": true,
                      "queues": ["scms#stage", "emx-core-trash#dev"],
                      "createdDate": "2023-03-28",
                      "modifiedDate": "2023-03-28"
                    },
                    {
                      "uuid": "32354541",
                      "name": "Rand",
                      "rule": "endpoint==\\"cars#stage/vendor\\"",
                      "description": "",
                      "enabled": true,
                      "queues": ["scms#stage"],
                      "createdDate": "2023-03-28",
                      "modifiedDate": "2023-03-28"
                    }
                  ]
                }
                """;
        var routes = Main.convertRuleToEndpointParts(body);
        assertThat(routes).hasSize(2);
        var elendRoute = routes.get(0);

        assertThat(elendRoute.queues()).hasSize(2);
        assertThat(elendRoute.queues()).isEqualTo(List.of("scms#stage", "emx-core-trash#dev"));
        assertThat(elendRoute.name()).isEqualTo("Elend");
        assertThat(elendRoute.uuid()).isEqualTo("32354541274");
        assertThat(elendRoute.description()).isEmpty();
        assertThat(elendRoute.enabled()).isTrue();
        assertThat(elendRoute.rule()).isEqualTo("endpoint.system==\"cars\" && endpoint.env==\"stage\" && emxDatatype==\"vendor\"");
        assertThat(elendRoute.createdDate()).isEqualTo("2023-03-28");
        assertThat(elendRoute.modifiedDate()).isEqualTo("2023-03-28");

        var randRoute = routes.get(1);
        assertThat(randRoute.rule()).isEqualTo("endpoint==\"cars#stage/vendor\"");
    }

    @Test
    public void convertRulesToHeaders() throws IOException {
        String body = """
                {
                  "routesList":[
                    {
                      "uuid": "32354541274",
                      "name": "Elend",
                      "rule": "endpoint.system==\\"cars\\" && endpoint.env==\\"stage\\" && emxDatatype==\\"vendor\\"",
                      "description": "",
                      "enabled": true,
                      "queues": ["scms#stage", "emx-core-trash#dev"],
                      "createdDate": "2023-03-28",
                      "modifiedDate": "2023-03-28"
                    },
                    {
                      "uuid": "32354541",
                      "name": "Rand",
                      "rule": "endpoint==\\"cars#stage/vendor\\"",
                      "description": "",
                      "enabled": true,
                      "queues": ["scms#stage"],
                      "createdDate": "2023-03-28",
                      "modifiedDate": "2023-03-28"
                    }
                  ]
                }
                """;
        var routes = Main.convertRuleToHeaders(body.getBytes(StandardCharsets.UTF_8));
        assertThat(routes).hasSize(2);
        var elendRoute = routes.get(0);

        assertThat(elendRoute.queues()).hasSize(2);
        assertThat(elendRoute.queues()).isEqualTo(List.of("scms#stage", "emx-core-trash#dev"));
        assertThat(elendRoute.name()).isEqualTo("Elend");
        assertThat(elendRoute.uuid()).isEqualTo("32354541274");
        assertThat(elendRoute.description()).isEmpty();
        assertThat(elendRoute.enabled()).isTrue();
        assertThat(elendRoute.rule()).isEqualTo("emxSourceSystem==\"cars\" && emxSourceEnvironment==\"stage\" && emxDatatype==\"vendor\"");
        assertThat(elendRoute.createdDate()).isEqualTo("2023-03-28");
        assertThat(elendRoute.modifiedDate()).isEqualTo("2023-03-28");

        var randRoute = routes.get(1);
        assertThat(randRoute.rule()).isEqualTo("endpoint==\"cars#stage/vendor\"");
    }

    @Test
    public void convertEndpointPattern() throws JsonProcessingException {
        String body = """
                {
                  "routesList":[
                    {
                      "uuid": "32354541274",
                      "name": "Elend",
                      "rule": "emxSourceSystem==\\"cars\\" && emxSourceEnvironment==\\"stage\\" && emxDatatype==\\"vendor\\"",
                      "description": "",
                      "enabled": true,
                      "queues": ["scms#stage", "emx-core-trash#dev"],
                      "createdDate": "2023-03-28",
                      "modifiedDate": "2023-03-28"
                    },
                    {
                      "uuid": "32354541",
                      "name": "Rand",
                      "rule": "endpoint==\\"cars#stage/vendor\\"",
                      "description": "",
                      "enabled": true,
                      "queues": ["scms#stage"],
                      "createdDate": "2023-03-28",
                      "modifiedDate": "2023-03-28"
                    },
                    {
                      "uuid": "32354541",
                      "name": "Perrin",
                      "rule": "endpoint==\\"cmiss#stage/raw\\" && (objectType==\\"bob\\"||objectType==\\"fred\\")",
                      "description": "",
                      "enabled": true,
                      "queues": ["scms#stage"],
                      "createdDate": "2023-03-28",
                      "modifiedDate": "2023-03-28"
                    },
                    {
                      "uuid": "32354541",
                      "name": "Mat",
                      "rule": "endpoint==\\"ews-payment#test\\" && s3.object.path==\\"joe/bob\\"",
                      "description": "",
                      "enabled": true,
                      "queues": ["scms#stage"],
                      "createdDate": "2023-03-28",
                      "modifiedDate": "2023-03-28"
                    },
                    {
                      "uuid": "32354541",
                      "name": "Egwene",
                      "rule": "(objectType==\\"Person\\" || objectType==\\"Unit\\") && endpoint==\\"cmiss#prod/raw\\"",
                      "description": "",
                      "enabled": true,
                      "queues": ["scms#stage"],
                      "createdDate": "2023-03-28",
                      "modifiedDate": "2023-03-28"
                    }
                  ]
                }
                """;
        var routes = Main.convertEndpointPattern(body);
        assertThat(routes).hasSize(5);
        var elendRoute = routes.get(0);
        assertThat(elendRoute.rule()).isEqualTo("emxSourceSystem==\"cars\" && emxSourceEnvironment==\"stage\" && emxDatatype==\"vendor\"");

        var randRoute = routes.get(1);
        assertThat(randRoute.rule()).isEqualTo("(endpoint==\"cars/vendor#stage\" || endpoint==\"cars#stage/vendor\")");

        var perrinRoute = routes.get(2);
        assertThat(perrinRoute.rule()).isEqualTo("(endpoint==\"cmiss/raw#stage\" || endpoint==\"cmiss#stage/raw\") && (objectType==\"bob\"||objectType==\"fred\")");

        var matRoute = routes.get(3);
        assertThat(matRoute.rule()).isEqualTo("endpoint==\"ews-payment#test\" && s3.object.path==\"joe/bob\"");

        var egweneRoute = routes.get(4);
        assertThat(egweneRoute.rule()).isEqualTo("(objectType==\"Person\" || objectType==\"Unit\") && (endpoint==\"cmiss/raw#prod\" || endpoint==\"cmiss#prod/raw\")");
    }

}
