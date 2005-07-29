/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/content/CmsChangeElementLocaleThread.java,v $
 * Date   : $Date: 2005/07/29 15:38:42 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.tools.content;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.page.CmsXmlPage;
import org.opencms.xml.page.CmsXmlPageFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Changes the element Locales of resources using the corresponding settings object.<p>
 * 
 * @author Andreas Zahner
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 6.0.1 
 */
public class CmsChangeElementLocaleThread extends A_CmsReportThread {

    private Throwable m_error;
    
    private CmsElementChangeLocaleSettings m_settings;

    /**
     * Creates a change element Locale Thread.<p>
     * 
     * @param cms the current cms context
     * @param settings the settings needed to perform the operation
     */
    public CmsChangeElementLocaleThread(CmsObject cms, CmsElementChangeLocaleSettings settings) {

        super(cms, Messages.get().key(cms.getRequestContext().getLocale(), Messages.GUI_CHANGEELEMENTLOCALE_THREAD_NAME_0, null));
        initHtmlReport(cms.getRequestContext().getLocale());
        m_settings = settings;
    }

    /**
     * @see org.opencms.report.A_CmsReportThread#getError()
     */
    public Throwable getError() {

        return m_error;
    }

    /**
     * @see org.opencms.report.A_CmsReportThread#getReportUpdate()
     */
    public String getReportUpdate() {

        return getReport().getReportUpdate();
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {

        getReport().println(
            Messages.get().container(
                Messages.RPT_CHANGEELEMENTLOCALE_BEGIN_2, m_settings.getOldLocale(), m_settings.getNewLocale()),
            I_CmsReport.FORMAT_HEADLINE);
        try {
            // change the element locales
            changeElementLocales();
        } catch (CmsException e) {
            getReport().println(e);
        }

        // append runtime statistics to report
        getReport().print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_STAT_0));
        getReport().println(
            org.opencms.report.Messages.get().container(
                org.opencms.report.Messages.RPT_STAT_DURATION_1,
                getReport().formatRuntime()));
        getReport().println(
            Messages.get().container(Messages.RPT_CHANGEELEMENTLOCALE_END_0),
            I_CmsReport.FORMAT_HEADLINE);
    }
    
    /**
     * Performs the changing of the element Locales.<p>
     * 
     * @throws CmsException if reading the list of resources to change fails
     */
    private void changeElementLocales() throws CmsException {

        // create Locale objects to work with
        Locale oldLocale = CmsLocaleManager.getLocale(m_settings.getOldLocale());
        Locale newLocale = CmsLocaleManager.getLocale(m_settings.getNewLocale());
        boolean checkTemplate = CmsStringUtil.isNotEmpty(m_settings.getTemplate());

        // set the resource filter to filter xml pages        
        CmsResourceFilter filter = CmsResourceFilter.IGNORE_EXPIRATION.addRequireType(CmsResourceTypeXmlPage.getStaticTypeId());
        String path = CmsResource.getFolderPath(m_settings.getVfsFolder());
        // get the list of resources to change
        List resources = getCms().readResources(path, filter, m_settings.isIncludeSubFolders());

        // set the report counters
        int count = 0;
        int resSize = resources.size();

        // iterate the resources
        Iterator i = resources.iterator();
        while (i.hasNext()) {

            count++;
            CmsResource res = (CmsResource)i.next();

            // generate report output
            getReport().print(
                org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_SUCCESSION_2,
                    String.valueOf(count),
                    String.valueOf(resSize)),
                I_CmsReport.FORMAT_NOTE);
            getReport().print(Messages.get().container(Messages.RPT_PROCESSING_PAGE_0), I_CmsReport.FORMAT_NOTE);
            getReport().print(
                org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_ARGUMENT_1,
                    getCms().getSitePath(res)));
            getReport().print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

            try {

                if (checkTemplate) {
                    // check the template property
                    String template = getCms().readPropertyObject(res, CmsPropertyDefinition.PROPERTY_TEMPLATE, true).getValue(
                        "");
                    if (!m_settings.getTemplate().equals(template)) {
                        // template property does not match, report and continue with next resource
                        getReport().println(
                            Messages.get().container(Messages.RPT_CHANGEELEMENTLOCALE_TEMPLATE_0),
                            I_CmsReport.FORMAT_NOTE);
                        continue;
                    }
                }

                // get the file contents
                CmsFile file = CmsFile.upgrade(res, getCms());
                // get the page object
                CmsXmlPage page = CmsXmlPageFactory.unmarshal(getCms(), file);
                // write the report output

                if (!page.hasLocale(oldLocale)) {
                    // old Locale not present, report and continue with next resource
                    getReport().println(
                        Messages.get().container(
                            Messages.RPT_CHANGEELEMENTLOCALE_OLDLOCALE_1,
                            m_settings.getOldLocale()),
                        I_CmsReport.FORMAT_NOTE);
                    continue;
                }

                if (page.hasLocale(newLocale)) {
                    // target Locale present, report and continue with next resource
                    getReport().println(
                        Messages.get().container(
                            Messages.RPT_CHANGEELEMENTLOCALE_NEWLOCALE_1,
                            m_settings.getNewLocale()),
                        I_CmsReport.FORMAT_NOTE);
                    continue;
                }

                // change the Locale of the elements
                page.moveLocale(oldLocale, newLocale);

                // set the file contents
                file.setContents(page.marshal());

                // check the lock state of the file to write
                CmsLock lock = getCms().getLock(res);
                boolean isLocked = false;
                boolean canWrite = false;
                if (lock.isNullLock()) {
                    // file not locked, lock it
                    getCms().lockResource(getCms().getSitePath(res));
                    isLocked = true;
                    canWrite = true;
                } else if (lock.getUserId().equals(getCms().getRequestContext().currentUser().getId())) {
                    // file locked by current user
                    canWrite = true;
                }

                if (canWrite) {
                    // write the file contents
                    getCms().writeFile(file);
                } else {
                    // no write operation possible
                    getReport().println(
                        Messages.get().container(Messages.RPT_CHANGEELEMENTLOCALE_NOTLOCKED_0),
                        I_CmsReport.FORMAT_NOTE);
                }

                if (isLocked) {
                    // unlock previously locked resource
                    getCms().unlockResource(getCms().getSitePath(res));
                }

                if (canWrite) {
                    // successfully changed, report it
                    getReport().println(
                        org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                        I_CmsReport.FORMAT_OK);
                }

            } catch (CmsException e) {
                // an error occurred, show exception on report output
                getReport().println(e);
            }
        }
    }
}