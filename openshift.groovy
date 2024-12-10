//Using functions
import groovy.json.*
import groovy.json.JsonSlurper


def createProject(){

  //Function for Creating Project
    def token = "gmbu0SirSt42peac952OBTItuDY4F4gNpUsDDq9Wcpg"
    def projectUrl = "https://openshift:6443/apis/project.openshift.io/v1/projects"
    def jsonProject = '{ "kind": "Project", "apiVersion": "project.openshift.io/v1", "metadata": { "name":"demo" } }' 
    def shellProject = [ 'bash','-c',"curl -s -w '%{http_code}' -o response.txt -k -X POST -H \"Authorization: Bearer $token\" -H 'Accept: application/json' -H 'Content-Type: application/json' $projectUrl -d '$jsonProject' > response_codeproject.txt "].execute()
    shellProject.waitFor()
    println shellProject.err.text
    println shellProject.text
    String responseCodeProject = new File('response_codeproject.txt').text
    println responseCodeProject

}

def createPod(){

  //Function for Creating Pod(Deploying Image)
    def token = "gmbu0SirSt42peac952OBTItuDY4F4gNpUsDDq9Wcpg"
    def podUrl = "https://openshift:6443/api/v1/namespaces/demo/pods"
    def jsonPod = '{ "apiVersion": "v1", "kind": "Pod", "metadata": { "name": "nginx", "labels": { "app": "nginx-app" }, "namespace": "demo" }, "spec": { "containers": [ { "name": "nginx", "image": "bitnami/nginx", "ports": [ { "containerPort": 8080 } ] } ] } }'
    def shellPod = [ 'bash','-c',"curl -s -w '%{http_code}' -o response.txt -k -X POST -H \"Authorization: Bearer $token\" -H 'Accept: application/json' -H 'Content-Type: application/json' $podUrl -d '$jsonPod' > response_codepod.txt "].execute()
    shellPod.waitFor()
    println shellPod.err.text
    println shellPod.text
    String responseCodePod = new File('response_codepod.txt').text
    println responseCodePod

}

def createService(){

  //Function for Creating Project
    def token = "gmbu0SirSt42peac952OBTItuDY4F4gNpUsDDq9Wcpg"
    def serviceUrl = "https://openshift:6443/api/v1/namespaces/demo/services"
    def jsonService = '{ "apiVersion": "v1" , "kind": "Service", "metadata": { "name": "nginx", "namespace": "demo" } , "spec": { "selector": { "app": "nginx-app" }, "ports": [ { "protocol": "TCP", "port": 80, "targetPort": 8080 } ] } }'
    def shellService = [ 'bash','-c',"curl -s -w '%{http_code}' -o response.txt -k -X POST -H \"Authorization: Bearer $token\" -H 'Accept: application/json' -H 'Content-Type: application/json' $serviceUrl -d '$jsonService' > response_codeservice.txt "].execute()
    shellService.waitFor()
    println shellService.err.text
    println shellService.text
    String responseCodeService = new File('response_codeservice.txt').text
    println responseCodeService

}

def createRoute(){

  //Function for Creating Project
    def token = "gmbu0SirSt42peac952OBTItuDY4F4gNpUsDDq9Wcpg"
    def routeUrl = "https://openshift:6443/apis/route.openshift.io/v1/namespaces/demo/routes"
    def jsonRoute = '{ "apiVersion": "route.openshift.io/v1", "kind": "Route", "metadata": { "name": "nginx", "namespace": "demo" }, "spec": { "path": "/", "to": { "kind": "Service", "name": "nginx" }, "port": { "targetPort": 8080 } } }'
    def shellRoute = [ 'bash','-c',"curl -s -w '%{http_code}' -o response.txt -k -X POST -H \"Authorization: Bearer $token\" -H 'Accept: application/json' -H 'Content-Type: application/json' $routeUrl -d '$jsonRoute' > response_coderouter.txt "].execute()
    shellRoute.waitFor()
    println shellRoute.err.text
    println shellRoute.text
    String responseCodeRouter = new File('response_coderouter.txt').text
    println responseCodeRouter

}

