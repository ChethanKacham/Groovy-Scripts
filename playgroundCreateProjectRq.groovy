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
   /* def toolDetails = new URL(rigUrl+"/api/riglets/connectorServerDetails").openConnection();
   def message = '{"rigletName":"'+rigletName+'","toolName":"'+toolName+'"}'
   toolDetails.setRequestMethod("POST")
    toolDetails.setDoOutput(true)
    toolDetails.setRequestProperty("Content-Type", "application/json")
    toolDetails.getOutputStream().write(message.getBytes("UTF-8"));
    def toolDetailsRC = toolDetails.getResponseCode();
    if(toolDetailsRC.equals(200) || toolDetailsRC.equals(201)) {
        def value = toolDetails.getInputStream().getText();
        def toolJsonValue=jsonSlurper.parseText(value) 
        */
        //def user = "${toolJsonValue['username']}"
        //def user = ""
       // def pass = "${toolJsonValue['password']}"
        //def pass = "RRU809REi2c6dkcKnl8Y5IwhX20nXgGiJhAufaJl1vk"
        def url="openshift:6443"
        def user = "admin"
        def pass = "admin"

        def command1 = "oc login --server=$url -u $user -p $pass"
        def proc1 = command1.execute()
        proc1.waitFor()
        def command2 = "oc whoami -t"
        def proc2 = command2.execute()
        proc2.waitFor()
        def token = proc2.in.text
        token = token.replaceAll("\\n", "")
        //def toolUrl = "${toolJsonValue['url']}"
        def toolUrl = "https://openshift:6443"
        def toolsJson = JsonOutput.toJson([user: "${user}", password: "${token}", toolUrl: "${toolUrl}"])
        return toolsJson;
     /*else
      {
        println "Error status Code get tools details - ${toolDetailsRC}"
        println "${toolDetails.getInputStream().getText()}"
        return;
    }*/
}
// update action results to rig - to be called in each action
def updateActionsToRig(status, rigletName, rigUrl, toolName, action) {
    /*def statusUpdateDetail = new URL(rigUrl+"/api/riglets/statusChange").openConnection();
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
    }*/
}
//Save Project details to Rig
def saveProjectDetails(rigletName, toolUrl, projName, rigUrl, projId, toolName) { 
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
        //println(saveProjectDetail.getInputStream().getText());
    }
}
def actionOne(toolDetails, projName) { // perform action 1    
    def jsonSlurper = new JsonSlurper()
    def projectUrl = toolDetails.toolUrl +"/apis/project.openshift.io/v1/projects"
    def token = toolDetails.password
    //println projName
    def data = "{\"kind\": \"Project\", \"apiVersion\": \"project.openshift.io/v1\", \"metadata\": { \"name\": \"$projName\" }}"
    //println data
    //println token
    def curl_cmd = "curl -s -w '%{http_code}' -o response.txt -k -X POST '$projectUrl' -H \"Authorization: Bearer $token\" -H 'Accept: application/json' -H 'Content-Type: application/json' -d '$data' > response_code.txt"
    //println (curl_cmd)
    def jobDetail = ['sh','-c',"$curl_cmd"].execute()
    jobDetail.waitFor()
    println jobDetail.err.text
    String response_code = new File('response_code.txt').text
    //String output_response= new File('response.txt').text
    //println output_response
    println response_code
    //println (jobDetail.in.text)
    println projectUrl
    // def postProj = new URL(projectUrl).openConnection();
    //def message = "{\"kind\": \"ProjectRequest\",\"apiVersion\": \"project.openshift.io/v1\",\"metadata\":{\"name\"= \"$projName\"}"
    // postProj.setRequestMethod("POST")
    // postProj.setDoOutput(true)
    // postProj.setRequestProperty("Content-Type", "application/json")
    // postProj.setRequestProperty("PRIVATE-TOKEN", token)
    // postProj.getOutputStream().write(message.getBytes("UTF-8"));
    // def postProjRC = postProj.getResponseCode();
    // println postProjRC
    // if (postProjRC == 200 || postProjRC == 201) {
    //    println (postProj.getInputStream().getText())
    //  }
    return response_code
}
def createResourceQuota(toolDetails, projName, rqName, pods, requestsCpu, requestsMemory, requestsEphemeralStorage, limitsCpu, limitsMemory, limitsEphemeralStorage){
    def jsonSlurper = new JsonSlurper()
    def projectUrl = toolDetails.toolUrl +"/api/v1/namespaces/$projName/resourcequotas"
    def token = toolDetails.password
    //println projName
    def data = "{ \"kind\": \"ResourceQuota\", \"apiVersion\": \"v1\", \"metadata\": { \"name\":\"$rqName\", \"namespace\":\"$projName\" }, \"spec\": { \"hard\": { \"pods\": \"$pods\", \"requests.cpu\": \"$requestsCpu\", \"requests.memory\": \"$requestsMemory\", \"requests.ephemeral-storage\": \"$requestsEphemeralStorage\", \"limits.cpu\": \"$limitsCpu\", \"limits.memory\": \"$limitsMemory\", \"limits.ephemeral-storage\": \"$limitsEphemeralStorage\" } } }"
    //println data
    //println token
    def curl_cmd = "curl -s -w '%{http_code}' -o response.txt -k -X POST '$projectUrl' -H \"Authorization: Bearer $token\" -H 'Accept: application/json' -H 'Content-Type: application/json' -d '$data' > response_code.txt"
    //println (curl_cmd)
    def jobDetail = ['sh','-c',"$curl_cmd"].execute()
    jobDetail.waitFor()
    println jobDetail.err.text
    String response_code = new File('response_code.txt').text
    //String output_response= new File('response.txt').text
    //println output_response
    println response_code
    //println (jobDetail.in.text)
    println projectUrl
    // def postProj = new URL(projectUrl).openConnection();
    //def message = "{\"kind\": \"ProjectRequest\",\"apiVersion\": \"project.openshift.io/v1\",\"metadata\":{\"name\"= \"$projName\"}"
    // postProj.setRequestMethod("POST")
    // postProj.setDoOutput(true)
    // postProj.setRequestProperty("Content-Type", "application/json")
    // postProj.setRequestProperty("PRIVATE-TOKEN", token)
    // postProj.getOutputStream().write(message.getBytes("UTF-8"));
    // def postProjRC = postProj.getResponseCode();
    // println postProjRC
    // if (postProjRC == 200 || postProjRC == 201) {
    //    println (postProj.getInputStream().getText())
    //  }
    return response_code

}


