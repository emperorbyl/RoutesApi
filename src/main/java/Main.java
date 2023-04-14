import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

public class Main {

  private static final HttpClient CLIENT =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
  private static final String QUEUE_ENVIRONMENT = "dev"; //dev, stage, prod
  private static final String URL_ENVIRONMENT = "dev"; //dev, np, prod
  private static final String BASE_URI =
      "https://emx-route-manager-" + URL_ENVIRONMENT + ".churchofjesuschrist.org/api/emx-router";
  private static final ObjectMapper MAPPER =
      JsonMapper.builder().addModule(new ParameterNamesModule()).build();

  public static void main(String[] args) throws IOException {
    Properties properties = new Properties();
    properties.load(
        new FileReader(System.getProperty("user.home") + File.separator + ".cred.properties"));
    var authHeader =
        properties.getProperty("emxaccount" + QUEUE_ENVIRONMENT + ".username")
            + ":"
            + properties.getProperty("emxaccount" + QUEUE_ENVIRONMENT + ".password");

    runEndpointConversion(authHeader);

    // The fallback
    //revert(authHeader);

  }

  private static void revert(String authHeader) throws IOException {

    try (var localFiles = Files.list(Path.of("./"))) {
      var backup = Files.readAllBytes(localFiles.findAny().orElseThrow());
      var queueRoutes = convertEndpointsToQueues(backup);
      parameterizeUpdate(authHeader, queueRoutes);
    }
  }

  private static void runEndpointConversion(String authHeader) throws IOException {
    var routes = getRoutes(authHeader);

    Files.writeString(Path.of("./OriginalRoutes" + LocalDateTime.now().format(
        DateTimeFormatter.ISO_LOCAL_DATE_TIME) + ".json"), routes);

    var endpointRoutes = convertQueuesToEndpoints(routes);
    parameterizeUpdate(authHeader, endpointRoutes);
  }

  private static void parameterizeUpdate(String authHeader, List<Route> routes) {
    for (var route : routes) {
      var parameters = addSharedParams(route);
      String body = createUrlEncodedBody(parameters);
      updateRoute(authHeader, body);
    }
  }

  private static String getRoutes(String authHeader) {
    var request =
        HttpRequest.newBuilder()
            .uri(URI.create(BASE_URI + "/routes"))
            .setHeader(
                "Authorization",
                "Basic "
                    + Base64.getEncoder()
                    .encodeToString(authHeader.getBytes(StandardCharsets.UTF_8)))
            .GET()
            .build();
    return makeHttpCall(request);
  }

  private static String updateRoute(String authHeader, String body) {
    var request =
        HttpRequest.newBuilder()
            .uri(URI.create(BASE_URI + "/updateroute"))
            .setHeader(
                "Authorization",
                "Basic "
                    + Base64.getEncoder()
                    .encodeToString(authHeader.getBytes(StandardCharsets.UTF_8)))
            .PUT(BodyPublishers.ofString(body))
            .build();
    return makeHttpCall(request);
  }

