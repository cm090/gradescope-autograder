package autograder;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import org.junit.internal.runners.statements.FailOnTimeout;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * Runs a single test class and writes the results to the output file.
 *
 * @see BlockJUnit4ClassRunner
 */
public class TestRunner extends BlockJUnit4ClassRunner {
  private static final String OUTPUT_FILE = "results.out";
  private static boolean isFirstRun = true;
  private static int numRunners = 0;
  private static int numCompleted = 0;
  private static int totalTestsExecuted = 0;
  private static int totalTestsFailed = 0;
  private static PrintWriter outputWriter;

  private int numTestsExecuted = 0;
  private int numTestsFailed = 0;

  /**
   * Constructs a TestRunner object, updates the global number of runners, and prepares the output
   * file.
   *
   * @param testClass the test class to run
   * @throws InitializationError if the test class is malformed
   */
  TestRunner(Class<?> testClass) throws InitializationError {
    super(testClass);
    synchronized (TestRunner.class) {
      numRunners++;
      try {
        if (isFirstRun) {
          outputWriter = new PrintWriter(new FileWriter(OUTPUT_FILE, StandardCharsets.UTF_8));
        }
      } catch (Exception e) {
        System.err.println("Unable to open output file");
      }
    }
  }

  /**
   * Extends the method configuration to include a timeout.
   *
   * @param method the method to run
   * @return the statement to run the method
   */
  @Override
  protected Statement methodBlock(FrameworkMethod method) {
    return FailOnTimeout.builder()
        .withTimeout(Configuration.instance.getTestTimeoutSeconds(), TimeUnit.SECONDS)
        .build(super.methodBlock(method));
  }

  /**
   * Runs the test class and writes the results to the output file.
   *
   * @param junitRunner the notifier to report test results to
   */
  @Override
  public void run(RunNotifier junitRunner) {
    writeStartMessage();
    Results.instance.addTest(getName(), Configuration.instance.getTestVisibility());

    RunNotifier decorator = buildRunNotifier(junitRunner);
    super.run(decorator);

    if (numTestsFailed > numTestsExecuted) {
      synchronized (TestRunner.class) {
        decrementTotalTestsFailed(numTestsFailed - numTestsExecuted);
      }
      numTestsFailed = numTestsExecuted;
    }

    Results.instance.addTestResult(getName(), numTestsExecuted, numTestsFailed);
    writePercentagePassed();
    writeEndMessage();
  }

  private void writeStartMessage() {
    if (outputWriter == null) {
      System.err.println("Unable to open output file");
      return;
    }
    synchronized (TestRunner.class) {
      if (isFirstRun) {
        isFirstRun = false;
        outputWriter.println("------------------------------------------------------------------");
        outputWriter.println("                   Gradescope Autograder Output");
        outputWriter.println("                      Running all unit tests");
        outputWriter.println("------------------------------------------------------------------");
      }
    }
  }

  private RunNotifier buildRunNotifier(RunNotifier junitRunner) {
    return new RunNotifier() {
      /**
       * Updates the number of test executed when a new test is started.
       *
       * @param description the description of the test
       * @throws StoppedByUserException if the user stops the test
       */
      @Override
      public void fireTestStarted(Description description) throws StoppedByUserException {
        numTestsExecuted++;
        synchronized (TestRunner.class) {
          totalTestsExecuted++;
        }
        junitRunner.fireTestStarted(description);
      }

      /**
       * Reports a test failure if a test fails.
       *
       * @param failure the failure that occurred
       */
      @Override
      public void fireTestFailure(Failure failure) {
        numTestsFailed++;
        synchronized (TestRunner.class) {
          totalTestsFailed++;
        }
        if (failure.getMessage() != null) {
          Results.instance.addTestFailure(getName(), failure.toString().replace("\"", "'"));
        } else {
          Results.instance.addTestFailure(getName(), failure.getTestHeader() + ": Test failed");
        }
        junitRunner.fireTestFailure(failure);
      }
    };
  }

  private void decrementTotalTestsFailed(int numDecrease) {
    totalTestsFailed -= numDecrease;
  }

  private void writePercentagePassed() {
    if (outputWriter == null) {
      System.err.println("Unable to open output file");
      return;
    }
    double percentagePassed = (numTestsExecuted == 0) ? 0
        : (double) (numTestsExecuted - numTestsFailed) / (double) numTestsExecuted * 100.0;
    outputWriter.printf("%5d   %8d   %10.1f%%   %-15s", numTestsExecuted,
        (numTestsExecuted - numTestsFailed), percentagePassed, this.getTestClass().getName()
            .substring(this.getTestClass().getName().lastIndexOf(".") + 1));
    outputWriter.println();
  }

  private void writeEndMessage() {
    if (outputWriter == null) {
      System.err.println("Unable to open output file");
      return;
    }
    synchronized (TestRunner.class) {
      numCompleted++;
      if (numCompleted == numRunners) {
        int allTestsPassedCount = totalTestsExecuted - totalTestsFailed;
        int allTestsRanCount = totalTestsExecuted - Configuration.instance.getExtraCreditTests();
        double allPercentagePassed = allTestsRanCount == 0 ? 0
            : ((double) allTestsPassedCount / (double) allTestsRanCount) * 100.0;
        outputWriter.println("------------------------------------------------------------------");
        outputWriter.printf("%5d   %8d   %10.1f%%   %-15s", totalTestsExecuted, allTestsPassedCount,
            allPercentagePassed, "<-- Grand Totals");
        outputWriter.println();
        outputWriter.println("------------------------------------------------------------------");
        outputWriter.close();
        Results.instance.toJson(allPercentagePassed);
      }
    }
  }
}
