def call() {

    node {

        stage('Load Config') {

            def props = new Properties()
            props.load(new StringReader(libraryResource('config.properties')))

            env.SLACK_CHANNEL_NAME = props['SLACK_CHANNEL_NAME']
            env.ENVIRONMENT        = props['ENVIRONMENT']
            env.CODE_BASE_PATH     = props['CODE_BASE_PATH']
            env.ACTION_MESSAGE     = props['ACTION_MESSAGE']
            env.KEEP_APPROVAL_STAGE = props['KEEP_APPROVAL_STAGE']

            env.GIT_URL            = props['GIT_URL']
            env.BRANCH             = props['BRANCH']
            env.PLAYBOOK           = props['PLAYBOOK']
            env.INVENTORY          = props['INVENTORY']
        }

        stage('Clone Code') {

            echo "Cloning repository..."

            git branch: env.BRANCH,
                url: env.GIT_URL

            echo "Code cloned successfully"
        }

        if (env.KEEP_APPROVAL_STAGE == "true") {

            stage('User Approval') {

                input message: "Approve deployment to ${env.ENVIRONMENT}?",
                      ok: "Approve"
            }
        }

        stage('Playbook Execution') {

            sh """
                ansible-playbook -i ${env.INVENTORY} ${env.PLAYBOOK}
            """
        }

        stage('Notification') {

            slackSend(
                channel: "#${env.SLACK_CHANNEL_NAME}",
                message: "${env.ACTION_MESSAGE}"
            )
        }
    }
}
