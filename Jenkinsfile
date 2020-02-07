pipeline {
    agent any
    tools {
        jdk 'Java 8'
    }
    stages {
        stage ('Initialize') {
            steps {
                sh '''
                    echo "PATH = ${PATH}"
                '''
            }
        }
        stage ('Build') {
            steps {
                sh './gradlew clean jar'
            }
            post {
              	always {
                    // junit 'target/surefire-reports/**/*.xml'
					fileOperations([fileDeleteOperation(includes: '*.log')])
                }
                success {
                    archiveArtifacts artifacts: 'build/libs/*.jar', excludes: '*-beans-*.jar', fingerprint: true
                }
            }
        }
    }
}

