# Tuleap Branch Source plugin for Jenkins

[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/tuleap-git-branch-source.svg)](https://plugins.jenkins.io/tuleap-git-branch-source/)
[![GitHub release](https://img.shields.io/github/release/jenkinsci/tuleap-git-branch-source.svg?label=changelog)](https://plugins.jenkins.io/tuleap-git-branch-source/releases/latest)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/tuleap-git-branch-source.svg?color=blue)](https://plugins.jenkins.io/tuleap-git-branch-source/)

This Jenkins plugin is a community effort.

This jenkins plugin allow autodiscovery of Tuleap's git repositories and branches to automatically create jenkins jobs when branches have `Jenkinsfile`.

Please find the documentation at [https://docs.tuleap.org/user-guide/ci.html](https://docs.tuleap.org/user-guide/ci.html)

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

## Authors

* RAMBELONTSALAMA Haja (project's initiator)
* ROBINSON Clarck
* VACELET Manuel
* GOYOT Martin
