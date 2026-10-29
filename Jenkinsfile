pipeline {
    agent any

    options {
        skipDefaultCheckout(true)
        timestamps()
    }

    environment {
        JAVA_HOME = '/opt/homebrew/opt/openjdk@21'
        PATH      = "${JAVA_HOME}/bin:/opt/homebrew/bin:/usr/local/bin:${env.PATH}"
    }

    stages {
        stage('Checkout') {
            steps { checkout scm }
        }
        stage('Build') {
            steps { sh './mvnw -B clean package -DskipTests' }
        }
        stage('Test') {
            steps { sh './mvnw -B test' }
            post {
                always { junit 'target/surefire-reports/*.xml' }
            }
        }
    }
}