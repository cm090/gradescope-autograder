package newAutograder;

import java.util.HashSet;
import java.util.Set;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

public class Runner {
  private final Set<TestRunner> runners;

  Runner() {
    runners = new HashSet<>();
  }

  void addRunners() {
    for (Class<?> testClass : Configuration.instance.getClasses()) {
      if (isClassExcluded(testClass.getName())) {
        continue;
      }
      try {
        runners.add(new TestRunner(testClass));
      } catch (NoClassDefFoundError | InitializationError ignored) {
      }
    }
  }

  private boolean isClassExcluded(String testClass) {
    Set<String> excludedClasses = Configuration.instance.getExcludedClasses();
    return excludedClasses.contains(testClass);
  }

  void runTests() {
    for (TestRunner t : runners) {
      t.run(new RunNotifier());
    }
  }
}
