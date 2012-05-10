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

package org.ops4j.pax.jmx;

import org.ops4j.pax.jmx.beans.ServiceAbstractMBean;
import org.ops4j.pax.jmx.beans.framework.*;
import org.ops4j.pax.jmx.service.monitor.MonitorAdminMBean;
import org.osgi.framework.*;
import org.osgi.jmx.framework.BundleStateMBean;
import org.osgi.jmx.framework.FrameworkMBean;
import org.osgi.jmx.framework.PackageStateMBean;
import org.osgi.jmx.framework.ServiceStateMBean;
import org.osgi.jmx.service.cm.ConfigurationAdminMBean;
import org.osgi.jmx.service.permissionadmin.PermissionAdminMBean;
import org.osgi.jmx.service.provisioning.ProvisioningServiceMBean;
import org.osgi.jmx.service.useradmin.UserAdminMBean;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.log.LogService;
import org.osgi.service.monitor.MonitorAdmin;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.permissionadmin.PermissionAdmin;
import org.osgi.service.provisioning.ProvisioningService;
import org.osgi.service.startlevel.StartLevel;
import org.osgi.service.useradmin.UserAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.ops4j.pax.jmx.beans.LogVisitor;
import org.ops4j.pax.jmx.beans.OsgiVisitor;
import org.ops4j.pax.jmx.beans.framework.BundleState;
import org.ops4j.pax.jmx.beans.framework.Framework;
import org.ops4j.pax.jmx.beans.framework.ServiceState;

import javax.management.*;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JMX Management Model Activator
 *
 * @author dmytro.pishchukhin
 */
public class Activator implements BundleActivator, OsgiVisitor, LogVisitor {
    /**
     * Logger
     */
    private static final Logger LOG = Logger.getLogger(Activator.class.getName());

    /**
     * JMX server instance
     */
    private MBeanServer server;

    /**
     * Bundle context
     */
    private BundleContext bc;

    /**
     * {@link FrameworkMBean} implementation instance
     */
    private Framework framework;
    /**
     * {@link PackageStateMBean} implementation instance
     */
    private PackageState packageState;
    /**
     * {@link BundleStateMBean} implementation instance
     */
    private BundleState bundleState;
    /**
     * {@link ServiceStateMBean} implementation instance
     */
    private ServiceState serviceState;

    /**
     * ServiceTracker for {@link PackageAdmin} services
     */
    private ServiceTracker packageAdminTracker;
    /**
     * ServiceTracker for {@link StartLevel} services
     */
    private ServiceTracker startLevelTracker;
    /**
     * ServiceTracker for {@link org.osgi.framework.launch.Framework} services
     */
    private ServiceTracker frameworkTracker;
    /**
     * ServiceTracker for {@link LogService} services
     */
    private ServiceTracker logServiceTracker;

    /**
     * ServiceTracker for {@link org.osgi.service.cm.ConfigurationAdmin} services
     */
    private ServiceTracker configurationAdminTracker;
    /**
     * ServiceTracker for {@link org.osgi.service.permissionadmin.PermissionAdmin} services
     */
    private ServiceTracker permissionAdminTracker;
    /**
     * ServiceTracker for {@link org.osgi.service.provisioning.ProvisioningService} services
     */
    private ServiceTracker provisioningServiceTracker;
    /**
     * ServiceTracker for {@link org.osgi.service.useradmin.UserAdmin} services
     */
    private ServiceTracker userAdminTracker;
    /**
     * ServiceTracker for {@link org.osgi.service.monitor.MonitorAdmin} services
     */
    private ServiceTracker monitorAdminTracker;

    /**
     * Compendium services MBean registration map.
     */
    private Map<String, ServiceAbstractMBean> compendiumServices = new HashMap<String, ServiceAbstractMBean>();

