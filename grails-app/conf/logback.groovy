import grails.util.BuildSettings
import grails.util.Environment
import org.springframework.boot.logging.logback.ColorConverter
import org.springframework.boot.logging.logback.WhitespaceThrowableProxyConverter

import java.nio.charset.Charset

conversionRule 'clr', ColorConverter
conversionRule 'wex', WhitespaceThrowableProxyConverter

// See http://logback.qos.ch/manual/groovy.html for details on configuration
appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        charset = Charset.forName('UTF-8')

        pattern =
                '%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} ' + // Date
                        '%clr(%5p) ' + // Log level
                        '%clr(---){faint} %clr([%15.15t]){faint} ' + // Thread
                        '%clr(%-40.40logger{39}){cyan} %clr(:){faint} ' + // Logger
                        '%m%n%wex' // Message
    }
}

appender('gelf', biz.paluch.logging.gelf.logback.GelfLogbackAppender) {
    host = 'udp:stacs-log-s.its.virginia.edu'
    port = 12201
    version = 1.1
    facility = 'lts-postedfeedback'
    extractStackTrace = true
    filterStackTrace = true
    includeLocation =  true
    mdcProfiling = true
    timestampPattern = 'yyyy-MM-dd HH:mm:ss,SSS'
    maximumMessageSize = 8192
}

logger 'org.hibernate.type.descriptor.sql.BasicBinder', ERROR, ['STDOUT','gelf']
logger 'org.hibernate.SQL', ERROR, ['STDOUT', 'gelf']
logger 'grails.plugin.cookiesession.CookieSessionRepository', ERROR, ['STDOUT', 'gelf']
root(INFO, ['STDOUT', 'gelf'])