def main() {
    //def inputJson = parseInput(args[1]);
    //boolean create_status = inputJson.tool_info.openshift.create;
    
   // String rigUrl = args[0];
   String rigUrl = "http://rig.wipro.com"
    // depends on the input json schema for which the connector is written
   // String toolName=inputJson.environment.tool.name; 
    String toolName = "openshift"
    //String projName = inputJson.environment.projects.project.name
    String projName = "daimler"
    String rigletName = "Tycoon"
    String rqName = "resourcequota"
    String pods = "6"
    String limitsCpu = "3"
    String limitsMemory = "3Gi"
    String limitsEphemeralStorage = "4Gi"
    String requestsCpu = "1"
    String requestsMemory = "1Gi"
    String requestsEphemeralStorage = "2Gi"
    //String rigletName=inputJson.riglet_info.name;
    // extract params based on the requirement and inputJson structure
    
    
    def toolDetails = parseInput(getToolDetails(rigUrl, rigletName, toolName));
    String toolUrl = toolDetails.toolUrl;
    def status = ""
    def action = "Project creation"
    def createdStatus = actionOne(toolDetails, projName);
    createResourceQuota(toolDetails, projName, rqName, pods, requestsCpu, requestsMemory, requestsEphemeralStorage, limitsCpu, limitsMemory, limitsEphemeralStorage);
    if ( createdStatus == 200 || createdStatus == 201)
     {
        status = "Success"
     }
     else
     {
        status = "Failure"
     }
    // updateActionsToRig(status, rigletName, rigUrl, toolName, action)
    //actionTwo(toolDetails, inputJson);
    
    // projectName and Id should be from the createProject action
   // saveProjectDetails(rigletName, toolUrl, projName, rigUrl, projId, toolName);
}
main();

