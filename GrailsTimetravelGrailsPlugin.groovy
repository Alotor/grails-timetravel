import grails.util.Environment

import org.codehaus.groovy.grails.orm.hibernate.support.SoftKey
import org.codehaus.groovy.grails.orm.hibernate.support.TimetravelClosureEventListener
import org.grails.plugins.timetravel.TimeTravel

class GrailsTimetravelGrailsPlugin {
    def version = "0.2"
    def grailsVersion = "2.0 > *"
    def pluginExcludes = [
            "grails-app/domain/**",
            "grails-app/controllers/**",
            "grails-app/services/**",
            "grails-app/utils/**",
            "grails-app/views/**",
            "grails-app/taglib/**",
            "grails-app/i18n/**",
            "test/**",
            "web-app/**"
    ]

    def title = "Grails Timetravel Plugin"
    def author = "Alonso Torres"
    def authorEmail = "alonso.javier.torres@gmail.com"
    def description = 'Provides utility methods to set custom dateCreated and lastUpdated timpestamped dates on your domain objects'
    def documentation = "https://github.com/Alotor/grails-timetravel/blob/master/README.md"

    def license = "APACHE"
    def organization = [ name: "Kaleidos", url: "http://kaleidos.net" ]
    def issueManagement = [ system: "GITHUB", url: "https://github.com/Alotor/grails-timetravel/issues" ]
    def scm = [ url: "https://github.com/Alotor/grails-timetravel.git" ]

    def doWithApplicationContext = { applicationContext ->
        // Only on test environment
        if (Environment.getCurrent() != Environment.TEST) {
            return
        }

        def eventTriggeringInterceptor = applicationContext.eventTriggeringInterceptor
        def datastore = eventTriggeringInterceptor.datastores.values().iterator().next()

        // For each domain instance add time-travel functionality
        application.domainClasses.each {
            def domainClazz = it.clazz

            // Intercepts save methods
            def saveEmptyMethod = it.metaClass.pickMethod("save", [] as Class[])
            it.metaClass.save = {
                TimeTravel.addUpdated(delegate)
                if (!delegate.id) {
                    TimeTravel.addNew(delegate)
                }
                saveEmptyMethod.invoke(delegate, [] as Object[])
            }

            def saveMapMethod = it.metaClass.pickMethod("save", [Map] as Class[])
            it.metaClass.save = { Map params ->
                TimeTravel.addUpdated(delegate)
                if (!delegate.id) {
                    TimeTravel.addNew(delegate)
                }
                saveMapMethod.invoke(delegate, [params] as Object[])
            }

            def saveBoolMethod = it.metaClass.pickMethod("save", [Boolean] as Class[])
            it.metaClass.save = { Boolean params ->
                TimeTravel.addUpdated(delegate)
                if (!delegate.id) {
                    TimeTravel.addNew(delegate)
                }
                saveBoolMethod.invoke(delegate, [params] as Object[])
            }
        }
    }
}
