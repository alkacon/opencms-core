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

package org.opencms.ade.containerpage.shared;

import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.CmsTemplateContextInfo;
import org.opencms.util.CmsUUID;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Convenience class to provide server-side information to the client.<p>
 *
 * @since 8.0.0
 */
public final class CmsCntPageData implements IsSerializable {

    /** The element delte modes. */
    public enum ElementDeleteMode {
        /** Don't ask, delete no longer referenced element resources. */
        alwaysDelete,
        /** Don't ask, keep no longer referenced element resources. */
        alwaysKeep,
        /** Ask if no longer referenced element resources should be deleted. Delete is preselected. */
        askDelete,
        /** Ask if no longer referenced element resources should be deleted. Keep is preselected. */
        askKeep
    }

    /** Enum representing the different ways dropping elements on a container page can be handled. */
    public enum ElementReuseMode {

        /** The user will be asked whether they want the 'copy' or 'reuse' behavior. */
        ask,

        /** The dropped element will be copied, and the container page will link to the copy. */
        copy,

        /** The container page will link to the dropped element. */
        reuse
    }

    /** Name of the used dictionary. */
    public static final String DICT_NAME = "org_opencms_ade_containerpage";

    /** Key 'detailElementId' for the detail content id. */
    public static final String JSONKEY_DETAIL_ELEMENT_ID = "detailElementId";

    /** Key 'isDetailOnly' used within the JSON representation of a container object. */
    public static final String JSONKEY_DETAILONLY = "isDetailOnly";

    /** Key 'isDetailView' used within the JSON representation of a container object. */
    public static final String JSONKEY_DETAILVIEW = "isDetailView";

    /** Key 'elementId' for the element id. */
    public static final String JSONKEY_ELEMENT_ID = "elementId";

    /** Key 'elements' used within the JSON representation of a container object. */
    public static final String JSONKEY_ELEMENTS = "elements";

    /** Key 'isDetailViewContainer' used within the JSON representation of a container object. */
    public static final String JSONKEY_ISDETAILVIEWCONTAINER = "isDetailViewContainer";

    /** Key 'maxElements' used within the JSON representation of a container object. */
    public static final String JSONKEY_MAXELEMENTS = "maxElements";

    /** Key 'name' used within the JSON representation of a container object. */
    public static final String JSONKEY_NAME = "name";

    /** JSON key for presets. */
    public static final String JSONKEY_PRESETS = "presets";

    /** Key 'type' used within the JSON representation of a container object. */
    public static final String JSONKEY_TYPE = "type";

    /** Key 'width' used within the JSON representation of a container object. */
    public static final String JSONKEY_WIDTH = "width";

    /** The editor back-link URI. */
    private static final String BACKLINK_URI = "/system/modules/org.opencms.ade.containerpage/editor-backlink.html";

    /** Temporary flag to disable the option to edit settings in content editor. */
    private boolean m_allowSettingsInEditor;

    /** The app title to display in the toolbar. */
    private String m_appTitle;

    /** The element delete mode. */
    private ElementDeleteMode m_deleteMode;

    /** The detail view container resource path. */
    private String m_detailContainerPage;

    /** The detail structure id, if available. */
    private CmsUUID m_detailId;

    /** The set of names of types for which the current page is registered as a detail page. */
    private Set<String> m_detailTypes;

    /** Flag which determines whether small elements should be editable initially. */
    private boolean m_editSmallElementsInitially;

    /** The current element view. */
    private CmsElementViewInfo m_elementView;

    /** The element views. */
    private List<CmsElementViewInfo> m_elementViews;

    /** True if the page is used for model groups. */
    private boolean m_isModelGroup;

    /** True if the container page is a model page. */
    private boolean m_isModelPage;

    /** The date at which the container page was last modified. */
    private long m_lastModified;

    /** The time when the page was loaded. */
    private long m_loadTime;

    /** The content locale. */
    private String m_locale;

    /** The locale link beans. */
    private Map<String, CmsLocaleLinkBean> m_localeLinkBeans;

