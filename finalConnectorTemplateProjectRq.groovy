import groovy.json.*
import groovy.json.JsonSlurper;

// parse json string
def parseInput(jsonString) { 
    def jsonSlurper = new JsonSlurper()
    def jsonObj = jsonSlurper.parseText(jsonString)
    return jsonObj;
}

// get tool details from the riglet 
def getToolDetails(rigUrl, rigletName, toolName) {
    def toolDetails = new URL(rigUrl+"/api/riglets/connectorServerDetails").openConnection();
   def message = '{"rigletName":"'+rigletName+'","toolName":"'+toolName+'"}'
   toolDetails.setRequestMethod("POST")
    toolDetails.setDoOutput(true)
    toolDetails.setRequestProperty("Content-Type", "application/json")
    toolDetails.getOutputStream().write(message.getBytes("UTF-8"));
    def toolDetailsRC = toolDetails.getResponseCode();
    if(toolDetailsRC.equals(200) || toolDetailsRC.equals(201)) {
        def value = toolDetails.getInputStream().getText();
        def jsonSlurper = new JsonSlurper()
        def toolJsonValue=jsonSlurper.parseText(value) 
        println "test" + toolJsonValue
        def user = "${toolJsonValue['username']}"
        def pass = "${toolJsonValue['password']}"
        def toolUrl = "${toolJsonValue['url']}"
        def command1 = "oc login --server=$toolUrl -u $user -p $pass"
        println command1
        def proc1 = command1.execute()
        proc1.waitFor()
        def command2 = "oc whoami -t"
        def proc2 = command2.execute()
        proc2.waitFor()
        def token = proc2.in.text
        token = token.replaceAll("\\n", "")
        println token
        def toolsJson = JsonOutput.toJson([user: "${user}", password: "${pass}", toolUrl: "${toolUrl}",rigletName: "${rigletName}", rigUrl: "${rigUrl}", toolName: "${toolName}", token:"${token}"])
        return toolsJson;
    }
    else {
        println "Error status Code get tools details - ${toolDetailsRC}"
        println "${toolDetails.getInputStream().getText()}"
        return;
    }
}

// Update action results to rig - to be called in each action
def updateActionsToRig(status, rigletName, rigUrl, toolName, action) {
    def statusUpdateDetail = new URL(rigUrl+"/api/riglets/statusChange").openConnection();
    def message = '{"rigletName":"'+rigletName+'","toolName":"'+toolName+'","action":"'+action+'","status":"'+status+'"}'
    statusUpdateDetail.setRequestMethod("POST")
    statusUpdateDetail.setDoOutput(true)
    statusUpdateDetail.setRequestProperty("Content-Type", "application/json")
    statusUpdateDetail.getOutputStream().write(message.getBytes("UTF-8"));
    def statusUpdateDetailRC = statusUpdateDetail.getResponseCode();
    if(statusUpdateDetailRC.equals(200) || statusUpdateDetailRC.equals(201)) {
        println(statusUpdateDetail.getInputStream().getText());
    }
    else {
        println "Error status Code of Status Update - ${statusUpdateDetailRC}"
    }
}

