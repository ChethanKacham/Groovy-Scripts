def main()
    {
    //POST Method for creating project
    def openshifturl = new URL('https://openshift:6443/apis/project.openshift.io/v1/projects')
    def openshiftConnection = openshifturl.openConnection()
    def message = '{ "kind": "Project","apiVersion": "project.openshift.io/v1", "metadata": { "name":"demo" } }​​​​'
    openshiftConnection.setRequestMethod("POST")
    openshiftConnection.setDoOutput(true)
    openshiftConnection.setRequestProperty("PRIVATE-TOKEN","9r4UfK1LzL5J1CZfEAVdvQMaeS_4712lYmZtK1OXhNQ")
    openshiftConnection.setRequestProperty("Content-Type", "application/json")
    openshiftConnection.getOutputStream().write(message.getBytes("UTF-8"));
    def responseCode = openshiftConnection.getResponseCode();
    println responseCode
    //if(responseCode.equals(200)) {
    //    println openshiftConnection.getInputStream().getText()
    //}
    }
main();


------------------------------------------------------------------
------------------------------------------------------------------

//Arko's code for creating project

import groovy.json.JsonSlurper;
def jsonSlurper = new JsonSlurper()
def projJson = ""
projects = "https://2886795296-8443-kota01.environments.katacoda.com:443/apis/project.openshift.io/v1/projects" // api to fetch project details
getprojDetail = new URL(projects).openConnection();
getprojDetail.setRequestMethod("GET")
getprojDetail.setDoOutput(true)
getprojDetail.setRequestProperty("Content-Type", "application/json")
getprojDetail.setRequestProperty("PRIVATE-TOKEN","VaeoJXL7SCRGxYoCdQayHa8jFK5jRUQD20fyo7I6nqE")


 

getprojRC = getprojDetail.getResponseCode();
if (getprojRC == 200)


 

projJson = getprojDetail.getInputStream().getText()
println projJson
projJson = jsonSlurper.parseText(projJson)




----------------------------------------------------------------------------------
----------------------------------------------------------------------------------
//For connecting to Digital Rig 


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
//def user = "${toolJsonValue['username']}"
def user = ""
// def pass = "${toolJsonValue['password']}"
def pass = "DsIwEpgSeKQBIV95IzuPDzbAOIUvkrE1qAxGHzyqcAU"
//def toolUrl = "${toolJsonValue['url']}"
def toolsUrl = "https://console-openshift-console-2886795292-443-host18nc.environments.katacoda.com/apis/project.openshift.io/v1/projects"
//def toolsJson = JsonOutput.toJson([user: "${user}", password: "${pass}", toolUrl: "${toolUrl}"])
def toolsJson = JsonOutput.toJson([user: "", password: "DsIwEpgSeKQBIV95IzuPDzbAOIUvkrE1qAxGHzyqcAU", toolUrl: "${ENDPOINT}"])
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
println "Error status Code of Save Project Details- ${saveProjectDetailRC}"
//println(saveProjectDetail.getInputStream().getText());
 }
}
def action1() {
// perform action 1
 import groovy.json.JsonSlurper;
 updateActionsToRig(status, rigletName, rigUrl, toolName, action)
def jsonSlurper = new JsonSlurper()
//def name = 'test_user'
def projJson = ""
projects = "https://console-openshift-console-2886795292-443-host18nc.environments.katacoda.com/apis/project.openshift.io/v1/projects" // api to fetch project details
getprojDetail = new URL(projects).openConnection();
def message = '{
 "kind": "Project",
 "apiVersion": "project.openshift.io/v1",
 "metadata": {
 "name"= "test-user"
 }'
getprojDetail.setRequestMethod("POST")
getprojDetail.setDoOutput(true)
getprojDetail.setRequestProperty("Content-Type", "application/json")
getprojDetail.setRequestProperty("PRIVATE-TOKEN","DsIwEpgSeKQBIV95IzuPDzbAOIUvkrE1qAxGHzyqcAU")
getprojDetail.getOutputStream().write(message.getBytes("UTF-8"));
getprojRC = getprojDetail.getResponseCode();
//println getprojRC
if (getprojRC == 200)
projJson = getprojDetail.getInputStream().getText()
//println projJson
def main() {
def inputJson = parseInput(args[1]);
//boolean create_status = inputJson.tool_info.openshift.create;
 
String rigUrl = args[0];
// depends on the input json schema for which the connector is written
String toolName=inputJson.tool.name; 
String rigletName=inputJson.riglet_info.name;
// extract params based on the requirement and inputJson structure
 
def toolDetails = parseInput(getToolDetails(rigUrl, rigletName, toolName));
/* if(create_status=true)
 {
 actionOne(toolDetails, inputJson);
 }*/
String toolUrl = toolDetails.toolUrl;
 action1(toolDetails, inputJson);
//actionTwo(toolDetails, inputJson);
 
// projectName and Id should be from the createProject action
 saveProjectDetails(rigletName, toolUrl, projName, rigUrl, projId, toolName);
}
main();
