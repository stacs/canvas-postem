package postem.tool

import grails.converters.JSON
import grails.core.GrailsApplication
import grails.plugins.rest.client.RestResponse
import grails.transaction.Transactional
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.multipart.MultipartFile
import postem.tool.canvas.File
import postem.tool.canvas.UploadParams

@Transactional
class CanvasFileService {

    def restClient
    GrailsApplication grailsApplication

    private def notifyCanvas(String fileName, Boolean uploadAsLocked, String courseId, String userId) {
        def canvasBaseURL = grailsApplication.config.getProperty('canvas.canvasBaseUrl')
        def oauthToken = grailsApplication.config.getProperty('canvas.oauthToken')
        def folderId = getFolderId(courseId)
        def resp = restClient.post(canvasBaseURL + '/api/v1/courses/' + courseId + '/files'){
            auth('Bearer ' + oauthToken)
            json{
                name = fileName
                parent_folder_id = folderId
                locked = uploadAsLocked
            }
        }
        //UploadParams uploadParams = new UploadParams(uploadUrl: resp.json.upload_url, awsAccessKeyId: resp.json.upload_params.AWSAccessKeyId,
        //        fileName: resp.json.upload_params.Filename, key: resp.json.upload_params.key, acl: resp.json.upload_params.acl, policy: resp.json.upload_params.Policy,
        //        signature: resp.json.upload_params.Signature, successAccessRedirect: resp.json.upload_params.success_action_redirect, contentType: resp.json.upload_params.'content-type')
        return resp
    }

    private def awsUpload(uploadParams, MultipartFile multipartFile) {
        String uploadUrl = uploadParams.json.upload_url
        JSONObject params = uploadParams.json.upload_params
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<String, Object>()
        params.each {k,v ->
            form.add(k.toString(),v)
        }
        LinkedMultiValueMap<String,String> headerMap = new LinkedMultiValueMap<>();
        headerMap.add("Content-disposition", "form-data; name=file; filename=" + multipartFile.getOriginalFilename());
        headerMap.add("Content-type", multipartFile.getContentType());
        HttpEntity<byte[]> doc = new HttpEntity<byte[]>(multipartFile.getBytes(),headerMap);
        form.add("file", doc)
        def resp = restClient.post(uploadUrl){
            accept("application/json")
            contentType("multipart/form-data")
            body(form)
        }

        def js = resp.json.toString()
        //println("result ->"+js)
        //JSON.parse(resp)
        return resp
    }

    def upload(MultipartFile multipartFile, Boolean uploadAsLocked, Boolean releaseFeedback, String courseId, String fileTitle, String userId){
        def oauthToken = grailsApplication.config.getProperty('canvas.oauthToken')
        def uploadParams = notifyCanvas(fileTitle, uploadAsLocked, courseId, userId)
        if(uploadParams.json != null){
            def awsUploadResponse = awsUpload(uploadParams, multipartFile)
            def resp = restClient.post(awsUploadResponse.headers.getLocation().toString()){
                auth('Bearer ' + oauthToken)
            }
            String fileId = resp.json.id
            log.info("[" + grailsApplication.config.getProperty('grails.serverURL') +"] File Uploaded for Posted Feedback Tool by user " + userId + " in course " + courseId + ". File Details : Title = " + fileTitle + "," + " Id = " + fileId + "," + "location = " + awsUploadResponse.headers.getLocation().toString() )

            if(releaseFeedback){
                hideFile(fileId, true)
            }
            else{
                hideFile(fileId, false)
            }
        }

    }

