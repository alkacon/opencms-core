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

package org.opencms.staticexport;

import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Help class for storing of rfs-rules.<p>
 *
 * @since 6.0.0
 */
public class CmsStaticExportRfsRule {

    /** Name for the default work path. */
    public static final Integer EXPORT_DEFAULT_BACKUPS = Integer.valueOf(0);

    /** Description of the rule. */
    private String m_description;

    /** Number of export backup folders. */
    private Integer m_exportBackups;

    /** Rfs export path. */
    private String m_exportPath;

    /** configured Rfs export path. */
    private final String m_exportPathConfigured;

    /** Rfs export work path. */
    private String m_exportWorkPath;

    /** configured Rfs export work path. */
    private final String m_exportWorkPathConfigured;

    /** Name of rule. */
    private String m_name;

    /** List of regular expressions to determine related system resources. */
    private List<Pattern> m_relatedSystemResources;

    /** configured Url prefix. */
    private final String m_rfsPreConfigured;

    /** Url prefix pattern. */
    private String m_rfsPrefix;

    /** Source regular expression. */
    private final Pattern m_source;

    /** Relative links value. */
    private Boolean m_useRelativeLinks;

    /**
     * Default constructor.<p>
     *
     * @param name the name of the rule
     * @param description the description for the rule
     * @param source the source regular expression
     * @param rfsPrefix the url prefix
     * @param exportPath the rfs export path
     * @param exportWorkPath the rfs export work path
     * @param exportBackups the number of backups
     * @param useRelativeLinks Relative links value
     */
    public CmsStaticExportRfsRule(
        String name,
        String description,
        String source,
        String rfsPrefix,
        String exportPath,
        String exportWorkPath,
        Integer exportBackups,
        Boolean useRelativeLinks) {

        m_name = name;
        m_description = description;
        m_source = Pattern.compile(source);
        m_rfsPreConfigured = rfsPrefix;
        m_exportPathConfigured = exportPath;
        m_exportWorkPathConfigured = exportWorkPath;
        m_exportBackups = exportBackups;
        m_useRelativeLinks = useRelativeLinks;
        m_relatedSystemResources = new ArrayList<Pattern>();
    }

    /**
     * Full constructor.<p>
     *
     * @param name the name of the rule
     * @param description the description for the rule
     * @param source the source regular expression
     * @param rfsPrefix the url prefix
     * @param exportPath the rfs export path
     * @param exportWorkPath the rfs export work path
     * @param exportBackups the number of backups
     * @param useRelativeLinks Relative links value
     * @param relatedSystemRes list of <code>{@link Pattern}</code>s
     */
    public CmsStaticExportRfsRule(
        String name,
        String description,
        String source,
        String rfsPrefix,
        String exportPath,
        String exportWorkPath,
        Integer exportBackups,
        Boolean useRelativeLinks,
        List<Pattern> relatedSystemRes) {

        this(name, description, source, rfsPrefix, exportPath, exportWorkPath, exportBackups, useRelativeLinks);
        m_relatedSystemResources.addAll(relatedSystemRes);
    }

    /**
     * Adds a regular expression of related system resources.<p>
     *
     * @param regex the regular expression to add
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
     * Returns the number of backups.<p>
     *
     * @return the number of backups
     */
    public Integer getExportBackups() {

        if (m_exportBackups != null) {
            return m_exportBackups;
        }
        // if backups not configured set to default value
        return EXPORT_DEFAULT_BACKUPS;
    }

    /**
     * Returns the rfs export Path.<p>
     *
     * @return the rfs export Path
     */
    public String getExportPath() {

        if (OpenCms.getStaticExportManager().isUseTempDir() && OpenCms.getStaticExportManager().isFullStaticExport()) {
            return getExportWorkPath();
        }
        return m_exportPath;
    }

    /**
     * Returns the configured rfs export Path with unsubstituted context values.<p>
     *
     * @return the configured rfs export Path
     */
    public String getExportPathConfigured() {

        return m_exportPathConfigured;
    }

    /**
     * Returns the rfs export Work Path.<p>
     *
     * @return the rfs export Work Path
     */
    public String getExportWorkPath() {

        return m_exportWorkPath;
    }

    /**
     * Returns the configured rfs export Work Path with unsubstituted context values.<p>
     *
     * @return the configured rfs export Work Path
     */
    public String getExportWorkPathConfigured() {

        if (m_exportWorkPathConfigured != null) {
            return m_exportWorkPathConfigured;
        }

        // if work path not configured set to default value
        return CmsStaticExportManager.EXPORT_DEFAULT_WORKPATH
            + OpenCms.getResourceManager().getFileTranslator().translateResource(m_name);
    }

    /**
     * Returns the rfs name for the given locale, only used for multi-language export.<p>
     *
     * @param rfsName the original rfs name
     * @param fileSeparator the file separator to use
     *
     * @return the rfs name for the given locale
     */
    public String getLocalizedRfsName(String rfsName, String fileSeparator) {

        String locRfsName = null;

        // this might be too simple
        locRfsName = CmsStringUtil.substitute(
            rfsName,
            fileSeparator + CmsLocaleManager.getDefaultLocale().toString() + fileSeparator,
            fileSeparator + getName() + fileSeparator);
        return locRfsName;
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
    public List<Pattern> getRelatedSystemResources() {

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
     * Returns the configured url Prefix with unsubstituted context values.<p>
     *
     * @return the configured url Prefix
     */
    public String getRfsPrefixConfigured() {

        return m_rfsPreConfigured;
    }

    /**
     * Returns the source regular expression pattern.<p>
     *
     * @return the source regular expression pattern
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
     *
     * @return true if the name matches one of the given related system resource patterns
     */
    public boolean match(String vfsName) {

        for (int j = 0; j < m_relatedSystemResources.size(); j++) {
            Pattern pattern = m_relatedSystemResources.get(j);
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
     * Sets the rfs export work Path after normalizing.<p>
     *
     * @param exportWorkPath the rfs export Work Path to set
     */
    public void setExportWorkPath(String exportWorkPath) {

        if (exportWorkPath.equals(OpenCms.getSystemInfo().getWebApplicationRfsPath())) {
            // not allowed because a full static export would delete the opencms directory
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_INVALID_EXPORT_PATH_1, m_name));
        }
        m_exportWorkPath = exportWorkPath;
    }

    /**
     * Sets the url Prefix after normalizing.<p>
     *
     * @param rfsPrefix the url Prefix to set
     */
    public void setRfsPrefix(String rfsPrefix) {

        m_rfsPrefix = rfsPrefix;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
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
