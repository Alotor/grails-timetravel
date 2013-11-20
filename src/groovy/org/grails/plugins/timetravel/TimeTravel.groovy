package org.grails.plugins.timetravel

import grails.util.Environment

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import org.springframework.web.context.request.RequestContextHolder

import org.codehaus.groovy.grails.orm.hibernate.support.SoftKey
import org.codehaus.groovy.grails.orm.hibernate.support.TimetravelClosureEventListener

class TimeTravel {
    private static final Log log = LogFactory.getLog(this)

    public static Map newProperties = Collections.synchronizedMap([:])
    public static Map updatedProperties = Collections.synchronizedMap([:])

    public static TimeHolder _holder

    def travel(Date dateToTravel, Closure closure) {
        if (Environment.getCurrent() != Environment.TEST) {
            throw new Exception("You can only use time-travel on a testing environment")
        }

        log.debug("Traveling: $dateToTravel")
        _holder = new TimeHolder(time: dateToTravel)
        try {
            closure.call()
        } finally {
            _holder = null
            log.debug("End time-travel")
        }
    }

    static getNew(object) {
        return newProperties[System.identityHashCode(object)]
    }

    static getUpdated(object) {
        return updatedProperties[System.identityHashCode(object)]
    }

    static addUpdated(object) {
        log.debug(">> ADD_UPDATED (${_holder?.time}) $object -> ${System.identityHashCode(object)}")
        if (_holder?.time) {
            _prepareEntity(object)
        }
        updatedProperties[System.identityHashCode(object)] = _holder?.time
    }

    static addNew(object) {
        log.debug(">> ADD_NEW (${_holder?.time}) $object -> ${System.identityHashCode(object)}")
        if (_holder?.time) {
            _prepareEntity(object)
        }
        newProperties[System.identityHashCode(object)] = _holder?.time
    }

    static _prepareEntity(object) {
        log.debug("PREPARING $object")
        if (object.hasProperty("domainClass")) {
            def applicationContext = object.domainClass.grailsApplication.mainContext
            def eventTriggeringInterceptor = applicationContext.getBean("eventTriggeringInterceptor")
            def datastore = eventTriggeringInterceptor.datastores.values().iterator().next()
            def interceptor = datastore.getEventTriggeringInterceptor()
            interceptor.eventListeners.clear()

            def key = new SoftKey(object.class)
            def eventListener = new TimetravelClosureEventListener(object.class, true, [])

            interceptor.eventListeners.put(key, eventListener)
        }
    }
}

class TimeHolder {
    Date time

    Date get() {
        return time
    }
}
