package postem.tool

import grails.core.GrailsApplication
import org.imsglobal.lti.launch.LtiVerificationResult
import org.imsglobal.lti.launch.LtiVerifier

import grails.plugin.cookiesession.SerializableSession
import javax.servlet.http.HttpSession

class LTIController {

    GrailsApplication grailsApplication

    def launch() {
        // Request params map
        Map<String, String> rqParams = new HashMap<String, String>()

        // Log incoming request
        log.debug("Request URL: " + request.getRequestURL().toString())
        log.debug("Query String: " + request.getQueryString())
        log.debug("HTTP Method: " + request.getMethod())
        for(header in request.getHeaderNames()){
            log.debug("${header}:${request.getHeader(header)}")
        }
        for(param in request.getParameterMap()){
            log.debug("${param.key}:${param.value}")
        }

        // Authenticate initial LTI Request and store request params
        if(request.getParameter("oauth_consumer_key") != null){
            def ltiSecret = grailsApplication.config.getProperty('canvas.ltiSecret')
            LtiVerifier ltiVerifier = new LtiOauthVerifierSSL()
            LtiVerificationResult ltiResult = ltiVerifier.verify(request, ltiSecret)
            if(ltiResult.success){
                session["user"] = params.custom_canvas_user_login_id
                session["courseId"] = params.custom_canvas_course_id
                session["userId"] = params.custom_canvas_user_id

                def ctx = grailsApplication.mainContext
                //println(Arrays.asList(ctx.getBeanDefinitionNames()));
                def sessionRepository = ctx.getBean("sessionRepository")
                HttpSession httpsession = request.getSession();
                httpsession.setAttribute("oauthSignature", params.oauth_signature)
                sessionRepository.saveSession((SerializableSession)httpsession, response)
                response.setHeader("Set-Cookie", response.getHeader("Set-Cookie") + "; SameSite=None");

                String roles = request.getParameter("roles")
                if(roles.contains('Instructor') || roles.contains('TeachingAssistant') || roles.contains('Administrator')){
                    redirect(controller: 'instructor', action: 'index', absolute: true, params: [user: params.custom_canvas_user_login_id, courseId: params.custom_canvas_course_id, userId: params.custom_canvas_user_id])
                }
                else if(roles.contains('Learner')){
                    redirect(controller: 'student', action: 'index', absolute: true, params: [user: params.custom_canvas_user_login_id, courseId: params.custom_canvas_course_id, userId: params.custom_canvas_user_id])
                }
            }
            else{
                log.error("LTI Result not SUCCESS: " + ltiResult.getMessage())
                respond ltiResult
            }
        }
    }
}
