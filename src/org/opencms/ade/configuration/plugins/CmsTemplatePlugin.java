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

import org.opencms.relations.CmsLinkInfo;

/**
 * Bean representing a template plugin.
 *
 * <p>Template plugins are links to external resources which are categorized into named groups and provided either by
 * formatters of elements in the current page, or by the current sitemap configuration. They can be programmatically
 * accessed by group name from the template. They also have an order attribute to control the order in which they wil be returned
 * (plugins will be sorted by descending order).
 */
public class CmsTemplatePlugin {

    /** The plugin group. */
    private String m_group;

    /** The order of the plugin. */
    private int m_order;

    /** The origin of the plugin, for debugging purposes. */
    private String m_origin;

    /** The plugin target. */
    private CmsLinkInfo m_target;

    /**
     * Creates a new instance.
     *
     * @param target the plugin target
     * @param group the plugin group
     * @param order the plugin order
     * @param origin the origin from which the plugin was read (for debugging)
     */
    public CmsTemplatePlugin(CmsLinkInfo target, String group, int order, String origin) {

        m_target = target;
        m_group = group;
        m_order = order;
        m_origin = origin;
    }

    /**
     * Gets the plugin group.
     *
     * @return the plugin group
     */
    public String getGroup() {

        return m_group;
    }

    /**
     * Returns the plugin order.
     *
     * <p>Plugins are sorted by descending order when retrieved in the template.
     *
     * @return the plugin order
     */
    public int getOrder() {

        return m_order;
    }

    /**
     * The plugin origin, for debugging purposes.
     *
     * @return the plugin origin
     */
    public String getOrigin() {

        return m_origin;
    }

    /**
     * Gets the plugin target.
     *
     * <p>When the plugins for a given group are retrieved, duplicate targets will be removed, and the position
     * of each target in the resulting list is decided by the highest order for that target.
     *
     * @return the target
     */
    public CmsLinkInfo getTarget() {

        return m_target;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return "[CmsTemplatePlugin: " + getTarget().toString() + "," + getGroup() + "," + getOrder() + "]";
    }
}
