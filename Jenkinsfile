node {
    properties(
            [parameters([choice(choices: ['Chrome', 'Firefox'], description: 'Which browser to use?', name: 'ENVIRONMENT')]),
             [$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '10']]
            ])

    deleteDir()
    def rtMaven = Artifactory.newMavenBuild();
    def version;
    def scmVars
    try{
        stage('Checkout') {
            scmVars = checkout scm
            echo env.BRANCH_NAME
            def pom = readMavenPom file: 'pom.xml'
            version = pom.version
            environment {
                POM_VERSION = version
            }
            currentBuild.displayName += " : " +version

        }

        stage('Integration Tests') {
            withCredentials([[
                                     $class: 'AmazonWebServicesCredentialsBinding',
                                     credentialsId: 'device-farm',
                                     accessKeyVariable: 'AWS_ACCESS_KEY_ID',
                                     secretKeyVariable: 'AWS_SECRET_ACCESS_KEY'
                             ]]){
                withCredentials([
                        usernamePassword(credentialsId: 'jira-techuser', usernameVariable: 'user', passwordVariable: 'pass')
                ]){
                    withMaven(
                            maven: 'maven-3',
                            mavenSettingsConfig: 'MavenSettings.xml',
                            publisherStrategy: 'EXPLICIT') {
                        sh "mvn clean dependency:purge-local-repository install -U -Dmaven.test.failure.ignore -Daws.accessKeyId=$AWS_ACCESS_KEY_ID -Daws.secretAccessKey=$AWS_SECRET_ACCESS_KEY -Djira:user=$user -Djira:password=$pass -Dxray:testEnvironment=${ENVIRONMENT} -Dmaven.test.failure.ignore -DselectedDevice=remote${ENVIRONMENT} -Dxray:executionName=Jenkins"
                        step([$class: 'Publisher', fileIncludePattern: '**/testng-results*.xml',markAsUnstable:true, copyHTMLInWorkspace:true])
                        rtp parserName: 'HTML', stableText: '<h2>Test Report</h2>${FILE:testng-reports-with-handlebars/testsByClassOverview.html}'
                        step([$class: 'Publisher', reportFilenamePattern: '**/testng-results*.xml'])
                    }
                }
            }
        }

        stage('Publish artifacts'){

            try {
                archiveArtifacts artifacts: "testautomat*.log"
            } catch (Throwable t) {
                //TODO: find a better solution
            }
            try {
                archiveArtifacts artifacts: "target/**/*testng-results*.xml"
            } catch (Throwable t) {
                //TODO: find a better solution
            }
            try {
                archiveArtifacts artifacts: "target/**/*.pdf"
            } catch (Throwable t) {
                //TODO: find a better solution
            }
            try {
                archiveArtifacts artifacts: "target/**/*jvm*.dump"
            } catch (Throwable t) {
                //TODO: find a better solution
            }

        }

    } catch(err) {
        echo 'Exception occurred'+err.getMessage()
        err.printStackTrace();
        currentBuild.result = 'FAILED'
        throw err
    } finally{
        echo "RESULT: ${currentBuild.result}"
    }
}
