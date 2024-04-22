import dev.jeka.core.api.system.JkLog;
import dev.jeka.core.api.utils.JkUtilsNet;
import dev.jeka.core.api.utils.JkUtilsSystem;

public abstract class ApplicationTester {

    protected int startTimeout = 15*1000;

    protected int reAttemptDelay = 1000;

    final void run() {
        JkLog.startTask("Starting application under test");
        try {
            startApp();
            checkUntilReady();
            JkLog.info("Application started");
        } finally {
            JkLog.endTask();
        }
        JkLog.startTask("Executing tests");
        try {
            executeTests();
        } finally {
            JkLog.endTask();
            JkLog.startTask("Stop application under test");
            stopApp();;
            JkLog.info("Application stopped");
        }
    }

    protected abstract void startApp();

    protected abstract boolean isApplicationReady();

    protected abstract void executeTests();

    protected abstract void stopApp();

    protected final int findFreePort() {
        return JkUtilsNet.findFreePort(49152, 65535);

    }

    private void checkUntilReady() {
        long start = System.currentTimeMillis();
        while ( (System.currentTimeMillis() - start) < startTimeout ) {
            if (isApplicationReady()) {
                return;
            }
            JkUtilsSystem.sleep(reAttemptDelay);
        }
        throw new IllegalStateException("Application did not get ready prior timeout.");
    }
}
