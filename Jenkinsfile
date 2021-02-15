pipeline {
  agent any
  stages {
    stage('Clean') {
      steps {
        sh './gradlew clean --no-daemon'
      }
    }

    stage('Build') {
      steps {
        sh './gradlew build --no-daemon'
      }
    }

    stage('Upload Artifacts') {
      steps {
        archiveArtifacts(artifacts: 'build/libs/**.jar', fingerprint: true)
      }
    }

    stage('Publish') {
      when {
        branch 'main'
      }
      steps {
        sh './gradlew publish --no-daemon'
      }
    }

  }
  environment {
    local_maven = '/var/jenkins_maven/'
  }
}
