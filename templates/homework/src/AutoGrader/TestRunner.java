package AutoGrader;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import org.junit.internal.runners.statements.FailOnTimeout;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * Interfaces with JUnit tests. Provided by RHIT CSSE Department.
 */
class TestRunner extends BlockJUnit4ClassRunner {
    // Updated in the configuration file
    static int extraCreditTests = 0;
    static int testTimeoutSeconds = 30;

    private static boolean firstRun = true;
    private static int runners = 0;
    private static int completed = 0;
    private static int allTestsFailedCount = 0;
    private static int allTestsExecutedCount = 0;
    private static PrintWriter output;

    private int testCount = 0;
    private int testFailure = 0;
    private String visibility;
    private GradescopeAutoGrader g;

    TestRunner(Class<?> testClass, GradescopeAutoGrader g, String visibility)
            throws org.junit.runners.model.InitializationError {
        super(testClass);
        this.g = g;
        this.visibility = visibility;
        synchronized (TestRunner.class) {
            runners++;
        }
        try {
            if (firstRun) {
                // Scores will be written to this file to avoid mixing with print statements from
                // the student's code
                output = new PrintWriter(new FileWriter("results.out"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Statement methodBlock(FrameworkMethod method) {
        // Sets the timeout for each test
        Statement statement = super.methodBlock(method);
        return FailOnTimeout.builder().withTimeout(testTimeoutSeconds, TimeUnit.SECONDS)
                .build(statement);
    }

    @Override
    public void run(RunNotifier ideJUnitRunner) {
        synchronized (TestRunner.class) {
            if (firstRun) {
                firstRun = false;
                output.println(
                        "------------------------------------------------------------------");
                output.println("                   Gradescope Autograder Output");
                output.println("                      Running all unit tests");
                output.println(
                        "------------------------------------------------------------------");
            }
        }

        this.g.addTest(getName(), visibility);

        // Count tests with Decorator Pattern
        RunNotifier decorator = new RunNotifier() {
            @Override
            public void fireTestStarted(Description description) throws StoppedByUserException {
                testCount++;
                synchronized (TestRunner.class) {
                    allTestsExecutedCount++;
                }
                ideJUnitRunner.fireTestStarted(description);
            }

            @Override
            public void fireTestFailure(Failure failure) {
                testFailure++;
                synchronized (TestRunner.class) {
                    allTestsFailedCount++;
                }
                if (failure.getMessage() != null)
                    g.addFailure(getName(), failure.toString().replace("\"", "'"));
                else
                    g.addFailure(getName(), failure.getTestHeader() + ": Test failed");
                ideJUnitRunner.fireTestFailure(failure);
            }
        };

        super.run(decorator);
        if (this.testFailure > this.testCount) {
            // Error in test, prevent over-counting
            synchronized (TestRunner.class) {
                allTestsFailedCount -= this.testFailure - this.testCount;
            }
            this.testFailure = this.testCount;
        }
        this.g.addResult(getName(), this.testCount, this.testFailure);

        double percentagePassed = (this.testCount == 0) ? 0
                : (double) (this.testCount - this.testFailure) / (double) this.testCount * 100.0;
        output.printf("%5d   %8d   %10.1f%%   %-15s\n", this.testCount,
                (this.testCount - this.testFailure), percentagePassed, this.getTestClass().getName()
                        .substring(this.getTestClass().getName().lastIndexOf(".") + 1));

        synchronized (TestRunner.class) {
            completed++;
            if (completed == runners) {
                int allTestsPassedCount = allTestsExecutedCount - allTestsFailedCount;
                double allPercentagePassed = (double) allTestsPassedCount
                        / ((double) allTestsExecutedCount - extraCreditTests) * 100.0;
                output.println(
                        "------------------------------------------------------------------");
                output.printf("%5d   %8d   %10.1f%%   %-15s\n", allTestsExecutedCount,
                        allTestsPassedCount, allPercentagePassed, "<-- Grand Totals");
                output.println(
                        "------------------------------------------------------------------");
                output.close();
                this.g.toJSON(allPercentagePassed);
            }
        }
    }
}
