package postem.tool

import grails.core.GrailsApplication

class AuthInterceptor {

    GrailsApplication grailsApplication

    AuthInterceptor() {
        matchAll()
                .excludes(controller: 'LTI')
                .excludes(uri: "/")
    }

    boolean before() {

        def ctx = grailsApplication.mainContext
        def sessionRepository = ctx.getBean("sessionRepository")
        def serialized = sessionRepository.restoreSession(request)

        if(serialized != null){
            sessionRepository.saveSession(serialized, response)
            response.setHeader("Set-Cookie", response.getHeader("Set-Cookie") + "; SameSite=None");
        }
        else{
            render(status: 401, text: 'You do not have permission to view this page or your session has timed out. Please reload the page if your session has timed out.')
            return false
        }

        true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
