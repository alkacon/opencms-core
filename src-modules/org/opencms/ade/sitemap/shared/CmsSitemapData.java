/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/shared/Attic/CmsSitemapData.java,v $
 * Date   : $Date: 2010/04/22 14:32:08 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.sitemap.shared;

import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Sitemap initialization data.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0
 */
public class CmsSitemapData implements IsSerializable {

    /** The default template. */
    private CmsSitemapTemplate m_defaultTemplate;

    /** The sitemap properties. */
    private Map<String, CmsXmlContentProperty> m_properties;

    /** The recent list. */
    private List<CmsClientSitemapEntry> m_recentList;

    /** The available templates. */
    private Map<String, CmsSitemapTemplate> m_templates;

    /**
     * Constructor.<p>
     */
    public CmsSitemapData() {

        // empty
    }

    /**
     * Constructor.<p>
     * 
     * @param defaultTemplate the default template
     * @param templates the available templates
     * @param properties the properties
     * @param recentList the recent list
     */
    public CmsSitemapData(
        CmsSitemapTemplate defaultTemplate,
        Map<String, CmsSitemapTemplate> templates,
        Map<String, CmsXmlContentProperty> properties,
        List<CmsClientSitemapEntry> recentList) {

        m_defaultTemplate = defaultTemplate;
        m_templates = templates;
        m_properties = properties;
        m_recentList = recentList;
    }

    /**
     * Returns the default template.<p>
     *
     * @return the default template
     */
    public CmsSitemapTemplate getDefaultTemplate() {

        return m_defaultTemplate;
    }

    /**
     * Returns the properties.<p>
     *
     * @return the properties
     */
    public Map<String, CmsXmlContentProperty> getProperties() {

        return m_properties;
    }

    /**
     * Returns the recent list.<p>
     *
     * @return the recent list
     */
    public List<CmsClientSitemapEntry> getRecentList() {

        return m_recentList;
    }

    /**
     * Returns the available templates.<p>
     *
     * @return the available templates
     */
    public Map<String, CmsSitemapTemplate> getTemplates() {

        return m_templates;
    }

}
