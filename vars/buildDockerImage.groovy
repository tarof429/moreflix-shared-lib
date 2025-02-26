#!/usr/bin/env groovy

def call(image, dockerfile='Dockerfile') {
    sh "docker build -f ${dockerfile} -t ${image} ."
}