import groovy.json.*
import groovy.json.JsonSlurper

def main()
{
    //POST Method for creating pod
    def token = "dDvV-I0faZUgt8p0wk4PVnC9TVxA9NXCAxtDmYJL574"
    def projectUrl = "https://openshift:6443/api/v1/namespaces/demo/pods"
    def projectName = "demo"
    def imageName = "nginx"
    def imageNameApp = "nginx-app"
    def dockerImage = "bitnami/nginx"
    def port = 8080
    def json = '{ "apiVersion": "v1", "kind": "Pod", "metadata": { "name": "nginx", "labels": { "app": "nginx-app" }, "namespace": "demo" }, "spec": { "containers": [ { "name": "nginx", "image": "bitnami/nginx", "ports": [ { "containerPort": 8080 } ] } ] } }'
    def shell = [ 'bash','-c',"curl -s -w '%{http_code}' -o response.txt -k -X POST -H \"Authorization: Bearer $token\" -H 'Accept: application/json' -H 'Content-Type: application/json' $projectUrl -d '$json' > response_code.txt "].execute()
    shell.waitFor()
    println shell.err.text
    println shell.text
    String responseCode = new File('response_code.txt').text
    println responseCode
}
main();
