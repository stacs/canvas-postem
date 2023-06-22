package postem.tool

import grails.converters.JSON
import grails.core.GrailsApplication
import grails.plugins.rest.client.RestResponse
import grails.transaction.Transactional
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import postem.tool.canvas.File
import postem.tool.canvas.User

@Transactional
class CanvasUserService {

    def restClient
    GrailsApplication grailsApplication

    def getUsersOfCourse(String course_id){

        def canvasBaseURL = grailsApplication.config.getProperty('canvas.canvasBaseUrl')
        def oauthToken = grailsApplication.config.getProperty('canvas.oauthToken')

        def resp = restClient.get(canvasBaseURL + '/api/v1/courses/'+ course_id + '/users?per_page=100'){
            auth('Bearer ' + oauthToken)
        }
        List<User> userList = new ArrayList<>()

        populateUsers(resp, userList)

        def nextPage = canvasNextPage(resp)
        while(nextPage != null){
            resp = restClient.get(nextPage){
                auth('Bearer ' + oauthToken)
            }
            populateUsers(resp, userList)
            nextPage = canvasNextPage(resp)
        }

        def userDisplayNameMap = new HashMap()
        for (user in userList){
            userDisplayNameMap.put(user.login_id, user.sortable_name )
        }
        return userDisplayNameMap
    }

    def populateUsers(RestResponse resp, List<User> userList) {
        if (resp != null && resp.json != null) {

            if(resp.json instanceof JSONArray){
                JSONArray respArr = (JSONArray) resp.json
                for (jsonObj in respArr) {
                    if(jsonObj != null) {
                        User user = new User(id: jsonObj.id, login_id: jsonObj.login_id, sortable_name: jsonObj.sortable_name)
                        userList.add(user)
                    }
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

}
