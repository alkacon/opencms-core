/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/content/languagecopy/CmsLanguageCopyReport.java,v $
 * Date   : $Date: 2011/03/23 14:50:24 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.tools.content.languagecopy;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.report.I_CmsReportThread;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.list.A_CmsListReport;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Provides a report for copying XML content language nodes from source language to target language.
 * <p>
 * 
 * @author Achim Westermann
 * @author Mario Jaeger
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 7.5.1
 */
public class CmsLanguageCopyReport extends A_CmsListReport {

    /** The resources to copy. */
    private String m_copyresources;

    /** The source language. */
    private String m_sourcelanguage;

    /** The source language. */
    private String m_targetlanguage;

    /**
     * Public constructor with JSP action element.
     * <p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsLanguageCopyReport(final CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.
     * <p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsLanguageCopyReport(final PageContext context, final HttpServletRequest req, final HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListReport#initializeThread()
     */
    @Override
    public I_CmsReportThread initializeThread() {

        I_CmsReportThread exportThread = new CmsLanguageCopyThread(getCms(), CmsStringUtil.splitAsArray(
            m_copyresources,
            ","), m_sourcelanguage, m_targetlanguage);

        return exportThread;
    }

    /**
     * Sets the resources to copy.<p>
     * 
     * @param resources the resources to copy
     */
    public void setParamCopyresources(String resources) {

        m_copyresources = resources;
    }

    /**
     * Sets the source language.<p>
     * 
     * @param language the source language
     */
    public void setParamSourcelanguage(String language) {

        m_sourcelanguage = language;
    }

    /**
     * Sets the target language.<p>
     * 
     * @param language the target language
     */
    public void setParamTargetlanguage(String language) {

        m_targetlanguage = language;
    }

}
