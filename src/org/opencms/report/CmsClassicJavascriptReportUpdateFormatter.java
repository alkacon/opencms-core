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

package org.opencms.report;

import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;

import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;

/**
 * Report update formatter for the Javascript-based report update format, which was used in the old workplace most of the time.<p>
 */
public class CmsClassicJavascriptReportUpdateFormatter implements I_CmsReportUpdateFormatter {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsClassicJavascriptReportUpdateFormatter.class);

    /** The locale. */
    private Locale m_locale;

    /**
     * Creates a new instance.<p>
     *
     * @param locale the locale
     */
    public CmsClassicJavascriptReportUpdateFormatter(Locale locale) {
        m_locale = locale;
    }

    /**
     * @see org.opencms.report.I_CmsReportUpdateFormatter#formatReportUpdate(java.util.List)
     */
    public String formatReportUpdate(List<CmsReportUpdateItem> updateItem) {

        StringBuffer buffer = new StringBuffer();
        for (CmsReportUpdateItem item : updateItem) {
            try {
                buffer.append(formatItem(item));
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return buffer.toString();
    }

    /**
     * Generates the formatted output for an exception.<P>
     *
     * @param throwable the exception
     *
     * @return the formatted output
     */
    private String formatException(Throwable throwable) {

        StringBuffer buf = new StringBuffer();
        buf.append("aT('");
        buf.append(Messages.get().getBundle(m_locale).key(Messages.RPT_EXCEPTION_0));
        String exception = CmsEncoder.escapeXml(CmsException.getStackTraceAsString(throwable));
        StringBuffer excBuffer = new StringBuffer(exception.length() + 50);
        StringTokenizer tok = new StringTokenizer(exception, "\r\n");
        while (tok.hasMoreTokens()) {
            excBuffer.append(tok.nextToken());
            excBuffer.append(CmsHtmlReport.LINEBREAK);
        }
        buf.append(CmsStringUtil.escapeJavaScript(excBuffer.toString()));
        buf.append("'); ");
        return buf.toString();
    }

    /**
     * Formats a single report item.<p>
     *
     * @param updateItem the report item
     *
     * @return the formatted output
     */
    private String formatItem(CmsReportUpdateItem updateItem) {

        StringBuffer buf = new StringBuffer();
        CmsReportFormatType format = updateItem.getType();
        if (format == CmsReportFormatType.fmtException) {
            return formatException((Throwable)(updateItem.getMessage()));
        } else {
            String value = CmsStringUtil.escapeJavaScript((String)(updateItem.getMessage()));
            switch (format) {
                case fmtHeadline:
                    buf = new StringBuffer();
                    buf.append("aH('");
                    buf.append(value);
                    buf.append("'); ");
                    break;
                case fmtWarning:
                    buf = new StringBuffer();
                    buf.append("aW('");
                    buf.append(value);
                    buf.append("'); ");
                    break;
                case fmtError:
                    buf = new StringBuffer();
                    buf.append("aE('");
                    buf.append(value);
                    buf.append("'); ");
                    break;
                case fmtNote:
                    buf = new StringBuffer();
                    buf.append("aN('");
                    buf.append(value);
                    buf.append("'); ");
                    break;
                case fmtOk:
                    buf = new StringBuffer();
                    buf.append("aO('");
                    buf.append(value);
                    buf.append("'); ");
                    break;
                case fmtNewline:
                    buf = new StringBuffer();
                    buf.append("a('" + CmsHtmlReport.LINEBREAK + "');");
                    break;
                case fmtDefault:
                default:
                    buf = new StringBuffer();
                    buf.append("a('");
                    buf.append(value);
                    buf.append("'); ");
            }

            // the output lines get split back into single lines on the client-side.
            // thus, a separate JavaScript call has to be added here to tell the
            // client that we want a linebreak here...
            if (value.trim().endsWith(CmsHtmlReport.LINEBREAK)) {
                buf.append("aB(); ");
            }
        }
        return buf.toString();
    }
}
