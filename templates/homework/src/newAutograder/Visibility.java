package newAutograder;

public enum Visibility {
    HIDDEN("hidden"), AFTER_DUE_DATE("after_due_date"), AFTER_PUBLISHED("after_published"), VISIBLE(
            "visible");

    private String key;

    private Visibility(String key) {
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

    String getValue() {
        return key;
    }
}