    /** The lock information, if the page is locked by another user. */
    private String m_lockInfo;

    /** The main locale to this page in case it is part of a locale group. */
    private String m_mainLocale;

    /** The model group base element id. */
    private String m_modelGroupEmenetId;

    /** The reason why the user is not able to edit the current container page. */
    private String m_noEditReason;

    /** The online link to the current page. */
    private String m_onlineLink;

    /** The current page info. */
    private CmsListInfoBean m_pageInfo;

    /** The original request parameters. */
    private String m_requestParams;

    /** The element reuse mode. */
    private ElementReuseMode m_reuseMode;

    /** The RPC context. */
    private CmsContainerPageRpcContext m_rpcContext;

    /** Flag indicating if the current user has the sitemap manager role. */
    private boolean m_sitemapManager;

    /** The current sitemap URI. */
    private String m_sitemapUri;

    /** The template context information. */
    private CmsTemplateContextInfo m_templateContextInfo;

    /** Flag indicating to use the classic XmlContent editor. */
    private boolean m_useClassicEditor;

    /** The detail container page id (may be null). */
    private CmsUUID m_detailContainerPageId;

    /** Key-value pairs to store in the sessionStorage. */
    private Map<String, String> m_sessionStorageData = new HashMap<>();

    /** Indicates if placement mode is enabled. */
    private boolean m_placementModeEnabled;

    /**
     * Constructor.<p>
     *
     * @param onlineLink the online link to the current page
     * @param noEditReason the reason why the current user is not allowed to edit the current container page
     * @param requestParams the original request parameters
     * @param sitemapUri the current sitemap URI
     * @param sitemapManager if the user has the sitemap manager role
     * @param detailId the detail resource id, if available
     * @param detailContainerPage the detail view container resource path
     * @param detailContainerPageId the detail view container page structure id
     * @param detailTypes the set of names of types for which this page is registered as detail page
     * @param lastModified the last modification date of the page
     * @param lockInfo lock information, if the page is locked by another user
     * @param pageInfo the current page info
     * @param locale the content locale
     * @param useClassicEditor <code>true</code> to use the classic XmlContent editor
     * @param contextInfo the template context information
     * @param showSmallElementsInitially flag which controls whether small elements should be shown initially
     * @param elementViews the element views
     * @param elementView the current element view
     * @param reuseMode the element reuse mode
     * @param deleteMode the element delete mode
     * @param isModelPage true if this is a model page
     * @param isModelGroup true if the page is used for model groups
     * @param modelGroupEmenetId the model group base element id
     * @param mainLocale the main locale to this page in case it is part of a locale group
     * @param localeLinkBeans beans for links to other pages in the locale group
     * @param appTitle the title to display in the toolbar
     * @param loadTime the current time
     */
    public CmsCntPageData(
        String onlineLink,
        String noEditReason,
        String requestParams,
        String sitemapUri,
        boolean sitemapManager,
        CmsUUID detailId,
        String detailContainerPage,
        CmsUUID detailContainerPageId,
        Set<String> detailTypes,
        long lastModified,
        String lockInfo,
        CmsListInfoBean pageInfo,
        String locale,
        boolean useClassicEditor,
        CmsTemplateContextInfo contextInfo,
        boolean showSmallElementsInitially,
        List<CmsElementViewInfo> elementViews,
        CmsElementViewInfo elementView,
        ElementReuseMode reuseMode,
        ElementDeleteMode deleteMode,
        boolean isModelPage,
        boolean isModelGroup,
        String modelGroupEmenetId,
        String mainLocale,
        Map<String, CmsLocaleLinkBean> localeLinkBeans,
        String appTitle,
        long loadTime) {

        m_onlineLink = onlineLink;
        m_noEditReason = noEditReason;
        m_requestParams = requestParams;
        m_sitemapUri = sitemapUri;
        m_sitemapManager = sitemapManager;
        m_lastModified = lastModified;
        m_lockInfo = lockInfo;
        m_pageInfo = pageInfo;
        m_locale = locale;
        m_detailId = detailId;
        m_detailContainerPage = detailContainerPage;
        m_detailContainerPageId = detailContainerPageId;
        m_detailTypes = detailTypes;
        m_useClassicEditor = useClassicEditor;
        m_templateContextInfo = contextInfo;
        m_editSmallElementsInitially = showSmallElementsInitially;
        m_elementViews = elementViews;
        m_elementView = elementView;
        m_reuseMode = reuseMode;
        m_deleteMode = deleteMode;
        m_isModelPage = isModelPage;
        m_isModelGroup = isModelGroup;
        m_modelGroupEmenetId = modelGroupEmenetId;
        m_mainLocale = mainLocale;
        m_localeLinkBeans = localeLinkBeans;
        m_appTitle = appTitle;
        m_loadTime = loadTime;
    }

