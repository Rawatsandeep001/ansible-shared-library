// vars/ansibleDeploy.groovy

def call() {

    node {

        def config = readProperties file: 'resources/config.properties'

        env.SLACK_CHANNEL_NAME = config.SLACK_CHANNEL_NAME
        env.ENVIRONMENT        = config.ENVIRONMENT
        env.CODE_BASE_PATH     = config.CODE_BASE_PATH
        env.ACTION_MESSAGE     = config.ACTION_MESSAGE
        env.KEEP_APPROVAL_STAGE= config.KEEP_APPROVAL_STAGE

        env.GIT_URL            = config.GIT_URL
        env.BRANCH             = config.BRANCH
        env.PLAYBOOK           = config.PLAYBOOK
        env.INVENTORY          = config.INVENTORY


        // -----------------------------
        // Stage 1: Clone
        // -----------------------------
        stage('Clone Code') {

            echo "Cloning repository..."

            git branch: env.BRANCH,
                url: env.GIT_URL

            echo "Code cloned successfully"
        }


        // -----------------------------
        // Stage 2: User Approval
        // -----------------------------
        if (env.KEEP_APPROVAL_STAGE == "true") {

            stage('User Approval') {

                echo "Waiting for approval..."

                input message: "Approve deployment to ${env.ENVIRONMENT} environment?",
                      ok: "Approve"

                echo "Approval received"
            }
        }


        // -----------------------------
        // Stage 3: Playbook Execution
        // -----------------------------
        stage('Ansible Playbook Execution') {

            echo "Running Ansible Playbook..."

            sh """
            ansible-playbook -i ${env.INVENTORY} ${env.PLAYBOOK}
            """

            echo "Playbook execution completed"
        }


        // -----------------------------
        // Stage 4: Notification
        // -----------------------------
        stage('Notification') {

            echo "Sending Slack Notification..."

            slackSend(
                channel: "#${env.SLACK_CHANNEL_NAME}",
                message: "${env.ACTION_MESSAGE} | Environment: ${env.ENVIRONMENT}"
            )

            echo "Notification sent"
        }
    }
}
