import groovy.json.*
import groovy.json.JsonSlurper

def main()
    {
    //POST Method for creating project
    def openshifturl = new URL('https://console-openshift-console-2886795280-443-simba02.environments.katacoda.com')
    def openshiftConnection = openshifturl.openConnection()
    def message = '{ "kind": "Project","apiVersion": "project.openshift.io/v1", "metadata": { "name":"demo" } }'
    openshiftConnection.setRequestMethod("POST")
    openshiftConnection.setDoOutput(true)
    openshiftConnection.setRequestProperty("PRIVATE-TOKEN","IDhohUctxf_zXMDxbAhLf1W42YgA5l9ksh6uur7Ym-o")
    openshiftConnection.setRequestProperty("Content-Type", "application/json")
    openshiftConnection.getOutputStream().write(message.getBytes("UTF-8"));
    def responseCode = openshiftConnection.getResponseCode();
    println responseCode
    //if(responseCode.equals(200)) {
    //    println openshiftConnection.getInputStream().getText()
    //}
    }
main();