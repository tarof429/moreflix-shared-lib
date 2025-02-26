#!/usr/bin/env groovy

def call(credentialsId, tag) {
    withCredentials([
        usernamePassword(
            credentialsId: ${credentialsId}, 
            passwordVariable: 'PASS', 
            usernameVariable: 'USER')]) {
            sh('echo $PASS | docker login -u $USER --password-stdin')
            sh("docker push ${tag}")
    }
}