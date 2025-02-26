#!/usr/bin/env groovy

def call(file) {
    if (file == 'setup.py') {
        updateSetupPy(file)
    }
}

def updateSetupPy(file) {
    def content = readFile(file)

    def matcher = content =~ /version=['"](\d+)\.(\d+)\.(\d+)['"]/

    if (!matcher.find()) {
        error("Version not found in ${file}")
    }
    def major = matcher.group(1)
    def minor = matcher.group(2)
    def patch = matcher.group(3)

    def nextPatch = patch.toInteger() + 1

    def updatedContent = content.replaceAll(/version\s*=\s*['"](\d+)\.(\d+)\.(\d+)['"]/, "version='${major}.${minor}.${nextPatchVersion}'")

    writeFile(file: file, text: updatedContent)
}