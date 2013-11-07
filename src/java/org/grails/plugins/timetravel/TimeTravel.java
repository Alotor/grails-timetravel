package org.grails.plugins.timetravel;

import groovy.time.TimeCategory;
import java.util.Date;

public class TimeTravel extends TimeCategory{
    public static void travel(Object test, Date dateToTravel) {
        System.out.println ("Traveling: " + dateToTravel);
    }
}


