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
        def proc1 = command1.execute()
        proc1.waitFor()
        println command1
        def command2 = "oc whoami -t"
        def proc2 = command2.execute()
        proc2.waitFor()
        println command2
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

//Delte Project details from Rig 
def deleteProjectDetails(rigletName, toolUrl, rigUrl, projName, toolName) { 
    String projUrl= "${toolUrl}" // create the url based on the tool
    def deleteProjectDetail = new URL(rigUrl+"/api/riglets/deleteToolProjectInfo").openConnection();
    def message = '{"rigletName":"'+rigletName+'","toolName":"'+toolName+'","url":"'+projUrl+',"projectId":"'+projectKey+'","projectName":"'+projName+'"}'
    deleteProjectDetail.setRequestMethod("DELETE")
    deleteProjectDetail.setDoOutput(true)
    deleteProjectDetail.setRequestProperty("Content-Type", "application/json")
    deleteProjectDetail.getOutputStream().write(message.getBytes("UTF-8"));
    def deleteProjectDetailRC = deleteProjectDetail.getResponseCode();
    if(deleteProjectDetailRC.equals(200) || deleteProjectDetailRC.equals(201)) {
        println(deleteProjectDetail.getInputStream().getText());
    }
    else {
        println "Error status Code of Delete Project Details-  ${deleteProjectDetailRC}"
      
    }
}

//Deleting Project
def deleteProject(toolDetails, projName, action) {     
    def jsonSlurper = new JsonSlurper()
    def projectUrl = toolDetails.toolUrl +"/apis/project.openshift.io/v1/projects"
    def token = toolDetails.token
    println token
    def rigletName = toolDetails.rigletName
    def rigUrl = toolDetails.rigUrl
    def toolName = toolDetails.toolName    
    def curl_cmd = "curl -s -w '%{http_code}' -o response.txt -k -X DELETE -H \"Authorization: Bearer $token\" -H 'Accept: application/json' -H 'Content-Type: application/json' $projectUrl/$projName > response_code.txt "
    def jobDetail = ['sh','-c',"$curl_cmd"].execute()
    jobDetail.waitFor()
    println jobDetail.err.text
    String response_code = new File('response_code.txt').text
    println response_code
    def actionStatus
    actionStatus = JsonOutput.toJson([isActionSucceeded: true,  projectName:"${projName}"]);
    /*
    if ( response_code == 200 || response_code == 201)
     {
      updateActionsToRig("Success", rigletName, rigUrl, toolName, action);
      actionStatus = JsonOutput.toJson([isActionSucceeded: true,  projectName:"${projName}"]);
     }
     else
     {
       updateActionsToRig("Failure", rigletName, rigUrl, toolName, action);
       actionStatus = JsonOutput.toJson([isActionSucceeded: false, projectName: null]);
     }
    */
        return actionStatus
}

def main() {
    def inputJson = parseInput(args[1]);
    
    String rigUrl = args[0];
    String toolName=inputJson.environment.tool.name; 
    String projName = inputJson.environment.project.name
    String rigletName=inputJson.riglet_info.name;
    println toolName
    println projName
    println rigletName
    def toolDetails = parseInput(getToolDetails(rigUrl, rigletName, toolName));
    println toolDetails
    String toolUrl = toolDetails.toolUrl;
    def action = "Deleting Project"
    def deletedStatus = parseInput(deleteProject(toolDetails, projName, action));
    println deletedStatus
    /*
    if(deletedStatus.isActionSucceeded) {
        deleteProjectDetails(rigletName, toolUrl, rigUrl, deletedStatus.projectName, toolName);
    } 
    else {
        deleteProjectDetails(rigletName, toolUrl, rigUrl, deletedStatus.projectName, toolName);
    }*/
}

main();