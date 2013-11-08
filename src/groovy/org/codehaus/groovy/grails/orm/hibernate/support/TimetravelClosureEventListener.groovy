package org.codehaus.groovy.grails.orm.hibernate.support

import grails.validation.ValidationException;
import groovy.lang.Closure;

import org.codehaus.groovy.grails.orm.hibernate.support.ClosureEventListener;
import org.hibernate.event.PreInsertEvent;
import org.hibernate.event.PreUpdateEvent;
import org.hibernate.event.AbstractEvent;
import java.util.List;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.springframework.validation.Errors;

import org.grails.plugins.timetravel.TimeTravel

// http://grepcode.com/file/repo1.maven.org/maven2/org.grails/grails-hibernate/2.1.0.RC1/org/codehaus/groovy/grails/orm/hibernate/EventTriggeringInterceptor.java#EventTriggeringInterceptor.findEventListener%28java.lang.Object%29
// http://grepcode.com/file/repo1.maven.org/maven2/org.grails/grails-hibernate/2.1.0.RC1/org/codehaus/groovy/grails/orm/hibernate/support/ClosureEventListener.java#ClosureEventListener
// https://github.com/grails/grails-core/tree/2.1.x/grails-hibernate/src/main/groovy/org/codehaus/groovy/grails/orm/hibernate
public class TimetravelClosureEventListener extends ClosureEventListener {
    public TimetravelClosureEventListener(Class<?> domainClazz, boolean failOnError, List failOnErrorPackages) {
        super(domainClazz, failOnError, failOnErrorPackages);
    }

    @Override
    public boolean onPreUpdate(final PreUpdateEvent event) {
        super.onPreUpdate(event)
    }

    @Override
    public boolean onPreInsert(final PreInsertEvent event) {
        def result = super.onPreInsert(event)
        println ">> result ${result} holder ${TimeTravel._holder}"
        if (TimeTravel._holder) {
            println "ENTER"
            event.entity.dateCreated = TimeTravel._holder.get()
            event.entity.lastUpdated = TimeTravel._holder.get()
        }
        return result
    }
/*
    @Override
    public boolean onPreUpdate(final PreUpdateEvent event) {
        return doWithManualSession(event, new Closure<Boolean>(this) {
            @Override
            public Boolean call() {
                Object entity = event.getEntity();
                boolean evict = false;
                if (preUpdateEventListener != null) {
                    evict = preUpdateEventListener.call(entity);
                    synchronizePersisterState(entity, event.getPersister(), event.getState());
                }
                if (lastUpdatedProperty != null && shouldTimestamp) {
                    Object now = DefaultGroovyMethods.newInstance(lastUpdatedProperty.getType(), [ System.currentTimeMillis ] as Object[]);
                    event.getState()[ArrayUtils.indexOf(event.getPersister().getPropertyNames(), GrailsDomainClassProperty.LAST_UPDATED)] = now;
                    lastUpdatedProperty.setProperty(entity, now);
                }
                if (!AbstractSavePersistentMethod.isAutoValidationDisabled(entity)
                        && !DefaultTypeTransformation.castToBoolean(validateMethod.invoke(entity, [ validateParams ] as Object[] ))) {
                    evict = true;
                    if (failOnErrorEnabled) {
                        Errors errors = (Errors) errorsProperty.getProperty(entity);
                        throw new ValidationException("Validation error whilst flushing entity [" + entity.getClass().getName()
                                + "]", errors);
                    }
                }
                return evict;
            }
        });
    }

    @Override
    public boolean onPreInsert(final PreInsertEvent event) {
        def beforeInsertCaller = super.beforeInsertCaller
        return doWithManualSession(event, new Closure<Boolean>(this) {
            @Override
            public Boolean call() {
                Object entity = event.getEntity();
                boolean synchronizeState = false;
                if (beforeInsertCaller != null) {
                    if (beforeInsertCaller.call(entity)) {
                        return true;
                    }
                    synchronizeState = true;
                }
                if (shouldTimestamp) {
                    long time = System.currentTimeMillis();
                    if (dateCreatedProperty != null) {
                        Object now = DefaultGroovyMethods.newInstance(dateCreatedProperty.getType(), [time] as Object[] );
                        dateCreatedProperty.setProperty(entity, now);
                        synchronizeState = true;
                    }
                    if (lastUpdatedProperty != null) {
                        Object now = DefaultGroovyMethods.newInstance(lastUpdatedProperty.getType(), [time] as Object[] );
                        lastUpdatedProperty.setProperty(entity, now);
                        synchronizeState = true;
                    }
                }

                if (synchronizeState) {
                    synchronizePersisterState(entity, event.getPersister(), event.getState());
                }

                boolean evict = false;
                if (!AbstractSavePersistentMethod.isAutoValidationDisabled(entity)
                        && !DefaultTypeTransformation.castToBoolean(validateMethod.invoke(entity, [validateParams] as Object[] ))) {
                    evict = true;
                    if (failOnErrorEnabled) {
                        Errors errors = (Errors) errorsProperty.getProperty(entity);
                        throw new ValidationException("Validation error whilst flushing entity [" + entity.getClass().getName()
                                + "]", errors);
                    }
                }
                return evict;
            }
        });
    }

    */
    private <T> T doWithManualSession(AbstractEvent event, Closure<T> callable) {
        Session session = event.getSession();
        FlushMode current = session.getFlushMode();
        try {
           session.setFlushMode(FlushMode.MANUAL);
           return callable.call();
        } finally {
            session.setFlushMode(current);
        }
    }
}
