#!/usr/bin/env groovy

def call() {
    echo 'Shutdown the whole thing'
    sh "IMAGE=dummy COMPOSE_PROFILES=db,app docker compose down --remove-orphans"
}