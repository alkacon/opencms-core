/*
 * This program is part of the Alkacon OpenCms Software library.
 *
 * This license applies to all programs, pages, Java classes, parts and
 * modules of the Alkacon OpenCms Software library published by
 * Alkacon Software GmbH & Co. KG, unless otherwise noted.
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * companys website: http://www.alkacon.com.
 *
 * For further information about OpenCms, please see the OpenCms project
 * website: http://www.opencms.org.
 *
 * The names "Alkacon", "Alkacon Software GmbH & Co. KG" and "OpenCms" must not be used
 * to endorse or promote products derived from this software without prior
 * written permission. For written permission, please contact info@alkacon.com.
 *
 * Products derived from this software may not be called "Alkacon",
 * "Alkacon Software GmbH & Co. KG" or "OpenCms", nor may "Alkacon", "Alkacon Software GmbH & Co. KG"
 * or "OpenCms" appear in their name, without prior written permission of
 * Alkacon Software GmbH & Co. KG.
 *
 * This program is also available under a commercial non-GPL license. For
 * pricing and ordering information, please inquire at sales@alkacon.com.
 */

package org.opencms.workplace.tools.searchindex;

import org.opencms.file.CmsObject;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsStringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements methods to utilize a report thread for <code>CmsIndexingReport</code>.<p>
 *
 * @since 6.0.0
 */
public class CmsIndexingReportThread extends A_CmsReportThread {

    /** The last error occurred. */
    private Throwable m_error;

    /** A list of names of the indexes to refresh or null for all indexes. */
    private List<String> m_indexNames;

    /**
     * Creates an indexing Thread for full update.<p>
     *
     * @param cms the current OpenCms context object
     * @param indexNames a list of names of the indexes to refresh or null for all indexes
     */
    public CmsIndexingReportThread(CmsObject cms, List<String> indexNames) {

        super(cms, Messages.get().getBundle().key(Messages.GUI_INDEXING_THREAD_NAME_0));
        initHtmlReport(cms.getRequestContext().getLocale());

        m_indexNames = indexNames;
    }

    /**
     * Returns the last error.<p>
     *
     * @see org.opencms.report.A_CmsReportThread#getError()
     */
    @Override
    public Throwable getError() {

        return m_error;
    }

    /**
     * Updates the report.<p>
     *
     * @see org.opencms.report.A_CmsReportThread#getReportUpdate()
     */
    @Override
    public String getReportUpdate() {

        return getReport().getReportUpdate();
    }

    /**
     * Starts the indexing report thread.<p>
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        getReport().println(
            Messages.get().container(Messages.RPT_REBUILD_SEARCH_INDEXES_BEGIN_0),
            I_CmsReport.FORMAT_HEADLINE);

        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put(I_CmsEventListener.KEY_REPORT, getReport());
            if (m_indexNames != null) {
                params.put(I_CmsEventListener.KEY_INDEX_NAMES, CmsStringUtil.collectionAsString(m_indexNames, ","));
            }
            OpenCms.fireCmsEvent(I_CmsEventListener.EVENT_REBUILD_SEARCHINDEXES, params);
            getReport().println(
                Messages.get().container(Messages.RPT_REBUILD_SEARCH_INDEXES_END_0),
                I_CmsReport.FORMAT_HEADLINE);
        } catch (Throwable exc) {
            getReport().println(
                org.opencms.search.Messages.get().container(org.opencms.search.Messages.RPT_SEARCH_INDEXING_FAILED_0),
                I_CmsReport.FORMAT_WARNING);
            getReport().println(exc);
            m_error = exc;
        }
    }
}
