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

package org.ops4j.pax.jmx.beans;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;

/**
 * Abstract MBean
 *
 * @author dpishchukhin
 */
public abstract class AbstractMBean extends StandardMBean {
    protected OsgiVisitor visitor;
    protected LogVisitor logVisitor;

    protected AbstractMBean(Class<?> mbeanInterface) throws NotCompliantMBeanException {
        super(mbeanInterface);
    }

    public void setVisitor(OsgiVisitor visitor) {
        this.visitor = visitor;
    }

    public void setLogVisitor(LogVisitor logVisitor) {
        this.logVisitor = logVisitor;
    }

    public void uninit() {
        visitor = null;
        logVisitor = null;
    }
}
