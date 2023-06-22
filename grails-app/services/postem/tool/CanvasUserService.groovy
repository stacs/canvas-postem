package postem.tool

import grails.converters.JSON
import grails.core.GrailsApplication
import grails.plugins.rest.client.RestResponse
import grails.transaction.Transactional
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject

@Transactional
class CanvasUserService {

    def restClient
    GrailsApplication grailsApplication

    def getUserDisplayName(String login, String course_id){

        def canvasBaseURL = grailsApplication.config.getProperty('canvas.canvasBaseUrl')
        def oauthToken = grailsApplication.config.getProperty('canvas.oauthToken')

        def display_name = ""

        def resp = restClient.get(canvasBaseURL + '/api/v1/courses/'+ course_id + '/search_users'){
            auth('Bearer ' + oauthToken)
            json{
                search_term = login
            }
        }
        def userId = ""
        if (resp != null && resp.json != null) {

            JSONArray respArr = (JSONArray)resp.json
            for(jsonObj in respArr){
                if(jsonObj.login_id == login){
                    userId = jsonObj.id
                    break
                }
            }

        }

        if(userId != ""){

            def resp2 = restClient.get(canvasBaseURL + '/api/v1/users/' + userId + '/profile'){
                auth('Bearer ' + oauthToken)
            }
            JSONObject user = (JSONObject)resp2.json
            display_name =  user["sortable_name"]

        }

        return display_name

    }

}