----------------------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------------------


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
   /* def toolDetails = new URL(rigUrl+"/api/riglets/connectorServerDetails").openConnection();
   def message = '{"rigletName":"'+rigletName+'","toolName":"'+toolName+'"}'
   toolDetails.setRequestMethod("POST")
    toolDetails.setDoOutput(true)
    toolDetails.setRequestProperty("Content-Type", "application/json")
    toolDetails.getOutputStream().write(message.getBytes("UTF-8"));
    def toolDetailsRC = toolDetails.getResponseCode();
    if(toolDetailsRC.equals(200) || toolDetailsRC.equals(201)) {
        def value = toolDetails.getInputStream().getText();
        def toolJsonValue=jsonSlurper.parseText(value) 
        */
        //def user = "${toolJsonValue['username']}"
        //def user = ""
       // def pass = "${toolJsonValue['password']}"
        //def pass = "RRU809REi2c6dkcKnl8Y5IwhX20nXgGiJhAufaJl1vk"
        def url="openshift:6443"
        def user = "admin"
        def pass = "admin"

        def command1 = "oc login --server=$url -u $user -p $pass"
        def proc1 = command1.execute()
        proc1.waitFor()
        def command2 = "oc whoami -t"
        def proc2 = command2.execute()
        proc2.waitFor()
        def token = proc2.in.text
        token = token.replaceAll("\\n", "")
        //def toolUrl = "${toolJsonValue['url']}"
        def toolUrl = "https://openshift:6443"
        def toolsJson = JsonOutput.toJson([user: "${user}", password: "${token}", toolUrl: "${toolUrl}"])
        return toolsJson;
     /*else
      {
        println "Error status Code get tools details - ${toolDetailsRC}"
        println "${toolDetails.getInputStream().getText()}"
        return;
    }*/
}
// update action results to rig - to be called in each action
def updateActionsToRig(status, rigletName, rigUrl, toolName, action) {
    /*def statusUpdateDetail = new URL(rigUrl+"/api/riglets/statusChange").openConnection();
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
    }*/
}
//Save Project details to Rig
def saveProjectDetails(rigletName, toolUrl, projName, rigUrl, projId, toolName) { 
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
        //println(saveProjectDetail.getInputStream().getText());
    }
}
def actionOne(toolDetails, projName) { // perform action 1    
    def jsonSlurper = new JsonSlurper()
    def projectUrl = toolDetails.toolUrl +"/apis/project.openshift.io/v1/projects"
    def token = toolDetails.password
    //println projName
    def data = "{\"kind\": \"Project\", \"apiVersion\": \"project.openshift.io/v1\", \"metadata\": { \"name\": \"$projName\" }}"
    //println data
    //println token
    def curl_cmd = "curl -s -w '%{http_code}' -o response.txt -k -X POST '$projectUrl' -H \"Authorization: Bearer $token\" -H 'Accept: application/json' -H 'Content-Type: application/json' -d '$data' > response_code.txt"
    //println (curl_cmd)
    def jobDetail = ['sh','-c',"$curl_cmd"].execute()
    jobDetail.waitFor()
    println jobDetail.err.text
    String response_code = new File('response_code.txt').text
    //String output_response= new File('response.txt').text
    //println output_response
    println response_code
    //println (jobDetail.in.text)
    println projectUrl
    // def postProj = new URL(projectUrl).openConnection();
    //def message = "{\"kind\": \"ProjectRequest\",\"apiVersion\": \"project.openshift.io/v1\",\"metadata\":{\"name\"= \"$projName\"}"
    // postProj.setRequestMethod("POST")
    // postProj.setDoOutput(true)
    // postProj.setRequestProperty("Content-Type", "application/json")
    // postProj.setRequestProperty("PRIVATE-TOKEN", token)
    // postProj.getOutputStream().write(message.getBytes("UTF-8"));
    // def postProjRC = postProj.getResponseCode();
    // println postProjRC
    // if (postProjRC == 200 || postProjRC == 201) {
    //    println (postProj.getInputStream().getText())
    //  }
    return response_code
}
def createResourceQuota(toolDetails, projName, requestsCpu, requestsMemory, persistantStorage, backupStorage, logfileStorage, servicesNodeports){
    def jsonSlurper = new JsonSlurper()
    def projectUrl = toolDetails.toolUrl +"/api/v1/namespaces/$projName/resourcequotas"
    def token = toolDetails.password
    //println projName
    def data = "{ \"kind\": \"ResourceQuota\", \"apiVersion\": \"v1\", \"metadata\": { \"name\":\"$projName\", \"namespace\":\"$projName\" }, \"spec\": { \"hard\": { \"requests.cpu\": \"$requestsCpu\", \"requests.memory\": \"$requestsMemory\", \"app.storageclass.storage.k8s.io/requests.storage\": \"$persistantStorage\", \"backup.storageclass.storage.k8s.io/requests.storage\": \"$backupStorage\", \"logging.storageclass.storage.k8s.io/requests.storage\": \"$logfileStorage\", \"services.nodeports\": \"$servicesNodeports\" } } }"
    //println data
    //println token
    def curl_cmd = "curl -s -w '%{http_code}' -o response.txt -k -X POST '$projectUrl' -H \"Authorization: Bearer $token\" -H 'Accept: application/json' -H 'Content-Type: application/json' -d '$data' > response_code.txt"
    //println (curl_cmd)
    def jobDetail = ['sh','-c',"$curl_cmd"].execute()
    jobDetail.waitFor()
    println jobDetail.err.text
    String response_code = new File('response_code.txt').text
    //String output_response= new File('response.txt').text
    //println output_response
    println response_code
    //println (jobDetail.in.text)
    println projectUrl
    // def postProj = new URL(projectUrl).openConnection();
    //def message = "{\"kind\": \"ProjectRequest\",\"apiVersion\": \"project.openshift.io/v1\",\"metadata\":{\"name\"= \"$projName\"}"
    // postProj.setRequestMethod("POST")
    // postProj.setDoOutput(true)
    // postProj.setRequestProperty("Content-Type", "application/json")
    // postProj.setRequestProperty("PRIVATE-TOKEN", token)
    // postProj.getOutputStream().write(message.getBytes("UTF-8"));
    // def postProjRC = postProj.getResponseCode();
    // println postProjRC
    // if (postProjRC == 200 || postProjRC == 201) {
    //    println (postProj.getInputStream().getText())
    //  }
    return response_code

}


