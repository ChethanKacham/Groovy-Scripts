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
        println command2
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
//Update Project details to Rig 
/*def updateProjectDetails(rigletName, toolUrl, rigUrl, projName, action, requests_Cpu, requests_Memory, persistent_Storage, backup_Storage, log_file_Storage, services_Node_ports) { 
    String projUrl= "${toolUrl}" 
    def updateProjectDetail = new URL(rigUrl+"/api/riglets/updateToolProjectInfo").openConnection();
    def message = '{"rigletName":"'+rigletName+'","toolName":"'+toolName+'","url":"'+projUrl+',"projectId":"'+projectKey+'","projectName":"'+projName+'","resourcequotaName":"'+rqName+'","pods":"'+pods+'","requestsCpu":"'+requestsCpu+'","requestsMemory":"'+requestsMemory+'","requestsEphemeralStorage":"'+requestsEphemeralStorage+'","limitsCpu":"'+limitsCpu+'","limitsMemory":"'+limitsMemory+'","limitsEphemeralStorage":"'+limitsEphemeralStorage+'"}'
    updateProjectDetail.setRequestMethod("PUT")
    updateProjectDetail.setDoOutput(true)
    updateProjectDetail.setRequestProperty("Content-Type", "application/json")
    updateProjectDetail.getOutputStream().write(message.getBytes("UTF-8"));
    def updateProjectDetailRC = updateProjectDetail.getResponseCode();
    if(updateProjectDetailRC.equals(200) || updateProjectDetailRC.equals(201)) {
        println(updateProjectDetail.getInputStream().getText());
    }
    else {
        println "Error status Code of Save Project Details-  ${updateProjectDetailRC}"
      
    }
}*/
def updateProjectDetails(rigletName, toolUrl, rigUrl, projName, toolName) {
    String projUrl= "${toolUrl}" // create the url based on the tool
    def saveProjectDetail = new URL(rigUrl+"/api/riglets/updateToolProjectInfo").openConnection();
    def message = '{"rigletName":"'+rigletName+'","toolName":"'+toolName+'","url":"'+projUrl+',"projectId":"'+projectKey+'","projectName":"'+projName+'"}'
    saveProjectDetail.setRequestMethod("PUT")
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

//Updating Project Resource quota values
def updateProject(toolDetails, projName, action, requests_Cpu, requests_Memory, persistent_Storage, backup_Storage, log_file_Storage, services_Node_ports) {    
    def jsonSlurper = new JsonSlurper()
    def projectUrl = toolDetails.toolUrl +"/api/v1/namespaces/$projName/resourcequotas/$projName"
    def token = toolDetails.token
    println token
    def rigletName = toolDetails.rigletName
    def rigUrl = toolDetails.rigUrl
    def toolName = toolDetails.toolName
    
    def updateDataJson = "{ \"kind\": \"ResourceQuota\", \"apiVersion\": \"v1\", \"metadata\": { \"name\":\"$projName\", \"namespace\":\"$projName\" }, \"spec\": { \"hard\": { \"requests.cpu\": \"$requests_Cpu\", \"requests.memory\": \"$requests_Memory\", \"app.storageclass.storage.k8s.io/requests.storage\": \"$persistent_Storage\", \"backup.storageclass.storage.k8s.io/requests.storage\": \"$backup_Storage\", \"logging.storageclass.storage.k8s.io/requests.storage\": \"$log_file_Storage\", \"services.nodeports\": \"$services_Node_ports\" } } }"
    def curl_cmd = "curl -s -w '%{http_code}' -o response.txt -k -X PUT '$projectUrl' -H \"Authorization: Bearer $token\" -H 'Accept: application/json' -H 'Content-Type: application/json' -d '$updateDataJson' > response_code.txt"
    def jobDetail = ['sh','-c',"$curl_cmd"].execute()

    jobDetail.waitFor()
    println jobDetail.err.text


    String response_code = new File('response_code.txt').text
    println response_code
    def actionStatus
    actionStatus = JsonOutput.toJson([isActionSucceeded: true, projectName:"${projName}", requestsCpu:"${requests_Cpu}", requestsMemory:"${requests_Memory}", persistentStorage:"${persistent_Storage}", backupStorage:"${backup_Storage}", logfileStorage:"${log_file_Storage}", servicesNodeports:"${services_Nodeports}"]);
    /*if ( response_code == 200 || response_code == 201)
     {
      updateActionsToRig("Success", rigletName, rigUrl, toolName, action);
      actionStatus = JsonOutput.toJson([isActionSucceeded: true, projectName:"${projName}", resourcequotaName:"${rqName}", pods:"${pods}", requestsCpu:"${requestsCpu}", requestsMemory:"${requestsMemory}", requestsEphemeralStorage:"${requestsEphemeralStorage}", limitsCpu:"${limitsCpu}", limitsMemory:"${limitsMemory}", limitsEphemeralStorage:"${limitsEphemeralStorage}"]);
     }
     else
     {
       updateActionsToRig("Failure", rigletName, rigUrl, toolName, action);
       actionStatus = JsonOutput.toJson([isActionSucceeded: false, projectName: null, resourcequotaName: null, pods: null, requestsCpu: null, requestsMemory: null, requestsEphemeralStorage: null, limitsCpu: null, limitsMemory: null, limitsEphemeralStorage: null]);
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
    String requests_Cpu = inputJson.environment.projectresourcequota.requestsCpu
    String requests_Memory = inputJson.environment.projectresourcequota.requestsMemory
    String persistent_Storage = inputJson.environment.projectresourcequota.persistentStorage
    String backup_Storage = inputJson.environment.projectresourcequota.backupStorage
    String log_file_Storage = inputJson.environment.projectresourcequota.logfileStorage
    String services_Node_ports = inputJson.environment.projectresourcequota.servicesNodeports
    println requests_Cpu
    println requests_Memory
    println persistent_Storage
    println backup_Storage
    println log_file_Storage
    println services_Node_ports

    def toolDetails = parseInput(getToolDetails(rigUrl, rigletName, toolName));
    println toolDetails
    String toolUrl = toolDetails.toolUrl;
    def action = "Update project details"
    def updatedStatus = parseInput(updateProject(toolDetails, projName, action, requests_Cpu, requests_Memory, persistent_Storage, backup_Storage, log_file_Storage, services_Node_ports));
    println updatedStatus
    /*if(updatedStatus.isActionSucceeded) {   
        updateProjectDetails(rigletName, toolUrl, rigUrl, updatedStatus.projectName, toolName, updatedStatus.resourcequotaName, updatedStatus.pods, updatedStatus.requestsCpu, updatedStatus.requestsMemory, updatedStatus.requestsEphemeralStorage, updatedStatus.limitsCpu, updatedStatus.limitsMemory, updatedStatus.limitsEphemeralStorage) ;
    } 
    else {
        updateProjectDetails(rigletName, toolUrl, rigUrl, updatedStatus.projectName, toolName, updatedStatus.resourcequotaName, updatedStatus.pods, updatedStatus.requestsCpu, updatedStatus.requestsMemory, updatedStatus.requestsEphemeralStorage, updatedStatus.limitsCpu, updatedStatus.limitsMemory, updatedStatus.limitsEphemeralStorage);
    }*/
}

main();
