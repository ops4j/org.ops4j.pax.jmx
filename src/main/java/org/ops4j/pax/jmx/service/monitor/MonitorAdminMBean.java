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

package org.ops4j.pax.jmx.service.monitor;

import org.osgi.jmx.Item;
import org.osgi.jmx.JmxConstants;

import javax.management.openmbean.*;
import java.io.IOException;

/**
 * This MBean provides the management interface to the OSGi Monitor Admin Service
 *
 * @author dmytro.pishchukhin
 */
public interface MonitorAdminMBean {
    /**
     * Monitor Admin MBean object name.
     */
    String OBJECTNAME = "osgi.compendium:service=monitor,version=1.0";

    /**
     * The key NAME, used in {@link MonitorAdminMBean#NAME_ITEM}
     */
    String NAME = "Name";
    /**
     * The item for <code>StatusVariable</code> name. The key is {@link MonitorAdminMBean#NAME} and type is <code>SimpleType.STRING</code>.
     */
    Item NAME_ITEM = new Item(NAME, "StatusVariable Name", SimpleType.STRING);
    /**
     * The key TYPE, used in {@link MonitorAdminMBean#TYPE_ITEM}
     */
    String TYPE = "Type";
    /**
     * The item for <code>StatusVariable</code> type. The key is {@link MonitorAdminMBean#TYPE} and type is <code>SimpleType.INTEGER</code>.
     */
    Item TYPE_ITEM = new Item(TYPE, "StatusVariable Type", SimpleType.INTEGER);
    /**
     * The key COLLECTION_METHOD, used in {@link MonitorAdminMBean#COLLECTION_METHOD_ITEM}
     */
    String COLLECTION_METHOD = "CollectionMethod";
    /**
     * The item for <code>StatusVariable</code> collection method. The key is {@link MonitorAdminMBean#COLLECTION_METHOD} and type is <code>SimpleType.INTEGER</code>.
     */
    Item COLLECTION_METHOD_ITEM = new Item(COLLECTION_METHOD, "StatusVariable Collection Method", SimpleType.INTEGER);
    /**
     * The key TIMESTAMP, used in {@link MonitorAdminMBean#TIMESTAMP_ITEM}
     */
    String TIMESTAMP = "Timestamp";
    /**
     * The item for <code>StatusVariable</code> timestamp. The key is {@link MonitorAdminMBean#TIMESTAMP} and type is <code>SimpleType.LONG</code>.
     */
    Item TIMESTAMP_ITEM = new Item(TIMESTAMP, "StatusVariable Timestamp", SimpleType.LONG);
    /**
     * The key VALUE, used in {@link MonitorAdminMBean#VALUE_ITEM}
     */
    String VALUE = "Value";
    /**
     * The item for <code>StatusVariable</code> value as String. The key is {@link MonitorAdminMBean#VALUE} and type is <code>SimpleType.STRING</code>.
     */
    Item VALUE_ITEM = new Item(VALUE, "StatusVariable Value as String", SimpleType.STRING);

    /**
     * The key STATUS_VARIABLE, used in {@link MonitorAdminMBean#STATUS_VARIABLE_TYPE}
     */
    String STATUS_VARIABLE = "StatusVariable";
    /**
     * The Composite Type for a <code>StatusVariable</code>. It contains the following items:
     * <ul>
     * <li>{@link MonitorAdminMBean#NAME}</li>
     * <li>{@link MonitorAdminMBean#TYPE}</li>
     * <li>{@link MonitorAdminMBean#COLLECTION_METHOD}</li>
     * <li>{@link MonitorAdminMBean#TIMESTAMP}</li>
     * <li>{@link MonitorAdminMBean#VALUE}</li>
     * </ul>
     */
    CompositeType STATUS_VARIABLE_TYPE = Item.compositeType(STATUS_VARIABLE, "This type incapsulates StatusVariable",
            NAME_ITEM, TYPE_ITEM, COLLECTION_METHOD_ITEM, TIMESTAMP_ITEM, VALUE_ITEM);

    /**
     * The key STATUS_VARIABLES, used in {@link MonitorAdminMBean#STATUS_VARIABLES_TYPE}
     */
    String STATUS_VARIABLES = "StatusVariables";
    /**
     * The Tabular Type for a list of <code>StatusVAriable</code>s. The row type is {@link MonitorAdminMBean#STATUS_VARIABLE_TYPE}
     */
    TabularType STATUS_VARIABLES_TYPE = Item.tabularType(STATUS_VARIABLES, "A list of StatusVariables", STATUS_VARIABLE_TYPE,
            NAME);

