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
        def toolsJson = JsonOutput.toJson([user: "${user}", password: "${pass}", toolUrl: "${toolUrl}"])
        return toolsJson;
    } else {
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

def actionOne() {
    // perform action 1
    updateActionsToRig(status, rigletName, rigUrl, toolName, action);
}

def actionTwo() {
    // perform action 2
    updateActionsToRig(status, rigletName, rigUrl, toolName, action)
}

// main function to trigger the connector sequence
def main() {
    def inputJson = parseInput(args[1]);
    String rigUrl = args[0];
    // depends on the input json schema for which the connector is written
    String toolName=inputJson.scm.tool.name; 
    String rigletName=inputJson.riglet_info.name;
    // extract params based on the requirement and inputJson structure
    
    def toolDetails = parseInput(getToolDetails(rigUrl, rigletName, toolName));
    String toolUrl = toolDetails.toolUrl;
    actionOne(toolDetails, inputJson);
    actionTwo(toolDetails, inputJson);
    
    // projectName and Id should be from the createProject action
    saveProjectDetails(rigletName, toolUrl, projName, rigUrl, projId, toolName);
}

main();