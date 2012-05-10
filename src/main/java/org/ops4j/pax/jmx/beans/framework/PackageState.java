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
import org.osgi.framework.Version;
import org.osgi.jmx.framework.PackageStateMBean;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;

import javax.management.NotCompliantMBeanException;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import java.io.IOException;
import java.util.*;

/**
 * PackageStateMBean Implementation
 *
 * @author dmytro.pishchukhin
 */
public class PackageState extends AbstractMBean implements PackageStateMBean {
    public PackageState() throws NotCompliantMBeanException {
        super(PackageStateMBean.class);
    }

    public long[] getExportingBundles(String packageName, String version) throws IOException {
        try {
            PackageAdmin packageAdmin = visitor.getPackageAdmin();
            if (packageAdmin == null) {
                throw new IOException("PackageAdmin is not available");
            }
            Version packageVersion = new Version(version);
            ExportedPackage[] packages = Utils.findPackages(packageAdmin.getExportedPackages((Bundle) null),
                    packageName, packageVersion);
            if (packages.length == 0) {
                throw new IllegalArgumentException("Package name/vesion are wrong: " + packageName + ", " + version);
            }
            Set<Bundle> bundles = new HashSet<Bundle>();
            for (ExportedPackage aPackage : packages) {
                Bundle bundle = aPackage.getExportingBundle();
                if (bundle != null) {
                    bundles.add(bundle);
                }
            }
            return Utils.getIds(bundles.toArray(new Bundle[bundles.size()]));
        } catch (IllegalArgumentException e) {
            logVisitor.warning("getExportingBundles error", e);
            throw e;
        } catch (IOException e) {
            logVisitor.warning("getExportingBundles error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("getExportingBundles error", e);
            throw new IOException(e.getMessage());
        }
    }

    public long[] getImportingBundles(String packageName, String version, long exportingBundle) throws IOException {
        try {
            Bundle bundle = visitor.getBundle(exportingBundle);
            PackageAdmin packageAdmin = visitor.getPackageAdmin();
            if (packageAdmin == null) {
                throw new IOException("PackageAdmin is not available");
            }
            Version packageVersion = new Version(version);

            ExportedPackage foundPackage = Utils.findPackage(packageAdmin.getExportedPackages(bundle), packageName, packageVersion);
            if (foundPackage == null) {
                throw new IllegalArgumentException("Package name/vesion are wrong: " + packageName + ", " + version);
            }
            return Utils.getIds(foundPackage.getImportingBundles());
        } catch (IllegalArgumentException e) {
            logVisitor.warning("getImportingBundles error", e);
            throw e;
        } catch (IOException e) {
            logVisitor.warning("getImportingBundles error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("getImportingBundles error", e);
            throw new IOException(e.getMessage());
        }
    }

    public TabularData listPackages() throws IOException {
        try {
            PackageAdmin packageAdmin = visitor.getPackageAdmin();
            if (packageAdmin == null) {
                throw new IOException("PackageAdmin is not available");
            }
            TabularDataSupport dataSupport = new TabularDataSupport(PACKAGES_TYPE);
            ExportedPackage[] exportedPackages = packageAdmin.getExportedPackages((Bundle) null);
            Map<String, PackageInfo> packages = new HashMap<String, PackageInfo>();
            if (exportedPackages != null) {
                for (ExportedPackage exportedPackage : exportedPackages) {
                    String key = exportedPackage.getName() + ";" + exportedPackage.getVersion().toString();
                    PackageInfo packageInfo = packages.get(key);
                    if (packageInfo == null) {
                        packageInfo = new PackageInfo(exportedPackage.getName(), exportedPackage.getVersion(), exportedPackage.isRemovalPending());
                        packages.put(key, packageInfo);
                    }
                    Bundle exportingBundle = exportedPackage.getExportingBundle();
                    if (exportingBundle != null) {
                        packageInfo.exportingBundles.add(exportingBundle);
                    }
                    Bundle[] importingBundles = exportedPackage.getImportingBundles();
                    if (importingBundles != null) {
                        packageInfo.importingBundles.addAll(Arrays.asList(importingBundles));
                    }
                }
            }
            Collection<PackageInfo> packageInfos = packages.values();
            for (PackageInfo packageInfo : packageInfos) {
                Map<String, Object> values = new HashMap<String, Object>();
                values.put(NAME, packageInfo.name);
                values.put(VERSION, packageInfo.version.toString());
                values.put(REMOVAL_PENDING, packageInfo.isRemovalPending);
                values.put(EXPORTING_BUNDLES, Utils.toLongArray(Utils.getIds(packageInfo.exportingBundles.toArray(new Bundle[packageInfo.exportingBundles.size()]))));
                values.put(IMPORTING_BUNDLES, Utils.toLongArray(Utils.getIds(packageInfo.importingBundles.toArray(new Bundle[packageInfo.importingBundles.size()]))));
                dataSupport.put(new CompositeDataSupport(PACKAGE_TYPE, values));
            }
            return dataSupport;
        } catch (IOException e) {
            logVisitor.warning("listPackages error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("listPackages error", e);
            throw new IOException(e.getMessage());
        }
    }

    public boolean isRemovalPending(String packageName, String version, long exportingBundle) throws IOException {
        try {
            Bundle bundle = visitor.getBundle(exportingBundle);
            PackageAdmin packageAdmin = visitor.getPackageAdmin();
            if (packageAdmin == null) {
                throw new IOException("PackageAdmin is not available");
            }
            Version packageVersion = new Version(version);

            ExportedPackage foundPackage = Utils.findPackage(packageAdmin.getExportedPackages(bundle), packageName, packageVersion);
            if (foundPackage == null) {
                throw new IllegalArgumentException("Package name/vesion are wrong: " + packageName + ", " + version);
            }
            return foundPackage.isRemovalPending();
        } catch (IllegalArgumentException e) {
            logVisitor.warning("isRemovalPending error", e);
            throw e;
        } catch (IOException e) {
            logVisitor.warning("isRemovalPending error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("isRemovalPending error", e);
            throw new IOException(e.getMessage());
        }
    }

    private static class PackageInfo {
        Set<Bundle> exportingBundles = new HashSet<Bundle>();
        Set<Bundle> importingBundles = new HashSet<Bundle>();
        boolean isRemovalPending;
        String name;
        Version version;

        private PackageInfo(String name, Version version, boolean removalPending) {
            this.name = name;
            this.version = version;
            isRemovalPending = removalPending;
        }
    }
}