def main() {
    //def inputJson = parseInput(args[1]);
    //boolean create_status = inputJson.tool_info.openshift.create;
    
   // String rigUrl = args[0];
   String rigUrl = "http://rig.wipro.com"
    // depends on the input json schema for which the connector is written
   // String toolName=inputJson.environment.tool.name; 
    String toolName = "openshift"
    //String projName = inputJson.environment.projects.project.name
    String projName = "daimler"
    String rigletName = "Tycoon"
    String requestsCpu = "1"
    String requestsMemory = "1Gi"
    String persistantStorage = "2Gi"
    String backupStorage = "3Gi"
    String logfileStorage = "4Gi"
    String servicesNodeports = "6"
    //String rigletName=inputJson.riglet_info.name;
    // extract params based on the requirement and inputJson structure
    
    
    def toolDetails = parseInput(getToolDetails(rigUrl, rigletName, toolName));
    String toolUrl = toolDetails.toolUrl;
    def status = ""
    def action = "Project creation"
    def createdStatus = actionOne(toolDetails, projName);
    createResourceQuota(toolDetails, projName, requestsCpu, requestsMemory, persistantStorage, backupStorage, logfileStorage, servicesNodeports );
    if ( createdStatus == 200 || createdStatus == 201)
     {
        status = "Success"
     }
     else
     {
        status = "Failure"
     }
    // updateActionsToRig(status, rigletName, rigUrl, toolName, action)
    //actionTwo(toolDetails, inputJson);
    
    // projectName and Id should be from the createProject action
   // saveProjectDetails(rigletName, toolUrl, projName, rigUrl, projId, toolName);
}
main();