//Save Project details to Rig 
/*def saveProjectDetails(rigletName, toolUrl, rigUrl, projName, toolName, requestsCpu, requestsMemory, persistantStorage, backupStorage, logfileStorage, servicesNodeports) { 
    String projUrl= "${toolUrl}" 
    def saveProjectDetail = new URL(rigUrl+"/api/riglets/saveToolProjectInfo").openConnection();
    def message = '{"rigletName":"'+rigletName+'","toolName":"'+toolName+'","url":"'+projUrl+',"projectId":"'+projectKey+'","projectName":"'+projName+'","requestsCpu":"'+requestsCpu+'","requestsMemory":"'+requestsMemory+'","persistentStorage":"'+persistentStorage+'","backupStorage":"'+backupStorage+'","logfileStorage":"'+logfileStorage+'","servicesNodeports":"'+servicesNodeports+'"}'
    saveProjectDetail.setRequestMethod("POST")
    saveProjectDetail.setDoOutput(true)
    saveProjectDetail.setRequestProperty("Content-Type", "application/json")
    saveProjectDetail.getOutputStream().write(message.getBytes("UTF-8"));
    def saveProjectDetailRC = saveProjectDetail.getResponseCode();
    if(saveProjectDetailRC.equals(200) || saveProjectDetailRC.equals(201)) {
        println(saveProjectDetail.getInputStream().getText());
    }
    else {
        println "Error status Code of Save Project Details-  ${saveProjectDetailRC}"   
    }
}*/
def saveProjectDetails(rigletName, toolUrl, rigUrl, projName, toolName) {
    String projUrl= "${toolUrl}" // create the url based on the tool
    def saveProjectDetail = new URL(rigUrl+"/api/riglets/saveToolProjectInfo").openConnection();
    def message = '{"rigletName":"'+rigletName+'","toolName":"'+toolName+'","url":"'+projUrl+',"projectId":"'+projectKey+'","projectName":"'+projName+'"}'
    saveProjectDetail.setRequestMethod("POST")
    saveProjectDetail.setDoOutput(true)
    saveProjectDetail.setRequestProperty("Content-Type", "application/json")
    saveProjectDetail.getOutputStream().write(message.getBytes("UTF-8"));
    def saveProjectDetailRC = saveProjectDetail.getResponseCode();
    if(saveProjectDetailRC.equals(200) || saveProjectDetailRC.equals(201)) {
        println(saveProjectDetail.getInputStream().getText());
    }
    else {
        println "Error status Code of Save Project Details-  ${saveProjectDetailRC}"

    }
}


//Creating Project with Resource quota function
def createProjectResourceQuota(toolDetails, projName, action, requestsCpu, requestsMemory, persistentStorage, backupStorage, logfileStorage, servicesNodeports) { 
    def jsonSlurper = new JsonSlurper()
    def projectUrl = toolDetails.toolUrl +"/apis/project.openshift.io/v1/projects"
    def token = toolDetails.token
    println token
    def rigletName = toolDetails.rigletName
    def rigUrl = toolDetails.rigUrl
    def toolName = toolDetails.toolName
    
    def projectData = "{\"kind\": \"ProjectRequest\", \"apiVersion\": \"project.openshift.io/v1\", \"metadata\": { \"name\": \"$projName\" }}"
    def curl_cmd = "curl -s -w '%{http_code}' -o response.txt -k -X POST '$projectUrl' -H \"Authorization: Bearer $token\" -H 'Accept: application/json' -H 'Content-Type: application/json' -d '$projectData' > response_code.txt"
    def projectJobDetail = ['sh','-c',"$curl_cmd"].execute()

    projectJobDetail.waitFor()
    println projectJobDetail.err.text
    String response_code = new File('response_code.txt').text

    println response_code
    def resourceQuotaUrl = toolDetails.toolUrl +"/api/v1/namespaces/$projName/resourcequotas"
    
    def resourceQuotaDataJson = "{ \"kind\": \"ResourceQuota\", \"apiVersion\": \"v1\", \"metadata\": { \"name\":\"$projName\", \"namespace\":\"$projName\" }, \"spec\": { \"hard\": { \"requests.cpu\": \"$requestsCpu\", \"requests.memory\": \"$requestsMemory\", \"app.storageclass.storage.k8s.io/requests.storage\": \"$persistentStorage\", \"backup.storageclass.storage.k8s.io/requests.storage\": \"$backupStorage\", \"logging.storageclass.storage.k8s.io/requests.storage\": \"$logfileStorage\", \"services.nodeports\": \"$servicesNodeports\" } }  }"
    def curl_cmd1 = "curl -s -w '%{http_code}' -o response1.txt -k -X POST '$resourceQuotaUrl' -H \"Authorization: Bearer $token\" -H 'Accept: application/json' -H 'Content-Type: application/json' -d '$resourceQuotaDataJson' > response_code1.txt"
    def rqJobDetail = ['sh','-c',"$curl_cmd1"].execute()

    rqJobDetail.waitFor()
    println rqJobDetail.err.text
    String response_code1 = new File('response_code1.txt').text
    println response_code1

    def actionStatus
    actionStatus = JsonOutput.toJson([isActionSucceeded: true, projectName:"${projName}", requestsCpu:"${requestsCpu}", requestsMemory:"${requestsMemory}", persistentStorage:"${persistentStorage}", backupStorage:"${backupStorage}", logfileStorage:"${logfileStorage}", servicesNodeports:"${servicesNodeports}" ]);
    /*if ( response_code1 == 200 || response_code1 == 201)
     {
      updateActionsToRig("Success", rigletName, rigUrl, toolName, action);
      actionStatus = JsonOutput.toJson([isActionSucceeded: true, projectName:"${projName}", requestsCpu:"${requestsCpu}", requestsMemory:"${requestsMemory}", persistentStorage:"${persistentStorage}", backupStorage:"${backupStorage}", logfileStorage:"${logfileStorage}", servicesNodeports:"${serviceNodeports}" ]);
     }
     else
     {
       updateActionsToRig("Failure", rigletName, rigUrl, toolName, action);
       actionStatus = JsonOutput.toJson([isActionSucceeded: true, projectName: null, requestsCpu: null, requestsMemory: null, persistentStorage: null, backupStorage: null, logfileStorage: null, servicesNodeports: null ]);
     }*/
    
        return actionStatus
}

