import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record RoutesList(
    @JsonProperty("routesList")
    List<Route> routes) {

}
