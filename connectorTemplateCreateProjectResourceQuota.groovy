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
        def toolJsonValue=jsonSlurper.parseText(value) 
        def user = "${toolJsonValue['username']}"
        def pass = "${toolJsonValue['password']}"
        def toolUrl = "${toolJsonValue['url']}"
        def command1 = "oc login --server=$toolUrl -u $user -p $pass"
        def proc1 = command1.execute()
        proc1.waitFor()
        def command2 = "oc whoami -t"
        def proc2 = command2.execute()
        proc2.waitFor()
        def token = proc2.in.text
        token = token.replaceAll("\\n", "")
        def toolsJson = JsonOutput.toJson([user: "${user}", password: "${pass}", toolUrl: "${toolUrl}",rigletName: "${rigletName}", rigUrl: "${rigUrl}", toolName: "${toolName}"])
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
def saveProjectDetails(rigletName, toolUrl, rigUrl, projName, toolName, rqName, pods, requestsCpu, requestsMemory, requestsEphemeralStorage, limitsCpu , limitsMemory, limitsEphemeralStorage) { 
    String projUrl= "${toolUrl}" 
    def saveProjectDetail = new URL(rigUrl+"/api/riglets/saveToolProjectInfo").openConnection();
    def message = '{"rigletName":"'+rigletName+'","toolName":"'+toolName+'","url":"'+projUrl+',"projectId":"'+projectKey+'","projectName":"'+projName+'","resourcequotaName":"'+rqName+'","pods":"'+pods+'","requestsCpu":"'+requestsCpu+'","requestsMemory":"'+requestsMemory+'","requestsEphemeralStorage":"'+requestsEphemeralStorage+'","limitsCpu":"'+limitsCpu+'","limitsMemory":"'+limitsMemory+'","limitsEphemeralStorage":"'+limitsEphemeralStorage+'"}'
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

//Creating Project function
def createProject(toolDetails, projName, action) {    
    def jsonSlurper = new JsonSlurper()
    def projectUrl = toolDetails.toolUrl +"/apis/project.openshift.io/v1/projects"
    def token = toolDetails.password
    def rigletName = toolDetails.rigletName
    def rigUrl = toolDetails.rigUrl
    def toolName = toolDetails.toolName
    
    def data = "{\"kind\": \"ProjectRequest\", \"apiVersion\": \"project.openshift.io/v1\", \"metadata\": { \"name\": \"$projName\" }}"
    def curl_cmd = "curl -s -w '%{http_code}' -o response.txt -k -X POST '$projectUrl' -H \"Authorization: Bearer $token\" -H 'Accept: application/json' -H 'Content-Type: application/json' -d '$data' > response_code.txt"
    def jobDetail = ['sh','-c',"$curl_cmd"].execute()

    jobDetail.waitFor()
    println jobDetail.err.text
    String response_code = new File('response_code.txt').text
    def actionStatus
    if ( response_code == 200 || response_code == 201)
     {
      updateActionsToRig("Success", rigletName, rigUrl, toolName, action);
      actionStatus1 = JsonOutput.toJson([isActionSucceeded: true, projectName:"${projName}"]);
     }
     else
     {
       updateActionsToRig("Failure", rigletName, rigUrl, toolName, action);
       actionStatus1 = JsonOutput.toJson([isActionSucceeded: false, projectName: null]);
     }
    
        return actionStatus1
}

//Creating Resource quota function
def createResourceQuota(toolDetails, projName, action, rqName, pods, requestsCpu, requestsMemory, requestsEphemeralStorage, limitsCpu, limitsMemory, limitsEphemeralStorage) { 
    def jsonSlurper = new JsonSlurper()
    def projectUrl = toolDetails.toolUrl +"/api/v1/namespaces/$projName/resourcequotas/"
    def token = toolDetails.password
    def rigletName = toolDetails.rigletName
    def rigUrl = toolDetails.rigUrl
    def toolName = toolDetails.toolName
    
    def createDataJson = "{ \"kind\": \"ResourceQuota\", \"apiVersion\": \"v1\", \"metadata\": { \"name\":\"$rqName\", \"namespace\":\"$projName\" }, \"spec\": { \"hard\": { \"pods\": \"$pods\", \"requests.cpu\": \"$requestsCpu\", \"requests.memory\": \"$requestsMemory\", \"requests.ephemeral-storage\": \"$requestsEphemeralStorage\", \"limits.cpu\": \"$limitsCpu\", \"limits.memory\": \"$limitsMemory\", \"limits.ephemeral-storage\": \"$limitsEphemeralStorage\" } } }"
    def curl_cmd = "curl -s -w '%{http_code}' -o response.txt -k -X POST '$projectUrl' -H \"Authorization: Bearer $token\" -H 'Accept: application/json' -H 'Content-Type: application/json' -d '$createDataJson' > response_code.txt"
    def jobDetail = ['sh','-c',"$curl_cmd"].execute()

    jobDetail.waitFor()
    println jobDetail.err.text
    String response_code = new File('response_code.txt').text
    def actionStatus
    if ( response_code == 200 || response_code == 201)
     {
      updateActionsToRig("Success", rigletName, rigUrl, toolName, action);
      actionStatus2 = JsonOutput.toJson([isActionSucceeded: true, projectName:"${projName}", resourcequotaName:"${rqName}", pods:"${pods}", requestsCpu:"${requestsCpu}", requestsMemory:"${requestsMemory}", requestsEphemeralStorage:"${requestsEphemeralStorage}", limitsCpu:"${limitsCpu}", limitsMemory:"${limitsMemory}", limitsEphemeralStorage:"${limitsEphemeralStorage}"]);
     }
     else
     {
       updateActionsToRig("Failure", rigletName, rigUrl, toolName, action);
       actionStatus2 = JsonOutput.toJson([isActionSucceeded: false, projectName: null, resourcequotaName: null, pods: null, requestsCpu: null, requestsMemory: null, requestsEphemeralStorage: null, limitsCpu: null, limitsMemory: null, limitsEphemeralStorage: null]);
     }
    
        return actionStatus2
}

def main() {

    def inputJson = parseInput(args[1]);
    def createRqJson=parseInput(args[2]);
    String rigUrl = args[0];
    String toolName=inputJson.environment.tool.name; 
    String projName = inputJson.environment.project.name
    String rigletName=inputJson.riglet_info.name;
    String rqName=inputJson.environment.resourcequota.name
    String pods =  createRqJson.spec.hard.pods
    String requestsCpu = createRqJson.spec.hard.requests.cpu
    String requestsMemory = createRqJson.spec.hard.requests.memory
    String requestsEphemeralStorage = createRqJson.spec.hard.requests.ephemeral-storage
    String limitsCpu = createRqJson.spec.hard.limits.cpu
    String limitsMemory = createRqJson.spec.hard.limits.memory
    String limitsEphemeralStorage = createRqJson.spec.hard.limits.ephemeral-storage

    def toolDetails = parseInput(getToolDetails(rigUrl, rigletName, toolName));
    String toolUrl = toolDetails.toolUrl;
    
    def actionOne = "Project creation"
    def createdStatus = parseInput(createProject(toolDetails, projName,actionOne));
    
    def actionTwo = "Creating Resource quotas"
    def createdStatusRq = parseInput(createResourceQuota(toolDetails, projName, actionTwo, rqName, pods, requestsCpu, requestsMemory, requestsEphemeralStorage, limitsCpu, limitsMemory, limitsEphemeralStorage));
    
    if(createdStatus.isActionSucceeded && createdStatusRq.isActionSucceeded) {   
        saveProjectDetails(rigletName, toolUrl, rigUrl, createdStatusRq.projectName, toolName, createdStatusRq.resourcequotaName, createdStatusRq.pods, createdStatusRq.requestsCpu, createdStatusRq.requestsMemory, createdStatusRq.requestsEphemeralStorage, createdStatusRq.limitsCpu, createdStatusRq.limitsMemory, createdStatusRq.limitsEphemeralStorage) ;
    } 
    else {
        saveProjectDetails(rigletName, toolUrl, rigUrl, createdStatusRq.projectName, toolName, createdStatusRq.resourcequotaName, createdStatusRq.pods, createdStatusRq.requestsCpu, createdStatusRq.requestsMemory, createdStatusRq.requestsEphemeralStorage, createdStatusRq.limitsCpu, createdStatusRq.limitsMemory, createdStatusRq.limitsEphemeralStorage) ;
    }
}

main();



---------------------------------------------------------------------------------
---------------------------------------------------------------------------------


//Bharath Code


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
        def toolJsonValue=jsonSlurper.parseText(value) 
        
        def user = "${toolJsonValue['username']}"
        
        def pass = "${toolJsonValue['password']}"
        
        def toolUrl = "${toolJsonValue['url']}"
        
        def command1 = "oc login --server=$toolUrl -u $user -p $pass"
        def proc1 = command1.execute()
        proc1.waitFor()
        def command2 = "oc whoami -t"
        def proc2 = command2.execute()
        proc2.waitFor()
        def token = proc2.in.text
        token = token.replaceAll("\\n", "")
        
        def toolsJson = JsonOutput.toJson([user: "${user}", password: "${pass}", toolUrl: "${toolUrl}",rigletName: "${rigletName}", rigUrl: "${rigUrl}", toolName: "${toolName}"])
        return toolsJson;
    }
    else {
        println "Error status Code get tools details - ${toolDetailsRC}"
        println "${toolDetails.getInputStream().getText()}"
        return;
    }
}
// update action results to rig - to be called in each action
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
def updateProjectDetails(rigletName, toolUrl, rigUrl, projName, toolName, rqName, pods, requestsCpu, requestsMemory, requestsEphemeralStorage, limitsCpu , limitsMemory, limitsEphemeralStorage) { 
    String projUrl= "${toolUrl}" 
    def updateProjectDetail = new URL(rigUrl+"/api/riglets/updateToolProjectInfo").openConnection();
    def message = '{"rigletName":"'+rigletName+'","toolName":"'+toolName+'","url":"'+projUrl+',"projectId":"'+projectKey+'","projectName":"'+projName+'","resourcequotaName":"'+rqName+'","pods":"'+pods+'","requestsCpu":"'+requestsCpu+'","requestsMemory":"'+requestsMemory+'","requestsEphemeralStorage":"'+requestsEphemeralStorage+'","limitsCpu":"'+limitsCpu+'","limitsMemory":"'+limitsMemory+'","limitsEphemeralStorage":"'+limitsEphemeralStorage+'"}'
    updateProjectDetail.setRequestMethod("POST")
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
}
def actionOne(toolDetails, projName,action1) { // perform action 1    
    def jsonSlurper = new JsonSlurper()
    def projectUrl = toolDetails.toolUrl +"/apis/project.openshift.io/v1/projectrequests"
    def token = toolDetails.password
    def rigletName = toolDetails.rigletName
    def rigUrl = toolDetails.rigUrl
    def toolName = toolDetails.toolName
    
    def data = "{\"kind\": \"ProjectRequest\", \"apiVersion\": \"project.openshift.io/v1\", \"metadata\": { \"name\": \"$projName\" }}"
    
    def curl_cmd = "curl -s -w '%{http_code}' -o response.txt -k -X POST '$projectUrl' -H \"Authorization: Bearer $token\" -H 'Accept: application/json' -H 'Content-Type: application/json' -d '$data' > response_code.txt"
    
    def jobDetail = ['sh','-c',"$curl_cmd"].execute()
    jobDetail.waitFor()
    println jobDetail.err.text
    String response_code = new File('response_code.txt').text
    def actionStatus
    if ( response_code == 200 || response_code == 201)
     {
      updateActionsToRig("Success", rigletName, rigUrl, toolName, action1);
      actionStatus = JsonOutput.toJson([isActionSucceeded: true, projectName:"${projName}"]);
     }
     else
     {
       updateActionsToRig("Failure", rigletName, rigUrl, toolName, action1);
       actionStatus = JsonOutput.toJson([isActionSucceeded: false, projectName: null]);
     }
    
        return actionStatus
}
def actionTwo(toolDetails, projName, action2, rqName, pods, requestsCpu, requestsMemory, requestsEphemeralStorage, limitsCpu, limitsMemory, limitsEphemeralStorage) { // perform action 1    
    def jsonSlurper = new JsonSlurper()
    def projectUrl = toolDetails.toolUrl +"/api/v1/namespaces/$projName/resourcequotas"
    def token = toolDetails.password
    def rigletName = toolDetails.rigletName
    def rigUrl = toolDetails.rigUrl
    def toolName = toolDetails.toolName
    
    def updateDataJson = "{ \"kind\": \"ResourceQuota\", \"apiVersion\": \"v1\", \"metadata\": { \"name\":\"$rqName\", \"namespace\":\"$projName\" }, \"spec\": { \"hard\": { \"pods\": \"$pods\", \"requests.cpu\": \"$requestsCpu\", \"requests.memory\": \"$requestsMemory\", \"requests.ephemeral-storage\": \"$requestsEphemeralStorage\", \"limits.cpu\": \"$limitsCpu\", \"limits.memory\": \"$limitsMemory\", \"limits.ephemeral-storage\": \"$limitsEphemeralStorage\" } } }"
    def curl_cmd = "curl -s -w '%{http_code}' -o response.txt -k -X POST '$projectUrl' -H \"Authorization: Bearer $token\" -H 'Accept: application/json' -H 'Content-Type: application/json' -d '$updateDataJson' > response_code.txt"
    def jobDetail = ['sh','-c',"$curl_cmd"].execute()

    jobDetail.waitFor()
    println jobDetail.err.text
    String response_code = new File('response_code.txt').text
    def actionStatus
    if ( response_code == 200 || response_code == 201)
     {
      updateActionsToRig("Success", rigletName, rigUrl, toolName, action2);
      actionStatus = JsonOutput.toJson([isActionSucceeded: true, projectName:"${projName}", resourcequotaName:"${rqName}", pods:"${pods}", requestsCpu:"${requestsCpu}", requestsMemory:"${requestsMemory}", requestsEphemeralStorage:"${requestsEphemeralStorage}", limitsCpu:"${limitsCpu}", limitsMemory:"${limitsMemory}", limitsEphemeralStorage:"${limitsEphemeralStorage}"]);
     }
     else
     {
       updateActionsToRig("Failure", rigletName, rigUrl, toolName, action2);
       actionStatus = JsonOutput.toJson([isActionSucceeded: false, projectName: null, resourcequotaName: null, pods: null, requestsCpu: null, requestsMemory: null, requestsEphemeralStorage: null, limitsCpu: null, limitsMemory: null, limitsEphemeralStorage: null]);
     }
    
        return actionStatus
}

