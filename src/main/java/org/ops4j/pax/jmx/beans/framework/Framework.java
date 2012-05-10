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
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.jmx.framework.FrameworkMBean;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.startlevel.StartLevel;

import javax.management.NotCompliantMBeanException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * FrameworkMBean Implementation
 *
 * @author dmytro.pishchukhin
 */
public class Framework extends AbstractMBean implements FrameworkMBean {
    public Framework() throws NotCompliantMBeanException {
        super(FrameworkMBean.class);
    }

    public int getFrameworkStartLevel() throws IOException {
        try {
            StartLevel startLevel = visitor.getStartLevel();
            if (startLevel == null) {
                throw new IOException("StartLevel is not available");
            }
            return startLevel.getStartLevel();
        } catch (IOException e) {
            logVisitor.warning("getFrameworkStartLevel error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("getFrameworkStartLevel error", e);
            throw new IOException(e.getMessage());
        }
    }

    public int getInitialBundleStartLevel() throws IOException {
        try {
            StartLevel startLevel = visitor.getStartLevel();
            if (startLevel == null) {
                throw new IOException("StartLevel is not available");
            }
            return startLevel.getInitialBundleStartLevel();
        } catch (IOException e) {
            logVisitor.warning("getInitialBundleStartLevel error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("getInitialBundleStartLevel error", e);
            throw new IOException(e.getMessage());
        }
    }

    public long installBundle(String location) throws IOException {
        try {
            Bundle bundle = visitor.installBundle(location);
            return bundle.getBundleId();
        } catch (Exception e) {
            logVisitor.warning("installBundle error", e);
            throw new IOException(e.getMessage());
        }
    }

    public long installBundleFromURL(String location, String url) throws IOException {
        try {
            Bundle bundle = visitor.installBundle(location, new URL(url).openStream());
            return bundle.getBundleId();
        } catch (Exception e) {
            logVisitor.warning("installBundleFromURL error", e);
            throw new IOException(e.getMessage());
        }
    }

    public CompositeData installBundles(String[] locations) throws IOException {
        try {
            Set<String> remainingLocations = new HashSet<String>();
            if (locations != null) {
                remainingLocations.addAll(Arrays.asList(locations));
            }
            Set<Bundle> completedBundles = new HashSet<Bundle>();
            boolean isSuccess = true;
            String errorBundleLocation = null;
            String errorDetails = null;

            Iterator<String> locationsIterator = remainingLocations.iterator();
            while (locationsIterator.hasNext()) {
                String location = locationsIterator.next();
                locationsIterator.remove();
                try {
                    Bundle bundle = visitor.installBundle(location);
                    completedBundles.add(bundle);
                } catch (BundleException e) {
                    logVisitor.warning("Bundle install error", e);
                    isSuccess = false;
                    errorBundleLocation = location;
                    errorDetails = e.getMessage();
                    break;
                }
            }

            Map<String, Object> values = new HashMap<String, Object>();
            values.put(REMAINING, remainingLocations.toArray(new String[remainingLocations.size()]));
            values.put(COMPLETED, Utils.getIds(completedBundles.toArray(new Bundle[completedBundles.size()])));
            values.put(BUNDLE_IN_ERROR, errorBundleLocation);
            values.put(ERROR, errorDetails);
            values.put(SUCCESS, isSuccess);
            return new CompositeDataSupport(BATCH_INSTALL_RESULT_TYPE, values);
        } catch (Exception e) {
            logVisitor.warning("Batch installBundles error", e);
            throw new IOException(e.getMessage());
        }
    }

    public CompositeData installBundlesFromURL(String[] locations, String[] urls) throws IOException {
        try {
            List<String> remainingLocations = new ArrayList<String>();
            List<Long> completedBundles = new ArrayList<Long>();
            boolean isSuccess = true;
            String errorBundleLocation = null;
            String errorDetails = null;

            if (locations != null && urls != null) {
                if (locations.length != urls.length) {
                    throw new IllegalArgumentException("Locations array length is not equal to urls array length");
                }
                remainingLocations.addAll(Arrays.asList(locations));

                for (int i = 0; remainingLocations.size() > 0; i++) {
                    String location = remainingLocations.remove(0);
                    try {
                        Bundle bundle = visitor.installBundle(location, new URL(urls[i]).openStream());
                        completedBundles.add(bundle.getBundleId());
                    } catch (Exception e) {
                        isSuccess = false;
                        errorBundleLocation = location;
                        errorDetails = e.getMessage();
                        break;
                    }
                }
            }
            Map<String, Object> values = new HashMap<String, Object>();
            values.put(REMAINING, remainingLocations.toArray(new String[remainingLocations.size()]));
            values.put(COMPLETED, completedBundles.toArray(new Long[completedBundles.size()]));
            values.put(BUNDLE_IN_ERROR, errorBundleLocation);
            values.put(ERROR, errorDetails);
            values.put(SUCCESS, isSuccess);
            return new CompositeDataSupport(BATCH_INSTALL_RESULT_TYPE, values);
        } catch (Exception e) {
            logVisitor.warning("Batch installBundlesFromURL error", e);
            throw new IOException(e.getMessage());
        }
    }

    public void refreshBundle(long bundleIdentifier) throws IOException {
        try {
            Bundle bundle = visitor.getBundle(bundleIdentifier);
            if (bundle == null) {
                throw new IllegalArgumentException("Bundle ID is wrong: " + bundleIdentifier);
            }
            PackageAdmin packageAdmin = visitor.getPackageAdmin();
            if (packageAdmin == null) {
                throw new IOException("PackageAdmin is not available");
            }
            packageAdmin.refreshPackages(new Bundle[]{bundle});
        } catch (IOException e) {
            logVisitor.warning("refreshBundle error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("refreshBundle error", e);
            throw new IOException(e.getMessage());
        }
    }

    public void refreshBundles(long[] bundleIdentifiers) throws IOException {
        try {
            if (bundleIdentifiers != null) {
                PackageAdmin packageAdmin = visitor.getPackageAdmin();
                if (packageAdmin == null) {
                    throw new IOException("PackageAdmin is not available");
                }
                Bundle[] bundles = new Bundle[bundleIdentifiers.length];
                for (int i = 0; i < bundleIdentifiers.length; i++) {
                    long bundleIdentifier = bundleIdentifiers[i];
                    Bundle bundle = visitor.getBundle(bundleIdentifier);
                    if (bundle == null) {
                        throw new IllegalArgumentException("Bundle ID is wrong: " + bundleIdentifier);
                    }
                    bundles[i] = bundle;
                }
                packageAdmin.refreshPackages(bundles);
            }
        } catch (IOException e) {
            logVisitor.warning("refreshBundles error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("refreshBundles error", e);
            throw new IOException(e.getMessage());
        }
    }

    public boolean resolveBundle(long bundleIdentifier) throws IOException {
        try {
            Bundle bundle = visitor.getBundle(bundleIdentifier);
            if (bundle == null) {
                throw new IllegalArgumentException("Bundle ID is wrong: " + bundleIdentifier);
            }
            PackageAdmin packageAdmin = visitor.getPackageAdmin();
            if (packageAdmin == null) {
                throw new IOException("PackageAdmin is not available");
            }
            return packageAdmin.resolveBundles(new Bundle[]{bundle});
        } catch (IllegalArgumentException e) {
            logVisitor.warning("resolveBundle error", e);
            throw e;
        } catch (IOException e) {
            logVisitor.warning("resolveBundle error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("resolveBundle error", e);
            throw new IOException(e.getMessage());
        }
    }

    public boolean resolveBundles(long[] bundleIdentifiers) throws IOException {
        try {
            if (bundleIdentifiers != null) {
                PackageAdmin packageAdmin = visitor.getPackageAdmin();
                if (packageAdmin == null) {
                    throw new IOException("PackageAdmin is not available");
                }
                Bundle[] bundles = new Bundle[bundleIdentifiers.length];
                for (int i = 0; i < bundleIdentifiers.length; i++) {
                    long bundleIdentifier = bundleIdentifiers[i];
                    Bundle bundle = visitor.getBundle(bundleIdentifier);
                    if (bundle == null) {
                        throw new IllegalArgumentException("Bundle ID is wrong: " + bundleIdentifier);
                    }
                    bundles[i] = bundle;
                }
                return packageAdmin.resolveBundles(bundles);
            } else {
                return false;
            }
        } catch (IOException e) {
            logVisitor.warning("resolveBundles error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("resolveBundles error", e);
            throw new IOException(e.getMessage());
        }
    }

    public void restartFramework() throws IOException {
        try {
            org.osgi.framework.launch.Framework framework = visitor.getFramework();
            if (framework != null) {
                framework.update();
            } else {
                Bundle bundle = visitor.getBundle(0);
                if (bundle != null) {
                    bundle.update();
                }
            }
        } catch (Exception e) {
            logVisitor.warning("restartFramework error", e);
            throw new IOException(e.getMessage());
        }
    }

    public void setBundleStartLevel(long bundleIdentifier, int newLevel) throws IOException {
        try {
            Bundle bundle = visitor.getBundle(bundleIdentifier);
            if (bundle == null) {
                throw new IllegalArgumentException("Bundle ID is wrong: " + bundleIdentifier);
            }
            StartLevel startLevel = visitor.getStartLevel();
            if (startLevel == null) {
                throw new IOException("StartLevel is not available");
            }
            startLevel.setBundleStartLevel(bundle, newLevel);
        } catch (IOException e) {
            logVisitor.warning("setBundleStartLevel error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("setBundleStartLevel error", e);
            throw new IOException(e.getMessage());
        }
    }

    public CompositeData setBundleStartLevels(long[] bundleIdentifiers, int[] newlevels) throws IOException {
        try {
            List<Long> bundleIds = new ArrayList<Long>();
            List<Long> completedBundles = new ArrayList<Long>();
            boolean isSuccess = true;
            long errorBundleId = 0;
            String errorDetails = null;

            if (bundleIdentifiers != null && newlevels != null) {
                if (bundleIdentifiers.length != newlevels.length) {
                    throw new IllegalArgumentException("BundlesId array length is not equal to levels array length");
                }
                bundleIds.addAll(Arrays.asList(Utils.toLongArray(bundleIdentifiers)));

                StartLevel startLevel = visitor.getStartLevel();
                if (startLevel == null) {
                    throw new IOException("StartLevel is not available");
                }

                for (int i = 0; bundleIds.size() > 0; i++) {
                    long bundleId = bundleIds.remove(0);
                    try {
                        Bundle bundle = visitor.getBundle(bundleId);
                        if (bundle == null) {
                            throw new IllegalArgumentException("Bundle ID is wrong: " + bundleId);
                        }
                        startLevel.setBundleStartLevel(bundle, newlevels[i]);
                        completedBundles.add(bundleId);
                    } catch (Exception e) {
                        logVisitor.warning("Bundle set start level error", e);
                        isSuccess = false;
                        errorBundleId = bundleId;
                        errorDetails = e.getMessage();
                        break;
                    }
                }
            }
            Map<String, Object> values = new HashMap<String, Object>();
            values.put(REMAINING, bundleIds.toArray(new Long[bundleIds.size()]));
            values.put(COMPLETED, completedBundles.toArray(new Long[completedBundles.size()]));
            values.put(BUNDLE_IN_ERROR, errorBundleId);
            values.put(ERROR, errorDetails);
            values.put(SUCCESS, isSuccess);
            return new CompositeDataSupport(BATCH_ACTION_RESULT_TYPE, values);
        } catch (Exception e) {
            logVisitor.warning("setBundleStartLevels error", e);
            throw new IOException(e.getMessage());
        }
    }

    public void setFrameworkStartLevel(int level) throws IOException {
        try {
            StartLevel startLevel = visitor.getStartLevel();
            if (startLevel == null) {
                throw new IOException("StartLevel is not available");
            }
            startLevel.setStartLevel(level);
        } catch (IOException e) {
            logVisitor.warning("setFrameworkStartLevel error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("setFrameworkStartLevel error", e);
            throw new IOException(e.getMessage());
        }
    }

    public void setInitialBundleStartLevel(int level) throws IOException {
        try {
            StartLevel startLevel = visitor.getStartLevel();
            if (startLevel == null) {
                throw new IOException("StartLevel is not available");
            }
            startLevel.setInitialBundleStartLevel(level);
        } catch (IOException e) {
            logVisitor.warning("setInitialBundleStartLevel error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("setInitialBundleStartLevel error", e);
            throw new IOException(e.getMessage());
        }
    }

    public void shutdownFramework() throws IOException {
        try {
            org.osgi.framework.launch.Framework framework = visitor.getFramework();
            if (framework != null) {
                framework.stop();
            } else {
                Bundle bundle = visitor.getBundle(0);
                if (bundle != null) {
                    bundle.stop();
                }
            }
        } catch (Exception e) {
            logVisitor.warning("shutdownFramework error", e);
            throw new IOException(e.getMessage());
        }
    }

    public void startBundle(long bundleIdentifier) throws IOException {
        try {
            Bundle bundle = visitor.getBundle(bundleIdentifier);
            if (bundle == null) {
                throw new IllegalArgumentException("Bundle ID is wrong: " + bundleIdentifier);
            }
            try {
                bundle.start();
            } catch (Exception e) {
                throw new IOException("Unable to start bundle: " + bundleIdentifier);
            }
        } catch (IllegalArgumentException e) {
            logVisitor.warning("startBundle error", e);
            throw e;
        } catch (IOException e) {
            logVisitor.warning("startBundle error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("startBundle error", e);
            throw new IOException(e.getMessage());
        }
    }

    public CompositeData startBundles(long[] bundleIdentifiers) throws IOException {
        try {
            List<Long> bundles = new ArrayList<Long>();
            List<Long> completedBundles = new ArrayList<Long>();
            boolean isSuccess = true;
            long errorBundleId = 0;
            String errorDetails = null;

            if (bundleIdentifiers != null) {
                bundles.addAll(Arrays.asList(Utils.toLongArray(bundleIdentifiers)));
            }

            Iterator<Long> bundleIterator = bundles.iterator();
            while (bundleIterator.hasNext()) {
                Long bundleId = bundleIterator.next();
                bundleIterator.remove();
                try {
                    Bundle bundle = visitor.getBundle(bundleId);
                    if (bundle == null) {
                        throw new IllegalArgumentException("Bundle ID is wrong: " + bundleId);
                    }
                    bundle.update();
                    completedBundles.add(bundleId);
                } catch (BundleException e) {
                    logVisitor.warning("Bundle start error", e);
                    isSuccess = false;
                    errorBundleId = bundleId;
                    errorDetails = e.getMessage();
                    break;
                }
            }

            Map<String, Object> values = new HashMap<String, Object>();
            values.put(REMAINING, bundles.toArray(new Long[bundles.size()]));
            values.put(COMPLETED, completedBundles.toArray(new Long[completedBundles.size()]));
            values.put(BUNDLE_IN_ERROR, errorBundleId);
            values.put(ERROR, errorDetails);
            values.put(SUCCESS, isSuccess);
            return new CompositeDataSupport(BATCH_ACTION_RESULT_TYPE, values);
        } catch (Exception e) {
            logVisitor.warning("startBundles error", e);
            throw new IOException(e.getMessage());
        }
    }

    public void stopBundle(long bundleIdentifier) throws IOException {
        try {
            Bundle bundle = visitor.getBundle(bundleIdentifier);
            if (bundle == null) {
                throw new IllegalArgumentException("Bundle ID is wrong: " + bundleIdentifier);
            }
            try {
                bundle.stop();
            } catch (Exception e) {
                throw new IOException("Unable to stop bundle: " + bundleIdentifier);
            }
        } catch (IllegalArgumentException e) {
            logVisitor.warning("stopBundle error", e);
            throw e;
        } catch (IOException e) {
            logVisitor.warning("stopBundle error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("stopBundle error", e);
            throw new IOException(e.getMessage());
        }
    }

    public CompositeData stopBundles(long[] bundleIdentifiers) throws IOException {
        try {
            List<Long> bundles = new ArrayList<Long>();
            if (bundleIdentifiers != null) {
                bundles.addAll(Arrays.asList(Utils.toLongArray(bundleIdentifiers)));
            }
            List<Long> completedBundles = new ArrayList<Long>();
            boolean isSuccess = true;
            long errorBundleId = 0;
            String errorDetails = null;

            Iterator<Long> bundleIterator = bundles.iterator();
            while (bundleIterator.hasNext()) {
                Long bundleId = bundleIterator.next();
                bundleIterator.remove();
                try {
                    Bundle bundle = visitor.getBundle(bundleId);
                    if (bundle == null) {
                        throw new IllegalArgumentException("Bundle ID is wrong: " + bundleId);
                    }
                    bundle.stop();
                    completedBundles.add(bundleId);
                } catch (BundleException e) {
                    logVisitor.warning("Bundle stop error", e);
                    isSuccess = false;
                    errorBundleId = bundleId;
                    errorDetails = e.getMessage();
                    break;
                }
            }

            Map<String, Object> values = new HashMap<String, Object>();
            values.put(REMAINING, bundles.toArray(new Long[bundles.size()]));
            values.put(COMPLETED, completedBundles.toArray(new Long[completedBundles.size()]));
            values.put(BUNDLE_IN_ERROR, errorBundleId);
            values.put(ERROR, errorDetails);
            values.put(SUCCESS, isSuccess);
            return new CompositeDataSupport(BATCH_ACTION_RESULT_TYPE, values);
        } catch (Exception e) {
            logVisitor.warning("stopBundles error", e);
            throw new IOException(e.getMessage());
        }
    }

    public void uninstallBundle(long bundleIdentifier) throws IOException {
        try {
            Bundle bundle = visitor.getBundle(bundleIdentifier);
            if (bundle == null) {
                throw new IllegalArgumentException("Bundle ID is wrong: " + bundleIdentifier);
            }
            try {
                bundle.uninstall();
            } catch (Exception e) {
                throw new IOException("Unable to uninstall bundle: " + bundleIdentifier);
            }
        } catch (IllegalArgumentException e) {
            logVisitor.warning("uninstallBundle error", e);
            throw e;
        } catch (IOException e) {
            logVisitor.warning("uninstallBundle error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("uninstallBundle error", e);
            throw new IOException(e.getMessage());
        }
    }

    public CompositeData uninstallBundles(long[] bundleIdentifiers) throws IOException {
        try {
            List<Long> bundleIds = new ArrayList<Long>();
            if (bundleIdentifiers != null) {
                bundleIds.addAll(Arrays.asList(Utils.toLongArray(bundleIdentifiers)));
            }
            List<Long> completedBundles = new ArrayList<Long>();
            boolean isSuccess = true;
            long errorBundleId = 0;
            String errorDetails = null;

            Iterator<Long> bundleIterator = bundleIds.iterator();
            while (bundleIterator.hasNext()) {
                long bundleId = bundleIterator.next();
                bundleIterator.remove();
                try {
                    Bundle bundle = visitor.getBundle(bundleId);
                    if (bundle == null) {
                        throw new IllegalArgumentException("Bundle ID is wrong: " + bundleId);
                    }
                    bundle.uninstall();
                    completedBundles.add(bundleId);
                } catch (BundleException e) {
                    logVisitor.warning("Bundle uninstall error", e);
                    isSuccess = false;
                    errorBundleId = bundleId;
                    errorDetails = e.getMessage();
                    break;
                }
            }

            Map<String, Object> values = new HashMap<String, Object>();
            values.put(REMAINING, bundleIds.toArray(new Long[bundleIds.size()]));
            values.put(COMPLETED, completedBundles.toArray(new Long[completedBundles.size()]));
            values.put(BUNDLE_IN_ERROR, errorBundleId);
            values.put(ERROR, errorDetails);
            values.put(SUCCESS, isSuccess);
            return new CompositeDataSupport(BATCH_ACTION_RESULT_TYPE, values);
        } catch (Exception e) {
            logVisitor.warning("uninstallBundles error", e);
            throw new IOException(e.getMessage());
        }
    }

    public void updateBundle(long bundleIdentifier) throws IOException {
        try {
            Bundle bundle = visitor.getBundle(bundleIdentifier);
            if (bundle == null) {
                throw new IllegalArgumentException("Bundle ID is wrong: " + bundleIdentifier);
            }
            try {
                bundle.update();
            } catch (Exception e) {
                throw new IOException("Unable to update bundle: " + bundleIdentifier);
            }
        } catch (IllegalArgumentException e) {
            logVisitor.warning("updateBundle error", e);
            throw e;
        } catch (IOException e) {
            logVisitor.warning("updateBundle error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("updateBundle error", e);
            throw new IOException(e.getMessage());
        }
    }

    public void updateBundleFromURL(long bundleIdentifier, String url) throws IOException {
        try {
            Bundle bundle = visitor.getBundle(bundleIdentifier);
            if (bundle == null) {
                throw new IllegalArgumentException("Bundle ID is wrong: " + bundleIdentifier);
            }
            try {
                bundle.update(new URL(url).openStream());
            } catch (Exception e) {
                throw new IOException("Unable to update bundle: " + bundleIdentifier);
            }
        } catch (IllegalArgumentException e) {
            logVisitor.warning("updateBundleFromURL error", e);
            throw e;
        } catch (IOException e) {
            logVisitor.warning("updateBundleFromURL error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("updateBundleFromURL error", e);
            throw new IOException(e.getMessage());
        }
    }

    public CompositeData updateBundles(long[] bundleIdentifiers) throws IOException {
        try {
            List<Long> bundleIds = new ArrayList<Long>();
            if (bundleIdentifiers != null) {
                bundleIds.addAll(Arrays.asList(Utils.toLongArray(bundleIdentifiers)));
            }
            List<Long> completedBundles = new ArrayList<Long>();
            boolean isSuccess = true;
            long errorBundleId = 0;
            String errorDetails = null;

            Iterator<Long> bundleIterator = bundleIds.iterator();
            while (bundleIterator.hasNext()) {
                long bundleId = bundleIterator.next();
                bundleIterator.remove();
                try {
                    Bundle bundle = visitor.getBundle(bundleId);
                    if (bundle == null) {
                        throw new IllegalArgumentException("Bundle ID is wrong: " + bundleId);
                    }
                    bundle.update();
                    completedBundles.add(bundleId);
                } catch (BundleException e) {
                    logVisitor.warning("Bundle update error", e);
                    isSuccess = false;
                    errorBundleId = bundleId;
                    errorDetails = e.getMessage();
                    break;
                }
            }

            Map<String, Object> values = new HashMap<String, Object>();
            values.put(REMAINING, bundleIds.toArray(new Long[bundleIds.size()]));
            values.put(COMPLETED, completedBundles.toArray(new Long[completedBundles.size()]));
            values.put(BUNDLE_IN_ERROR, errorBundleId);
            values.put(ERROR, errorDetails);
            values.put(SUCCESS, isSuccess);
            return new CompositeDataSupport(BATCH_ACTION_RESULT_TYPE, values);
        } catch (Exception e) {
            logVisitor.warning("updateBundles error", e);
            throw new IOException(e.getMessage());
        }
    }

    public CompositeData updateBundlesFromURL(long[] bundleIdentifiers, String[] urls) throws IOException {
        try {
            List<Long> bundleIds = new ArrayList<Long>();
            List<Long> completedBundles = new ArrayList<Long>();
            boolean isSuccess = true;
            long errorBundleId = 0;
            String errorDetails = null;

            if (bundleIdentifiers != null && urls != null) {
                if (bundleIdentifiers.length != urls.length) {
                    throw new IllegalArgumentException("BundlesId array length is not equal to urls array length");
                }
                bundleIds.addAll(Arrays.asList(Utils.toLongArray(bundleIdentifiers)));

                for (int i = 0; bundleIds.size() > 0; i++) {
                    long bundleId = bundleIds.remove(0);
                    try {
                        Bundle bundle = visitor.getBundle(bundleId);
                        if (bundle == null) {
                            throw new IllegalArgumentException("Bundle ID is wrong: " + bundleId);
                        }
                        bundle.update(new URL(urls[i]).openStream());
                        completedBundles.add(bundleId);
                    } catch (Exception e) {
                        logVisitor.warning("Bundle update from URL error", e);
                        isSuccess = false;
                        errorBundleId = bundleId;
                        errorDetails = e.getMessage();
                        break;
                    }
                }
            }
            Map<String, Object> values = new HashMap<String, Object>();
            values.put(REMAINING, bundleIds.toArray(new Long[bundleIds.size()]));
            values.put(COMPLETED, completedBundles.toArray(new Long[completedBundles.size()]));
            values.put(BUNDLE_IN_ERROR, errorBundleId);
            values.put(ERROR, errorDetails);
            values.put(SUCCESS, isSuccess);
            return new CompositeDataSupport(BATCH_ACTION_RESULT_TYPE, values);
        } catch (Exception e) {
            logVisitor.warning("updateBundlesFromURL error", e);
            throw new IOException(e.getMessage());
        }
    }

    public void updateFramework() throws IOException {
        try {
            Bundle bundle = visitor.getBundle(0);
            if (bundle != null) {
                bundle.update();
            }
        } catch (Exception e) {
            logVisitor.warning("updateFramework error", e);
            throw new IOException(e.getMessage());
        }
    }
}
