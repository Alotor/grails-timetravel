package org.grails.plugins.timetravel

import grails.util.Environment

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

class TimeTravel {
    private static final Log log = LogFactory.getLog(this)

    public static Map properties = Collections.synchronizedMap([:])
    public static TimeHolder _holder

    def travel(Date dateToTravel, Closure closure) {
        if (Environment.getCurrent() != Environment.TEST) {
            throw new Exception("You can only use time-travel on a testing environment")
        }

        log.debug("Traveling: $dateToTravel")
        _holder = new TimeHolder(time: dateToTravel)
        closure.call()
        _holder = null
        log.debug("End time-travel")
    }

    static get(object) {
        return properties[System.identityHashCode(object)]
    }

    static add(object) {
        properties[System.identityHashCode(object)] = _holder.time
    }
}

class TimeHolder {
    Date time

    Date get() {
        return time
    }
}
