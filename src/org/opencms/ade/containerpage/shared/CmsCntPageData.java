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

package org.opencms.ade.containerpage.shared;

import org.opencms.gwt.shared.CmsTemplateContextInfo;
import org.opencms.util.CmsUUID;

import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Convenience class to provide server-side information to the client.<p>
 * 
 * @since 8.0.0
 */
public final class CmsCntPageData implements IsSerializable {

    /** Name of the used dictionary. */
    public static final String DICT_NAME = "org_opencms_ade_containerpage";

    /** Key 'isDetailOnly' used within the JSON representation of a container object. */
    public static final String JSONKEY_DETAILONLY = "isDetailOnly";

    /** Key 'isDetailView' used within the JSON representation of a container object. */
    public static final String JSONKEY_DETAILVIEW = "isDetailView";

    /** Key 'elements' used within the JSON representation of a container object. */
    public static final String JSONKEY_ELEMENTS = "elements";

    /** Key 'maxElements' used within the JSON representation of a container object. */
    public static final String JSONKEY_MAXELEMENTS = "maxElements";

    /** Key 'name' used within the JSON representation of a container object. */
    public static final String JSONKEY_NAME = "name";

    /** Key 'type' used within the JSON representation of a container object. */
    public static final String JSONKEY_TYPE = "type";

    /** Key 'width' used within the JSON representation of a container object. */
    public static final String JSONKEY_WIDTH = "width";

    /** Key 'elementId' for the element id. */
    public static final String JSONKEY_ELEMENT_ID = "elementId";

    /** Key 'detailElementId' for the detail content id. */
    public static final String JSONKEY_DETAIL_ELEMENT_ID = "detailElementId";

    /** The editor back-link URI. */
    private static final String BACKLINK_URI = "/system/modules/org.opencms.ade.containerpage/editor-backlink.html";

    /** The xml-content editor URI. */
    private static final String EDITOR_URI = "/system/workplace/editors/editor.jsp";

    /** The detail view container resource path. */
    private String m_detailContainerPage;

    /** The detail structure id, if available. */
    private CmsUUID m_detailId;

    /** Flag which determines whether small elements should be editable initially. */
    private boolean m_editSmallElementsInitially;

    /** The date at which the container page was last modified. */
    private long m_lastModified;

    /** The content locale. */
    private String m_locale;

    /** The lock information, if the page is locked by another user. */
    private String m_lockInfo;

    /** The map of available types and their new resource id's. */
    private Map<String, String> m_newTypes;

    /** The reason why the user is not able to edit the current container page. */
    private String m_noEditReason;

    /** The original request parameters. */
    private String m_requestParams;

    /** The current sitemap URI. */
    private String m_sitemapUri;

    /** The template context information. */
    private CmsTemplateContextInfo m_templateContextInfo;

    /** Flag indicating to use the classic XmlContent editor. */
    private boolean m_useClassicEditor;

    /**
     * Constructor.<p>
     * 
     * @param noEditReason the reason why the current user is not allowed to edit the current container page
     * @param requestParams the original request parameters
     * @param sitemapUri the current sitemap URI
     * @param detailId the detail resource id, if available
     * @param detailContainerPage the detail view container resource path
     * @param newTypes the map of available types and their new resource id's
     * @param lastModified the last modification date of the page 
     * @param lockInfo lock information, if the page is locked by another user
     * @param locale the content locale
     * @param useClassicEditor <code>true</code> to use the classic XmlContent editor
     * @param contextInfo the template context information 
     * @param showSmallElementsInitially flag which controls whether small elements should be shown initially 
     */
    public CmsCntPageData(
        String noEditReason,
        String requestParams,
        String sitemapUri,
        CmsUUID detailId,
        String detailContainerPage,
        Map<String, String> newTypes,
        long lastModified,
        String lockInfo,
        String locale,
        boolean useClassicEditor,
        CmsTemplateContextInfo contextInfo,
        boolean showSmallElementsInitially) {

        m_noEditReason = noEditReason;
        m_requestParams = requestParams;
        m_sitemapUri = sitemapUri;
        m_newTypes = newTypes;
        m_lastModified = lastModified;
        m_lockInfo = lockInfo;
        m_locale = locale;
        m_detailId = detailId;
        m_detailContainerPage = detailContainerPage;
        m_useClassicEditor = useClassicEditor;
        m_templateContextInfo = contextInfo;
        m_editSmallElementsInitially = showSmallElementsInitially;
    }

    /**
     * Serialization constructor.<p> 
     */
    protected CmsCntPageData() {

        // empty
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
     * Returns the detail view container resource path.<p>
     *
     * @return the detail view container resource path
     */
    public String getDetailContainerPage() {

        return m_detailContainerPage;
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
     * Returns the xml-content editor URI.<p>
     * 
     * @return the xml-content editor URI
     */
    public String getEditorUri() {

        return EDITOR_URI;
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
     * Returns the lock information, if the page is locked by another user.<p>
     *
     * @return the lock infomation
     */
    public String getLockInfo() {

        return m_lockInfo;
    }

    /**
     * Returns the map of available types and their new resource id's.<p>
     * 
     * @return the map of available types and their new resource id's
     */
    public Map<String, String> getNewTypes() {

        return m_newTypes;
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
     * Returns the request parameters.<p>
     * 
     * @return the request parameters
     */
    public String getRequestParams() {

        return m_requestParams;
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
     * Returns if the classic XmlContent editor should be used.<p>
     *
     * @return <code>true</code> if the classic XmlContent editor should be used
     */
    public boolean isUseClassicEditor() {

        return m_useClassicEditor;
    }

}
