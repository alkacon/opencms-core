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

package org.opencms.loader;

import org.opencms.cache.CmsVfsMemoryObjectCache;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.json.JSONTokener;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsFunctionFormatterBean;
import org.opencms.xml.containerpage.I_CmsFormatterBean;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * Template context provider that can be used to migrate from one template to another.
 *
 * <p>Note: The template provider by itself does not transform anything, the feature is just named like that.
 */
public class CmsTransformerTemplateProvider implements I_CmsTemplateContextProvider {

    /**
     * Contains the configuration data for the provider, usually read from a configuration file.
     */
    public class Configuration {

        /** The map of template contexts. */
        private Map<String, CmsTemplateContext> m_contextMap = new HashMap<>();

        /** The path filter regexes for restricting functions in the gallery search results.*/
        private Map<String, Pattern> m_functionFilters = new HashMap<>();

        /** The context menu label. */
        private CmsJsonMessageContainer m_menuLabel;

        /** Map from template key to template compatibility string. */
        private Map<String, String> m_templateCompatibility = new HashMap<>();

        /**
         * Creates a new instance.
         */
        public Configuration() {}

        /**
         * Creates a new instance.
         *
         * @param configJson the configuration JSON object
         * @throws JSONException if something goes wrong with the JSON
         */
        public Configuration(JSONObject configJson)
        throws JSONException {

            Map<String, CmsTemplateContext> contextMap = new LinkedHashMap<>();
            JSONObject source = configJson.getJSONObject(JsonKeys.sourceTemplate.name());
            JSONObject target = configJson.getJSONObject(JsonKeys.targetTemplate.name());
            CmsTemplateContext sourceContext = parseTemplateContext(TEMPLATE_KEY_SOURCE, source);
            CmsTemplateContext targetContext = parseTemplateContext(TEMPLATE_KEY_TARGET, target);
            contextMap.put(TEMPLATE_KEY_SOURCE, sourceContext);
            contextMap.put(TEMPLATE_KEY_TARGET, targetContext);
            m_contextMap = Collections.unmodifiableMap(contextMap);
            Object menuLabel = configJson.opt(JsonKeys.menuLabel.name());
            if (menuLabel != null) {
                m_menuLabel = new CmsJsonMessageContainer(menuLabel);
            }

        }

        /**
         * Gets the map of template contexts, with their internal names as keys.
         *
         * @return the map of template contexts
         */
        public Map<String, CmsTemplateContext> getContextMap() {

            return m_contextMap;
        }

        /**
         * Gets the function filter pattern used to filter dynamic function paths.
         *
         * <p>If this returns null, dynamic functions shouldn't be filtered.
         *
         * @param key the template context key
         * @return the dynamic function filter
         */
        public Pattern getFunctionFilter(String key) {

            return m_functionFilters.get(key);
        }

        /**
         * Gets the label for the context menu
         *
         * @return the label for the context menu
         */
        public CmsJsonMessageContainer getMenuLabel() {

            return m_menuLabel;
        }

        /**
         * Gets the template compatibility for the given template context.
         *
         * @param currentContext the current template context
         * @return the template compatibility for the template context
         */
        public String getTemplateCompatibility(String currentContext) {

            return m_templateCompatibility.get(currentContext);
        }

        /**
         * Helper method to read a template context from a JSON value.
         *
         * @param key the name of the template context
         * @param object the JSON value to construct the template context from
         * @return the constructed template context
         *
         * @throws JSONException if something goes wrong with the JSON
         */
        private CmsTemplateContext parseTemplateContext(String key, JSONObject object) throws JSONException {

            Object niceNameValue = object.opt(JsonKeys.niceName.name());
            CmsTemplateContext context = new CmsTemplateContext(
                key,
                object.getString(JsonKeys.path.name()),
                niceNameValue != null ? new CmsJsonMessageContainer(niceNameValue) : null,
                CmsTransformerTemplateProvider.this,
                Collections.emptyList(),
                false);
            String functionFilter = object.optString(JsonKeys.functionFilter.name(), null);
            if (functionFilter != null) {
                m_functionFilters.put(key, Pattern.compile(functionFilter));
            }
            String templateCompatibility = object.optString(JsonKeys.compatibility.name(), null);
            if (templateCompatibility != null) {
                m_templateCompatibility.put(key, templateCompatibility);
            }

            return context;
        }
    }

