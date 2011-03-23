/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/content/languagecopy/CmsLanguageCopyThread.java,v $
 * Date   : $Date: 2011/03/23 14:50:23 $
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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.CmsHtmlReport;
import org.opencms.report.CmsLogReport;
import org.opencms.report.I_CmsReport;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

/**
 * Copies language nodes in XML contents.
 * <p>
 * 
 * @author Achim Westermann
 * @author Mario Jaeger
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 7.5.1
 */
public class CmsLanguageCopyThread extends A_CmsReportThread {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLanguageCopyThread.class);

    /** The resources to copy. */
    private String[] m_copyresources;

    /** The special multiplex report. */
    private I_CmsReport m_report;

    /** The source language. */
    private String m_sourceLanguage;

    /** The source language. */
    private String m_targetLanguage;

    /**
     * Copies language nodes in XML contents..
     * <p>
     * 
     * @param cms the current CmsObject
     * @param copyResources the resources to copy
     * @param sourceLanguage the source language
     * @param targetLanguage the target language
     *            
     */
    public CmsLanguageCopyThread(
        final CmsObject cms,
        String[] copyResources,
        String sourceLanguage,
        String targetLanguage) {

        super(cms, Messages.get().getBundle().key(Messages.GUI_REPORT_LANGUAGECOPY_NAME_0));
        m_copyresources = copyResources;
        m_sourceLanguage = sourceLanguage;
        m_targetLanguage = targetLanguage;
        initHtmlReport(cms.getRequestContext().getLocale());
        CmsMultiplexReport report = new CmsMultiplexReport();
        Locale locale = cms.getRequestContext().getLocale();
        report.addReport(new CmsHtmlReport(locale, cms.getRequestContext().getSiteRoot()));
        report.addReport(new CmsLogReport(locale, CmsLanguageCopyThread.class));
        this.m_report = report;
    }

    /**
     * @see org.opencms.report.A_CmsReportThread#getReportUpdate()
     */
    @Override
    public String getReportUpdate() {

        return this.getReport().getReportUpdate();
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        CmsMultiplexReport report = (CmsMultiplexReport)this.getReport();
        int totalFiles = this.m_copyresources.length;
        Locale sourceLocale = new Locale(this.m_sourceLanguage);
        Locale targetLocale = new Locale(this.m_targetLanguage);
        report.println(Messages.get().container(
            Messages.GUI_REPORT_LANGUAGEC0PY_START_3,
            new Object[] {new Integer(totalFiles), sourceLocale, targetLocale}), I_CmsReport.FORMAT_HEADLINE);

        try {
            this.copyLanguageNodes();

        } catch (Throwable e) {
            report.println(e);
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.ERR_REPORT_LANGUAGEC0PY_0), e);
            }
        }
        // List the errors:
        List<CmsMessageContainer> errors = report.getErrors();
        List<CmsMessageContainer> warnings = report.getWarnings();

        report.println(Messages.get().container(
            Messages.GUI_REPORT_LANGUAGEC0PY_END_2,
            new Object[] {new Integer(warnings.size()), new Integer(errors.size())}), I_CmsReport.FORMAT_HEADLINE);
        for (CmsMessageContainer f : warnings) {
            report.println(f, I_CmsReport.FORMAT_WARNING);
        }
        for (CmsMessageContainer f : errors) {
            report.println(f, I_CmsReport.FORMAT_ERROR);
        }

    }

    /**
     * @see org.opencms.report.A_CmsReportThread#getReport()
     */
    @Override
    protected I_CmsReport getReport() {

        return this.m_report;
    }

    /**
     * Does the job.
     * <p>
     */
    private void copyLanguageNodes() {

        CmsObject cms = this.getCms();
        CmsMultiplexReport report = (CmsMultiplexReport)this.getReport();
        CmsFile file;
        CmsXmlContent content;
        int totalFiles = this.m_copyresources.length;
        int processedFiles = 0;
        Locale sourceLocale = new Locale(this.m_sourceLanguage);
        Locale targetLocale = new Locale(this.m_targetLanguage);

        for (int i = 0; i < this.m_copyresources.length; i++) {
            processedFiles++;
            report.print(org.opencms.report.Messages.get().container(
                org.opencms.report.Messages.RPT_SUCCESSION_2,
                new Object[] {String.valueOf(processedFiles), String.valueOf(totalFiles)}), I_CmsReport.FORMAT_NOTE);

            report.print(Messages.get().container(
                Messages.LOCALIZATION_BYPASS_1,
                new Object[] {this.m_copyresources[i]}));
            report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

            try {
                file = cms.readFile(this.m_copyresources[i]);
                content = CmsXmlContentFactory.unmarshal(cms, file);

                if (!content.hasLocale(sourceLocale)) {
                    report.println(Messages.get().container(
                        Messages.GUI_REPORT_LANGUAGEC0PY_WARN_SOURCELOCALE_MISSING_1,
                        new Object[] {sourceLocale}), I_CmsReport.FORMAT_WARNING);
                    CmsMessageContainer container = Messages.get().container(
                        Messages.GUI_REPORT_LANGUAGEC0PY_WARN_SOURCELOCALE_MISSING_2,
                        new Object[] {this.m_copyresources[i], sourceLocale});
                    report.addWarning(container);
                } else if (content.hasLocale(targetLocale)) {
                    report.println(Messages.get().container(
                        Messages.GUI_REPORT_LANGUAGEC0PY_WARN_TARGETLOCALE_EXISTS_1,
                        new Object[] {targetLocale}), I_CmsReport.FORMAT_WARNING);
                    CmsMessageContainer container = Messages.get().container(
                        Messages.GUI_REPORT_LANGUAGEC0PY_WARN_TARGETLOCALE_EXISTS_2,
                        new Object[] {this.m_copyresources[i], targetLocale});
                    report.addWarning(container);
                } else {
                    content.copyLocale(sourceLocale, targetLocale);
                    file.setContents(content.marshal());
                    CmsLock lock = cms.getLock(file);
                    if (lock.isInherited()) {
                        this.unlockInherited(file.getRootPath());
                        cms.lockResource(this.m_copyresources[i]);
                    } else {
                        if (lock.isNullLock()) {
                            cms.lockResource(this.m_copyresources[i]);
                        } else {
                            if (!lock.isLockableBy(cms.getRequestContext().currentUser())) {
                                cms.changeLock(this.m_copyresources[i]);
                            }
                        }
                    }
                    cms.writeFile(file);
                    cms.unlockResource(this.m_copyresources[i]);
                    report.println(
                        org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                        I_CmsReport.FORMAT_OK);
                }

            } catch (Throwable f) {
                CmsMessageContainer error = Messages.get().container(
                    Messages.GUI_REPORT_LANGUAGEC0PY_ERROR_2,
                    new String[] {this.m_copyresources[i], CmsException.getStackTraceAsString(f)});

                report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_FAILED_0),
                    I_CmsReport.FORMAT_ERROR);
                // report.println(f);
                report.addError(error);

            }
        }
    }

    /**
     * Returns the lock of a possible locked parent folder of a resource, system locks are ignored.
     * <p>
     * 
     * @param absoluteResourceName the name of the resource
     * 
     * @return the lock of a parent folder, or {@link CmsLock#getNullLock()} if no parent folders are locked by a non
     *  system lock.
     */
    @SuppressWarnings("unchecked")
    private CmsLock getParentFolderLock(final String absoluteResourceName) {

        Iterator<CmsLock> itLocks = OpenCms.getMemoryMonitor().getAllCachedLocks().iterator();
        while (itLocks.hasNext()) {
            CmsLock lock = itLocks.next();
            if (lock.getResourceName().endsWith("/")
                && absoluteResourceName.startsWith(lock.getResourceName())
                && !absoluteResourceName.equals(lock.getResourceName())) {
                // system locks does not get inherited
                lock = lock.getEditionLock();
                // check the lock
                if (!lock.isUnlocked()) {
                    return lock;
                }
            }
        }
        return CmsLock.getNullLock();
    }

    /**
     * Returns the inherited lock of a resource.
     * <p>
     * 
     * @param absoluteResourcename the absolute path of the resource
     * @return the inherited lock or the null lock
     */
    private CmsLock getParentLock(final String absoluteResourcename) {

        CmsLock parentFolderLock = getParentFolderLock(absoluteResourcename);
        if (!parentFolderLock.isNullLock()) {
            return parentFolderLock;
        }
        return CmsLock.getNullLock();
    }

    /**
     * Recursively steps up to the resource that is the originator of the given resource which has an inherited lock.
     * <p>
     * 
     * @param absoluteResourcename the absolute resource with the inherited lock.
     * 
     * @throws CmsException if something goes wrong.
     */
    private void unlockInherited(final String absoluteResourcename) throws CmsException {

        CmsObject cms = this.getCms();
        CmsLock parentLock = this.getParentLock(absoluteResourcename);
        if (!parentLock.isNullLock()) {

            if (parentLock.isInherited()) {
                this.unlockInherited(parentLock.getResourceName());
            } else {
                if (!parentLock.isLockableBy(cms.getRequestContext().currentUser())) {
                    cms.changeLock(cms.getRequestContext().removeSiteRoot(parentLock.getResourceName()));
                }
                cms.unlockResource(cms.getRequestContext().removeSiteRoot(parentLock.getResourceName()));
            }
        }

    }

}
