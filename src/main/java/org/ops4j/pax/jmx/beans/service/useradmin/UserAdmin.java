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

package org.ops4j.pax.jmx.beans.service.useradmin;

import org.ops4j.pax.jmx.Utils;
import org.ops4j.pax.jmx.beans.ServiceAbstractMBean;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.jmx.service.useradmin.UserAdminMBean;
import org.osgi.service.useradmin.Authorization;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;

import javax.management.NotCompliantMBeanException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.TabularData;
import java.io.IOException;
import java.util.*;

/**
 * UserAdminMBean Implementation
 *
 * @author dmytro.pishchukhin
 */
public class UserAdmin extends ServiceAbstractMBean<org.osgi.service.useradmin.UserAdmin>
        implements UserAdminMBean {

    public UserAdmin() throws NotCompliantMBeanException {
        super(UserAdminMBean.class);
    }

    public void addCredential(String key, byte[] value, String username) throws IOException {
        try {
            Role role = service.getRole(username);
            if (role == null || role.getType() != Role.USER) {
                throw new IllegalArgumentException(username + " is not User name");
            }
            Dictionary credentials = ((User) role).getCredentials();
            if (credentials != null) {
                credentials.put(key, value);
            }
        } catch (IllegalArgumentException e) {
            logVisitor.warning("addCredential error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("addCredential error", e);
            throw new IOException(e.getMessage());
        }
    }

    public void addCredentialString(String key, String value, String username) throws IOException {
        try {
            Role role = service.getRole(username);
            if (role == null || role.getType() != Role.USER) {
                throw new IllegalArgumentException(username + " is not User name");
            }
            Dictionary credentials = ((User) role).getCredentials();
            if (credentials != null) {
                credentials.put(key, value);
            }
        } catch (IllegalArgumentException e) {
            logVisitor.warning("addCredentialString error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("addCredentialString error", e);
            throw new IOException(e.getMessage());
        }
    }

    public boolean addMember(String groupname, String rolename) throws IOException {
        try {
            Role role = service.getRole(groupname);
            if (role != null && role.getType() == Role.GROUP) {
                Role member = service.getRole(rolename);
                if (member != null) {
                    return ((Group) role).addMember(member);
                }
            }
            return false;
        } catch (Exception e) {
            logVisitor.warning("addMember error", e);
            throw new IOException(e.getMessage());
        }
    }

    public void addPropertyString(String key, String value, String rolename) throws IOException {
        try {
            Role role = service.getRole(rolename);
            if (role != null) {
                Dictionary properties = role.getProperties();
                if (properties != null) {
                    properties.put(key, value);
                }
            }
        } catch (Exception e) {
            logVisitor.warning("addPropertyString error", e);
            throw new IOException(e.getMessage());
        }
    }

    public void addProperty(String key, byte[] value, String rolename) throws IOException {
        try {
            Role role = service.getRole(rolename);
            if (role != null) {
                Dictionary properties = role.getProperties();
                if (properties != null) {
                    properties.put(key, value);
                }
            }
        } catch (Exception e) {
            logVisitor.warning("addProperty error", e);
            throw new IOException(e.getMessage());
        }
    }

    public boolean addRequiredMember(String groupname, String rolename) throws IOException {
        try {
            Role role = service.getRole(groupname);
            if (role != null && role.getType() == Role.GROUP) {
                Role requiredMember = service.getRole(rolename);
                if (requiredMember != null) {
                    return ((Group) role).addRequiredMember(requiredMember);
                }
            }
            return false;
        } catch (Exception e) {
            logVisitor.warning("addRequiredMember error", e);
            throw new IOException(e.getMessage());
        }
    }

    public void createUser(String name) throws IOException {
        try {
            service.createRole(name, Role.USER);
        } catch (Exception e) {
            logVisitor.warning("createUser error", e);
            throw new IOException(e.getMessage());
        }
    }

    public void createGroup(String name) throws IOException {
        try {
            service.createRole(name, Role.GROUP);
        } catch (Exception e) {
            logVisitor.warning("createGroup error", e);
            throw new IOException(e.getMessage());
        }
    }

    public void createRole(String name) throws IOException {
        try {
            service.createRole(name, Role.ROLE);
        } catch (Exception e) {
            logVisitor.warning("createRole error", e);
            throw new IOException(e.getMessage());
        }
    }

    public CompositeData getAuthorization(String username) throws IOException {
        try {
            Role role = service.getRole(username);
            if (role == null || role.getType() != Role.USER) {
                throw new IllegalArgumentException(username + " is not User name");
            }
            Authorization authorization = service.getAuthorization((User) role);
            Map<String, Object> values = new HashMap<String, Object>();
            String name = authorization.getName();
            values.put(NAME, name);
            Role authRole = service.getRole(name);
            values.put(TYPE, authRole.getType());
            return new CompositeDataSupport(AUTORIZATION_TYPE, values);
        } catch (IllegalArgumentException e) {
            logVisitor.warning("getAuthorization error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("getAuthorization error", e);
            throw new IOException(e.getMessage());
        }
    }

    public TabularData getCredentials(String username) throws IOException {
        try {
            Role role = service.getRole(username);
            if (role == null || role.getType() != Role.USER) {
                throw new IllegalArgumentException(username + " is not User name");
            }
            return Utils.getProperties(((User) role).getCredentials());
        } catch (IllegalArgumentException e) {
            logVisitor.warning("getCredentials error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("getCredentials error", e);
            throw new IOException(e.getMessage());
        }
    }

    public CompositeData getGroup(String groupname) throws IOException {
        try {
            Role role = service.getRole(groupname);
            if (role == null || role.getType() != Role.GROUP) {
                throw new IllegalArgumentException(groupname + " is not Group name");
            }
            Group group = (Group) role;
            Map<String, Object> values = new HashMap<String, Object>();
            values.put(NAME, group.getName());
            values.put(TYPE, group.getProperties());
            values.put(PROPERTIES, Utils.getProperties(group.getProperties()));
            values.put(CREDENTIALS, Utils.getProperties(group.getCredentials()));
            values.put(MEMBERS, getRoleNames(group.getMembers()));
            values.put(REQUIRED_MEMBERS, getRoleNames(group.getRequiredMembers()));
            return new CompositeDataSupport(GROUP_TYPE, values);
        } catch (IllegalArgumentException e) {
            logVisitor.warning("getGroup error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("getGroup error", e);
            throw new IOException(e.getMessage());
        }
    }

    public String[] listGroups() throws IOException {
        try {
            return getRoleNames(null, Role.GROUP);
        } catch (Exception e) {
            logVisitor.warning("listGroups error", e);
            throw new IOException(e.getMessage());
        }
    }

    public String[] getGroups(String filter) throws IOException {
        try {
            return getRoleNames(filter, Role.GROUP);
        } catch (Exception e) {
            logVisitor.warning("getGroups error", e);
            throw new IOException(e.getMessage());
        }
    }

    public String[] getImpliedRoles(String username) throws IOException {
        try {
            Role role = service.getRole(username);
            if (role == null || role.getType() != Role.USER) {
                throw new IllegalArgumentException(username + " is not User name");
            }
            Authorization authorization = service.getAuthorization((User) role);
            if (authorization != null) {
                return authorization.getRoles();
            } else {
                return new String[0];
            }
        } catch (IllegalArgumentException e) {
            logVisitor.warning("getImpliedRoles error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("getImpliedRoles error", e);
            throw new IOException(e.getMessage());
        }
    }

    public String[] getMembers(String groupname) throws IOException {
        try {
            Role role = service.getRole(groupname);
            if (role == null || role.getType() != Role.GROUP) {
                throw new IllegalArgumentException(groupname + " is not Group name");
            }
            return getRoleNames(((Group) role).getMembers());
        } catch (IllegalArgumentException e) {
            logVisitor.warning("getMembers error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("getMembers error", e);
            throw new IOException(e.getMessage());
        }
    }

    public TabularData getProperties(String rolename) throws IOException {
        try {
            Role role = service.getRole(rolename);
            return Utils.getProperties(role != null ? role.getProperties() : null);
        } catch (Exception e) {
            logVisitor.warning("getProperties error", e);
            throw new IOException(e.getMessage());
        }
    }

    public String[] getRequiredMembers(String groupname) throws IOException {
        try {
            Role role = service.getRole(groupname);
            if (role == null || role.getType() != Role.GROUP) {
                throw new IllegalArgumentException(groupname + " is not Group name");
            }
            return getRoleNames(((Group) role).getRequiredMembers());
        } catch (IllegalArgumentException e) {
            logVisitor.warning("getRequiredMembers error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("getRequiredMembers error", e);
            throw new IOException(e.getMessage());
        }
    }

    public CompositeData getRole(String name) throws IOException {
        try {
            Role role = service.getRole(name);
            if (role != null) {
                Map<String, Object> values = new HashMap<String, Object>();
                values.put(NAME, role.getName());
                values.put(TYPE, role.getProperties());
                values.put(PROPERTIES, Utils.getProperties(role.getProperties()));
                return new CompositeDataSupport(ROLE_TYPE, values);
            }
            return null;
        } catch (Exception e) {
            logVisitor.warning("getRole error", e);
            throw new IOException(e.getMessage());
        }
    }

    public String[] listRoles() throws IOException {
        try {
            return getRoleNames(null, Role.ROLE);
        } catch (Exception e) {
            logVisitor.warning("listRoles error", e);
            throw new IOException(e.getMessage());
        }
    }

    public String[] getRoles(String filter) throws IOException {
        try {
            return getRoleNames(filter, Role.ROLE);
        } catch (Exception e) {
            logVisitor.warning("getRoles error", e);
            throw new IOException(e.getMessage());
        }
    }

    public CompositeData getUser(String username) throws IOException {
        try {
            Role role = service.getRole(username);
            if (role == null || role.getType() != Role.USER) {
                throw new IllegalArgumentException(username + " is not User name");
            }
            User user = (User) role;
            Map<String, Object> values = new HashMap<String, Object>();
            values.put(NAME, user.getName());
            values.put(TYPE, user.getProperties());
            values.put(PROPERTIES, Utils.getProperties(user.getProperties()));
            values.put(CREDENTIALS, Utils.getProperties(user.getCredentials()));
            return new CompositeDataSupport(USER_TYPE, values);
        } catch (IllegalArgumentException e) {
            logVisitor.warning("getUser error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("getUser error", e);
            throw new IOException(e.getMessage());
        }
    }

    public String getUserWithProperty(String key, String value) throws IOException {
        try {
            User user = service.getUser(key, value);
            if (user != null) {
                return user.getName();
            } else {
                return null;
            }
        } catch (Exception e) {
            logVisitor.warning("getUserWithProperty error", e);
            throw new IOException(e.getMessage());
        }
    }

    public String[] listUsers() throws IOException {
        try {
            return getRoleNames(null, Role.USER);
        } catch (Exception e) {
            logVisitor.warning("listUsers error", e);
            throw new IOException(e.getMessage());
        }
    }

    public String[] getUsers(String filter) throws IOException {
        try {
            return getRoleNames(filter, Role.USER);
        } catch (Exception e) {
            logVisitor.warning("getUsers error", e);
            throw new IOException(e.getMessage());
        }
    }

    public void removeCredential(String key, String username) throws IOException {
        try {
            Role role = service.getRole(username);
            if (role == null || role.getType() != Role.USER) {
                throw new IllegalArgumentException(username + " is not User name");
            }
            Dictionary credentials = ((User) role).getCredentials();
            if (credentials != null) {
                credentials.remove(key);
            }
        } catch (IllegalArgumentException e) {
            logVisitor.warning("removeCredential error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("removeCredential error", e);
            throw new IOException(e.getMessage());
        }
    }

    public boolean removeMember(String groupname, String rolename) throws IOException {
        try {
            Role role = service.getRole(groupname);
            if (role == null || role.getType() != Role.GROUP) {
                throw new IllegalArgumentException(groupname + " is not Group name");
            }
            return ((Group) role).removeMember(service.getRole(rolename));
        } catch (IllegalArgumentException e) {
            logVisitor.warning("removeMember error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("removeMember error", e);
            throw new IOException(e.getMessage());
        }
    }

    public void removeProperty(String key, String rolename) throws IOException {
        try {
            Role role = service.getRole(rolename);
            if (role != null) {
                Dictionary properties = role.getProperties();
                if (properties != null) {
                    properties.remove(key);
                }
            }
        } catch (Exception e) {
            logVisitor.warning("removeProperty error", e);
            throw new IOException(e.getMessage());
        }
    }

    public boolean removeRole(String name) throws IOException {
        try {
            return removeRole(name, Role.ROLE);
        } catch (Exception e) {
            logVisitor.warning("removeRole error", e);
            throw new IOException(e.getMessage());
        }
    }

    public boolean removeGroup(String name) throws IOException {
        try {
            return removeRole(name, Role.GROUP);
        } catch (Exception e) {
            logVisitor.warning("removeGroup error", e);
            throw new IOException(e.getMessage());
        }
    }

    public boolean removeUser(String name) throws IOException {
        try {
            return removeRole(name, Role.USER);
        } catch (Exception e) {
            logVisitor.warning("removeUser error", e);
            throw new IOException(e.getMessage());
        }
    }

    private String[] getRoleNames(String filter, int type) throws InvalidSyntaxException {
        Role[] roles = service.getRoles(filter);
        List<String> names = new ArrayList<String>();
        if (roles != null) {
            for (Role role : roles) {
                if (role.getType() == type) {
                    names.add(role.getName());
                }
            }
        }
        return names.toArray(new String[names.size()]);
    }

    private String[] getRoleNames(Role[] roles) {
        List<String> names = new ArrayList<String>();
        if (roles != null) {
            for (Role role : roles) {
                names.add(role.getName());
            }
        }
        return names.toArray(new String[names.size()]);
    }

    private boolean removeRole(String name, int type) {
        Role role = service.getRole(name);
        if (role != null && role.getType() == type) {
            return service.removeRole(name);
        } else {
            return false;
        }
    }
}
