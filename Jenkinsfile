pipeline {
  agent {
    docker {
      image 'maven:3-alpine'
      args '-v /root/.m2:/root/.m2 --link sonarqube:sonar'
    }
    
  }
  stages {
    stage('Build') {
      steps {
        sh './mvnw package -P war'
      }
    }
  }
}