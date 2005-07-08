/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/CmsStaticExportRfsRule.java,v $
 * Date   : $Date: 2005/07/08 17:42:47 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.staticexport;

import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Help class for storing of rfs-rules..<p>
 * 
 * @author Michael Moossen
 * @version $Revision: 1.1 $
 * @since 6.0.0
 */
public class CmsStaticExportRfsRule {

    /** Description of the rule. */
    private String m_description;

    /** Rfs export path. */
    private String m_exportPath;

    /** configured Rfs export path. */
    private final String m_exportPathConfigured;

    /** Name of rule. */
    private String m_name;

    /** List of regular expresions to determine related system resources. */
    private List m_relatedSystemResources;

    /** configured Url prefix. */
    private final String m_rfsPreConfigured;

    /** Url prefix pattern. */
    private String m_rfsPrefix;

    /** Source regex. */
    private final Pattern m_source;

    /** Relative links value. */
    private Boolean m_useRelativeLinks;

    /**
     * Default constructor.<p>
     * 
     * @param name the name of the rule
     * @param description the description for the rule
     * @param source the source regex
     * @param rfsPrefix the url prefix
     * @param exportPath the rfs export path
     * @param useRelativeLinks Relative links value
     */
    public CmsStaticExportRfsRule(
        String name,
        String description,
        String source,
        String rfsPrefix,
        String exportPath,
        Boolean useRelativeLinks) {

        m_name = name;
        m_description = description;
        m_source = Pattern.compile(source);
        m_rfsPreConfigured = rfsPrefix;
        m_exportPathConfigured = exportPath;
        m_useRelativeLinks = useRelativeLinks;
        m_relatedSystemResources = new ArrayList();
    }

    /**
     * Full constructor.<p>
     * 
     * @param name the name of the rule
     * @param description the description for the rule
     * @param source the source regex
     * @param rfsPrefix the url prefix
     * @param exportPath the rfs export path
     * @param useRelativeLinks Relative links value
     * @param relatedSystemRes list of <code>{@link Pattern}</code>s
     */
    public CmsStaticExportRfsRule(
        String name,
        String description,
        String source,
        String rfsPrefix,
        String exportPath,
        Boolean useRelativeLinks,
        List relatedSystemRes) {

        this(name, description, source, rfsPrefix, exportPath, useRelativeLinks);
        m_relatedSystemResources.addAll(relatedSystemRes);
    }

    /**
     * Adds a regex of related system resources.<p>
     * 
     * @param regex the regex to add
     */
    public void addRelatedSystemRes(String regex) {

        m_relatedSystemResources.add(Pattern.compile(regex));
    }

    /**
     * Returns the description.<p>
     *
     * @return the description
     */
    public String getDescription() {

        return m_description;
    }

    /**
     * Returns the rfs export Path.<p>
     *
     * @return the rfs export Path
     */
    public String getExportPath() {

        return m_exportPath;
    }

    /**
     * Returns the configured rfs export Path with unstubstituted context values.<p>
     *
     * @return the configured rfs export Path
     */
    public String getExportPathConfigured() {

        return m_exportPathConfigured;
    }

    /**
     * Returns the name.<p>
     *
     * @return the name
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the related system resources list as list of <code>{@link Pattern}</code>.<p>
     * 
     * @return the related resources list as list of <code>{@link Pattern}</code>
     */
    public List getRelatedSystemResources() {

        return Collections.unmodifiableList(m_relatedSystemResources);
    }

    /**
     * Returns the url Prefix with macro replacement.<p>
     *
     * @return the url Prefix
     */
    public String getRfsPrefix() {

        return m_rfsPrefix;
    }

    /**
     * Returns the configured url Prefix with unstubstituted context values.<p>
     *
     * @return the configured url Prefix
     */
    public String getRfsPrefixConfigured() {

        return m_rfsPreConfigured;
    }

    /**
     * Returns the source regex pattern.<p>
     *
     * @return the source regex pattern
     */
    public Pattern getSource() {

        return m_source;
    }

    /**
     * Returns true if the links in the static export should be relative.<p>
     * 
     * @return true if the links in the static export should be relative
     */
    public Boolean getUseRelativeLinks() {

        return m_useRelativeLinks;
    }

    /**
     * Checks if a vfsName matches the given related system resource patterns.<p>
     * 
     * @param vfsName the vfs name of a resource to check
     * @return true if the name matches one of the given related system resource patterns
     */
    public boolean match(String vfsName) {

        for (int j = 0; j < m_relatedSystemResources.size(); j++) {
            Pattern pattern = (Pattern)m_relatedSystemResources.get(j);
            if (pattern.matcher(vfsName).matches()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the rfs export Path after normalizing.<p>
     *
     * @param exportPath the rfs export Path to set
     */
    public void setExportPath(String exportPath) {

        if (exportPath.equals(OpenCms.getSystemInfo().getWebApplicationRfsPath())) {
            // not allowed because a full static export would delete the opencms directory
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_INVALID_EXPORT_PATH_1, m_name));
        }
        m_exportPath = exportPath;
    }

    /**
     * Sets the url Prefix after normalizing.<p>
     *
     * @param rfsPrefix the url Prefix to set
     */
    public void setRfsPrefix(String rfsPrefix) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(rfsPrefix)) {
            // not allowed, getting troubles translating rfsName -> vfsName
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_INVALID_RFS_PREFIX_1, m_name));
        }
        m_rfsPrefix = rfsPrefix;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        StringBuffer ret = new StringBuffer(getClass().getName());
        ret.append(":[");
        ret.append("name: ").append(m_name).append("; ");
        ret.append("description: ").append(m_description).append("; ");
        ret.append("source: ").append(m_source).append("; ");
        ret.append("exportPath: ").append(m_exportPath).append("; ");
        ret.append("rfsPrefix: ").append(m_rfsPrefix).append("; ");
        ret.append("useRelativeLinks: ").append(m_useRelativeLinks).append("; ");
        ret.append("relatedSystemRes: ").append(m_relatedSystemResources).append("; ");
        return ret.append("]").toString();
    }

}
