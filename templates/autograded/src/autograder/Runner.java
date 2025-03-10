package autograder;

import java.util.HashSet;
import java.util.Set;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

/**
 * Create test runners and run all the JUnit tests.
 */
public class Runner {
  private final Set<TestRunner> runners;

  Runner() {
    runners = new HashSet<>();
  }

  /**
   * For each class in the configuration, create a test runner and add it to the set of runners.
   */
  void addRunners() {
    for (Class<?> testClass : Configuration.instance.getClasses()) {
      if (isClassExcluded(testClass.getName())) {
        continue;
      }
      try {
        runners.add(new TestRunner(testClass, Configuration.instance.getCalculationType()));
      } catch (NoClassDefFoundError | InitializationError ignored) {
        // Do nothing
      }
    }
  }

  /**
   * Determine whether a test class should be excluded from the test suite.
   *
   * @param testClass the name of the class
   * @return true if the class should be excluded, false otherwise
   * @see Configuration#getExcludedClasses()
   */
  private boolean isClassExcluded(String testClass) {
    return Configuration.instance.getExcludedClasses().stream().anyMatch(testClass::contains);
  }

  /**
   * Run all the JUnit tests.
   */
  void runTests() {
    for (TestRunner t : runners) {
      t.run(new RunNotifier());
    }
  }
}
