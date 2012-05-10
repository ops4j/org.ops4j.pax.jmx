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

package org.ops4j.pax.jmx.beans.framework;

import org.ops4j.pax.jmx.Utils;
import org.ops4j.pax.jmx.beans.AbstractMBean;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.jmx.JmxConstants;
import org.osgi.jmx.framework.ServiceStateMBean;

import javax.management.*;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * ServiceStateMBean Implementation
 *
 * @author dmytro.pishchukhin
 */
public class ServiceState extends AbstractMBean implements ServiceStateMBean, NotificationBroadcaster, ServiceListener {
    private NotificationBroadcasterSupport nbs;
    private MBeanNotificationInfo[] notificationInfos;
    private int sequenceNumber = 0;

    public ServiceState() throws NotCompliantMBeanException {
        super(ServiceStateMBean.class);
        nbs = new NotificationBroadcasterSupport();
    }

    public String[] getObjectClass(long serviceId) throws IOException {
        try {
            ServiceReference serviceReference = visitor.getServiceReferenceById(serviceId);
            if (serviceReference == null) {
                throw new IllegalArgumentException("Wrong Service ID: " + serviceId);
            }
            return (String[]) serviceReference.getProperty(Constants.OBJECTCLASS);
        } catch (IllegalArgumentException e) {
            logVisitor.warning("getObjectClass error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("getObjectClass error", e);
            throw new IOException(e.getMessage());
        }
    }

    public long getBundleIdentifier(long serviceId) throws IOException {
        try {
            ServiceReference serviceReference = visitor.getServiceReferenceById(serviceId);
            if (serviceReference == null) {
                throw new IllegalArgumentException("Wrong Service ID: " + serviceId);
            }
            return serviceReference.getBundle().getBundleId();
        } catch (IllegalArgumentException e) {
            logVisitor.warning("getBundleIdentifier error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("getBundleIdentifier error", e);
            throw new IOException(e.getMessage());
        }
    }

    public TabularData getProperties(long serviceId) throws IOException {
        try {
            ServiceReference serviceReference = visitor.getServiceReferenceById(serviceId);
            if (serviceReference == null) {
                throw new IllegalArgumentException("Wrong Service ID: " + serviceId);
            }
            TabularDataSupport dataSupport = new TabularDataSupport(JmxConstants.PROPERTIES_TYPE);
            String[] keys = serviceReference.getPropertyKeys();
            for (String key : keys) {
                Object value = serviceReference.getProperty(key);
                Map<String, Object> values = new HashMap<String, Object>();
                values.put(JmxConstants.KEY, key);
                values.put(JmxConstants.TYPE, Utils.getValueType(value));
                values.put(JmxConstants.VALUE, Utils.serializeToString(value));
                dataSupport.put(new CompositeDataSupport(JmxConstants.PROPERTY_TYPE, values));
            }
            return dataSupport;
        } catch (IllegalArgumentException e) {
            logVisitor.warning("getProperties error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("getProperties error", e);
            throw new IOException(e.getMessage());
        }
    }

    public TabularData listServices() throws IOException {
        try {
            ServiceReference[] serviceReferences = visitor.getAllServiceReferences();
            TabularDataSupport dataSupport = new TabularDataSupport(SERVICES_TYPE);
            if (serviceReferences != null) {
                for (ServiceReference serviceReference : serviceReferences) {
                    Map<String, Object> values = new HashMap<String, Object>();
                    values.put(BUNDLE_IDENTIFIER, serviceReference.getBundle().getBundleId());
                    values.put(IDENTIFIER, serviceReference.getProperty(Constants.SERVICE_ID));
                    values.put(OBJECT_CLASS, serviceReference.getProperty(Constants.OBJECTCLASS));
                    values.put(USING_BUNDLES, Utils.toLongArray(Utils.getIds(serviceReference.getUsingBundles())));
                    dataSupport.put(new CompositeDataSupport(SERVICE_TYPE, values));
                }
            }
            return dataSupport;
        } catch (Exception e) {
            logVisitor.warning("listServices error", e);
            throw new IOException(e.getMessage());
        }
    }

    public long[] getUsingBundles(long serviceId) throws IOException {
        try {
            ServiceReference serviceReference = visitor.getServiceReferenceById(serviceId);
            if (serviceReference == null) {
                throw new IllegalArgumentException("Wrong Service ID: " + serviceId);
            }
            return Utils.getIds(serviceReference.getUsingBundles());
        } catch (IllegalArgumentException e) {
            logVisitor.warning("getUsingBundles error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("getUsingBundles error", e);
            throw new IOException(e.getMessage());
        }
    }

    public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws IllegalArgumentException {
        nbs.addNotificationListener(listener, filter, handback);
    }

    public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException {
        nbs.removeNotificationListener(listener);
    }

    public synchronized MBeanNotificationInfo[] getNotificationInfo() {
        if (notificationInfos == null) {
            notificationInfos = new MBeanNotificationInfo[]{
                    new MBeanNotificationInfo(new String[]{ServiceStateMBean.EVENT},
                            Notification.class.getName(), ServiceStateMBean.EVENT)
            };
        }
        return notificationInfos;
    }

    public synchronized void serviceChanged(ServiceEvent event) {
        Notification notification = new Notification(ServiceStateMBean.EVENT, this, ++sequenceNumber,
                System.currentTimeMillis());

        try {
            ServiceReference serviceReference = event.getServiceReference();
            Map<String, Object> values = new HashMap<String, Object>();
            values.put(IDENTIFIER, serviceReference.getProperty(Constants.SERVICE_ID));
            values.put(OBJECT_CLASS, serviceReference.getProperty(Constants.OBJECTCLASS));
            values.put(BUNDLE_IDENTIFIER, serviceReference.getBundle().getBundleId());
            values.put(BUNDLE_LOCATION, serviceReference.getBundle().getLocation());
            values.put(BUNDLE_SYMBOLIC_NAME, serviceReference.getBundle().getSymbolicName());
            values.put(EVENT, event.getType());
            notification.setUserData(new CompositeDataSupport(ServiceStateMBean.SERVICE_EVENT_TYPE, values));

            nbs.sendNotification(notification);
        } catch (Exception e) {
            logVisitor.warning("Unable to send ServiceEvent notification", e);
        }
    }
}
