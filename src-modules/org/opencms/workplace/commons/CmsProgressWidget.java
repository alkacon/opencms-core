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

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsIllegalStateException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.list.A_CmsListDialog;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * This is a widget to be used in a dialog which should show a progress bar based
 * on a list.<p>
 *
 * The progress bar uses Ajax to not reload the whole page. The code which runs
 * inside the thread has to update the progress in the current thread.<p>
 *
 * The progress to be displayed is the progress of building large lists which can
 * take some time until they are finished.<p>
 *
 * There is a progress bar shown with the percentages on the left. Additionaly it
 * is possible to show a description above the progress bar.<p>
 *
 * @see A_CmsListDialog
 *
 * @since 7.0.0
 */
public class CmsProgressWidget {

    /** The name of the key request parameter. */
    public static final String PARAMETER_KEY = "progresskey";

    /** The name of the refresh rate request parameter. */
    public static final String PARAMETER_REFRESHRATE = "refreshrate";

    /** The name of the show wait time request parameter. */
    public static final String PARAMETER_SHOWWAITTIME = "showwaittime";

    /** The time period after finished thread will be removed (10 min). */
    private static final long CLEANUP_PERIOD = 10 * 60 * 1000;

    /** The default width of the progress bar. */
    private static final String DEFAULT_COLOR = "blue";

    /** The default refresh rate (in ms) of the progress bar. */
    private static final int DEFAULT_REFRESH_RATE = 2000;