    def listFiles(String courseId){
        def folder = 'Posted Feedback (Do NOT Publish)'
        def canvasBaseURL = grailsApplication.config.getProperty('canvas.canvasBaseUrl')
        def oauthToken = grailsApplication.config.getProperty('canvas.oauthToken')
        def resp = restClient.get(canvasBaseURL + '/api/v1/courses/' + courseId + '/folders/by_path/' + folder){
            auth('Bearer ' + oauthToken)
        }
        if(resp.status != 200){
            def createFolderResp = restClient.post(canvasBaseURL + '/api/v1/courses/' + courseId + '/folders'){
                auth('Bearer ' + oauthToken)
                json{
                    name = folder
                    locked = true
                    parent_folder_path = '/'
                }
            }
            return fetchFiles(createFolderResp.json.id)
        }
        JSONArray respArr = (JSONArray)resp.json
        def folderId = -1
        for(jsonObj in respArr){
            if(jsonObj.name == folder){
                folderId = jsonObj.id
                break
            }
        }
        return fetchFiles(folderId)
    }

    def getFolderId(String courseId){
        def folder = 'Posted Feedback (Do NOT Publish)'
        def canvasBaseURL = grailsApplication.config.getProperty('canvas.canvasBaseUrl')
        def oauthToken = grailsApplication.config.getProperty('canvas.oauthToken')
        def resp = restClient.get(canvasBaseURL + '/api/v1/courses/' + courseId + '/folders/by_path/' + folder){
            auth('Bearer ' + oauthToken)
        }
        if(resp.status != 200){
            def createFolderResp = restClient.post(canvasBaseURL + '/api/v1/courses/' + courseId + '/folders'){
                auth('Bearer ' + oauthToken)
                json{
                    name = folder
                    locked = true
                    parent_folder_path = '/'
                }
            }
            return createFolderResp.json.id;
        }
        JSONArray respArr = (JSONArray)resp.json
        def folderId = -1
        for(jsonObj in respArr){
            if(jsonObj.name == folder){
                folderId = jsonObj.id
                break
            }
        }
        return folderId;
    }

    private def fetchFiles(Long folderId){
        def canvasBaseURL = grailsApplication.config.getProperty('canvas.canvasBaseUrl')
        def oauthToken = grailsApplication.config.getProperty('canvas.oauthToken')
        List<File> fileList = new ArrayList<>()
        def resp = restClient.get(canvasBaseURL + '/api/v1/folders/' + folderId + '/files?include[]=user'){
            auth('Bearer ' + oauthToken)
        }
        populateFileList(resp, fileList)

        def nextPage = canvasNextPage(resp)
        while(nextPage != null){
            resp = restClient.get(nextPage){
                auth('Bearer ' + oauthToken)
            }
            populateFileList(resp, fileList)
            nextPage = canvasNextPage(resp)
        }

        return fileList
    }

    private static def populateFileList(RestResponse resp, List<File> fileList) {
        if (resp != null && resp.json != null) {

            if(resp.json instanceof JSONArray){
                JSONArray respArr = (JSONArray) resp.json
                for (jsonObj in respArr) {
                    if(jsonObj != null) {
                        String formattedFileName = jsonObj.display_name
                        if (formattedFileName.contains('.csv')) {
                            formattedFileName = formattedFileName.take(formattedFileName.lastIndexOf('.'))
                        }
                        String modifiedBy = jsonObj.user != null ? jsonObj.user.display_name : ""
                        File file = new File(fileId: jsonObj.id, displayName: formattedFileName, fileName: jsonObj.filename, modifiedBy: modifiedBy, updatedAt: Date.parse("yyyy-MM-dd'T'HH:mm:ss'Z'", (String) jsonObj.updated_at), locked: jsonObj.locked, hidden: jsonObj.hidden, url: jsonObj.url)
                        fileList.add(file)
                    }
                }
            }
        }

    }

    def deleteFile(String fileId){
        def canvasBaseURL = grailsApplication.config.getProperty('canvas.canvasBaseUrl')
        def oauthToken = grailsApplication.config.getProperty('canvas.oauthToken')
        def resp = restClient.get(canvasBaseURL + '/api/v1/files/' + fileId){
            auth('Bearer ' + oauthToken)
        }
        def display_name = "";
        if (resp != null && resp.json != null) {
            def jsonObj = resp.json
            display_name =  jsonObj.display_name
            restClient.delete(canvasBaseURL + '/api/v1/files/' + fileId){
                auth('Bearer ' + oauthToken)
            }

        }
        return display_name

    }

