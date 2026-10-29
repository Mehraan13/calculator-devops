pipeline {
    agent any

    options {
        skipDefaultCheckout(true)
        timestamps()
    }

    environment {
        JAVA_HOME = '/opt/homebrew/opt/openjdk@21'
        PATH      = "${JAVA_HOME}/bin:/opt/homebrew/bin:/usr/local/bin:${env.PATH}"
        IMAGE_REPO = 'mehraan13/calculator-devops'
        IMAGE_TAG  = "${env.BUILD_NUMBER}"
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
        
        stage('Docker Build') {
            steps {
                sh 'docker build -t ${IMAGE_REPO}:${IMAGE_TAG} -t ${IMAGE_REPO}:latest .'
            }
        }

        stage('Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds',
                                                  usernameVariable: 'DH_USER',
                                                  passwordVariable: 'DH_PASS')]) {
                    sh '''
                        echo "$DH_PASS" | docker login -u "$DH_USER" --password-stdin
                        docker push ${IMAGE_REPO}:${IMAGE_TAG}
                        docker push ${IMAGE_REPO}:latest
                        docker logout
                    '''
                }
            }
        }
    }
}