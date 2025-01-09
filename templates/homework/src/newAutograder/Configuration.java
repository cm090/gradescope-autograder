package newAutograder;

import java.io.File;
import java.util.Set;
import java.util.regex.Pattern;

public class Configuration {
    private static final Pattern MAX_SCORE_PATTERN = null;
    static Configuration instance = new Configuration();

    private File configFile;
    private File metadataFile;
    private double maxScore;
    private int extraCreditTests;
    private int testTimeoutSeconds;
    private Visibility testVisibility;
    private Set<Class<?>> classes;

    private Configuration() {}

    static void build(String configFile, String metadataFile) {}

    private void parseMaxScore() {}

    private void parseExtraCreditTests() {}

    private void parseTestTimeoutSeconds() {}

    private void parseTestVisibility() {}

    private void parseClasses() {}

    double getMaxScore() {
        return maxScore;
    }

    int getExtraCreditTests() {
        return extraCreditTests;
    }

    int getTestTimeoutSeconds() {
        return testTimeoutSeconds;
    }

    Visibility getTestVisibility() {
        return testVisibility;
    }

    Set<Class<?>> getClasses() {
        return Set.copyOf(classes);
    }
}
