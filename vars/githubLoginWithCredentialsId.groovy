#!/usr/bin/env groovy

def call(credentialsId, url) {
    withCredentials([
        string(
            credentialsId: ${credentialsId}, 
            variable: 'GIT_PAT')]) {
        url = url.replace("https://github.com", "https://${GIT_PATH}@github.com")

        echo "Committing changes to ${url}"

        sh("git config --global user.email 'jenkins@release.com'")
        sh("git config --global user.name 'jenkins'")
        sh("git remote set-url origin ${url}")
    }
}