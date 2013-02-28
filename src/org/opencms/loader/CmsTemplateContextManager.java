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

package org.opencms.loader;

import org.opencms.ade.containerpage.Messages;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexController;
import org.opencms.gwt.shared.CmsTemplateContextInfo;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsDefaultSet;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

/**
 * Manager class for template context providers.<p>
 */
public class CmsTemplateContextManager {

    /** A bean containing information about the selected template. */
    public static final String ATTR_TEMPLATE_BEAN = "ATTR_TEMPLATE_BEAN";

    /** The request attribute in which the template context is stored. */
    public static final String ATTR_TEMPLATE_CONTEXT = "templateContext";

    /** Attribute name which contains the template name for non-dynamically selected templates. */
    public static final String ATTR_TEMPLATE_NAME = "cmsTemplateName";

    /** Attribute name for the template resource. */
    public static final String ATTR_TEMPLATE_RESOURCE = "cmsTemplateResource";

    /** The prefix used in the template property to activate dynamic template selection. */
    public static final String DYNAMIC_TEMPLATE_PREFIX = "provider=";

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsTemplateContextManager.class);

    /** The CMS context. */
    private CmsObject m_cms;

    /** A cache in which the template context provider instances are stored, with their class name as the key. */
    private Map<String, I_CmsTemplateContextProvider> m_providerInstances = new HashMap<String, I_CmsTemplateContextProvider>();

    /**
     * Creates a new instance.<p>
     * 
     * @param cms the CMS context to use
     */
    public CmsTemplateContextManager(CmsObject cms) {

        m_cms = cms;
        CmsFlexController.registerUncacheableAttribute(ATTR_TEMPLATE_RESOURCE);
        CmsFlexController.registerUncacheableAttribute(ATTR_TEMPLATE_CONTEXT);
        CmsFlexController.registerUncacheableAttribute(ATTR_TEMPLATE_RESOURCE);
    }

    /**
     * Creates a bean with information about the current template context, for use in the client-side code.<p>
     *  
     * @param cms the current CMS context 
     * @param request the current request
     * 
     * @return the bean with the template context information  
     */
    public CmsTemplateContextInfo getContextInfoBean(CmsObject cms, HttpServletRequest request) {

        CmsTemplateContextInfo result = new CmsTemplateContextInfo();
        CmsTemplateContext context = (CmsTemplateContext)request.getAttribute(ATTR_TEMPLATE_CONTEXT);
        if (context != null) {
            result.setCurrentContext(context.getKey());

            I_CmsTemplateContextProvider provider = context.getProvider();
            Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
            CmsXmlContentProperty settingDefinition = createTemplateContextsPropertyDefinition(provider, locale);
            result.setSettingDefinition(settingDefinition);
            String cookieName = context.getProvider().getOverrideCookieName();
            if (cookieName != null) {
                String cookieValue = CmsRequestUtil.getCookieValue(request.getCookies(), cookieName);
                result.setSelectedContext(cookieValue);
            }
            result.setCookieName(cookieName);
            Map<String, String> niceNames = new HashMap<String, String>();
            for (Map.Entry<String, CmsTemplateContext> entry : provider.getAllContexts().entrySet()) {
                CmsTemplateContext otherContext = entry.getValue();
                String niceName = otherContext.getLocalizedName(locale);
                niceNames.put(otherContext.getKey(), niceName);
            }
            result.setContextLabels(niceNames);
            result.setContextProvider(provider.getClass().getName());
        }
        Map<String, CmsDefaultSet<String>> allowedContextMap = safeGetAllowedContextMap();
        result.setAllowedContexts(allowedContextMap);
        return result;
    }

    /**
     * Gets the template context to use.<p>
     * 
     * @param providerName the name of the template context provider 
     * @param cms the current CMS context
     * @param request the current request 
     * @param resource the current resource
     *  
     * @return the current template context 
     */
    public CmsTemplateContext getTemplateContext(
        String providerName,
        CmsObject cms,
        HttpServletRequest request,
        CmsResource resource) {

        I_CmsTemplateContextProvider provider = getTemplateContextProvider(providerName);
        if (provider == null) {
            return null;
        }
        String cookieName = provider.getOverrideCookieName();
        if (cookieName != null) {
            String cookieValue = CmsRequestUtil.getCookieValue(request.getCookies(), cookieName);
            if (cookieValue != null) {
                Map<String, CmsTemplateContext> contextMap = provider.getAllContexts();
                if (contextMap.containsKey(cookieValue)) {
                    return contextMap.get(cookieValue);
                }
            }
        }
        return provider.getTemplateContext(cms, request, resource);
    }

    /**
     * Gets the template context provider for a given path.<p>
     * 
     * @param cms the current CMS context 
     * @param path the path for which the template context provider should be determined 
     * 
     * @return the template context provider for the given path 
     * 
     * @throws CmsException if something goes wrong 
     */
    public I_CmsTemplateContextProvider getTemplateContextProvider(CmsObject cms, String path) throws CmsException {

        CmsResource resource = cms.readResource(path);
        I_CmsResourceLoader loader = OpenCms.getResourceManager().getLoader(resource);
        if (loader instanceof A_CmsXmlDocumentLoader) {
            String propertyName = ((A_CmsXmlDocumentLoader)loader).getTemplatePropertyDefinition();
            List<CmsProperty> properties = cms.readPropertyObjects(resource, true);
            CmsProperty property = CmsProperty.get(propertyName, properties);
            if ((property != null) && !property.isNullProperty()) {
                String propertyValue = property.getValue();
                if (propertyValue.startsWith(DYNAMIC_TEMPLATE_PREFIX)) {
                    return getTemplateContextProvider(propertyValue.substring(DYNAMIC_TEMPLATE_PREFIX.length()));
                }
            }
            return null;
        } else {
            return null;
        }
    }

    /** 
     * Retrieves an instance of a template context provider given its name (optionally prefixed by the 'dynamic:' prefix).<p>
     * 
     * @param providerName the name of the provider 
     * 
     * @return an instance of the provider class 
     */
    public I_CmsTemplateContextProvider getTemplateContextProvider(String providerName) {

        providerName = providerName.trim();
        if (providerName.startsWith(DYNAMIC_TEMPLATE_PREFIX)) {
            providerName = providerName.substring(DYNAMIC_TEMPLATE_PREFIX.length());
        }
        I_CmsTemplateContextProvider result = m_providerInstances.get(providerName);
        if (result == null) {
            try {
                Class<?> providerClass = Class.forName(providerName);
                if (I_CmsTemplateContextProvider.class.isAssignableFrom(providerClass)) {
                    result = (I_CmsTemplateContextProvider)providerClass.newInstance();
                    result.initialize(m_cms);
                    m_providerInstances.put(providerName, result);
                }
            } catch (Throwable t) {
                LOG.error(t.getLocalizedMessage(), t);
            }
        }
        return result;
    }

    /**
     * Helper method to check whether a given type should not be shown in a context.<p>
     * 
     * @param context the template context 
     * @param typeName the type name 
     * 
     * @return true if the context does not prohibit showing the type 
     */
    public boolean shouldShowType(CmsTemplateContext context, String typeName) {

        Map<String, CmsDefaultSet<String>> allowedContextMap = safeGetAllowedContextMap();
        CmsDefaultSet<String> allowedContexts = allowedContextMap.get(typeName);
        if (allowedContexts == null) {
            return true;
        }
        return allowedContexts.contains(context.getKey());
    }

    /**
     * Creates the setting definition for the templateContexts setting.<p>
     * 
     * @param contextProvider the context provider 
     * @param locale the current locale
     *  
     * @return the setting definition  
     */
    protected CmsXmlContentProperty createTemplateContextsPropertyDefinition(
        I_CmsTemplateContextProvider contextProvider,
        Locale locale) {

        if (contextProvider == null) {
            return null;
        }
        List<String> contextOptions = new ArrayList<String>();
        for (CmsTemplateContext context : contextProvider.getAllContexts().values()) {
            contextOptions.add(context.getKey() + ":" + context.getLocalizedName(locale));
        }
        String widgetConfig = CmsStringUtil.listAsString(contextOptions, "|");

        String niceName = Messages.get().getBundle(locale).key(Messages.GUI_SETTING_TEMPLATE_CONTEXTS_NAME_0);
        String description = Messages.get().getBundle(locale).key(Messages.GUI_SETTING_TEMPLATE_CONTEXTS_DESCRIPTION_0);
        CmsXmlContentProperty propDef = new CmsXmlContentProperty(
            CmsTemplateContextInfo.SETTING,
            "string",
            "multicheck",
            widgetConfig,
            null,
            null,
            "",
            niceName,
            description,
            "",
            "false");
        return propDef;
    }

    /**
     * Helper method for getting the forbidden contexts from the resource manager without a try-catch block.<p>
     * 
     * @return the forbidden context map
     */
    protected Map<String, CmsDefaultSet<String>> safeGetAllowedContextMap() {

        try {
            return OpenCms.getResourceManager().getAllowedContextMap(m_cms);
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return Collections.emptyMap();
        }
    }
}
