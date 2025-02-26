#!/usr/bin/env groovy

def call(tag, newTag) {
    sh "docker tag ${tag} ${newTag}"
}