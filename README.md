# Native Spring-Boot Applications with JeKa

This repository showcases how [JeKa](https://jeka-dev.github.io/jeka/) can create native Docker images **effortlessly**, from a Spring Boot application. 

We'll also detail how to create *distrolless* images (more compact and secured) for running your native applications.

> *Note:* The entire demo can be executed on Windows, macOS or Linux.

## Prerequisites

- Nothing to install to build and run: the `./jeka` script downloads JeKa, the JDK and GraalVM.
  The commands below use `jeka`: use `./jeka` if JeKa is not installed.
- Docker, for the Docker images.

The project follows the standard structure:
- Code is organized in the *Maven* layout (*src/main/java, ...*).
- Java dependencies are declared in [jeka.project.deps](./jeka.project.deps).
- The build is configured in [jeka.properties](./jeka.properties) (no code is needed here).

The application serves a simple REST API for managing a list of users, backed by an in-memory database.

Once the application has started, you can query the service at:  http://localhost:8080/users

It has been copied from the tutorial [Spring Boot and Angular Web](https://www.baeldung.com/spring-boot-angular-web).

## Build the Application

To run the tests and build the application, execute:
```shell
jeka project: test pack
```
This creates a bootable jar in *jeka-output* dir. 

To run the JAR built in previous step, simply execute
```shell
jeka project: runJar
```

You can also run it more directly by executing:
```shell
jeka -p
```
The last command runs the first executable or jar file found under *jeka-output* dir.

## Create JVM Docker Images

Create a Docker image running the Java application:
```shell
jeka docker: build
```
This creates a Docker image with efficient layering for Java, running as a non-root user.

Run the image:
```shell
docker run --rm -p 8080:8080 demo-project-springboot-headless:latest
```

## Create Native Images

To create an executable file under *jeka-output* dir, execute:
```shell
jeka native: compile
```

To run the created executable file, simply execute:
```shell
jeka -p
```
Fine ! We created a native Spring-Boot application without the need to install or configure anything on the host machine.

## Create Docker Images running a Native Executable

```shell
jeka docker: buildNative
```
Execute the native Docker image:
```shell
docker run --rm -p 8080:8080 native-demo-project-springboot-headless:latest
```
We have created a Docker image running the Spring Boot native application.

We can inspect the generated image in its Docker build dir, *jeka-output/docker-build-native-demo-project-springboot-headless#latest*, 
or by executing:
```shell
jeka docker: infoNative
```

## Configure Native Images

By default, the native image is based on Ubuntu and uses statically linked libc.

We can generate a smaller and more secure image based on a minimal distroless version. 
This requires compiling with statically linked libc.

For this, we can add the following lines in *jeka.properties*

```properties
@native.staticLink=MUSL
@docker.nativeBaseImage=gcr.io/distroless/static-debian13:nonroot
```
Check the result by running:
```shell
jeka docker: infoNative
```
We can fine-tune the content of the Docker image using Java code.
Simply create a KBean class in the jeka-src directory (create the directory if needed) with content like this:

```java
class Build extends KBean {

    @Override
    public void init() {
        load(DockerKBean.class).customizeNativeImage(steps -> steps
                .addCopy(aFile, "/etc/myconfig")
                .insertBefore("USER nonroot", "COPY ....", "RUN ...")
                .add("RUN ..."));
    }
}
```
With the API, we can add or insert Docker build instructions at a specific point.
It also provides convenient methods - to copy arbitrary files from the host filesystem to the target image.

## Conclusion

We created secured native Docker images of our Springboot application in different flavor (Ubuntu and distroless based) and we made it **effortlessly** 🙂 :

- We didn’t need to install or configure GraalVM or JDKs.
- We didn’t need to edit Dockerfiles or any configuration files.
- `jeka native: compile` runs on the host, making it simpler to troubleshoot; `jeka docker: buildNative` compiles inside a container, so the executable targets Linux on any host.

We can explore available functions and settings using commands as: `jeka native: --doc`, `jeka docker: --doc` or `jeka springboot: --doc`.

## Links

- [native KBean](https://jeka-dev.github.io/jeka/reference/kbeans-native/)
- [docker KBean](https://jeka-dev.github.io/jeka/reference/kbeans-docker/)
- [Spring Boot plugin](https://github.com/jeka-dev/jeka/tree/master/plugins/plugins.springboot)
