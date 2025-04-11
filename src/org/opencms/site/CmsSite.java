/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.site;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsSingleTreeLocaleHandler;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsPath;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Describes a configured site in OpenCms.<p>
 *
 * @since 6.0.0
 */
public final class CmsSite implements Cloneable, Comparable<CmsSite>, Serializable {

    /**
     * The site localization modes.<p>
     */
    public enum LocalizationMode {
        /** The multi tree localization mode. */
        multiTree,

        /** The single tree localization mode. */
        singleTree,

        /** The default mode. */
        standard
    }

    /** The localization mode parameter name. */
    public static final String PARAM_LOCALIZATION_MODE = "localizationMode";

    /** Parameter name for the main locale. */
    public static final String PARAM_MAIN_LOCALE = "locale.main";

    /** Parameter name for the secondary locales. */
    public static final String PARAM_SECONDARY_LOCALES = "locale.secondary";

    /** Log instance. */
    private static final Log LOG = CmsLog.getLog(CmsSite.class);

    /** The serial version id. */
    private static final long serialVersionUID = 815429861399510277L;
    /** The aliases for this site, a vector of CmsSiteMatcher Objects. */
    private List<CmsSiteMatcher> m_aliases = new ArrayList<CmsSiteMatcher>();

    /** Alternative site root mapping. */
    private CmsAlternativeSiteRootMapping m_alternativeSiteRootMapping;

    /** The URI to use as error page for this site. */
    private String m_errorPage;

    /** If exclusive, and set to true will generate a 404 error, if set to false will redirect to secure url. */
    private boolean m_exclusiveError;

    /** If set to true, secure resources will only be available using the configured secure url. */
    private boolean m_exclusiveUrl;

    /** True if site is generated. */
    private boolean m_generated;

    /**Indicates is this site was published once to the online repo. */
    private boolean m_isOnlineUpdated;

    /** The localization mode. */
    private LocalizationMode m_localizationMode;

    /** The site parameters. */
    private SortedMap<String, String> m_parameters;

    /** This value defines a relative sorting order. */
    private float m_position;

    /** The Url of the secure server. */
    private CmsSiteMatcher m_secureServer;

    /** The site matcher that describes the site. */
    private CmsSiteMatcher m_siteMatcher;

    /** Root directory of this site in the OpenCms VFS. */
    private String m_siteRoot;

    /** UUID of this site's root directory in the OpenCms VFS. */
    private CmsUUID m_siteRootUUID;

    /** The SSL Mode of the site. */
    private CmsSSLMode m_sslMode = CmsSSLMode.SECURE_SERVER;

    /** True if subsite selection is enabled for this site. */
    private boolean m_subsiteSelectionEnabled;

    /** Display title of this site. */
    private String m_title;

    /** True if permanent redirects should be used when redirecting to the secure URL of this site. */
    private boolean m_usesPermanentRedirects;

    /** Indicates whether this site should be considered when writing the web server configuration. */
    private boolean m_webserver = true;

    /** Cached list of configured translation locales, the first locale being the main translation locale. */
    private transient List<Locale> m_translationLocales;

    /**
     * Constructs a new site object without title and id information,
     * this is to be used for lookup purposes only.<p>
     *
     * @param siteRoot root directory of this site in the OpenCms VFS
     * @param siteMatcher the site matcher for this site
     */
    public CmsSite(String siteRoot, CmsSiteMatcher siteMatcher) {

        this(siteRoot, CmsUUID.getNullUUID(), siteRoot, siteMatcher, "");
    }

    /**
     * Constructs a new site object with a default (wildcard) a site matcher,
     * this is to be used for display purposes only.<p>
     *
     * @param siteRoot root directory of this site in the OpenCms VFS
     * @param siteRootUUID UUID of this site's root directory in the OpenCms VFS
     * @param title display name of this site
     */
    public CmsSite(String siteRoot, CmsUUID siteRootUUID, String title) {

        this(siteRoot, siteRootUUID, title, CmsSiteMatcher.DEFAULT_MATCHER, "");
    }

    /**
     * Constructs a new site object.<p>
     *
     * @param siteRoot root directory of this site in the OpenCms VFS
     * @param siteRootUUID UUID of this site's root directory in the OpenCms VFS
     * @param title display name of this site
     * @param siteMatcher the site matcher for this site
     * @param position the sorting position
     */
    public CmsSite(String siteRoot, CmsUUID siteRootUUID, String title, CmsSiteMatcher siteMatcher, String position) {

        setSiteRoot(siteRoot);
        setSiteRootUUID(siteRootUUID);
        setTitle(title);
        setSiteMatcher(siteMatcher);
        // init the position value
        m_position = Float.MAX_VALUE;
        try {
            if (position != null) {
                m_position = Float.parseFloat(position);
            }
        } catch (Throwable e) {
            // m_position will have Float.MAX_VALUE, so this site will appear last
        }
    }

