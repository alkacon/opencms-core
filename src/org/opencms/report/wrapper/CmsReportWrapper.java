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

package org.opencms.report.wrapper;

import org.opencms.i18n.I_CmsMessageBundle;
import org.opencms.report.I_CmsReport;

import java.util.Arrays;
import java.util.Collection;

/**
 * Wrapper for writing reports.
 *
 * It allows to write the same messages to multiple reports and has high-level interface for writing.
 *
 * @author Daniel Seidel
 *
 * @version $Revision: 1.0 $
 *
 * @since 12.0.0
 */
public class CmsReportWrapper {

    /** The reports to write to. */
    private Collection<I_CmsReport> m_reports;

    /** The message bundle to read the messages from. */
    private I_CmsMessageBundle m_messages;

    /**
     * Constructor for the wrapper.
     *
     * @param messages the message bundle to read the printed messages from.
     * @param reports the reports to write to.
     */
    public CmsReportWrapper(I_CmsMessageBundle messages, Collection<I_CmsReport> reports) {

        m_messages = messages;
        m_reports = reports;
    }

    /**
     * Constructor for the wrapper.
     *
     * @param messages the message bundle to read the printed messages from.
     * @param report a sequence of reports to write the messages to.
     */
    public CmsReportWrapper(I_CmsMessageBundle messages, I_CmsReport... report) {

        m_messages = messages;
        m_reports = Arrays.asList(report);
    }

    /**
     * Adds a warning to the report (invisible in the GUI).
     * @param warning the warning to add.
     */
    public void reportAddWarning(Object warning) {

        for (I_CmsReport r : m_reports) {
            r.addWarning(warning);
        }
    }

    /**
     * Print a message in default style.
     * @param message the message
     * @param params the parameters
     */
    public void reportDefault(String message, Object... params) {

        for (I_CmsReport r : m_reports) {
            r.println(m_messages.container(message, params));
        }

    }

    /**
     * Print a message in default style without linebreak.
     * @param message the message
     * @param params the parameters
     */
    public void reportDefaultNoBreak(String message, Object... params) {

        for (I_CmsReport r : m_reports) {
            r.print(m_messages.container(message, params));
        }

    }

    /**
     * Report failed.
     * @param withDots with dots or only the word.
     */
    public void reportFailed(boolean withDots) {

        for (I_CmsReport r : m_reports) {
            r.println(
                DefaultReportMessages.get().container(
                    withDots ? DefaultReportMessages.REPORT_FAILED_0 : DefaultReportMessages.REPORT_FAILED_NO_DOTS_0),
                I_CmsReport.FORMAT_ERROR);
        }
    }

    /**
     * Report failed.
     * @param message the message to print
     * @param params parameters of the message
     */
    public void reportFailed(String message, Object... params) {

        for (I_CmsReport r : m_reports) {
            if (null != message) {
                r.print(m_messages.container(message, params), I_CmsReport.FORMAT_ERROR);
            }
            r.println(
                DefaultReportMessages.get().container(DefaultReportMessages.REPORT_FAILED_0),
                I_CmsReport.FORMAT_ERROR);
        }
    }

    /**
     * Print a message as headline.
     * @param message the message
     * @param params the parameters
     */
    public void reportHeadline(String message, Object... params) {

        for (I_CmsReport r : m_reports) {
            r.println(m_messages.container(message, params), I_CmsReport.FORMAT_HEADLINE);
        }
    }

    /**
     * Print a message as headline.
     * @param message the message
     * @param params the parameters
     */
    public void reportHeadlineNoBreak(String message, Object... params) {

        for (I_CmsReport r : m_reports) {
            r.print(m_messages.container(message, params), I_CmsReport.FORMAT_HEADLINE);
        }
    }

    /**
     * Prints an empty line.
     */
    public void reportNewline() {

        for (I_CmsReport r : m_reports) {
            r.println();
        }
    }

    /**
     * Print a message as note.
     * @param message the message
     * @param params the parameters
     */
    public void reportNote(String message, Object... params) {

        for (I_CmsReport r : m_reports) {
            r.println(m_messages.container(message, params), I_CmsReport.FORMAT_NOTE);
        }

    }

    /**
     * Print a message as note without linebreak.
     * @param message the message
     * @param params the parameters
     */
    public void reportNoteNoBreak(String message, Object... params) {

        for (I_CmsReport r : m_reports) {
            r.print(m_messages.container(message, params), I_CmsReport.FORMAT_NOTE);
        }

    }

    /**
     * Report ok.
     * @param withDots with dots or only the word.
     */
    public void reportOk(boolean withDots) {

        for (I_CmsReport r : m_reports) {
            r.println(
                DefaultReportMessages.get().container(
                    withDots ? DefaultReportMessages.REPORT_OK_0 : DefaultReportMessages.REPORT_OK_NO_DOTS_0),
                I_CmsReport.FORMAT_OK);
        }
    }

    /**
     * Report ok.
     * @param message the message to print
     * @param params parameters of the message
     */
    public void reportOk(String message, Object... params) {

        for (I_CmsReport r : m_reports) {
            if (null != message) {
                r.print(m_messages.container(message, params), I_CmsReport.FORMAT_OK);
            }
            r.println(DefaultReportMessages.get().container(DefaultReportMessages.REPORT_OK_0), I_CmsReport.FORMAT_OK);
        }
    }

    /**
     * Report ok.
     * @param withDots with dots or only the word.
     */
    public void reportSkipped(boolean withDots) {

        for (I_CmsReport r : m_reports) {
            r.println(
                DefaultReportMessages.get().container(
                    withDots ? DefaultReportMessages.REPORT_SKIPPED_0 : DefaultReportMessages.REPORT_SKIPPED_NO_DOTS_0),
                I_CmsReport.FORMAT_WARNING);
        }
    }

    /**
     * Report skipped.
     * @param message the message to print
     * @param params parameters of the message
     */
    public void reportSkipped(String message, Object... params) {

        for (I_CmsReport r : m_reports) {
            if (null != message) {
                r.print(m_messages.container(message, params), I_CmsReport.FORMAT_WARNING);
            }
            r.println(
                DefaultReportMessages.get().container(DefaultReportMessages.REPORT_SKIPPED_0),
                I_CmsReport.FORMAT_WARNING);
        }

    }

    /**
     * Print a message in warning style.
     * @param message the message
     * @param params the parameters
     */
    public void reportWarning(String message, Object... params) {

        for (I_CmsReport r : m_reports) {
            r.println(m_messages.container(message, params), I_CmsReport.FORMAT_WARNING);
        }
    }

    /**
     * Print a message in warning style.
     * @param message the message
     * @param params the parameters
     */
    public void reportWarningNoBreak(String message, Object... params) {

        for (I_CmsReport r : m_reports) {
            r.println(m_messages.container(message, params), I_CmsReport.FORMAT_WARNING);
        }
    }
}
