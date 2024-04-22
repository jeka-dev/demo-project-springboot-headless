import dev.jeka.core.api.project.JkProject;
import dev.jeka.core.api.system.JkLog;
import dev.jeka.core.api.testing.JkTestProcessor;
import dev.jeka.core.api.testing.JkTestSelection;
import dev.jeka.core.api.utils.JkUtilsNet;
import dev.jeka.core.tool.JkDoc;
import dev.jeka.core.tool.KBean;
import dev.jeka.core.tool.builtins.project.ProjectKBean;
import dev.jeka.plugins.sonarqube.JkSonarqube;
import dev.jeka.plugins.sonarqube.SonarqubeKBean;

class Build extends KBean {

    final JkProject project = load(ProjectKBean.class).project;

    static final String E2E_TEST_PATTERN = "^e2e\\..*";

    /*
     * Configures KBean project
     * When this method is called, option fields have already been injected from command line.
     */
    @Override
    protected void init() {
        project.testing.testSelection.addExcludePatterns(E2E_TEST_PATTERN); // exclude e2e from unit tests
    }

    @JkDoc("Execute a Sonarqube scan on the NodeJs project")
    public void sonarJs() {
        JkSonarqube javaSonarqube = load(SonarqubeKBean.class).sonarqube;
        JkSonarqube jsSonarqube = javaSonarqube.copyWithoutProperties();
        String projectId = project.getBaseDir().toAbsolutePath().getFileName() + "-js";
        jsSonarqube
                .setProperty(JkSonarqube.PROJECT_KEY, projectId)
                .setProperty(JkSonarqube.PROJECT_NAME, projectId)
                .setProperty(JkSonarqube.HOST_URL, getRunbase().getProperties().get("sonar.host.url"))
                .setProperty(JkSonarqube.TOKEN, getRunbase().getProperties().get("sonar.token"))
                .setProperty(JkSonarqube.PROJECT_BASE_DIR, getBaseDir().resolve("app-js").toString())
                .setProperty(JkSonarqube.SOURCES, "src")
                .setProperty(JkSonarqube.EXCLUSIONS, "dist/**/*, node_modules/**/*")
                .setProperty(JkSonarqube.SOURCE_ENCODING, "UTF-8")
                .setProperty(JkSonarqube.TEST, "src")
                .setProperty(JkSonarqube.TEST_INCLUSIONS, "**/*.spec.ts")
                .setProperty("sonar.typescript.lcov.reportPaths", "coverage/client/lcov.info");
        jsSonarqube.run();
    }


    public void e2e() {
        new AppTester().run();
    }

    private class AppTester extends ApplicationTester {

        int port;

        String baseUrl;

        @Override
        public void startApp() {
            port = findFreePort();
            baseUrl = "http://localhost:" + port;
            project.prepareRunJar(JkProject.RuntimeDeps.EXCLUDE)
                    .addJavaOptions("-Dserver.port=" + port)
                    .addJavaOptions("-Dmanagement.endpoint.shutdown.enabled=true")
                    .setInheritIO(false)
                    .execAsync();
        }

        @Override
        public boolean isApplicationReady() {
            return JkUtilsNet.isStatusOk(baseUrl + "/actuator/health", JkLog.isDebug());
        }

        @Override
        public void executeTests() {
            JkTestSelection selection =  project.testing.createDefaultTestSelection()
                    .addIncludePatterns(E2E_TEST_PATTERN);
            JkTestProcessor testProcessor = project.testing.createDefaultTestProcessor().setForkingProcess(true);
            testProcessor.getForkingProcess()
                    .setLogWithJekaDecorator(true)
                    .setLogCommand(true)
                    .addJavaOptions("-Dselenide.reportsFolder=jeka-output/test-report/selenide")
                    .addJavaOptions("-Dselenide.downloadsFolder=jeka-output/test-report/selenide-download")
                    .addJavaOptions("-Dselenide.headless=true")
                    .addJavaOptions("-Dselenide.baseUrl=" + baseUrl);
            testProcessor.launch(project.testing.getTestClasspath(), selection).assertSuccess();
        }

        @Override
        public void stopApp() {
            String shutdownUrl = baseUrl + "/actuator/shutdown";
            JkLog.info("Invoke %s", shutdownUrl);
            JkUtilsNet.sendHttpRequest(shutdownUrl, "POST", null).asserOk();
        }
    }

}