    /**
     * Constructs a new site object.<p>
     *
     * @param siteRoot root directory of this site in the OpenCms VFS
     * @param siteRootUUID UUID of this site's root directory in the OpenCms VFS
     * @param title display name of this site
     * @param siteMatcher the site matcher for this site
     * @param position the sorting position
     * @param errorPage the optional error page for this site
     * @param secureSite the secure site
     * @param exclusiveUrl the exclusive flag
     * @param exclusiveError the exclusive error flag
     * @param webserver indicates whether to write the web server configuration for this site or not
     * @param aliases the aliases
     * @param subsiteSelection enable/disable subsite selection for this site
     */
    public CmsSite(
        String siteRoot,
        CmsUUID siteRootUUID,
        String title,
        CmsSiteMatcher siteMatcher,
        String position,
        String errorPage,
        CmsSiteMatcher secureSite,
        boolean exclusiveUrl,
        boolean exclusiveError,
        boolean webserver,
        List<CmsSiteMatcher> aliases,
        boolean subsiteSelection) {

        setSiteRoot(siteRoot);
        setSiteRootUUID(siteRootUUID);
        setTitle(title);
        setSiteMatcher(siteMatcher);
        // init the position value
        m_position = Float.MAX_VALUE;
        try {
            m_position = Float.parseFloat(position);
        } catch (Throwable e) {
            // m_position will have Float.MAX_VALUE, so this site will appear last
        }
        setErrorPage(errorPage);
        setSecureServer(secureSite);
        setExclusiveUrl(exclusiveUrl);
        setExclusiveError(exclusiveError);
        setWebserver(webserver);
        setAliases(aliases);
        m_subsiteSelectionEnabled = subsiteSelection;
    }

    /**
     * Constructs a new site object without title and id information,
     * with a site matcher generated from the provided URL.<p>
     *
     * This is to be used for test purposes only.<p>
     *
     * @param siteRoot root directory of this site in the OpenCms VFS
     * @param siteURL the URL to create the site matcher for this site from
     */
    public CmsSite(String siteRoot, String siteURL) {

        this(siteRoot, new CmsSiteMatcher(siteURL));
    }

    /**
     * Returns a clone of this Objects instance.<p>
     *
     * @return a clone of this instance
     */
    @Override
    public CmsSite clone() {

        CmsSite res = new CmsSite(
            getSiteRoot(),
            (CmsUUID)getSiteRootUUID().clone(),
            getTitle(),
            (CmsSiteMatcher)getSiteMatcher().clone(),
            String.valueOf(getPosition()),
            getErrorPage(),
            getSecureServer(),
            isExclusiveUrl(),
            isExclusiveError(),
            isWebserver(),
            getAliases(),
            m_subsiteSelectionEnabled);
        res.setParameters(getParameters());
        res.setSSLMode(getSSLMode());
        res.setGenerated(isGenerated());
        res.setAlternativeSiteRootMapping(getAlternativeSiteRootMapping());
        return res;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(CmsSite that) {

        if (that == this) {
            return 0;
        }
        float thatPos = that.getPosition();
        // please note: can't just subtract and cast to int here because of float precision loss
        if (m_position == thatPos) {
            if (m_position == Float.MAX_VALUE) {
                // if they both do not have any position, sort by title
                return m_title.compareTo((that).getTitle());
            }
            return 0;
        }
        return (m_position < thatPos) ? -1 : 1;
    }

    /**
     * Creates a generated site for the extension folder configured on this site, or null if no extension folder is set.
     *
     * @return the generated site for the extension folder
     */
    public CmsSite createAlternativeSiteRootSite() {

        Optional<CmsAlternativeSiteRootMapping> optMapping = getAlternativeSiteRootMapping();
        if (optMapping.isPresent()) {
            CmsSite result = clone();
            CmsAlternativeSiteRootMapping mapping = optMapping.get();
            result.setSiteRoot(mapping.getSiteRoot().asString());
            result.setTitle(getTitle() + " [" + mapping.getTitleSuffix() + "]");
            result.setGenerated(true);
            return result;
        }
        return null;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsSite) {
            return (m_siteMatcher != null) && m_siteMatcher.equals(((CmsSite)obj).m_siteMatcher);
        }
        return false;
    }

