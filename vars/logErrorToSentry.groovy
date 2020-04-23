import com.sap.piper.Utils
import com.sap.piper.ConfigurationHelper
import static com.sap.piper.Prerequisites.checkScript

import groovy.transform.Field

@Field def STEP_NAME = 'logErrorToSentry'
@Field Set STEP_CONFIG_KEYS = [
    'sentryDsn'
]
@Field Set GENERAL_CONFIG_KEYS = STEP_CONFIG_KEYS
@Field Set PARAMETER_KEYS = STEP_CONFIG_KEYS

/**
 * logToSentry
 *
 * @param errMessage
 */
def call(Map parameters = [:], stepName, stageName, errMessage) {
    def script = checkScript(this, parameters) ?: this
    stageName = stageName.replace('Declarative: ', '')
    // load default & individual configuration
    Map config = ConfigurationHelper.newInstance(this)
        .loadStepDefaults()
        .mixinGeneralConfig(script.commonPipelineEnvironment,  GENERAL_CONFIG_KEYS)
        .mixinStepConfig(script.commonPipelineEnvironment, STEP_CONFIG_KEYS)
        .mixinStageConfig(script.commonPipelineEnvironment, parameters.stageName?:env.STAGE_NAME, STEP_CONFIG_KEYS)
        .mixin(parameters, PARAMETER_KEYS)
        .withMandatoryProperty('sentryDsn')
        .use()

    //echo "NODE_NAME = ${env.NODE_NAME}"
    //println "logErrorToSentry config: " + config
    //println "logErrorToSentry parameters: " + script
    
    // def sentryDsn = config.sentryDsn
    // //println "logErrorToSentry sentryDsn:" + sentryDsn

    // def breadcrumpFile = "sentryBreadcrumps.log"
    // def breadcrumpsLogStr = getBreadcrumpsStrLogfile(config)
    // def breadcrumpsCliStr = getBreadcrumpsStrParameter(config)

    

    node {
        def sentryDsn = config.sentryDsn
        //println "logErrorToSentry sentryDsn:" + sentryDsn

        def breadcrumpFile = "sentryBreadcrumps.log"
        def breadcrumpsLogStr = getBreadcrumpsStrLogfile(config)
        def breadcrumpsCliStr = getBreadcrumpsStrParameter(config)
        jenkinsMaterializeLog script:this, { name -> 
            sh "mv '$name' '$breadcrumpFile'"
        }
        // debug:
        echo "logErrorToSentry CLI-Call: ./sentry-cli send-event -t stepName:'$stepName' -t stageName:'$stageName' -m '$errMessage'"
        
        // supress output
        //sh(returnStdout: false, script: "set +x && echo '$breadcrumpsLogStr' > '$breadcrumpFile'")

        sh(returnStdout: true, script: """
sed -i '/\\[8mha/d' ``'$breadcrumpFile' # remove raw formatted lines
export SENTRY_DSN='$sentryDsn'

if test -f sentry-cli; then
    echo "[logErrorToSentry]: sentry-cli already initialized"
else
    echo "[logErrorToSentry]: Initializing sentry-cli"
    curl -o sentry-cli https://downloads.sentry-cdn.com/sentry-cli/1.51.1/sentry-cli-Linux-x86_64
    chmod +x sentry-cli
fi
./sentry-cli send-event -t stepName:'$stepName' -t stageName:'$stageName' -t job:'${env.JOB_URL}' -m '$errMessage' -e testKey:testValue $breadcrumpsCliStr --logfile '$breadcrumpFile'
        """)
    } 
}

private String getBreadcrumpsStrLogfile(Map config) {
    def str = ''
    config.each {key, value ->
        str += '' + key + ': ' + value + '\n'
    }
    return str
}

private String getBreadcrumpsStrParameter(Map config) {
    def str = ''
    config.each {key, value ->
        str += ' -e "' + key + '":"' + value + '"'
    }
    return str
}