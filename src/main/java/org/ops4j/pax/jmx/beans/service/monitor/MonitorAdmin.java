/*
 * Copyright (c) 2012 Dmytro Pishchukhin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ops4j.pax.jmx.beans.service.monitor;

import org.ops4j.pax.jmx.beans.LogVisitor;
import org.ops4j.pax.jmx.beans.OsgiVisitor;
import org.ops4j.pax.jmx.beans.ServiceAbstractMBean;
import org.ops4j.pax.jmx.service.monitor.MonitorAdminMBean;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.monitor.MonitoringJob;
import org.osgi.service.monitor.StatusVariable;

import javax.management.*;
import javax.management.openmbean.*;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dmytro.pishchukhin
 */
public class MonitorAdmin extends ServiceAbstractMBean<org.osgi.service.monitor.MonitorAdmin>
        implements MonitorAdminMBean, NotificationBroadcaster {
    private NotificationBroadcasterSupport nbs;
    private MBeanNotificationInfo[] notificationInfos;
    private ServiceRegistration handlerRegistration;

    public MonitorAdmin() throws NotCompliantMBeanException {
        super(MonitorAdminMBean.class);
        nbs = new NotificationBroadcasterSupport();
    }

    @Override
    public void init() {
        super.init();

        try {
            Class<?> handlerClass = Class.forName("org.ops4j.pax.jmx.beans.service.monitor.MonitorAdminEventHandler");
            Constructor<?> constructor = handlerClass.getConstructor(OsgiVisitor.class, LogVisitor.class,
                    NotificationBroadcasterSupport.class, NotificationBroadcaster.class);
            Object handler = constructor.newInstance(visitor, logVisitor, nbs, this);
            Dictionary props = (Dictionary) handlerClass.getMethod("getHandlerProperties").invoke(handler);
            handlerRegistration = visitor.registerService("org.osgi.service.event.EventHandler", handler, props);
        } catch (Exception e) {
            logVisitor.warning("Unable to init EventHandler. MonitorAdmin events are ignored", e);
        }
    }

    @Override
    public void uninit() {
        if (handlerRegistration != null) {
            handlerRegistration.unregister();
            handlerRegistration = null;
        }
        super.uninit();
    }

    public String getDescription(String path) throws IllegalArgumentException, IOException {
        try {
            return service.getDescription(path);
        } catch (IllegalArgumentException e) {
            logVisitor.warning("getDescription error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("getDescription error", e);
            throw new IOException(e.getMessage());
        }
    }

    public CompositeData getStatusVariable(String path) throws IllegalArgumentException, IOException {
        try {
            StatusVariable statusVariable = service.getStatusVariable(path);
            return getCompositeData(statusVariable);
        } catch (IllegalArgumentException e) {
            logVisitor.warning("getStatusVariable error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("getStatusVariable error", e);
            throw new IOException(e.getMessage());
        }
    }

    public String[] getMonitorableNames() throws IOException {
        try {
            return service.getMonitorableNames();
        } catch (Exception e) {
            logVisitor.warning("getMonitorableNames error", e);
            throw new IOException(e.getMessage());
        }
    }

    public TabularData getStatusVariables(String monitorableId) throws IllegalArgumentException, IOException {
        try {
            StatusVariable[] statusVariables = service.getStatusVariables(monitorableId);
            TabularDataSupport dataSupport = new TabularDataSupport(STATUS_VARIABLES_TYPE);
            for (StatusVariable statusVariable : statusVariables) {
                dataSupport.put(getCompositeData(statusVariable));
            }
            return dataSupport;
        } catch (IllegalArgumentException e) {
            logVisitor.warning("getStatusVariables error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("getStatusVariables error", e);
            throw new IOException(e.getMessage());
        }
    }

    public String[] getStatusVariableNames(String monitorableId) throws IllegalArgumentException, IOException {
        try {
            return service.getStatusVariableNames(monitorableId);
        } catch (IllegalArgumentException e) {
            logVisitor.warning("getStatusVariableNames error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("getStatusVariableNames error", e);
            throw new IOException(e.getMessage());
        }
    }

    public void switchEvents(String path, boolean on) throws IllegalArgumentException, IOException {
        try {
            service.switchEvents(path, on);
        } catch (IllegalArgumentException e) {
            logVisitor.warning("switchEvents error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("switchEvents error", e);
            throw new IOException(e.getMessage());
        }
    }

    public boolean resetStatusVariable(String path) throws IllegalArgumentException, IOException {
        try {
            return service.resetStatusVariable(path);
        } catch (IllegalArgumentException e) {
            logVisitor.warning("resetStatusVariable error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("resetStatusVariable error", e);
            throw new IOException(e.getMessage());
        }
    }

    public TabularData getRunningJobs() throws IOException {
        try {
            MonitoringJob[] monitoringJobs = service.getRunningJobs();
            TabularDataSupport dataSupport = new TabularDataSupport(MONITORING_JOBS_TYPE);
            for (MonitoringJob monitoringJob : monitoringJobs) {
                dataSupport.put(getCompositeData(monitoringJob));
            }
            return dataSupport;
        } catch (Exception e) {
            logVisitor.warning("getRunningJobs error", e);
            throw new IOException(e.getMessage());
        }
    }

    private String getValueAsString(StatusVariable statusVariable) {
        switch (statusVariable.getType()) {
            case StatusVariable.TYPE_BOOLEAN:
                return Boolean.toString(statusVariable.getBoolean());
            case StatusVariable.TYPE_FLOAT:
                return Float.toString(statusVariable.getFloat());
            case StatusVariable.TYPE_INTEGER:
                return Integer.toString(statusVariable.getInteger());
            case StatusVariable.TYPE_STRING:
                return statusVariable.getString();
        }
        return "";
    }

    private CompositeData getCompositeData(StatusVariable statusVariable) throws OpenDataException {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put(NAME, statusVariable.getID());
        values.put(TYPE, statusVariable.getType());
        values.put(COLLECTION_METHOD, statusVariable.getCollectionMethod());
        values.put(TIMESTAMP, statusVariable.getTimeStamp().getTime());
        values.put(VALUE, getValueAsString(statusVariable));
        return new CompositeDataSupport(STATUS_VARIABLE_TYPE, values);
    }

    private CompositeData getCompositeData(MonitoringJob monitoringJob) throws OpenDataException {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put(INITIATOR, monitoringJob.getInitiator());
        values.put(REPORT_COUNT, monitoringJob.getReportCount());
        values.put(SCHEDULE, monitoringJob.getSchedule());
        values.put(STATUS_VARIABLE_NAMES, monitoringJob.getStatusVariableNames());
        values.put(LOCAL, monitoringJob.isLocal());
        values.put(RUNNING, monitoringJob.isRunning());
        return new CompositeDataSupport(MONITORING_JOB_TYPE, values);
    }

    public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws IllegalArgumentException {
        nbs.addNotificationListener(listener, filter, handback);
    }

    public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException {
        nbs.removeNotificationListener(listener);
    }

    public MBeanNotificationInfo[] getNotificationInfo() {
        if (notificationInfos == null) {
            notificationInfos = new MBeanNotificationInfo[]{
                    new MBeanNotificationInfo(new String[]{MonitorAdminMBean.EVENT},
                            Notification.class.getName(), MonitorAdminMBean.EVENT)
            };
        }
        return notificationInfos;
    }
}
