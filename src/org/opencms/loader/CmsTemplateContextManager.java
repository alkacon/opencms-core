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
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.flex.CmsFlexController;
import org.opencms.gwt.shared.CmsClientVariantInfo;
import org.opencms.gwt.shared.CmsGwtConstants;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

/**
 * Manager class for template context providers.<p>
 */
public class CmsTemplateContextManager {

    /** Request attribute used to set the template context during RPC calls. */
    public static final String ATTR_RPC_CONTEXT_OVERRIDE = "ATTR_RPC_CONTEXT_OVERRIDE";

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

    /** Legacy prefix for property providers. */
    private static final String DYNAMIC_TEMPLATE_LEGACY_PREFIX = "dynamic:";

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsTemplateContextManager.class);

    /** Cached allowed context map. */
    private volatile Map<String, CmsDefaultSet<String>> m_cachedContextMap = null;

    /** The CMS context. */
    private CmsObject m_cms;

    /** A cache in which the template context provider instances are stored, with their class name as the key. */
    private Map<String, I_CmsTemplateContextProvider> m_providerInstances = new ConcurrentHashMap<String, I_CmsTemplateContextProvider>();

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
        OpenCms.getExecutor().scheduleWithFixedDelay(this::updateContextMap, 1, 15, TimeUnit.SECONDS);
    }

    /**
     * Checks if the property value starts with the prefix which marks a dynamic template provider.<p>
     *
     * @param propertyValue the property value to check
     * @return true if the value has the format of a dynamic template provider
     */
    public static boolean hasPropertyPrefix(String propertyValue) {

        return (propertyValue != null)
            && (propertyValue.startsWith(DYNAMIC_TEMPLATE_PREFIX)
                || propertyValue.startsWith(DYNAMIC_TEMPLATE_LEGACY_PREFIX));
    }

    /**
     * Checks if a template property value refers to a  template context provider.<p>
     *
     * @param templatePath the template property value
     * @return true if this value refers to a template context provider
     */
    public static boolean isProvider(String templatePath) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(templatePath)) {
            return false;
        }
        templatePath = templatePath.trim();
        return templatePath.startsWith(DYNAMIC_TEMPLATE_LEGACY_PREFIX)
            || templatePath.startsWith(DYNAMIC_TEMPLATE_PREFIX);
    }

    /**
     * Removes the prefix which marks a property value as a dynamic template provider.<p>
     *
     * @param propertyValue the value from which to remove the prefix
     *
     * @return the string with the prefix removed
     */
    public static String removePropertyPrefix(String propertyValue) {

        if (propertyValue == null) {
            return null;
        }
        if (propertyValue.startsWith(DYNAMIC_TEMPLATE_PREFIX)) {
            return propertyValue.substring(DYNAMIC_TEMPLATE_PREFIX.length());
        }
        if (propertyValue.startsWith(DYNAMIC_TEMPLATE_LEGACY_PREFIX)) {
            return propertyValue.substring(DYNAMIC_TEMPLATE_LEGACY_PREFIX.length());
        }
        return propertyValue;
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
            result.setMenuLabel(provider.getMenuLabel(locale));
            result.setDefaultLabel(provider.getDefaultLabel(locale));
            result.setShouldShowElementTemplateContextSelection(
                provider.shouldShowElementTemplateContextSelection(cms));
            CmsXmlContentProperty settingDefinition = createTemplateContextsPropertyDefinition(provider, locale);
            result.setSettingDefinition(settingDefinition);
            String cookieName = context.getProvider().getOverrideCookieName();
            if (cookieName != null) {
                String cookieValue = CmsRequestUtil.getCookieValue(request.getCookies(), cookieName);
                result.setSelectedContext(cookieValue);
            }
            result.setCookieName(cookieName);
            Map<String, String> niceNames = new LinkedHashMap<String, String>();
            for (Map.Entry<String, CmsTemplateContext> entry : provider.getAllContexts().entrySet()) {
                CmsTemplateContext otherContext = entry.getValue();
                if (provider.isHiddenContext(otherContext.getKey())) {
                    continue;
                }
                String niceName = otherContext.getLocalizedName(locale);
                niceNames.put(otherContext.getKey(), niceName);
                for (CmsClientVariant variant : otherContext.getClientVariants().values()) {
                    CmsClientVariantInfo info = new CmsClientVariantInfo(
                        variant.getName(),
                        variant.getNiceName(locale),
                        variant.getScreenWidth(),
                        variant.getScreenHeight(),
                        variant.getParameters());
                    result.setClientVariant(otherContext.getKey(), variant.getName(), info);
                }
            }
            result.setContextLabels(niceNames);
            String providerKey = OpenCms.getTemplateContextManager().getProviderKey(provider);
            result.setContextProvider(providerKey);
        }
        Map<String, CmsDefaultSet<String>> allowedContextMap = safeGetAllowedContextMap();
        result.setAllowedContexts(allowedContextMap);
        return result;
    }

    /**
     * Gets the key of a cached template provider (consisting of class name and parameters)
     * that can later be used as an argument to getTemplateContextProvider.
     *
     * <p>If the provider is not already cached, returns null.
     *
     * @param provider the template provider
     *
     * @return the cache key
     */
    public String getProviderKey(I_CmsTemplateContextProvider provider) {

        // Just do a linear search over the map entries. There should only be a small number of different configured template providers,
        // so this is not a problem for performance.
        for (Map.Entry<String, I_CmsTemplateContextProvider> entry : m_providerInstances.entrySet()) {
            if (entry.getValue() == provider) {
                return entry.getKey();
            }
        }
        return null;
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
        String forcedValue = null;
        if (request != null) {
            String paramTemplateContext = request.getParameter(CmsGwtConstants.PARAM_TEMPLATE_CONTEXT);
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(paramTemplateContext)) {
                forcedValue = paramTemplateContext;
            } else if (cookieName != null) {
                forcedValue = CmsRequestUtil.getCookieValue(request.getCookies(), cookieName);
            }
        }
        if (forcedValue != null) {
            Map<String, CmsTemplateContext> contextMap = provider.getAllContexts();
            if (contextMap.containsKey(forcedValue)) {
                CmsTemplateContext contextBean = contextMap.get(forcedValue);
                return new CmsTemplateContext(
                    contextBean.getKey(),
                    contextBean.getTemplatePath(),
                    contextBean.getMessageContainer(),
                    contextBean.getProvider(),
                    contextBean.getClientVariants().values(),
                    true);

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

        CmsResource resource = cms.readResource(path, CmsResourceFilter.IGNORE_EXPIRATION);
        I_CmsResourceLoader loader = OpenCms.getResourceManager().getLoader(resource);
        if (loader instanceof A_CmsXmlDocumentLoader) {
            String propertyName = ((A_CmsXmlDocumentLoader)loader).getTemplatePropertyDefinition();
            List<CmsProperty> properties = cms.readPropertyObjects(resource, true);
            CmsProperty property = CmsProperty.get(propertyName, properties);
            if ((property != null) && !property.isNullProperty()) {
                String propertyValue = property.getValue();
                if (CmsTemplateContextManager.hasPropertyPrefix(propertyValue)) {
                    return getTemplateContextProvider(removePropertyPrefix(propertyValue));
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

        if (providerName == null) {
            return null;
        }
        providerName = providerName.trim();
        providerName = removePropertyPrefix(providerName);
        String providerClassName = providerName;
        String providerConfig = "";

        // get provider configuration string if available
        int separatorIndex = providerName.indexOf(",");
        if (separatorIndex > 0) {
            providerClassName = providerName.substring(0, separatorIndex);
            providerConfig = providerName.substring(separatorIndex + 1);
        }

        I_CmsTemplateContextProvider result = m_providerInstances.get(providerName);
        if (result == null) {
            try {
                Class<?> providerClass = Class.forName(providerClassName, false, getClass().getClassLoader());
                if (I_CmsTemplateContextProvider.class.isAssignableFrom(providerClass)) {
                    result = (I_CmsTemplateContextProvider)providerClass.newInstance();
                    result.initialize(m_cms, providerConfig);
                    //note: we use the provider name as a key here, which includes configuration parameters
                    m_providerInstances.put(providerName, result);
                }
            } catch (Throwable t) {
                LOG.error(t.getLocalizedMessage(), t);
            }
        }
        return result;
    }

    /**
     * Utility method which either reads a property from the template used for a specific resource, or from the template context provider used for the resource if available.<p>
     *
     * @param cms the CMS context to use
     * @param res the resource from whose template or template context provider the property should be read
     * @param propertyName the property name
     * @param fallbackValue the fallback value
     *
     * @return the property value
     */
    public String readPropertyFromTemplate(CmsObject cms, CmsResource res, String propertyName, String fallbackValue) {

        try {
            CmsProperty templateProp = cms.readPropertyObject(res, CmsPropertyDefinition.PROPERTY_TEMPLATE, true);
            String templatePath = templateProp.getValue().trim();
            if (hasPropertyPrefix(templatePath)) {
                I_CmsTemplateContextProvider provider = getTemplateContextProvider(templatePath);
                return provider.readCommonProperty(cms, propertyName, fallbackValue);
            } else {
                return cms.readPropertyObject(templatePath, propertyName, false).getValue(fallbackValue);
            }
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return fallbackValue;
        }
    }

    /**
     * Helper method to check whether a given type should not be shown in a context.<p>
     *
     * @param contextKey the key of the template context
     * @param typeName the type name
     *
     * @return true if the context does not prohibit showing the type
     */
    public boolean shouldShowType(String contextKey, String typeName) {

        Map<String, CmsDefaultSet<String>> allowedContextMap = safeGetAllowedContextMap();
        CmsDefaultSet<String> allowedContexts = allowedContextMap.get(typeName);
        if (allowedContexts == null) {
            return true;
        }
        return allowedContexts.contains(contextKey);
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

        Map<String, CmsDefaultSet<String>> result = m_cachedContextMap;
        if (result != null) {
            return result;
        }
        try {
            return OpenCms.getResourceManager().getAllowedContextMap(m_cms);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * Updates the cached context map.
     */
    void updateContextMap() {

        try {
            LOG.debug("Updating cached 'allowed template contexts' map.");
            m_cachedContextMap = OpenCms.getResourceManager().getAllowedContextMap(m_cms);
            LOG.debug("Finished updating cached 'allowed template contexts' map.");
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }
}