    public void start(BundleContext context) throws Exception {
        try {
            bc = context;

            logServiceTracker = new ServiceTracker(bc, "org.osgi.service.log.LogService", null);
            logServiceTracker.open();

            server = ManagementFactory.getPlatformMBeanServer();

            registerCoreTrackers();

            registerJmxBeans();

            registerCompendiumTrackers();

            info("JMX Management Model started", null);
        } catch (Throwable e) {
            error("Unable to start JMX Management Model", e);

            tryToUnregisterBeans();

            throw new Exception(e);
        }
    }

    private void tryToUnregisterBeans() {
        if (server != null) {
            unregisterBean(PackageStateMBean.OBJECTNAME);
            unregisterBean(ServiceStateMBean.OBJECTNAME);
            unregisterBean(BundleStateMBean.OBJECTNAME);
            unregisterBean(FrameworkMBean.OBJECTNAME);

            unregisterTrackedBeans(permissionAdminTracker, PermissionAdminMBean.OBJECTNAME);
            unregisterTrackedBeans(configurationAdminTracker, ConfigurationAdminMBean.OBJECTNAME);
            unregisterTrackedBeans(provisioningServiceTracker, ProvisioningServiceMBean.OBJECTNAME);
            unregisterTrackedBeans(userAdminTracker, UserAdminMBean.OBJECTNAME);
            unregisterTrackedBeans(monitorAdminTracker, MonitorAdminMBean.OBJECTNAME);
        }
    }

    private void unregisterTrackedBeans(ServiceTracker tracker, String objectname) {
        if (tracker != null) {
            ServiceReference[] serviceReferences = tracker.getServiceReferences();
            if (serviceReferences != null) {
            for (ServiceReference serviceReference : serviceReferences) {
                unregisterBean(createObjectName(objectname, getServiceReferenceId(serviceReference)));
            }
        }
        }
    }

    private void unregisterBean(String beanName) {
        try {
            server.unregisterMBean(new ObjectName(beanName));
        } catch (Throwable e) {
            // ignore
        }
    }

    public void stop(BundleContext context) throws Exception {
        unregisterCompendiumTrackers();

        unregisterJmxBeans();

        unregisterCoreTrackers();

        info("JMX Management Model stoppped", null);

        server = null;

        if (logServiceTracker != null) {
            logServiceTracker.close();
            logServiceTracker = null;
        }

        bc = null;
    }

    private void unregisterCompendiumTrackers() {
        if (monitorAdminTracker != null) {
            monitorAdminTracker.close();
            monitorAdminTracker = null;
        }

        if (userAdminTracker != null) {
            userAdminTracker.close();
            userAdminTracker = null;
        }

        if (provisioningServiceTracker != null) {
            provisioningServiceTracker.close();
            provisioningServiceTracker = null;
        }

        if (configurationAdminTracker != null) {
            configurationAdminTracker.close();
            configurationAdminTracker = null;
        }

        if (permissionAdminTracker != null) {
            permissionAdminTracker.close();
            permissionAdminTracker = null;
        }
    }

    private void unregisterCoreTrackers() {
        if (frameworkTracker != null) {
            frameworkTracker.close();
            frameworkTracker = null;
        }

        if (startLevelTracker != null) {
            startLevelTracker.close();
            startLevelTracker = null;
        }

        if (packageAdminTracker != null) {
            packageAdminTracker.close();
            packageAdminTracker = null;
        }
    }

