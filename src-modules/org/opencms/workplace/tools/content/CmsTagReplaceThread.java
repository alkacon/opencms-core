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

package org.opencms.workplace.tools.content;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

import org.htmlparser.util.ParserException;

/**
 * Replaces HTML tags of xmlpage resources using the corresponding settings object.
 * <p>
 *
 * @since 6.1.8
 */
public class CmsTagReplaceThread extends A_CmsReportThread {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsTagReplaceThread.class);

    private CmsProperty m_markerProperty;

    private CmsTagReplaceSettings m_settings;

    /**
     * Creates a replace html tag Thread.<p>
     *
     * @param cms the current cms context.
     *
     * @param settings the settings needed to perform the operation.
     */
    public CmsTagReplaceThread(CmsObject cms, CmsTagReplaceSettings settings) {

        super(cms, Messages.get().getBundle().key(Messages.GUI_TAGREPLACE_THREAD_NAME_0));
        initHtmlReport(cms.getRequestContext().getLocale());
        m_settings = settings;
        m_markerProperty = new CmsProperty(
            CmsTagReplaceSettings.PROPERTY_CONTENTOOLS_TAGREPLACE,
            null,
            m_settings.getPropertyValueTagReplaceID(),
            true);
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
            Messages.get().container(Messages.RPT_TAGREPLACE_BEGIN_1, m_settings.getWorkPath()),
            I_CmsReport.FORMAT_HEADLINE);
        try {
            // change the element locales
            replaceTags();
        } catch (CmsException e) {
            getReport().println(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_FAILED_0),
                I_CmsReport.FORMAT_ERROR);
            getReport().println(e.getMessageContainer(), I_CmsReport.FORMAT_ERROR);
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getMessageContainer(), e);
            }
        } catch (Throwable f) {
            getReport().println(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_FAILED_0),
                I_CmsReport.FORMAT_ERROR);
            getReport().println(f);
            if (LOG.isErrorEnabled()) {
                LOG.error(f);
            }
        }

        // append runtime statistics to report
        getReport().print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_STAT_0));
        getReport().println(
            org.opencms.report.Messages.get().container(
                org.opencms.report.Messages.RPT_STAT_DURATION_1,
                getReport().formatRuntime()));
        getReport().println(Messages.get().container(Messages.RPT_TAGREPLACE_END_0), I_CmsReport.FORMAT_HEADLINE);
    }

    /**
     * Checks the shared property {@link CmsTagReplaceSettings#PROPERTY_CONTENTOOLS_TAGREPLACE} if
     * it has the value of this configuration ({@link CmsTagReplaceSettings#getPropertyValueTagReplaceID()}).<p>
     *
     * @param resource the resource to test.
     *
     * @return true if the property with the value was found.
     *
     * @throws CmsException if reading a property fails.
     */
    private boolean isProcessedBefore(CmsResource resource) throws CmsException {

        CmsProperty testProp = getCms().readPropertyObject(
            resource,
            CmsTagReplaceSettings.PROPERTY_CONTENTOOLS_TAGREPLACE,
            false);
        if (testProp.isNullProperty()) {
            return false;
        } else {
            String testValue = testProp.getResourceValue();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(testValue)) {
                return false;
            } else {
                return testValue.equals(m_settings.getPropertyValueTagReplaceID());
            }
        }
    }

    private void replaceTags() throws CmsException {

        I_CmsReport report = getReport();
        report.print(Messages.get().container(Messages.RPT_TAGREPLACE_READ_RESOURCES_1, m_settings.getWorkPath()));
        report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
        if (LOG.isDebugEnabled()) {
            LOG.debug(
                Messages.get().getBundle().key(Messages.RPT_TAGREPLACE_READ_RESOURCES_1, m_settings.getWorkPath()));
        }
        CmsResourceFilter filter = CmsResourceFilter.ALL.addRequireType(
            OpenCms.getResourceManager().getResourceType("xmlpage").getTypeId());
        List resources = getCms().readResources(m_settings.getWorkPath(), filter, true);
        if (LOG.isDebugEnabled()) {
            LOG.debug(
                Messages.get().getBundle().key(Messages.LOG_TAGREPLACE_READ_RESOURCES_OK_1, m_settings.getWorkPath()));
        }
        report.println(
            org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
            I_CmsReport.FORMAT_OK);
        Integer size = Integer.valueOf(resources.size());
        Iterator itResources = resources.iterator();
        CmsResource resource;
        int count = 1;
        while (itResources.hasNext()) {
            resource = (CmsResource)itResources.next();
            replaceTags(resource, size, Integer.valueOf(count));
            count++;
        }
    }

    /**
     * Replaces all replacement mappings configured in the internal {@link CmsTagReplaceSettings}
     * instance in the content of the given resource.<p>
     *
     * No modifications will be done:
     * <ol>
     * <li>the resource is locked by another user.</li>
     * <li>the special marker property with the value that stands for the replacement configuration
     * is set on the resource (shared).</li>
     * <li>locking of the non-locked resource fails.</li>
     * <li>Loading of the content fails.</li>
     * <li>Unmarshalling fails.</li>
     * <li>Unexpected exception while replacing occur.</li>
     * <li>Marshalling of XML fails.</li>
     * <li>Writing of the marker property fails.</li>
     * <li>Writing of the file fails.</li>
     * </ol>
     * <p>
     *
     * @param resource denotes the content to process.
     *
     * @param totalJobCount for fancy report writing.
     *
     * @param actualJobCount for even fancier report writing.
     *
     * @throws CmsException if sth. goes wrong.
     */
    private void replaceTags(CmsResource resource, Integer totalJobCount, Integer actualJobCount) throws CmsException {

        I_CmsReport report = getReport();
        report.print(org.opencms.report.Messages.get().container(
            org.opencms.report.Messages.RPT_SUCCESSION_2,
            actualJobCount,
            totalJobCount));
        report.print(
            Messages.get().container(
                Messages.RPT_TAGREPLACE_PROCESS_FILE_1,
                getCms().getRequestContext().removeSiteRoot(resource.getRootPath())));
        report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

        if (isProcessedBefore(resource)) {
            report.print(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_SKIPPED_0),
                I_CmsReport.FORMAT_OK);
            report.println(
                Messages.get().container(Messages.RPT_TAGREPLACE_SKIP_REASON_PROPERTY_0),
                I_CmsReport.FORMAT_OK);
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(
                Messages.get().getBundle().key(Messages.LOG_DEBUG_TAGREPLACE_LOCK_RESOURCE_1, resource.getRootPath()));
        }
        try {
            // checking the lock:
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    Messages.get().getBundle().key(Messages.LOG_DEBUG_TAGREPLACE_LOCK_READ_1, resource.getRootPath()));
            }

            CmsLock lock = getCms().getLock(resource);

            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    Messages.get().getBundle().key(Messages.LOG_DEBUG_TAGREPLACE_LOCK_READ_1, resource.getRootPath()));
            }

            boolean myLock = !lock.isNullLock() && lock.isOwnedBy(getCms().getRequestContext().getCurrentUser());
            if (lock.isNullLock() || myLock) {
                if (!myLock) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(
                            Messages.get().getBundle().key(
                                Messages.LOG_DEBUG_TAGREPLACE_LOCK_RESOURCE_1,
                                resource.getRootPath()));
                    }
                    // obtaining the lock:
                    getCms().lockResource(getCms().getRequestContext().removeSiteRoot(resource.getRootPath()));
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(
                            Messages.get().getBundle().key(
                                Messages.LOG_DEBUG_TAGREPLACE_LOCK_RESOURCE_OK_1,
                                resource.getRootPath()));
                    }
                }
            } else {
                // locked by another user:
                if (LOG.isDebugEnabled()) {
                    LOG.debug(
                        Messages.get().getBundle().key(
                            Messages.LOG_DEBUG_TAGREPLACE_RESOURCE_SKIPPED_1,
                            resource.getRootPath()));
                    LOG.debug(Messages.get().getBundle().key(Messages.RPT_TAGREPLACE_SKIP_REASON_LOCKED_0));
                }
                report.print(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_SKIPPED_0),
                    I_CmsReport.FORMAT_WARNING);
                try {
                    CmsUser locker = getCms().readUser(lock.getUserId());
                    report.println(
                        Messages.get().container(Messages.RPT_TAGREPLACE_SKIP_REASON_LOCKED_1, locker.getName()),
                        I_CmsReport.FORMAT_WARNING);
                } catch (Throwable f) {
                    report.println(
                        Messages.get().container(Messages.RPT_TAGREPLACE_SKIP_REASON_ERR_LOCK_0),
                        I_CmsReport.FORMAT_WARNING);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(
                            Messages.get().getBundle().key(
                                Messages.LOG_DEBUG_TAGREPLACE_RESOURCE_SKIPPED_1,
                                resource.getRootPath()));
                        LOG.debug(Messages.get().getBundle().key(Messages.RPT_TAGREPLACE_SKIP_REASON_ERR_LOCK_0));
                    }
                }
                return;
            }
        } catch (CmsException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(
                        Messages.LOG_WARN_TAGREPLACE_LOCK_RESOURCE_FAILED_1,
                        resource.getRootPath()),
                    e);
            }
            report.print(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_SKIPPED_0),
                I_CmsReport.FORMAT_WARNING);
            report.println(
                Messages.get().container(Messages.RPT_TAGREPLACE_SKIP_REASON_LOCKED_0),
                I_CmsReport.FORMAT_WARNING);
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(
                Messages.get().getBundle().key(Messages.LOG_DEBUG_TAGREPLACE_LOAD_FILE_1, resource.getRootPath()));
        }

        CmsFile file = getCms().readFile(resource);

        if (LOG.isDebugEnabled()) {
            LOG.debug(
                Messages.get().getBundle().key(Messages.LOG_DEBUG_TAGREPLACE_LOAD_FILE_OK_1, resource.getRootPath()));
            LOG.debug(
                Messages.get().getBundle().key(Messages.LOG_DEBUG_TAGREPLACE_UNMARSHAL_1, resource.getRootPath()));
        }

        CmsXmlContent xmlcontent = CmsXmlContentFactory.unmarshal(getCms(), file);

        if (LOG.isDebugEnabled()) {
            LOG.debug(
                Messages.get().getBundle().key(Messages.LOG_DEBUG_TAGREPLACE_UNMARSHAL_OK_1, resource.getRootPath()));
        }

        List locales = xmlcontent.getLocales();
        Iterator itLocales = locales.iterator();
        List elements;
        Iterator itElements;
        Locale locale;
        CmsTagReplaceParser parser = new CmsTagReplaceParser(m_settings);
        I_CmsXmlContentValue value;
        int count = 1;
        while (itLocales.hasNext()) {
            locale = (Locale)itLocales.next();
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_DEBUG_TAGREPLACE_LOCALE_1, locale.getLanguage()));
            }

            elements = xmlcontent.getValues(locale);
            itElements = elements.iterator();
            while (itElements.hasNext()) {
                value = (I_CmsXmlContentValue)itElements.next();
                String content = value.getStringValue(getCms());
                if (LOG.isDebugEnabled()) {
                    LOG.debug(
                        Messages.get().getBundle().key(
                            Messages.LOG_DEBUG_TAGREPLACE_ELEMENT_2,
                            value.getPath(),
                            content));
                }
                try {

                    parser.process(content, xmlcontent.getEncoding());
                    value.setStringValue(getCms(), parser.getResult());
                } catch (ParserException e) {
                    CmsMessageContainer container = Messages.get().container(
                        Messages.ERR_TAGREPLACE_PARSE_4,
                        new Object[] {
                            getCms().getRequestContext().removeSiteRoot(resource.getRootPath()),
                            locale.getLanguage(),
                            value.getPath(),
                            parser.getResult()});
                    throw new CmsXmlException(container, e);
                }
            }
            count++;
        }

        if (parser.isChangedContent()) {

            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    Messages.get().getBundle().key(Messages.LOG_DEBUG_TAGREPLACE_MARSHAL_1, resource.getRootPath()));
            }
            byte[] content = xmlcontent.marshal();
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    Messages.get().getBundle().key(Messages.LOG_DEBUG_TAGREPLACE_MARSHAL_OK_1, resource.getRootPath()));
            }

            // write back the modified xmlcontent:
            file.setContents(content);

            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    Messages.get().getBundle().key(Messages.LOG_DEBUG_TAGREPLACE_WRITE_1, resource.getRootPath()));
            }

            getCms().writeFile(file);

            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    Messages.get().getBundle().key(Messages.LOG_DEBUG_TAGREPLACE_WRITE_OK_1, resource.getRootPath()));
            }

            try {
                // set the marker property:

                if (LOG.isDebugEnabled()) {
                    LOG.debug(
                        Messages.get().getBundle().key(
                            Messages.LOG_DEBUG_TAGREPLACE_PROPERTY_WRITE_3,
                            new Object[] {
                                m_markerProperty.getName(),
                                m_markerProperty.getResourceValue(),
                                resource.getRootPath()}));
                }
                getCms().writePropertyObject(
                    getCms().getRequestContext().removeSiteRoot(resource.getRootPath()),
                    m_markerProperty);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_DEBUG_TAGREPLACE_PROPERTY_WRITE_OK_0));
                }
                report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.FORMAT_OK);
            } catch (CmsException e) {
                CmsMessageContainer container = Messages.get().container(
                    Messages.LOG_ERROR_TAGREPLACE_PROPERTY_WRITE_3,
                    new Object[] {
                        m_markerProperty.getName(),
                        m_markerProperty.getResourceValue(),
                        resource.getRootPath()});
                throw new CmsXmlException(container, e);
            }

        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    Messages.get().container(Messages.LOG_DEBUG_TAGREPLACE_UNLOCK_FILE_1, resource.getRootPath()));
            }
            getCms().unlockResource(getCms().getRequestContext().removeSiteRoot(resource.getRootPath()));
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().container(Messages.LOG_DEBUG_TAGREPLACE_UNLOCK_FILE_OK_0));
            }
            report.print(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_SKIPPED_0),
                I_CmsReport.FORMAT_OK);
            report.println(
                Messages.get().container(Messages.RPT_TAGREPLACE_SKIP_REASON_UNMODIFIED_0),
                I_CmsReport.FORMAT_OK);

        }
    }
}