    /**
     * Returns the aliases for this site.<p>
     *
     * @return a ArrayList with the aliases
     */
    public List<CmsSiteMatcher> getAliases() {

        return m_aliases;
    }

    /**
     * Gets all site matchers which should be used for matching the site.<p>
     *
     * @return all site matchers to be used for matching the site
     */
    public List<CmsSiteMatcher> getAllMatchers() {

        List<CmsSiteMatcher> result = Lists.newArrayList();
        switch (getSSLMode()) {
            case LETS_ENCRYPT:
            case MANUAL_EP_TERMINATION:
                List<CmsSiteMatcher> baseMatchers = Lists.newArrayList();
                baseMatchers.add(m_siteMatcher);
                for (CmsSiteMatcher alias : m_aliases) {
                    baseMatchers.add(alias);
                }
                // For each matcher, compute both a https and http variant.
                // Store them in a map, so we don't get duplicates if the variants have both
                // been manually defined
                Map<String, CmsSiteMatcher> matchersByUrl = Maps.newHashMap();
                for (CmsSiteMatcher matcher : baseMatchers) {
                    CmsSiteMatcher httpMatcher = matcher.forDifferentScheme("http");
                    CmsSiteMatcher httpsMatcher = matcher.forDifferentScheme("https");
                    for (CmsSiteMatcher current : new CmsSiteMatcher[] {httpMatcher, httpsMatcher}) {
                        matchersByUrl.put(current.getUrl(), current);
                    }
                }
                return Lists.newArrayList(matchersByUrl.values());

            case NO:
            case SECURE_SERVER:
            case MANUAL:
            default:
                result = Lists.newArrayList();
                result.add(m_siteMatcher);
                for (CmsSiteMatcher alias : m_aliases) {
                    result.add(alias);
                }
                if (m_secureServer != null) {
                    result.add(m_secureServer);
                }
                return result;

        }

    }

    /**
     * Gets the (optional) alternative site root mapping.
     *
     * @return the alternative site root mapping
     */
    public Optional<CmsAlternativeSiteRootMapping> getAlternativeSiteRootMapping() {

        return m_alternativeSiteRootMapping != null ? Optional.of(m_alternativeSiteRootMapping) : Optional.empty();
    }

    /**
     * Returns the errorPage.<p>
     *
     * @return the errorPage
     */
    public String getErrorPage() {

        return m_errorPage;
    }

    /**
     * Returns the localization mode.<p>
     *
     * @return the localization mode
     */
    public LocalizationMode getLocalizationMode() {

        if (m_localizationMode == null) {
            try {
                m_localizationMode = LocalizationMode.valueOf(m_parameters.get(PARAM_LOCALIZATION_MODE));
            } catch (Exception e) {
                m_localizationMode = LocalizationMode.standard;
            }
        }
        return m_localizationMode;
    }

    /**
     * Gets the main translation locale for this site.<p>
     *
     * @param defaultValue the value to return as a default when no main translation locale is set
     * @return the main translation locale
     */
    public Locale getMainTranslationLocale(Locale defaultValue) {

        Map<String, String> params = getParameters();
        String value = params.get(PARAM_MAIN_LOCALE);
        if (!CmsStringUtil.isEmpty(value)) {
            return CmsLocaleManager.getLocale(value);
        } else {
            return defaultValue;
        }
    }

    /**
     * Returns the parameters.<p>
     *
     * @return the parameters
     */
    public SortedMap<String, String> getParameters() {

        return m_parameters;
    }

    /**
     * Returns the sorting position.<p>
     *
     * @return the sorting position
     */
    public float getPosition() {

        return m_position;
    }

    /**
     * Gets the list of secondary translation locales.<p>
     *
     * @return the list of secondary translation locales
     */
    public List<Locale> getSecondaryTranslationLocales() {

        List<Locale> result = Lists.newArrayList();
        Map<String, String> params = getParameters();
        String value = params.get(PARAM_SECONDARY_LOCALES);
        if (!CmsStringUtil.isEmpty(value)) {
            String[] tokens = value.trim().split(" *, *");
            for (String token : tokens) {
                Locale locale = CmsLocaleManager.getLocale(token);
                if (!result.contains(locale)) {
                    result.add(locale);
                }
            }
        }
        return result;
    }

    /**
     * Returns the secureServer.<p>
     *
     * @return the secureServer
     */
    public CmsSiteMatcher getSecureServer() {

        return m_secureServer;
    }