    /** Enum representing the keys in the configuration JSON file. */
    enum JsonKeys {
        /** Key for the template compatibility. */
        compatibility,

        /** Key for the regex used for filtering dynamic function paths. */
        functionFilter,

        /** Key for the context menu label. */
        menuLabel,

        /** Key for the nice name of a template. */
        niceName,

        /** Key for the template path. */
        path,

        /** Key for the source template. */
        sourceTemplate,

        /** Key for the target template. */
        targetTemplate;
    }

    /** Version string used for cookie name calculation. */
    private static final String VERSION = "2";

    /** The cookie prefix. */
    public static final String COOKIE_PREFIX = "templatetransformer_override_";

    /** Parameter for the configuration file in the template provider string. */
    public static final String PARAM_CONFIG = "config";

    /** The template context key for the source template. */
    public static final String TEMPLATE_KEY_SOURCE = "source";

    /** The template context key for the target template. */
    public static final String TEMPLATE_KEY_TARGET = "target";

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsTransformerTemplateProvider.class);

    /** Instantiates the configuration cache when accessed. */
    private static Supplier<CmsVfsMemoryObjectCache> m_configCacheProvider = Suppliers.memoize(
        () -> new CmsVfsMemoryObjectCache());

    /** The CmsObject this provider was initialized with (in the Online project). */
    private CmsObject m_cms;

    /** The path for the configuration file. */
    private String m_configPath;

    /** Cookie name for the template context override cookie. */
    private String m_cookieName;

    /**
     * @see org.opencms.loader.I_CmsTemplateContextProvider#getAllContexts()
     */
    public Map<String, CmsTemplateContext> getAllContexts() {

        return getConfiguration().getContextMap();
    }

    /**
     * Gets the configuration data that was read from the config file.
     *
     * @return the configuration data from the config file
     */
    public Configuration getConfiguration() {

        Configuration config = (Configuration)(m_configCacheProvider.get().getCachedObject(m_cms, m_configPath));
        if (config != null) {
            return config;
        } else {
            try {
                config = loadConfiguration();
                m_configCacheProvider.get().putCachedObject(m_cms, m_configPath, config);
                return config;
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
                return new Configuration();
            }
        }
    }

    /**
     * @see org.opencms.loader.I_CmsTemplateContextProvider#getDefaultLabel(java.util.Locale)
     */
    public String getDefaultLabel(Locale locale) {

        Configuration config = getConfiguration();
        return config.getContextMap().get(TEMPLATE_KEY_SOURCE).getLocalizedName(locale);
    }

    /**
     * @see org.opencms.loader.I_CmsTemplateContextProvider#getEditorStyleSheet(org.opencms.file.CmsObject, java.lang.String)
     */
    public String getEditorStyleSheet(CmsObject cms, String editedResourcePath) {

        // we assume here that the WYSIWYG editor stylesheet is configured via sitemap configuration.
        return null;
    }

    /**
     * @see org.opencms.loader.I_CmsTemplateContextProvider#getFunctionsForGallery(org.opencms.file.CmsObject, java.lang.String)
     */
    public Set<CmsUUID> getFunctionsForGallery(CmsObject cms, String templateContext) {

        Configuration config = getConfiguration();
        Pattern functionFilter = config.getFunctionFilter(templateContext);
        if (functionFilter == null) {
            // everything allowed
            return null;
        }
        Set<CmsUUID> result = new HashSet<>();
        for (I_CmsFormatterBean formatter : OpenCms.getADEManager().getCachedFormatters(
            false).getFormatters().values()) {
            if (!(formatter instanceof CmsFunctionFormatterBean)) {
                continue;
            }
            if (!CmsUUID.isValidUUID(formatter.getId()) || (formatter.getLocation() == null)) {
                continue;
            }
            if (!functionFilter.matcher(formatter.getLocation()).matches()) {
                continue;
            }
            result.add(new CmsUUID(formatter.getId()));
        }
        return Collections.unmodifiableSet(result);
    }

    /**
     * @see org.opencms.loader.I_CmsTemplateContextProvider#getMenuLabel(java.util.Locale)
     */
    public String getMenuLabel(Locale locale) {

        CmsJsonMessageContainer container = getConfiguration().getMenuLabel();
        if (container != null) {
            return container.key(locale);
        } else {
            return null;
        }
    }

    /**
     * @see org.opencms.loader.I_CmsTemplateContextProvider#getMenuPosition()
     */
    public int getMenuPosition() {

        return 1;
    }

    /**
     * @see org.opencms.loader.I_CmsTemplateContextProvider#getOverrideCookieName()
     */
    public String getOverrideCookieName() {

        return m_cookieName;
    }

    /**
     * @see org.opencms.loader.I_CmsTemplateContextProvider#getTemplateCompatibility(java.lang.String)
     */
    public String getTemplateCompatibility(String currentContext) {

        return getConfiguration().getTemplateCompatibility(currentContext);
    }

    /**
     * @see org.opencms.loader.I_CmsTemplateContextProvider#getTemplateContext(org.opencms.file.CmsObject, javax.servlet.http.HttpServletRequest, org.opencms.file.CmsResource)
     */
    public CmsTemplateContext getTemplateContext(CmsObject cms, HttpServletRequest request, CmsResource resource) {

        Configuration config = getConfiguration();
        return config.getContextMap().get(TEMPLATE_KEY_SOURCE);
    }

    /**
     * @see org.opencms.loader.I_CmsTemplateContextProvider#initialize(org.opencms.file.CmsObject, java.lang.String)
     */
    public void initialize(CmsObject cms, String config) {

        m_cms = cms;
        if (config == null) {
            config = "";
        }
        config = config.trim();
        Map<String, String> parsedConfig = CmsStringUtil.splitAsMap(config, ",", "=");
        m_configPath = parsedConfig.get(PARAM_CONFIG);
        if (m_configPath == null) {
            throw new RuntimeException(
                "Missing parameter '" + PARAM_CONFIG + "' for template provider '" + getClass().getName() + "'");
        }
        // Use MD5 of configuration path for cookie name, so users can switch templates independently for differently configured instances of the template provider
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(m_configPath.getBytes(StandardCharsets.UTF_8));
            md5.update((byte)0);
            md5.update(VERSION.getBytes(StandardCharsets.UTF_8));
            byte[] md5bytes = md5.digest();
            m_cookieName = COOKIE_PREFIX + Hex.encodeHexString(md5bytes);
        } catch (NoSuchAlgorithmException e) {
            // shouldn't happen - MD5 must be in standard library
            throw new RuntimeException(e);
        }
    }

    /**
     * @see org.opencms.loader.I_CmsTemplateContextProvider#isHiddenContext(java.lang.String)
     */
    public boolean isHiddenContext(String key) {

        return TEMPLATE_KEY_SOURCE.equals(key);
    }

    /**
     * @see org.opencms.loader.I_CmsTemplateContextProvider#isIgnoreTemplateContextsSetting()
     */
    public boolean isIgnoreTemplateContextsSetting() {
        return true;
    }

    /**
     * @see org.opencms.loader.I_CmsTemplateContextProvider#readCommonProperty(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public String readCommonProperty(CmsObject cms, String propertyName, String fallbackValue) {

        Configuration config = getConfiguration();
        try {
            CmsProperty prop = cms.readPropertyObject(
                config.getContextMap().get(TEMPLATE_KEY_SOURCE).getTemplatePath(),
                propertyName,
                false);
            return prop.getValue();
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * @see org.opencms.loader.I_CmsTemplateContextProvider#shouldShowContextMenuOption(org.opencms.file.CmsObject)
     */
    public boolean shouldShowContextMenuOption(CmsObject cms) {

        return OpenCms.getRoleManager().hasRole(cms, CmsRole.DEVELOPER);
    }

    /**
     * @see org.opencms.loader.I_CmsTemplateContextProvider#shouldShowElementTemplateContextSelection(org.opencms.file.CmsObject)
     */
    public boolean shouldShowElementTemplateContextSelection(CmsObject cms) {

        return false;
    }

    /**
     * Helper method for loading the configuration from the VFS.
     *
     * @return the provider configuration
     * @throws Exception if something goes wrong
     */
    protected Configuration loadConfiguration() throws Exception {

        CmsFile configFile = m_cms.readFile(m_configPath);
        String configStr = new String(configFile.getContents(), StandardCharsets.UTF_8);
        JSONTokener tok = new JSONTokener(configStr);
        tok.setOrdered(true);
        JSONObject configJson = new JSONObject(tok, true);
        return new Configuration(configJson);
    }

}
