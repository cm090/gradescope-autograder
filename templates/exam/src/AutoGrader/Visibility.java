package autograder;

/**
 * The visibility of a question or assignment.
 * 
 * <ul>
 * <li>{@link #HIDDEN}: Always hidden from students.</li>
 * <li>{@link #AFTER_DUE_DATE}: Visible to students after the due date.</li>
 * <li>{@link #AFTER_PUBLISHED}: Visible to students after grades are published.</li>
 * <li>{@link #VISIBLE}: Always visible to students.</li>
 */
public enum Visibility {
  HIDDEN("hidden"),
  AFTER_DUE_DATE("after_due_date"),
  AFTER_PUBLISHED("after_published"),
  VISIBLE("visible");

  private final String key;

  Visibility(String key) {
    this.key = key;
  }

  static Visibility getVisibility(String key) {
    for (Visibility visibility : Visibility.values()) {
      if (visibility.key.equals(key)) {
        return visibility;
      }
    }
    throw new RuntimeException("Invalid visibility key: " + key);
  }

  public String getValue() {
    return key;
  }
}
