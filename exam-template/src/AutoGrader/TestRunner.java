package AutoGrader;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import org.junit.internal.runners.statements.FailOnTimeout;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * Interfaces with JUnit tests
 * Provided by RHIT CSSE Department
 */
public class TestRunner extends BlockJUnit4ClassRunner {
    // Change this to the number of extra credit tests you have, if any
    private static final int EXTRA_CREDIT_TESTS = 0;
    // Individual tests will fail if they take longer than the time set below
    private static final int TEST_TIMEOUT_SECONDS = 30;

    private static boolean firstRun = true;
    private static int runners = 0;
    private static int completed = 0;
    private static int allTestsFailedCount = 0;
    private static int allTestsExecutedCount = 0;

    private int testCount = 0;
    private int testFailure = 0;
    private String visibility;

    private GradescopeAutoGrader g;
    private static PrintWriter output;

    public TestRunner(Class<?> testClass, GradescopeAutoGrader g, String visibility)
            throws org.junit.runners.model.InitializationError {
        super(testClass);
        this.g = g;
        this.visibility = visibility;
        synchronized (TestRunner.class) {
            runners++;
        }
        try {
            if (firstRun)
                output = new PrintWriter(new FileWriter("results.out"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public TestRunner(Class<?> testClass, GradescopeAutoGrader g) throws org.junit.runners.model.InitializationError {
        this(testClass, g, "after_due_date");
    }

    @Override
    protected Statement methodBlock(FrameworkMethod method) {
        Statement statement = super.methodBlock(method);
        return FailOnTimeout.builder().withTimeout(TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS).build(statement);
    }

    @Override
    public void run(RunNotifier ideJUnitRunner) {
        synchronized (TestRunner.class) {
            if (firstRun) {
                firstRun = false;
                output.println("------------------------------------------------------------------");
                output.println("                   Gradescope Autograder Output");
                output.println("                      Running all unit tests");
                output.println("------------------------------------------------------------------");
            }
        }

        g.addTest(getName(), visibility);

        // count tests with Decorator Pattern
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
        if (testFailure > testCount) {
            synchronized (TestRunner.class) {
                allTestsFailedCount -= testFailure - testCount;
            }
            testFailure = testCount;
        }
        g.addResult(getName(), testCount, testFailure);

        double percentagePassed = (testCount == 0) ? 0 : (double) (testCount - testFailure) / (double) testCount * 100.0;
        output.printf("%5d   %8d   %10.1f%%   %-15s\n", testCount, (testCount - testFailure), percentagePassed,
                this.getTestClass().getName().substring(this.getTestClass().getName().lastIndexOf(".") + 1));

        synchronized (TestRunner.class) {
            completed++;
            if (completed == runners) {
                int allTestsPassedCount = allTestsExecutedCount - allTestsFailedCount;
                double allPercentagePassed = (double) allTestsPassedCount
                        / ((double) allTestsExecutedCount - EXTRA_CREDIT_TESTS)
                        * 100.0;
                output.println("------------------------------------------------------------------");
                output.printf("%5d   %8d   %10.1f%%   %-15s\n", allTestsExecutedCount, allTestsPassedCount,
                        allPercentagePassed, "<-- Grand Totals");
                output.close();
                g.toJSON(allPercentagePassed);
            }
        }
    }
}