    /**
     * Returns the secure server url of this site root.<p>
     *
     * @return the secure server url
     */
    public String getSecureUrl() {

        if (m_secureServer != null) {
            return m_secureServer.getUrl();
        } else {
            LOG.warn(Messages.get().getBundle().key(Messages.ERR_SECURESERVER_MISSING_1, toString()));
            return getUrl();
        }
    }

    /**
     * Returns the server prefix for the given resource in this site, used to distinguish between
     * secure (https) and non-secure (http) sites.<p>
     *
     * This is required since a resource may have an individual "secure" setting using the property
     * {@link org.opencms.file.CmsPropertyDefinition#PROPERTY_SECURE}, which means this resource
     * must be delivered only using a secure protocol.<p>
     *
     * The result will look like <code>http://site.enterprise.com:8080/</code> or <code>https://site.enterprise.com/</code>.<p>
     *
     * @param cms the current users OpenCms context
     * @param resource the resource to use
     *
     * @return the server prefix for the given resource in this site
     *
     * @see #getServerPrefix(CmsObject, String)
     */
    public String getServerPrefix(CmsObject cms, CmsResource resource) {

        return getServerPrefix(cms, resource.getRootPath());
    }

    /**
     * Returns the server prefix for the given resource in this site, used to distinguish between
     * secure (https) and non-secure (http) sites.<p>
     *
     * This is required since a resource may have an individual "secure" setting using the property
     * {@link org.opencms.file.CmsPropertyDefinition#PROPERTY_SECURE}, which means this resource
     * must be delivered only using a secure protocol.<p>
     *
     * The result will look like <code>http://site.enterprise.com:8080/</code> or <code>https://site.enterprise.com/</code>.<p>
     *
     * @param cms the current users OpenCms context
     * @param resourceName the resource name
     *
     * @return the server prefix for the given resource in this site
     *
     * @see #getSecureUrl()
     * @see #getUrl()
     */
    public String getServerPrefix(CmsObject cms, String resourceName) {

        if (resourceName.startsWith(cms.getRequestContext().getSiteRoot())) {
            // make sure this can also be used with a resource root path
            resourceName = resourceName.substring(cms.getRequestContext().getSiteRoot().length());
        }
        boolean secure = OpenCms.getStaticExportManager().isSecureLink(
            cms,
            resourceName,
            cms.getRequestContext().isSecureRequest());

        return (secure ? getSecureUrl() : getUrl());
    }

    /**
     * Returns the site matcher that describes the URL of this site.<p>
     *
     * @return the site matcher that describes the URL of this site
     */
    public CmsSiteMatcher getSiteMatcher() {

        return m_siteMatcher;
    }

    /**
     * Returns the site path for the given root path in case the root path
     * actually starts with this site root, or <code>null</code> in case
     * the root path does not.<p>
     *
     * @param rootPath the root path to get the site path for
     *
     * @return the site path for the given root path in case the root path
     *      actually starts with this site root, or <code>null</code> in case
     *      the root path does not
     */
    public String getSitePath(String rootPath) {

        String result = null;
        if (CmsStringUtil.isNotEmpty(rootPath)) {
            if (rootPath.startsWith(m_siteRoot)) {
                result = rootPath.substring(m_siteRoot.length());
            }
        }
        return result;
    }

    /**
     * Returns the path of this site's root directory in the OpenCms VFS without tailing slash.<p>
     * <ul><li><code>e.g. /sites/default</code></li></ul>
     *
     * @return the path of this site's root directory in the OpenCms VFS without tailing slash
     */
    public String getSiteRoot() {

        return m_siteRoot;
    }

    /**
     * Returns the UUID of this site's root directory in the OpenCms VFS.<p>
     *
     * @return the UUID of this site's root directory in the OpenCms VFS
     */
    public CmsUUID getSiteRootUUID() {

        return m_siteRootUUID;
    }

    /**
     * Gets the SSLMode of the site.<p>
     *
     * @return CmsSSLMode
     */
    public CmsSSLMode getSSLMode() {

        return m_sslMode;
    }

    /**
     * Returns the display title of this site.<p>
     *
     * @return the display title of this site
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * Returns all configured translation locales for this site.<p>
     *
     * The first entry in the list will be the main translation locale.
     * If the main translation locale is not correctly configured, an empty list is returned.<p>
     *
     * @return all configured translation locales for this site
     */
    public List<Locale> getTranslationLocales() {

        if (m_translationLocales == null) {
            m_translationLocales = new ArrayList<Locale>();
            Locale main = getMainTranslationLocale(null);
            if (main != null) {
                // locales are configured correctly for translation
                m_translationLocales.add(main);
                m_translationLocales.addAll(getSecondaryTranslationLocales());
            }
        }
        return m_translationLocales;
    }

