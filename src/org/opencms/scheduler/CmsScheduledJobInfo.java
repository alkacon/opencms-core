/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/scheduler/CmsScheduledJobInfo.java,v $
 * Date   : $Date: 2005/05/11 10:22:41 $
 * Version: $Revision: 1.8 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.scheduler;

import org.opencms.configuration.I_CmsConfigurationParameterHandler;
import org.opencms.main.CmsContextInfo;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;

import org.quartz.Trigger;

/**
 * Describes a scheduled job for the OpenCms scheduler.<p>
 * 
 * <p>
 * The time the scheduled job is executed is defined with Unix 'cron-like' definitions.
 * </p>
 * 
 * <p>
 * For those unfamiliar with "cron", this means being able to create a firing
 * schedule such as: "At 8:00am every Monday through Friday" or "At 1:30am
 * every last Friday of the month".
 * </p>
 * 
 * <p>
 * A "Cron-Expression" is a string comprised of 6 or 7 fields separated by
 * white space. The 6 mandatory and 1 optional fields are as follows: <br>
 * 
 * <table cellspacing="8">
 * <tr>
 * <th align="left">Field Name</th>
 * <th align="left">&nbsp;</th>
 * <th align="left">Allowed Values</th>
 * <th align="left">&nbsp;</th>
 * <th align="left">Allowed Special Characters</th>
 * </tr>
 * <tr>
 * <td align="left"><code>Seconds</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>0-59</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Minutes</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>0-59</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Hours</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>0-23</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Day-of-month</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>1-31</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>, - * ? / L C</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Month</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>1-12 or JAN-DEC</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Day-of-Week</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>1-7 or SUN-SAT</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>, - * ? / L C #</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Year (Optional)</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>empty, 1970-2099</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * </table>
 * </p>
 * 
 * <p>
 * The '*' character is used to specify all values. For example, "*" in the
 * minute field means "every minute".
 * </p>
 * 
 * <p>
 * The '?' character is allowed for the day-of-month and day-of-week fields. It
 * is used to specify 'no specific value'. This is useful when you need to
 * specify something in one of the two fileds, but not the other. See the
 * examples below for clarification.
 * </p>
 * 
 * <p>
 * The '-' character is used to specify ranges For example "10-12" in the hour
 * field means "the hours 10, 11 and 12".
 * </p>
 * 
 * <p>
 * The ',' character is used to specify additional values. For example
 * "MON,WED,FRI" in the day-of-week field means "the days Monday, Wednesday,
 * and Friday".
 * </p>
 * 
 * <p>
 * The '/' character is used to specify increments. For example "0/15" in the
 * seconds field means "the seconds 0, 15, 30, and 45". And "5/15" in the
 * seconds field means "the seconds 5, 20, 35, and 50". You can also specify
 * '/' after the '*' character - in this case '*' is equivalent to having '0'
 * before the '/'.
 * </p>
 * 
 * <p>
 * The 'L' character is allowed for the day-of-month and day-of-week fields.
 * This character is short-hand for "last", but it has different meaning in
 * each of the two fields. For example, the value "L" in the day-of-month field
 * means "the last day of the month" - day 31 for January, day 28 for February
 * on non-leap years. If used in the day-of-week field by itself, it simply
 * means "7" or "SAT". But if used in the day-of-week field after another
 * value, it means "the last xxx day of the month" - for example "6L" means
 * "the last friday of the month". When using the 'L' option, it is important
 * not to specify lists, or ranges of values, as you'll get confusing results.
 * </p>
 * 
 * <p>
 * The 'W' character is allowed for the day-of-month field.  This character 
 * is used to specify the weekday (Monday-Friday) nearest the given day.  As an 
 * example, if you were to specify "15W" as the value for the day-of-month 
 * field, the meaning is: "the nearest weekday to the 15th of the month".  So
 * if the 15th is a Saturday, the trigger will fire on Friday the 14th.  If the
 * 15th is a Sunday, the trigger will fire on Monday the 16th.  If the 15th is
 * a Tuesday, then it will fire on Tuesday the 15th.  However if you specify
 * "1W" as the value for day-of-month, and the 1st is a Saturday, the trigger
 * will fire on Monday the 3rd, as it will not 'jump' over the boundary of a 
 * month's days.  The 'W' character can only be specified when the day-of-month 
 * is a single day, not a range or list of days.  
 * </p>
 * 
 * <p>
 * The 'L' and 'W' characters can also be combined for the day-of-month 
 * expression to yield 'LW', which translates to "last weekday of the month".
 * </p>
 * 
 * <p>
 * The '#' character is allowed for the day-of-week field. This character is
 * used to specify "the nth" XXX day of the month. For example, the value of
 * "6#3" in the day-of-week field means the third Friday of the month (day 6 =
 * Friday and "#3" = the 3rd one in the month). Other examples: "2#1" = the
 * first Monday of the month and "4#5" = the fifth Wednesday of the month. Note
 * that if you specify "#5" and there is not 5 of the given day-of-week in the
 * month, then no firing will occur that month.
 * </p>
 * 
 * <p>
 * The 'C' character is allowed for the day-of-month and day-of-week fields.
 * This character is short-hand for "calendar". This means values are
 * calculated against the associated calendar, if any. If no calendar is
 * associated, then it is equivalent to having an all-inclusive calendar. A
 * value of "5C" in the day-of-month field means "the first day included by the
 * calendar on or after the 5th". A value of "1C" in the day-of-week field
 * means "the first day included by the calendar on or after sunday".
 * </p>
 * 
 * <p>
 * The legal characters and the names of months and days of the week are not
 * case sensitive.
 * </p>
 * 
 * <p>
 * Here are some full examples: <br><table cellspacing="8">
 * <tr>
 * <th align="left">Expression</th>
 * <th align="left">&nbsp;</th>
 * <th align="left">Meaning</th>
 * </tr>
 * <tr>
 * <td align="left"><code>"0 0 12 * * ?"</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>Fire at 12pm (noon) every day</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>"0 15 10 ? * *"</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>Fire at 10:15am every day</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>"0 15 10 * * ?"</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>Fire at 10:15am every day</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>"0 15 10 * * ? *"</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>Fire at 10:15am every day</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>"0 15 10 * * ? 2005"</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>Fire at 10:15am every day during the year 2005</code>
 * </td>
 * </tr>
 * <tr>
 * <td align="left"><code>"0 * 14 * * ?"</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>Fire every minute starting at 2pm and ending at 2:59pm, every day</code>
 * </td>
 * </tr>
 * <tr>
 * <td align="left"><code>"0 0/5 14 * * ?"</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>Fire every 5 minutes starting at 2pm and ending at 2:55pm, every day</code>
 * </td>
 * </tr>
 * <tr>
 * <td align="left"><code>"0 0/5 14,18 * * ?"</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>Fire every 5 minutes starting at 2pm and ending at 2:55pm, AND fire every 5 minutes starting at 6pm and ending at 6:55pm, every day</code>
 * </td>
 * </tr>
 * <tr>
 * <td align="left"><code>"0 0-5 14 * * ?"</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>Fire every minute starting at 2pm and ending at 2:05pm, every day</code>
 * </td>
 * </tr>
 * <tr>
 * <td align="left"><code>"0 10,44 14 ? 3 WED"</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>Fire at 2:10pm and at 2:44pm every Wednesday in the month of March.</code>
 * </td>
 * </tr>
 * <tr>
 * <td align="left"><code>"0 15 10 ? * MON-FRI"</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>Fire at 10:15am every Monday, Tuesday, Wednesday, Thursday and Friday</code>
 * </td>
 * </tr>
 * <tr>
 * <td align="left"><code>"0 15 10 15 * ?"</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>Fire at 10:15am on the 15th day of every month</code>
 * </td>
 * </tr>
 * <tr>
 * <td align="left"><code>"0 15 10 L * ?"</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>Fire at 10:15am on the last day of every month</code>
 * </td>
 * </tr>
 * <tr>
 * <td align="left"><code>"0 15 10 ? * 6L"</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>Fire at 10:15am on the last Friday of every month</code>
 * </td>
 * </tr>
 * <tr>
 * <td align="left"><code>"0 15 10 ? * 6L"</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>Fire at 10:15am on the last Friday of every month</code>
 * </td>
 * </tr>
 * <tr>
 * <td align="left"><code>"0 15 10 ? * 6L 2002-2005"</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>Fire at 10:15am on every last friday of every month during the years 2002, 2003, 2004 and 2005</code>
 * </td>
 * </tr>
 * <tr>
 * <td align="left"><code>"0 15 10 ? * 6#3"</code></td>
 * <td align="left">&nbsp;</td>
 * <td align="left"><code>Fire at 10:15am on the third Friday of every month</code>
 * </td>
 * </tr>
 * </table>
 * </p>
 * 
 * <p>
 * Pay attention to the effects of '?' and '*' in the day-of-week and
 * day-of-month fields!
 * </p>
 * 
 * <p>
 * <b>NOTES:</b>
 * <ul>
 * <li>Support for the features described for the 'C' character is not
 * complete.</li>
 * <li>Support for specifying both a day-of-week and a day-of-month value is
 * not complete (you'll need to use the '?' character in on of these fields).
 * </li>
 * <li>Be careful when setting fire times between mid-night and 1:00 AM -
 * "daylight savings" can cause a skip or a repeat depending on whether the
 * time moves back or jumps forward.</li>
 * </ul>
 * </p>
 * 
 */
