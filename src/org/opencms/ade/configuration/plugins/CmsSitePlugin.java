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

package org.opencms.ade.configuration.plugins;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentRootLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Immutable collection of template plugins, normally read from a file of type site-plugin.
 */
public class CmsSitePlugin {

    /** Content value node name. */
    public static final String N_DESCRIPTION = "Description";

    /** Content value node name. */
    public static final String N_NICE_NAME = "NiceName";

    /** Content value node name. */
    public static final String N_PLUGIN = "Plugin";

    /** The nice name. */
    private String m_niceName;

    /** The description. */
    private String m_description;

    /** The origin, for debugging. */
    private String m_origin;

    /** The list of plugins. */
    private List<CmsTemplatePlugin> m_plugins;

    /** The id (normally the structure id of the content from which this was read. */
    private CmsUUID m_id;

    /**
     * Creates a new instance.
     *
     * @param id the id of the plugin group
     * @param niceName the nice name
     * @param description the description
     * @param plugins the list of plugins
     * @param origin the origin (for debugging purposes)
     */
    public CmsSitePlugin(
        CmsUUID id,
        String niceName,
        String description,
        List<CmsTemplatePlugin> plugins,
        String origin) {

        m_id = id;
        m_plugins = Collections.unmodifiableList(new ArrayList<>(plugins));
        m_niceName = niceName;
        m_description = description;
        m_origin = origin;
    }

    /**
     * Reads a site plugin from a file.
     *
     * @param cms the CMS context to use
     * @param res the resource
     * @return the site plugin read from the file
     * @throws CmsException if something goes wrong
     */
    public static CmsSitePlugin read(CmsObject cms, CmsResource res) throws CmsException {

        CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, cms.readFile(res));
        return readSitePlugin(cms, content);

    }

    /**
     * Reads a list of plugins from the given XML content.
     *
     * @param cms the CMS context
     * @param content the XML content object
     * @return the template plugin group
     */
    public static CmsSitePlugin readSitePlugin(CmsObject cms, CmsXmlContent content) {

        CmsXmlContentRootLocation root = new CmsXmlContentRootLocation(content, Locale.ENGLISH);
        String niceName = root.getSubValue(N_NICE_NAME).getValue().getStringValue(cms).trim();
        String description = root.getSubValue(N_DESCRIPTION).getValue().getStringValue(cms).trim();
        List<CmsTemplatePlugin> plugins = CmsTemplatePlugin.parsePlugins(cms, root, N_PLUGIN);
        CmsSitePlugin result = new CmsSitePlugin(
            content.getFile().getStructureId(),
            niceName,
            description,
            plugins,
            content.getFile().getRootPath());
        return result;

    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription() {

        return m_description;
    }

    /**
     * Gets the id.
     *
     * <p>Normally, this is the structure id of the file from which this group was read.
     *
     * @return the id
     */
    public CmsUUID getId() {

        return m_id;
    }

    /**
     * Gets the nice name.
     *
     * @return the nice name
     */
    public String getNiceName() {

        return m_niceName;
    }

    /**
     * Gets the origin, for debugging purposes.
     *
     * @return the origin
     */
    public String getOrigin() {

        return m_origin;
    }

    /**
     * Gets the immutable list of plugins.
     *
     * @return the list of plugins in this collection
     */
    public List<CmsTemplatePlugin> getPlugins() {

        return m_plugins;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
