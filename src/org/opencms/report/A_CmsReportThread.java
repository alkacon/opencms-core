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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.report;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.util.CmsUUID;

import java.util.List;
import java.util.Locale;

/**
 * Provides a common Thread class for the reports.<p>
 *
 * @since 6.0.0
 */
public abstract class A_CmsReportThread extends Thread implements I_CmsReportThread {

    /** The OpenCms request context to use. */
    private CmsObject m_cms;

    /** Indicates if the thread was already checked by the grim reaper. */
    private boolean m_doomed;

    /** The id of this report. */
    private CmsUUID m_id;

    /** The report that belongs to the thread. */
    private I_CmsReport m_report;

    /** The time this report is running. */
    private long m_starttime;

    /**
     * Constructs a new report Thread with the given name.<p>
     *
     * @param cms the current OpenCms context object
     * @param name the name of the Thread
     */
    protected A_CmsReportThread(CmsObject cms, String name) {

        super(OpenCms.getThreadStore().getThreadGroup(), name);
        // report Threads are never daemon Threads
        setDaemon(false);
        // the session in the cms context must not be updated when it is used in a report
        m_cms = cms;
        m_cms.getRequestContext().setUpdateSessionEnabled(false);
        // generate the report Thread id
        m_id = new CmsUUID();
        setName(name + " [" + m_id + "]");
        // new Threads are not doomed
        m_doomed = false;
        // set start time
        m_starttime = System.currentTimeMillis();
        // add this Thread to the main Thread store
        OpenCms.getThreadStore().addThread(this);
    }

    /**
     * Adds an error object to the list of errors that occurred during the report.<p>
     *
     * @param obj the error object
     */
    public void addError(Object obj) {

        if (getReport() != null) {
            getReport().addError(obj);
        }
    }

    /**
     * Returns the error exception in case there was an error during the execution of
     * this Thread, null otherwise.<p>
     *
     * @return the error exception in case there was an error, null otherwise
     */
    public Throwable getError() {

        return null;
    }

    /**
     * Returns a list of all errors that occurred during the report.<p>
     *
     * @return an error list that occurred during the report
     */
    public List<Object> getErrors() {

        if (getReport() != null) {
            return getReport().getErrors();
        } else {
            return null;
        }
    }

    /**
     * Returns the time of last report entry.<p>
     *
     * Will return zero if no entry has been written.<p>
     *
     * @return time of last report entry
     */
    public long getLastEntryTime() {

        if (getReport() == null) {
            return 0;
        }
        return getReport().getLastEntryTime();
    }

    /**
     * Returns the logger to which the report output should also be directed.<p>
     *
     * If this returns null, report output is not sent to a logger.
     *
     * @return the logger to which report output should be sent.
     */
    public Object getLogChannel() {

        return null;
    }

    /**
     * Returns the part of the report that is ready for output.<p>
     *
     * @return the part of the report that is ready for output
     */
    public abstract String getReportUpdate();

    /**
     * Returns the time this report has been running.<p>
     *
     * @return the time this report has been running
     */
    public synchronized long getRuntime() {

        if (m_doomed) {
            return m_starttime;
        } else {
            return System.currentTimeMillis() - m_starttime;
        }
    }

    /**
     * Returns the OpenCms UUID of this report thread.<p>
     *
     * @return the OpenCms UUID of this report thread
     */
    public CmsUUID getUUID() {

        return m_id;
    }

    /**
     * Returns if the report generated an error output.<p>
     *
     * @return true if the report generated an error, otherwise false
     */
    public boolean hasError() {

        if (getReport() != null) {
            return (getReport().getErrors().size() > 0);
        } else {
            return false;
        }
    }

    /**
     * Returns true if this thread is already "doomed" to be deleted.<p>
     *
     * A OpenCms deamon Thread (the "Grim Reaper") will collect all
     * doomed Threads, i.e. threads that are not longer active for some
     * time.<p>
     *
     * @return true if this thread is already "doomed" to be deleted
     */
    public synchronized boolean isDoomed() {

        if (isAlive()) {
            // as long as the Thread is still active it is never doomed
            return false;
        }
        if (m_doomed) {
            // not longer active, and already doomed, so rest in peace...
            return true;
        }
        // condemn the Thread to be collected by the grim reaper next time
        m_starttime = getRuntime();
        m_doomed = true;
        return false;
    }

    /**
     * Returns the OpenCms context object this Thread is initialized with.<p>
     *
     * @return the OpenCms context object this Thread is initialized with
     */
    protected CmsObject getCms() {

        return m_cms;
    }

    /**
     * Returns the report where the output of this Thread is written to.<p>
     *
     * @return the report where the output of this Thread is written to
     */
    protected I_CmsReport getReport() {

        return m_report;
    }

    /**
     * Initialize a HTML report for this Thread.<p>
     *
     * @param locale the locale for the report output messages
     */
    protected void initHtmlReport(Locale locale) {

        boolean isVaadin = A_CmsUI.get() != null;
        m_report = isVaadin
        ? new CmsVaadinHtmlReport(locale, m_cms.getRequestContext().getSiteRoot(), getLogChannel())
        : new CmsHtmlReport(locale, m_cms.getRequestContext().getSiteRoot());
    }

    /**
     * Initialize a HTML report for this Thread.<p>
     *
     * This method is reserved for older report threads that still use
     * XML templates to generate their output.<p>
     *
     * @param locale the locale for the report output messages
     */
    protected void initOldHtmlReport(Locale locale) {

        boolean isVaadin = A_CmsUI.get() != null;
        m_report = isVaadin
        ? new CmsVaadinHtmlReport(locale, m_cms.getRequestContext().getSiteRoot(), true, false, getLogChannel())
        : new CmsHtmlReport(locale, m_cms.getRequestContext().getSiteRoot(), true, false);
    }
}