    /**
     * Serialization constructor.<p>
     */
    protected CmsCntPageData() {

        // empty
    }

    /**
     * Returns whether editing settings in the content editor is allowed.<p>
     *
     * @return <code>true</code> in case editing settings in the content editor is allowed
     */
    public boolean allowSettingsInEditor() {

        return m_allowSettingsInEditor;
    }

    /**
     * Gets the title to display in the toolbar.<p>
     *
     * @return the title for the toolbar
     */
    public String getAppTitle() {

        return m_appTitle;
    }

    /**
     * Returns the xml-content editor back-link URI.<p>
     *
     * @return the back-link URI
     */
    public String getBacklinkUri() {

        return BACKLINK_URI;
    }

    /**
     * Gets the date at which the page was last modified.<p>
     *
     * @return the date at which the page was last modified
     */
    public long getDateLastModified() {

        return m_lastModified;
    }

    /**
     * Returns the element delete mode.<p>
     *
     * @return the element delete mode
     */
    public ElementDeleteMode getDeleteMode() {

        return m_deleteMode;
    }

    /**
     * Returns the detail view container resource path.<p>
     *
     * @return the detail view container resource path
     */
    public String getDetailContainerPage() {

        return m_detailContainerPage;
    }

    /**
     * Gets the structure id of the detail container page (or null if we are not on on a detail page, or there is no detail container page).
     *
     * @return the structure id of the detail container page
     */
    public CmsUUID getDetailContainerPageId() {

        return m_detailContainerPageId;
    }

    /**
     * Returns the detail structure id, if available.<p>
     *
     * @return the detail structure id
     */
    public CmsUUID getDetailId() {

        return m_detailId;
    }

    /**
     * Gets the set of names of types for which the container page is registered as a detail page.
     *
     * @return the set of names of detail types
     */
    public Set<String> getDetailTypes() {

        return m_detailTypes;
    }

    /**
     * Gets the element reuse mode.<p>
     *
     * @return the element reuse mode
     */
    public ElementReuseMode getElementReuseMode() {

        return m_reuseMode;
    }

    /**
     * Returns the current element view.<p>
     *
     * @return the current element view
     */
    public CmsElementViewInfo getElementView() {

        return m_elementView;
    }

    /**
     * Returns the available element views.<p>
     *
     * @return the element views
     */
    public List<CmsElementViewInfo> getElementViews() {

        return m_elementViews;
    }

    /**
     * Returns the time off page load.<p>
     *
     * @return the time stamp
     */
    public long getLoadTime() {

        return m_loadTime;
    }

    /**
     * Returns the content locale.<p>
     *
     * @return the locale
     */
    public String getLocale() {

        return m_locale;
    }

    /**
     * Gets the locale link beans, with localized language names as keys.<p>
     *
     * The beans represent links to different locale variants of this page.
     *
     * @return the locale link bean map for this
     */
    public Map<String, CmsLocaleLinkBean> getLocaleLinkBeans() {

        return m_localeLinkBeans;
    }

    /**
     * Returns the lock information, if the page is locked by another user.<p>
     *
     * @return the lock infomation
     */
    public String getLockInfo() {

        return m_lockInfo;
    }

