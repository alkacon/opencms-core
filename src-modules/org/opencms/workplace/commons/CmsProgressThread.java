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

package org.opencms.workplace.commons;

import org.opencms.main.CmsLog;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsHtmlList;
import org.opencms.workplace.list.CmsListExplorerColumn;
import org.opencms.workplace.threads.A_CmsProgressThread;

import java.util.Locale;

import org.apache.commons.logging.Log;

/**
 * This class is used to build a list ({@link A_CmsListDialog}) in the background
 * and to show the progress of building to the user.<p>
 *
 * To work correctly the operations while building the list have to update the
 * actual progress.<p>
 *
 * @since 7.0.0
 */
public class CmsProgressThread extends A_CmsProgressThread {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsProgressThread.class);

    /** The description to show for the progress. */
    private String m_description;

    /** Stores the error occurred while the thread was running. */
    private Throwable m_error;

    /** The time the thread has finished. */
    private long m_finishtime;

    /** The key of this thread. */
    private String m_key;

    /** The list the thread displays the progress of building it. */
    private A_CmsListDialog m_list;

    /** The locale to use for this thread. */
    private Locale m_locale;

    /** The actual progress of the thread. */
    private int m_progress;

    /** The finished result as HTML of the list. */
    private String m_result;

    /** The time the thread has started. */
    private long m_starttime;

    /**
     * Constructs a new progress thread with the given name.<p>
     *
     * @param list the list to use for the progress to display
     * @param key the key of the thread
     * @param locale the locale to use for this thread
     */
    public CmsProgressThread(A_CmsListDialog list, String key, Locale locale) {

        m_progress = 0;
        m_list = list;
        m_key = key;
        m_starttime = 0;
        m_finishtime = 0;
        m_locale = locale;

        setName(Messages.get().getBundle().key(Messages.GUI_PROGRESS_THREAD_NAME_1, key));
    }

    /**
     * Returns the description to show for the progress.<p>
     *
     * @return the description to show for the progress
     */
    public String getDescription() {

        return m_description;
    }

    /**
     * Returns the error exception in case there was an error during the execution of
     * this thread, null otherwise.<p>
     *
     * @return the error exception in case there was an error, null otherwise
     */
    public Throwable getError() {

        return m_error;
    }

    /**
     * Returns the time the thread was finished.<p>
     *
     * @return the time the thread was finished
     */
    public long getFinishTime() {

        return m_finishtime;
    }

    /**
     * Returns the key of this thread.<p>
     *
     * @return the key of this thread
     */
    public String getKey() {

        return m_key;
    }

    /**
     * Returns the list of the progress bar.<p>
     *
     * @return the list of the progress bar
     */
    public A_CmsListDialog getList() {

        return m_list;
    }

    /**
     * Returns the locale to use for this thread.<p>
     *
     * @return the locale to use for this thread
     */
    public Locale getLocale() {

        return m_locale;
    }

    /**
     * Returns the progress.<p>
     *
     * @return the progress
     */
    public int getProgress() {

        return m_progress;
    }

    /**
     * Returns the finished result as HTML of the list.<p>
     *
     * @return the finished result as HTML of the list
     */
    public String getResult() {

        return m_result;
    }

    /**
     * Returns the time this report has been running.<p>
     *
     * @return the time this report has been running
     */
    public synchronized long getRuntime() {

        if (!isAlive()) {
            return m_finishtime - m_starttime;
        } else if (m_starttime > 0) {
            return System.currentTimeMillis() - m_starttime;
        } else {
            return 0;
        }
    }

    /**
     * Returns the time the thread has started.<p>
     *
     * @return the time the thread has started
     */
    public long getStartTime() {

        return m_starttime;
    }

    /**
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {

        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_PROGRESS_START_THREAD_1, getKey()));
            }
            m_starttime = System.currentTimeMillis();

            // calculate size of the list
            m_list.refreshList();

            // create the list
            CmsHtmlList list = m_list.getList();
            list.setBoxed(false);

            StringBuffer result = new StringBuffer();

            result.append("<input type='hidden' name='result' value='");
            result.append(list.getTotalSize()).append("'>\n");
            result.append(CmsListExplorerColumn.getExplorerStyleDef());
            result.append("<div style='height:200px; overflow: auto;'>\n");
            result.append(list.listHtml());
            result.append("</div>\n");

            m_result = result.toString();
        } catch (Throwable t) {
            m_error = t;

            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_ERROR_EXECUTING_THREAD_0), t);
            }
        } finally {
            m_finishtime = System.currentTimeMillis();
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_PROGRESS_FINISHED_THREAD_1, getKey()));
            }
        }

    }

    /**
     * Sets the description to show for the progress.<p>
     *
     * @param description the description to show for the progress to set
     */
    public void setDescription(String description) {

        m_description = description;
    }

    /**
     * Sets the locale to use for this thread.<p>
     *
     * @param locale the locale to use for this thread to set
     */
    public void setLocale(Locale locale) {

        m_locale = locale;
    }

    /**
     * Sets the progress.<p>
     *
     * @param progress the progress to set
     */
    public void setProgress(int progress) {

        m_progress = progress;
    }

}
