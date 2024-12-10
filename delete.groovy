import groovy.json.*
import groovy.json.JsonSlurper

def main()
{
    //Deleting project
    def token = "mW3uRIvAHW-fDynLN9N9nx4YiwWKHMizel3G-AoxyhU"
    def projectUrl = "https://openshift:6443/apis/project.openshift.io/v1/projects"
    def projectName = "demo"
    def shell = [ 'bash','-c',"curl -s -w '%{http_code}' -o response.txt -k -X DELETE -H \"Authorization: Bearer $token\" -H 'Accept: application/json' -H 'Content-Type: application/json' $projectUrl/$projectName > response_code.txt "].execute()
    shell.waitFor()
    println shell.err.text
    println shell.text
    String responseCode = new File('response_code.txt').text
    println responseCode
}
main();




//For deleting project
curl -k \
    -X DELETE \
    -H "Authorization: Bearer 4K5Xg0d8bdT6gddTQH-ZckAdxR-oHXPnPNKWBdXY5UE" \
    -H 'Accept: application/json' \
    https://openshift:6443/apis/project.openshift.io/v1/projects/demo 

