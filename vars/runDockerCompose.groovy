#!/usr/bin/env groovy

def call(profile) {
    sh(returnStatus: true, script: "docker compose run ${profile}")
}