    /**
     * The key INITIATOR, used in {@link MonitorAdminMBean#INITIATOR_ITEM}
     */
    String INITIATOR = "Initiator";
    /**
     * The item for <code>MonitoringJob</code> initiator. The key is {@link MonitorAdminMBean#INITIATOR} and type is <code>SimpleType.STRING</code>.
     */
    Item INITIATOR_ITEM = new Item(INITIATOR, "MonitoringJob Initiator", SimpleType.STRING);
    /**
     * The key REPORT_COUNT, used in {@link MonitorAdminMBean#REPORT_COUNT_ITEM}
     */
    String REPORT_COUNT = "ReportCount";
    /**
     * The item for <code>MonitoringJob</code> report count value. The key is {@link MonitorAdminMBean#REPORT_COUNT} and type is <code>SimpleType.INTEGER</code>.
     */
    Item REPORT_COUNT_ITEM = new Item(REPORT_COUNT, "MonitoringJob Report Count", SimpleType.INTEGER);
    /**
     * The key SCHEDULE, used in {@link MonitorAdminMBean#SCHEDULE_ITEM}
     */
    String SCHEDULE = "Schedule";
    /**
     * The item for <code>MonitoringJob</code> schedule value. The key is {@link MonitorAdminMBean#SCHEDULE} and type is <code>SimpleType.INTEGER</code>.
     */
    Item SCHEDULE_ITEM = new Item(SCHEDULE, "MonitoringJob Schedule", SimpleType.INTEGER);
    /**
     * The key STATUS_VARIABLE_NAMES, used in {@link MonitorAdminMBean#STATUS_VARIABLE_NAMES_ITEM}
     */
    String STATUS_VARIABLE_NAMES = "StatusVariable Names";
    /**
     * The item for <code>MonitoringJob</code> <code>StatusVariable</code> names. The key is {@link MonitorAdminMBean#STATUS_VARIABLE_NAMES} and type is <code>{@link JmxConstants#STRING_ARRAY_TYPE}</code>.
     */
    Item STATUS_VARIABLE_NAMES_ITEM = new Item(STATUS_VARIABLE_NAMES, "MonitoringJob StatusVariable Names", JmxConstants.STRING_ARRAY_TYPE);
    /**
     * The key LOCAL, used in {@link MonitorAdminMBean#LOCAL_ITEM}
     */
    String LOCAL = "Local";
    /**
     * The item for <code>MonitoringJob</code> <code>isLocal</code> value. The key is {@link MonitorAdminMBean#LOCAL} and type is <code>SimpleType.BOOLEAN</code>.
     */
    Item LOCAL_ITEM = new Item(LOCAL, "MonitoringJob isLocal flag", SimpleType.BOOLEAN);
    /**
     * The key RUNNING, used in {@link MonitorAdminMBean#RUNNING_ITEM}
     */
    String RUNNING = "Running";
    /**
     * The item for <code>MonitoringJob</code> <code>isRunning</code> value. The key is {@link MonitorAdminMBean#RUNNING} and type is <code>SimpleType.BOOLEAN</code>.
     */
    Item RUNNING_ITEM = new Item(RUNNING, "MonitoringJob isRunning flag", SimpleType.BOOLEAN);

    String MONITORING_JOB = "MonitoringJob";
    /**
     * The Composite Type for a <code>MonitoringJob</code>. It contains the following items:
     * <ul>
     * <li>{@link MonitorAdminMBean#INITIATOR}</li>
     * <li>{@link MonitorAdminMBean#REPORT_COUNT}</li>
     * <li>{@link MonitorAdminMBean#SCHEDULE}</li>
     * <li>{@link MonitorAdminMBean#STATUS_VARIABLE_NAMES}</li>
     * <li>{@link MonitorAdminMBean#LOCAL}</li>
     * <li>{@link MonitorAdminMBean#RUNNING}</li>
     * </ul>
     */
    CompositeType MONITORING_JOB_TYPE = Item.compositeType(MONITORING_JOB, "This type incapsulates MonitoringJob",
            INITIATOR_ITEM, REPORT_COUNT_ITEM, SCHEDULE_ITEM, STATUS_VARIABLE_NAMES_ITEM, LOCAL_ITEM, RUNNING_ITEM);

