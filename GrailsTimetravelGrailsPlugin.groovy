import org.codehaus.groovy.grails.orm.hibernate.support.ClosureEventTriggeringInterceptor
import org.codehaus.groovy.grails.orm.hibernate.HibernateDatastore
import org.codehaus.groovy.grails.orm.hibernate.EventTriggeringInterceptor
import org.codehaus.groovy.grails.orm.hibernate.support.ClosureEventListener
import org.codehaus.groovy.grails.orm.hibernate.support.SoftKey
import org.codehaus.groovy.grails.orm.hibernate.support.TimetravelClosureEventListener
import org.grails.plugins.timetravel.TimeTravel
import grails.util.Environment

class GrailsTimetravelGrailsPlugin {
    // the plugin version
    def version = "0.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.0 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp",
            "grails-app/domain/**",
            "grails-app/controllers/**",
            "grails-app/taglib/**",
            "grails-app/i18n/**",
            "web-app/**"
    ]

    def title = "Grails Timetravel Plugin"
    def author = "Alonso Torres"
    def authorEmail = "alonso.javier.torres@gmail.com"
    def description = '''\
This plugin provides utility methods to set custom dateCreated and lastUpdated timpestamped dates
on your domain objects.
'''

    // URL to the plugin's documentation
    def documentation = "https://github.com/Alotor/grails-timetravel/blob/master/README.md"

    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
    def organization = [ name: "Kaleidos", url: "http://kaleidos.net" ]

    // Location of the plugin's issue tracker.
    def issueManagement = [ system: "GITHUB", url: "https://github.com/Alotor/grails-timetravel/issues" ]

    // Online location of the plugin's browseable source code.
    def scm = [ url: "https://github.com/Alotor/grails-timetravel.git" ]

    def doWithApplicationContext = { applicationContext ->
        // Only on test environment
        if (Environment.getCurrent() == Environment.TEST) {
            def grailsApplication = applicationContext.getBean("grailsApplication")
            def eventTriggeringInterceptor = applicationContext.getBean("eventTriggeringInterceptor")
            def datastore = eventTriggeringInterceptor.datastores.values().iterator().next()

            // For each domain instance add time-travel functionality
            grailsApplication.domainClasses.each {
                def domainClazz = it.clazz

                def interceptor = datastore.getEventTriggeringInterceptor()
                interceptor.eventListeners.clear()
                def key = new SoftKey(domainClazz)
                def eventListener = new TimetravelClosureEventListener(domainClazz, true, [])
                interceptor.eventListeners.put(key, eventListener)

                // Intercepts save methods
                def saveEmptyMethod = it.metaClass.pickMethod("save", [] as Class[])
                it.metaClass.save = {
                    TimeTravel.add(delegate)
                    saveEmptyMethod.invoke(delegate, [] as Object[])
                }

                def saveMapMethod = it.metaClass.pickMethod("save", [Map] as Class[])
                it.metaClass.save = { Map params ->
                    TimeTravel.add(delegate)
                    saveMapMethod.invoke(delegate, [params] as Object[])
                }

                def saveBoolMethod = it.metaClass.pickMethod("save", [Boolean] as Class[])
                it.metaClass.save = { Boolean params ->
                    TimeTravel.add(delegate)
                    saveBoolMethod.invoke(delegate, [params] as Object[])
                }
            }
        }
    }
}