    /**
     * Returns the server url of this site root.<p>
     *
     * @return the server url
     */
    public String getUrl() {

        if (m_siteMatcher == null) {
            return null;
        }
        return m_siteMatcher.getUrl();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_siteRootUUID.hashCode();
    }

    /**
     * Returns true, if the site has a secure server.<p>
     *
     * @return true, if the site has a secure server
     */
    public boolean hasSecureServer() {

        return m_secureServer != null;
    }

    /**
     * Returns the exclusive error flag.<p>
     *
     * @return <code>true</code> will generate a 404 error,
     *      or <code>false</code> will redirect to secure url.
     */
    public boolean isExclusiveError() {

        return m_exclusiveError;
    }

    /**
     * Returns the exclusive protocol flag.<p>
     *
     * @return <code>true</code> secure resources will only be available using the configured secure url,
     *      or <code>false</code> if the uri (protocol + servername) does not really matter.
     */
    public boolean isExclusiveUrl() {

        return m_exclusiveUrl;
    }

    /**
     * Returns true if this is an automatically generated site (and thus should not be used in the site management UI).
     *
     * @return true if this is a generated site
     */
    public boolean isGenerated() {

        return m_generated;
    }

    /**
     * Checks if the site it in the online project.<p>
     * @return true if it is in online project
     */
    public boolean isOnlineUpdated() {

        return m_isOnlineUpdated;
    }

    /**
     * Returns a flag indicating if the site is the shared site.
     *
     * @return <code>true</code> if the site is the shared site,
     *      or <code>false</code> if it is not the shared site.
     */
    public boolean isSharedSite() {

        return OpenCms.getSiteManager().getSharedFolder().equals(m_siteRoot + "/");
    }

    /**
     * Returns true if subsite selection is enabled for this site.
     *
     * @return true if subsite selection is enabled for this site
     */
    public boolean isSubsiteSelectionEnabled() {

        return m_subsiteSelectionEnabled;
    }

    /**
     * Returns the web server.<p>
     *
     * @return the web server
     */
    public boolean isWebserver() {

        return m_webserver;
    }

    /**
     * Returns true if there is an alternative site root mapping configured for this site, and it matches the given path.
     *
     * @param path the path from the request
     * @return true if the alternative site root mapping matches the path
     */
    public boolean matchAlternativeSiteRoot(String path) {

        Optional<CmsAlternativeSiteRootMapping> optMapping = getAlternativeSiteRootMapping();
        if (optMapping.isPresent()) {
            CmsAlternativeSiteRootMapping extConfig = optMapping.get();
            if (getLocalizationMode() == LocalizationMode.singleTree) {
                Locale locale = CmsSingleTreeLocaleHandler.getLocaleFromPath(path);
                if (locale != null) {
                    path = path.substring(locale.toString().length() + 1);
                }
            }
            for (CmsPath prefix : extConfig.getPrefixes()) {
                if (prefix.isPrefixOfStr(path)) {
                    return true;
                }
            }
        }
        return false;

    }

    /**
     * Sets the alternative site root mapping.
     *
     * @param alternativeSiteRootMapping the alternative site root mapping
     */
    public void setAlternativeSiteRootMapping(Optional<CmsAlternativeSiteRootMapping> alternativeSiteRootMapping) {

        m_alternativeSiteRootMapping = alternativeSiteRootMapping.isPresent() ? alternativeSiteRootMapping.get() : null;
    }

    /**
     * Sets the errorPage.<p>
     *
     * @param errorPage the errorPage to set
     */
    public void setErrorPage(String errorPage) {

        m_errorPage = errorPage;
    }

    /**
     * Sets the exclusive error flag.<p>
     *
     * @param error the exclusive error flag
     */
    public void setExclusiveError(boolean error) {

        m_exclusiveError = error;
    }

    /**
     * Sets the exclusive protocol flag.<p>
     *
     * @param exclusive the exclusive protocol flag
     */
    public void setExclusiveUrl(boolean exclusive) {

        m_exclusiveUrl = exclusive;
    }

    /**
     * Sets the online status.
     *
     * @param isOnline true -> site has online version
     */
    public void sethasOnlineUpdated(boolean isOnline) {

        m_isOnlineUpdated = isOnline;

    }

