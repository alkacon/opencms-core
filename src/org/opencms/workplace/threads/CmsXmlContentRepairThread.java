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

package org.opencms.workplace.threads;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Repairs XML content resources according to their XSD using the corresponding settings object.<p>
 *
 */
public class CmsXmlContentRepairThread extends A_CmsReportThread {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsXmlContentRepairThread.class);

    /** The dialog settings used to configure the repair thread. */
    private CmsXmlContentRepairSettings m_settings;

    /**
     * Creates a repair XML content resources thread.<p>
     *
     * @param cms the current cms context
     * @param settings the settings needed to perform the repair operation
     */
    public CmsXmlContentRepairThread(CmsObject cms, CmsXmlContentRepairSettings settings) {

        super(cms, Messages.get().getBundle().key(Messages.GUI_XMLCONTENTREPAIR_THREAD_NAME_0));
        initHtmlReport(cms.getRequestContext().getLocale());
        m_settings = settings;
    }

    /**
     * @see org.opencms.report.A_CmsReportThread#getReportUpdate()
     */
    @Override
    public String getReportUpdate() {

        return getReport().getReportUpdate();
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        getReport().println(
            Messages.get().container(
                Messages.RPT_XMLCONTENTREPAIR_BEGIN_2,
                m_settings.getResourceType(),
                m_settings.getVfsFolder()),
            I_CmsReport.FORMAT_HEADLINE);
        try {
            // repair the XML content resources
            repairXmlContents();
        } catch (Throwable f) {
            getReport().println(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_FAILED_0),
                I_CmsReport.FORMAT_ERROR);
            getReport().println(f);
            if (LOG.isErrorEnabled()) {
                LOG.error(f);
            }
        }

        // append runtime statistics to the report output
        getReport().print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_STAT_0));
        getReport().println(
            org.opencms.report.Messages.get().container(
                org.opencms.report.Messages.RPT_STAT_DURATION_1,
                getReport().formatRuntime()));
        getReport().println(Messages.get().container(Messages.RPT_XMLCONTENTREPAIR_END_0), I_CmsReport.FORMAT_HEADLINE);
    }

    /**
     * Performs the correction of the XML content resources according to their XML schema definition.<p>
     *
     * @throws CmsException if reading the list of resources to repair fails
     */
    private void repairXmlContents() throws CmsException {

        // set the resource filter to filter XML contents of the selected type
        CmsResourceFilter filter = CmsResourceFilter.IGNORE_EXPIRATION.addRequireType(m_settings.getResourceTypeId());
        String path = CmsResource.getFolderPath(m_settings.getVfsFolder());
        // get the list of resources to check
        List<CmsResource> resources = getCms().readResources(path, filter, m_settings.isIncludeSubFolders());

        // set the report counters
        int count = 0;
        int resSize = resources.size();

        // create an entity resolver to use
        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(getCms());

        // iterate the resources
        Iterator<CmsResource> i = resources.iterator();
        while (i.hasNext()) {

            count++;
            CmsResource res = i.next();

            // generate report output
            getReport().print(
                org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_SUCCESSION_2,
                    String.valueOf(count),
                    String.valueOf(resSize)),
                I_CmsReport.FORMAT_NOTE);
            getReport().print(Messages.get().container(Messages.RPT_PROCESSING_XMLCONTENT_0), I_CmsReport.FORMAT_NOTE);
            getReport().print(
                org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_ARGUMENT_1,
                    getCms().getSitePath(res)));
            getReport().print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

            try {

                // get the file contents
                CmsFile file = getCms().readFile(res);
                // get the XML content
                CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(getCms(), file);

                // check the XML structure
                boolean fixFile = m_settings.isForce();
                if (!fixFile) {
                    try {
                        xmlContent.validateXmlStructure(resolver);
                    } catch (CmsXmlException e) {
                        // XML structure is not valid, this file has to be fixed
                        fixFile = true;
                    }
                }
                if (fixFile) {

                    // check the lock state of the file to repair
                    CmsLock lock = getCms().getLock(res);
                    boolean isLocked = false;
                    boolean canWrite = false;
                    if (lock.isNullLock()) {
                        // file is not locked, lock it
                        getCms().lockResource(getCms().getSitePath(res));
                        isLocked = true;
                        canWrite = true;
                    } else if (lock.isOwnedBy(getCms().getRequestContext().getCurrentUser())) {
                        // file is locked by current user
                        canWrite = true;
                    }

                    if (canWrite) {
                        // enable "auto correction mode" - this is required or the XML structure will not be fully corrected
                        xmlContent.setAutoCorrectionEnabled(true);
                        // now correct the XML
                        xmlContent.correctXmlStructure(getCms());
                        file.setContents(xmlContent.marshal());
                        // write the corrected file
                        getCms().writeFile(file);
                    } else {
                        // no write operation possible
                        getReport().println(
                            Messages.get().container(Messages.RPT_XMLCONTENTREPAIR_NOTLOCKED_0),
                            I_CmsReport.FORMAT_NOTE);
                    }

                    if (isLocked) {
                        // unlock previously locked resource
                        getCms().unlockResource(getCms().getSitePath(res));
                    }

                    if (canWrite) {
                        // successfully repaired XML content, report it
                        getReport().println(
                            Messages.get().container(Messages.RPT_XMLCONTENTREPAIR_REPAIRED_0),
                            I_CmsReport.FORMAT_OK);
                    }

                } else {
                    // nothing to fix, skip this file
                    getReport().println(
                        Messages.get().container(Messages.RPT_XMLCONTENTREPAIR_SKIPFILE_0),
                        I_CmsReport.FORMAT_NOTE);
                }

            } catch (CmsException e) {
                // an error occurred, show exception on report output
                getReport().println(e);
            }
        }
    }

}