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

package org.opencms.workplace.galleries;

import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeFolderExtended;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsCategory;
import org.opencms.relations.CmsCategoryService;
import org.opencms.security.CmsPermissionSet;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceManager;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Provides the general helper methods to generate the content of a gallery dialog used in the XML content editors,
 * WYSIWYG editors and context menu. It <p>
 *
 * It is also used for AJAX requests to dynamically switch galleries or categories and get additional information
 * for the currently active item of the dialog.<p>
 *
 * Extend this class for every gallery type (e.g. image gallery or download gallery) to build.<p>
 *
 * @since 7.5.0
 */
public abstract class A_CmsAjaxGallery extends CmsDialog {

    /** Request parameter value for the action: change the item link url value. */
    public static final String DIALOG_CHANGEITEMLINKURL = "changeitemlinkurl";

    /** Request parameter value for the action: change the item title property value. */
    public static final String DIALOG_CHANGEITEMTITLE = "changeitemtitle";

    /** Request parameter value for the action: get the currently active item object. */
    public static final String DIALOG_GETACTIVEITEM = "getactiveitem";

    /** Request parameter value for the action: get the category selection list. */
    public static final String DIALOG_GETCATEGORIES = "getcategories";

    /** Request parameter value for the action: get the gallery selection list. */
    public static final String DIALOG_GETGALLERIES = "getgalleries";

    /** Request parameter value for the action: get a specific gallery. */
    public static final String DIALOG_GETGALLERY = "getgallery";

    /** Request parameter value for the action: get the items for a gallery or category. */
    public static final String DIALOG_GETITEMS = "getitems";

    /** Request parameter value for the action: list gallery items. */
    public static final String DIALOG_LIST = "list";

    /** The list mode name "category" for getting the items. */
    public static final String LISTMODE_CATEGORY = "category";

    /** The list mode name "gallery" for getting the items. */
    public static final String LISTMODE_GALLERY = "gallery";

    /** Request parameter value for the dialog mode: editor. */
    public static final String MODE_EDITOR = "editor";

    /** Request parameter value for the dialog mode: view. */
    public static final String MODE_VIEW = "view";

    /** Request parameter value for the dialog mode: widget. */
    public static final String MODE_WIDGET = "widget";

    /** Request parameter name for the dialog mode (widget or editor). */
    public static final String PARAM_DIALOGMODE = "dialogmode";

    /** Request parameter name for the edited resource. */
    public static final String PARAM_EDITEDRESOURCE = "editedresource";

    /** Request parameter name for the input field id. */
    public static final String PARAM_FIELDID = "fieldid";

    /** Request parameter name for the gallery path. */
    public static final String PARAM_GALLERYPATH = "gallerypath";

    /** Request parameter name for the active item path. */
    public static final String PARAM_ITEMPATH = "itempath";

    /** Request parameter name for the dialog initialization parameters. */
    public static final String PARAM_PARAMS = "params";

    /** Request parameter name for the startup folder. */
    public static final String PARAM_STARTUPFOLDER = "startupfolder";

    /** Request parameter name for the startup type. */
    public static final String PARAM_STARTUPTYPE = "startuptype";

    /** The galleries path in the workplace containing the JSPs. */
    public static final String PATH_GALLERIES = CmsWorkplace.VFS_PATH_WORKPLACE + "galleries/";

    /** Value that is returned if no result was found. */
    public static final String RETURNVALUE_NONE = "none";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(A_CmsAjaxGallery.class);

    /** The optional parameters for the gallery from the XML configuration. */
    protected String m_galleryTypeParams;

    /** The gallery items to display. */
    private List<CmsResource> m_galleryItems;

    /** The dialog mode the gallery is running in. */
    private String m_paramDialogMode;

    /** The input field id that is required when in widget mode. */
    private String m_paramFieldId;

    /** The current gallery path. */
    private String m_paramGalleryPath;

    /** The list mode to get the item either from a gallery or by a category. */
    private String m_paramListMode;

    /** The value of the property (current property definition: Title). */
    private String m_paramPropertyValue;

    /** The gallery base resource type. */
    private CmsResourceTypeFolderExtended m_resourceType;