def main(){

    def projectName = "demo"
    def imageName = "nginx"
    def imageNameApp = "nginx-app"
    def dockerImage = "bitnami/nginx"
    def port = 80
    def targetPort = 8080
    def token = "BBaukaEi-ZPdsVSlOQDOcDDdW_UQVIitiea-K39aYGk"

    //Creating Project
    createProject()

    //Creating Pod(Deploying Image)
    createPod()

    //Creating Service
    createService()

    //Creating Route
    createRoute()

}

main();




/*
//Without using functions
import groovy.json.*
import groovy.json.JsonSlurper


def main(){

    def projectName = "demo"
    def imageName = "nginx"
    def imageNameApp = "nginx-app"
    def dockerImage = "bitnami/nginx"
    def port = 80
    def targetPort = 8080
    def token = "BBaukaEi-ZPdsVSlOQDOcDDdW_UQVIitiea-K39aYGk"

    //Creating Project
    def projectUrl = "https://openshift:6443/apis/project.openshift.io/v1/projects"
    def jsonProject = '{ "kind": "Project", "apiVersion": "project.openshift.io/v1", "metadata": { "name":"demo" } }' 
    def shellProject = [ 'bash','-c',"curl -s -w '%{http_code}' -o response.txt -k -X POST -H \"Authorization: Bearer $token\" -H 'Accept: application/json' -H 'Content-Type: application/json' $projectUrl -d '$jsonProject' > response_codeproject.txt "].execute()
    shellProject.waitFor()
    println shellProject.err.text
    println shellProject.text
    String responseCodeProject = new File('response_codeproject.txt').text
    println responseCodeProject

    //Creating Pod(Deploying Image)
    def podUrl = "https://openshift:6443/api/v1/namespaces/demo/pods"
    def jsonPod = '{ "apiVersion": "v1", "kind": "Pod", "metadata": { "name": "nginx", "labels": { "app": "nginx-app" }, "namespace": "demo" }, "spec": { "containers": [ { "name": "nginx", "image": "bitnami/nginx", "ports": [ { "containerPort": 8080 } ] } ] } }'
    def shellPod = [ 'bash','-c',"curl -s -w '%{http_code}' -o response.txt -k -X POST -H \"Authorization: Bearer $token\" -H 'Accept: application/json' -H 'Content-Type: application/json' $podUrl -d '$jsonPod' > response_codepod.txt "].execute()
    shellPod.waitFor()
    println shellPod.err.text
    println shellPod.text
    String responseCodePod = new File('response_codepod.txt').text
    println responseCodePod

    //Creating Service
    def serviceUrl = "https://openshift:6443/api/v1/namespaces/demo/services"
    def jsonService = '{ "apiVersion": "v1" , "kind": "Service", "metadata": { "name": "nginx", "namespace": "demo" } , "spec": { "selector": { "app": "nginx-app" }, "ports": [ { "protocol": "TCP", "port": 80, "targetPort": 8080 } ] } }'
    def shellService = [ 'bash','-c',"curl -s -w '%{http_code}' -o response.txt -k -X POST -H \"Authorization: Bearer $token\" -H 'Accept: application/json' -H 'Content-Type: application/json' $serviceUrl -d '$jsonService' > response_codeservice.txt "].execute()
    shellService.waitFor()
    println shellService.err.text
    println shellService.text
    String responseCodeService = new File('response_codeservice.txt').text
    println responseCodeService

    //Creating Route
    def routeUrl = "https://openshift:6443/apis/route.openshift.io/v1/namespaces/demo/routes"
    def jsonRoute = '{ "apiVersion": "route.openshift.io/v1", "kind": "Route", "metadata": { "name": "nginx", "namespace": "demo" }, "spec": { "path": "/", "to": { "kind": "Service", "name": "nginx" }, "port": { "targetPort": 8080 } } }'
    def shellRoute = [ 'bash','-c',"curl -s -w '%{http_code}' -o response.txt -k -X POST -H \"Authorization: Bearer $token\" -H 'Accept: application/json' -H 'Content-Type: application/json' $routeUrl -d '$jsonRoute' > response_coderouter.txt "].execute()
    shellRoute.waitFor()
    println shellRoute.err.text
    println shellRoute.text
    String responseCodeRouter = new File('response_coderouter.txt').text
    println responseCodeRouter


    //String responseCode = new File('response_code.txt').text
    //println responseCode

}

main();
*/

