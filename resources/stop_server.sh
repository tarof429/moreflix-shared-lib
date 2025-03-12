#!/bin/bash

export IMAGE="$1"

COMPOSE_PROFILES=dummy docker-compose -f docker-compose.yaml down

# Forcefully stop the docker image
docker stop $(docker ps -qf name=app)

docker stop $(docker ps -qf name=db)