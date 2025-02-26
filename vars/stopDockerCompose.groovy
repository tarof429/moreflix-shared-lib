#!/usr/bin/env groovy

def call(profiles="") {
    sh "COMPOSE_PROFILES=${profiles} docker compose down"
}