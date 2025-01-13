package newAutograder;

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

  TestRunner(Class<?> testClass) throws InitializationError {
    super(testClass);
    synchronized (TestRunner.class) {
      numRunners++;
    }
    try {
      if (isFirstRun) {
        outputWriter = new PrintWriter(new FileWriter(OUTPUT_FILE, StandardCharsets.UTF_8));
      }
    } catch (Exception e) {
      System.err.println("Unable to open output file");
    }
  }

  @Override
  protected Statement methodBlock(FrameworkMethod method) {
    return FailOnTimeout.builder()
        .withTimeout(Configuration.instance.getTestTimeoutSeconds(), TimeUnit.SECONDS)
        .build(super.methodBlock(method));
  }

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
      @Override
      public void fireTestStarted(Description description) throws StoppedByUserException {
        numTestsExecuted++;
        synchronized (TestRunner.class) {
          totalTestsExecuted++;
        }
        junitRunner.fireTestStarted(description);
      }

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
    double percentagePassed = (numTestsExecuted == 0) ? 0 :
        (double) (numTestsExecuted - numTestsFailed) / (double) numTestsExecuted * 100.0;
    outputWriter.printf("%5d   %8d   %10.1f%%   %-15s", numTestsExecuted,
        (numTestsExecuted - numTestsFailed), percentagePassed, this.getTestClass().getName()
            .substring(this.getTestClass().getName().lastIndexOf(".") + 1));
    outputWriter.println();
  }

  private void writeEndMessage() {
    synchronized (TestRunner.class) {
      numCompleted++;
      if (numCompleted == numRunners) {
        int allTestsPassedCount = totalTestsExecuted - totalTestsFailed;
        double allPercentagePassed = (double) allTestsPassedCount /
            ((double) totalTestsExecuted - Configuration.instance.getExtraCreditTests()) * 100.0;
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