    def hideFile(String fileId, Boolean hide){
        def canvasBaseURL = grailsApplication.config.getProperty('canvas.canvasBaseUrl')
        def oauthToken = grailsApplication.config.getProperty('canvas.oauthToken')
        restClient.put(canvasBaseURL + '/api/v1/files/' + fileId){
            auth('Bearer ' + oauthToken)
            json{
                hidden = hide
            }
        }
    }

    def listUserLogins(String courseId){
        def users = []
        def canvasBaseURL = grailsApplication.config.getProperty('canvas.canvasBaseUrl')
        def oauthToken = grailsApplication.config.getProperty('canvas.oauthToken')
        def resp = restClient.get(canvasBaseURL + '/api/v1/courses/' + courseId + '/users?enrollment_type[]=student&enrollment_state[]=active&per_page=100'){
            auth('Bearer ' + oauthToken)
        }
        populateUserLogins(resp, users)

        def nextPage = canvasNextPage(resp)
        while(nextPage != null){
            resp = restClient.get(nextPage){
                auth('Bearer ' + oauthToken)
            }
            populateUserLogins(resp, users)
            nextPage = canvasNextPage(resp)
        }

        return users
    }

    private static def populateUserLogins(RestResponse resp, users){
        if (resp != null && resp.json != null) {
            if (resp.json instanceof JSONArray) {
                JSONArray respArr = (JSONArray)resp.json
                for(jsonObj in respArr){
                    users.add(jsonObj.login_id)
                }
            }
        }
    }

    def listUserDetails(String courseId){
        def users = []
        def canvasBaseURL = grailsApplication.config.getProperty('canvas.canvasBaseUrl')
        def oauthToken = grailsApplication.config.getProperty('canvas.oauthToken')
        def resp = restClient.get(canvasBaseURL + '/api/v1/courses/' + courseId + '/users?enrollment_type[]=student&enrollment_state[]=active&per_page=100'){
            auth('Bearer ' + oauthToken)
        }
        populateUserDetails(resp, users)

        def nextPage = canvasNextPage(resp)
        while(nextPage != null){
            resp = restClient.get(nextPage){
                auth('Bearer ' + oauthToken)
            }
            populateUserDetails(resp, users)
            nextPage = canvasNextPage(resp)
        }

        return users
    }

    private static def populateUserDetails(RestResponse resp, users){
        if (resp != null && resp.json != null) {
            if (resp.json instanceof JSONArray) {
                JSONArray respArr = (JSONArray)resp.json
                for(jsonObj in respArr){
                    String sortableName = jsonObj.sortable_name
                    String[] splitName = sortableName.split(',')
                    def user;
                    if(splitName.length == 2){
                        user = [jsonObj.login_id, splitName[0].trim(), splitName[1].trim()]
                    }
                    else{
                        user = [jsonObj.login_id, splitName[0], ""]
                    }
                    users.add(user)

                }
            }
        }

    }

    def canvasNextPage(RestResponse resp){
        String linkHeader = resp.headers.getFirst('Link')
        String nextLink = null
        if(linkHeader != null){
            String[] links = linkHeader.split(',')
            for(link in links){
                String[] linkParts = link.split(';')
                String relVal = linkParts[0]
                String relType = linkParts[1]
                if(relType.contains('next')){
                    nextLink = relVal.substring(1,relVal.length()-1)
                    break
                }
            }
        }
        return nextLink
    }

    def updateFileName(String fileId, String fileName){
        def canvasBaseURL = grailsApplication.config.getProperty('canvas.canvasBaseUrl')
        def oauthToken = grailsApplication.config.getProperty('canvas.oauthToken')
        restClient.put(canvasBaseURL + '/api/v1/files/' + fileId){
            auth('Bearer ' + oauthToken)
            json{
                name = fileName
                on_duplicate = 'rename'
            }
        }
    }
}