  private static String makeHttpCall(HttpRequest request) {
    HttpResponse<String> response;
    try {
      response = CLIENT.send(request, BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("There was an interruption to the thread in the http call.");
    } catch (IOException e) {
      throw new IllegalStateException("There was an io Exception in the http call.");
    }
    if (response.statusCode() != 200) {
      throw new IllegalStateException(
          "There was an error in the http call. StatusCode: "
              + response.statusCode()
              + " Body: "
              + response.body());
    }
    return response.body();
  }

  static List<Route> convertQueuesToEndpoints(String body) throws JsonProcessingException {
    var routes = MAPPER.readValue(body, RoutesList.class);
    List<Route> updatedRoutes = new ArrayList<>();
    for (var route : routes.routes()) {
      List<String> endpoints = new ArrayList<>();
      for (var queue : route.queues()) {
        if ("emx-trash".equals(queue)) {
          endpoints.add("emx-core-trash#" + QUEUE_ENVIRONMENT);
        } else if ("emx-to-archive-core".equals(queue)) {
          endpoints.add("emx-core-archive#" + QUEUE_ENVIRONMENT);
        } else if ("emx-to-emx-healthcheck".equals(queue)) {
          endpoints.add("emx-core-healthcheck#" + QUEUE_ENVIRONMENT);
        } else if (queue.contains("#")) {
          endpoints.add(queue);
        } else {
          var pieces = queue.split("-");
          StringBuilder system = new StringBuilder();
          for (int i = 2; i < pieces.length - 1; i++) {
            system.append(pieces[i]).append("-");
          }
          system.deleteCharAt(system.length() - 1);
          String systemEncoded = URLEncoder.encode(system.toString(), StandardCharsets.UTF_8);
          String environment = URLEncoder.encode(pieces[pieces.length - 1], StandardCharsets.UTF_8);
          endpoints.add(systemEncoded + "#" + environment);
        }
      }
      if (endpoints.isEmpty()) {
        continue;
      }
      var updatedRoute =
          new Route(
              route.uuid(),
              route.name(),
              route.rule(),
              route.description(),
              route.enabled(),
              endpoints,
              route.createdDate(),
              route.modifiedDate());
      updatedRoutes.add(updatedRoute);
    }
    return updatedRoutes;
  }

  static List<Route> convertEndpointsToQueues(byte[] input) throws IOException {
    var routes = MAPPER.readValue(input, RoutesList.class);
    List<Route> updatedRoutes = new ArrayList<>();
    for (var route : routes.routes()) {
      List<String> queues = new ArrayList<>();
      for (var endpoint : route.queues()) {
        if (("emx-core-trash#" + QUEUE_ENVIRONMENT).equals(endpoint)) {
          queues.add("emx-trash");
        } else if (("emx-core-archive#" + QUEUE_ENVIRONMENT).equals(endpoint)) {
          queues.add("emx-to-archive-core");
        } else if (("emx-core-healthcheck#" + QUEUE_ENVIRONMENT).equals(endpoint)) {
          queues.add("emx-to-emx-healthcheck");
        } else if (endpoint.contains("emx-to") || "emx-trash".equals(endpoint)) {
          queues.add(endpoint);
        } else {
          var pieces = endpoint.split("#");
          String system = URLDecoder.decode(pieces[0], StandardCharsets.UTF_8);
          String environment = URLDecoder.decode(pieces[1], StandardCharsets.UTF_8);
          queues.add("emx-to-" + system + "-" + environment);
        }
      }
      if (queues.isEmpty()) {
        continue;
      }
      var updatedRoute =
          new Route(
              route.uuid(),
              route.name(),
              route.rule(),
              route.description(),
              route.enabled(),
              queues,
              route.createdDate(),
              route.modifiedDate());
      updatedRoutes.add(updatedRoute);
    }
    return updatedRoutes;
  }

  static String createUrlEncodedBody(List<NameValuePair> params) {
    try {
      UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(params, "UTF-8");
      int size = (int) urlEncodedFormEntity.getContentLength();
      byte[] content = new byte[size];
      int bytesRead = urlEncodedFormEntity.getContent().read(content);
      return new String(content);
    } catch (Exception exception) {
      throw new IllegalStateException(exception.getMessage(), exception);
    }
  }

  /**
   * Adds the common name value pairs used in getting routes
   *
   * @param route name value pairs are created of route properties
   */
  static List<NameValuePair> addSharedParams(Route route) {
    List<NameValuePair> params = new ArrayList<>();
    params.add(new BasicNameValuePair("name", route.name()));
    params.add(new BasicNameValuePair("rule", route.rule()));
    params.add(new BasicNameValuePair("description", route.description()));
    params.add(new BasicNameValuePair("queues", String.join(",", route.queues())));
    params.add(new BasicNameValuePair("enabled", Boolean.toString(route.enabled())));
    params.add(new BasicNameValuePair("uuid", route.uuid()));
    return params;
  }
}
