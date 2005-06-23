/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/searchindex/CmsIndexingReportThread.java,v $
 * Date   : $Date: 2005/06/23 09:05:01 $
 * Version: $Revision: 1.5 $
 *
 * This program is part of the Alkacon OpenCms Software library.
 *
 * This license applies to all programs, pages, Java classes, parts and
 * modules of the Alkacon OpenCms Software library published by
 * Alkacon Software, unless otherwise noted.
 *
 * Copyright (C) 2003 - 2005 Alkacon Software (http://www.alkacon.com)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * For further information about Alkacon Software, please see the
 * companys website: http://www.alkacon.com.
 * 
 * For further information about OpenCms, please see the OpenCms project
 * website: http://www.opencms.org.
 * 
 * The names "Alkacon", "Alkacon Software" and "OpenCms" must not be used 
 * to endorse or promote products derived from this software without prior 
 * written permission. For written permission, please contact info@alkacon.com.
 * 
 * Products derived from this software may not be called "Alkacon", 
 * "Alkacon Software" or "OpenCms", nor may "Alkacon", "Alkacon Software" 
 * or "OpenCms" appear in their name, without prior written permission of 
 * Alkacon Software. 
 *
 * This program is also available under a commercial non-GPL license. For
 * pricing and ordering information, please inquire at sales@alkacon.com.
 */

package org.opencms.workplace.tools.searchindex;

import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsVfsResourceNotFoundException;

/**
 * Implements methods to utilize a report thread for <code>CmsIndexingReport</code>.<p>
 * 
 * @author Carsten Weinholz 
 * 
 * @version $Revision: 1.5 $ 
 * 
 * @since 6.0.0 
 */
public class CmsIndexingReportThread extends A_CmsReportThread {

    /** The last error occured. */
    private Throwable m_error;

    /** The name of the index to refresh or null for all indexes. */
    private String m_indexName;

    /**
     * Creates an indexing Thread for full update.<p>
     * 
     * @param cms the current OpenCms context object
     */
    public CmsIndexingReportThread(CmsObject cms) {

        super(cms, Messages.get().key(cms.getRequestContext().getLocale(), Messages.GUI_INDEXING_THREAD_NAME_0, null));
        initHtmlReport(cms.getRequestContext().getLocale());

        m_indexName = null;

        start();
    }

    /**
     * Creates an indexing thread for refreshing a single index.<p>
     * 
     * @param cms the cms object
     * @param indexName the name of the index to refresh
     */
    public CmsIndexingReportThread(CmsObject cms, String indexName) {

        super(cms, Messages.get().key(
            cms.getRequestContext().getLocale(),
            Messages.GUI_INDEXING_THREAD_NAME_1,
            new Object[] {indexName}));
        initHtmlReport(cms.getRequestContext().getLocale());

        m_indexName = indexName;

        start();
    }

    /**
     * Returns the last error.<p>
     * 
     * @see org.opencms.report.A_CmsReportThread#getError()
     */
    public Throwable getError() {

        return m_error;
    }

    /**
     * Updates the report.<p>
     * 
     * @see org.opencms.report.A_CmsReportThread#getReportUpdate()
     */
    public String getReportUpdate() {

        return getReport().getReportUpdate();
    }

    /**
     * Starts the indexing report thread.<p>
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {

        getReport().println(
            Messages.get().container(Messages.RPT_REBUILD_SEARCH_INDEXES_BEGIN_0),
            I_CmsReport.C_FORMAT_HEADLINE);
        try {

            if (m_indexName == null) {
                OpenCms.getSearchManager().updateIndex(getReport());
            } else {
                OpenCms.getSearchManager().updateIndex(m_indexName, getReport());
            }
            getReport().println(
                Messages.get().container(Messages.RPT_REBUILD_SEARCH_INDEXES_END_0),
                I_CmsReport.C_FORMAT_HEADLINE);
        } catch (CmsVfsResourceNotFoundException e) {

            getReport().println(
                Messages.get().container(Messages.RPT_SEARCH_CONFIG_NOT_FOUND_0),
                I_CmsReport.C_FORMAT_NOTE);
            m_error = e;
        } catch (CmsException exc) {
            getReport().println(
                org.opencms.search.Messages.get().container(org.opencms.search.Messages.RPT_SEARCH_INDEXING_FAILED_0),
                I_CmsReport.C_FORMAT_WARNING);
            getReport().println(exc);
            m_error = exc;
        }
    }

}