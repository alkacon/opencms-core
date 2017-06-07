/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.site.xmlsitemap;

import org.opencms.ade.detailpage.CmsDetailPageInfo;
import org.opencms.db.CmsAlias;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.gwt.shared.alias.CmsAliasMode;
import org.opencms.jsp.CmsJspNavBuilder;
import org.opencms.jsp.CmsJspNavElement;
import org.opencms.loader.CmsLoaderException;
import org.opencms.loader.CmsResourceManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Class for generating XML sitemaps for SEO purposes, as described in
 * <a href="http://www.sitemaps.org/protocol.html">http://www.sitemaps.org/protocol.html</a>.<p>
 */
public class CmsXmlSitemapGenerator {

    /**
     * A bean that consists of a sitemap URL bean and a priority score, to determine which of multiple entries with the same
     * URL are to be preferred.<p>
     */
    protected class ResultEntry {

        /** Internal priority to determine which of multiple entries with the same URL is used.
         * Note that this has nothing to do with the priority in the URL bean itself!
         */
        private int m_priority;

        /** The URL bean. */
        private CmsXmlSitemapUrlBean m_urlBean;

        /**
         * Creates a new result entry.<p>
         *
         * @param urlBean the url bean
         *
         * @param priority the internal priority
         */
        public ResultEntry(CmsXmlSitemapUrlBean urlBean, int priority) {

            m_priority = priority;
            m_urlBean = urlBean;
        }

        /**
         * Gets the internal priority used to determine which of multiple entries with the same URL to use.<p>
         * This has nothing to do with the priority defined in the URL beans themselves!
         *
         * @return the internal priority
         */
        public int getPriority() {

            return m_priority;
        }

        /**
         * Gets the URL bean.<p>
         *
         * @return the URL bean
         */
        public CmsXmlSitemapUrlBean getUrlBean() {

            return m_urlBean;
        }
    }

    /** The default change frequency. */
    public static final String DEFAULT_CHANGE_FREQUENCY = "daily";

