import groovy.json.*
import groovy.json.JsonSlurper

def main()
{
    //POST Method for creating service
    def token = "dDvV-I0faZUgt8p0wk4PVnC9TVxA9NXCAxtDmYJL574"
    def projectUrl = "https://openshift:6443/api/v1/namespaces/demo/services"
    def projectName = "demo"
    def imageName = "nginx"
    def imageNameApp = "nginx-app"
    def port = 80
    def targetPort = 8080
    def json = '{ "apiVersion": "v1" , "kind": "Service", "metadata": { "name": "nginx", "namespace": "demo" } , "spec": { "selector": { "app": "nginx-app" }, "ports": [ { "protocol": "TCP", "port": 80, "targetPort": 8080 } ] } }'
    def shell = [ 'bash','-c',"curl -s -w '%{http_code}' -o response.txt -k -X POST -H \"Authorization: Bearer $token\" -H 'Accept: application/json' -H 'Content-Type: application/json' $projectUrl -d '$json' > response_code.txt "].execute()
    shell.waitFor()
    println shell.err.text
    println shell.text
    String responseCode = new File('response_code.txt').text
    println responseCode
}
main();
