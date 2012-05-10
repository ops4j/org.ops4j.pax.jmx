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

package org.ops4j.pax.jmx.beans.service.provisioning;

import org.ops4j.pax.jmx.Utils;
import org.ops4j.pax.jmx.beans.ServiceAbstractMBean;
import org.osgi.jmx.service.provisioning.ProvisioningServiceMBean;

import javax.management.NotCompliantMBeanException;
import javax.management.openmbean.TabularData;
import java.io.IOException;
import java.net.URL;
import java.util.zip.ZipInputStream;

/**
 * ProvisioningServiceMBean Implementation
 *
 * @author dmytro.pishchukhin
 */
public class ProvisioningService extends ServiceAbstractMBean<org.osgi.service.provisioning.ProvisioningService>
        implements ProvisioningServiceMBean {

    public ProvisioningService() throws NotCompliantMBeanException {
        super(ProvisioningServiceMBean.class);
    }

    public void addInformationFromZip(String zipURL) throws IOException {
        try {
            service.addInformation(new ZipInputStream(new URL(zipURL).openStream()));
        } catch (IOException e) {
            logVisitor.warning("addInformationFromZip error", e);
            throw e;
        } catch (Exception e) {
            logVisitor.warning("addInformationFromZip error", e);
            throw new IOException(e.getMessage());
        }
    }

    public void addInformation(TabularData tabularData) throws IOException {
        try {
            service.addInformation(Utils.convertToDictionary(tabularData, true));
        } catch (Exception e) {
            logVisitor.warning("addInformation error", e);
            throw new IOException(e.getMessage());
        }
    }

    public TabularData listInformation() throws IOException {
        try {
            return Utils.getProperties(service.getInformation());
        } catch (Exception e) {
            logVisitor.warning("listInformation error", e);
            throw new IOException(e.getMessage());
        }
    }

    public void setInformation(TabularData tabularData) throws IOException {
        try {
            service.setInformation(Utils.convertToDictionary(tabularData, true));
        } catch (Exception e) {
            logVisitor.warning("setInformation error", e);
            throw new IOException(e.getMessage());
        }
    }
}