------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------

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
   /* def toolDetails = new URL(rigUrl+"/api/riglets/connectorServerDetails").openConnection();
   def message = '{"rigletName":"'+rigletName+'","toolName":"'+toolName+'"}'
   toolDetails.setRequestMethod("POST")
    toolDetails.setDoOutput(true)
    toolDetails.setRequestProperty("Content-Type", "application/json")
    toolDetails.getOutputStream().write(message.getBytes("UTF-8"));
    def toolDetailsRC = toolDetails.getResponseCode();
    if(toolDetailsRC.equals(200) || toolDetailsRC.equals(201)) {
        def value = toolDetails.getInputStream().getText();
        def toolJsonValue=jsonSlurper.parseText(value) 
        */
        //def user = "${toolJsonValue['username']}"
        //def user = ""
       // def pass = "${toolJsonValue['password']}"
        //def pass = "RRU809REi2c6dkcKnl8Y5IwhX20nXgGiJhAufaJl1vk"
        def url="openshift:6443"
        def user = "admin"
        def pass = "admin"

        def command1 = "oc login --server=$url -u $user -p $pass"
        def proc1 = command1.execute()
        proc1.waitFor()
        def command2 = "oc whoami -t"
        def proc2 = command2.execute()
        proc2.waitFor()
        def token = proc2.in.text
        token = token.replaceAll("\\n", "")
        //def toolUrl = "${toolJsonValue['url']}"
        def toolUrl = "https://openshift:6443"
        def toolsJson = JsonOutput.toJson([user: "${user}", password: "${token}", toolUrl: "${toolUrl}"])
        return toolsJson;
     /*else
      {
        println "Error status Code get tools details - ${toolDetailsRC}"
        println "${toolDetails.getInputStream().getText()}"
        return;
    }*/
}
// update action results to rig - to be called in each action
def updateActionsToRig(status, rigletName, rigUrl, toolName, action) {
    /*def statusUpdateDetail = new URL(rigUrl+"/api/riglets/statusChange").openConnection();
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
    }*/
}
//Save Project details to Rig
def saveProjectDetails(rigletName, toolUrl, projName, rigUrl, projId, toolName) { 
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
        //println(saveProjectDetail.getInputStream().getText());
    }
}
/*
def actionOne(toolDetails, projName) { // perform action 1    
    def jsonSlurper = new JsonSlurper()
    def projectUrl = toolDetails.toolUrl +"/apis/project.openshift.io/v1/projects"
    def token = toolDetails.password
    //println projName
    def data = "{\"kind\": \"Project\", \"apiVersion\": \"project.openshift.io/v1\", \"metadata\": { \"name\": \"$projName\" }}"
    //println data
    //println token
    def curl_cmd = "curl -s -w '%{http_code}' -o response.txt -k -X POST '$projectUrl' -H \"Authorization: Bearer $token\" -H 'Accept: application/json' -H 'Content-Type: application/json' -d '$data' > response_code.txt"
    //println (curl_cmd)
    def jobDetail = ['sh','-c',"$curl_cmd"].execute()
    jobDetail.waitFor()
    println jobDetail.err.text
    String response_code = new File('response_code.txt').text
    //String output_response= new File('response.txt').text
    //println output_response
    println response_code
    //println (jobDetail.in.text)
    println projectUrl
    // def postProj = new URL(projectUrl).openConnection();
    //def message = "{\"kind\": \"ProjectRequest\",\"apiVersion\": \"project.openshift.io/v1\",\"metadata\":{\"name\"= \"$projName\"}"
    // postProj.setRequestMethod("POST")
    // postProj.setDoOutput(true)
    // postProj.setRequestProperty("Content-Type", "application/json")
    // postProj.setRequestProperty("PRIVATE-TOKEN", token)
    // postProj.getOutputStream().write(message.getBytes("UTF-8"));
    // def postProjRC = postProj.getResponseCode();
    // println postProjRC
    // if (postProjRC == 200 || postProjRC == 201) {
    //    println (postProj.getInputStream().getText())
    //  }
    return response_code
}*/
def createProjectResourceQuota(toolDetails, projName, requestsCpu, requestsMemory, persistantStorage, backupStorage, logfileStorage, servicesNodeports){
    def jsonSlurper = new JsonSlurper()
    def token = toolDetails.password
    def projectUrl = toolDetails.toolUrl +"/apis/project.openshift.io/v1/projects"
    def projectData = "{\"kind\": \"Project\", \"apiVersion\": \"project.openshift.io/v1\", \"metadata\": { \"name\": \"$projName\" }}"
    //println data
    //println token
    def curl_cmd = "curl -s -w '%{http_code}' -o response.txt -k -X POST '$projectUrl' -H \"Authorization: Bearer $token\" -H 'Accept: application/json' -H 'Content-Type: application/json' -d '$projectData' > response_code.txt"
    //println (curl_cmd)
    def projectJobDetail = ['sh','-c',"$curl_cmd"].execute()
    projectJobDetail.waitFor()
    println projectJobDetail.err.text
    String response_code = new File('response_code.txt').text
    //String output_response= new File('response.txt').text
    //println output_response
    println response_code
    //println (jobDetail.in.text)
    println projectUrl



    def resourceQuotaUrl = toolDetails.toolUrl +"/api/v1/namespaces/$projName/resourcequotas"
    def resourceQuotaData = "{ \"kind\": \"ResourceQuota\", \"apiVersion\": \"v1\", \"metadata\": { \"name\":\"$projName\", \"namespace\":\"$projName\" }, \"spec\": { \"hard\": { \"requests.cpu\": \"$requestsCpu\", \"requests.memory\": \"$requestsMemory\", \"app.storageclass.storage.k8s.io/requests.storage\": \"$persistantStorage\", \"backup.storageclass.storage.k8s.io/requests.storage\": \"$backupStorage\", \"logging.storageclass.storage.k8s.io/requests.storage\": \"$logfileStorage\", \"services.nodeports\": \"$servicesNodeports\" } } }"
    def curl_cmd1 = "curl -s -w '%{http_code}' -o response.txt -k -X POST '$resourceQuotaUrl' -H \"Authorization: Bearer $token\" -H 'Accept: application/json' -H 'Content-Type: application/json' -d '$resourceQuotaData' > response_code1.txt"
    //println (curl_cmd)
    //println data
    //println token
    def resourceQuotaJobDetail = ['sh','-c',"$curl_cmd1"].execute()
    resourceQuotaJobDetail.waitFor()
    println resourceQuotaJobDetail.err.text
    String response_code1 = new File('response_code1.txt').text
    //String output_response= new File('response.txt').text
    //println output_response
    println response_code1
    //println (jobDetail.in.text)
    println resourceQuotaUrl
    // def postProj = new URL(projectUrl).openConnection();
    //def message = "{\"kind\": \"ProjectRequest\",\"apiVersion\": \"project.openshift.io/v1\",\"metadata\":{\"name\"= \"$projName\"}"
    // postProj.setRequestMethod("POST")
    // postProj.setDoOutput(true)
    // postProj.setRequestProperty("Content-Type", "application/json")
    // postProj.setRequestProperty("PRIVATE-TOKEN", token)
    // postProj.getOutputStream().write(message.getBytes("UTF-8"));
    // def postProjRC = postProj.getResponseCode();
    // println postProjRC
    // if (postProjRC == 200 || postProjRC == 201) {
    //    println (postProj.getInputStream().getText())
    //  }
    return response_code1
}


