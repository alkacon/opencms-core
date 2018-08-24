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

package org.opencms.xml.templatemapper;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;
import org.opencms.ui.Messages;
import org.opencms.util.CmsExpiringValue;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.CmsGroupContainerBean;
import org.opencms.xml.containerpage.CmsXmlContainerPage;
import org.opencms.xml.containerpage.CmsXmlContainerPageFactory;
import org.opencms.xml.containerpage.CmsXmlGroupContainer;
import org.opencms.xml.containerpage.CmsXmlGroupContainerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

/**
 * Report thread for rewriting pages in a folder according to a given template mapper configuration.<p>
 */
public class CmsTemplateMappingContentRewriter extends A_CmsReportThread {

    /** Cache for the status. */
    private static CmsExpiringValue<Boolean> m_moduleCheckCache = new CmsExpiringValue<>(2000);

    /** The logger instance for the class. */
    private static final Log LOG = CmsLog.getLog(CmsTemplateMappingContentRewriter.class);

    /** The folder path. */
    private String m_folder;

    /** The folder resource. */
    private CmsResource m_folderRes;

    /**
     * Creates a new instance.<p>
     *
     * @param cms the CMS context
     * @param folder the folder to process
     *
     * @throws CmsException if something goes wrong
     */
    protected CmsTemplateMappingContentRewriter(CmsObject cms, CmsResource folder)
    throws CmsException {

        super(
            OpenCms.initCmsObject(cms),
            CmsTemplateMappingContentRewriter.class.getName() + "-" + Thread.currentThread().getId());
        m_folder = cms.getSitePath(folder);
        m_folderRes = folder;
        initHtmlReport(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms));
    }

    /**
     * Checks if template mapper is configured in modules.
     *
     * @return true if the template mapper is configured in modules
     */
    public static boolean checkConfiguredInModules() {

        Boolean result = m_moduleCheckCache.get();
        if (result == null) {
            result = Boolean.valueOf(getConfiguredTemplateMapping() != null);
            m_moduleCheckCache.set(result);
        }
        return result.booleanValue();
    }

    /**
     * Tries to read the path to the template mapping file from module parameters.<p>
     *
     * @return the template mapping file path
     */
    public static String getConfiguredTemplateMapping() {

        String param = null;
        for (CmsModule module : OpenCms.getModuleManager().getAllInstalledModules()) {
            param = module.getParameter("template-mapping");
            if (param != null) {
                break;
            }
        }
        return param;
    }

    /**
     * @see org.opencms.report.A_CmsReportThread#getReportUpdate()
     */
    @Override
    public String getReportUpdate() {

        return getReport().getReportUpdate();
    }

    /**
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {

        CmsObject cms = getCms();
        I_CmsReport report = getReport();
        try {
            String file = getConfiguredTemplateMapping();
            cms.readResource(file);
            CmsLockUtil.ensureLock(cms, m_folderRes);
            CmsTemplateMapper mapper = new CmsTemplateMapper(file);
            mapper.setForSave(true);
            List<CmsResource> pages = resourcesForType(m_folder, CmsResourceTypeXmlContainerPage.getStaticTypeName());
            int j = 0;
            for (CmsResource page : pages) {
                j++;
                try {
                    report.println(
                        message(
                            Messages.RPT_TEMPLATEMAPPER_PROCESSING_PAGE_2,
                            "" + j + "/" + pages.size(),
                            page.getRootPath()));
                    CmsXmlContainerPage pageXml = CmsXmlContainerPageFactory.unmarshal(cms, cms.readFile(page));
                    CmsContainerPageBean bean = pageXml.getContainerPage(cms);
                    CmsContainerPageBean transformedBean = mapper.transformContainerpageBean(
                        cms,
                        bean,
                        page.getRootPath());
                    pageXml.save(cms, transformedBean);
                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage(), e);
                    getReport().println(e);
                }

            }

            List<CmsResource> groups = resourcesForType(
                m_folder,
                CmsResourceTypeXmlContainerPage.GROUP_CONTAINER_TYPE_NAME);
            j = 0;
            for (CmsResource group : groups) {
                try {
                    j++;
                    report.println(
                        message(
                            Messages.RPT_TEMPLATEMAPPER_PROCESSING_GROUP_2,
                            "" + j + "/" + groups.size(),
                            group.getRootPath()));
                    CmsXmlGroupContainer groupXml = CmsXmlGroupContainerFactory.unmarshal(cms, cms.readFile(group));
                    CmsGroupContainerBean groupContainer = groupXml.getGroupContainer(cms);
                    CmsGroupContainerBean transformedContainer = mapper.transformGroupContainer(
                        cms,
                        groupContainer,
                        group.getRootPath());

                    groupXml.save(cms, transformedContainer, Locale.ENGLISH);
                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage(), e);
                    getReport().println(e);
                }
            }
            CmsLockUtil.tryUnlock(cms, m_folderRes);
            report.println(message(Messages.RPT_TEMPLATEMAPPER_DONE_0));
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            getReport().println(e);
        }

    }

    /**
     * Gets a message container.<p>
     *
     * @param key the key
     * @param args the message parameters
     *
     * @return the message container
     */
    CmsMessageContainer message(String key, String... args) {

        return Messages.get().container(key, args);

    }

    /**
     * Reads resources of some type in the given folder.<p>
     *
     * @param folder the folder
     * @param name the type name
     * @return the list of resources of the given type in the given folder
     */
    private List<CmsResource> resourcesForType(String folder, String name) {

        try {
            CmsObject cms = getCms();
            I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(name);
            CmsResourceFilter filter = CmsResourceFilter.IGNORE_EXPIRATION.addRequireType(type);
            return cms.readResources(folder, filter);
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            getReport().println(e);
            return Collections.emptyList();
        }

    }

}
