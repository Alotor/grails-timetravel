package org.codehaus.groovy.grails.orm.hibernate.support;

import grails.validation.ValidationException;
import groovy.lang.Closure;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import groovy.lang.MetaMethod;
import groovy.lang.MetaProperty;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.commons.GrailsClassUtils;
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty;
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsDomainBinder;
import org.codehaus.groovy.grails.orm.hibernate.cfg.Mapping;
import org.codehaus.groovy.grails.orm.hibernate.metaclass.AbstractDynamicPersistentMethod;
import org.codehaus.groovy.grails.orm.hibernate.metaclass.AbstractSavePersistentMethod;
import org.codehaus.groovy.grails.orm.hibernate.metaclass.ValidatePersistentMethod;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;
import org.grails.plugins.timetravel.TimeTravel;
import org.hibernate.EntityMode;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.event.AbstractEvent;
import org.hibernate.event.PreInsertEvent;
import org.hibernate.event.PreUpdateEvent;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.Errors;

// http://grepcode.com/file/repo1.maven.org/maven2/org.grails/grails-hibernate/2.1.0.RC1/org/codehaus/groovy/grails/orm/hibernate/EventTriggeringInterceptor.java#EventTriggeringInterceptor.findEventListener%28java.lang.Object%29
// http://grepcode.com/file/repo1.maven.org/maven2/org.grails/grails-hibernate/2.1.0.RC1/org/codehaus/groovy/grails/orm/hibernate/support/ClosureEventListener.java#ClosureEventListener
// https://github.com/grails/grails-core/tree/2.1.x/grails-hibernate/src/main/groovy/org/codehaus/groovy/grails/orm/hibernate
public class TimetravelClosureEventListener extends ClosureEventListener {

    private static final long serialVersionUID = 1;

    private static final Log log = LogFactory.getLog(TimetravelClosureEventListener.class);
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[] {};

    EventTriggerCaller beforeInsertCaller;
    EventTriggerCaller preUpdateEventListener;

    boolean shouldTimestamp = false;
    boolean failOnErrorEnabled = false;

    MetaProperty dateCreatedProperty;
    MetaProperty lastUpdatedProperty;
    MetaProperty errorsProperty;

    MetaClass domainMetaClass;
    MetaMethod validateMethod;

    Map validateParams;

    public TimetravelClosureEventListener(Class<?> domainClazz, boolean failOnError, List failOnErrorPackages) {
        super(domainClazz, failOnError, failOnErrorPackages);
        domainMetaClass = GroovySystem.getMetaClassRegistry().getMetaClass(domainClazz);
        dateCreatedProperty = domainMetaClass.getMetaProperty(GrailsDomainClassProperty.DATE_CREATED);
        lastUpdatedProperty = domainMetaClass.getMetaProperty(GrailsDomainClassProperty.LAST_UPDATED);
        if (dateCreatedProperty != null || lastUpdatedProperty != null) {
            Mapping m = GrailsDomainBinder.getMapping(domainClazz);
            shouldTimestamp = m == null || m.isAutoTimestamp();
            log.debug("" + domainClazz + ": " + shouldTimestamp);
        }

        beforeInsertCaller = buildCaller(domainClazz, ClosureEventTriggeringInterceptor.BEFORE_INSERT_EVENT);
        preUpdateEventListener = buildCaller(domainClazz, ClosureEventTriggeringInterceptor.BEFORE_UPDATE_EVENT);

        if (failOnErrorPackages.size() > 0) {
            failOnErrorEnabled = GrailsClassUtils.isClassBelowPackage(domainClazz, failOnErrorPackages);
        } else {
            failOnErrorEnabled = failOnError;
        }

        validateParams = new HashMap();
        validateParams.put(ValidatePersistentMethod.ARGUMENT_DEEP_VALIDATE, false);

        errorsProperty = domainMetaClass.getMetaProperty(AbstractDynamicPersistentMethod.ERRORS_PROPERTY);

        validateMethod = domainMetaClass.getMetaMethod(ValidatePersistentMethod.METHOD_SIGNATURE, new Object[] { Map.class });
    }

    @Override
    public boolean onPreUpdate(final PreUpdateEvent event) {
        log.debug( "%% PRE UPDATE" );
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
                    long time = System.currentTimeMillis();

                    if (TimeTravel.getUpdated(entity) != null) {
                        time = ((Date)TimeTravel.getUpdated(entity)).getTime();
                        log.debug("Updated time: " + TimeTravel.getUpdated(entity));
                    }

                    Object now = DefaultGroovyMethods.newInstance(lastUpdatedProperty.getType(), new Object[] { time });
                    event.getState()[ArrayUtils.indexOf(event.getPersister().getPropertyNames(), GrailsDomainClassProperty.LAST_UPDATED)] = now;
                    lastUpdatedProperty.setProperty(entity, now);
                }
                if (!AbstractSavePersistentMethod.isAutoValidationDisabled(entity)
                        && !DefaultTypeTransformation.castToBoolean(validateMethod.invoke(entity,
                                new Object[] { validateParams }))) {
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
        log.debug( "%% PRE INSERT " + event.getEntity() + " (" + shouldTimestamp + ")");

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
                    long updateTime = System.currentTimeMillis();
                    long newTime = System.currentTimeMillis();

                    log.debug(">> PRE-INSERT: " + entity);
                    if (TimeTravel.getUpdated(entity) != null) {
                        updateTime = ((Date)TimeTravel.getUpdated(entity)).getTime();
                    }
                    if (TimeTravel.getNew(entity) != null) {
                        newTime = ((Date)TimeTravel.getNew(entity)).getTime();
                    }

                    if (dateCreatedProperty != null) {
                        Object now = DefaultGroovyMethods.newInstance(dateCreatedProperty.getType(), new Object[] { newTime });
                        dateCreatedProperty.setProperty(entity, now);
                        synchronizeState = true;
                    }
                    if (lastUpdatedProperty != null) {
                        Object now = DefaultGroovyMethods.newInstance(lastUpdatedProperty.getType(), new Object[] { updateTime });
                        lastUpdatedProperty.setProperty(entity, now);
                        synchronizeState = true;
                    }
                }

