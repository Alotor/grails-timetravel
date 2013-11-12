import org.codehaus.groovy.grails.orm.hibernate.support.ClosureEventTriggeringInterceptor
import org.codehaus.groovy.grails.orm.hibernate.HibernateDatastore
import org.codehaus.groovy.grails.orm.hibernate.EventTriggeringInterceptor
import org.codehaus.groovy.grails.orm.hibernate.support.ClosureEventListener
import org.codehaus.groovy.grails.orm.hibernate.support.SoftKey
import org.codehaus.groovy.grails.orm.hibernate.support.TimetravelClosureEventListener
import org.grails.plugins.timetravel.TimeTravel

class GrailsTimetravelGrailsPlugin {
    // the plugin version
    def version = "0.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.1 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def title = "Grails Timetravel Plugin" // Headline display name of the plugin
    def author = "Your name"
    def authorEmail = ""
    def description = '''\
Brief summary/description of the plugin.
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/grails-timetravel"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
//    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
//    def organization = [ name: "My Company", url: "http://www.my-company.com/" ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    // Online location of the plugin's browseable source code.
//    def scm = [ url: "http://svn.codehaus.org/grails-plugins/" ]

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before
    }

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
        def grailsApplication = applicationContext.getBean("grailsApplication")

        grailsApplication.domainClasses.each {
            def domainClazz = it.clazz

            ClosureEventTriggeringInterceptor eventTriggeringInterceptor = applicationContext.getBean("eventTriggeringInterceptor")
            HibernateDatastore datastore = eventTriggeringInterceptor.datastores.values().iterator().next()
            EventTriggeringInterceptor interceptor = datastore.getEventTriggeringInterceptor()

            interceptor.eventListeners.clear()
            def key = new SoftKey(domainClazz)
            def eventListener = new TimetravelClosureEventListener(domainClazz, true, [])
            interceptor.eventListeners.put(key, eventListener)

            def saveMethod1 = it.metaClass.pickMethod("save", [] as Class[])
            it.metaClass.save = {
                TimeTravel.add(delegate)
                saveMethod1.invoke(delegate, [] as Object[])
            }

            def saveMethod2 = it.metaClass.pickMethod("save", [Map] as Class[])
            it.metaClass.save = { Map params ->
                TimeTravel.add(delegate)
                saveMethod2.invoke(delegate, [params] as Object[])
            }

            def saveMethod3 = it.metaClass.pickMethod("save", [Boolean] as Class[])
            it.metaClass.save = { Boolean params ->
                TimeTravel.add(delegate)
                saveMethod3.invoke(delegate, [params] as Object[])
            }

            //ClosureEventListener listener = interceptor.findEventListener(domainClazz.newInstance())

            /*
            domainClazz.metaClass.static.disableTimestamping = {
                // Mapping m = GrailsDomainBinder.getMapping(domainClazz)
                // m.autoTimestamp = false
                listener.shouldTimestamp = false
            }
            domainClazz.metaClass.static.enableTimestamping = {
                // Mapping m = GrailsDomainBinder.getMapping(domainClazz);
                // m.autoTimestamp = true
                listener.shouldTimestamp = true
            }
            domainClazz.metaClass.static.doWithoutTimestamping = { Closure closure ->
                // Mapping m = GrailsDomainBinder.getMapping(domainClazz);
                // m.autoTimestamp = false
                listener.shouldTimestamp = false
                closure.call()
                listener.shouldTimestamp = true
                // m.autoTimestamp = true
            }
            */
        }
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
