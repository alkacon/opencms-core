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

package org.opencms.jsp.util;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.file.CmsObject;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.OpenCms;
import org.opencms.xml.containerpage.CmsFormatterBean;
import org.opencms.xml.containerpage.CmsFunctionFormatterBean;
import org.opencms.xml.containerpage.I_CmsFormatterBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Wrapper bean for querying information related to a container type in JSPs.
 */
public class CmsContainerTypeInfoWrapper {

    /** The  CMS context. */
    private CmsObject m_cms;

    /** The current sitemap configuration. */
    private CmsADEConfigData m_config;

    /** The container type. */
    private String m_containerType;

    /** The matching resource types. */
    private Set<String> m_matchingResourceTypes = new TreeSet<>();

    /** The list of matching functions. */
    private List<CmsFunctionFormatterBean> m_matchingFunctions = new ArrayList<>();

    private CmsJspStandardContextBean m_context;

    /**
     * Creates a new instance.
     * @param context the standard context bean
     * @param cms the CMS context
     * @param config the sitemap configuration
     * @param type the container type to wrap
     */
    public CmsContainerTypeInfoWrapper(
        CmsJspStandardContextBean context,
        CmsObject cms,
        CmsADEConfigData config,
        String type) {

        m_cms = cms;
        m_config = config;
        m_context = context;
        m_containerType = type;
        for (I_CmsFormatterBean formatter : m_config.getActiveFormatters().values()) {
            if ((formatter.getContainerTypes() != null) && formatter.getContainerTypes().contains(m_containerType)) {
                if (formatter instanceof CmsFunctionFormatterBean) {
                    m_matchingFunctions.add((CmsFunctionFormatterBean)formatter);
                } else if (formatter instanceof CmsFormatterBean) {
                    m_matchingResourceTypes.addAll(formatter.getResourceTypeNames());
                }
            }
        }
    }

    /**
     * Gets the combined list of both resource types which have matching formatter for this container type, and functions which match this container type.
     *
     * @return the combined list of resource types and functions for this container type
     */
    public List<I_CmsFormatterInfo> getAllowedElements() {

        List<I_CmsFormatterInfo> result = new ArrayList<>();
        result.addAll(getAllowedResourceTypes());
        result.addAll(getAllowedFunctions());
        return result;
    }

    /**
     * Gets the list of active functions which match this container type.-
     *
     * @return the list of matching functions for the container type
     */
    public List<CmsFormatterInfoWrapper> getAllowedFunctions() {

        return m_context.wrapFormatters(m_matchingFunctions);

    }

    /**
     * Gets the list of resource types which have active formatters matching this container type.
     *
     * @return the list of resource types matching this container type
     */
    public List<I_CmsFormatterInfo> getAllowedResourceTypes() {

        List<I_CmsFormatterInfo> result = new ArrayList<>();
        for (String name : m_matchingResourceTypes) {
            try {
                I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(name);
                CmsResourceTypeInfoWrapper wrapper = new CmsResourceTypeInfoWrapper(m_context, m_cms, m_config, type);
                result.add(wrapper);
            } catch (CmsLoaderException e) {
                // type not found, ignore and continue
            }
        }
        return result;
    }

}
