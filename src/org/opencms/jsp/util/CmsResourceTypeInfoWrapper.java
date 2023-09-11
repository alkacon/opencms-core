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
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsWorkplaceMessages;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.xml.containerpage.I_CmsFormatterBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Wrapper for resource type information for use in JSPs.
 */
public class CmsResourceTypeInfoWrapper implements I_CmsFormatterInfo {

    /** Whether the type is active in the sitemap configuration. */
    private boolean m_active;

    /** The active formatters. */
    private List<I_CmsFormatterBean> m_activeFormatters = new ArrayList<>();

    /** The active formatters grouped by container type. */
    private Multimap<String, I_CmsFormatterBean> m_activeFormattersByContainerType = ArrayListMultimap.create();

    /** The current CMS context. */
    private CmsObject m_cms;

    /** The current sitemap configuration. */
    private CmsADEConfigData m_config;

    /** The wrapped resource type. */
    private I_CmsResourceType m_type;

    private CmsJspStandardContextBean m_context;

    /**
     * Creates a new instance.
     *
     * @param context the standard context bean
     * @param cms the current CMS context
     * @param config the current sitemap configuration
     * @param type the type to wrap
     */
    public CmsResourceTypeInfoWrapper(
        CmsJspStandardContextBean context,
        CmsObject cms,
        CmsADEConfigData config,
        I_CmsResourceType type) {

        m_cms = cms;
        m_config = config;
        m_type = type;
        m_context = context;
        for (I_CmsFormatterBean formatter : config.getActiveFormatters().values()) {
            if (!formatter.getResourceTypeNames().contains(type.getTypeName())) {
                continue;
            }
            m_activeFormatters.add(formatter);
            for (String containerType : formatter.getContainerTypes()) {
                m_activeFormattersByContainerType.put(containerType, formatter);
            }
        }
        m_active = m_config.getResourceTypes().stream().anyMatch(
            sitemapConfigType -> m_type.getTypeName().equals(sitemapConfigType.getTypeName()));
    }

    /**
     * Gets the description for the type in the given locale.
     *
     * @param locale the locale to use
     * @return the type description
     */
    public String description(Locale locale) {

        try {
            return CmsWorkplaceMessages.getResourceTypeDescription(locale, m_type.getTypeName());
        } catch (Throwable e) {
            return m_type.getTypeName();
        }
    }

    /**
     * Gets the formatter information beans for a specific container type.
     *
     * @param containerType the container type
     * @return the formatter information
     */
    public List<CmsFormatterInfoWrapper> formatterInfoForContainer(String containerType) {

        return m_context.wrapFormatters(m_activeFormattersByContainerType.get(containerType));

    }

    /**
     * Gets the type description in the current locale.
     *
     * @return the type description
     */
    public String getDescription() {

        return description(m_cms.getRequestContext().getLocale());
    }

    /**
     * @see org.opencms.jsp.util.I_CmsFormatterInfo#getDescriptionKey()
     */
    public String getDescriptionKey() {

        CmsExplorerTypeSettings explorerType = OpenCms.getWorkplaceManager().getExplorerTypeSetting(
            m_type.getTypeName());
        return explorerType.getInfo();
    }

    /**
     * @see org.opencms.jsp.util.I_CmsFormatterInfo#getDescriptionRaw()
     */
    public String getDescriptionRaw() {

        return getDescriptionKey();
    }

    /**
     * Gets the set of container types configured for any active formatters for this resource type.
     *
     * @return the set of container types for formatters
     */
    public Set<String> getFormatterContainerTypes() {

        return Collections.unmodifiableSet(m_activeFormattersByContainerType.keySet());
    }

    /**
     * Gets the formatter information beans for all active formatters for this type.
     *
     * @return the formatter information beans
     */
    public List<CmsFormatterInfoWrapper> getFormatterInfo() {

        return m_context.wrapFormatters(m_activeFormatters);

    }

    /**
     * Returns true if the type is active in the current sitemap configuration.
     *
     * @return true if the type is active
     */
    public boolean getIsActive() {

        return m_active;

    }

    /**
     * @see org.opencms.jsp.util.I_CmsFormatterInfo#getIsFormatter()
     */
    public boolean getIsFormatter() {

        return false;
    }

    /**
     * @see org.opencms.jsp.util.I_CmsFormatterInfo#getIsFunction()
     */
    public boolean getIsFunction() {

        return false;
    }

    /**
     * @see org.opencms.jsp.util.I_CmsFormatterInfo#getIsResourceType()
     */
    public boolean getIsResourceType() {

        return true;
    }

    /**
     * Gets the type name.
     *
     * @return the type name
     */
    public String getName() {

        return m_type.getTypeName();
    }

    /**
     * Gets the user-readable nice name of the type in the current locale.
     *
     * @return the nice name
     */
    public String getNiceName() {

        return niceName(m_cms.getRequestContext().getLocale());
    }

    /**
     * @see org.opencms.jsp.util.I_CmsFormatterInfo#getNiceNameKey()
     */
    public String getNiceNameKey() {

        CmsExplorerTypeSettings explorerType = OpenCms.getWorkplaceManager().getExplorerTypeSetting(
            m_type.getTypeName());
        return explorerType.getKey();

    }

    /**
     * @see org.opencms.jsp.util.I_CmsFormatterInfo#getNiceNameRaw()
     */
    public String getNiceNameRaw() {

        return getNiceNameKey();
    }

    /**
     * Gets the nice name of the type in the given locale.
     *
     * @param locale the locale to use
     * @return the nice name for the type
     */
    public String niceName(Locale locale) {

        try {
            return CmsWorkplaceMessages.getResourceTypeName(locale, m_type.getTypeName());
        } catch (Throwable e) {
            return m_type.getTypeName();
        }
    }

}
