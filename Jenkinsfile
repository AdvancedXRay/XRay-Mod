pipeline {
  agent any
  stages {
    stage('Build') {
      steps {
        sh './gradlew --refresh-dependencies --continue build '
      }
    }

    stage('publish') {
      steps {
        archiveArtifacts(fingerprint: true, onlyIfSuccessful: true, artifacts: 'build/libs/**/*.jar')
      }
    }

  }
}