    /**
     * The key MONITORING_JOBS, used in {@link MonitorAdminMBean#MONITORING_JOBS_TYPE}
     */
    String MONITORING_JOBS = "MonitoringJobs";
    /**
     * The Tabular Type for a list of <code>MonitoringJob</code>s. The row type is {@link MonitorAdminMBean#MONITORING_JOB_TYPE}
     */
    TabularType MONITORING_JOBS_TYPE = Item.tabularType(MONITORING_JOBS, "A list of MonitoringJobs", MONITORING_JOB_TYPE,
            INITIATOR, STATUS_VARIABLE_NAMES);

    /**
     * The key MONITORABLE_PID, used in {@link MonitorAdminMBean#MONITORABLE_PID_ITEM}
     */
    String MONITORABLE_PID = "MonitorablePid";
    /**
     * The item for <code>StatusVariable</code> event Monitorable PID value. The key is {@link MonitorAdminMBean#MONITORABLE_PID}
     * and type is <code>SimpleType.STRING</code>.
     */
    Item MONITORABLE_PID_ITEM = new Item(MONITORABLE_PID, "StatusVariable event Monitorable PID", SimpleType.STRING);
    /**
     * The key STATUS_VARIABLE_NAME, used in {@link MonitorAdminMBean#STATUS_VARIABLE_NAME_ITEM}
     */
    String STATUS_VARIABLE_NAME = "StatusVariableName";
    /**
     * The item for <code>StatusVariable</code> event name value. The key is {@link MonitorAdminMBean#STATUS_VARIABLE_NAME}
     * and type is <code>SimpleType.STRING</code>.
     */
    Item STATUS_VARIABLE_NAME_ITEM = new Item(STATUS_VARIABLE_NAME, "StatusVariable event name value", SimpleType.STRING);
    /**
     * The key STATUS_VARIABLE_VALUE, used in {@link MonitorAdminMBean#STATUS_VARIABLE_VALUE_ITEM}
     */
    String STATUS_VARIABLE_VALUE = "StatusVariableValue";
    /**
     * The item for <code>StatusVariable</code> event value. The key is {@link MonitorAdminMBean#STATUS_VARIABLE_VALUE}
     * and type is <code>SimpleType.STRING</code>.
     */
    Item STATUS_VARIABLE_VALUE_ITEM = new Item(STATUS_VARIABLE_VALUE, "StatusVariable event value", SimpleType.STRING);
    /**
     * The key EVENT_INITIATOR, used in {@link MonitorAdminMBean#EVENT_INITIATOR_ITEM}
     */
    String EVENT_INITIATOR = "EventInitiator";
    /**
     * The item for <code>StatusVariable</code> event initiator. The key is {@link MonitorAdminMBean#EVENT_INITIATOR}
     * and type is <code>SimpleType.STRING</code>.
     */
    Item EVENT_INITIATOR_ITEM = new Item(EVENT_INITIATOR, "StatusVariable event initiator", SimpleType.STRING);
    /**
     * The key MONITORING_JOBS, used in {@link MonitorAdminMBean#EVENT_TYPE}
     */
    String EVENT = "MonitorAdminEvent";
    /**
     * The Composite Type for a <code>StatusVariable</code> change event. It contains the following items:
     * <ul>
     * <li>{@link MonitorAdminMBean#MONITORABLE_PID_ITEM}</li>
     * <li>{@link MonitorAdminMBean#STATUS_VARIABLE_NAME_ITEM}</li>
     * <li>{@link MonitorAdminMBean#STATUS_VARIABLE_VALUE_ITEM}</li>
     * <li>{@link MonitorAdminMBean#EVENT_INITIATOR_ITEM}</li>
     * </ul>
     */
    CompositeType EVENT_TYPE = Item.compositeType(EVENT, "This type incapsulates StatusVariable change event",
            MONITORABLE_PID_ITEM, STATUS_VARIABLE_NAME_ITEM, STATUS_VARIABLE_VALUE_ITEM, EVENT_INITIATOR_ITEM);

    /**
     * Returns a human readable description of the given <code>StatusVariable</code>.
     * The null value may be returned if there is no description for the given <code>StatusVariable</code>.
     *
     * @param path the full path of the <code>StatusVariable</code> in [Monitorable_ID]/[StatusVariable_ID] format
     * @return the human readable description of this <code>StatusVariable</code> or <code>null</code> if it is not set
     *
     * @throws IllegalArgumentException if path is <code>null</code> or otherwise invalid, or points to a non-existing <code>StatusVariable</code>
     * @throws IOException              if the operation fails
     */
    String getDescription(String path) throws IllegalArgumentException, IOException;

