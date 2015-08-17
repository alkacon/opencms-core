/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt.shared;

import org.opencms.util.CmsDefaultSet;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Client-compatible bean with information about the current template context.<p>
 */
public class CmsTemplateContextInfo implements IsSerializable {

    /** Dummy element marker class. */
    public static final String DUMMY_ELEMENT_MARKER = "cmsTemplateContextDummyMarker";

    /** The constant used for empty setting. */
    public static final String EMPTY_VALUE = "none";

    /** The setting name used for storing the compatible template contexts. */
    public static final String SETTING = "templateContexts";

    /** The map of allowed contexts for each type. */
    private Map<String, CmsDefaultSet<String>> m_allowedContextMap;

    /** Client variant information. */
    private Map<String, Map<String, CmsClientVariantInfo>> m_clientVariantInfo = new LinkedHashMap<String, Map<String, CmsClientVariantInfo>>();

    /** A map from the names of all available template contexts to their localized names. */
    private Map<String, String> m_contextLabels = new LinkedHashMap<String, String>();

    /** The context provider class. */
    private String m_contextProvider;

    /** The name of the cookie used for overriding the template context. */
    private String m_cookieName;

    /** The key of the currently active context. */
    private String m_currentContext;

    /** The name of the selected context (using the cookie) .*/
    private String m_selectedContext;

    /** The setting definition for the templateContexts setting. */
    private CmsXmlContentProperty m_settingDefinition;

    /**
     * Default constructor.<p>
     */
    public CmsTemplateContextInfo() {

        // default constructor
    }

    /**
     * Gets the map of forbidden contexts for resource types.<p>
     *
     * @return the map of forbidden contexts for resource types
     */
    public Map<String, CmsDefaultSet<String>> getAllowedContexts() {

        return m_allowedContextMap;

    }

    /**
     * Gets the client variant information for a specific context.<p>
     *
     * @param context the context name
     *
     * @return the client variant information for that context
     */
    public Map<String, CmsClientVariantInfo> getClientVariants(String context) {

        return m_clientVariantInfo.get(context);
    }

    /**
     * Gets the map of labels for the different template contexts.<p>
     *
     * @return the map of template context labels
     */
    public Map<String, String> getContextLabels() {

        return m_contextLabels;
    }

    /**
     * Gets the name of the context provider class.<p>
     *
     * @return the name of the context provider class
     */
    public String getContextProvider() {

        return m_contextProvider;
    }

    /**
     * Gets the name of the cookie used for overriding the template context.<p>
     *
     * @return the cookie name
     */
    public String getCookieName() {

        return m_cookieName;
    }

    /**
     * Gets the key of the currently active template context.<p>
     *
     * @return the key of the currently active template context
     */
    public String getCurrentContext() {

        return m_currentContext;
    }

    /**
     * Gets the key of the currently selected template context, using the cookie.<p>
     *
     * @return the name of the currently selected template context
     */
    public String getSelectedContext() {

        return m_selectedContext;
    }

    /**
     * Gets the property definition for the templateContexts setting.<p>
     *
     * @return the property definition for the templateContexts setting
     */
    public CmsXmlContentProperty getSettingDefinition() {

        return m_settingDefinition;
    }

    /**
     * Checks if client variants for the given context are present.<p>
     *
     * @param context a context name
     * @return true if there are client variants for the context
     */
    public boolean hasClientVariants(String context) {

        return m_clientVariantInfo.containsKey(context);
    }

    /**
     * Sets the allowed contexts.<p>
     *
     * @param allowedContextMap the map of allowed contexts
     */
    public void setAllowedContexts(Map<String, CmsDefaultSet<String>> allowedContextMap) {

        m_allowedContextMap = allowedContextMap;
    }

    /**
     * Adds a client variant.<p>
     *
     * @param context a context name
     * @param variant the variant name
     * @param info the bean with the variant information
     */
    public void setClientVariant(String context, String variant, CmsClientVariantInfo info) {

        if (!m_clientVariantInfo.containsKey(context)) {
            Map<String, CmsClientVariantInfo> variants = new LinkedHashMap<String, CmsClientVariantInfo>();
            m_clientVariantInfo.put(context, variants);
        }
        m_clientVariantInfo.get(context).put(variant, info);
    }

    /**
     * Sets the map of labels for the contexts.<p>
     *
     * @param contextLabels the map of context labels
     */
    public void setContextLabels(Map<String, String> contextLabels) {

        m_contextLabels = contextLabels;
    }

    /**
     * Sets the context provider class name.<p>
     *
     * @param contextProvider the context provider class name
     */
    public void setContextProvider(String contextProvider) {

        m_contextProvider = contextProvider;
    }

    /**
     * Sets the name of the cookie used for overriding the template context.<p>
     *
     * @param cookieName the name of the cookie used for overriding the template context
     */
    public void setCookieName(String cookieName) {

        m_cookieName = cookieName;
    }

    /**
     * Sets the active context.<p>
     *
     * @param context the active context
     */
    public void setCurrentContext(String context) {

        m_currentContext = context;
    }

    /**
     * Sets the selected context.<p>
     *
     * @param selectedContext the selected context
     */
    public void setSelectedContext(String selectedContext) {

        m_selectedContext = selectedContext;
    }

    /**
     * Sets the property definition for the templateContexts setting.<p>
     *
     * @param definition the property definition
     */
    public void setSettingDefinition(CmsXmlContentProperty definition) {

        m_settingDefinition = definition;
    }

    /**
     * Returns true if the template context selection should be shown for container elements.<p>
     *
     * @return true if the template context selection for elements should be shown
     */
    public boolean shouldShowElementTemplateContextSelection() {

        return hasMoreThanOneOption();
    }

    /**
     * Returns true if the template context selection context menu entry should be shown.<p>
     *
     * @return true if the template context selection context menu entry should be shown
     */
    public boolean shouldShowTemplateContextContextMenuEntry() {

        return hasMoreThanOneOption();

    }

    /**
     * Returns true if there is more than one template context to choose from.<p>
     *
     * @return true if there is more than one template context
     */
    private boolean hasMoreThanOneOption() {

        return (m_currentContext != null) && (m_contextLabels.size() > 1);
    }
}
