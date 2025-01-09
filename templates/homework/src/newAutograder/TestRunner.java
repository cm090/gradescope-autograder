package newAutograder;

import java.io.PrintWriter;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

public class TestRunner extends BlockJUnit4ClassRunner {
    private static final String OUTPUT_FILE = "";
    private static boolean isFirstRun = true;
    private static int numRunners = 0;
    private static int numCompleted = 0;
    private static int totalTestsExecuted = 0;
    private static int totalTestsFailed = 0;
    private static PrintWriter outputWriter = null;

    private int numTestsExecuted = 0;
    private int numTestsFailed = 0;

    TestRunner(Class<?> testClass, Results results) throws InitializationError {
        super(testClass);
    }

    @Override
    protected Statement methodBlock(FrameworkMethod method) {
        return super.methodBlock(method);
    }

    @Override
    public void run(RunNotifier junitRunner) {
        super.run(junitRunner);
    }
}