    /**
     * Public empty constructor, required for {@link A_CmsAjaxGallery#createInstance(String, CmsJspActionElement)}.<p>
     */
    public A_CmsAjaxGallery() {

        this(null);
    }

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public A_CmsAjaxGallery(CmsJspActionElement jsp) {

        super(jsp);
        // perform other initialization
        init();

    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public A_CmsAjaxGallery(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Creates a new gallery instance of the given gallery type name.<p>
     *
     * @param galleryTypeName the gallery type name to create the instance for
     * @param jsp an initialized JSP action element
     *
     * @return a new gallery instance of the given gallery type name
     */
    public static A_CmsAjaxGallery createInstance(String galleryTypeName, CmsJspActionElement jsp) {

        if (jsp != null) {
            // must have a valid JSP in order to read from the user session
            HttpSession session = jsp.getRequest().getSession();
            // lookup the workplace settings
            CmsWorkplaceSettings settings = (CmsWorkplaceSettings)session.getAttribute(
                CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS);
            if (settings != null) {
                if (CmsStringUtil.isEmpty(galleryTypeName)) {
                    // look up the gallery type from the settings
                    galleryTypeName = settings.getGalleryType();
                } else {
                    // store the last used gallery type name
                    settings.setGalleryType(galleryTypeName);
                }
            }
        }
        // get the gallery class name for the type
        A_CmsAjaxGallery template = OpenCms.getWorkplaceManager().getGalleries().get(galleryTypeName);

        if (template == null) {
            // requested gallery type is not configured
            CmsMessageContainer message;
            if (jsp == null) {
                message = Messages.get().container(Messages.LOG_UNKNOWN_GALLERY_TYPE_REQ_1, galleryTypeName);
            } else {
                message = Messages.get().container(
                    Messages.LOG_UNKNOWN_GALLERY_TYPE_REQ_JSP_2,
                    galleryTypeName,
                    jsp.info("opencms.request.element.uri"));
            }
            LOG.error(message.key());
            throw new CmsRuntimeException(message);
        }

        try {
            // first get the class of the gallery
            Class<?> galleryClass = Class.forName(template.getResourceType().getFolderClassName());
            // create a new instance and cast to a gallery
            A_CmsAjaxGallery galleryInstance = (A_CmsAjaxGallery)galleryClass.newInstance();
            // set the type name and id
            galleryInstance.m_resourceType = template.getResourceType();
            galleryInstance.m_galleryTypeParams = template.getResourceType().getFolderClassParams();
            // initialize the members
            galleryInstance.initWorkplaceMembers(jsp);
            // perform other initialization
            galleryInstance.init();
            // return the result
            return galleryInstance;
        } catch (Exception e) {
            // requested type is not configured
            CmsMessageContainer message;
            if (jsp == null) {
                message = Messages.get().container(
                    Messages.LOG_CREATE_GALLERY_INSTANCE_FAILED_2,
                    template.getResourceType().getFolderClassName(),
                    galleryTypeName);
            } else {
                message = Messages.get().container(
                    Messages.LOG_CREATE_GALLERY_INSTANCE_FAILED_JSP_3,
                    template.getResourceType().getFolderClassName(),
                    galleryTypeName,
                    jsp.info("opencms.request.element.uri"));
            }
            LOG.error(message.key());
            throw new CmsRuntimeException(message);
        }
    }

    /**
     * Returns a list of galleries which have the required gallery type id.<p>
     *
     * @param galleryTypeId type id of the gallery
     * @param cms the initialized CmsObject for the current user
     * @return a list of galleries
     */
    public static List<CmsResource> getGalleries(int galleryTypeId, CmsObject cms) {

        List<CmsResource> galleries = new ArrayList<CmsResource>();
        try {
            // get the galleries of the current site
            galleries = cms.readResources("/", CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(galleryTypeId));
        } catch (CmsException e) {
            // error reading resources with filter
            LOG.error(e.getLocalizedMessage(), e);
        }

        // if the current site is NOT the root site - add all other galleries from the system path
        if (!cms.getRequestContext().getSiteRoot().equals("")) {
            List<CmsResource> systemGalleries = null;
            try {
                // get the galleries in the /system/ folder
                systemGalleries = cms.readResources(
                    CmsWorkplace.VFS_PATH_SYSTEM,
                    CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(galleryTypeId));
            } catch (CmsException e) {
                // error reading resources with filter
                LOG.error(e.getLocalizedMessage(), e);
            }

            if ((systemGalleries != null) && (systemGalleries.size() > 0)) {
                // add the found system galleries to the result
                galleries.addAll(systemGalleries);
            }
        }

        // return the found galleries
        return galleries;
    }

    /**
     * Initializes the gallery dialog before redirecting.<p>
     *
     * @param wp the workplace object
     */
    public static void initGallery(CmsDialog wp) {

        // 1. get "gallerytypename" by reading the folderpath
        String galleryTypeName = null;
        if (wp.useNewStyle()) {
            galleryTypeName = CmsResource.getName(CmsResource.getFolderPath(wp.getAdminTool().getHandler().getLink()));
        } else {
            galleryTypeName = CmsResource.getName(CmsResource.getFolderPath(wp.getJsp().getRequestContext().getUri()));
        }
        if (galleryTypeName.endsWith("/")) {
            galleryTypeName = galleryTypeName.substring(0, galleryTypeName.length() - 1);
        }
        if (!galleryTypeName.equals("commons")) {
            // 2. set in user settings
            wp.getSettings().setGalleryType(galleryTypeName);
        }
    }

    /**
     * Called from the JSP that is used for the AJAX requests to OpenCms.<p>
     */
    public void displayDialog() {

        if (DIALOG_CHANGEITEMTITLE.equals(getParamAction())) {
            // build the JSON object for the current item with changed title property
            changeItemTitle(getJsp().getRequest().getParameter(PARAM_ITEMPATH));
        } else if (DIALOG_CHANGEITEMLINKURL.equals(getParamAction())) {
            // build the JSON object for the current item with changed resource content (CmsResourcePointer)
            changeItemLinkUrl(getJsp().getRequest().getParameter(PARAM_ITEMPATH));
        } else if (DIALOG_GETCATEGORIES.equals(getParamAction())) {
            // get the available categories as JSON array
            buildJsonCategoryList();
        } else if (DIALOG_GETGALLERIES.equals(getParamAction())) {
            // get the available galleries as JSON array
            buildJsonGalleryList();
        } else if (DIALOG_GETGALLERY.equals(getParamAction())) {
            // get the desired gallery as JSON object
            buildJsonGalleryItem(getJsp().getRequest().getParameter(PARAM_GALLERYPATH));
        } else if (DIALOG_GETITEMS.equals(getParamAction())) {
            if (LISTMODE_CATEGORY.equals(getParamListMode())) {
                // get the items of selected category
                buildJsonResourceItems(getCategoryItems(), null);
            } else {
                // get the items of a selected gallery
                buildJsonResourceItems(getGalleryItems(), getParamGalleryPath());
            }
        } else if (DIALOG_GETACTIVEITEM.equals(getParamAction())) {
            // build the JSON object for the currently active item
            buildJsonActiveItem(getJsp().getRequest().getParameter(PARAM_ITEMPATH));
        }
    }

    /**
     * Returns a list of galleries which have the required gallery type id.<p>
     *
     * @return a list of galleries
     */
    public List<CmsResource> getGalleries() {

        return getGalleries(getGalleryTypeId(), getCms());
    }

    /**
     * Returns a list of gallery items (resources) for the currently selected gallery and resource type id.<p>
     *
     * @return a list of gallery items (resources)
     */
    public List<CmsResource> getGalleryItems() {

        if (m_galleryItems == null) {
            // gallery items have not been read yet
            int resTypeId = getGalleryItemsTypeId();
            if (CmsStringUtil.isNotEmpty(getParamGalleryPath())) {
                try {
                    // set last used gallery in settings to current gallery
                    getSettings().setLastUsedGallery("" + getGalleryTypeId(), getParamGalleryPath());
                    CmsResourceFilter filter;
                    if (resTypeId == -1) {
                        // filter all resources that are files
                        filter = CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireTimerange().addRequireFile();
                    } else {
                        // filter all resources of the required type
                        filter = CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireTimerange().addRequireType(
                            resTypeId);
                    }
                    m_galleryItems = getCms().readResources(getParamGalleryPath(), filter, false);
                } catch (CmsException e) {
                    // error reading resources
                    LOG.error(e.getLocalizedMessage(), e);
                } catch (NullPointerException e) {
                    // ignore this exception
                }
            }
        }
        return m_galleryItems;
    }

    /**
     * Returns the type id of the gallery items that should be listed.<p>
     *
     * In case of downloadgallery use '-1' to list all resources excluding folders.<p>
     *
     * @return the type id of the gallery items that should be listed
     */
    public abstract int getGalleryItemsTypeId();

    /**
     * Returns the type id of this gallery instance.<p>
     *
     * @return the type id of this gallery instance
     */
    public abstract int getGalleryTypeId();

    /**
     * Returns the type name of this gallery instance.<p>
     *
     * @return the type name of this gallery instance
     */
    public abstract String getGalleryTypeName();

    /**
     * Returns the (optional) parameters of this gallery instance.<p>
     *
     * @return the (optional) parameters of this gallery instance
     */
    public String getGalleryTypeParams() {

        return m_galleryTypeParams;
    }

    /**
     * Returns the current mode of the dialog.<p>
     *
     * This is necessary to distinguish between widget mode, view mode and editor mode.<p>
     *
     * @return the current mode of the dialog
     */
    public String getParamDialogMode() {

        if (m_paramDialogMode == null) {
            return "";
        }
        return m_paramDialogMode;
    }

    /**
     * Returns the input field ID when in widget mode.<p>
     *
     * @return the input field ID
     */
    public String getParamFieldId() {

        return m_paramFieldId;
    }

    /**
     * Returns the path of the gallery to display.<p>
     *
     * @return the path of the gallery to display
     */
    public String getParamGalleryPath() {

        if (CmsStringUtil.isEmpty(m_paramGalleryPath)) {
            m_paramGalleryPath = "";
        }
        return m_paramGalleryPath;
    }

    /**
     * Returns the list mode for getting the items, either {@link #LISTMODE_CATEGORY} or {@link #LISTMODE_GALLERY}.<p>
     *
     * @return the list mode for getting the item
     */
    public String getParamListMode() {

        return m_paramListMode;
    }

    /**
     * Returns the property value parameter.<p>
     *
     * @return the property value parameter
     */
    public String getParamPropertyValue() {

        return m_paramPropertyValue;
    }

    /**
     * Returns the extended folder resource type this gallery is based on.<p>
     *
     * @return the extended folder resource type this gallery is based on
     */
    public CmsResourceTypeFolderExtended getResourceType() {

        return m_resourceType;
    }

    /**
     * Initialization method that is called after the gallery instance has been created.<p>
     *
     * It can be overwritten in the inherited class, e.g. {@link org.opencms.workplace.galleries.CmsAjaxImageGallery#init()}.<p>
     */
    public void init() {

        // default gallery does not require initialization
    }

    /**
     * Returns if the dialog mode is the "editor" mode.<p>
     *
     * @return <code>true</code> if the dialog mode is the "editor" mode, otherwise <code>false</code>
     */
    public boolean isModeEditor() {

        return MODE_EDITOR.equals(getParamDialogMode());
    }

    /**
     * Returns if the dialog mode is the "view" mode.<p>
     *
     * @return <code>true</code> if the dialog mode is the "view" mode, otherwise <code>false</code>
     */
    public boolean isModeView() {

        return MODE_VIEW.equals(getParamDialogMode());
    }

    /**
     * Returns if the dialog mode is the "widget" mode.<p>
     *
     * @return <code>true</code> if the dialog mode is the "editor" mode, otherwise <code>false</code>
     */
    public boolean isModeWidget() {

        return MODE_WIDGET.equals(getParamDialogMode());
    }

    /**
     * Sets the current mode of the dialog.<p>
     *
     * This is necessary to distinguish between widget mode and editor mode.<p>
     *
     * @param dialogMode the current mode of the dialog
     */
    public void setParamDialogMode(String dialogMode) {

        m_paramDialogMode = dialogMode;
    }

    /**
     * Sets the input field ID if in widget mode.<p>
     *
     * @param fieldId the input field ID
     */
    public void setParamFieldId(String fieldId) {

        m_paramFieldId = fieldId;
    }

    /**
     * Sets the path of the gallery to display.<p>
     *
     * @param galleryPath the path of the gallery to display
     */
    public void setParamGalleryPath(String galleryPath) {

        m_paramGalleryPath = galleryPath;
    }

    /**
     * Sets the list mode for getting the items, either {@link #LISTMODE_CATEGORY} or {@link #LISTMODE_GALLERY}.<p>
     *
     * @param paramListMode the list mode for getting the items
     */
    public void setParamListMode(String paramListMode) {

        m_paramListMode = paramListMode;
    }

    /**
     * Sets the property value parameter.<p>
     *
     * @param paramPropertyValue the property value parameter to set
     */
    public void setParamPropertyValue(String paramPropertyValue) {

        m_paramPropertyValue = paramPropertyValue;
    }

    /**
     * Sets the extended folder resource type this gallery is based on.<p>
     *
     * @param type the extended folder resource type this gallery is based on
     */
    public void setResourceType(CmsResourceTypeFolderExtended type) {

        m_resourceType = type;
    }

    /**
     * Builds the Javascript to set the currently active item object.<p>
     *
     * @param itemUrl the URL of the currently selected item
     */
    protected void buildJsonActiveItem(String itemUrl) {

        if (itemUrl.startsWith(OpenCms.getSiteManager().getWorkplaceServer())) {
            // remove workplace server prefix
            itemUrl = itemUrl.substring(OpenCms.getSiteManager().getWorkplaceServer().length());
        }
        // remove context prefix to read resource from VFS
        itemUrl = CmsLinkManager.removeOpenCmsContext(itemUrl);
        try {
            JspWriter out = getJsp().getJspContext().getOut();
            if (getCms().existsResource(itemUrl)) {
                try {
                    out.print(buildJsonItemObject(getCms().readResource(itemUrl)));
                } catch (CmsException e) {
                    // can not happen in theory, because we used existsResource() before...
                }
            } else {
                out.print(RETURNVALUE_NONE);
            }
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Builds the JSON code for the category list as JSON array.<p>
     */
    protected void buildJsonCategoryList() {

        CmsCategoryService catService = CmsCategoryService.getInstance();
        List<CmsCategory> foundCategories = Collections.emptyList();
        String editedResource = null;
        if (CmsStringUtil.isNotEmpty(getParamResource())) {
            editedResource = getParamResource();
        }
        try {
            foundCategories = catService.readCategories(getCms(), "", true, editedResource);
        } catch (CmsException e) {
            // error reading categories
        }

        // the next lines sort the categories according to their path
        Map<String, CmsCategory> sorted = new TreeMap<String, CmsCategory>();

        Iterator<CmsCategory> i = foundCategories.iterator();
        while (i.hasNext()) {
            CmsCategory category = i.next();
            String categoryPath = category.getPath();
            if (sorted.get(categoryPath) != null) {
                continue;
            }
            sorted.put(categoryPath, category);
        }

        foundCategories = new ArrayList<CmsCategory>(sorted.values());
        JSONArray categories = new JSONArray();
        i = foundCategories.iterator();
        while (i.hasNext()) {
            CmsCategory cat = i.next();

            JSONObject jsonObj = new JSONObject();
            try {
                // 1: category title
                jsonObj.put("title", cat.getTitle());
                // 2: category path
                jsonObj.put("path", cat.getPath());
                // 3: category root path
                jsonObj.put("rootpath", cat.getRootPath());
                // 4 category level
                jsonObj.put("level", CmsResource.getPathLevel(cat.getPath()));
                // 4: active flag
                jsonObj.put("active", false);
                categories.put(jsonObj);
            } catch (JSONException e) {
                // TODO: error handling
            }
        }
        JspWriter out = getJsp().getJspContext().getOut();
        try {
            out.print(categories.toString());
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }

    }

    /**
     * Creates a JSON object with the information found on the given gallery URL.<p>
     *
     * @param galleryUrl the given gallery URL
     */
    protected void buildJsonGalleryItem(String galleryUrl) {

        try {
            JspWriter out = getJsp().getJspContext().getOut();
            if (getCms().existsResource(galleryUrl)) {
                JSONObject jsonObj = new JSONObject();
                try {
                    CmsResource res = getCms().readResource(galleryUrl);
                    String path = getCms().getSitePath(res);
                    // read the gallery title
                    String title = getCms().readPropertyObject(
                        res,
                        CmsPropertyDefinition.PROPERTY_TITLE,
                        false).getValue("");
                    try {
                        // 1: gallery title
                        jsonObj.put("title", title);
                        // 2: gallery path
                        jsonObj.put("path", path);
                        // 3: active flag
                        jsonObj.put("active", true);
                        out.print(jsonObj);
                    } catch (JSONException e) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error(e.getLocalizedMessage(), e);
                        }
                    }
                } catch (CmsException e) {
                    // error reading title property
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
            } else {
                out.print(RETURNVALUE_NONE);
            }
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Builds the JSON code for the gallery list as JSON array.<p>
     */
    protected void buildJsonGalleryList() {

        String lastUsed = getSettings().getLastUsedGallery("" + getGalleryTypeId());
        // check the value of last Used, if gallery is opened for the first time
        if (CmsStringUtil.isEmpty(lastUsed)) {
            // start gallery settings for this gallery type for the current user
            String startGallerySetting = getSettings().getUserSettings().getStartGallery(
                getGalleryTypeName(),
                getCms());
            if (startGallerySetting != null) {
                // handle the case, "global settings" are selected
                if (startGallerySetting.equals(CmsWorkplace.INPUT_DEFAULT)) {
                    // get selected value from workplace xml settings
                    String preselectedValue = OpenCms.getWorkplaceManager().getDefaultUserSettings().getStartGallery(
                        getGalleryTypeName());
                    if (preselectedValue != null) {
                        startGallerySetting = preselectedValue;
                    }
                }
                // checks if the resource exists
                String sitePath = getCms().getRequestContext().removeSiteRoot(startGallerySetting);
                if (getCms().existsResource(sitePath)) {
                    lastUsed = sitePath;
                }
            }
        }
        JSONArray galleries = new JSONArray();
        Iterator<CmsResource> i = getGalleries().iterator();
        boolean isFirst = true;
        while (i.hasNext()) {
            CmsResource res = i.next();
            String path = getCms().getSitePath(res);
            JSONObject jsonObj = new JSONObject();
            // 1: gallery title
            String title = "";
            try {
                // read the gallery title
                title = getCms().readPropertyObject(path, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue("");
            } catch (CmsException e) {
                // error reading title property
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            try {
                jsonObj.put("title", title);
                // 2: gallery path
                jsonObj.put("path", path);
                // 3: active flag
                boolean active = false;
                if ((CmsStringUtil.isEmpty(lastUsed) && isFirst) || path.equals(lastUsed)) {
                    // TODO: adjust logic to get active gallery
                    active = true;
                }
                jsonObj.put("active", active);
                galleries.put(jsonObj);
            } catch (JSONException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            isFirst = false;
        }
        JspWriter out = getJsp().getJspContext().getOut();
        try {
            out.print(galleries.toString());
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Fills the JSON object with the information used for all resource types.<p>
     *
     * <ul>
     * <li><code>sitepath</code>: site path to the resource.</li>
     * <li><code>linkpath</code>: substituted url of the resource.</li>
     * <li><code>title</code>: title property of the resource.</li>
     * <li><code>size</code>: size of the resource in kb.</li>
     * <li><code>datecreated</code>: the creation date of the resource.</li>
     * <li><code>datelastmodified</code>: the modification date.</li>
     * <li><code>state</code>: the state of the resource, new or changed.</li>
     * <li><code>lockedby</code>: indicates if the resource is locked by another user.</li>
     * <li><code>editable</code>: editable flag to determine if item is editable and can be lockes by the user.</li>
     * <li><code>writepermission</code>: flag to indicate if the user has write permissions for given resource.</li>
     * <li><code>directpublish</code>: flag to indicate if the user has write direct publish permission for given resource.</li>
     * <li><code>description</code>: description property of the resource.</li>
     * </ul>
     *
     * @param jsonObj containing information used by all possible resource
     * @param res the resource to create the object from
     * @param sitePath site path to the object
     */
    protected void buildJsonItemCommonPart(JSONObject jsonObj, CmsResource res, String sitePath) {

        try {
            // 1: file item site path
            jsonObj.put("sitepath", sitePath);
            // 2: substituted file item url
            jsonObj.put("linkpath", getJsp().link(sitePath));
            // 3: file item title
            jsonObj.put(
                "title",
                CmsStringUtil.escapeJavaScript(
                    getJsp().property(CmsPropertyDefinition.PROPERTY_TITLE, sitePath, res.getName())));
            // 4: file size (in kb)
            jsonObj.put(
                "size",
                (res.getLength() / 1024) + " " + key(org.opencms.workplace.galleries.Messages.GUI_LABEL_KILOBYTES_0));
            // 5: file creation date (formatted)
            jsonObj.put("datecreated", getMessages().getDateTime(res.getDateCreated()));
            // 6: file modification date (formatted)
            jsonObj.put("datelastmodified", getMessages().getDateTime(res.getDateLastModified()));
            // 7: file state, if the item is new or changed
            CmsResourceState state = res.getState();
            CmsLock lock = CmsLock.getNullLock();
            try {
                // obtain current lock state to determine correct resource state and editable flag
                lock = getCms().getLock(res);
            } catch (CmsException e) {
                // ignore, lock state could not be determined
            }
            if (!lock.isNullLock() && lock.getType().isPublish()) {
                state = CmsResourceState.STATE_UNCHANGED;
            }
            jsonObj.put("state", state);
            // 8: determine if the item is locked by another user
            String locked = "";
            if (!lock.isNullLock()
                && !lock.getType().isPublish()
                && !lock.getUserId().equals(getCms().getRequestContext().getCurrentUser().getId())) {
                try {
                    locked = getCms().readUser(lock.getUserId()).getName();
                } catch (CmsException e) {
                    // failed to read user, use ID as user name
                    locked = lock.getUserId().toString();
                }
            }
            jsonObj.put("lockedby", locked);
            // 9: item editable flag to determine if item is editable (offline project and can be locked by the current user)
            boolean editable = false;
            // 10: item write permissions to determine if the user has write permission
            boolean writePermission = false;
            // 10: item direct publish flag to determine if the user has direct publish permission
            boolean directPublishPermission = false;
            try {
                // test if the resource is in the offline project and it can be locked by the user
                if (!getCms().getRequestContext().getCurrentProject().isOnlineProject()
                    && lock.isLockableBy(getCms().getRequestContext().getCurrentUser())) {
                    editable = true;
                    // test if the user has the write permission
                    if (getCms().hasPermissions(res, CmsPermissionSet.ACCESS_WRITE, false, CmsResourceFilter.ALL)) {
                        writePermission = true;
                    }
                    // test if the user has direct publish permission
                    if (getCms().hasPermissions(
                        res,
                        CmsPermissionSet.ACCESS_DIRECT_PUBLISH,
                        false,
                        CmsResourceFilter.ALL)) {
                        directPublishPermission = true;
                    }

                }
            } catch (CmsException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            jsonObj.put("editable", editable);
            jsonObj.put("writepermission", writePermission);
            jsonObj.put("directpublish", directPublishPermission);
            // 11: item description
            String desc = getJsp().property(CmsPropertyDefinition.PROPERTY_DESCRIPTION, sitePath, "");
            jsonObj.put("description", CmsStringUtil.escapeJavaScript(desc));
        } catch (JSONException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Returns a JSON object containing information of the given resource for usage in the gallery.<p>
     *
     * The content of the JSON object consists of a common and a specific part of the given resource.<p>
     *
     * @param res the resource to create the object from
     * @return the JSON object containing information from the given resource
     */
    protected JSONObject buildJsonItemObject(CmsResource res) {

        // create a new JSON object
        JSONObject jsonObj = new JSONObject();
        String sitePath = getCms().getRequestContext().getSitePath(res);
        // fill JSON object with common information
        buildJsonItemCommonPart(jsonObj, res, sitePath);
        // fill JSON object with specific information
        buildJsonItemSpecificPart(jsonObj, res, sitePath);

        return jsonObj;
    }

    /**
     * Fills the JSON object with the specific information used for this resource type.<p>
     *
     * @param jsonObj containing information used by all possible resource
     * @param res the resource to create the object from
     * @param sitePath site path to the object
     */
    protected abstract void buildJsonItemSpecificPart(JSONObject jsonObj, CmsResource res, String sitePath);

    /**
     * Builds the JSON code to create items for the folder.<p>
     *
     * @param resourceitems the file resource to build the displayed items
     * @param parentFolder the parent folder of the collected files (for a gallery)
     */
    protected void buildJsonResourceItems(List<CmsResource> resourceitems, String parentFolder) {

        if (resourceitems == null) {
            resourceitems = new ArrayList<CmsResource>();
        }

        boolean isPublishEnabled = false;
        boolean hasDirectPublish = false;
        boolean hasWritePermission = false;
        if (CmsStringUtil.isNotEmpty(parentFolder)) {
            // check if there are changes in the currently selected gallery and the user has direct edit permissions
            try {
                if ((OpenCms.getPublishManager().getPublishList(
                    getCms(),
                    getCms().readResource(parentFolder),
                    false).size() > 0)) {
                    isPublishEnabled = true;
                }
            } catch (CmsException e) {
                // ignore, gallery can not be published
            }
            // check if the user has direst publish permissions,
            // used to enable the gallerypublish button, if user has enough permissions
            try {
                if (getCms().hasPermissions(
                    getCms().readResource(parentFolder),
                    CmsPermissionSet.ACCESS_DIRECT_PUBLISH,
                    false,
                    CmsResourceFilter.ALL)) {
                    hasDirectPublish = true;
                }
            } catch (CmsException e) {
                // ignore, no publish permissions for gallery
            }
            try {
                // check if the user has write permissions,
                // used to display the upload buttons
                if (getCms().hasPermissions(
                    getCms().readResource(parentFolder),
                    CmsPermissionSet.ACCESS_WRITE,
                    false,
                    CmsResourceFilter.ALL)) {
                    hasWritePermission = true;
                }
            } catch (CmsException e) {
                // ignore, no write permissions for gallery
            }
        }
        JSONObject publishInfo = new JSONObject();
        try {
            publishInfo.put("publishable", isPublishEnabled);
            publishInfo.put("directpublish", hasDirectPublish);
            publishInfo.put("writepermission", hasWritePermission);
        } catch (JSONException e) {
            // ignore
        }
        JSONArray items = new JSONArray();
        items.put(publishInfo);
        Iterator<CmsResource> i = resourceitems.iterator();
        while (i.hasNext()) {
            CmsResource res = i.next();
            // build a JSON object from the item and add it to the list
            items.put(buildJsonItemObject(res));
        }
        JspWriter out = getJsp().getJspContext().getOut();
        try {
            // print the JSON array
            out.print(items.toString());
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Changes the content of the CmsResource.<p>
     * This function should be overwritten in {@link org.opencms.workplace.galleries.CmsAjaxLinkGallery}.<p>
     *
     * @param itemUrl the item URL
     *
     */
    protected void changeItemLinkUrl(String itemUrl) {

        // the most galleries do not provide this method

    }

    /**
     * Changes the title property value of the given item.<p>
     *
     * @param itemUrl the item URL on which the title is changed
     */
    protected void changeItemTitle(String itemUrl) {

        try {
            JspWriter out = getJsp().getJspContext().getOut();
            if (getCms().existsResource(itemUrl)) {
                try {
                    writeTitleProperty(getCms().readResource(itemUrl));
                    out.print(buildJsonItemObject(getCms().readResource(itemUrl)));
                } catch (CmsException e) {
                    // can not happen in theory, because we used existsResource() before...
                }
            } else {
                out.print(RETURNVALUE_NONE);
            }
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Returns the resource items for the selected category.<p>
     *
     * @return the resource items for the selected category
     */
    protected List<CmsResource> getCategoryItems() {

        List<CmsResource> result = Collections.emptyList();
        if (CmsStringUtil.isNotEmpty(getParamGalleryPath())) {
            try {
                CmsCategoryService service = CmsCategoryService.getInstance();
                // get the edited resource if present
                String editedResource = "/";
                if (CmsStringUtil.isNotEmpty(getParamResource())) {
                    editedResource = CmsResource.getFolderPath(getParamResource());
                }
                // read the matching resources for the category
                result = service.readCategoryResources(getCms(), getParamGalleryPath(), true, editedResource);
                // filter the matched resources to get only the specific items as result
                int resTypeId = getGalleryItemsTypeId();
                if (resTypeId != -1) {
                    List<CmsResource> unfiltered = new ArrayList<CmsResource>(result);
                    result = new ArrayList<CmsResource>(unfiltered.size());
                    Iterator<CmsResource> i = unfiltered.iterator();
                    while (i.hasNext()) {
                        CmsResource res = i.next();
                        if (res.getTypeId() == resTypeId) {
                            result.add(res);
                        }
                    }
                }
            } catch (CmsException e) {
                // error reading resources
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            } catch (NullPointerException e) {
                // ignore this exception
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        return result;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);
        // set the dialog type
        setParamDialogtype(LISTMODE_GALLERY);
        if (CmsStringUtil.isEmpty(getParamGalleryPath())) {
            String lastUsedGallery = getSettings().getLastUsedGallery("" + getGalleryTypeId());
            if (CmsStringUtil.isNotEmpty(lastUsedGallery)) {
                // set the resourcepath of the last used gallery if the resource is not deleted
                try {
                    getCms().readResource(lastUsedGallery, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
                    setParamGalleryPath(lastUsedGallery);
                } catch (CmsException e) {
                    // reading the last used gallery failed, may be deleted
                }
            }
        }
    }

    /**
     * Changes the value of the property title for the specified resource.<p>
     *
     * @param res the resource to change the property value
     */
    protected void writeTitleProperty(CmsResource res) {

        String resPath = getCms().getSitePath(res);
        String currentPropertyValue = getParamPropertyValue();
        try {
            CmsProperty currentProperty = getCms().readPropertyObject(
                resPath,
                CmsPropertyDefinition.PROPERTY_TITLE,
                false);
            // detect if property is a null property or not
            if (currentProperty.isNullProperty()) {
                // create new property object and set key and value
                currentProperty = new CmsProperty();
                currentProperty.setName(CmsPropertyDefinition.PROPERTY_TITLE);
                if (OpenCms.getWorkplaceManager().isDefaultPropertiesOnStructure()) {
                    // set structure value
                    currentProperty.setStructureValue(currentPropertyValue);
                    currentProperty.setResourceValue(null);
                } else {
                    // set resource value
                    currentProperty.setStructureValue(null);
                    currentProperty.setResourceValue(currentPropertyValue);
                }
            } else if (currentProperty.getStructureValue() != null) {
                // structure value has to be updated
                currentProperty.setStructureValue(currentPropertyValue);
                currentProperty.setResourceValue(null);
            } else {
                // resource value has to be updated
                currentProperty.setStructureValue(null);
                currentProperty.setResourceValue(currentPropertyValue);
            }
            CmsLock lock = getCms().getLock(res);
            if (lock.isUnlocked()) {
                // lock resource before operation
                getCms().lockResource(resPath);
            }
            // write the property to the resource
            getCms().writePropertyObject(resPath, currentProperty);
            // unlock the resource
            getCms().unlockResource(resPath);
        } catch (CmsException e) {
            // writing the property failed, log error
            LOG.error(e.getLocalizedMessage(), e);
        }
    }
}