    /** The default priority. */
    public static final double DEFAULT_PRIORITY = 0.5;

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsXmlSitemapGenerator.class);

    /** The root path for the sitemap root folder. */
    protected String m_baseFolderRootPath;

    /** The site path of the base folder. */
    protected String m_baseFolderSitePath;

    /** Flag to control whether container page dates should be computed. */
    protected boolean m_computeContainerPageDates;

    /** The list of detail page info beans. */
    protected List<CmsDetailPageInfo> m_detailPageInfos = new ArrayList<CmsDetailPageInfo>();

    /** A map from type names to lists of potential detail resources of that type. */
    protected Map<String, List<CmsResource>> m_detailResources = new HashMap<String, List<CmsResource>>();

    /** A multimap from detail page root paths to corresponding types. */
    protected Multimap<String, String> m_detailTypesByPage = ArrayListMultimap.create();

    /** A CMS context with guest privileges. */
    protected CmsObject m_guestCms;

    /** The include/exclude configuration used for choosing pages for the XML sitemap. */
    protected CmsPathIncludeExcludeSet m_includeExcludeSet = new CmsPathIncludeExcludeSet();

    /** A map from structure ids to page aliases below the base folder which point to the given structure id. */
    protected Multimap<CmsUUID, CmsAlias> m_pageAliasesBelowBaseFolderByStructureId = ArrayListMultimap.create();

    /** The map used for storing the results, with URLs as keys. */
    protected Map<String, ResultEntry> m_resultMap = new LinkedHashMap<String, ResultEntry>();

    /** A guest user CMS object with the site root of the base folder. */
    protected CmsObject m_siteGuestCms;

    /** The site root of the base folder. */
    protected String m_siteRoot;

    /** A link to the site root. */
    protected String m_siteRootLink;

    /** Configured replacement server URL. */
    private String m_serverUrl;

    /**
     * Creates a new sitemap generator instance.<p>
     *
     * @param folderRootPath the root folder for the XML sitemap to generate
     *
     * @throws CmsException if something goes wrong
     */
    public CmsXmlSitemapGenerator(String folderRootPath)
    throws CmsException {

        m_baseFolderRootPath = CmsFileUtil.removeTrailingSeparator(folderRootPath);
        m_guestCms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserGuest());
        m_siteGuestCms = OpenCms.initCmsObject(m_guestCms);
        CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(CmsStringUtil.joinPaths(folderRootPath, "/"));
        m_siteRoot = site.getSiteRoot();

        m_siteGuestCms.getRequestContext().setSiteRoot(m_siteRoot);
        m_baseFolderSitePath = CmsStringUtil.joinPaths(
            "/",
            m_siteGuestCms.getRequestContext().removeSiteRoot(m_baseFolderRootPath));
    }

    /**
     * Replaces the protocol/host/port of a link with the ones from the given server URI, if it's not empty.<p>
     *
     * @param link the link to change
     * @param server the server URI string
    
     * @return the changed link
     */
    public static String replaceServerUri(String link, String server) {

        String serverUriStr = server;

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(serverUriStr)) {
            return link;
        }
        try {
            URI serverUri = new URI(serverUriStr);
            URI linkUri = new URI(link);
            URI result = new URI(
                serverUri.getScheme(),
                serverUri.getAuthority(),
                linkUri.getPath(),
                linkUri.getQuery(),
                linkUri.getFragment());
            return result.toString();
        } catch (URISyntaxException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return link;
        }

    }

    /**
     * Gets the change frequency for a sitemap entry from a list of properties.<p>
     *
     * If the change frequency is not defined in the properties, this method will return null.<p>
     *
     * @param properties the properties from which the change frequency should be obtained
     *
     * @return the change frequency string
     */
    protected static String getChangeFrequency(List<CmsProperty> properties) {

        CmsProperty prop = CmsProperty.get(CmsPropertyDefinition.PROPERTY_XMLSITEMAP_CHANGEFREQ, properties);
        if (prop.isNullProperty()) {
            return null;
        }
        String result = prop.getValue().trim();
        return result;
    }

    /**
     * Gets the page priority from a list of properties.<p>
     *
     * If the page priority can't be found among the properties, -1 will be returned.<p>
     *
     * @param properties the properties of a resource
     *
     * @return the page priority read from the properties, or -1
     */
    protected static double getPriority(List<CmsProperty> properties) {

        CmsProperty prop = CmsProperty.get(CmsPropertyDefinition.PROPERTY_XMLSITEMAP_PRIORITY, properties);
        if (prop.isNullProperty()) {
            return -1.0;
        }
        try {
            double result = Double.parseDouble(prop.getValue().trim());
            return result;
        } catch (NumberFormatException e) {
            return -1.0;
        }
    }

    /**
     * Removes files marked as internal from a resource list.<p>
     *
     * @param resources the list which should be replaced
     */
    protected static void removeInternalFiles(List<CmsResource> resources) {

        Iterator<CmsResource> iter = resources.iterator();
        while (iter.hasNext()) {
            CmsResource resource = iter.next();
            if (resource.isInternal()) {
                iter.remove();
            }
        }
    }

    /**
     * Generates a list of XML sitemap entry beans for the root folder which has been set in the constructor.<p>
     *
     * @return the list of XML sitemap entries
     *
     * @throws CmsException if something goes wrong
     */
    public List<CmsXmlSitemapUrlBean> generateSitemapBeans() throws CmsException {

        String baseSitePath = m_siteGuestCms.getRequestContext().removeSiteRoot(m_baseFolderRootPath);
        initializeFileData(baseSitePath);
        for (CmsResource resource : getDirectPages()) {
            String sitePath = m_siteGuestCms.getSitePath(resource);
            List<CmsProperty> propertyList = m_siteGuestCms.readPropertyObjects(resource, true);
            String onlineLink = OpenCms.getLinkManager().getOnlineLink(m_siteGuestCms, sitePath);
            boolean isContainerPage = CmsResourceTypeXmlContainerPage.isContainerPage(resource);
            long dateModified = resource.getDateLastModified();
            if (isContainerPage) {
                if (m_computeContainerPageDates) {
                    dateModified = computeContainerPageModificationDate(resource);
                } else {
                    dateModified = -1;
                }
            }
            CmsXmlSitemapUrlBean urlBean = new CmsXmlSitemapUrlBean(
                replaceServerUri(onlineLink),
                dateModified,
                getChangeFrequency(propertyList),
                getPriority(propertyList));
            urlBean.setOriginalResource(resource);
            addResult(urlBean, 3);
            if (isContainerPage) {
                Locale locale = getLocale(resource, propertyList);
                addDetailLinks(resource, locale);
            }
        }

        for (CmsUUID aliasStructureId : m_pageAliasesBelowBaseFolderByStructureId.keySet()) {
            addAliasLinks(aliasStructureId);
        }

        List<CmsXmlSitemapUrlBean> result = new ArrayList<CmsXmlSitemapUrlBean>();
        for (ResultEntry resultEntry : m_resultMap.values()) {
            result.add(resultEntry.getUrlBean());
        }
        return result;
    }

    /**
     * Gets the include/exclude configuration of this XML sitemap generator.<p>
     *
     * @return the include/exclude configuration
     */
    public CmsPathIncludeExcludeSet getIncludeExcludeSet() {

        return m_includeExcludeSet;
    }

    /**
     * Generates a sitemap and formats it as a string.<p>
     *
     * @return the sitemap XML data
     *
     * @throws CmsException if something goes wrong
     */
    public String renderSitemap() throws CmsException {

        StringBuffer buffer = new StringBuffer();
        List<CmsXmlSitemapUrlBean> urlBeans = generateSitemapBeans();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        buffer.append(getUrlSetOpenTag() + "\n");
        for (CmsXmlSitemapUrlBean bean : urlBeans) {
            buffer.append(getXmlForEntry(bean));
            buffer.append("\n");
        }
        buffer.append("</urlset>");
        return buffer.toString();
    }

    /**
     * Enables or disables computation of container page dates.<p>
     *
     * @param computeContainerPageDates the new value
     */
    public void setComputeContainerPageDates(boolean computeContainerPageDates) {

        m_computeContainerPageDates = computeContainerPageDates;
    }

    /**
     * Sets the replacement server URL.<p>
     *
     * The replacement server URL will replace the scheme/host/port from the URLs returned by getOnlineLink.
     *
     * @param serverUrl the server URL
     */
    public void setServerUrl(String serverUrl) {

        m_serverUrl = serverUrl;
    }

    /**
     * Adds the detail page links for a given page to the results.<p>
     *
     * @param containerPage the container page resource
     * @param locale the locale of the container page
     *
     * @throws CmsException if something goes wrong
     */
    protected void addDetailLinks(CmsResource containerPage, Locale locale) throws CmsException {

        List<I_CmsResourceType> types = getDetailTypesForPage(containerPage);
        for (I_CmsResourceType type : types) {
            List<CmsResource> resourcesForType = getDetailResources(type);
            for (CmsResource detailRes : resourcesForType) {
                if (!isValidDetailPageCombination(containerPage, locale, detailRes)) {
                    continue;
                }
                List<CmsProperty> detailProps = m_guestCms.readPropertyObjects(detailRes, true);
                String detailLink = getDetailLink(containerPage, detailRes, locale);
                detailLink = CmsFileUtil.removeTrailingSeparator(detailLink);
                CmsXmlSitemapUrlBean detailUrlBean = new CmsXmlSitemapUrlBean(
                    replaceServerUri(detailLink),
                    detailRes.getDateLastModified(),
                    getChangeFrequency(detailProps),
                    getPriority(detailProps));
                detailUrlBean.setOriginalResource(detailRes);
                detailUrlBean.setDetailPageResource(containerPage);
                addResult(detailUrlBean, 2);
            }
        }
    }

    /**
     * Adds an URL bean to the internal map of results, but only if there is no existing entry with higher internal priority
     * than the priority given as an argument.<p>
     *
     * @param result the result URL bean to add
     *
     * @param resultPriority the internal priority to use for updating the map of results
     */
    protected void addResult(CmsXmlSitemapUrlBean result, int resultPriority) {

        String url = CmsFileUtil.removeTrailingSeparator(result.getUrl());
        boolean writeEntry = true;
        if (m_resultMap.containsKey(url)) {
            LOG.warn("Encountered duplicate URL with while generating sitemap: " + result.getUrl());
            ResultEntry entry = m_resultMap.get(url);
            writeEntry = entry.getPriority() <= resultPriority;
        }
        if (writeEntry) {
            m_resultMap.put(url, new ResultEntry(result, resultPriority));
        }
    }

    /**
     * Computes the container the container page modification date from its referenced contents.<p>
     *
     * @param containerPage the container page
     *
     * @return the computed modification date
     *
     * @throws CmsException if something goes wrong
     */
    protected long computeContainerPageModificationDate(CmsResource containerPage) throws CmsException {

        CmsRelationFilter filter = CmsRelationFilter.relationsFromStructureId(
            containerPage.getStructureId()).filterType(CmsRelationType.XML_STRONG);
        List<CmsRelation> relations = m_guestCms.readRelations(filter);
        long result = containerPage.getDateLastModified();
        for (CmsRelation relation : relations) {
            try {
                CmsResource target = relation.getTarget(
                    m_guestCms,
                    CmsResourceFilter.DEFAULT_FILES.addRequireVisible());
                long targetDate = target.getDateLastModified();
                if (targetDate > result) {
                    result = targetDate;
                }
            } catch (CmsException e) {
                LOG.warn(
                    "Could not get relation target for relation "
                        + relation.toString()
                        + " | "
                        + e.getLocalizedMessage(),
                    e);
            }
        }

        return result;
    }

    /**
     * Gets the detail link for a given container page and detail content.<p>
     *
     * @param pageRes the container page
     * @param detailRes the detail content
     * @param locale the locale for which we want the link
     *
     * @return the detail page link
     */
    protected String getDetailLink(CmsResource pageRes, CmsResource detailRes, Locale locale) {

        String pageSitePath = m_siteGuestCms.getSitePath(pageRes);
        String detailSitePath = m_siteGuestCms.getSitePath(detailRes);
        CmsRequestContext requestContext = m_siteGuestCms.getRequestContext();
        String originalUri = requestContext.getUri();
        Locale originalLocale = requestContext.getLocale();
        try {
            requestContext.setUri(pageSitePath);
            requestContext.setLocale(locale);
            return OpenCms.getLinkManager().getOnlineLink(m_siteGuestCms, detailSitePath, true);
        } finally {
            requestContext.setUri(originalUri);
            requestContext.setLocale(originalLocale);
        }
    }

    /**
     * Gets the types for which a given resource is configured as a detail page.<p>
     *
     * @param resource a resource for which we want to find the detail page types
     *
     * @return the list of resource types for which the given page is configured as a detail page
     */
    protected List<I_CmsResourceType> getDetailTypesForPage(CmsResource resource) {

        Collection<String> typesForPage = m_detailTypesByPage.get(resource.getRootPath());
        String parentPath = CmsFileUtil.removeTrailingSeparator(CmsResource.getParentFolder(resource.getRootPath()));
        Collection<String> typesForFolder = m_detailTypesByPage.get(parentPath);
        Set<String> allTypes = new HashSet<String>();
        allTypes.addAll(typesForPage);
        allTypes.addAll(typesForFolder);
        List<I_CmsResourceType> resTypes = new ArrayList<I_CmsResourceType>();
        CmsResourceManager resMan = OpenCms.getResourceManager();
        for (String typeName : allTypes) {
            try {
                I_CmsResourceType resType = resMan.getResourceType(typeName);
                resTypes.add(resType);
            } catch (CmsLoaderException e) {
                LOG.warn("Invalid resource type name" + typeName + "! " + e.getLocalizedMessage(), e);
            }
        }
        return resTypes;
    }

    /**
     * Gets the list of pages which should be directly added to the XML sitemap.<p>
     *
     * @return the list of resources which should be directly added to the XML sitemap
     *
     * @throws CmsException if something goes wrong
     */
    protected List<CmsResource> getDirectPages() throws CmsException {

        List<CmsResource> result = new ArrayList<CmsResource>();
        result.addAll(getNavigationPages());
        Set<String> includeRoots = m_includeExcludeSet.getIncludeRoots();
        for (String includeRoot : includeRoots) {
            try {
                CmsResource resource = m_guestCms.readResource(includeRoot);
                if (resource.isFile()) {
                    result.add(resource);
                } else {
                    List<CmsResource> subtreeFiles = m_guestCms.readResources(
                        includeRoot,
                        CmsResourceFilter.DEFAULT_FILES,
                        true);
                    result.addAll(subtreeFiles);
                }
            } catch (CmsVfsResourceNotFoundException e) {
                LOG.warn("Could not read include resource: " + includeRoot);
            }
        }
        Iterator<CmsResource> filterIter = result.iterator();
        while (filterIter.hasNext()) {
            CmsResource currentResource = filterIter.next();
            if (currentResource.isInternal() || m_includeExcludeSet.isExcluded(currentResource.getRootPath())) {
                filterIter.remove();
            }
        }
        return result;
    }

    /**
     * Writes the inner node content for an url element to a buffer.<p>
     *
     * @param entry the entry for which the content should be written
     * @return the inner XML
     */
    protected String getInnerXmlForEntry(CmsXmlSitemapUrlBean entry) {

        StringBuffer buffer = new StringBuffer();
        entry.writeElement(buffer, "loc", entry.getUrl());
        entry.writeLastmod(buffer);
        entry.writeChangefreq(buffer);
        entry.writePriority(buffer);
        return buffer.toString();
    }

    /**
     * Gets the list of pages from the navigation which should be directly added to the XML sitemap.<p>
     *
     * @return the list of pages to add to the XML sitemap
     */
    protected List<CmsResource> getNavigationPages() {

        List<CmsResource> result = new ArrayList<CmsResource>();
        CmsJspNavBuilder navBuilder = new CmsJspNavBuilder(m_siteGuestCms);
        try {
            CmsResource rootDefaultFile = m_siteGuestCms.readDefaultFile(
                m_siteGuestCms.getRequestContext().removeSiteRoot(m_baseFolderRootPath),
                CmsResourceFilter.DEFAULT);
            if (rootDefaultFile != null) {
                result.add(rootDefaultFile);
            }
        } catch (Exception e) {
            LOG.info(e.getLocalizedMessage(), e);
        }
        List<CmsJspNavElement> navElements = navBuilder.getSiteNavigation(m_baseFolderSitePath, -1);
        for (CmsJspNavElement navElement : navElements) {
            CmsResource navResource = navElement.getResource();
            if (navResource.isFolder()) {
                try {
                    CmsResource defaultFile = m_guestCms.readDefaultFile(navResource, CmsResourceFilter.DEFAULT_FILES);
                    if (defaultFile != null) {
                        result.add(defaultFile);
                    } else {
                        LOG.warn("Could not get default file for " + navResource.getRootPath());
                    }
                } catch (CmsException e) {
                    LOG.warn("Could not get default file for " + navResource.getRootPath());
                }
            } else {
                result.add(navResource);
            }
        }
        return result;
    }

    /**
     * Gets the opening tag for the urlset element (can be overridden to add e.g. more namespaces.<p>
     *
     * @return the opening tag
     */
    protected String getUrlSetOpenTag() {

        return "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">";
    }

    /**
     * Writes the XML for an URL entry to a buffer.<p>
     *
     * @param entry the XML sitemap entry bean
     *
     * @return an XML representation of this bean
     */
    protected String getXmlForEntry(CmsXmlSitemapUrlBean entry) {

        StringBuffer buffer = new StringBuffer();
        buffer.append("<url>");
        buffer.append(getInnerXmlForEntry(entry));
        buffer.append("</url>");
        return buffer.toString();
    }

    /**
     * Checks whether the given alias is below the base folder.<p>
     *
     * @param alias the alias to check
     *
     * @return true if the alias is below the base folder
     */
    protected boolean isAliasBelowBaseFolder(CmsAlias alias) {

        boolean isBelowBaseFolder = CmsStringUtil.isPrefixPath(m_baseFolderSitePath, alias.getAliasPath());
        return isBelowBaseFolder;
    }

    /**
     * Replaces the protocol/host/port of a link with the ones from the configured server URI, if it's not empty.<p>
     *
     * @param link the link to change
     *
     * @return the changed link
     */
    protected String replaceServerUri(String link) {

        return replaceServerUri(link, m_serverUrl);
    }

    /**
     * Adds the alias links for a given structure id to the results.<p>
     *
     * @param aliasStructureId the alias target structure id
     */
    private void addAliasLinks(CmsUUID aliasStructureId) {

        try {
            CmsResource aliasTarget = m_guestCms.readResource(aliasStructureId);
            List<CmsProperty> properties = m_guestCms.readPropertyObjects(aliasTarget, true);
            double priority = getPriority(properties);
            String changeFrequency = getChangeFrequency(properties);
            Collection<CmsAlias> aliases = m_pageAliasesBelowBaseFolderByStructureId.get(aliasStructureId);
            for (CmsAlias alias : aliases) {
                String aliasLink = (m_siteRootLink + "/" + alias.getAliasPath()).replaceAll("(?<!:)//+", "/");
                CmsXmlSitemapUrlBean aliasUrlBean = new CmsXmlSitemapUrlBean(
                    replaceServerUri(aliasLink),
                    -1,
                    changeFrequency,
                    priority);
                aliasUrlBean.setOriginalResource(aliasTarget);
                addResult(aliasUrlBean, 1);
            }
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Gets all resources from the folder tree beneath the base folder or the shared folder which have a given type.<p>
     *
     * @param type the type to filter by
     *
     * @return the list of resources with the given type
     *
     * @throws CmsException if something goes wrong
     */
    private List<CmsResource> getDetailResources(I_CmsResourceType type) throws CmsException {

        String typeName = type.getTypeName();
        if (!m_detailResources.containsKey(typeName)) {
            List<CmsResource> result = new ArrayList<CmsResource>();
            CmsResourceFilter filter = CmsResourceFilter.DEFAULT_FILES.addRequireType(type);
            List<CmsResource> siteFiles = m_guestCms.readResources(m_siteRoot, filter, true);
            result.addAll(siteFiles);
            String shared = CmsFileUtil.removeTrailingSeparator(OpenCms.getSiteManager().getSharedFolder());
            if (shared != null) {
                List<CmsResource> sharedFiles = m_guestCms.readResources(shared, filter, true);
                result.addAll(sharedFiles);
            }
            m_detailResources.put(typeName, result);
        }
        return m_detailResources.get(typeName);
    }

    /**
     * Gets the locale to use for the given resource.<p>
     *
     * @param resource the resource
     * @param propertyList the properties of the resource
     *
     * @return the locale to use for the given resource
     */
    private Locale getLocale(CmsResource resource, List<CmsProperty> propertyList) {

        return OpenCms.getLocaleManager().getDefaultLocale(m_guestCms, m_guestCms.getSitePath(resource));
    }

    /**
     * Reads the data necessary for building the sitemap from the VFS and initializes the internal data structures.<p>
     *
     * @param baseSitePath the base site path
     *
     * @throws CmsException if something goes wrong
     */
    private void initializeFileData(String baseSitePath) throws CmsException {

        m_resultMap.clear();
        m_siteRootLink = OpenCms.getLinkManager().getOnlineLink(m_siteGuestCms, "/");
        m_siteRootLink = CmsFileUtil.removeTrailingSeparator(m_siteRootLink);
        m_detailPageInfos = OpenCms.getADEManager().getAllDetailPages(m_guestCms);
        for (CmsDetailPageInfo detailPageInfo : m_detailPageInfos) {
            String type = detailPageInfo.getType();
            String path = detailPageInfo.getUri();
            path = CmsFileUtil.removeTrailingSeparator(path);
            m_detailTypesByPage.put(path, type);
        }
        List<CmsAlias> siteAliases = OpenCms.getAliasManager().getAliasesForSite(
            m_siteGuestCms,
            m_siteGuestCms.getRequestContext().getSiteRoot());
        for (CmsAlias alias : siteAliases) {
            if (isAliasBelowBaseFolder(alias) && (alias.getMode() == CmsAliasMode.page)) {
                CmsUUID aliasId = alias.getStructureId();
                m_pageAliasesBelowBaseFolderByStructureId.put(aliasId, alias);
            }
        }

    }

    /**
     * Checks whether the page/detail content combination is a valid detail page.<p>
     *
     * @param page the container page
     * @param locale the locale
     * @param detailRes the detail content resource
     *
     * @return true if this is a valid detail page combination
     */
    private boolean isValidDetailPageCombination(CmsResource page, Locale locale, CmsResource detailRes) {

        return true;
    }

}