    /** The default width of the progress bar. */
    private static final String DEFAULT_WIDTH = "200px";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsProgressWidget.class);

    /** Map of running threads. */
    private static Map<String, CmsProgressThread> m_threads = new HashMap<String, CmsProgressThread>();

    /** The color of the progress bar. */
    private String m_color;

    /** The name of the JavaScript method to call after progress is finished. */
    private String m_jsFinishMethod;

    /** The current JSP action element. */
    private CmsJspActionElement m_jsp;

    /** The unique key of the thread belonging to this widget. */
    private String m_key;

    /** The time interval the progress gets refreshed (in ms). */
    private int m_refreshRate;

    /** The time period the show the wait symbol before the progress bar is shown.<p>
     *  Set to 0 (zero) to disable this.<p> */
    private int m_showWaitTime;

    /** The width of the progress bar to use in HTML.<p> */
    private String m_width;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsProgressWidget(CmsJspActionElement jsp) {

        m_jsp = jsp;

        // set default values
        m_width = DEFAULT_WIDTH;
        m_color = DEFAULT_COLOR;

        // find the show wait time from the request
        m_showWaitTime = 0;
        if (getJsp().getRequest().getParameter(PARAMETER_SHOWWAITTIME) != null) {
            m_showWaitTime = Integer.valueOf(getJsp().getRequest().getParameter(PARAMETER_SHOWWAITTIME)).intValue();
        }

        // find the show wait time from the request
        m_refreshRate = DEFAULT_REFRESH_RATE;
        if (getJsp().getRequest().getParameter(PARAMETER_REFRESHRATE) != null) {
            m_refreshRate = Integer.valueOf(getJsp().getRequest().getParameter(PARAMETER_REFRESHRATE)).intValue();
        }

        // find the key from the request
        m_key = getJsp().getRequest().getParameter(PARAMETER_KEY);
        if (m_key == null) {
            // generate unique key
            m_key = new CmsUUID().toString();
        }
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsProgressWidget(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Returns the thread for the progress with the given key.<p>
     *
     * @param key the key of the thread
     *
     * @return the thread for the progress with the given key
     */
    public static CmsProgressThread getProgressThread(String key) {

        return m_threads.get(key);
    }

    /**
     * Removes the thread for the progress with the given key from the list with the actual threads.<p>
     *
     * @param key the key of the thread for the progress to remove from the list
     */
    public static void removeProgressThread(String key) {

        m_threads.remove(key);
    }

    /**
     * Returns the actual progress in percent.<p>
     *
     * The return value depends on the state of the progress/thread. This can be
     * <ul>
     * <li>the actual progress in percent with an optional description.</li>
     * <li>the result as the html code for the list.</li>
     * <li>an error message.</li>
     * </ul><p>
     *
     * The result will be interpreted by the JavaScript method "updateProgressbar()".<p>
     *
     * @return the actual progress as a String
     */
    public String getActualProgress() {

        try {
            CmsProgressThread thread;
            if (getProgressThread(getKey()) != null) {
                thread = m_threads.get(getKey());

                if (thread.isAlive()) {
                    // wait the configured time until to update the progress the first time
                    if (thread.getRuntime() < getShowWaitTime()) {
                        while ((thread.getRuntime() < getShowWaitTime()) && (thread.isAlive())) {
                            synchronized (this) {
                                wait(500);
                            }
                        }
                    } else {
                        // wait the configured refresh rate before returning
                        synchronized (this) {
                            wait(getRefreshRate());
                        }
                    }
                }

                if (!thread.isAlive()) {
                    // is an error occurred in the execution of the thread?
                    if (thread.getError() != null) {
                        return createError(
                            Messages.get().getBundle(getJsp().getRequestContext().getLocale()).key(
                                Messages.GUI_PROGRESS_ERROR_IN_THREAD_0),
                            thread.getError());
                    }

                    // return the result of the list created in the progress
                    return thread.getResult();
                }

                // create and return the actual progress in percent with the description to be shown
                StringBuffer result = new StringBuffer();

                result.append("PRO");
                result.append(thread.getProgress());
                result.append("%");
                result.append("|");
                result.append(thread.getDescription());

                return result.toString();
            } else {
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(Messages.LOG_PROGRESS_THREAD_NOT_FOUND_1, getKey()));
                }
                return createError(Messages.get().getBundle(getJsp().getRequestContext().getLocale()).key(
                    Messages.GUI_PROGRESS_THREAD_NOT_FOUND_1,
                    getKey()));
            }

        } catch (Throwable t) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_PROGRESS_ERROR_CALC_PROGRESS_0), t);
            }
            return createError(
                Messages.get().getBundle(getJsp().getRequestContext().getLocale()).key(
                    Messages.GUI_PROGRESS_ERROR_CALCULATING_0),
                t);
        }
    }

    /**
     * Returns the color of the progress bar.<p>
     *
     * @return the color of the progress bar
     */
    public String getColor() {

        return m_color;
    }

    /**
     * Returns the name of the JavaScript method to call after progress is finished.<p>
     *
     * @return the name of the JavaScript method to call after progress is finished
     */
    public String getJsFinishMethod() {

        return m_jsFinishMethod;
    }

    /**
     * Generates the necessary JavaScript inclusion code for this widget.<p>
     *
     * @return the JavaScript inclusion code
     */
    public String getJsIncludes() {

        StringBuffer result = new StringBuffer();

        result.append("<script type=\"text/javascript\" src=\"");
        result.append(CmsWorkplace.getSkinUri());
        result.append("commons/ajax.js\"></script>\n");

        result.append("<script type=\"text/javascript\">\n");

        // initialize global variables
        result.append("\tvar progressState = 0;\n");
        result.append("\tvar progressResult = '';\n");

        // function update progress bar
        result.append("\tfunction updateProgressBar(msg, state) {\n");

        // check state of progress
        result.append("\t\tif (progressState != 1) {\n");
        result.append("\t\t\tprogressState = 0;\n");
        result.append("\t\t\treturn;\n");
        result.append("\t\t}\n");

        result.append("\t\tif (state == 'ok') {\n");

        // get all elements to use
        result.append("\t\t\tvar bar = document.getElementById(\"progressbar_bar\");\n");
        result.append("\t\t\tvar percent = document.getElementById(\"progressbar_percent\");\n");
        result.append("\t\t\tvar wait = document.getElementById(\"progressbar_wait\");\n");
        result.append("\t\t\tvar desc = document.getElementById(\"progressbar_desc\");\n");

        result.append("\t\t\tif (msg != \"\") {\n");

        result.append("\t\t\t\tbar.parentNode.style.display = \"block\";\n");
        result.append("\t\t\t\tpercent.style.display = \"inline\";\n");
        result.append("\t\t\t\twait.style.display = \"none\";\n");
        result.append("\t\t\t\tdesc.style.display = \"block\";\n");

        // update progress
        result.append("\t\t\t\tif (msg.substring(0,3) == \"PRO\") {\n");
        result.append("\t\t\t\t\tvar splitted = msg.split(\"|\");\n");
        result.append("\t\t\t\t\tbar.style.width = splitted[0].substr(3);\n");
        result.append("\t\t\t\t\tpercent.innerHTML = splitted[0].substr(3);\n");
        result.append("\t\t\t\t\tdesc.innerHTML = splitted[1];\n");
        result.append("\t\t\t\t\tmakeRequest('");
        result.append(getJsp().link("/system/workplace/commons/report-progress.jsp"));
        result.append("', '");
        result.append(PARAMETER_KEY);
        result.append("=");
        result.append(getKey());
        result.append("&");
        result.append(PARAMETER_SHOWWAITTIME);
        result.append("=");
        result.append(getShowWaitTime());
        result.append("&");
        result.append(PARAMETER_REFRESHRATE);
        result.append("=");
        result.append(getRefreshRate());
        result.append("', 'updateProgressBar');\n");

        // set error message
        result.append("\t\t\t\t} else if (msg.substring(0,3) == \"ERR\") {\n");
        result.append("\t\t\t\t\tsetProgressBarError(msg.substr(3));\n");

        // set result
        result.append("\t\t\t\t} else {\n");
        result.append("\t\t\t\t\tprogressState = 0;\n");
        result.append("\t\t\t\t\tbar.style.width = \"100%\";\n");
        result.append("\t\t\t\t\tpercent.innerHTML = \"100%\";\n");
        result.append("\t\t\t\t\tprogressResult = msg;\n");

        result.append("\t\t\t\t\tbar.parentNode.style.display = \"none\";\n");
        result.append("\t\t\t\t\tpercent.style.display = \"none\";\n");
        result.append("\t\t\t\t\tdesc.style.display = \"none\";\n");
        result.append("\t\t\t\t\twait.style.display = \"block\";\n");

        result.append("\t\t\t\t\twindow.setTimeout(\"");
        result.append(getJsFinishMethod());
        result.append("()\",100);\n");
        result.append("\t\t\t\t}\n");
        result.append("\t\t\t} else {\n");
        result.append("\t\t\t\tbar.style.width = \"100%\";\n");
        result.append("\t\t\t}\n");

        // fatal error returned by ajax
        result.append("\t\t} else if (state == 'fatal') {\n");
        result.append("\t\t\tprogressState = 0;\n");
        result.append("\t\t\tsetProgressBarError(\"");
        result.append(
            org.opencms.workplace.Messages.get().getBundle(getJsp().getRequestContext().getLocale()).key(
                org.opencms.workplace.Messages.GUI_AJAX_REPORT_GIVEUP_0));
        result.append("\");\n");

        // error returned by ajax
        result.append("\t\t} else if (state == 'error') {\n");
        result.append("\t\t\tprogressState = 0;\n");
        result.append("\t\t\tsetProgressBarError(\"");
        result.append(
            org.opencms.workplace.Messages.get().getBundle(getJsp().getRequestContext().getLocale()).key(
                org.opencms.workplace.Messages.GUI_AJAX_REPORT_ERROR_0));
        result.append(" \" + msg);\n");

        // wait returned by ajax -> display wait symbol
        result.append("\t\t} else if (state == 'wait') {\n");
        result.append("\t\t\tbar.parentNode.style.display = \"none\";\n");
        result.append("\t\t\tpercent.style.display = \"none\";\n");
        result.append("\t\t\twait.style.display = \"block\";\n");
        result.append("\t\t}\n");
        result.append("\t}\n");

        // function set error
        result.append("\tfunction setProgressBarError(msg) {\n");
        result.append("\t\tvar error = document.getElementById(\"progressbar_error\");\n");
        result.append("\t\tvar bar = document.getElementById(\"progressbar_bar\");\n");
        result.append("\t\tvar percent = document.getElementById(\"progressbar_percent\");\n");
        result.append("\t\tvar desc = document.getElementById(\"progressbar_desc\");\n");

        result.append("\t\terror.innerHTML = msg;\n");
        result.append("\t\terror.style.display = \"block\";\n");

        result.append("\t\tbar.style.display = \"none\";\n");
        result.append("\t\tpercent.style.display = \"none\";\n");
        result.append("\t\tdesc.style.display = \"none\";\n");

        result.append("\t}\n");

        // function reset progress bar
        result.append("\tfunction resetProgressBar() {\n");
        result.append("\t\tvar bar = document.getElementById(\"progressbar_bar\");\n");
        result.append("\t\tbar.parentNode.style.display = \"inline\";\n");
        result.append("\t\tbar.style.width = \"0%\";\n");
        result.append("\t\tbar.style.display = \"block\";\n");

        result.append("\t\tvar percent = document.getElementById(\"progressbar_percent\");\n");
        result.append("\t\tpercent.innerHTML = \"0%\";\n");
        result.append("\t\tpercent.style.display = \"inline\";\n");

        result.append("\t\tvar error = document.getElementById(\"progressbar_error\");\n");
        result.append("\t\terror.innerHTML = \"\";\n");
        result.append("\t\terror.style.display = \"none\";\n");

        result.append("\t\tvar wait = document.getElementById(\"progressbar_wait\");\n");
        result.append("\t\twait.style.display = \"none\";\n");

        result.append("\t\tvar desc = document.getElementById(\"progressbar_desc\");\n");
        result.append("\t\tdesc.style.display = \"block\";\n");
        result.append("\t\tdesc.innerHTML = \"\";\n");

        result.append("\t\t\t\t\tprogressResult = \"\";\n");

        result.append("\t}\n");

        // function start progress bar
        result.append("\tfunction startProgressBar() {\n");
        result.append("\t\tif (progressState > 0) {\n");
        result.append("\t\t\tprogressState = 2;\n");
        result.append("\t\t\twindow.setTimeout(\"startProgressBar()\",");
        result.append(getRefreshRate());
        result.append(");\n");
        result.append("\t\t\treturn;\n");
        result.append("\t\t}\n");
        result.append("\t\tprogressState = 1;\n");
        result.append("\t\tmakeRequest('");
        result.append(getJsp().link("/system/workplace/commons/report-progress.jsp"));
        result.append("', '");
        result.append(PARAMETER_KEY);
        result.append("=");
        result.append(getKey());
        result.append("&");
        result.append(PARAMETER_SHOWWAITTIME);
        result.append("=");
        result.append(getShowWaitTime());
        result.append("&");
        result.append(PARAMETER_REFRESHRATE);
        result.append("=");
        result.append(getRefreshRate());
        result.append("', 'updateProgressBar');\n");
        result.append("\t}\n");

        result.append("</script>\n");

        return result.toString();
    }

    /**
     * Returns the current JSP action element.<p>
     *
     * @return the the current JSP action element
     */
    public CmsJspActionElement getJsp() {

        return m_jsp;
    }

    /**
     * Returns the unique key of the thread belonging to this widget.<p>
     *
     * @return the unique key of the thread belonging to this widget
     */
    public String getKey() {

        return m_key;
    }

    /**
     * Returns the refresh rate in ms of the progress bar.<p>
     *
     * @return the refresh rate in ms of the progress bar
     */
    public int getRefreshRate() {

        return m_refreshRate;
    }

    /**
     * Returns the time period the show the wait symbol before the progress bar is shown.<p>
     *
     * @return the time period the show the wait symbol before the progress bar is shown
     */
    public int getShowWaitTime() {

        return m_showWaitTime;
    }

    /**
     * Generates the widget HTML for the progress bar.<p>
     *
     * @return the widget HTML for the progress bar
     */
    public String getWidget() {

        StringBuffer result = new StringBuffer();

        CmsProgressThread thread = getProgressThread(getKey());

        // if the thread is finished before the widget is rendered
        // show directly the result
        if ((thread != null) && (!thread.isAlive())) {
            result.append("<script type=\"text/javascript\">\n");
            result.append("\tprogressState = 0;\n");
            result.append("\tprogressResult = '");
            result.append(CmsStringUtil.escapeJavaScript(getActualProgress()));
            result.append("';\n");
            result.append("\t");
            result.append(getJsFinishMethod());
            result.append("();\n");
            result.append("</script>\n");
        } else {
            // check if to show the wait symbol
            boolean showWait = false;
            if (getShowWaitTime() > 0) {
                // show if the thread is running and the time running is smaller than the configured
                if ((thread != null) && (thread.isAlive()) && (thread.getRuntime() < getShowWaitTime())) {
                    showWait = true;
                } else if ((thread == null) && (getShowWaitTime() > 0)) {
                    // show if there is no thread
                    showWait = true;
                }
            }

            result.append("<div id=\"progressbar_desc\" style=\"margin-bottom:5px;display:");
            result.append(showWait ? "none" : "block");
            result.append("\"></div>");

            result.append("<div style=\"width:");
            result.append(getWidth());
            result.append(";border-width:1px;border-style:solid;padding:0px;margin:0px;float:left;display:");
            result.append(showWait ? "none" : "inline");
            result.append(";\">\n");
            result.append("\t<div id=\"progressbar_bar\" style=\"width:0%;background-color:");
            result.append(getColor());
            result.append(";\">&nbsp;</div>\n");
            result.append("</div>\n");
            result.append("&nbsp;");
            result.append("<div id=\"progressbar_percent\" style=\"display:");
            result.append(showWait ? "none" : "inline");
            result.append(";\" >0%</div>\n");

            result.append(
                "<div id=\"progressbar_error\" style=\"display:none;color:#B40000;font-weight:bold;\"></div>\n");

            result.append("<div id=\"progressbar_wait\" style=\"display:");
            result.append(showWait ? "block" : "none");
            result.append(";color:#000099;font-weight:bold;\"><img src=\"");
            result.append(CmsWorkplace.getSkinUri());
            result.append("commons/wait.gif\" width='32' height='32' alt='' align='absmiddle' />");
            result.append(
                org.opencms.workplace.Messages.get().getBundle(getJsp().getRequestContext().getLocale()).key(
                    org.opencms.workplace.Messages.GUI_AJAX_REPORT_WAIT_0));
            result.append("</div>\n");

            result.append("<script type=\"text/javascript\">\n");
            result.append("\tstartProgressBar();\n");
            result.append("</script>\n");
        }

        return result.toString();
    }

    /**
     * Returns the width of the progress bar.<p>
     *
     * @return the width of the progress bar
     */
    public String getWidth() {

        return m_width;
    }

    /**
     * Sets the color of the progress bar.<p>
     *
     * @param color the color of the progress bar to set
     */
    public void setColor(String color) {

        m_color = color;
    }

    /**
     * Sets the name of the JavaScript method to call after progress is finished.<p>
     *
     * @param jsFinishMethod the name of the JavaScript method to call after progress is finished to set
     */
    public void setJsFinishMethod(String jsFinishMethod) {

        m_jsFinishMethod = jsFinishMethod;
    }

    /**
     * Sets the refresh rate in ms of the progress bar.<p>
     *
     * @param refreshRate the refresh rate in ms of the progress bar to set
     */
    public void setRefreshRate(int refreshRate) {

        m_refreshRate = refreshRate;
    }

    /**
     * Sets the time period the show the wait symbol before the progress bar is shown.<p>
     *
     * @param showWaitTime the time period the show the wait symbol before the progress bar is shown to set
     */
    public void setShowWaitTime(int showWaitTime) {

        m_showWaitTime = showWaitTime;
    }

    /**
     * Sets the width of the progress bar.<p>
     *
     * @param width the width of the progress bar to set
     */
    public void setWidth(String width) {

        m_width = width;
    }

    /**
     * Starts a thread for the progress on the given list.<p>
     *
     * @param list the list to use for the progress bar
     */
    public void startProgress(A_CmsListDialog list) {

        startProgress(list, false);
    }

    /**
     * Starts a thread for the progress on the given list.<p>
     *
     * @param list the list to use for the progress bar
     * @param abortExisting if true then an already existing thread will be killed
     */
    public void startProgress(A_CmsListDialog list, boolean abortExisting) {

        // check the list
        if (list == null) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_PROGRESS_START_INVALID_LIST_0));
        }

        // check if created key already exists
        if (m_threads.get(getKey()) != null) {
            if (abortExisting) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_PROGRESS_INTERRUPT_THREAD_1, getKey()));
                }
                Thread thread = m_threads.get(getKey());
                thread.interrupt();
            } else {
                throw new CmsIllegalStateException(
                    Messages.get().container(Messages.ERR_PROGRESS_START_THREAD_EXISTS_0));
            }
        }

        // create the thread
        CmsProgressThread thread = new CmsProgressThread(list, getKey(), list.getLocale());

        Map<String, CmsProgressThread> threadsAbandoned = new HashMap<String, CmsProgressThread>();
        Map<String, CmsProgressThread> threadsAlive = new HashMap<String, CmsProgressThread>();
        synchronized (m_threads) {

            // clean up abandoned threads
            for (Iterator<Map.Entry<String, CmsProgressThread>> iter = m_threads.entrySet().iterator(); iter.hasNext();) {
                Map.Entry<String, CmsProgressThread> entry = iter.next();
                CmsProgressThread value = entry.getValue();

                if ((!value.isAlive()) && ((System.currentTimeMillis() - value.getFinishTime()) > CLEANUP_PERIOD)) {
                    threadsAbandoned.put(entry.getKey(), value);
                } else {
                    threadsAlive.put(entry.getKey(), value);
                }
            }

            // add and start new thread
            threadsAlive.put(thread.getKey(), thread);
            thread.start();

            m_threads = threadsAlive;
        }

        if (LOG.isDebugEnabled()) {
            for (Iterator<String> iter = threadsAbandoned.keySet().iterator(); iter.hasNext();) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_PROGRESS_CLEAN_UP_THREAD_1, iter.next()));
            }
        }
    }

    /**
     * Creates the html code for the given error message.<p>
     *
     * @param errorMsg the error message to place in the html code
     *
     * @return the html code for the error message
     */
    private String createError(String errorMsg) {

        StringBuffer result = new StringBuffer();

        result.append("ERR");
        result.append(errorMsg);

        return result.toString();
    }

    /**
     * Creates the html code for the given error message and the
     * provided Exception.<p>
     *
     * @param errorMsg the error message to place in the html code
     * @param t the exception to add to the error message
     *
     * @return the html code for the error message
     */
    private String createError(String errorMsg, Throwable t) {

        StringBuffer msg = new StringBuffer();
        msg.append(errorMsg);
        msg.append("\n");
        msg.append(t.getMessage());
        msg.append("\n");
        msg.append(CmsException.getStackTraceAsString(t));

        return createError(msg.toString());
    }

}
