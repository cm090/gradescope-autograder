package rhit.domain;

import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

public class BuilderData {
  @Getter
  private static final Set<String> templateFiles = new HashSet<>();
  @Getter
  @Setter
  private static String templateDir;
  @Getter
  @Setter
  private static String starterCodeDir;
  @Getter
  @Setter
  private static TemplateType templateType = TemplateType.AUTO;

  public static void addTemplateFile(String file) {
    templateFiles.add(file);
  }
}
