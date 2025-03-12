#!/usr/bin/env groovy

package com.github.moreflix

import java.io.Serializable

class Helper implements Serializable {
    def script
    def simulator
    def serverIP
    def ec2User

    Helper(script) {
        this.script = script
        this.simulator = false
        this.serverIP = '18.237.6.141'
        this.ec2User = 'ec2-user'
    }
    
    def setSimulator(simulator) {
        this.simulator = simulator
    }

    def setServerIP(serverIP) {
        this.serverIP = serverIP
    }

    def setec2User(ec2User) {
        this.ec2User = ec2User
    }

    def stopAndRemoveOrphans() {
        if (simulator) {
            this.script.echo('Shutdown the whole thing')
            this.script.echo("Will run: IMAGE=dummy COMPOSE_PROFILES=db,app docker compose down --remove-orphans")
        } else {
            this.script.sh("IMAGE=dummy COMPOSE_PROFILES=db,app docker compose down --remove-orphans")
}
    }
    def buildDockerImage(image, dockerfile='Dockerfile') {
        if (this.simulator) {
            this.script.echo("Running docker build -f ${dockerfile} -t ${image} .")
        } else {
            this.script.sh("docker build -f ${dockerfile} -t ${image} .")
        }
    }

    def deployToAWS(image) {
        if (this.simulator) {
            this.script.echo("Deploying ${image} to AWS server ${this.serverIP}")

            this.script.sshagent(['docker-server']) {
                this.script.echo("Will run: scp docker-compose.yaml ${this.ec2User}@${this.serverIP}:/home/ec2-user/")
                this.script.echo("Will run: scp stop_server.sh ${this.ec2User}@${this.serverIP}:/home/ec2-user/")
                this.script.echo("Will run: scp start_server.sh ${this.ec2User}@${this.serverIP}:/home/ec2-user/")

                // Display remote directory contents
                this.script.sh("ssh -o StrictHostKeyChecking=no ${this.ec2User}@${this.serverIP} ls -l")

                // Copy stop_server.sh
                def stopServerFileContent = this.script.libraryResource('stop_server.sh')
                this.script.writeFile(file: '/tmp/stop_server.sh', text: stopServerFileContent)
                this.script.sh("scp -o StrictHostKeyChecking=no /tmp/stop_server.sh ${this.ec2User}@${this.serverIP}:/home/ec2-user/")
                // Display remote directory contents
                this.script.sh("ssh -o StrictHostKeyChecking=no ${this.ec2User}@${this.serverIP} ls -l")

            }
        } else {
            def stopServerFileContent = this.script.libraryResource('stop_server.sh')
            this.script.writeFile(file: '/tmp/stop_server.sh', text: stopServerFileContent)
            def stopServerCmd = "bash ./stop_server.sh dummy"

            def startServerFileContent = this.script.libraryResource('start_server.sh')
            this.script.writeFile(file: '/tmp/start_server.sh', text: startServerFileContent)
            def startServerCmd = "bash ./start_server.sh ${image}"

            this.script.sshagent(['docker-server']) {
                this.script.sh("scp -o StrictHostKeyChecking=no .env ${this.ec2User}@${this.serverIP}:/home/ec2-user/")
                this.script.sh("scp -o StrictHostKeyChecking=no docker-compose.yaml ${this.ec2User}@${this.serverIP}:/home/ec2-user/")
                this.script.sh("scp -o StrictHostKeyChecking=no /tmp/stop_server.sh ${this.ec2User}@${this.serverIP}:/home/ec2-user/")
                this.script.sh("scp -o StrictHostKeyChecking=no /tmp/start_server.sh ${this.ec2User}@${this.serverIP}:/home/ec2-user/")
                
                this.script.echo("Running ${stopServerCmd}")
                try {
                    this.script.sh("ssh -o StrictHostKeyChecking=no ${this.ec2User}@${this.serverIP} $stopServerCmd")
                } catch (Exception e) {
                    this.script.echo("An exception occurred: " + e.toString())
                }

                this.script.echo("Running ${startServerCmd}")
                this.script.sh("ssh -o StrictHostKeyChecking=no ${this.ec2User}@${this.serverIP} $startServerCmd")
            }
        }
    }

    def getVersion(file) {
        if (this.simulator) {
            return "1.2.3"
        } else {
            if (file == 'setup.py') {
                this.getVersionSetupPy(file)
            }
        }
    }

