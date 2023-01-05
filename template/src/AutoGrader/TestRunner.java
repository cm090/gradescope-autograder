package AutoGrader;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;
import org.junit.runners.BlockJUnit4ClassRunner;

/**
 * Interfaces with JUnit tests
 * Provided by RHIT CSSE Department
 */
public class TestRunner extends BlockJUnit4ClassRunner {

    private static boolean firstRun = true;
    private static int runners = 0;
    private static int completed = 0;
    private static int allTestsFailedCount = 0;
    private static int allTestsExecutedCount = 0;

    private int testCount = 0;
    private int testFailure = 0;
    private String visibility;

    private GradescopeAutoGrader g;

    public TestRunner(Class<?> testClass, GradescopeAutoGrader g, String visibility) throws org.junit.runners.model.InitializationError {
        super(testClass);
        this.g = g;
        this.visibility = visibility;
        synchronized (TestRunner.class) {
            runners++;
        }
    }

    public TestRunner(Class<?> testClass, GradescopeAutoGrader g) throws org.junit.runners.model.InitializationError {
        this(testClass, g, "after_due_date");
    }

    @Override
    public void run(RunNotifier ideJUnitRunner) {
        synchronized (TestRunner.class) {
            if (firstRun) {
                firstRun = false;
                System.out.println("------------------------------------------------------------------");
                System.out.println("                   Gradescope Autograder Output");
                System.out.println("                      Running all unit tests");
                System.out.println("------------------------------------------------------------------");
            }
        }

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
                ideJUnitRunner.fireTestFailure(failure);
            }

            @Override
            public void fireTestFinished(Description description) {
                // Executes regardless whether the test passed.
                ideJUnitRunner.fireTestFinished(description);
            }

            @Override
            public void fireTestSuiteStarted(Description description) {
                ideJUnitRunner.fireTestSuiteStarted(description);
            }

            @Override
            public void fireTestSuiteFinished(Description description) {
                ideJUnitRunner.fireTestSuiteFinished(description);
            }

            @Override
            public void fireTestRunStarted(Description description) {
                ideJUnitRunner.fireTestRunStarted(description);
            }

            @Override
            public void fireTestRunFinished(Result result) {
                ideJUnitRunner.fireTestRunFinished(result);
            }

            @Override
            public void pleaseStop() {
                ideJUnitRunner.pleaseStop();
            }

            @Override
            public void addFirstListener(RunListener listener) {
                ideJUnitRunner.addFirstListener(listener);
            }

            @Override
            public void addListener(RunListener listener) {
                ideJUnitRunner.addListener(listener);
            }

            @Override
            public void fireTestAssumptionFailed(Failure failure) {
                ideJUnitRunner.fireTestAssumptionFailed(failure);
            }

            @Override
            public void fireTestIgnored(Description description) {
                ideJUnitRunner.fireTestIgnored(description);
            }

            @Override
            public void removeListener(RunListener listener) {
                ideJUnitRunner.removeListener(listener);
            }
        };

        super.run(decorator);

        g.addTest(getName(), testCount, visibility);
        g.addResult(getName(), testCount - testFailure);

        double percentagePassed = (double) (testCount - testFailure) / (double) testCount * 100.0;
        if (!this.getTestClass().getName().contains("."))
            System.out.printf("%5d   %8d   %10.1f%%   %-15s\n", testCount, (testCount - testFailure), percentagePassed,
                    this.getTestClass().getName());

        synchronized (TestRunner.class) {
            completed++;
            if (completed == runners) {
                int allTestsPassedCount = allTestsExecutedCount - allTestsFailedCount;
                double allPercentagePassed = (double) allTestsPassedCount / (double) allTestsExecutedCount
                        * 100.0;
                System.out.println("------------------------------------------------------------------");
                System.out.printf("%5d   %8d   %10.1f%%   %-15s\n", allTestsExecutedCount, allTestsPassedCount,
                        allPercentagePassed, "<-- Grand Totals");
                g.toJSON(allPercentagePassed);
            }
        }
    }
}