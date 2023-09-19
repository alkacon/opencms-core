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

package org.opencms.ade.detailpage;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.configuration.CmsADEManager;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.logging.Log;

/**
 * This class uses information from the detail page information stored in the sitemap to find/recognize the detail pages for
 * a given resource.<p>
 *
 * @since 8.0.0
 */
public class CmsDefaultDetailPageHandler implements I_CmsDetailPageHandler {

    /**
     * Helper class for storing intermediate results when looking up detail pages for a resource.
     */
    private static class DetailPageConfigData {

        /** The link source config. */
        private CmsADEConfigData m_sourceConfig;

        /** The configuration whose detail pages are used. */
        private CmsADEConfigData m_configForDetailPages;

        /** The link target configuration. */
        private CmsADEConfigData m_targetConfig;

        /** The detail pages. */
        private List<CmsDetailPageInfo> m_detailPages = new ArrayList<>();

        /**
         * Gets the config for detail pages.
         *
         * @return the config for detail pages
         */
        public CmsADEConfigData getConfigForDetailPages() {

            return m_configForDetailPages;
        }

        /**
         * Gets the detail pages.
         *
         * @return the detail pages
         */
        public List<CmsDetailPageInfo> getDetailPages() {

            return m_detailPages;
        }

        /**
         * Gets the source config.
         *
         * @return the source config
         */
        @SuppressWarnings("unused")
        public CmsADEConfigData getSourceConfig() {

            return m_sourceConfig;
        }

        /**
         * Gets the target config.
         *
         * @return the target config
         */
        @SuppressWarnings("unused")
        public CmsADEConfigData getTargetConfig() {

            return m_targetConfig;
        }

        /**
         * Sets the config for detail pages.
         *
         * @param configForDetailPages the new config for detail pages
         */
        public void setConfigForDetailPages(CmsADEConfigData configForDetailPages) {

            m_configForDetailPages = configForDetailPages;
        }

        /**
         * Sets the detail pages.
         *
         * @param detailPages the new detail pages
         */
        public void setDetailPages(List<CmsDetailPageInfo> detailPages) {

            m_detailPages = detailPages;
        }

        /**
         * Sets the source config.
         *
         * @param sourceConfig the new source config
         */
        public void setSourceConfig(CmsADEConfigData sourceConfig) {

            m_sourceConfig = sourceConfig;
        }