def main() {
    //def inputJson = parseInput(args[1]);
    //boolean create_status = inputJson.tool_info.openshift.create;
    
   // String rigUrl = args[0];
   String rigUrl = "http://rig.wipro.com"
    // depends on the input json schema for which the connector is written
   // String toolName=inputJson.environment.tool.name; 
    String toolName = "openshift"
    //String projName = inputJson.environment.projects.project.name
    String projName = "daimler6"
    String rigletName = "Tycoon"
    String requestsCpu = "1"
    String requestsMemory = "1Gi"
    String persistantStorage = "2Gi"
    String backupStorage = "3Gi"
    String logfileStorage = "4Gi"
    String servicesNodeports = "6"
    //String rigletName=inputJson.riglet_info.name;
    // extract params based on the requirement and inputJson structure
    
    
    def toolDetails = parseInput(getToolDetails(rigUrl, rigletName, toolName));
    String toolUrl = toolDetails.toolUrl;
    def status = ""
    def action = "Project creation"
    //def createdStatus = actionOne(toolDetails, projName);
    createProjectResourceQuota(toolDetails, projName, requestsCpu, requestsMemory, persistantStorage, backupStorage, logfileStorage, servicesNodeports );
    /*if ( createdStatus == 200 || createdStatus == 201)
     {
        status = "Success"
     }
     else
     {
        status = "Failure"
     }*/
    // updateActionsToRig(status, rigletName, rigUrl, toolName, action)
    //actionTwo(toolDetails, inputJson);
    
    // projectName and Id should be from the createProject action
   // saveProjectDetails(rigletName, toolUrl, projName, rigUrl, projId, toolName);
}
main();

