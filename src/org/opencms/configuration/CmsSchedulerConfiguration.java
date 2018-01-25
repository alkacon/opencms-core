/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.configuration;

import org.opencms.main.CmsContextInfo;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.scheduler.CmsScheduleManager;
import org.opencms.scheduler.CmsScheduledJobInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.digester3.Digester;

import org.dom4j.Element;

/**
 * Scheduled jobs configuration class.<p>
 */
public class CmsSchedulerConfiguration extends A_CmsXmlConfiguration {

    /** The name of the DTD for this configuration. */
    public static final String CONFIGURATION_DTD_NAME = "opencms-scheduler.dtd";

    /** The name of the default XML file for this configuration. */
    public static final String DEFAULT_XML_FILE_NAME = "opencms-scheduler.xml";

    /** The node name for the job "active" value. */
    public static final String N_ACTIVE = "active";

    /** The node name for a job class. */
    public static final String N_CLASS = "class";

    /** The node name for the job context. */
    public static final String N_CONTEXT = "context";

    /** The node name for the job cron expression. */
    public static final String N_CRONEXPRESSION = "cronexpression";

    /** The node name for the context encoding. */
    public static final String N_ENCODING = "encoding";

    /** The node name for a job. */
    public static final String N_JOB = "job";

    /** The node name for individual locales. */
    public static final String N_LOCALE = "locale";

    /** The node name for the job parameters. */
    public static final String N_PARAMETERS = "parameters";

    /** The node name for the context project name. */
    public static final String N_PROJECT = "project";

    /** The node name for the context remote addr. */
    public static final String N_REMOTEADDR = "remoteaddr";

    /** The node name for the context requested uri. */
    public static final String N_REQUESTEDURI = "requesteduri";

    /** The node name for the job "reuseinstance" value. */
    public static final String N_REUSEINSTANCE = "reuseinstance";

    /** The node name for the scheduler. */
    public static final String N_SCHEDULER = "scheduler";

    /** The node name for the context site root. */
    public static final String N_SITEROOT = "siteroot";

    /** The node name for the context user name. */
    public static final String N_USERNAME = "user";

    /** The list of jobs for the scheduler. */
    private List<CmsScheduledJobInfo> m_configuredJobs;

    /** The configured schedule manager. */
    private CmsScheduleManager m_scheduleManager;

