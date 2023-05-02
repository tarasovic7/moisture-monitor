pipeline {
    agent any
    options {
        ansiColor('xterm')
    }
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/tarasovic7/moisture-monitor'
            }
        }
        stage('Build') {
            steps {
                sh 'mvn clean install'
            }
        }
        stage('Backup') {
            steps {
                sshagent(credentials:['pi-zero']) {
                    sh 'ssh pi@192.168.0.201 "if [ -f /home/pi/moisture-0.0.1-SNAPSHOT.jar ]; then mv /home/pi/moisture-0.0.1-SNAPSHOT.jar /home/pi/moisture-0.0.1-SNAPSHOT.jar.bak; fi"'
                }
            }
        }
        stage('Deploy') {
            steps {
                sshagent(credentials:['pi-zero']) {
                    sh 'scp target/moisture-0.0.1-SNAPSHOT.jar pi@192.168.0.201:/home/pi/'
                }
            }
        }
        stage('Restart service') {
            steps {
                sshagent(credentials:['pi-zero']) {
                    sh 'ssh pi@192.168.0.201 "sudo systemctl restart moisture" '
                }
            }
        }
    }
    post {
        success {
            echo 'Build successful!'
        }
    }
}