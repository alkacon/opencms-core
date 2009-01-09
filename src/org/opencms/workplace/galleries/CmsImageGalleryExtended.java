/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/galleries/Attic/CmsImageGalleryExtended.java,v $
 * Date   : $Date: 2009/01/09 15:59:32 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2008 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.loader.CmsImageScaler;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsCategory;
import org.opencms.relations.CmsCategoryService;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
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
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Provides helper methods to generate the special image gallery dialog used in the XML content editors,
 * WYSIWYG editors and context menu.<p>
 * 
 * It is also used for AJAX requests to dynamically switch galleries or categories and get additional information
 * for the currently active image of the dialog.<p>
 * 
 * @author Andreas Zahner
 * 
 * @since 7.0.6
 */
public class CmsImageGalleryExtended extends CmsDialog {

    /** Request parameter value for the action: change the image title property value. */
    public static final String DIALOG_CHANGEIMAGETITLE = "changeimagetitle";

    /** Request parameter value for the action: get the currently active image object. */
    public static final String DIALOG_GETACTIVEIMAGE = "getactiveimage";

    /** Request parameter value for the action: get the category selection list. */
    public static final String DIALOG_GETCATEGORIES = "getcategories";

    /** Request parameter value for the action: get the gallery selection list. */
    public static final String DIALOG_GETGALLERIES = "getgalleries";

    /** Request parameter value for the action: get a specific gallery. */
    public static final String DIALOG_GETGALLERY = "getgallery";

    /** Request parameter value for the action: get the images for a gallery or category. */
    public static final String DIALOG_GETIMAGES = "getimages";

    /** The list mode name "category" for getting the images. */
    public static final String LISTMODE_CATEGORY = "category";

    /** The list mode name "gallery" for getting the images. */
    public static final String LISTMODE_GALLERY = "gallery";

    /** Request parameter name for the image description. */
    public static final String PARAM_DESCRIPTION = "description";

    /** Request parameter name for the edited resource. */
    public static final String PARAM_EDITEDRESOURCE = "editedresource";

    /** Request parameter name for the input field id. */
    public static final String PARAM_FIELDID = "fieldid";

    /** Request parameter name for the format name. */
    public static final String PARAM_FORMATNAME = "formatname";

    /** Request parameter name for the format value. */
    public static final String PARAM_FORMATVALUE = "formatvalue";

    /** Request parameter name for the input field hash id. */
    public static final String PARAM_HASHID = "hashid";

    /** Request parameter name for the active image path. */
    public static final String PARAM_IMAGEPATH = "imagepath";

    /** Request parameter name for the image height. */
    public static final String PARAM_IMGHEIGHT = "imgheight";

    /** Request parameter name for the image width. */
    public static final String PARAM_IMGWIDTH = "imgwidth";

    /** Request parameter name for the dialog initialization parameters. */
    public static final String PARAM_PARAMS = "params";

    /** Request parameter name for the image scale parameters. */
    public static final String PARAM_SCALE = "scale";

    /** Request parameter name for the startup folder. */
    public static final String PARAM_STARTUPFOLDER = "startupfolder";

    /** Request parameter name for the startup type. */
    public static final String PARAM_STARTUPTYPE = "startuptype";

    /** Request parameter name for the use formats flag. */
    public static final String PARAM_USEFORMATS = "useformats";

    /** Property definition name for the Copyright property. */
    public static final String PROPERTY_COPYRIGHT = "Copyright";

