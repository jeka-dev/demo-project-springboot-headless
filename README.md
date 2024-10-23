# JeKa Spring-Boot application

This repository showcases how to:
- Build a Spring Boot application.
- Effortlessly create different flavors of native Spring Boot Docker images (Ubuntu, distroless).

The project follows the standard structure:
- Code is organized in the *Maven* layout (*src/main/java, ...*).
- Java dependencies are declared in [dependencies.txt](./dependencies.txt).
- The build is configured in [jeka.properties](./jeka.properties) (no code is needed here).

The application serves a simple REST API for managing a list of users, backed by an in-memory database.

Once the application has started, you can query the service at:  http://localhost:8080/users

It has been copied from the tutorial [Spring Boot and Angular Web](https://www.baeldung.com/spring-boot-angular-web).

## Build the application

To build application including testing, execute :
```shell
jeka project: pack
```
This creates a bootable jar in *jeka-output* dir. 

To run the JAR built in previous step, you can execute
```shell
jeka project: runJar
```

You can also run it more directly by executing:
```shell
jeka -p
```
The last command runs the first executable or jar file found under *jeka-output* dir.

## Create JVM Docker image

Create a Docker image running the Java application:
```shell
jeka docker: build
```
This creates a Docker image with efficient layering for Java, running as a non-root user.

Run the image:
```shell
docker run --rm -p 8080:8080 demo-project-springboot-headless:latest
```

## Create native images

Create an executable file under *jeka-output* dir:
```shell
jeka native: compile
```

Run it:
```shell
jeka -p
```
Fine ! We created a native Spring-Boot application without the need to install or configure anything on the host machine.

## Create Docker image running the native executable

Now, let's create a Docker image of the native application:
```shell
jeka docker: buildNative
```
Execute the native Docker image:
```shell
docker run --rm -p 8080:8080 native-demo-project-springboot-headless:latest
```
We have created a Docker image running the Spring Boot native application.

We can inspect the generated image by visiting [Docker build dir](./jeka-output/docker-build-native-demo-project-springboot-headless#latest), 
or by executing:
```shell
jeka docker: infoNative
```

## Configure the native image

By default, the native image is based on Ubuntu and uses statically linked libc.

We can generate a smaller and more secure image based on a minimal distroless version. 
This requires compiling with statically linked libc.

For this, we can add the following lines in *jeka.properties*

```properties
@native.staticLink=MUSL
@docker.nativeBaseImage=gcr.io/distroless/static-debian12:nonroot
```
Check effect by running:
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
                .add("RUN ..."));
    }
}
```
The API allows us to add or insert Docker build instructions at a specific point.
It also provides convenient methods to copy arbitrary files from the host filesystem to the target image.