public class CmsScheduledJobInfo implements I_CmsConfigurationParameterHandler {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsScheduledJobInfo.class);

    /** Indicates if this job is currently active in the scheduler or not. */
    private boolean m_active;

    /** The name of the class to schedule. */
    private String m_className;

    /** The context information for the user to execute the job with. */
    private CmsContextInfo m_context;

    /** The cron expression for this scheduler job. */
    private String m_cronExpression;

    /** Indicates if the configuration of this job is finalized (frozen). */
    private boolean m_frozen;

    /** The id of this job. */
    private String m_id;

    /** Instance object of the scheduled job (only required when instance is re-used). */
    private I_CmsScheduledJob m_jobInstance;

    /** The name of the job (for information purposes). */
    private String m_jobName;

    /** The parameters used for this job entry. */
    private SortedMap m_parameters;

    /** Indicates if the job instance should be re-used if the job is run. */
    private boolean m_reuseInstance;

    /** The (cron) trigger used for scheduling this job. */
    private Trigger m_trigger;

    /**
     * Default constructor.<p>
     */
    public CmsScheduledJobInfo() {

        m_reuseInstance = false;
        m_frozen = false;
        // parameters are stored in a tree map 
        m_parameters = new TreeMap();
        // a job is active by default
        m_active = true;
    }

    /**
     * Constructor for creating a new job with all required parameters.<p> 
     * 
     * @param id the id of the job of <code>null</code> if a new id should be automatically generated
     * @param jobName the display name of the job 
     * @param className the class name of the job, must be an instance of <code>{@link I_CmsScheduledJob}</code>
     * @param context the OpenCms user context information to use when executing the job
     * @param cronExpression the cron expression for scheduling the job
     * @param reuseInstance indicates if the job class should be re-used
     * @param active indicates if the job should be active in the scheduler
     * @param parameters the job parameters
     */
    public CmsScheduledJobInfo(
        String id,
        String jobName,
        String className,
        CmsContextInfo context,
        String cronExpression,
        boolean reuseInstance,
        boolean active,
        Map parameters) {

        m_id = id;
        m_jobName = jobName;
        m_className = className;
        m_context = context;
        m_cronExpression = cronExpression;
        m_reuseInstance = reuseInstance;
        m_active = active;
        m_parameters = new TreeMap(parameters);
        m_frozen = false;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        checkFrozen();
        // add the configured parameter
        m_parameters.put(paramName, paramValue);
        if (LOG.isDebugEnabled()) {
            LOG.debug(org.opencms.configuration.Messages.get().key(
                org.opencms.configuration.Messages.LOG_ADD_CONFIG_PARAM_CALLED_3,
                paramName,
                paramValue,
                this));
        }
    }

    /**
     * Creates a clone of this scheduled job.<p>
     * 
     * The clone will not be active in the scheduler until it is scheduled
     * with <code>{@link CmsScheduleManager#scheduleJob(org.opencms.file.CmsObject, CmsScheduledJobInfo)}</code>. 
     * The job id returned by <code>{@link #getId()}</code> will be the same.
     * The <code>{@link #isActive()}</code> flag will be set to false. 
     * The clones job instance class will be the same 
     * if the <code>{@link #isReuseInstance()}</code> flag is set.<p>
     * 
     * @see java.lang.Object#clone()
     */
    public Object clone() {

        CmsScheduledJobInfo result = new CmsScheduledJobInfo();

        result.m_id = m_id;
        result.m_active = false;
        result.m_frozen = false;
        result.m_className = m_className;
        if (isReuseInstance()) {
            result.m_jobInstance = m_jobInstance;
        }
        result.m_context = m_context;
        result.m_cronExpression = m_cronExpression;
        result.m_jobName = m_jobName;
        result.m_parameters = new TreeMap(m_parameters);
        result.m_trigger = null;

        return result;
    }

    /**
     * Returns the name of the class to schedule.<p>
     * 
     * @return the name of the class to schedule
     */
    public String getClassName() {

        return m_className;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public Map getConfiguration() {

        // this configuration does not support parameters
        if (LOG.isDebugEnabled()) {
            LOG.debug(org.opencms.configuration.Messages.get().key(
                org.opencms.configuration.Messages.LOG_GET_CONFIGURATION_1,
                this));
        }
        return getParameters();
    }

    /**
     * Returns the context information for the user executing the job.<p>
     *
     * @return the context information for the user executing the job
     */
    public CmsContextInfo getContextInfo() {

        return m_context;
    }

    /**
     * Returns the cron expression for this job entry.<p>
     * 
     * @return the cron expression for this job entry
     */
    public String getCronExpression() {

        return m_cronExpression;
    }

    /**
     * Returns the next time at which this job will be executed, after the given time.<p>
     * 
     * If this job will not be executed after the given time, <code>null</code> will be returned..<p>
     * 
     * @param date the after which the next execution time should be calculated
     * @return the next time at which this job will be executed, after the given time
     */
    public Date getExecutionTimeAfter(Date date) {

        if (!m_active || (m_trigger == null)) {
            // if the job is not active, no time can be calculated
            return null;
        }

        return m_trigger.getFireTimeAfter(date);
    }

    /**
     * Returns the next time at which this job will be executed.<p> 
     * 
     * If the job will not execute again, <code>null</code> will be returned.<p>
     * 
     * @return the next time at which this job will be executed
     */
    public Date getExecutionTimeNext() {

        if (!m_active || (m_trigger == null)) {
            // if the job is not active, no time can be calculated
            return null;
        }

        return m_trigger.getNextFireTime();
    }

    /**
     * Returns the previous time at which this job will be executed.<p>
     * 
     * If this job has not yet been executed, <code>null</code> will be returned.
     * 
     * @return the previous time at which this job will be executed
     */
    public Date getExecutionTimePrevious() {

        if (!m_active || (m_trigger == null)) {
            // if the job is not active, no time can be calculated
            return null;
        }

        return m_trigger.getPreviousFireTime();
    }

    /**
     * Returns the internal id of this job in the scheduler.<p>
     * 
     * Can be used to remove this job from the scheduler with 
     * <code>{@link CmsScheduleManager#unscheduleJob(org.opencms.file.CmsObject, String)}</code>.<p> 
     * 
     * @return the internal id of this job in the scheduler
     */
    public String getId() {

        return m_id;
    }

    /**
     * Returns an instance of the configured job class.<p>
     * 
     * If any error occurs during class invocaion, the error 
     * is written to the OpenCms log and <code>null</code> is returned.<p>
     *
     * @return an instance of the configured job class, or null if an error occured
     */
    public synchronized I_CmsScheduledJob getJobInstance() {

        if (m_jobInstance != null) {

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().key(Messages.LOG_REUSING_INSTANCE_1, m_jobInstance.getClass().getName()));
            }

            // job instance already initialized
            return m_jobInstance;
        }

        I_CmsScheduledJob job = null;

        try {
            // create an instance of the OpenCms job class
            job = (I_CmsScheduledJob)Class.forName(getClassName()).newInstance();
        } catch (ClassNotFoundException e) {
            LOG.error(Messages.get().key(Messages.LOG_CLASS_NOT_FOUND_1, getClassName()), e);
        } catch (IllegalAccessException e) {
            LOG.error(Messages.get().key(Messages.LOG_ILLEGAL_ACCESS_0), e);
        } catch (InstantiationException e) {
            LOG.error(Messages.get().key(Messages.LOG_INSTANCE_GENERATION_0), e);
        } catch (ClassCastException e) {
            LOG.error(Messages.get().key(Messages.LOG_BAD_INTERFACE_0), e);
        }

        if (m_reuseInstance) {
            // job instance must be re-used
            m_jobInstance = job;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_JOB_CREATED_1, getClassName()));
        }

        return job;
    }

    /**
     * Returns the job name.<p>
     *
     * @return the job name
     */
    public String getJobName() {

        return m_jobName;
    }

    /**
     * Returns the parameters.<p>
     *
     * @return the parameters
     */
    public SortedMap getParameters() {

        return m_parameters;
    }

    /**
     * Finalizes (freezes) the configuration of this scheduler job entry.<p>
     * 
     * After this job entry has been frozen, any attempt to change the 
     * configuration of this entry with one of the "set..." methods
     * will lead to a <code>RuntimeException</code>.<p> 
     * 
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() {

        // simple default configuration does not need to be initialized
        if (LOG.isDebugEnabled()) {
            LOG.debug(org.opencms.configuration.Messages.get().key(
                org.opencms.configuration.Messages.LOG_INIT_CONFIGURATION_1,
                this));
        }
        setFrozen(true);
    }

    /**
     * Returns <code>true</code> if this job is currently active in the scheduler.<p>
     *
     * @return <code>true</code> if this job is currently active in the scheduler
     */
    public boolean isActive() {

        return m_active;
    }

    /**
     * Returns true if the job instance class is reused for this job.<p>
     *
     * @return true if the job instance class is reused for this job
     */
    public boolean isReuseInstance() {

        return m_reuseInstance;
    }

    /**
     * Sets the active state of this job.<p>
     *
     * @param active the active state to set
     */
    public void setActive(boolean active) {

        checkFrozen();
        m_active = active;
    }

    /**
     * Sets the name of the class to schedule.<p>
     * 
     * @param className the class name to set
     */
    public void setClassName(String className) {

        checkFrozen();
        m_className = className;

        if (m_jobName == null) {
            // initialize job name with class name as default
            setJobName(className);
        }
    }

    /**
     * Sets the context information for the user executing the job.<p>
     *
     * This will also "freeze" the context information that is set.<p>
     *
     * @param contextInfo the context information for the user executing the job
     * 
     * @see CmsContextInfo#freeze()
     */
    public void setContextInfo(CmsContextInfo contextInfo) {

        checkFrozen();
        contextInfo.freeze();
        m_context = contextInfo;
    }

    /**
     * Sets the cron expression for this job entry.<p>
     * 
     * @param cronExpression the cron expression to set
     */
    public void setCronExpression(String cronExpression) {

        checkFrozen();
        m_cronExpression = cronExpression;
    }

    /**
     * Sets the job name.<p>
     *
     * @param jobName the job name to set
     */
    public void setJobName(String jobName) {

        checkFrozen();
        m_jobName = jobName;
    }

    /**
     * Sets the job parameters.<p>
     *
     * @param parameters the parameters to set
     */
    public void setParameters(SortedMap parameters) {

        checkFrozen();
        m_parameters = parameters;
    }

    /**
     * Controls if the job instance class is reused for this job,
     * of if a new instance is generated every time the job is run.<p>
     * 
     * @param reuseInstance must be true if the job instance class is to be reused
     */
    public void setReuseInstance(boolean reuseInstance) {

        checkFrozen();
        m_reuseInstance = reuseInstance;
    }

    /**
     * Checks if this job info configuration is frozen.<p>
     * 
     * @throws CmsRuntimeException in case the configuration is already frozen
     */
    protected void checkFrozen() throws CmsRuntimeException {

        if (m_frozen) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_JOB_INFO_FROZEN_1, getJobName()));
        }
    }

    /**
     * Sets the "frozen" state of this job.<p>
     *
     * This is an internal operation to be used only by the <code>{@link CmsScheduleManager}</code>.<p>
     *
     * @param frozen the "frozen" state to set
     */
    protected synchronized void setFrozen(boolean frozen) {

        if (frozen && !m_frozen) {
            // "freeze" the job configuration
            m_parameters = Collections.unmodifiableSortedMap(m_parameters);
            m_frozen = true;
        } else if (!frozen && m_frozen) {
            // "unfreeze" the job configuration
            m_parameters = new TreeMap(m_parameters);
            m_frozen = false;
        }
    }

    /**
     * Sets the is used for scheduling this job.<p>
     *
     * This is an internal operation that should only by performed by the 
     * <code>{@link CmsScheduleManager}</code>, never by using this API directly.<p>
     * 
     * @param id the id to set
     */
    protected void setId(String id) {

        checkFrozen();
        m_id = id;
    }

    /**
     * Sets the (cron) trigger used for scheduling this job.<p>
     *
     * This is an internal operation that should only by performed by the 
     * <code>{@link CmsScheduleManager}</code>, never by using this API directly.<p>
     * 
     * @param trigger the (cron) trigger to set
     */
    protected void setTrigger(Trigger trigger) {

        checkFrozen();
        m_trigger = trigger;
    }
}