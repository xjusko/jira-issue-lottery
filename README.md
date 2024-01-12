# jira-issue-lottery

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Set up the app

### Fork the Repository
Fork this repository to your GitHub account.

### Configure JIRA Lottery
1. Write a valid `jira-issue-lottery.yml` config in the `.github` directory in your forked repository.
    - **Valid Config Example:**
      ```yaml
      delay: P14D
      participants:
        - username: The-Huginn
          projects:
            - project: WFLY
              components: [Logging]
              maxIssues: 5
            - project: ELY
              components: [HTTP, Core]
              maxIssues: 3
      ```
   `delay`::
   The duration of the lottery cycle before participants are selected again.
   + Duration as a String in ISO-8601 format, mandatory, no default.
   
   `participants`::
   A list of participants with their preferences for participating in the lottery.
   + List of Participants, mandatory, no default.
   
   `username`::
   The GitHub username of the participant.
   + String, mandatory, no default.
   
   `projects`::
   The list of JIRA projects the participant is interested in, each with specific components and maximum allowed issues.
   + List of Projects, mandatory, no default.
   
   `project`::
   The name of the JIRA project.
   + String, mandatory, no default.
   
   `components`::
   The specific components within the project that the participant is interested in.
   + List of Strings, mandatory, no default.
   
   `maxIssues`::
   The maximum number of issues the participant is willing to take for the specified project and components.
   + Integer, mandatory, no default.


2. Commit and push the changes to your forked repository.

### Configure Application Properties
1. Change the value of `jira-issue-lottery.config-file-repo` in the `application.properties` file to point to a public repository with a valid config file according to first step in [Configuration section](#configure-jira-lottery) such as `https://github.com/jboss-set/jira-issue-lottery`.

### Create .env File
1. Create a `.env` file in the main directory of your app.
2. Create a Jira Personal Access Token ([Guide on how to create the token](https://confluence.atlassian.com/enterprise/using-personal-access-tokens-1026032365.html))
3. Add the following variable to the `.env` file:
   ```env
   jira-issue-lottery.access-token=<YOUR TOKEN>
   
## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/jira-lottery-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.

## Related Guides

- Camel Jira ([guide](https://camel.apache.org/camel-quarkus/latest/reference/extensions/jira.html)): Interact with JIRA issue tracker
- Picocli ([guide](https://quarkus.io/guides/picocli)): Develop command line applications with Picocli

## Provided Code

### Picocli Example

Hello and goodbye are civilization fundamentals. Let's not forget it with this example picocli application by changing the <code>command</code> and <code>parameters</code>.

[Related guide section...](https://quarkus.io/guides/picocli#command-line-application-with-multiple-commands)

Also for picocli applications the dev mode is supported. When running dev mode, the picocli application is executed and on press of the Enter key, is restarted.

As picocli applications will often require arguments to be passed on the commandline, this is also possible in dev mode via:
```shell script
./mvnw compile quarkus:dev -Dquarkus.args='Quarky'
```
