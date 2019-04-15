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

import org.apache.commons.logging.Log;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * Contains configuration for access restrictions to JSON handler.
 */
public class CmsJsonAccessPolicy {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJsonAccessPolicy.class);

    /** Group which should be allowed access. */
    private String m_accessGroup;

    /** Include path patterns. */
    private List<Pattern> m_include;

    /** Exclude path patterns. */
    private List<Pattern> m_exclude;

    /** If this is set to a non-null value, that value will always be returned from checkAccess. */
    private Boolean m_overrideValue;

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
     */
    public CmsJsonAccessPolicy(String accessGroup, List<String> includePatterns, List<String> excludePatterns) {

        m_accessGroup = accessGroup;
        m_include = includePatterns.stream().map(Pattern::compile).collect(Collectors.toList());
        m_exclude = excludePatterns.stream().map(Pattern::compile).collect(Collectors.toList());
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
        List<String> includes = root.elements("include").stream().map(elem -> elem.getTextTrim()).collect(
            Collectors.toList());
        List<String> excludes = root.elements("exclude").stream().map(elem -> elem.getTextTrim()).collect(
            Collectors.toList());
        return new CmsJsonAccessPolicy(groupName, includes, excludes);
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

}
