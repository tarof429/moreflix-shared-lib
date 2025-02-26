#!/usr/bin/env groovy

def call(file) {
    if (file == 'setup.py') {
        getVersionSetupPy(file)
    }
}

def getVersionSetupPy(file) {
    def content = readFile(file)

    def matcher = content =~ /version=['"](\d+)\.(\d+)\.(\d+)['"]/

    if (!matcher.find()) {
        error("Version not found in ${file}")
    }
    def major = matcher.group(1)
    def minor = matcher.group(2)
    def patch = matcher.group(3)

    return "${major}.${minor}.${patch}"
}