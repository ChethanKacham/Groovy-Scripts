import groovy.json.*
import groovy.json.JsonSlurper

def main()
{
    //POST Method for creating project
    def token = "dDvV-I0faZUgt8p0wk4PVnC9TVxA9NXCAxtDmYJL574"
    def projectUrl = "https://openshift:6443/apis/project.openshift.io/v1/projects"
    def projectName = "demo"
    def json = '{ "kind": "Project", "apiVersion": "project.openshift.io/v1", "metadata": { "name":"demo" } }' 
    def shell = [ 'bash','-c',"curl -s -w '%{http_code}' -o response.txt -k -X POST -H \"Authorization: Bearer $token\" -H 'Accept: application/json' -H 'Content-Type: application/json' $projectUrl -d '$json' > response_code.txt "].execute()
    shell.waitFor()
    println shell.err.text
    println shell.text
    String responseCode = new File('response_code.txt').text
    println responseCode
}
main();

