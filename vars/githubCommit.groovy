#!/usr/bin/env groovy

def call() {
    def branch = env.BRANCH_NAME
    sh 'git add .'
    sh 'git commit -m "CI: version bump"'
    sh "git push origin HEAD:${branch}"
}