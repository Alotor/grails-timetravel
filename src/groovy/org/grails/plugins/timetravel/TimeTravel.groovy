package org.grails.plugins.timetravel

import org.grails.plugins.timetravel.TimeTravel

class TimeTravel {
    public static Map properties = Collections.synchronizedMap([:])
    public static TimeHolder _holder = null

    def travel(Date dateToTravel, Closure closure) {
        System.out.println ("Traveling: " + dateToTravel);
        _holder = new TimeHolder(time: dateToTravel)
        closure.call()
        _holder = null
        System.out.println("I'm back");
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

    def Date get() {
        return time
    }
}
