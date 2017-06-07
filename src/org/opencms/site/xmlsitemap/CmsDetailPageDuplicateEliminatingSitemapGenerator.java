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

package org.opencms.site.xmlsitemap;

import org.opencms.ade.configuration.CmsADEConfigData.DetailInfo;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsPathMap;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

/**
 * Sitemap generator class which tries to eliminate duplicate detail pages for the same content and locale.<p>
 *
 * In principle, any detail page for a type somewhere in the system could be used to display contents anywhere
 * else in the system. This sitemap generator, instead of generating all detail page URLs that could possibly be generated,
 * instead tries to find only the best candidate URL for each content / locale combination.
 */
public class CmsDetailPageDuplicateEliminatingSitemapGenerator extends CmsXmlSitemapGenerator {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDetailPageDuplicateEliminatingSitemapGenerator.class);

    /** The detail page information. */
    protected List<DetailInfo> m_detailInfos = new ArrayList<DetailInfo>();

    /** Multimap of detail infos with the detail page as key. */
    private Multimap<String, DetailInfo> m_detailInfosByPage;

    /** Cache for path maps containing the content resources. */
    private HashMap<String, CmsPathMap<CmsResource>> m_pathMapsByType = Maps.newHashMap();

    /**
     * Constructor.<p>
     *
     * @param sitemapPath the sitemap path
     * @throws CmsException if something goes wrong
     */
    public CmsDetailPageDuplicateEliminatingSitemapGenerator(String sitemapPath)
    throws CmsException {
        super(sitemapPath);
        List<DetailInfo> rawDetailInfo = OpenCms.getADEManager().getDetailInfo(m_guestCms);
        List<DetailInfo> filteredDetailInfo = Lists.newArrayList();
        for (DetailInfo item : rawDetailInfo) {
            String path = item.getFolderPath();
            if (OpenCms.getSiteManager().startsWithShared(path) || CmsStringUtil.isPrefixPath(m_siteRoot, path)) {
                filteredDetailInfo.add(item);
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Filtered detail info: " + item);
                }
            }
        }
        m_detailInfos = filteredDetailInfo;

    }

    /**
     * @see org.opencms.site.xmlsitemap.CmsXmlSitemapGenerator#generateSitemapBeans()
     */
    @Override
    public List<CmsXmlSitemapUrlBean> generateSitemapBeans() throws CmsException {

        List<CmsXmlSitemapUrlBean> parentResult = super.generateSitemapBeans();
        List<CmsXmlSitemapUrlBean> result = Lists.newArrayList();
        Multimap<String, CmsXmlSitemapUrlBean> detailPageBeans = ArrayListMultimap.create();

        // We want to eliminate duplicate detail pages for the same detail content and locale,
        // so first we group the XML sitemap beans belonging to detail pages by their locale/content combination,
        // and then we sort each group by the sitemap configuration where the detail page is coming from,
        // and then only take the last element in each group.

        for (CmsXmlSitemapUrlBean urlBean : parentResult) {
            if (urlBean.getDetailPageResource() == null) {
                result.add(urlBean);
            } else {
                String localeKey = urlBean.getOriginalResource().getStructureId() + "_" + urlBean.getLocale();
                detailPageBeans.put(localeKey, urlBean);
            }
        }
        Comparator<CmsXmlSitemapUrlBean> pathComparator = new Comparator<CmsXmlSitemapUrlBean>() {

            public int compare(CmsXmlSitemapUrlBean urlbean1, CmsXmlSitemapUrlBean urlbean2) {

                String subsite1 = urlbean1.getSubsite();
                if (subsite1 == null) {
                    subsite1 = "";
                }
                String subsite2 = urlbean2.getSubsite();
                if (subsite2 == null) {
                    subsite2 = "";
                }
                return subsite1.compareTo(subsite2);
            }
        };
        for (String key : detailPageBeans.keySet()) {
            result.add(Collections.max(detailPageBeans.get(key), pathComparator));
        }
        return result;
    }

    /**
     * @see org.opencms.site.xmlsitemap.CmsXmlSitemapGenerator#addDetailLinks(org.opencms.file.CmsResource, java.util.Locale)
     */
    @Override
    protected void addDetailLinks(CmsResource containerPage, Locale locale) throws CmsException {

        Collection<DetailInfo> detailInfos = getDetailInfosForPage(containerPage);
        for (DetailInfo info : detailInfos) {
            List<CmsResource> contents = getContents(info.getFolderPath(), info.getType());
            for (CmsResource detailRes : contents) {
                List<CmsProperty> detailProps = m_guestCms.readPropertyObjects(detailRes, true);
                String detailLink = getDetailLink(containerPage, detailRes, locale);
                detailLink = CmsFileUtil.removeTrailingSeparator(detailLink);
                CmsXmlSitemapUrlBean detailUrlBean = new CmsXmlSitemapUrlBean(
                    replaceServerUri(detailLink),
                    detailRes.getDateLastModified(),
                    getChangeFrequency(detailProps),
                    getPriority(detailProps));
                detailUrlBean.setLocale(locale);
                detailUrlBean.setOriginalResource(detailRes);
                detailUrlBean.setDetailPageResource(containerPage);
                detailUrlBean.setSubsite(info.getBasePath());
                addResult(detailUrlBean, 2);
            }
        }
    }

    /**
     * Gets the contents for the given folder path and type name.<p>
     *
     * @param folderPath the content folder path
     * @param type the type name
     * @return the list of contents
     *
     * @throws CmsException if something goes wrong
     */
    private List<CmsResource> getContents(String folderPath, String type) throws CmsException {

        CmsPathMap<CmsResource> pathMap = getPathMapForType(type);
        return pathMap.getChildValues(folderPath);
    }

    /**
     * Gets the detail information for the given container page.<p>
     *
     * @param containerPage the container page
     * @return the detail information
     */
    private Collection<DetailInfo> getDetailInfosForPage(CmsResource containerPage) {

        if (m_detailInfosByPage == null) {
            m_detailInfosByPage = ArrayListMultimap.create();
            for (DetailInfo detailInfo : m_detailInfos) {
                m_detailInfosByPage.put(detailInfo.getDetailPageInfo().getUri(), detailInfo);
            }
        }
        String folderPath = CmsResource.getParentFolder(containerPage.getRootPath());
        Collection<DetailInfo> result = m_detailInfosByPage.get(containerPage.getRootPath());
        if (result.isEmpty()) {
            result = m_detailInfosByPage.get(folderPath);
        }
        return result;
    }

    /**
     * Gets the path map containing the contents for the given type.<p>
     *
     * @param typeName the type name
     * @return the path map with the content resources
     *
     * @throws CmsException if something goes wrong
     */
    private CmsPathMap<CmsResource> getPathMapForType(String typeName) throws CmsException {

        if (!m_pathMapsByType.containsKey(typeName)) {
            CmsPathMap<CmsResource> pathMap = readPathMapForType(
                OpenCms.getResourceManager().getResourceType(typeName));
            m_pathMapsByType.put(typeName, pathMap);
        }
        return m_pathMapsByType.get(typeName);
    }

    /**
     * Reads the contents of a given type and stores them in a path map.<p>
     *
     * @param type the type for which to read the contents
     * @return the path map containing the contents
     */
    private CmsPathMap<CmsResource> readPathMapForType(I_CmsResourceType type) {

        List<CmsResource> result = new ArrayList<CmsResource>();
        CmsResourceFilter filter = CmsResourceFilter.DEFAULT_FILES.addRequireType(type);
        try {
            List<CmsResource> siteFiles = m_guestCms.readResources(m_siteRoot, filter, true);
            result.addAll(siteFiles);
        } catch (CmsException e) {
            LOG.error("XML sitemap generator error: " + e.getLocalizedMessage(), e);
        }
        String shared = CmsFileUtil.removeTrailingSeparator(OpenCms.getSiteManager().getSharedFolder());
        if (shared != null) {
            try {
                List<CmsResource> sharedFiles = m_guestCms.readResources(shared, filter, true);
                result.addAll(sharedFiles);
            } catch (CmsException e) {
                LOG.error("XML sitemap generator error: " + e.getLocalizedMessage(), e);
            }
        }
        CmsPathMap<CmsResource> resultMap = new CmsPathMap<CmsResource>();
        for (CmsResource resource : result) {
            resultMap.add(resource.getRootPath(), resource);
        }
        return resultMap;
    }

}