    private void registerCompendiumTrackers() {
        permissionAdminTracker = new ServiceTracker(bc, "org.osgi.service.permissionadmin.PermissionAdmin",
                new CompendiumServiceCustomizer<PermissionAdmin>(org.ops4j.pax.jmx.beans.service.permissionadmin.PermissionAdmin.class,
                        PermissionAdminMBean.OBJECTNAME));
        permissionAdminTracker.open();

        configurationAdminTracker = new ServiceTracker(bc, "org.osgi.service.cm.ConfigurationAdmin",
                new CompendiumServiceCustomizer<ConfigurationAdmin>(org.ops4j.pax.jmx.beans.service.cm.ConfigurationAdmin.class,
                        ConfigurationAdminMBean.OBJECTNAME));
        configurationAdminTracker.open();

        provisioningServiceTracker = new ServiceTracker(bc, "org.osgi.service.provisioning.ProvisioningService",
                new CompendiumServiceCustomizer<ProvisioningService>(org.ops4j.pax.jmx.beans.service.provisioning.ProvisioningService.class,
                        ProvisioningServiceMBean.OBJECTNAME));
        provisioningServiceTracker.open();

        userAdminTracker = new ServiceTracker(bc, "org.osgi.service.useradmin.UserAdmin",
                new CompendiumServiceCustomizer<UserAdmin>(org.ops4j.pax.jmx.beans.service.useradmin.UserAdmin.class,
                        UserAdminMBean.OBJECTNAME));
        userAdminTracker.open();

        monitorAdminTracker = new ServiceTracker(bc, "org.osgi.service.monitor.MonitorAdmin",
                new CompendiumServiceCustomizer<MonitorAdmin>(org.ops4j.pax.jmx.beans.service.monitor.MonitorAdmin.class,
                        MonitorAdminMBean.OBJECTNAME));
        monitorAdminTracker.open();
    }

    private void registerCoreTrackers() {
        packageAdminTracker = new ServiceTracker(bc, "org.osgi.service.packageadmin.PackageAdmin", null);
        packageAdminTracker.open();

        startLevelTracker = new ServiceTracker(bc, "org.osgi.service.startlevel.StartLevel", null);
        startLevelTracker.open();

        frameworkTracker = new ServiceTracker(bc, "org.osgi.framework.launch.Framework", null);
        frameworkTracker.open();
    }

    private void unregisterJmxBeans() {
        unregisterBean(PackageStateMBean.OBJECTNAME);
        packageState.uninit();

        bc.removeServiceListener(serviceState);
        unregisterBean(ServiceStateMBean.OBJECTNAME);
        serviceState.uninit();

        bc.removeBundleListener(bundleState);
        unregisterBean(BundleStateMBean.OBJECTNAME);
        bundleState.uninit();

        unregisterBean(FrameworkMBean.OBJECTNAME);
        framework.uninit();
    }

    private void registerJmxBeans()
            throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
        framework = new Framework();
        framework.setVisitor(this);
        framework.setLogVisitor(this);
        server.registerMBean(framework, new ObjectName(FrameworkMBean.OBJECTNAME));

        bundleState = new BundleState();
        bundleState.setVisitor(this);
        bundleState.setLogVisitor(this);
        bc.addBundleListener(bundleState);
        server.registerMBean(bundleState, new ObjectName(BundleStateMBean.OBJECTNAME));

        serviceState = new ServiceState();
        serviceState.setVisitor(this);
        serviceState.setLogVisitor(this);
        bc.addServiceListener(serviceState);
        server.registerMBean(serviceState, new ObjectName(ServiceStateMBean.OBJECTNAME));

