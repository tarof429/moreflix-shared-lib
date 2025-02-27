#!/usr/bin/env groovy

def call(file) {
    if (file == 'setup.py') {
        updateSetupPy(file)
    }
}

def updateSetupPy(String file) {
    def content = readFile(file)

    def updatedLines = content.readLines().collect { line ->
        if (line.trim().startsWith("version=")) {
            def versionLine = line.trim().split("=", 2) // Ensure proper splitting
            if (versionLine.size() < 2) {
                error("Invalid version format in ${file}: ${line}")
            }

            def versionString = versionLine[1].replaceAll("[\"']", "").trim() // Remove quotes if present
            versionString = versionString.replaceAll(",", "").trim() // Remove commas
            def versionParts = versionString.tokenize('.') // Split safely

            if (versionParts.size() != 3) {
                error("Invalid version format in ${file}: ${versionString}")
            }

            def major = versionParts[0].toString()
            def minor = versionParts[1].toString()
            def patch = (versionParts[2].toInteger() + 1).toString()

            return "    version='${major}.${minor}.${patch}'," // Replace the line with the updated version
        }
        return line
    }

    def updatedContent = updatedLines.join("\n")
    writeFile(file: file, text: updatedContent)

    echo "Updated ${file}: version updated to ${updatedContent.find(/version=['\"](\d+\.\d+\.\d+)['\"]/)}"
}