    def getVersionSetupPy(file) {
        def content = this.script.readFile(file)

        def matcher = content =~ /version=['"](\d+)\.(\d+)\.(\d+)['"]/

        if (!matcher.find()) {
            this.script.error("Version not found in ${file}")
        }
        def major = matcher.group(1)
        def minor = matcher.group(2)
        def patch = matcher.group(3)

        return "${major}.${minor}.${patch}"
    }

    def githubCommit(credentialsId, url) {
        if (this.simulator) {
            this.script.echo("Will commit changes to github")
            this.script.withCredentials([
                this.script.string(
                    credentialsId: credentialsId, 
                    variable: 'GIT_PAT')]) {
                        url = url.replace("https://github.com", "https://${this.script.GIT_PAT}@github.com")
                this.script.echo("Inside committing changes...")
            }

        } else {
            this.script.withCredentials([
                this.script.string(
                    credentialsId: credentialsId, 
                    variable: 'GIT_PAT')]) {
                        url = url.replace("https://github.com", "https://${this.script.GIT_PAT}@github.com")

                        this.script.echo("Committing changes to ${url}")

                        this.script.sh("git config --global user.email 'jenkins@release.com'")
                        this.script.sh("git config --global user.name 'jenkins'")
                        this.script.sh("git remote set-url origin ${url}")

                        def branch = this.script.env.BRANCH_NAME
                        this.script.sh('git add .')
                        this.script.sh('git commit -m "CI: version bump"')
                        this.script.sh("git push origin HEAD:${branch}")
                
            }
        }
    }

    def startDockerCompose(image, profiles="") {
        if (this.simulator) {
            this.script.echo("Will run: IMAGE=${image} COMPOSE_PROFILES=${profiles} docker compose up -d")
        } else {
            this.script.sh("IMAGE=${image} COMPOSE_PROFILES=${profiles} docker compose up -d")
        }
    }

    def runDockerCompose(image, profile) {
        if (this.simulator) {
            this.script.echo("Will run: IMAGE=${image} docker compose run ${profile}")
            return 0
        } else {
            this.script.sh(returnStatus: true, script: "IMAGE=${image} docker compose run ${profile}")
        }
    }

    def updateVersion(file) {
        if (this.simulator) {
            return "Updating version..."
        } else {
            if (file == 'setup.py') {
                updateSetupPy(file)
            }
        }
    }

    def updateSetupPy(String file) {
        def content = this.script.readFile(file)

        def updatedLines = content.readLines().collect { line ->
            if (line.trim().startsWith("version=")) {
                def versionLine = line.trim().split("=", 2) // Ensure proper splitting
                if (versionLine.size() < 2) {
                    this.script.error("Invalid version format in ${file}: ${line}")
                }

                def versionString = versionLine[1].replaceAll("[\"']", "").trim() // Remove quotes if present
                versionString = versionString.replaceAll(",", "").trim() // Remove commas
                def versionParts = versionString.tokenize('.') // Split safely

                if (versionParts.size() != 3) {
                    this.script.error("Invalid version format in ${file}: ${versionString}")
                }

                def major = versionParts[0].toString()
                def minor = versionParts[1].toString()
                def patch = (versionParts[2].toInteger() + 1).toString()

                return "    version='${major}.${minor}.${patch}'," // Replace the line with the updated version
            }
            return line
        }

        def updatedContent = updatedLines.join("\n")
        this.script.writeFile(file: file, text: updatedContent)

        this.script.echo("Updated ${file}: version updated to ${updatedContent.find(/version=['\"](\d+\.\d+\.\d+)['\"]/)}")
    }

    def tagDockerImage(tag, newTag) {
        if (this.simulator) {
            this.script.echo("Will run: docker tag ${tag} ${newTag}")
        } else {
            this.script.sh("docker tag ${tag} ${newTag}")
        }
    }

    def stopDockerCompose(image, profiles="") {
        if (this.simulator) {
            this.script.echo("Will run: IMAGE=${image} COMPOSE_PROFILES=${profiles} docker compose down")
        } else {
            this.script.sh("IMAGE=${image} COMPOSE_PROFILES=${profiles} docker compose down")
        }
    }

    def pushDockerTag(credentialsId, tag) {
        if (this.simulator) {
            this.script.echo("Will push ${tag}")
            this.script.withCredentials([
                this.script.usernamePassword(
                credentialsId: credentialsId, 
                passwordVariable: 'PASS', 
                usernameVariable: 'USER')]) {
                this.script.echo("Logging in as ${this.script.env.USER}")
            }
        } else {
            this.script.withCredentials([
                this.script.usernamePassword(
                credentialsId: credentialsId, 
                passwordVariable: 'PASS', 
                usernameVariable: 'USER')]) {
                    this.script.sh("echo ${this.script.env.PASS} | docker login -u ${this.script.env.USER} --password-stdin")
                    this.script.sh("docker push ${tag}")
            }
        }
    }
}