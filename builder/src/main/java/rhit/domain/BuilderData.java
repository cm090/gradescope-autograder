package rhit.domain;

import lombok.Getter;
import lombok.Setter;

public class BuilderData {
  @Getter
  @Setter
  private static String templateDir;

  @Getter
  @Setter
  private static TemplateType templateType;
}
