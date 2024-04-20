import dev.jeka.core.api.project.JkProject;
import dev.jeka.core.api.system.JkProperties;
import dev.jeka.core.tool.KBean;
import dev.jeka.core.tool.builtins.project.ProjectKBean;
import dev.jeka.plugins.nodejs.JkNodeJs;
import dev.jeka.plugins.nodejs.NodeJsKBean;
import dev.jeka.plugins.sonarqube.JkSonarqube;
import dev.jeka.plugins.sonarqube.SonarqubeKBean;

class Build extends KBean {

    final JkProject project = load(ProjectKBean.class).project;

    /*
     * Configures KBean project
     * When this method is called, option fields have already been injected from command line.
     */
    @Override
    protected void init() {
        // configure project instance here
    }

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

}