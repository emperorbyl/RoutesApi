import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.Test;

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
    var expectedQueues = List.of("scms#stage", "emx-core-trash#dev", "emx-core-archive#dev",
        "emx-core-healthcheck#dev", "cfis#dev%3Askim", "scms#test", "cfis#test%3B");
    assertThat(routes).singleElement().satisfies(
        route -> {
          assertThat(route.queues()).hasSize(7);
          assertThat(route.queues()).isEqualTo(expectedQueues);
          assertThat(route.name()).isEqualTo("Elend");
          assertThat(route.uuid()).isEqualTo("32354541274");
          assertThat(route.description()).isEmpty();
          assertThat(route.enabled()).isTrue();
          assertThat(route.rule()).contains("emxSourceEnvironment");
          assertThat(route.createdDate()).isEqualTo("2023-03-28");
          assertThat(route.modifiedDate()).isEqualTo("2023-03-28");
        }
    );
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
    var expectedQueues = List.of("emx-to-scms-stage", "emx-trash", "emx-to-archive-core",
        "emx-to-emx-healthcheck", "emx-to-cfis-dev:skim", "emx-to-scms-test", "emx-to-cfis-test;");
    assertThat(routes).singleElement().satisfies(
        route -> {
          assertThat(route.queues()).hasSize(7);
          assertThat(route.queues()).isEqualTo(expectedQueues);
          assertThat(route.name()).isEqualTo("Elend");
          assertThat(route.uuid()).isEqualTo("32354541274");
          assertThat(route.description()).isEmpty();
          assertThat(route.enabled()).isTrue();
          assertThat(route.rule()).contains("emxSourceEnvironment");
          assertThat(route.createdDate()).isEqualTo("2023-03-28");
          assertThat(route.modifiedDate()).isEqualTo("2023-03-28");
        }
    );
  }

}
