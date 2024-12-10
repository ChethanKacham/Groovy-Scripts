/*def get = new URL('https://postman-echo.com/get?foo1=bar1&foo2=bar2').openConnection()
//getConnection.requestMethod = 'GET'
def responseCode = get.getResponseCode()
println responseCode
if(responseCode.equals(200)){
    println ​​​​​​​get.getInputStream().getText()
}*/

//GET Method
def postmanGet = new URL('https://postman-echo.com/get?foo1=bar1&foo2=bar2')
def getConnection = postmanGet.openConnection();
getConnection.requestMethod = 'GET'
def responseCode = getConnection.getResponseCode();
println responseCode
if(responseCode.equals(200)) {
    println getConnection.getInputStream().getText()
}


//POST Method
def postmanPost = new URL('https://postman-echo.com/post')
def postConnection = postmanPost.openConnection()
def message = '{​​​​​​​"kind": "Project", "apiVersion": "project.openshift.io/v1", "metadata": {​​​​​​​ "name"= "test-user" }​​​​'
postConnection.setRequestMethod("POST")
postConnection.setDoOutput(true)
postConnection.setRequestProperty("Content-Type", "application/json")
postConnection.setRequestProperty("PRIVATE-TOKEN","DsIwEpgSeKQBIV95IzuPDzbAOIUvkrE1qAxGHzyqcAU")
postConnection.getOutputStream().write(message.getBytes("UTF-8"));
def responseCode1 = postConnection.getResponseCode();
println responseCode1
if(responseCode1.equals(200)) {
    println postConnection.getInputStream().getText()
}