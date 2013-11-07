package org.grails.plugins.timetravel.listener;

import org.codehaus.groovy.grails.orm.hibernate.support.ClosureEventListener;
import org.hibernate.event.PreInsertEvent;
import org.hibernate.event.PreUpdateEvent;
import java.util.List;

// http://grepcode.com/file/repo1.maven.org/maven2/org.grails/grails-hibernate/2.1.0.RC1/org/codehaus/groovy/grails/orm/hibernate/EventTriggeringInterceptor.java#EventTriggeringInterceptor.findEventListener%28java.lang.Object%29
// http://grepcode.com/file/repo1.maven.org/maven2/org.grails/grails-hibernate/2.1.0.RC1/org/codehaus/groovy/grails/orm/hibernate/support/ClosureEventListener.java#ClosureEventListener
// https://github.com/grails/grails-core/tree/2.1.x/grails-hibernate/src/main/groovy/org/codehaus/groovy/grails/orm/hibernate
public class TimetravelClosureEventListener extends ClosureEventListener {
    public TimetravelClosureEventListener(Class<?> domainClazz, boolean failOnError, List failOnErrorPackages) {
        super(domainClazz, failOnError, failOnErrorPackages);
    }

    @Override
    public boolean onPreInsert(final PreInsertEvent event) {
        System.err.println("SUCCESS INSERT!!!");
        return super.onPreInsert(event);
    }

    @Override
    public boolean onPreUpdate(final PreUpdateEvent event) {
        System.err.println("SUCCESS UPDATE!!!");
        return super.onPreUpdate(event);
    }
}