                if (synchronizeState) {
                    synchronizePersisterState(entity, event.getPersister(), event.getState());
                }

                boolean evict = false;
                if (!AbstractSavePersistentMethod.isAutoValidationDisabled(entity)
                        && !DefaultTypeTransformation.castToBoolean(validateMethod.invoke(entity,
                                new Object[] { validateParams }))) {
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

    private EventTriggerCaller buildCaller(Class<?> domainClazz, String event) {
        Method method = ReflectionUtils.findMethod(domainClazz, event);
        if (method != null) {
            ReflectionUtils.makeAccessible(method);
            return new MethodCaller(method);
        }

        Field field = ReflectionUtils.findField(domainClazz, event);
        if (field != null) {
            ReflectionUtils.makeAccessible(field);
            return new FieldClosureCaller(field);
        }

        MetaMethod metaMethod = domainMetaClass.getMetaMethod(event, EMPTY_OBJECT_ARRAY);
        if (metaMethod != null) {
            return new MetaMethodCaller(metaMethod);
        }

        MetaProperty metaProperty = domainMetaClass.getMetaProperty(event);
        if (metaProperty != null) {
            return new MetaPropertyClosureCaller(metaProperty);
        }

        return null;
    }

    private void synchronizePersisterState(Object entity, EntityPersister persister, Object[] state) {
        String[] propertyNames = persister.getPropertyNames();
        for (int i = 0; i < propertyNames.length; i++) {
            String p = propertyNames[i];
            MetaProperty metaProperty = domainMetaClass.getMetaProperty(p);
            if (ClosureEventTriggeringInterceptor.IGNORED.contains(p) || metaProperty == null) {
                continue;
            }
            Object value = metaProperty.getProperty(entity);
            state[i] = value;
            persister.setPropertyValue(entity, i, value, EntityMode.POJO);
        }
    }

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

    private static abstract class EventTriggerCaller {

        public abstract boolean call(Object entity);

        boolean resolveReturnValue(Object retval) {
            if (retval instanceof Boolean) {
                return !((Boolean) retval).booleanValue();
            }
            return false;
        }
    }

    private static class MethodCaller extends EventTriggerCaller {
        Method method;

        MethodCaller(Method method) {
            this.method = method;
        }

        @Override
        public boolean call(Object entity) {
            Object retval = ReflectionUtils.invokeMethod(method, entity);
            return resolveReturnValue(retval);
        }
    }

    private static class MetaMethodCaller extends EventTriggerCaller {
        MetaMethod method;

        MetaMethodCaller(MetaMethod method) {
            this.method = method;
        }

        @Override
        public boolean call(Object entity) {
            Object retval = method.invoke(entity, EMPTY_OBJECT_ARRAY);
            return resolveReturnValue(retval);
        }
    }

    private static abstract class ClosureCaller extends EventTriggerCaller {
        boolean cloneFirst = false;

        Object callClosure(Object entity, Closure callable) {
            if (cloneFirst) {
                callable = (Closure)callable.clone();
            }
            callable.setResolveStrategy(Closure.DELEGATE_FIRST);
            callable.setDelegate(entity);
            return callable.call();
        }
    }

    private static class FieldClosureCaller extends ClosureCaller {
        Field field;

        FieldClosureCaller(Field field) {
            this.field = field;
            if (Modifier.isStatic(field.getModifiers())) {
                cloneFirst = true;
            }
        }

        @Override
        public boolean call(Object entity) {
            Object fieldval = ReflectionUtils.getField(field, entity);
            if (fieldval instanceof Closure) {
                return resolveReturnValue(callClosure(entity, (Closure) fieldval));
            }
            log.error("Field " + field + " is not Closure or method.");
            return false;
        }
    }

    private static class MetaPropertyClosureCaller extends ClosureCaller {
        MetaProperty metaProperty;

        MetaPropertyClosureCaller(MetaProperty metaProperty) {
            this.metaProperty = metaProperty;
            if (Modifier.isStatic(metaProperty.getModifiers())) {
                cloneFirst = true;
            }
        }

        @Override
        public boolean call(Object entity) {
            Object fieldval = metaProperty.getProperty(entity);
            if (fieldval instanceof Closure) {
                return resolveReturnValue(callClosure(entity, (Closure) fieldval));
            }
            log.error("Field " + metaProperty + " is not Closure.");
            return false;
        }
    }
}
