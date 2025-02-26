#!/usr/bin/env groovy

def call() {
    withCredentials([
        string(
            credentialsId: credentialsId, 
            variable: 'GIT_PAT')]) {
        url = url.replace("https://github.com", "https://${GIT_PAT}@github.com")

        echo "Committing changes to ${url}"

        sh("git config --global user.email 'jenkins@release.com'")
        sh("git config --global user.name 'jenkins'")
        sh("git remote set-url origin ${url}")
}
        def branch = env.BRANCH_NAME
        sh 'git add .'
        sh 'git commit -m "CI: version bump"'
        sh "git push origin HEAD:${branch}"
    }
}