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

import org.opencms.main.CmsLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.logging.Log;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateErrorListener;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;

/**
 * Report update formatter for the new Vaadin-based workplace.<p>
 */
public class CmsVaadinHtmlReportUpdateFormatter implements I_CmsReportUpdateFormatter {

    /** The logger instance used for this class. */
    private static final Log LOG = CmsLog.getLog(CmsVaadinHtmlReportUpdateFormatter.class);

    /** The StringTemplate group used by this report. */
    private StringTemplateGroup m_templateGroup;

    /**
     * Creates a new instance.<p>
     */
    public CmsVaadinHtmlReportUpdateFormatter() {
        try (InputStream stream = CmsVaadinHtmlReportUpdateFormatter.class.getResourceAsStream("report.st")) {
            try {
                m_templateGroup = new StringTemplateGroup(
                    new InputStreamReader(stream, "UTF-8"),
                    DefaultTemplateLexer.class,
                    new StringTemplateErrorListener() {

                        @SuppressWarnings("synthetic-access")
                        public void error(String arg0, Throwable arg1) {

                            LOG.error(arg0 + ": " + arg1.getMessage(), arg1);
                        }

                        @SuppressWarnings("synthetic-access")
                        public void warning(String arg0) {

                            LOG.warn(arg0);

                        }
                    });
            } catch (UnsupportedEncodingException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * @see org.opencms.report.I_CmsReportUpdateFormatter#formatReportUpdate(java.util.List)
     */
    public String formatReportUpdate(List<CmsReportUpdateItem> updateItem) {

        StringBuffer buffer = new StringBuffer();
        for (CmsReportUpdateItem entry : updateItem) {
            try {
                StringTemplate template = m_templateGroup.getInstanceOf(entry.getType().getFormatName());
                boolean needsParam = template.getFormalArguments().get("message") != null;
                if (needsParam) {
                    template.setAttribute("message", entry.getMessage());
                }
                buffer.append(template.toString());
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return buffer.toString();
    }

}