    /** Value that is returned if no result was found. */
    public static final String RETURNVALUE_NONE = "none";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsImageGalleryExtended.class);

    /** The image gallery instance used to display image galleries. */
    private CmsImageGallery m_gallery;

    /** The list mode to get the images either from a gallery or by a category. */
    private String m_paramListMode;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsImageGalleryExtended(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsImageGalleryExtended(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Called from the JSP that is used for the AJAX requests to OpenCms.<p>
     */
    public void displayDialog() {

        if (DIALOG_CHANGEIMAGETITLE.equals(getParamAction())) {
            // get the available categories as JSON array
            changeImageTitle(getJsp().getRequest().getParameter(PARAM_IMAGEPATH));
        } else if (DIALOG_GETCATEGORIES.equals(getParamAction())) {
            // get the available categories as JSON array
            buildJsonCategoryList();
        } else if (DIALOG_GETGALLERIES.equals(getParamAction())) {
            // get the available galleries as JSON array
            buildJsonGalleryList();
        } else if (DIALOG_GETGALLERY.equals(getParamAction())) {
            // get the desired gallery as JSON object
            buildJsonGalleryItem(getJsp().getRequest().getParameter(A_CmsGallery.PARAM_GALLERYPATH));
        } else if (DIALOG_GETIMAGES.equals(getParamAction())) {
            if (LISTMODE_CATEGORY.equals(getParamListMode())) {
                // get the items of selected category
                buildJsonImageItems(getCategoryItems(), null);
            } else {
                // get the items of a selected gallery
                buildJsonImageItems(getImageGallery().getGalleryItems(), getImageGallery().getParamGalleryPath());
            }
        } else if (DIALOG_GETACTIVEIMAGE.equals(getParamAction())) {
            // build the JSON object for the currently active image
            buildJsonActiveImage(getJsp().getRequest().getParameter(PARAM_IMAGEPATH));
        }
    }

    /**
     * Returns the image gallery instance used to display image galleries.<p>
     * 
     * @return the image gallery instance used to display image galleries
     */
    public CmsImageGallery getImageGallery() {

        if (m_gallery == null) {
            // initialize the image gallery instance
            A_CmsGallery aGallery = A_CmsGallery.createInstance("imagegallery", getJsp());
            m_gallery = (CmsImageGallery)aGallery;
        }
        return m_gallery;
    }

    /**
     * Returns the current dialog mode, either "editor", "view" or "widget".<p>
     * 
     * @return the current dialog mode
     */
    public String getMode() {

        return getImageGallery().getParamDialogMode();
    }

    /**
     * Returns the list mode for getting the images, either {@link #LISTMODE_CATEGORY} or {@link #LISTMODE_GALLERY}.<p>
     * 
     * @return the list mode for getting the images
     */
    public String getParamListMode() {

        return m_paramListMode;
    }

    /**
     * Returns if the dialog mode is the "editor" mode.<p>
     * 
     * @return <code>true</code> if the dialog mode is the "editor" mode, otherwise <code>false</code>
     */
    public boolean isModeEditor() {

        return A_CmsGallery.MODE_EDITOR.equals(getMode());
    }

    /**
     * Returns if the dialog mode is the "view" mode.<p>
     * 
     * @return <code>true</code> if the dialog mode is the "view" mode, otherwise <code>false</code>
     */
    public boolean isModeView() {

        return A_CmsGallery.MODE_VIEW.equals(getMode());
    }

    /**
     * Returns if the dialog mode is the "widget" mode.<p>
     * 
     * @return <code>true</code> if the dialog mode is the "widget" mode, otherwise <code>false</code>
     */
    public boolean isModeWidget() {

        return A_CmsGallery.MODE_WIDGET.equals(getMode());
    }

    /**
     * Sets the list mode for getting the images, either {@link #LISTMODE_CATEGORY} or {@link #LISTMODE_GALLERY}.<p>
     * 
     * @param paramListMode the list mode for getting the images
     */
    public void setParamListMode(String paramListMode) {

        m_paramListMode = paramListMode;
    }

    /**
     * Builds the Javascript to set the currently active image object.<p>
     * 
     * @param imgUrl the URL of the currently selected image
     */
    protected void buildJsonActiveImage(String imgUrl) {

        if (imgUrl.startsWith(OpenCms.getSiteManager().getWorkplaceServer())) {
            // remove workplace server prefix
            imgUrl = imgUrl.substring(OpenCms.getSiteManager().getWorkplaceServer().length());
        }
        if (imgUrl.startsWith(OpenCms.getSystemInfo().getOpenCmsContext())) {
            // remove context prefix to read resource from VFS
            imgUrl = imgUrl.substring(OpenCms.getSystemInfo().getOpenCmsContext().length());
        }
        try {
            JspWriter out = getJsp().getJspContext().getOut();
            if (getCms().existsResource(imgUrl)) {
                try {
                    out.print(buildJsonImageObject(getCms().readResource(imgUrl)));
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
     * Builds the JSON code for the image category list as JSON array.<p>
     */
    protected void buildJsonCategoryList() {

        List categories = new ArrayList();
        CmsCategoryService catService = CmsCategoryService.getInstance();
        List foundCategories = Collections.EMPTY_LIST;
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
        Map sorted = new TreeMap();

        Iterator i = foundCategories.iterator();
        while (i.hasNext()) {
            CmsCategory category = (CmsCategory)i.next();
            String categoryPath = category.getPath();
            if (sorted.get(categoryPath) != null) {
                continue;
            }
            sorted.put(categoryPath, category);
        }

        foundCategories = new ArrayList(sorted.values());

        i = foundCategories.iterator();
        while (i.hasNext()) {
            CmsCategory cat = (CmsCategory)i.next();

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
                categories.add(jsonObj);
            } catch (JSONException e) {
                // TODO: error handling
            }
        }
        JSONArray jsonArr = new JSONArray(categories);
        JspWriter out = getJsp().getJspContext().getOut();
        try {
            out.print(jsonArr.toString());
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
                    String title = getCms().readPropertyObject(res, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue(
                        "");
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
     * Builds the JSON code for the image gallery list as JSON array.<p>
     */
    protected void buildJsonGalleryList() {

        String lastUsed = getImageGallery().getSettings().getLastUsedGallery(getImageGallery().getGalleryTypeId());
        List galleries = new ArrayList();
        Iterator i = getImageGallery().getGalleries().iterator();
        boolean isFirst = true;
        while (i.hasNext()) {
            CmsResource res = (CmsResource)i.next();
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
                galleries.add(jsonObj);
            } catch (JSONException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            isFirst = false;
        }
        JSONArray jsonArr = new JSONArray(galleries);
        JspWriter out = getJsp().getJspContext().getOut();
        try {
            out.print(jsonArr.toString());
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Builds the JSON code to create image items.<p>
     * 
     * @param images the image resource to build the displayed items
     * @param imgParentFolder the parent folder of the collected images (for a gallery)
     */
    protected void buildJsonImageItems(List images, String imgParentFolder) {

        if (images == null) {
            images = new ArrayList();
        }
        List items = new ArrayList(images.size() + 1);
        boolean isPublisEnabled = false;
        if (CmsStringUtil.isNotEmpty(imgParentFolder)) {
            // check if there are changes in the currently selected gallery
            try {
                if (OpenCms.getPublishManager().getPublishList(getCms(), getCms().readResource(imgParentFolder), false).size() > 0) {
                    isPublisEnabled = true;
                }
            } catch (Exception e) {
                // ignore, gallery can not be published
            }
        }
        JSONObject publishInfo = new JSONObject();
        try {
            publishInfo.put("publishable", isPublisEnabled);
        } catch (JSONException e) {
            // ignore
        }
        items.add(publishInfo);
        Iterator i = images.iterator();
        while (i.hasNext()) {
            CmsResource res = (CmsResource)i.next();
            // build a JSON object from the image and add it to the list
            items.add(buildJsonImageObject(res));
        }
        // create a JSON array containing all image objects
        JSONArray jsonArr = new JSONArray(items);
        JspWriter out = getJsp().getJspContext().getOut();
        try {
            // print the JSON array
            out.print(jsonArr.toString());
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Changes the title property value of the given image.<p>
     * 
     * @param imgUrl the image URL on which the title is changed
     */
    protected void changeImageTitle(String imgUrl) {

        try {
            JspWriter out = getJsp().getJspContext().getOut();
            if (getCms().existsResource(imgUrl)) {
                try {
                    getImageGallery().writeTitleProperty(getCms().readResource(imgUrl));
                    out.print(buildJsonImageObject(getCms().readResource(imgUrl)));
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
    protected List getCategoryItems() {

        List result = Collections.EMPTY_LIST;
        if (CmsStringUtil.isNotEmpty(getImageGallery().getParamGalleryPath())) {
            try {
                CmsCategoryService service = CmsCategoryService.getInstance();
                // get the edited resource if present
                String editedResource = "/";
                if (CmsStringUtil.isNotEmpty(getParamResource())) {
                    editedResource = CmsResource.getFolderPath(getParamResource());
                }
                // read the matching resources for the category
                result = service.readCategoryResources(
                    getCms(),
                    getImageGallery().getParamGalleryPath(),
                    true,
                    editedResource);
                // filter the matched resources to get only images as result
                int resTypeId = getImageGallery().getGalleryItemsTypeId();
                if (resTypeId != -1) {
                    List unfiltered = new ArrayList(result);
                    result = new ArrayList(unfiltered.size());
                    Iterator i = unfiltered.iterator();
                    while (i.hasNext()) {
                        CmsResource res = (CmsResource)i.next();
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
            }
        }
        return result;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);
    }

    /**
     * Returns a JSON object containing image information from the given resource for usage in the gallery.<p>
     * 
     * @param res the resource to create the image object from
     * @return the JSON object containing image information from the given resource
     */
    private JSONObject buildJsonImageObject(CmsResource res) {

        // create a new JSON object
        JSONObject jsonObj = new JSONObject();
        String sitePath = getCms().getRequestContext().getSitePath(res);
        OpenCms.getSystemInfo().getOpenCmsContext();
        // get the scaler for the image resource
        CmsImageScaler scaler = new CmsImageScaler(getCms(), res);
        try {
            // 1: image site path
            jsonObj.put("sitepath", sitePath);
            // 2: substituted image url
            jsonObj.put("linkpath", getJsp().link(sitePath));
            // 3: substituted image link including scale parameters for item list display
            String scaleParams = "";
            // if scaling is disabled, the scale parameters might be null!
            if (getImageGallery().getDefaultScaleParams() != null) {
                scaleParams = getImageGallery().getDefaultScaleParams().toRequestParam();
            }
            jsonObj.put("scalepath", getJsp().link(sitePath + scaleParams));
            // 4: image title
            jsonObj.put("title", CmsStringUtil.escapeJavaScript(getJsp().property(
                CmsPropertyDefinition.PROPERTY_TITLE,
                sitePath,
                res.getName())));
            // 5: image width
            if (scaler.isValid()) {
                jsonObj.put("width", scaler.getWidth());
            } else {
                jsonObj.put("width", -1);
            }
            // 6: image height
            if (scaler.isValid()) {
                jsonObj.put("height", scaler.getHeight());
            } else {
                jsonObj.put("height", -1);
            }
            // 7: image size (in kb)
            jsonObj.put("size", (res.getLength() / 1024)
                + " "
                + key(org.opencms.workplace.galleries.Messages.GUI_LABEL_KILOBYTES_0));
            // 8: image creation date (formatted)
            jsonObj.put("datecreated", getMessages().getDateTime(res.getDateCreated()));
            // 9: image modification date (formatted)
            jsonObj.put("datelastmodified", getMessages().getDateTime(res.getDateLastModified()));
            // 10: image ID
            jsonObj.put("id", res.getStructureId());
            // 11: image type (gif, jpg, etc.)
            String type = "";
            int dotIndex = res.getName().lastIndexOf('.');
            if (dotIndex != -1) {
                type = res.getName().substring(dotIndex + 1).toLowerCase();
            }
            jsonObj.put("type", type);
            // 12: image state, if the image is new or changed
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
            // 13: determine if the image is locked by another user
            String locked = "";
            if (!lock.isNullLock() && !lock.getType().isPublish() && !lock.getUserId().equals(getCms().getRequestContext().currentUser().getId())) {
                try {
                    locked = getCms().readUser(lock.getUserId()).getName();
                } catch (CmsException e) {
                    // failed to read user, use ID as user name
                    locked = lock.getUserId().toString();
                }
            }
            jsonObj.put("lockedby", locked);
            // 14: image editable flag to determine if image is editable and can be published
            boolean editable = false;
            if (!getCms().getRequestContext().currentProject().isOnlineProject()
                    && lock.isLockableBy(getCms().getRequestContext().currentUser())) {
                editable = true;
            }
            jsonObj.put("editable", editable);
            // 15: image structure id hash code
            jsonObj.put("hash", res.getStructureId().hashCode());
            // 16: image copyright
            String copyright = getJsp().property(PROPERTY_COPYRIGHT, sitePath, "");
            jsonObj.put("copyright", CmsStringUtil.escapeJavaScript(copyright));
            // 17: image description
            String desc = getJsp().property(CmsPropertyDefinition.PROPERTY_DESCRIPTION, sitePath, "");
            jsonObj.put("description", CmsStringUtil.escapeJavaScript(desc));
        } catch (JSONException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return jsonObj;
    }

}
