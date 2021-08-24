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

package org.opencms.xml.xml2json;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * Contains configuration for access restrictions to JSON handler.
 */
public class CmsJsonAccessPolicy {

    /** Default property filter: Property name must not contain secret, api, password or key. */
    public static final Pattern DEFAULT_PROP_FILTER = Pattern.compile("(?i)^(?!.*(?:secret|api|password|key)).*$");

    /** Default CORS filter. */
    public static final String DEFAULT_CORS_FILTER = "*";

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJsonAccessPolicy.class);

    /** Group which should be allowed access. */
    private String m_accessGroup;

    /** Exclude path patterns. */
    private List<Pattern> m_exclude;

    /** HTTP response header Access-Control-Allow-Origin */
    private String m_corsAllowOrigin = DEFAULT_CORS_FILTER;

    /** HTTP response header Access-Control-Allow-Methods */
    private String m_corsAllowMethods = DEFAULT_CORS_FILTER;

    /** HTTP response header Access-Control-Allow-Headers */
    private String m_corsAllowHeaders = DEFAULT_CORS_FILTER;

    /** Include path patterns. */
    private List<Pattern> m_include;

    /** If this is set to a non-null value, that value will always be returned from checkAccess. */
    private Boolean m_overrideValue;

    /** The property filter regex - only properties with names it matches are written to JSON .*/
    private Pattern m_propertyFilter = DEFAULT_PROP_FILTER;

    /**
     * Creates new access policy with a fixed return value for checkAccess.
     *
     * @param enabled true if allowed, false if forbidden
     */
    public CmsJsonAccessPolicy(boolean enabled) {

        m_overrideValue = Boolean.valueOf(enabled);
    }

    /**
     * Creates a new instance.
     *
     * @param accessGroup the access group (may be null)
     * @param includePatterns the include regexes
     * @param excludePatterns the exclude regexes
     * @param propertyFilterRegex the regular expression to filter property names with
     * @param corsAllowOrigin the HTTP response header Access-Control-Allow-Origin
     * @param corsAllowMethods the HTTP response header Access-Control-Allow-Methods
     * @param corsAllowHeaders the HTTP response header Access-Control-Allow-Headers
     */
    public CmsJsonAccessPolicy(
        String accessGroup,
        List<String> includePatterns,
        List<String> excludePatterns,
        String propertyFilterRegex,
        String corsAllowOrigin,
        String corsAllowMethods,
        String corsAllowHeaders) {

        m_accessGroup = accessGroup;
        m_include = includePatterns.stream().map(Pattern::compile).collect(Collectors.toList());
        m_exclude = excludePatterns.stream().map(Pattern::compile).collect(Collectors.toList());
        if (propertyFilterRegex != null) {
            m_propertyFilter = Pattern.compile(propertyFilterRegex);
        }
        m_corsAllowOrigin = corsAllowOrigin;
        m_corsAllowMethods = corsAllowMethods;
        m_corsAllowHeaders = corsAllowHeaders;
    }

    /**
     * Parses an JSON handler access policy file.
     *
     * @param data the data
     * @return the access policy
     *
     * @throws DocumentException if parsing fails
     */
    public static CmsJsonAccessPolicy parse(byte[] data) throws DocumentException {

        try (ByteArrayInputStream stream = new ByteArrayInputStream(data)) {
            return parse(stream);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Parses an JSON handler access policy file.
     *
     * @param stream the XML data stream
     * @return the access policy
     *
     * @throws DocumentException if parsing fails
    
     */
    public static CmsJsonAccessPolicy parse(InputStream stream) throws DocumentException {

        SAXReader reader = new SAXReader();
        Document document = reader.read(stream);
        Element root = document.getRootElement();
        Element groupElem = root.element("group");
        String groupName = null;
        if (groupElem != null) {
            groupName = groupElem.getTextTrim();
        }
        Element propertyFilterElem = root.element("property-filter");
        String propertyFilterRegex = null;
        if (propertyFilterElem != null) {
            propertyFilterRegex = propertyFilterElem.getTextTrim();
        }
        List<String> includes = root.elements("include").stream().map(elem -> elem.getTextTrim()).collect(
            Collectors.toList());
        List<String> excludes = root.elements("exclude").stream().map(elem -> elem.getTextTrim()).collect(
            Collectors.toList());
        Element elementCors = root.element("cors");
        String corsAllowOrigin = DEFAULT_CORS_FILTER;
        String corsAllowMethods = DEFAULT_CORS_FILTER;
        String corsAllowHeaders = DEFAULT_CORS_FILTER;
        if (elementCors != null) {
            corsAllowOrigin = elementCors.elementTextTrim("allow-origin");
            corsAllowMethods = elementCors.elementTextTrim("allow-methods");
            corsAllowHeaders = elementCors.elementTextTrim("allow-headers");
        }
        return new CmsJsonAccessPolicy(
            groupName,
            includes,
            excludes,
            propertyFilterRegex,
            corsAllowOrigin,
            corsAllowMethods,
            corsAllowHeaders);
    }

    /**
     * Checks if a JSON handler request is allowed for this policy.
     *
     * @param cms the CMS context
     * @param path the path
     *
     * @return true if the request is allowed
     */
    public boolean checkAccess(CmsObject cms, String path) {

        if (m_overrideValue != null) {
            return m_overrideValue.booleanValue();
        }
        if (m_accessGroup != null) {
            try {
                List<CmsGroup> groups = cms.getGroupsOfUser(
                    cms.getRequestContext().getCurrentUser().getName(),
                    true,
                    true);
                boolean foundGroup = groups.stream().anyMatch(group -> group.getName().equals(m_accessGroup));
                if (!foundGroup) {
                    return false;
                }
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
                return false;
            }
        }

        boolean included = (m_include.isEmpty()
            || m_include.stream().anyMatch(include -> include.matcher(path).matches()))
            && !m_exclude.stream().anyMatch(exclude -> exclude.matcher(path).matches());
        return included;

    }

    /**
     * Checks if the property can be accessed (i.e. is not filtered out by property filter).
     *
     * @param property the property name to check
     * @return true if the property can be written to JSON
     */
    public boolean checkPropertyAccess(String property) {

        boolean result = (m_propertyFilter == null) || m_propertyFilter.matcher(property).matches();
        if (!result) {
            LOG.info("Filtered property " + property + " because it does not match the JSON property filter.");
        }
        return result;
    }

    /**
     * Sets the configured CORS headers for a given HTTP servlet response.<p>
     *
     * @param response the given HTTP servlet response
     */
    public void setCorsHeaders(HttpServletResponse response) {

        if (m_corsAllowOrigin != null) {
            response.setHeader("Access-Control-Allow-Origin", m_corsAllowOrigin);
        }
        if (m_corsAllowMethods != null) {
            response.setHeader("Access-Control-Allow-Methods", m_corsAllowMethods);
        }
        if (m_corsAllowHeaders != null) {
            response.setHeader("Access-Control-Allow-Headers", m_corsAllowHeaders);
        }
    }

}
