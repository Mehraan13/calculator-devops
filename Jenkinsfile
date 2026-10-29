pipeline {
    agent any

    options {
        skipDefaultCheckout(true)
        timestamps()
    }
    
    triggers { 
        githubPush() 
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

        stage('Deploy') {
            steps {
                sh '''
                    APP_IMAGE=${IMAGE_REPO}:${IMAGE_TAG} docker compose -p calculator up -d --remove-orphans
                    for i in $(seq 1 30); do
                        if curl -fsS http://localhost:8081/actuator/health | grep -q '"status":"UP"'; then
                            echo "Deployment healthy"
                            exit 0
                        fi
                        sleep 3
                    done
                    echo "Health check failed"
                    docker compose -p calculator logs
                    exit 1
                '''
            }
        }
    }

    post {
        success {
            mail to: 'mehraanazharmir@gmail.com',
                 subject: "SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: "Build ${env.BUILD_NUMBER} passed and was deployed.\n${env.BUILD_URL}"
        }
        failure {
            mail to: 'mehraanazharmir@gmail.com',
                 subject: "FAILURE: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: "Build ${env.BUILD_NUMBER} failed.\n${env.BUILD_URL}console"
        }
    }
}