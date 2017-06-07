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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Configuration bean which represents the options which are configurable from a 'seo-file' resource.<p>
 */
public class CmsXmlSeoConfiguration {

    /** Mode name constant. */
    public static final Object MODE_ROBOTS_TXT = "robotstxt";

    /** Mode name constant. */
    public static final Object MODE_XML_SITEMAP = "xmlsitemap";

    /** Node name. */
    public static final String N_COMPUTE_CONTAINER_PAGE_DATES = "ComputeContPageDates";

    /** Node name. */
    public static final String N_EXCLUDE = "SitemapExclude";

    /** Node name. */
    public static final String N_SERVER_URL = "ServerUrl";

    /** Node name. */
    public static final String N_GENERATOR_CLASS = "GeneratorClass";

    /** Node name. */
    public static final String N_INCLUDE = "SitemapInclude";

    /** Node name. */
    public static final String N_MODE = "Mode";

    /** Node name. */
    public static final String N_ROBOTS_TXT_TEXT = "RobotsTxtText";

    /** The file type used for generating XML sitemaps or robots.txt files. */
    public static final String SEO_FILE_TYPE = "seo_file";

    /** The exclude paths. */
    protected List<String> m_excludes = new ArrayList<String>();

    /** The include paths. */
    protected List<String> m_includes = new ArrayList<String>();

    /** The mode. */
    protected String m_mode;

    /** Text to be included in robots.txt after the sitemap references. */
    protected String m_robotsTxtText = "";

    /** Flag which indicates whether container page modification dates should be computed. */
    private boolean m_computeContainerPageDates;

    /** The sitemap generator class name. */
    private String m_generatorClassName;

    /** The server URL replacement. */
    private String m_serverUrl;

    /**
     * Gets the list of exclude paths.<p>
     *
     * @return the list of exclude paths
     */
    public List<String> getExcludes() {

        return Collections.unmodifiableList(m_excludes);
    }

    /**
     * Gets the list of include paths.<p>
     *
     * @return the list of include paths
     */
    public List<String> getIncludes() {

        return Collections.unmodifiableList(m_includes);
    }

    /**
     * Gets the mode.<p>
     *
     * @return the mode
     */
    public String getMode() {

        return m_mode;
    }

    /**
     * Gets the text which should be inserted in robots.txt mode.<p>
     *
     * @return the text to insert in robots.txt mode
     */
    public String getRobotsTxtText() {

        return m_robotsTxtText;
    }

    /**
     * Gets the configured server URL.<p>
     *
     * This, if set, replaces the host/port used by getOnlineLink() for the URLs
     * in the XML sitemap.<p>
     *
     * @return the server URL
     */
    public String getServerUrl() {

        return m_serverUrl;
    }

    /**
     * Gets the class name for the sitemap generator class (may return null if none is explicitly configured).
     *
     * @return the sitemap generator class name
     */
    public String getSitemapGeneratorClassName() {

        return m_generatorClassName;
    }

    /**
     * Returns true if this configuration is configured as robots.txt mode.<p>
     *
     * @return true if the mode is set to the robots.txt mode
     */
    public boolean isXmlSitemapMode() {

        return MODE_XML_SITEMAP.equals(m_mode);
    }

    /**
     * Loads the bean data from the given resource.<p>
     *
     * @param cms the CMS context to use
     * @param resource the resource from which to load the data
     *
     * @throws CmsException if something goes wrong
     */
    public void load(CmsObject cms, CmsResource resource) throws CmsException {

        CmsFile file = cms.readFile(resource);
        CmsObject rootCms = OpenCms.initCmsObject(cms);
        rootCms.getRequestContext().setSiteRoot("");
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);
        Locale en = new Locale("en");
        for (I_CmsXmlContentValue value : content.getValues(N_INCLUDE, en)) {
            String include = value.getStringValue(rootCms);
            if (!CmsStringUtil.isEmpty(include)) {
                m_includes.add(include);
            }
        }
        for (I_CmsXmlContentValue value : content.getValues(N_EXCLUDE, en)) {
            String exclude = value.getStringValue(rootCms);
            if (!CmsStringUtil.isEmpty(exclude)) {
                m_excludes.add(exclude);
            }
        }
        I_CmsXmlContentValue robotsValue = content.getValue(N_MODE, en);
        m_mode = robotsValue.getStringValue(rootCms);

        I_CmsXmlContentValue robotsTxtTextValue = content.getValue(N_ROBOTS_TXT_TEXT, en);
        if (robotsTxtTextValue != null) {
            m_robotsTxtText = robotsTxtTextValue.getStringValue(rootCms);
        }

        I_CmsXmlContentValue computeContPageDates = content.getValue(N_COMPUTE_CONTAINER_PAGE_DATES, en);
        if (computeContPageDates != null) {
            m_computeContainerPageDates = Boolean.parseBoolean(computeContPageDates.getStringValue(rootCms));
        }

        I_CmsXmlContentValue generatorClassValue = content.getValue(N_GENERATOR_CLASS, en);
        if (generatorClassValue != null) {
            m_generatorClassName = generatorClassValue.getStringValue(rootCms);
        }

        I_CmsXmlContentValue serverUrlValue = content.getValue(N_SERVER_URL, en);
        if (serverUrlValue != null) {
            m_serverUrl = serverUrlValue.getStringValue(rootCms);
        }
    }

    /**
     * Returns true if container page modification dates should be computed.<p>
     *
     * @return true if container page modification dates should be computed
     */
    public boolean shouldComputeContainerPageModificationDates() {

        return m_computeContainerPageDates;
    }
}
