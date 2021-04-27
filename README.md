# Tuleap Branch Source plugin for Jenkins

[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/tuleap-git-branch-source.svg)](https://plugins.jenkins.io/tuleap-git-branch-source/)
[![GitHub release](https://img.shields.io/github/release/jenkinsci/tuleap-git-branch-source.svg?label=changelog)](https://plugins.jenkins.io/tuleap-git-branch-source/releases/latest)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/tuleap-git-branch-source.svg?color=blue)](https://plugins.jenkins.io/tuleap-git-branch-source/)

This jenkins plugin allow autodiscovery of Tuleap's git repositories and branches to automatically create jenkins jobs when branches have `Jenkinsfile`.

Works with any recent version of Jenkins >= 2.164.1 (latest LTS preferred).

# How to use

* Install the plugin
* In Jenkins global configuration you should reference your Tuleap instance (There is a limitation of 1 Tuleap server per Jenkins instance)
* Then any Jenkins user can create a new "Tuleap project" job type
  * They should have a [`Tuleap Access Key` jenkins credential](https://docs.tuleap.org/user-guide/citizen.html#access-keys) with a user that can access the project you want to target
  * They need to select the project they want to work on
  * Update the filters (by default all repositories are ignored), for instance remove the "*" in "Exclude"
  * Save the configuration
  * Then Jenkins should be automatically scanning the project
    * Find all matching git repositories
    * For each repo, scan all branches
    * For each branch, when there is a `Jenkinsfile` a job should be created and scheduled accordingly

## Tuleap job webhook

  When a push is done in a Tuleap git repository, the linked Jenkins job will be automatically built.

### How to configure
On the Tuleap side:
 * Please refer to the [Tuleap documentation](https://docs.tuleap.org/user-guide/code-versioning/git.html?#webhooks) for instructions

If you do not want to trigger via Tuleap, you can use the `https://JENKINS_URL/tuleap-hook/` URL.
If you use the URL you have to give the request body, for instance:

```json
{
    "tuleapProjectId": "130",
    "repositoryName": "repo001",
    "branchName":"master"
}
```

## Report issues

Issues must be reported in [Request tracker of the Tuleap project](https://tuleap.net/plugins/tracker/?report=1136) under the category "Jenkins Branch Source plugin".

# Development

## On jenkins, connect to Tuleap

Configure Jenkins to accept a tuleap dev environment certificate

    echo -n | openssl s_client -connect tuleap-web.tuleap-aio-dev.docker:443 |    sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p'  > /usr/local/share/ca-certificates/tuleap-web.tuleap-aio-dev.docker.crt
    keytool -keystore {$JAVA_HOME}/jre/lib/security/cacerts   -import -trustcacerts -storepass changeit -noprompt -alias tuleap-web-dev -file /usr/local/share/ca-certificates/tuleap-web.tuleap-aio-dev.docker.crt
    update-ca-certificates

## Build

### You have a local java / maven env

Tested with OpenJDK 8

    $> mvn clean install
    $> cp target/tuleap-branch-source.hpi onto jenkins

### With docker

    docker run -it --rm -u $(id -u) \
           -v ~/.m2:/var/maven/.m2 -e MAVEN_CONFIG=/var/maven/.m2 \
           -v "$(pwd)":/usr/src/mymaven -w /usr/src/mymaven \
           maven:3.3-jdk-8 \
           mvn -Duser.home=/var/maven clean install

## See also

* [tuleap-oauth-plugin](https://github.com/jenkinsci/tuleap-oauth-plugin) a plugin to link Jenkins to Tuleap's OAuth, coupling identification and authorisation on Tuleap, reducing users management effort.

## Authors

* RAMBELONTSALAMA Haja (project's initiator)
* ROBINSON Clarck
* VACELET Manuel
* GOYOT Martin