    /**
     * Returns a <code>StatusVariable</code> addressed by its full path.
     * The Composite Data is typed by {@link MonitorAdminMBean#STATUS_VARIABLE_TYPE}.
     *
     * @param path the full path of the <code>StatusVariable</code> in [Monitorable_ID]/[StatusVariable_ID] format
     * @return <code>StatusVariable</code> typed by {@link MonitorAdminMBean#STATUS_VARIABLE_TYPE}
     *
     * @throws IllegalArgumentException if path is <code>null</code> or otherwise invalid, or points to a non-existing <code>StatusVariable</code>
     * @throws IOException              if the operation fails
     */
    CompositeData getStatusVariable(String path) throws IllegalArgumentException, IOException;

    /**
     * Returns the names of the <code>Monitorable</code> services that are currently registered.
     * The returned array contains the names in alphabetical order.
     * It cannot be <code>null</code>, an empty array is returned if no <code>Monitorable</code> services are registered.
     *
     * @return the array of <code>Monitorable</code> names
     *
     * @throws IOException if the operation fails
     */
    java.lang.String[] getMonitorableNames() throws IOException;

    /**
     * Returns the <code>StatusVariable</code> objects published by a <code>Monitorable</code> instance.
     * The returned Tabular Data is typed by {@link MonitorAdminMBean#STATUS_VARIABLES_TYPE}.
     * The elements in the returned array are in no particular order.
     *
     * @param monitorableId the identifier of a <code>Monitorable</code> instance
     * @return <code>StatusVariable</code>s typed by {@link MonitorAdminMBean#STATUS_VARIABLES_TYPE}
     *
     * @throws IllegalArgumentException if monitorableId  is <code>null</code> or otherwise invalid, or points to a non-existing <code>Monitorable</code>
     * @throws IOException              if the operation fails
     */
    TabularData getStatusVariables(String monitorableId) throws IllegalArgumentException, IOException;

    /**
     * Returns the list of <code>StatusVariable</code> names published by a <code>Monitorable</code> instance.
     * The returned array does not contain duplicates, and the elements are in alphabetical order. It cannot be <code>null</code>,
     * an empty array is returned if no (authorized and readable) Status Variables are provided by the given <code>Monitorable</code>.
     *
     * @param monitorableId the identifier of a <code>Monitorable</code> instance
     * @return a list of <code>StatusVariable</code> objects names published by the specified <code>Monitorable</code>
     *
     * @throws IllegalArgumentException if monitorableId  is <code>null</code> or otherwise invalid, or points to a non-existing <code>Monitorable</code>
     * @throws IOException              if the operation fails
     */
    String[] getStatusVariableNames(String monitorableId) throws IllegalArgumentException, IOException;

    /**
     * Switches event sending on or off for the specified <code>StatusVariable</code>s.
     *
     * @param path the identifier of the <code>StatusVariable</code>(s) in [Monitorable_id]/[StatusVariable_id] format,
     *             possibly with the "*" wildcard at the end of either path fragment
     * @param on   <code>false</code> if event sending should be switched off, <code>true</code> if it should be switched on for the given path
     * @throws IllegalArgumentException if path is <code>null</code> or otherwise invalid, or points to a non-existing <code>StatusVariable</code>
     * @throws IOException              if the operation fails
     */
    void switchEvents(String path, boolean on) throws IllegalArgumentException, IOException;

    /**
     * Issues a request to reset a given <code>StatusVariable</code>.
     *
     * @param path the identifier of the <code>StatusVariable</code> in [Monitorable_id]/[StatusVariable_id] format
     * @return <code>true</code> if the <code>Monitorable</code> could successfully reset the given <code>StatusVariable</code>, <code>false</code> otherwise
     *
     * @throws IllegalArgumentException if path is <code>null</code> or otherwise invalid, or points to a non-existing <code>StatusVariable</code>
     * @throws IOException              if the operation fails
     */
    boolean resetStatusVariable(String path) throws IllegalArgumentException, IOException;

    /**
     * Returns the list of currently running <code>MonitoringJob</code>s.
     * The returned Tabular Data is typed by {@link MonitorAdminMBean#MONITORING_JOBS_TYPE}
     *
     * @return <code>MonitoringJob</code>s typed by {@link MonitorAdminMBean#MONITORING_JOBS_TYPE}
     *
     * @throws IOException if the operation fails
     */
    TabularData getRunningJobs() throws IOException;
}