def main() {
    def inputJson = parseInput(args[1]);
    def updateJson=parseInput(args[2]);
    String rigUrl = args[0];
    String toolName=inputJson.environment.tool.name; 
    String projName = inputJson.environment.project.name
    String rigletName=inputJson.riglet_info.name;
    //(dbt:How to pass resorce quota name)String rqName=inputJson.environment.resourcequota.name
    String pods =  updateJson.spec.hard.pods
    String requestsCpu = updateJson.spec.hard.requests.cpu
    String requestsMemory = updateJson.spec.hard.requests.memory
    String requestsEphemeralStorage = updateJson.spec.hard.requests.ephemeral-storage
    String limitsCpu = updateJson.spec.hard.limits.cpu
    String limitsMemory = updateJson.spec.hard.limits.memory
    String limitsEphemeralStorage = updateJson.spec.hard.limits.ephemeral-storage

    def toolDetails = parseInput(getToolDetails(rigUrl, rigletName, toolName));
    String toolUrl = toolDetails.toolUrl;
    def action1 = "Project Creation"
    def action2 = "Resource Quotas Creation"
    def createdStatus = parseInput(actionOne(toolDetails, projName,action1));
    def updatedStatus = parseInput(actionTwo(toolDetails, projName, action2, rqName, pods, requestsCpu, requestsMemory, requestsEphemeralStorage, limitsCpu, limitsMemory, limitsEphemeralStorage));
    
    if(createdStatus.isActionSucceeded && updatedStatus.isActionSucceeded) {   
        updateProjectDetails(rigletName, toolUrl, rigUrl, updatedStatus.projectName, toolName, updatedStatus.resourcequotaName, updatedStatus.pods, updatedStatus.requestsCpu, updatedStatus.requestsMemory, updatedStatus.requestsEphemeralStorage, updatedStatus.limitsCpu, updatedStatus.limitsMemory, updatedStatus.limitsEphemeralStorage) ;
    } 
    else {
        updateProjectDetails(rigletName, toolUrl, rigUrl, updatedStatus.projectName, toolName, updatedStatus.resourcequotaName, updatedStatus.pods, updatedStatus.requestsCpu, updatedStatus.requestsMemory, updatedStatus.requestsEphemeralStorage, updatedStatus.limitsCpu, updatedStatus.limitsMemory, updatedStatus.limitsEphemeralStorage);
    }
}

main();