def main() {
    def inputJson = parseInput(args[1]);
    String rigUrl = args[0];
    String toolName=inputJson.environment.tool.name; 
    println toolName
    String projName = inputJson.environment.project.name
    println projName
    String rigletName=inputJson.riglet_info.name;
    println rigUrl
    println rigletName
    String requestsCpu = inputJson.environment.projectresourcequota.requestsCpu
    String requestsMemory = inputJson.environment.projectresourcequota.requestsMemory
    String persistentStorage = inputJson.environment.projectresourcequota.persistentStorage
    String backupStorage = inputJson.environment.projectresourcequota.backupStorage
    String logfileStorage = inputJson.environment.projectresourcequota.logfileStorage
    String servicesNodeports = inputJson.environment.projectresourcequota.servicesNodeports
    println requestsCpu
    println requestsMemory
    println persistentStorage
    println backupStorage
    println logfileStorage
    println servicesNodeports
    def toolDetails = parseInput(getToolDetails(rigUrl, rigletName, toolName));
    String toolUrl = toolDetails.toolUrl;
    
    println toolDetails
    def action = "Creating Project and Resource quotas"
    def createdStatus = parseInput(createProjectResourceQuota(toolDetails, projName, action, requestsCpu, requestsMemory, persistentStorage, backupStorage, logfileStorage, servicesNodeports));
    println createdStatus
   /* if(createdStatus.isActionSucceeded) {   
        saveProjectDetails(rigletName, toolUrl, rigUrl, createdStatus.projectName, toolName, createdStatus.requestsCpu, createdStatus.requestsMemory, createdStatus.persistentStorage, createdStatus.backupStorage, createdStatus.logfileStorage, createdStatus.servicesNodeports) ;
    } 
    else {
        saveProjectDetails(rigletName, toolUrl, rigUrl, createdStatusRq.projectName, toolName, createdStatusRq.resourcequotaName, createdStatusRq.pods, createdStatusRq.requestsCpu, createdStatusRq.requestsMemory, createdStatusRq.requestsEphemeralStorage, createdStatusRq.limitsCpu, createdStatusRq.limitsMemory, createdStatusRq.limitsEphemeralStorage) ;
    }*/
}

main();

