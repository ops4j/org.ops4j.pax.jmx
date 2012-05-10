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

package org.ops4j.pax.jmx.beans.service.permissionadmin;

import org.ops4j.pax.jmx.beans.ServiceAbstractMBean;
import org.osgi.jmx.service.permissionadmin.PermissionAdminMBean;
import org.osgi.service.permissionadmin.PermissionInfo;

import javax.management.NotCompliantMBeanException;
import java.io.IOException;

/**
 * PermissionAdminMBean Implementation
 *
 * @author dmytro.pishchukhin
 */
public class PermissionAdmin extends ServiceAbstractMBean<org.osgi.service.permissionadmin.PermissionAdmin>
        implements PermissionAdminMBean {

    public PermissionAdmin() throws NotCompliantMBeanException {
        super(PermissionAdminMBean.class);
    }

    public String[] listLocations() throws IOException {
        try {
            return service.getLocations();
        } catch (Exception e) {
            logVisitor.warning("listLocations error", e);
            throw new IOException(e.getMessage());
        }
    }

    public String[] getPermissions(String location) throws IOException {
        try {
            return getPermissions(service.getPermissions(location));
        } catch (Exception e) {
            logVisitor.warning("getPermissions error", e);
            throw new IOException(e.getMessage());
        }
    }

    public void setDefaultPermissions(String[] encodedPermissions) throws IOException {
        try {
            service.setDefaultPermissions(getPermissions(encodedPermissions));
        } catch (Exception e) {
            logVisitor.warning("setDefaultPermissions error", e);
            throw new IOException(e.getMessage());
        }
    }

    public String[] listDefaultPermissions() throws IOException {
        try {
            return getPermissions(service.getDefaultPermissions());
        } catch (Exception e) {
            logVisitor.warning("listDefaultPermissions error", e);
            throw new IOException(e.getMessage());
        }
    }

    public void setPermissions(String location, String[] encodedPermissions) throws IOException {
        try {
            service.setPermissions(location, getPermissions(encodedPermissions));
        } catch (Exception e) {
            logVisitor.warning("setPermissions error", e);
            throw new IOException(e.getMessage());
        }
    }

    private PermissionInfo[] getPermissions(String[] encodedPermissions) {
        if (encodedPermissions == null) {
            return null;
        }
        PermissionInfo[] permissions = new PermissionInfo[encodedPermissions.length];
        for (int i = 0; i < encodedPermissions.length; i++) {
            permissions[i] = new PermissionInfo(encodedPermissions[i]);
        }
        return permissions;
    }

    private String[] getPermissions(PermissionInfo[] permissions) {
        if (permissions != null) {
            String[] result = new String[permissions.length];
            for (int i = 0; i < permissions.length; i++) {
                result[i] = permissions[i].getEncoded();
            }
            return result;
        }
        return new String[0];
    }
}
