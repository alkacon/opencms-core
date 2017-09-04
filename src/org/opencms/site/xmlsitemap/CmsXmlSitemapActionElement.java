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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;

import java.lang.reflect.Constructor;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Action element class for displaying the XML sitemap from a JSP.<p>
 */
public class CmsXmlSitemapActionElement extends CmsJspActionElement {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsXmlSitemapActionElement.class);

    /** Runtime property name for the default sitemap generator class. */
    private static final String PARAM_DEFAULT_SITEMAP_GENERATOR = "sitemap.generator";

    /** The configuration bean. */
    protected CmsXmlSeoConfiguration m_configuration;

    /**
     * Constructor, with parameters.
     *
     * @param pageContext the JSP page context object
     * @param request the JSP request
     * @param response the JSP response
     */
    public CmsXmlSitemapActionElement(
        PageContext pageContext,
        HttpServletRequest request,
        HttpServletResponse response) {

        super(pageContext, request, response);
    }

    /**
     * Creates an XML sitemap generator instance given a class name and the root path for the sitemap.<p>
     *
     * @param className the class name of the sitemap generator (may be null for the default
     * @param folderRootPath the root path of the start folder for the sitemap
     * @return the sitemap generator instance
     *
     * @throws CmsException if something goes wrong
     */
    public static CmsXmlSitemapGenerator createSitemapGenerator(String className, String folderRootPath)
    throws CmsException {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(className)) {
            className = (String)(OpenCms.getRuntimeProperty(PARAM_DEFAULT_SITEMAP_GENERATOR));
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(className)) {
            className = CmsXmlSitemapGenerator.class.getName();
        }
        try {
            Class<? extends CmsXmlSitemapGenerator> generatorClass = Class.forName(className).asSubclass(
                CmsXmlSitemapGenerator.class);
            Constructor<? extends CmsXmlSitemapGenerator> constructor = generatorClass.getConstructor(String.class);
            CmsXmlSitemapGenerator generator = constructor.newInstance(folderRootPath);
            return generator;
        } catch (Exception e) {
            LOG.error(
                "Could not create configured sitemap generator " + className + ", using the default class instead",
                e);
            return new CmsXmlSitemapGenerator(folderRootPath);
        }
    }

    /**
     * Constructs an XML sitemap generator given an XML sitemap configuration file.<p>
     *
     * @param seoFileRes the sitemap XML file
     * @param config the parsed configuration
     *
     * @return the sitemap generator, or null if the given configuration is not an XML sitemap configuration
     *
     * @throws CmsException if something goes wrong
     */
    public static CmsXmlSitemapGenerator prepareSitemapGenerator(CmsResource seoFileRes, CmsXmlSeoConfiguration config)
    throws CmsException {

        if (config.getMode().equals(CmsXmlSeoConfiguration.MODE_XML_SITEMAP)) {
            String baseFolderRootPath = CmsFileUtil.removeTrailingSeparator(
                CmsResource.getParentFolder(seoFileRes.getRootPath()));
            CmsXmlSitemapGenerator xmlSitemapGenerator = createSitemapGenerator(
                config.getSitemapGeneratorClassName(),
                baseFolderRootPath);
            xmlSitemapGenerator.setComputeContainerPageDates(config.shouldComputeContainerPageModificationDates());
            CmsPathIncludeExcludeSet inexcludeSet = xmlSitemapGenerator.getIncludeExcludeSet();
            for (String include : config.getIncludes()) {
                inexcludeSet.addInclude(include);
            }
            for (String exclude : config.getExcludes()) {
                inexcludeSet.addExclude(exclude);
            }
            xmlSitemapGenerator.setServerUrl(config.getServerUrl());
            return xmlSitemapGenerator;
        }
        return null;
    }

    /**
     * Displays either the generated sitemap.xml or the generated robots.txt, depending on the configuration.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void run() throws Exception {

        CmsObject cms = getCmsObject();
        String seoFilePath = cms.getRequestContext().getUri();
        CmsResource seoFile = cms.readResource(seoFilePath);
        m_configuration = new CmsXmlSeoConfiguration();
        m_configuration.load(cms, seoFile);
        String mode = m_configuration.getMode();
        if (mode.equals(CmsXmlSeoConfiguration.MODE_ROBOTS_TXT)) {
            showRobotsTxt();
        } else {
            boolean updateCache = Boolean.parseBoolean(getRequest().getParameter("updateCache"));
            String value = "";
            if (updateCache && m_configuration.usesCache()) {
                // update request, and caching is configured -> update the cache
                CmsXmlSitemapGenerator generator = prepareSitemapGenerator(seoFile, m_configuration);
                value = generator.renderSitemap();
                CmsXmlSitemapCache.INSTANCE.put(seoFile.getRootPath(), value);
            } else if (!updateCache && m_configuration.usesCache()) {
                // normal request, and caching is configured -> look in the cache first, and if not found, calculate sitemap and store it in cache
                String cachedValue = CmsXmlSitemapCache.INSTANCE.get(seoFile.getRootPath());
                if (cachedValue != null) {
                    value = cachedValue;
                } else {
                    CmsXmlSitemapGenerator generator = prepareSitemapGenerator(seoFile, m_configuration);
                    value = generator.renderSitemap();
                    CmsXmlSitemapCache.INSTANCE.put(seoFile.getRootPath(), value);
                }
            } else if (!updateCache && !m_configuration.usesCache()) {
                // normal request, caching is not configured -> always generate a fresh sitemap
                CmsXmlSitemapGenerator generator = prepareSitemapGenerator(seoFile, m_configuration);
                value = generator.renderSitemap();
            } else if (updateCache && !m_configuration.usesCache()) {
                // update request with no caching configured -> ignore
            }
            getResponse().getWriter().print(value);
        }

    }

    /**
     * Renders the robots.txt data containing the sitemaps automatically.<p>
     *
     * @throws Exception if something goes wrong
     */
    private void showRobotsTxt() throws Exception {

        CmsObject cms = getCmsObject();
        StringBuffer buffer = new StringBuffer();
        I_CmsResourceType seoFileType = OpenCms.getResourceManager().getResourceType(
            CmsXmlSeoConfiguration.SEO_FILE_TYPE);
        List<CmsResource> seoFiles = cms.readResources(
            "/",
            CmsResourceFilter.DEFAULT_FILES.addRequireVisible().addRequireType(seoFileType));
        for (CmsResource seoFile : seoFiles) {
            try {
                if (seoFile.getName().contains("test")) {
                    continue;
                }
                CmsXmlSeoConfiguration seoFileConfig = new CmsXmlSeoConfiguration();
                seoFileConfig.load(cms, seoFile);
                if (seoFileConfig.isXmlSitemapMode()) {
                    buffer.append(
                        "Sitemap: "
                            + CmsXmlSitemapGenerator.replaceServerUri(
                                OpenCms.getLinkManager().getOnlineLink(cms, cms.getSitePath(seoFile)),
                                m_configuration.getServerUrl()));
                    buffer.append("\n");
                }
            } catch (CmsException e) {
                LOG.error("Error while generating robots.txt : " + e.getLocalizedMessage(), e);
            }
        }
        buffer.append("\n");
        buffer.append(m_configuration.getRobotsTxtText());
        buffer.append("\n");
        getResponse().getWriter().print(buffer.toString());
    }

}