        packageState = new PackageState();
        packageState.setVisitor(this);
        packageState.setLogVisitor(this);
        server.registerMBean(packageState, new ObjectName(PackageStateMBean.OBJECTNAME));
    }

    public void debug(String message, Throwable throwable) {
        Object logService = logServiceTracker.getService();
        if (logService != null) {
            ((LogService) logService).log(LogService.LOG_DEBUG, message, throwable);
        } else {
            LOG.log(Level.FINE, message, throwable);
        }
    }

    public void info(String message, Throwable throwable) {
        Object logService = logServiceTracker.getService();
        if (logService != null) {
            ((LogService) logService).log(LogService.LOG_INFO, message, throwable);
        } else {
            LOG.log(Level.INFO, message, throwable);
        }
    }

    public void warning(String message, Throwable throwable) {
        Object logService = logServiceTracker.getService();
        if (logService != null) {
            ((LogService) logService).log(LogService.LOG_WARNING, message, throwable);
        } else {
            LOG.log(Level.WARNING, message, throwable);
        }
    }

    public void error(String message, Throwable throwable) {
        Object logService = logServiceTracker.getService();
        if (logService != null) {
            ((LogService) logService).log(LogService.LOG_ERROR, message, throwable);
        } else {
            LOG.log(Level.SEVERE, message, throwable);
        }
    }

    public Bundle getBundle(long id) {
        return bc.getBundle(id);
    }

    public ServiceReference getServiceReferenceById(long id) {
        try {
            ServiceReference[] serviceReferences = bc.getAllServiceReferences(null, createServiceIdFilter(id));
            if (serviceReferences != null && serviceReferences.length == 1) {
                return serviceReferences[0];
            }
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ServiceReference[] getAllServiceReferences() {
        try {
            ServiceReference[] serviceReferences = bc.getAllServiceReferences(null, null);
            if (serviceReferences == null) {
                return new ServiceReference[0];
            }
            return serviceReferences;
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
        }
        return new ServiceReference[0];
    }

    public PackageAdmin getPackageAdmin() {
        return (PackageAdmin) packageAdminTracker.getService();
    }

    public StartLevel getStartLevel() {
        return (StartLevel) startLevelTracker.getService();
    }

    public Bundle installBundle(String location) throws BundleException {
        return bc.installBundle(location);
    }

    public Bundle installBundle(String location, InputStream stream) throws BundleException {
        return bc.installBundle(location, stream);
    }

    public org.osgi.framework.launch.Framework getFramework() {
        return (org.osgi.framework.launch.Framework) frameworkTracker.getService();
    }

    public Bundle[] getBundles() {
        return bc.getBundles();
    }

    public String getProperty(String name) {
        return bc.getProperty(name);
    }

    public ServiceRegistration registerService(String className, Object object, Dictionary props) {
        return bc.registerService(className, object, props);
    }

    private String createServiceIdFilter(long id) {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        builder.append(Constants.SERVICE_ID);
        builder.append('=');
        builder.append(id);
        builder.append(')');
        return builder.toString();
    }

    private String createObjectName(String objectNamePrefix, long serviceId) {
        return objectNamePrefix + ",service.id=" + serviceId;
    }

    private Long getServiceReferenceId(ServiceReference reference) {
        return (Long) reference.getProperty(Constants.SERVICE_ID);
    }

    private class CompendiumServiceCustomizer<T> implements ServiceTrackerCustomizer {
        private Class<? extends ServiceAbstractMBean<T>> beanClass;
        private String objectNamePrefix;

        private CompendiumServiceCustomizer(Class<? extends ServiceAbstractMBean<T>> beanClass, String objectNamePrefix) {
            this.beanClass = beanClass;
            this.objectNamePrefix = objectNamePrefix;
        }

        public Object addingService(ServiceReference reference) {
            long serviceId = getServiceReferenceId(reference);
            T service = (T) bc.getService(reference);
            try {
                ServiceAbstractMBean<T> mBean = beanClass.newInstance();
                mBean.setVisitor(Activator.this);
                mBean.setLogVisitor(Activator.this);
                mBean.setService(service);
                mBean.init();
                String objectName = createObjectName(objectNamePrefix, serviceId);
                compendiumServices.put(objectName, mBean);
                server.registerMBean(mBean, new ObjectName(objectName));
                info("MBean registered for " + beanClass.getSimpleName() + " service.id: " + serviceId, null);
            } catch (Exception e) {
                warning("Unable to register MBean for " + beanClass.getSimpleName() + " service.id: " + serviceId, e);
            }
            return service;
        }

        public void modifiedService(ServiceReference reference, Object service) {
            // do nothing
        }

        public void removedService(ServiceReference reference, Object service) {
            long serviceId = getServiceReferenceId(reference);
            try {
                String objectName = createObjectName(objectNamePrefix, serviceId);
                ServiceAbstractMBean serviceMBean = compendiumServices.get(objectName);
                server.unregisterMBean(new ObjectName(objectName));
                serviceMBean.uninit();
                info("MBean unregistered for " + beanClass.getSimpleName() + " service.id: " + serviceId, null);
            } catch (Exception e) {
                warning("Unable to unregister MBean for " + beanClass.getSimpleName() + " service.id: " + serviceId, e);
            }

            bc.ungetService(reference);
        }
    }
}