    /**
     * Adds a new job description for the scheduler.<p>
     *
     * @param jobInfo the job description to add
     */
    public void addJobFromConfiguration(CmsScheduledJobInfo jobInfo) {

        m_configuredJobs.add(jobInfo);

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                Messages.get().getBundle().key(
                    Messages.INIT_SCHEDULER_CONFIG_JOB_3,
                    jobInfo.getJobName(),
                    jobInfo.getClassName(),
                    jobInfo.getContextInfo().getUserName()));
        }
    }

    /**
     * Generates the schedule manager.<p>
     */
    public void addScheduleManager() {

        m_scheduleManager = new CmsScheduleManager(m_configuredJobs);
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#addXmlDigesterRules(org.apache.commons.digester3.Digester)
     */
    public void addXmlDigesterRules(Digester digester) {

        // add scheduler creation rule
        digester.addCallMethod("*/" + N_SCHEDULER, "addScheduleManager");

        // add scheduler job creation rule
        digester.addObjectCreate("*/" + N_SCHEDULER + "/" + N_JOB, CmsScheduledJobInfo.class);
        digester.addBeanPropertySetter("*/" + N_SCHEDULER + "/" + N_JOB + "/" + N_NAME, "jobName");
        digester.addBeanPropertySetter("*/" + N_SCHEDULER + "/" + N_JOB + "/" + N_CLASS, "className");
        digester.addBeanPropertySetter("*/" + N_SCHEDULER + "/" + N_JOB + "/" + N_CRONEXPRESSION, "cronExpression");
        digester.addBeanPropertySetter("*/" + N_SCHEDULER + "/" + N_JOB + "/" + N_REUSEINSTANCE, "reuseInstance");
        digester.addBeanPropertySetter("*/" + N_SCHEDULER + "/" + N_JOB + "/" + N_ACTIVE, "active");
        digester.addSetNext("*/" + N_SCHEDULER + "/" + N_JOB, "addJobFromConfiguration");

        // add job context creation rule
        digester.addObjectCreate("*/" + N_SCHEDULER + "/" + N_JOB + "/" + N_CONTEXT, CmsContextInfo.class);
        digester.addBeanPropertySetter(
            "*/" + N_SCHEDULER + "/" + N_JOB + "/" + N_CONTEXT + "/" + N_USERNAME,
            "userName");
        digester.addBeanPropertySetter(
            "*/" + N_SCHEDULER + "/" + N_JOB + "/" + N_CONTEXT + "/" + N_PROJECT,
            "projectName");
        digester.addBeanPropertySetter(
            "*/" + N_SCHEDULER + "/" + N_JOB + "/" + N_CONTEXT + "/" + N_SITEROOT,
            "siteRoot");
        digester.addBeanPropertySetter(
            "*/" + N_SCHEDULER + "/" + N_JOB + "/" + N_CONTEXT + "/" + N_REQUESTEDURI,
            "requestedUri");
        digester.addBeanPropertySetter(
            "*/" + N_SCHEDULER + "/" + N_JOB + "/" + N_CONTEXT + "/" + N_LOCALE,
            "localeName");
        digester.addBeanPropertySetter("*/" + N_SCHEDULER + "/" + N_JOB + "/" + N_CONTEXT + "/" + N_ENCODING);
        digester.addBeanPropertySetter(
            "*/" + N_SCHEDULER + "/" + N_JOB + "/" + N_CONTEXT + "/" + N_REMOTEADDR,
            "remoteAddr");
        digester.addSetNext("*/" + N_SCHEDULER + "/" + N_JOB + "/" + N_CONTEXT, "setContextInfo");

        // add generic parameter rules for jobs, password handler
        digester.addCallMethod(
            "*/" + I_CmsXmlConfiguration.N_PARAM,
            I_CmsConfigurationParameterHandler.ADD_PARAMETER_METHOD,
            2);
        digester.addCallParam("*/" + I_CmsXmlConfiguration.N_PARAM, 0, I_CmsXmlConfiguration.A_NAME);
        digester.addCallParam("*/" + I_CmsXmlConfiguration.N_PARAM, 1);
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#generateXml(org.dom4j.Element)
     */
    public Element generateXml(Element parent) {

        // generate system node and sub notes
        Element schedulerElement = parent.addElement(N_SCHEDULER);
        if (OpenCms.getRunLevel() >= OpenCms.RUNLEVEL_3_SHELL_ACCESS) {
            m_configuredJobs = OpenCms.getScheduleManager().getJobs();
        }

        Iterator<CmsScheduledJobInfo> jobs = m_configuredJobs.iterator();
        while (jobs.hasNext()) {
            CmsScheduledJobInfo jobInfo = jobs.next();
            Element jobElement = schedulerElement.addElement(N_JOB);
            jobElement.addElement(N_NAME).addText(jobInfo.getJobName());
            jobElement.addElement(N_CLASS).addText(jobInfo.getClassName());
            jobElement.addElement(N_REUSEINSTANCE).addText(String.valueOf(jobInfo.isReuseInstance()));
            jobElement.addElement(N_ACTIVE).addText(String.valueOf(jobInfo.isActive()));
            jobElement.addElement(N_CRONEXPRESSION).addCDATA(jobInfo.getCronExpression());
            Element contextElement = jobElement.addElement(N_CONTEXT);
            contextElement.addElement(N_USERNAME).setText(jobInfo.getContextInfo().getUserName());
            contextElement.addElement(N_PROJECT).setText(jobInfo.getContextInfo().getProjectName());
            contextElement.addElement(N_SITEROOT).setText(jobInfo.getContextInfo().getSiteRoot());
            contextElement.addElement(N_REQUESTEDURI).setText(jobInfo.getContextInfo().getRequestedUri());
            contextElement.addElement(N_LOCALE).setText(jobInfo.getContextInfo().getLocaleName());
            contextElement.addElement(N_ENCODING).setText(jobInfo.getContextInfo().getEncoding());
            contextElement.addElement(N_REMOTEADDR).setText(jobInfo.getContextInfo().getRemoteAddr());
            CmsParameterConfiguration jobParameters = jobInfo.getConfiguration();
            if ((jobParameters != null) && (jobParameters.size() > 0)) {
                Element parameterElement = jobElement.addElement(N_PARAMETERS);
                jobParameters.appendToXml(parameterElement);
            }
        }

        return schedulerElement;
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#getDtdFilename()
     */
    public String getDtdFilename() {

        return CONFIGURATION_DTD_NAME;
    }

    /**
     * Returns the configured schedule manager.<p>
     *
     * @return the configured schedule manager
     */
    public CmsScheduleManager getScheduleManager() {

        return m_scheduleManager;
    }

    /**
     * @see org.opencms.configuration.A_CmsXmlConfiguration#initMembers()
     */
    @Override
    protected void initMembers() {

        setXmlFileName(DEFAULT_XML_FILE_NAME);
        m_configuredJobs = new ArrayList<CmsScheduledJobInfo>();
    }

}
