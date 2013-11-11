package org.grails.plugins.timetravel

import org.grails.plugins.timetravel.TimeTravel

class TimeTravel {
    public static TimeHolder _holder = null

    def travel(Date dateToTravel, Closure closure) {
        System.out.println ("Traveling: " + dateToTravel);
        _holder = new TimeHolder(time: dateToTravel)
        closure.call()
        _holder = null
        System.out.println("I'm back");
    }
}

class TimeHolder {
    Date time

    def Date get() {
        return time
    }
}
