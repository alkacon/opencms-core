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

package org.opencms.site;

import org.opencms.configuration.I_CmsXmlConfiguration;
import org.opencms.util.CmsPath;
import org.opencms.util.CmsStringUtil;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import org.dom4j.Element;

/**
 * Describes a mapping rule that selects a different site root based on whether the requested path matches a set of prefixes or not.
 *
 * <p>The path prefixes must be non-trivial, they can't just be the empty string or '/' - these values will be ignored.
 */
public class CmsAlternativeSiteRootMapping implements Serializable {

    /** Node name for the alternative site root mapping. */
    public static final String N_ALTERNATIVE_SITE_ROOT_MAPPING = "alternative-site-root";

    /** Node name for mapped paths. */
    public static final String N_PATH = "path";

    /** Suffix to append to the title of the site. */
    public static final String A_TITLE_SUFFIX = "title-suffix";

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The list of prefixes which should map to the different site root. */
    private List<CmsPath> m_prefixes;

    /** The alternative site root. */
    private CmsPath m_siteRoot;

    /** Suffix to append to the site title. */
    private String m_titleSuffix;

    /**
     * Creates a new instance.
     *
     * @param siteRoot the site root
     * @param prefixes the prefixes
     * @param titleSuffix the title suffix
     */
    public CmsAlternativeSiteRootMapping(String siteRoot, List<String> prefixes, String titleSuffix) {

        super();
        m_siteRoot = new CmsPath(siteRoot);
        m_titleSuffix = titleSuffix != null ? titleSuffix : "";
        // remove invalid prefixes, like empty string or '/'
        m_prefixes = prefixes.stream().filter(prefix -> prefix != null).map(prefix -> prefix.trim()).filter(
            prefix -> !prefix.equals("/") && !CmsStringUtil.isEmptyOrWhitespaceOnly(prefix)).map(CmsPath::new).collect(
                Collectors.toList());
        Collections.sort(m_prefixes);
    }

    /**
     * Appends configuration data to the given parent element.
     *
     * @param parent the parent element
     */
    public void appendXml(Element parent) {

        Element mapping = parent.addElement(N_ALTERNATIVE_SITE_ROOT_MAPPING);
        mapping.addAttribute(I_CmsXmlConfiguration.A_URI, m_siteRoot.asString());
        mapping.addAttribute(A_TITLE_SUFFIX, m_titleSuffix);
        for (CmsPath path : getPrefixes()) {
            mapping.addElement(N_PATH).addText(path.asString());
        }
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof CmsAlternativeSiteRootMapping)) {
            return false;
        }
        CmsAlternativeSiteRootMapping that = (CmsAlternativeSiteRootMapping)obj;
        return that.getSiteRoot().equals(getSiteRoot())
            && that.getPrefixes().equals(getPrefixes())
            && that.getTitleSuffix().equals(getTitleSuffix());
    }

    /**
     * Gets the path prefixes.
     *
     * @return the path prefixes
     */
    public List<CmsPath> getPrefixes() {

        return Collections.unmodifiableList(m_prefixes);
    }

    /**
     * Gets the site root as a CmsPath.
     *
     * @return the site root as a CmsPath
     */
    public CmsPath getSiteRoot() {

        return m_siteRoot;
    }

    /**
     * Gets the suffix to append to the site title.
     *
     * @return the suffix to append to the site title
     */
    public String getTitleSuffix() {

        return m_titleSuffix;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return ((((m_prefixes.hashCode()) * 31) + m_siteRoot.hashCode()) * 31) + m_titleSuffix.hashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);

    }

}