    /**
     * Sets the parameters.<p>
     *
     * @param parameters the parameters to set
     */
    public void setParameters(SortedMap<String, String> parameters) {

        m_parameters = new TreeMap<String, String>(parameters);
    }

    /**
     * Sets the server URL prefix to which this site is mapped.<p>
     *
     * @param siteRoot the server URL prefix to which this site is mapped
     */
    public void setSiteRoot(String siteRoot) {

        // site roots must never end with a "/"
        siteRoot = siteRoot.trim();
        if (siteRoot.endsWith("/")) {
            m_siteRoot = siteRoot.substring(0, siteRoot.length() - 1).trim();

        } else {
            m_siteRoot = siteRoot.trim();
        }
    }

    /**
     * Sets the SSLMode of the site.<p>
     *
     * @param mode the CmsSSLMode
     */
    public void setSSLMode(CmsSSLMode mode) {

        m_sslMode = mode;
    }

    /**
     * Enables / disables subsite selection for this site.
     *
     * @param enabled true if subsite selection should be enabled
     */
    public void setSubsiteSelectionEnabled(boolean enabled) {

        m_subsiteSelectionEnabled = enabled;

    }

    /**
     * Enables use of permanent redirects instead of temporary redirects to the secure site.<p>
     *
     * @param usePermanentRedirects true if permanent redirects should be used
     */
    public void setUsePermanentRedirects(boolean usePermanentRedirects) {

        m_usesPermanentRedirects = usePermanentRedirects;
    }

    /**
     * Sets the web server.<p>
     *
     * @param webserver the web server to set
     */
    public void setWebserver(boolean webserver) {

        m_webserver = webserver;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuffer result = new StringBuffer(128);
        result.append("server: ");
        result.append(m_siteMatcher != null ? m_siteMatcher.toString() : "null");
        // some extra effort to make debugging easier
        if (m_siteRoot != null) {
            result.append(" siteRoot: ");
            result.append(m_siteRoot);
        } else {
            result.append(" (no siteRoot)");
        }
        if (m_title != null) {
            result.append(" title: ");
            result.append(m_title);
        } else {
            result.append(" (no title)");
        }
        return result.toString();
    }

    /**
     * Returns true if permanent redirects should be used for redirecting to the secure URL for this site.<p>
     *
     * @return true if permanent redirects should be used
     */
    public boolean usesPermanentRedirects() {

        return m_usesPermanentRedirects;
    }

    /**
     * Adds an alias for the site.<p>
     *
     * @param aliasServer the sitematcher for the alias
     */
    protected void addAlias(CmsSiteMatcher aliasServer) {

        m_aliases.add(aliasServer);
    }

    /**
     * Returns the site matcher for the secure site, or null if no secure site is defined.<p>
     *
     * @return the site matcher for the secure site
     */
    protected CmsSiteMatcher getSecureServerMatcher() {

        return m_secureServer;
    }

    /**
     * Sets the aliases for the site.<p>
     *
     * @param aliases the aliases for the site
     */
    protected void setAliases(List<CmsSiteMatcher> aliases) {

        m_aliases = aliases;
    }

    /**
     * Sets the display title of this site.<p>
     *
     * @param position the display title of this site
     */
    protected void setPosition(float position) {

        m_position = position;
    }

    /**
     * Sets the secure server.<p>
     *
     * @param secureServer the sitematcher of the secure server
     */
    protected void setSecureServer(CmsSiteMatcher secureServer) {

        m_secureServer = secureServer;
    }

    /**
     * Sets the site matcher that describes the URL of this site.<p>
     *
     * @param siteMatcher the site matcher that describes the URL of this site
     */
    protected void setSiteMatcher(CmsSiteMatcher siteMatcher) {

        m_siteMatcher = siteMatcher;
    }

    /**
     * Sets the UUID of this site's root directory in the OpenCms VFS.<p>
     *
     * @param siteRootUUID the UUID of this site's root directory in the OpenCms VFS
     */
    protected void setSiteRootUUID(CmsUUID siteRootUUID) {

        m_siteRootUUID = siteRootUUID;
    }

    /**
     * Sets the display title of this site.<p>
     *
     * @param name the display title of this site
     */
    protected void setTitle(String name) {

        m_title = name;
    }

    /**
     * Enables/disableds the 'generated' state, i.e. if this site is automatically generated.
     *
     * @param generated true to mark the site as generated
     */
    void setGenerated(boolean generated) {

        m_generated = generated;
    }

}