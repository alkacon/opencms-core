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

package org.opencms.workplace.tools.content.languagecopy;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.CmsHtmlReport;
import org.opencms.report.CmsLogReport;
import org.opencms.report.CmsMultiplexReport;
import org.opencms.report.I_CmsReport;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

/**
 * Copies language nodes in XML contents.<p>
 *
 * @since 7.5.1
 */
public class CmsLanguageCopyThread extends A_CmsReportThread {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLanguageCopyThread.class);

    /** The resources to copy. */
    private String[] m_copyresources;

    /** The delete flag. */
    private boolean m_delete;

    /** The special multiplex report. */
    private I_CmsReport m_report;

    /** The source language. */
    private String m_sourceLanguage;

    /** The source language. */
    private String m_targetLanguage;

    /**
     * Copies language nodes in XML contents.<p>
     *
     * @param cms the current cms context
     * @param copyResources the resources to copy
     * @param delete the delete flag
     * @param sourceLanguage the source language
     * @param targetLanguage the target language
     */
    public CmsLanguageCopyThread(
        final CmsObject cms,
        String[] copyResources,
        boolean delete,
        String sourceLanguage,
        String targetLanguage) {

        super(cms, Messages.get().getBundle().key(Messages.GUI_REPORT_LANGUAGECOPY_NAME_0));
        m_copyresources = copyResources;
        m_sourceLanguage = sourceLanguage;
        m_targetLanguage = targetLanguage;
        m_delete = delete;
        initHtmlReport(cms.getRequestContext().getLocale());
        CmsMultiplexReport report = new CmsMultiplexReport();
        Locale locale = cms.getRequestContext().getLocale();
        report.addReport(new CmsHtmlReport(locale, cms.getRequestContext().getSiteRoot()));
        report.addReport(new CmsLogReport(locale, CmsLanguageCopyThread.class));
        m_report = report;
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

        CmsMultiplexReport report = (CmsMultiplexReport)getReport();
        int totalFiles = m_copyresources.length;
        Locale sourceLocale = CmsLocaleManager.getLocale(m_sourceLanguage);
        Locale targetLocale = CmsLocaleManager.getLocale(m_targetLanguage);
        report.println(
            Messages.get().container(
                Messages.GUI_REPORT_LANGUAGEC0PY_START_3,
                new Object[] {Integer.valueOf(totalFiles), sourceLocale, targetLocale}),
            I_CmsReport.FORMAT_HEADLINE);

        try {
            copyLanguageNodes();
        } catch (Throwable e) {
            report.println(e);
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.ERR_REPORT_LANGUAGEC0PY_0), e);
            }
        }
        // List the errors:
        List<Object> errors = report.getErrors();
        List<Object> warnings = report.getWarnings();

        report.println(
            Messages.get().container(
                Messages.GUI_REPORT_LANGUAGEC0PY_END_2,
                new Object[] {Integer.valueOf(warnings.size()), Integer.valueOf(errors.size())}),
            I_CmsReport.FORMAT_HEADLINE);
        for (Object f : warnings) {
            if (f instanceof CmsMessageContainer) {
                report.println((CmsMessageContainer)f, I_CmsReport.FORMAT_WARNING);
            }
        }
        for (Object f : errors) {
            if (f instanceof CmsMessageContainer) {
                report.println((CmsMessageContainer)f, I_CmsReport.FORMAT_ERROR);
            }
        }
    }

    /**
     * @see org.opencms.report.A_CmsReportThread#getReport()
     */
    @Override
    protected I_CmsReport getReport() {

        return m_report;
    }

    /**
     * Does the job.<p>
     */
    private void copyLanguageNodes() {

        CmsObject cms = getCms();
        CmsMultiplexReport report = (CmsMultiplexReport)getReport();
        CmsFile file;
        CmsXmlContent content;
        int totalFiles = m_copyresources.length;
        int processedFiles = 0;
        Locale sourceLocale = CmsLocaleManager.getLocale(m_sourceLanguage);
        Locale targetLocale = CmsLocaleManager.getLocale(m_targetLanguage);

        for (int i = 0; i < m_copyresources.length; i++) {
            processedFiles++;
            report.print(
                org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_SUCCESSION_2,
                    new Object[] {String.valueOf(processedFiles), String.valueOf(totalFiles)}),
                I_CmsReport.FORMAT_NOTE);

            report.print(
                Messages.get().container(Messages.RPT_LOCALIZATION_BYPASS_1, new Object[] {m_copyresources[i]}));
            report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

            try {
                file = cms.readFile(m_copyresources[i]);
                content = CmsXmlContentFactory.unmarshal(cms, file);

                if (!content.hasLocale(sourceLocale)) {
                    report.println(
                        Messages.get().container(
                            Messages.GUI_REPORT_LANGUAGEC0PY_WARN_SOURCELOCALE_MISSING_1,
                            new Object[] {sourceLocale}),
                        I_CmsReport.FORMAT_WARNING);
                    CmsMessageContainer container = Messages.get().container(
                        Messages.GUI_REPORT_LANGUAGEC0PY_WARN_SOURCELOCALE_MISSING_2,
                        new Object[] {m_copyresources[i], sourceLocale});
                    report.addWarning(container);
                } else if (content.hasLocale(targetLocale)) {
                    report.println(
                        Messages.get().container(
                            Messages.GUI_REPORT_LANGUAGEC0PY_WARN_TARGETLOCALE_EXISTS_1,
                            new Object[] {targetLocale}),
                        I_CmsReport.FORMAT_WARNING);
                    CmsMessageContainer container = Messages.get().container(
                        Messages.GUI_REPORT_LANGUAGEC0PY_WARN_TARGETLOCALE_EXISTS_2,
                        new Object[] {m_copyresources[i], targetLocale});
                    report.addWarning(container);
                } else {
                    content.copyLocale(sourceLocale, targetLocale);
                    if (m_delete) {
                        content.removeLocale(sourceLocale);
                    }
                    file.setContents(content.marshal());
                    CmsLock lock = cms.getLock(file);
                    if (lock.isInherited()) {
                        unlockInherited(file.getRootPath());
                        cms.lockResource(m_copyresources[i]);
                    } else {
                        if (lock.isNullLock()) {
                            cms.lockResource(m_copyresources[i]);
                        } else {
                            if (!lock.isLockableBy(cms.getRequestContext().getCurrentUser())) {
                                cms.changeLock(m_copyresources[i]);
                            }
                        }
                    }
                    cms.writeFile(file);
                    cms.unlockResource(m_copyresources[i]);
                    report.println(
                        org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                        I_CmsReport.FORMAT_OK);
                }

            } catch (Throwable f) {
                CmsMessageContainer error = Messages.get().container(
                    Messages.GUI_REPORT_LANGUAGEC0PY_ERROR_2,
                    new String[] {m_copyresources[i], CmsException.getStackTraceAsString(f)});

                report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_FAILED_0),
                    I_CmsReport.FORMAT_ERROR);
                // report.println(f);
                report.addError(error);

            }
        }
    }

    /**
     * Returns the lock of a possible locked parent folder of a resource, system locks are ignored.<p>
     *
     * @param absoluteResourceName the name of the resource
     *
     * @return the lock of a parent folder, or {@link CmsLock#getNullLock()}
     *            if no parent folders are locked by a non system lock
     */
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
     * Returns the inherited lock of a resource.<p>
     *
     * @param absoluteResourcename the absolute path of the resource
     *
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
     * Recursively steps up to the resource that is the originator of the given
     * resource which has an inherited lock.<p>
     *
     * @param absoluteResourcename the absolute resource with the inherited lock
     *
     * @throws CmsException if something goes wrong
     */
    private void unlockInherited(final String absoluteResourcename) throws CmsException {

        CmsObject cms = getCms();
        CmsLock parentLock = getParentLock(absoluteResourcename);
        if (!parentLock.isNullLock()) {
            if (parentLock.isInherited()) {
                unlockInherited(parentLock.getResourceName());
            } else {
                if (!parentLock.isLockableBy(cms.getRequestContext().getCurrentUser())) {
                    cms.changeLock(cms.getRequestContext().removeSiteRoot(parentLock.getResourceName()));
                }
                cms.unlockResource(cms.getRequestContext().removeSiteRoot(parentLock.getResourceName()));
            }
        }

    }
}