        /**
         * Sets the target config.
         *
         * @param targetConfig the new target config
         */
        public void setTargetConfig(CmsADEConfigData targetConfig) {

            m_targetConfig = targetConfig;
        }

    }

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDefaultDetailPageHandler.class);

    /** The configuration. */
    private CmsParameterConfiguration m_config = new CmsParameterConfiguration();

    /**
     * Constructor.
     */
    public CmsDefaultDetailPageHandler() {

        // just for debugging
        LOG.debug("Created default detail page handler.");
    }

    /**
     * Adds the configuration parameter.
     *
     * @param paramName the param name
     * @param paramValue the param value
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        m_config.add(paramName, paramValue);

    }

    /**
     * Gets the all detail pages.
     *
     * @param cms the cms
     * @param resType the res type
     * @return the all detail pages
     * @throws CmsException the cms exception
     * @see org.opencms.ade.detailpage.I_CmsDetailPageHandler#getAllDetailPages(org.opencms.file.CmsObject, int)
     */
    public Collection<String> getAllDetailPages(CmsObject cms, int resType) throws CmsException {

        if (!OpenCms.getADEManager().isInitialized()) {
            return new ArrayList<String>();
        }
        String typeName = OpenCms.getResourceManager().getResourceType(resType).getTypeName();
        return OpenCms.getADEManager().getDetailPages(cms, typeName);
    }

    /**
     * Gets the configuration.
     *
     * @return the configuration
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public CmsParameterConfiguration getConfiguration() {

        return m_config;

    }

    /**
     * Gets the detail page for a content element.<p>
     *
     * @param manager the ADE manager instance.
     * @param cms the CMS context
     * @param contentRootPath the element's root path
     * @param originPath the site path from which the detail content is being linked
     * @param targetDetailPage the target detail page to use
     *
     * @return the detail page for the content element
     */
    public String getDetailPage(
        CmsADEManager manager,
        CmsObject cms,
        String contentRootPath,
        String originPath,
        String targetDetailPage) {

        boolean online = cms.getRequestContext().getCurrentProject().isOnlineProject();
        String resType = manager.getParentFolderType(online, contentRootPath);
        // resType may not actually be the resource type of the resource at contentRootPath. We determine
        // the actual resource type further below, but if getParentFolderType() returns null here, we can stop
        // without reading any resources.
        if (resType == null) {
            return null;
        }

        if (targetDetailPage != null) {
            try {
                CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(targetDetailPage);
                CmsResource targetDetailPageRes = null;
                if (site != null) {
                    CmsObject rootCms = OpenCms.initCmsObject(cms);
                    rootCms.getRequestContext().setSiteRoot("");
                    targetDetailPageRes = rootCms.readResource(targetDetailPage);
                } else {
                    targetDetailPageRes = cms.readResource(targetDetailPage);
                }
                if (manager.isDetailPage(cms, targetDetailPageRes)) {
                    return targetDetailPageRes.getRootPath();
                }
            } catch (CmsVfsResourceNotFoundException e) {
                LOG.debug(e.getLocalizedMessage(), e);
            } catch (Exception e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
        }

        try {
            CmsObject rootCms = OpenCms.initCmsObject(cms);
            rootCms.getRequestContext().setSiteRoot("");
            CmsResource detailResource = rootCms.readResource(contentRootPath);
            resType = OpenCms.getResourceManager().getResourceType(detailResource).getTypeName();
        } catch (CmsVfsResourceNotFoundException e) {
            LOG.info(e.getLocalizedMessage(), e);
        } catch (Exception e) {
            LOG.warn(e.getLocalizedMessage(), e);
        }
        DetailPageConfigData context = lookupDetailPageConfigData(manager, cms, contentRootPath, originPath, resType);
        List<CmsDetailPageInfo> relevantPages = context.getDetailPages();
        if (context.getConfigForDetailPages() == null) {
            return null;
        }
        LOG.info(
            "Trying to determine detail page for '"
                + contentRootPath
                + "' in context '"
                + context.getConfigForDetailPages().getBasePath()
                + "'");
        if (!CmsStringUtil.isPrefixPath(
            context.getConfigForDetailPages().getExternalDetailContentExclusionFolder(),
            contentRootPath)) {
            return null;
        }
        String result = new CmsDetailPageFilter(cms, contentRootPath).filterDetailPages(relevantPages).map(
            info -> info.getUri()).findFirst().orElse(null);
        return result;
    }

    /**
     * Gets the detail page to use for a detail resource.
     *
     * @param cms the cms
     * @param rootPath the root path
     * @param linkSource the link source
     * @param targetDetailPage the target detail page
     * @return the detail page
     * @see org.opencms.ade.detailpage.I_CmsDetailPageHandler#getDetailPage(org.opencms.file.CmsObject, java.lang.String, java.lang.String, java.lang.String)
     */
    public String getDetailPage(CmsObject cms, String rootPath, String linkSource, String targetDetailPage) {

        CmsADEManager manager = OpenCms.getADEManager();
        if (!manager.isInitialized()) {
            return null;
        }

        if (rootPath.endsWith(".jsp") || rootPath.startsWith(CmsWorkplace.VFS_PATH_WORKPLACE)) {
            // exclude these for performance reasons
            return null;
        }
        String result = getDetailPage(manager, cms, rootPath, linkSource, targetDetailPage);
        if (result == null) {
            return null;
        }
        if (!CmsResource.isFolder(result)) {
            result = CmsResource.getFolderPath(result);
        }
        return result;
    }

    /**
     * Inits the configuration.
     *
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() {

        m_config = CmsParameterConfiguration.unmodifiableVersion(m_config);

    }

    /**
     * Initialize.
     *
     * @param offlineCms the offline cms
     * @param onlineCms the online cms
     * @see org.opencms.ade.detailpage.I_CmsDetailPageHandler#initialize(org.opencms.file.CmsObject, org.opencms.file.CmsObject)
     */
    public void initialize(CmsObject offlineCms, CmsObject onlineCms) {

        // do nothing
    }

    /**
     * Checks whether the given detail page is valid for the given resource.<p>
     *
     * @param cms the CMS context
     * @param page the detail page
     * @param detailRes the detail resource
     *
     * @return true if the given detail page is valid
     */
    public boolean isValidDetailPage(CmsObject cms, CmsResource page, CmsResource detailRes) {

        String p = "[" + RandomStringUtils.randomAlphanumeric(6) + "] ";
        LOG.debug(p + "isValidDetailPage(" + page.getRootPath() + "," + detailRes.getRootPath() + ")");
        if (OpenCms.getSystemInfo().isRestrictDetailContents()) {
            // in 'restrict detail contents mode', do not allow detail contents from a real site on a detail page of a different real site
            CmsSite pageSite = OpenCms.getSiteManager().getSiteForRootPath(page.getRootPath());
            CmsSite detailSite = OpenCms.getSiteManager().getSiteForRootPath(detailRes.getRootPath());
            if ((pageSite != null)
                && (detailSite != null)
                && !pageSite.getSiteRoot().equals(detailSite.getSiteRoot())) {
                LOG.debug(p + "returned false because of restrict-detail-contents option");
                return false;
            }
        }

        if (!OpenCms.getADEManager().isDetailPage(cms, page)) {
            LOG.debug(p + "returned false because the page is not a detail page.");
            return false;
        }

        String typeName = OpenCms.getResourceManager().getResourceType(detailRes).getTypeName();
        DetailPageConfigData context = lookupDetailPageConfigData(
            OpenCms.getADEManager(),
            cms,
            detailRes.getRootPath(),
            cms.getSitePath(page),
            typeName);
        String pageFolder = CmsFileUtil.removeTrailingSeparator(CmsResource.getParentFolder(page.getRootPath()));
        CmsDetailPageFilter detailPageFilter = new CmsDetailPageFilter(cms, detailRes);
        boolean foundDetailPage = detailPageFilter.filterDetailPages(context.getDetailPages()).anyMatch(
            info -> pageFolder.equals(CmsFileUtil.removeTrailingSeparator(info.getUri())));
        CmsADEConfigData configForPage = context.getConfigForDetailPages();
        if (configForPage == null) {
            LOG.debug(p + "Returned false because no valid sitemap configuration found");
            return false;
        }
        if (!foundDetailPage) {
            LOG.debug(p + "Returned false because detail page is not in context " + configForPage.getBasePath());
            return false;
        }
        if (!CmsStringUtil.isPrefixPath(
            configForPage.getExternalDetailContentExclusionFolder(),
            detailRes.getRootPath())) {
            LOG.debug(
                p
                    + "returned false because of external detail content exclusion folder "
                    + configForPage.getExternalDetailContentExclusionFolder());
            return false;
        }
        return true;
    }

    /**
     * Gets the context.
     *
     * @param manager the manager
     * @param cms the cms
     * @param contentPath the content path
     * @param originPath the origin path
     * @param resType the res type
     * @return the context
     */
    private DetailPageConfigData lookupDetailPageConfigData(
        CmsADEManager manager,
        CmsObject cms,
        String contentPath,
        String originPath,
        String resType) {

        DetailPageConfigData context = new DetailPageConfigData();

        CmsADEConfigData configData = manager.lookupConfigurationWithCache(
            cms,
            cms.getRequestContext().addSiteRoot(originPath));
        context.setSourceConfig(configData);
        CmsADEConfigData targetConfigData = manager.lookupConfigurationWithCache(cms, contentPath);
        context.setTargetConfig(targetConfigData);
        boolean targetFirst = targetConfigData.isPreferDetailPagesForLocalContents();
        List<CmsADEConfigData> configs = targetFirst
        ? Arrays.asList(targetConfigData, configData)
        : Arrays.asList(configData, targetConfigData);
        for (CmsADEConfigData config : configs) {
            List<CmsDetailPageInfo> pageInfo = config.getDetailPagesForType(resType);
            if ((pageInfo != null) && !pageInfo.isEmpty()) {
                context.setConfigForDetailPages(config);
                context.setDetailPages(pageInfo);
                break;
            }
        }
        return context;
    }

}
