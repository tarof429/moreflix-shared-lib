#!/bin/bash

export IMAGE="$1"

COMPOSE_PROFILES=db,app docker-compose -f docker-compose.yaml up -d