    /**
     * Returns the main locale to this page in case it is part of a locale group.<p>
     *
     * @return the main locale to this page in case it is part of a locale group
     */
    public String getMainLocale() {

        return m_mainLocale;
    }

    /**
     * Returns the model group base element id.<p>
     *
     * @return the model group base element id
     */
    public String getModelGroupElementId() {

        return m_modelGroupEmenetId;
    }

    /**
     * Returns the no-edit reason.<p>
     *
     * @return the no-edit reason, if empty editing is allowed
     */
    public String getNoEditReason() {

        return m_noEditReason;
    }

    /**
     * Returns the online link to the current page.<p>
     *
     * @return the online link to the current page
     */
    public String getOnlineLink() {

        return m_onlineLink;
    }

    /**
     * Returns the current page info.<p>
     *
     * @return the current page info
     */
    public CmsListInfoBean getPageInfo() {

        return m_pageInfo;
    }

    /**
     * Returns the request parameters.<p>
     *
     * @return the request parameters
     */
    public String getRequestParams() {

        return m_requestParams;
    }

    /**
     * Gets the RPC context.<p>
     *
     * @return the RPC context
     */
    public CmsContainerPageRpcContext getRpcContext() {

        return m_rpcContext;
    }

    /**
     * Gets the key-value pairs to store in the session storage of the browser.
     *
     * @return the map of entries to store in the session storage
     */
    public Map<String, String> getSessionStorageData() {

        return m_sessionStorageData;
    }

    /**
     * Returns the sitemap URI.<p>
     *
     * @return the sitemap URI
     */
    public String getSitemapUri() {

        return m_sitemapUri;
    }

    /**
     * Gets the template context information.<p>
     *
     * @return the template context information
     */
    public CmsTemplateContextInfo getTemplateContextInfo() {

        return m_templateContextInfo;
    }

    /**
     * Returns true if small elements should be editable initially.<p>
     *
     * @return true if small elements should be editable initially
     */
    public boolean isEditSmallElementsInitially() {

        return m_editSmallElementsInitially;
    }

    /**
     * Returns if the page is used for model groups.<p>
     *
     * @return true if the page is used for model groups
     */
    public boolean isModelGroup() {

        return m_isModelGroup;
    }

    /**
     * True if the container page is a model page.<P>
     *
     * @return true if this is a model page
     */
    public boolean isModelPage() {

        return m_isModelPage;
    }

    /**
     * Returns true if placement mode is enabled.
     *
     * @return true if placement mode is enabled
     */
    public boolean isPlacementModeEnabled() {
        return m_placementModeEnabled;
    }

    /**
     * Returns if the current user has the sitemap manager role.<p>
     *
     * @return if the current user has the sitemap manager role
     */
    public boolean isSitemapManager() {

        return m_sitemapManager;
    }

    /**
     * Returns if the classic XmlContent editor should be used.<p>
     *
     * @return <code>true</code> if the classic XmlContent editor should be used
     */
    public boolean isUseClassicEditor() {

        return m_useClassicEditor;
    }

    /**
     * Sets whether editing settings in the content editor is allowed.<p>
     *
     * @param allowSettingsInEditor <code>true</code> to set editing settings in the content editor is allowed
     */
    public void setAllowSettingsInEditor(boolean allowSettingsInEditor) {

        m_allowSettingsInEditor = allowSettingsInEditor;
    }

    /**
     * Sets the 'placement mode enabled' flag.
     *
     * @param placementModeEnabled the new value for the 'placement mode enabled' flag
     */
    public void setPlacementModeEnabled(boolean placementModeEnabled) {
        m_placementModeEnabled = placementModeEnabled;
    }

    /**
     * Sets the RPC context.<p>
     *
     * @param context the RPC context
     */
    public void setRpcContext(CmsContainerPageRpcContext context) {

        m_rpcContext = context;
    }

    /**
     * Sets the entries to store in the browser's session storage.
     *
     * @param sessionStorageData the entries to store in the browser's session storage
     */
    public void setSessionStorageData(Map<String, String> sessionStorageData) {

        m_sessionStorageData = sessionStorageData;
    }

}
