import java.util.List;
import java.util.Objects;

public record Route(
      String uuid,
      String name,
      String rule,
      String description,
      boolean enabled,
      List<String> queues,
      String createdDate,
      String modifiedDate) {
    public Route{
    Objects.requireNonNull(uuid, "Uuid can not be null");
    Objects.requireNonNull(name, "Rule name cannot be null");
    Objects.requireNonNull(rule, "Rule string cannot be null");
    Objects.requireNonNull(queues, "Queues set cannot be null");
  }

}
