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

        /** Context for classic direct edit provider. */
        editprovider,

        /** Context for gallery dialog. */
        gallery,

        /** Context for container page. */
        pageeditor,

        /** Context for publish dialog. */
        publish,

        /** Context for resource info dialog. */
        resourceinfo,

        /** Context for sitemap. */
        sitemapeditor
    }

    /** The available client modules. */
    public enum ModuleKey {

        /** Container page editor. */
        containerpage,

        /** Content editor. */
        contenteditor,

        /** Direct edit provider. */
        editprovider,

        /** Galleries. */
        galleries,

        /** Post upload dialog. */
        postupload,

        /** Properties dialog. */
        properties,

        /** Publish dialog. */
        publish,

        /** Sitemap editor. */
        sitemap,

        /** Upload dialog. */
        upload
    }

    /**
     * Bean class containing info about the current user.<p>
     */
    public static class UserInfo implements IsSerializable {

        /** True if the user is an administrator. */
        private boolean m_isAdmin;

        /** True if the user is a category manager. */
        private boolean m_isCategoryManager;

        /** True if the user is a template developer. */
        private boolean m_isDeveloper;

        /** True if the user is managed. */
        private boolean m_isManaged;

        /** True if the user is a workplace user. */
        private boolean m_isWorkplaceUser;

        /** The user name. */
        private String m_name;

        /** The user icon path. */
        private String m_userIcon;

        /**
         * Creates a new instance.<p>
         *
         * @param name the user name
         * @param userIcon the user icon path
         * @param isAdmin true if the user is an administrator
         * @param isDeveloper true if the user is a template developer
         * @param isCategoryManager true if the user is a category manager
         * @param isWorkplaceUser true if the user is a workplace user
         * @param isManaged true if the user is managed
         */
        public UserInfo(
            String name,
            String userIcon,
            boolean isAdmin,
            boolean isDeveloper,
            boolean isCategoryManager,
            boolean isWorkplaceUser,
            boolean isManaged) {

            m_isDeveloper = isDeveloper;
            m_isCategoryManager = isCategoryManager;
            m_isAdmin = isAdmin;
            m_isManaged = isManaged;
            m_isWorkplaceUser = isWorkplaceUser;
            m_name = name;
            m_userIcon = userIcon;
        }

        /**
         * Default constructor, needed for serialization.<p>
         */
        protected UserInfo() {

            // empty
        }

        /**
         * Gets the user name.<p>
         *
         * @return the user name
         */
        public String getName() {

            return m_name;
        }

        /**
         * Returns the user icon path.<p>
         *
         * @return the user icon path
         */
        public String getUserIcon() {

            return m_userIcon;
        }

        /**
         * Returns true if the user is an administrator.<p>
         *
         * @return true if the user is an administrator
         */
        public boolean isAdmin() {

            return m_isAdmin;
        }

        /**
         * Returns true if the user is a category manager.<p>
         *
         * @return true if the user is a category manager
         */
        public boolean isCategoryManager() {

            return m_isCategoryManager;
        }

        /**
         * Returns true if the user is a template developer.<p>
         *
         * @return true if the user is a template developer
         */
        public boolean isDeveloper() {

            return m_isDeveloper;
        }

        /**
         * Returns if the user is managed.<p>
         *
         * @return <code>true</code> if the user is managed
         */
        public boolean isManaged() {

            return m_isManaged;
        }

        /**
         * Returns true if the current user is a workplace user.<p>
         *
         * @return true if the current user is a workplace user
         */
        public boolean isWorkplaceUser() {

            return m_isWorkplaceUser;
        }
    }

    /** Name of the used js variable. */
    public static final String DICT_NAME = "org_opencms_gwt";

    /** The key for the GWT build id property. */
    public static final String KEY_GWT_BUILDID = "gwt.buildid";

    /** The meta element name to the requested module key. */
    public static final String META_PARAM_MODULE_KEY = "opencms-module";

    /** The parameter name for path. */
    public static final String PARAM_PATH = "path";

    /** The parameter name for the return code. */
    public static final String PARAM_RETURNCODE = "returncode";

    /** The time sent from the server when loading the data. */
    protected long m_serverTime;

    /** A bean with information about the current user. */
    protected UserInfo m_userInfo;

    /** The link to the page displayed in the "about" dialog. */
    private String m_aboutLink;

    /** ADE parameters. */
    private Map<String, String> m_adeParameters;

    /** The category base folder. */
    private String m_categoryBaseFolder;

    /** The XML content editor back-link URL. */
    private String m_contentEditorBacklinkUrl;

    /** The XML content editor URL. */
    private String m_contentEditorUrl;

    /** The default link to use for opening the workplace. */
    private String m_defaultWorkplaceLink;

    /** The embedded dialogs URL. */
    private String m_embeddedDialogsUrl;

    /** The mappings of file extensions to resource types. */
    private Map<String, String> m_extensionMapping;

    /** The file explorer link. */
    private String m_fileExplorerLink;

    /** True if deactivated types should be hidden in the gallery dialog. */
    private boolean m_hideDisabledGalleryTypes;

    /** The resource icon mapping. */
    private Map<String, String> m_iconMapping;

    /** The show editor help flag. */
    private boolean m_isShowEditorHelp;

    /** Keep-alive setting. */
    private boolean m_keepAlive;

    /** The current request locale. */
    private String m_locale;

    /** The login JSP URL. */
    private String m_loginURL;

    /** The current navigation URI. */
    private String m_navigationUri;

    /** The project id. */
    private CmsUUID m_projectId;

    /** The shared folder. */
    private String m_sharedFolder;

    /** The current site root. */
    private String m_siteRoot;

    /** The structure id of the resource. */
    private CmsUUID m_structureId;

    /** The data for the TinyMCE editor. */
    private CmsTinyMCEData m_tinymce;

    /** A flag which indicates whether the toolbar should be shown initially. */
    private boolean m_toolbarVisible;

    /** Flag indicating whether upload buttons should be disabled. */
    private boolean m_uploadDisabled;

    /** The maximum file size for the upload. */
    private long m_uploadFileSizeLimit;

    /** The upload restriction data. */
    private CmsUploadRestrictionInfo m_uploadRestriction;

    /** The current uri. */
    private String m_uri;

    /** The OpenCms VFS prefix. */
    private String m_vfsPrefix;

    /** Flag which indicates whether the user should be warned when editing a reused element. */
    private boolean m_warnWhenEditingReusedElement;

    /** The workplaces resources path prefix. */
    private String m_workplaceResourcesPrefix;

    /** The current workplace locale. */
    private String m_wpLocale;

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
            clone.getLoginURL(),
            clone.getVfsPrefix(),
            clone.getFileExplorerLink(),
            clone.getWorkplaceResourcesPrefix(),
            clone.getEmbeddedDialogsUrl(),
            clone.getSiteRoot(),
            clone.getSharedFolder(),
            clone.getProjectId(),
            clone.getLocale(),
            clone.getWpLocale(),
            clone.getUri(),
            clone.getNavigationUri(),
            clone.getStructureId(),
            clone.getExtensionMapping(),
            clone.getIconMapping(),
            clone.getServerTime(),
            clone.isShowEditorHelp(),
            clone.isToolbarVisible(),
            clone.getDefaultWorkplaceLink(),
            clone.getAboutLink(),
            clone.getUserInfo(),
            clone.getUploadFileSizeLimit(),
            clone.isKeepAlive(),
            clone.isUploadDisabled(),
            clone.m_adeParameters,
            clone.m_uploadRestriction,
            clone.m_categoryBaseFolder,
            clone.m_hideDisabledGalleryTypes,
            clone.m_warnWhenEditingReusedElement);
        setTinymce(clone.getTinymce());
    }

    /**
     * Constructor.<p>
     *
     * @param contentEditorUrl the XML content editor URL
     * @param contentEditorBacklinkUrl the XML content editor back-link URL
     * @param loginUrl the login JSP URL
     * @param vfsPrefix the OpenCms VFS prefix
     * @param fileExplorerLink the file explorer link
     * @param workplaceResourcesPrefix the workplace resources path prefix
     * @param embeddedDialogsUrl the embedded dialogs URL
     * @param siteRoot the current site root
     * @param sharedFolder the shared folder
     * @param projectId the project id
     * @param locale the current request locale
     * @param wpLocale the workplace locale
     * @param uri the current uri
     * @param structureId the structure id of tbe resource
     * @param navigationUri the current navigation URI
     * @param extensionMapping the mappings of file extensions to resource types
     * @param iconMapping the resource icon mapping
     * @param serverTime the current time
     * @param isShowEditorHelp the show editor help flag
     * @param toolbarVisible a flag to indicate whether the toolbar should be visible initially
     * @param defaultWorkplaceLink the default link to use for opening the workplace
     * @param aboutLink the link to the "About" page
     * @param userInfo information about the current user
     * @param uploadFileSizeLimit the file upload size limit
     * @param isKeepAlive the keep-alive mode
     * @param uploadDisabled true if upload buttons should be disabled
     * @param adeParameters the map of ADE configuration parameters
     * @param uploadRestriction the upload restriction data
     * @param categoryBaseFolder the category base folder
     * @param hideDisabledGalleryTypes true if deactivated types should be hidden in the gallery dialog
     * @param warnWhenEditingReusedElement true if a warning dialog should be shown when editing a reused element
     */
    public CmsCoreData(
        String contentEditorUrl,
        String contentEditorBacklinkUrl,
        String loginUrl,
        String vfsPrefix,
        String fileExplorerLink,
        String workplaceResourcesPrefix,
        String embeddedDialogsUrl,
        String siteRoot,
        String sharedFolder,
        CmsUUID projectId,
        String locale,
        String wpLocale,
        String uri,
        String navigationUri,
        CmsUUID structureId,
        Map<String, String> extensionMapping,
        Map<String, String> iconMapping,
        long serverTime,
        boolean isShowEditorHelp,
        boolean toolbarVisible,
        String defaultWorkplaceLink,
        String aboutLink,
        UserInfo userInfo,
        long uploadFileSizeLimit,
        boolean isKeepAlive,
        boolean uploadDisabled,
        Map<String, String> adeParameters,
        CmsUploadRestrictionInfo uploadRestriction,
        String categoryBaseFolder,
        boolean hideDisabledGalleryTypes,
        boolean warnWhenEditingReusedElement) {

        m_contentEditorUrl = contentEditorUrl;
        m_contentEditorBacklinkUrl = contentEditorBacklinkUrl;
        m_loginURL = loginUrl;
        m_vfsPrefix = vfsPrefix;
        m_workplaceResourcesPrefix = workplaceResourcesPrefix;
        m_embeddedDialogsUrl = embeddedDialogsUrl;
        m_siteRoot = siteRoot;
        m_projectId = projectId;
        m_locale = locale;
        m_wpLocale = wpLocale;
        m_uri = uri;
        m_navigationUri = navigationUri;
        m_extensionMapping = extensionMapping;
        m_iconMapping = iconMapping;
        m_serverTime = serverTime;
        m_isShowEditorHelp = isShowEditorHelp;
        m_toolbarVisible = toolbarVisible;
        m_structureId = structureId;
        m_defaultWorkplaceLink = defaultWorkplaceLink;
        m_aboutLink = aboutLink;
        m_userInfo = userInfo;
        m_uploadFileSizeLimit = uploadFileSizeLimit;
        m_keepAlive = isKeepAlive;
        m_adeParameters = adeParameters;
        m_fileExplorerLink = fileExplorerLink;
        m_uploadDisabled = uploadDisabled;
        m_uploadRestriction = uploadRestriction;
        m_sharedFolder = sharedFolder;
        m_categoryBaseFolder = categoryBaseFolder;
        m_hideDisabledGalleryTypes = hideDisabledGalleryTypes;
        m_warnWhenEditingReusedElement = warnWhenEditingReusedElement;
    }

    /**
     * Gets the "About" link.<p>
     *
     * @return the "about" link
     */
    public String getAboutLink() {

        return m_aboutLink;
    }

    /**
     * Gets the map of ADE configuration parameters.<p>
     *
     * @return the ADE configuration parameters
     */
    public Map<String, String> getAdeParameters() {

        return m_adeParameters;
    }

    /**
     * Gets the category folder name.
     *
     * @return the category folder name
     */
    public String getCategoryBaseFolder() {

        return m_categoryBaseFolder;
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
     * Returns the XML content editor URL.<p>
     *
     * @return the XML content editor URL
     */
    public String getContentEditorUrl() {

        return m_contentEditorUrl;
    }

    /**
     * Gets the default link to use for opening the workplace.<p>
     *
     * @return the default workplace link
     */
    public String getDefaultWorkplaceLink() {

        return m_defaultWorkplaceLink;
    }

    /**
     * Returns the embeddedDialogsUrl.<p>
     *
     * @return the embeddedDialogsUrl
     */
    public String getEmbeddedDialogsUrl() {

        return m_embeddedDialogsUrl;
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
     * Returns the resource icon mapping.<p>
     *
     * @return the resource icon mapping
     */
    public Map<String, String> getIconMapping() {

        return m_iconMapping;
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
     * Gets the project id.
     *
     * @return the project id
     */
    public CmsUUID getProjectId() {

        return m_projectId;
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
     * Gets the shared folder.
     *
     * @return the shared folder
     */
    public String getSharedFolder() {

        return m_sharedFolder;
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
     * Gets the data for the TinyMCE editor.<p>
     *
     * @return the data for TinyMCE
     */
    public CmsTinyMCEData getTinymce() {

        // TODO Auto-generated method stub
        return m_tinymce;
    }

    /**
     * Returns the file upload size limit.<p>
     *
     * @return the file upload size limit
     */
    public long getUploadFileSizeLimit() {

        return m_uploadFileSizeLimit;
    }

    /**
     * Gets the upload restriction data.
     *
     * @return the upload restriction data
     */
    public CmsUploadRestrictionInfo getUploadRestriction() {

        return m_uploadRestriction;
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
     * Gets the information about the current user.<p>
     *
     * @return the information about the current user
     */
    public UserInfo getUserInfo() {

        return m_userInfo;
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
     * Returns the workplace resources path prefix.<p>
     *
     * @return the workplace resources path prefix
     */
    public String getWorkplaceResourcesPrefix() {

        return m_workplaceResourcesPrefix;
    }

    /**
     * Gets the language part of the workplace locale.
     *
     * @return the language part of the workplace locale
     */
    public String getWpLanguage() {

        String locale = getWpLocale();
        String result = locale;
        int underscorePos = locale.indexOf("_");
        if (underscorePos > -1) {
            result = locale.substring(0, underscorePos);
        }
        return result;
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
     * Returns true if deactivated types should be hidden in the gallery dialog.
     *
     * @return true if deactivated types should be hidden
     */
    public boolean isHideDisabledGalleryTypes() {

        return m_hideDisabledGalleryTypes;
    }

    /**
     * Returns true if the session should be kept alive even without user actions.<p>
     *
     * @return true if keep-alive mode is active
     */
    public boolean isKeepAlive() {

        return m_keepAlive;
    }

    /**
     * Returns the show editor help flag.<p>
     *
     * @return the show editor help flag
     */
    public boolean isShowEditorHelp() {

        return m_isShowEditorHelp;
    }

    /**
     * Returns true if the toolbar should be visible initially.<p>
     *
     * @return true if the toolbar should be visible initially
     */
    public boolean isToolbarVisible() {

        return m_toolbarVisible;
    }

    /**
     * Checks if uploads are disabled.
     *
     * @return true if uploads are disabled
     */
    public boolean isUploadDisabled() {

        return m_uploadDisabled;

    }

    /**
     * Checks if a warning dialog should be shown when a reused element is edited.
     *
     * @return true if a warning dialog should be shown when editing reused elements
     */
    public boolean isWarnWhenEditingReusedElement() {

        return m_warnWhenEditingReusedElement;
    }

    /**
     * Sets the data for the TinyMCE editor.<p>
     *
     * @param tinyMceData the data for TinyMCE
     */
    public void setTinymce(CmsTinyMCEData tinyMceData) {

        m_tinymce = tinyMceData;
    }

    /**
     * Enables/disables warning dialog when editing reused elements.
     *
     * @param warnWhenEditingReusedElement true if a warning dialog should be shown when editing reused elements
     */
    public void setWarnWhenEditingReusedElement(boolean warnWhenEditingReusedElement) {

        m_warnWhenEditingReusedElement = warnWhenEditingReusedElement;
    }

    /**
     * Returns the file explorer link prefix. Append resource site path for complete link.<p>
     *
     * @return the file explorer link prefix
     */
    protected String getFileExplorerLink() {

        return m_fileExplorerLink;
    }

    /**
     * Sets the show editor help flag.<p>
     *
     * @param show <code>true</code> to show editor help
     */
    protected void setShowEditorHelp(boolean show) {

        m_isShowEditorHelp = show;
    }
}