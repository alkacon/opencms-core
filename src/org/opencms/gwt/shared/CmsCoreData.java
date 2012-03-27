/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.util.CmsUUID;

import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Runtime data bean for prefetching.<p>
 * 
 * @since 8.0.0
 */
public class CmsCoreData implements IsSerializable {

    /** A enumeration for the ADE context. */
    public enum AdeContext {

        /** Context for container page. */
        containerpage,

        /** Context for sitemap. */
        sitemap
    }

    /** Name of the used js variable. */
    public static final String DICT_NAME = "org_opencms_gwt";

    /** The key for the GWT build id property. */
    public static final String KEY_GWT_BUILDID = "gwt.buildid";

    /** The parameter name for path. */
    public static final String PARAM_PATH = "path";

    /** The parameter name for the return code. */
    public static final String PARAM_RETURNCODE = "returncode";

    /** The time sent from the server when loading the data. */
    protected long m_serverTime;

    /** The XML content editor back-link URL. */
    private String m_contentEditorBacklinkUrl;

    /** The XML content editor delete-link URL. */
    private String m_contentEditorDeleteLinkUrl;

    /** The XML content editor URL. */
    private String m_contentEditorUrl;

    /** The mappings of file extensions to resource types. */
    private Map<String, String> m_extensionMapping;

    /** The current request locale. */
    private String m_locale;

    /** The login JSP URL. */
    private String m_loginURL;

    /** The current navigation URI. */
    private String m_navigationUri;

    /** The current site root. */
    private String m_siteRoot;

    /** The structure id of the resource. */
    private CmsUUID m_structureId;

    /** A flag which indicates whether the toolbar should be shown initially. */
    private boolean m_toolbarVisible;

    /** The current uri. */
    private String m_uri;

    /** The OpenCms VFS prefix. */
    private String m_vfsPrefix;

    /** The current workplace locale. */
    private String m_wpLocale;

    /** The default link to use for opening the workplace. */
    private String m_defaultWorkplaceLink;

    /** The map of GWT build ids. */
    private Map<String, String> m_gwtBuildIds;

    /**
     * Constructor.<p>
     */
    public CmsCoreData() {

        // empty
    }

    /**
     * Clone constructor.<p>
     * 
     * @param clone the instance to clone 
     */
    public CmsCoreData(CmsCoreData clone) {

        this(
            clone.getContentEditorUrl(),
            clone.getContentEditorBacklinkUrl(),
            clone.getContentEditorDeleteLinkUrl(),
            clone.getLoginURL(),
            clone.getVfsPrefix(),
            clone.getSiteRoot(),
            clone.getLocale(),
            clone.getWpLocale(),
            clone.getUri(),
            clone.getNavigationUri(),
            clone.getStructureId(),
            clone.getExtensionMapping(),
            clone.getServerTime(),
            clone.isToolbarVisible(),
            clone.getGwtBuildIds());
    }

    /**
     * Constructor.<p>
     * 
     * @param contentEditorUrl the XML content editor URL
     * @param contentEditorBacklinkUrl the XML content editor back-link URL
     * @param contentEditorDeleteLinkUrl the XML content editor delete-link URL
     * @param loginUrl the login JSP URL
     * @param vfsPrefix the OpenCms VFS prefix
     * @param siteRoot the current site root
     * @param locale the current request locale
     * @param wpLocale the workplace locale
     * @param uri the current uri
     * @param structureId the structure id of tbe resource 
     * @param navigationUri the current navigation URI
     * @param extensionMapping the mappings of file extensions to resource types
     * @param serverTime the current time  
     * @param toolbarVisible a flag to indicate whether the toolbar should be visible initially
     * @param gwtBuildIds the map of GWT build ids 
     */
    public CmsCoreData(
        String contentEditorUrl,
        String contentEditorBacklinkUrl,
        String contentEditorDeleteLinkUrl,
        String loginUrl,
        String vfsPrefix,
        String siteRoot,
        String locale,
        String wpLocale,
        String uri,
        String navigationUri,
        CmsUUID structureId,
        Map<String, String> extensionMapping,
        long serverTime,
        boolean toolbarVisible,
        Map<String, String> gwtBuildIds) {

        m_contentEditorUrl = contentEditorUrl;
        m_contentEditorBacklinkUrl = contentEditorBacklinkUrl;
        m_contentEditorDeleteLinkUrl = contentEditorDeleteLinkUrl;
        m_loginURL = loginUrl;
        m_vfsPrefix = vfsPrefix;
        m_siteRoot = siteRoot;
        m_locale = locale;
        m_wpLocale = wpLocale;
        m_uri = uri;
        m_navigationUri = navigationUri;
        m_extensionMapping = extensionMapping;
        m_serverTime = serverTime;
        m_toolbarVisible = toolbarVisible;
        m_structureId = structureId;
        m_gwtBuildIds = gwtBuildIds;
    }

    /**
     * Returns the XML content editor back-link URL.<p>
     *
     * @return the XML content editor back-link URL
     */
    public String getContentEditorBacklinkUrl() {

        return m_contentEditorBacklinkUrl;
    }

    /**
     * Returns the XML content editor delete-link URL.<p>
     * 
     * @return the XML content editor delete-link URL
     */
    public String getContentEditorDeleteLinkUrl() {

        return m_contentEditorDeleteLinkUrl;
    }

    /**
     * Returns the XML content editor URL.<p>
     *
     * @return the XML content editor URL
     */
    public String getContentEditorUrl() {

        return m_contentEditorUrl;
    }

    /**
     * Returns the extensionMapping.<p>
     *
     * @return the extensionMapping
     */
    public Map<String, String> getExtensionMapping() {

        return m_extensionMapping;
    }

    /**
     * Gets the map of GWT build ids.<p>
     * 
     * @return the map containing the GWT build ids 
     */
    public Map<String, String> getGwtBuildIds() {

        return m_gwtBuildIds;
    }

    /**
     * Returns the current request locale.<p>
     *
     * @return the current request locale
     */
    public String getLocale() {

        return m_locale;
    }

    /**
     * Returns the login URL.<p>
     *
     * @return the login URL
     */
    public String getLoginURL() {

        return m_loginURL;
    }

    /**
     * Returns the current navigation (sitemap) URI.<p>
     *  
     * @return the current navigation URI 
     */
    public String getNavigationUri() {

        return m_navigationUri;
    }

    /**
     * Returns the time of the server when the data was loaded.<p>
     * 
     * @return the time of the server when the data was loaded 
     */
    public long getServerTime() {

        return m_serverTime;
    }

    /**
     * Returns the current site root.<p>
     *
     * @return the current site root
     */
    public String getSiteRoot() {

        return m_siteRoot;
    }

    /**
     * Gets the structure id of the current resource.<p>
     *  
     * @return the structure id of the current resource 
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * Returns the current uri.<p>
     *
     * @return the current uri
     */
    public String getUri() {

        return m_uri;
    }

    /**
     * Returns the OpenCms VFS prefix.<p>
     *
     * @return the OpenCms VFS prefix
     */
    public String getVfsPrefix() {

        return m_vfsPrefix;
    }

    /**
     * Returns the current workplace locale.<p>
     *
     * @return the current workplace locale
     */
    public String getWpLocale() {

        return m_wpLocale;
    }

    /**
     * Returns true if the toolbar should be visible initially.<p>
     * 
     * @return true if the toolbar should be visible initially 
     */
    public boolean isToolbarVisible() {

        return m_toolbarVisible;
    }

}
