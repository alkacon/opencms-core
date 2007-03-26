/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/galleries/Attic/CmsLinkGallery.java,v $
 * Date   : $Date: 2007/03/26 09:12:03 $
 * Version: $Revision: 1.22.4.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypePointer;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.explorer.CmsNewResource;
import org.opencms.workplace.explorer.CmsNewResourceUpload;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Generates the links gallery popup window which can be used in editors or as a dialog widget.<p>
 * 
 * The following files use this class:
 * <ul>
 * <li>/commons/galeries/download_fs.jsp
 * </ul>
 * <p>
 * 
 * @author Armen Markarian 
 * 
 * @version $Revision: 1.22.4.7 $ 
 * 
 * @since 6.0.0 
 */
public class CmsLinkGallery extends A_CmsGallery {

    /**
     *  Request parameter name needed for the action: edit property value.<p>
     *  
     *  The value of this request parameter will denote the property to edit on the pointer resource.<p>
     */
    public static final String PARAM_EDITPROPERTY = A_CmsGallery.DIALOG_EDITPROPERTY;

    /**
     *  Request parameter name needed for the action: edit property value.<p>
     *  
     *  The value of this request parameter will contain the value for the property to edit which 
     *  is denoted by <code>{@link #PARAM_EDITPROPERTY}</code>.<p>
     */
    public static final String PARAM_EDITPROPERTY_VALUE = A_CmsGallery.DIALOG_EDITPROPERTY + "value";

    /** URI of the image gallery popup dialog. */
    public static final String URI_GALLERY = PATH_GALLERIES + "link_fs.jsp";

    /** Request parameter value for parameter  <code>{@link #PARAM_EDITPROPERTY}</code> for editing the link of a pointer. */
    public static final String VALUE_EDITPROPERTY_LINK = "editpropertylink";

    /** Request parameter value for parameter  <code>{@link #PARAM_EDITPROPERTY}</code> for editing the title of a pointer. */
    public static final String VALUE_EDITPROPERTY_TITLE = "editpropertytitle";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLinkGallery.class);

    /** The order value of the gallery for sorting the galleries. */
    private static final Integer ORDER_GALLERY = new Integer(30);

    /** The property to edit on the previewed resource. */
    private String m_paramEditProperty;

    /** The property value to edit for the property <code>{@link #m_paramEditProperty}</code> on the previewed resource. */
    private String m_paramEditPropertyValue;

    /**
     * Public empty constructor, required for {@link A_CmsGallery#createInstance(String, CmsJspActionElement)}.<p>
     */
    public CmsLinkGallery() {

        // noop
    }

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsLinkGallery(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsLinkGallery(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.galleries.A_CmsGallery#applyButton()
     */
    public String applyButton() {

        if (MODE_VIEW.equals(getParamDialogMode())) {
            // in view mode, generate disabled button
            return button(null, null, "apply_in.png", Messages.GUI_BUTTON_PASTE_0, 0);
        } else {
            String uri = getParamResourcePath();
            if (MODE_WIDGET.equals(getParamDialogMode())) {
                // get real link target in widget mode from file content
                try {
                    CmsResource res = getCms().readResource(getParamResourcePath());
                    uri = new String(CmsFile.upgrade(res, getCms()).getContents());
                } catch (CmsException e) {
                    // this should never happen
                    LOG.error(e.getLocalizedMessage(), e);
                }
            } else {
                // in editor mode, create a valid link from resource path
                uri = getJsp().link(uri);
            }

            return button(
                "javascript:link('" + uri + "',document.form.title.value, document.form.title.value);",
                null,
                "apply.png",
                Messages.GUI_BUTTON_PASTE_0,
                0);
        }
    }

    /**
     * Builds the html String for the buttonbar frame.<p>
     * 
     * @return the html String for the buttonbar frame
     */
    public String buildGalleryButtonBar() {

        StringBuffer buttonBar = new StringBuffer();
        try {
            if (CmsStringUtil.isNotEmpty(getParamResourcePath())) {
                // we have a resource to display
                CmsResource res = getCms().readResource(getParamResourcePath());
                if (res != null) {
                    setCurrentResource(res);
                    String title = CmsEncoder.escapeXml(getPropertyValue(res, CmsPropertyDefinition.PROPERTY_TITLE));
                    buttonBar.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" ");
                    buttonBar.append("style=\"align: left; width:100%; background-color: ThreeDFace; margin: 0; border-right: 1px solid ThreeDShadow\">");
                    buttonBar.append("<tr align=\"left\">");
                    buttonBar.append(buttonBarStartTab(0, 0));
                    // apply button
                    buttonBar.append(applyButton());
                    // publish button
                    buttonBar.append(publishButton());
                    // delete button
                    buttonBar.append(deleteButton());
                    buttonBar.append(buttonBarSeparator(5, 5));
                    buttonBar.append("<td class=\"nowrap\"><b><nobr>");
                    buttonBar.append(title).append("</nobr></b>&nbsp;</td>\n");
                    buttonBar.append("<td class=\"maxwidth\">&nbsp;</td>\n");
                    // hidden field 
                    buttonBar.append("<input type=\"hidden\" name=\"");
                    buttonBar.append(PARAM_PROPERTYVALUE);
                    buttonBar.append("\" value=\"");
                    buttonBar.append(title);
                    buttonBar.append("\">\r\n");
                    // target select
                    buttonBar.append(targetSelectBox());
                    // preview button
                    buttonBar.append(previewButton());
                    buttonBar.append(buttonBar(HTML_END));
                }
            } else {
                // no resource to display, create empty row
                buttonBar.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td>");
                buttonBar.append("<img height=\"22\" border=\"0\" src=\"");
                buttonBar.append(getJsp().link(CmsWorkplace.VFS_PATH_RESOURCES + "tree/empty.gif"));
                buttonBar.append("\">");
                buttonBar.append("</td></tr></table>");
            }
        } catch (CmsException e) {
            // resource is deleted, display empty table
            buttonBar.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td>");
            buttonBar.append("<img height=\"22\" border=\"0\" src=\"");
            buttonBar.append(getJsp().link(CmsWorkplace.VFS_PATH_RESOURCES + "tree/empty.gif"));
            buttonBar.append("\">");
            buttonBar.append("</td></tr></table>");
        }
        return buttonBar.toString();
    }

    /**
     * Builds the html String for the preview frame.<p>
     * 
     * @return the html String for the preview frame
     */
    public String buildGalleryItemPreview() {

        StringBuffer html = new StringBuffer(32);
        try {
            if (CmsStringUtil.isNotEmpty(getParamResourcePath())) {
                CmsResource res = getCms().readResource(getParamResourcePath());
                if (res != null) {
                    setCurrentResource(res);
                    // overtake the values in case this is an edit submit
                    changePointer(res);

                    // build the html

                    // file target
                    String pointer = new String(CmsFile.upgrade(res, getCms()).getContents());
                    if (CmsStringUtil.isEmptyOrWhitespaceOnly(pointer)) {
                        pointer = getJsp().link(getCms().getSitePath(res));
                    }
                    String title = getPropertyValue(res, CmsPropertyDefinition.PROPERTY_TITLE);
                    String description = getJsp().property(
                        CmsPropertyDefinition.PROPERTY_DESCRIPTION,
                        getParamResourcePath());
                    String keywords = getJsp().property(CmsPropertyDefinition.PROPERTY_KEYWORDS, getParamResourcePath());
                    String lastmodified = getMessages().getDateTime(res.getDateLastModified());

                    html.append("<table cellpadding=\"2\" cellspacing=\"2\" border=\"0\" style=\"align: middle; width:100%; background-color: ThreeDFace; margin: 0;\">\n");

                    // Link href input field
                    String link = new StringBuffer(key(Messages.GUI_INPUT_LINKTO_0)).append(
                        " (<a href=\"#\" onclick=\"javascript:window.open('").append(
                        getJsp().link(getCms().getSitePath(res))).append("','_preview','')\">").append(
                        key(Messages.GUI_BUTTON_PREVIEW_0)).append("</a>)").toString();
                    html.append(previewRow(link, pointer, VALUE_EDITPROPERTY_LINK));
                    // file title
                    html.append(previewRow(key(Messages.GUI_INPUT_TITLE_0), title, VALUE_EDITPROPERTY_TITLE));
                    // file name
                    html.append(previewRow(key(Messages.GUI_LABEL_NAME_0), res.getName(), false));
                    // file last modified date
                    html.append(previewRow(key(Messages.GUI_INPUT_DATELASTMODIFIED_0), lastmodified, false));
                    // file description if existing
                    if (CmsStringUtil.isNotEmpty(description)) {
                        html.append(previewRow(key(Messages.GUI_INPUT_DESCRIPTION_0), description));
                    }
                    // file keywords if existing
                    if (CmsStringUtil.isNotEmpty(keywords)) {
                        html.append(previewRow(key(Messages.GUI_INPUT_KEYWORDS_0), keywords));
                    }

                    // hidden value change submit form, controlled by javascript 
                    html.append("<form name=\"pointerchange\" action=\"").append(getJsp().link("gallery_preview.jsp")).append(
                        "\" target=\"gallery_preview\" method=\"post\" class=\"nomargin\">\n");
                    html.append("\t<input type=\"hidden\" id=\"").append(PARAM_EDITPROPERTY).append("\" name=\"").append(
                        PARAM_EDITPROPERTY).append("\"/>\n");
                    html.append("\t<input type=\"hidden\" id=\"").append(PARAM_EDITPROPERTY_VALUE).append("\" name=\"").append(
                        PARAM_EDITPROPERTY_VALUE).append("\"/>\n");
                    html.append("\t<input type=\"hidden\" id=\"").append(PARAM_ACTION).append("\" name=\"").append(
                        PARAM_ACTION).append("\" value=\"").append(DIALOG_EDITPROPERTY).append("\"/>\n");
                    html.append("\t<input type=\"hidden\" id=\"").append(PARAM_RESOURCEPATH).append("\" name=\"").append(
                        PARAM_RESOURCEPATH).append("\"pointerPropertyValue\" value=\"").append(
                        this.getParamResourcePath()).append("\"/>\n");

                    html.append("</form>\n");

                }
            }
        } catch (CmsException e) {
            // reading the resource or property value failed
            LOG.error(e.getLocalizedMessage(), e);
        }
        return html.toString();
    }

    /**
     * Builds the javascript for submitting the property changes for the current pointer in the
     * preview frame.<p>
     * 
     * @return the javascript for submitting the property changes for the current pointer in the
     *      preview frame
     */
    public String dialogScriptSubmit() {

        if (useNewStyle()) {
            return super.dialogScriptSubmit();
        }
        StringBuffer result = new StringBuffer(512);
        result.append("function submitAction(submitFieldId) {\n");
        result.append("\tdocument.pointerchange.").append(PARAM_EDITPROPERTY).append(".value = submitFieldId;\n");
        result.append("\tdocument.pointerchange.").append(PARAM_EDITPROPERTY_VALUE).append(
            ".value = document.getElementById(submitFieldId).value;\n");
        //        result.append("top.setTimeOut('')")
        result.append("\tdocument.pointerchange.submit();\n");
        result.append("\ttop.setTimeout('top.gallery_fs.gallery_head.displayGallery()', 1000);\n");
        result.append("\ttop.setTimeout('top.preview_fs.gallery_buttonbar.location.reload()', 1000);\n");
        result.append("}\n");

        return result.toString();
    }

    /**
     * @see org.opencms.workplace.galleries.A_CmsGallery#getGalleryItemsTypeId()
     */
    public int getGalleryItemsTypeId() {

        return CmsResourceTypePointer.getStaticTypeId();
    }

    /**
     * @see org.opencms.workplace.galleries.A_CmsGallery#getHeadFrameSetHeight()
     */
    public String getHeadFrameSetHeight() {

        return "450";
    }

    /**
     * Returns the order of the implemented gallery, used to sort the gallery buttons in the editors.<p>
     * 
     * @return the order of the implemented gallery
     */
    public Integer getOrder() {

        return ORDER_GALLERY;
    }

    /**
     * Returns the property to edit on the previewed resource parameter value.<p>
     * 
     * @return the parameter value
     */
    public String getParamEditProperty() {

        return m_paramEditProperty;
    }

    /**
     * Returns the property value to edit on the previewed resource parameter value.<p>
     * 
     * @return the parameter value
     */
    public String getParamEditPropertyValue() {

        return m_paramEditPropertyValue;
    }

    /**
     * Returns the url for the new CmsResourceTypePointer dialog.<p>
     * 
     * @return the url for the wizard dialog
     */
    public String getWizardUrl() {

        StringBuffer wizardUrl = new StringBuffer(8);
        wizardUrl.append(getJsp().link(
            PATH_DIALOGS
                + OpenCms.getWorkplaceManager().getExplorerTypeSetting(CmsResourceTypePointer.getStaticTypeName()).getNewResourceUri()));
        wizardUrl.append("?action=newform&");
        wizardUrl.append(CmsNewResourceUpload.PARAM_REDIRECTURL);
        wizardUrl.append("=");
        wizardUrl.append(PATH_GALLERIES);
        wizardUrl.append("gallery_list.jsp&");
        wizardUrl.append(CmsNewResourceUpload.PARAM_TARGETFRAME);
        wizardUrl.append("=gallery_list&");
        wizardUrl.append(CmsNewResource.PARAM_CURRENTFOLDER);
        wizardUrl.append("=");

        return wizardUrl.toString();
    }

    /**
     * Generates a HTML table row with two columns that contain the name on the left side and an a text input on the right side.<p>
     * 
     * The first column includes the given display String, the second column includes an input field with the 
     * id attribute set to <code>column1</code> preset with <code>column2</code>.<p>
     * 
     * @param column1 the string value for the first column
     * 
     * @param column2 the string value for the second column 
     * 
     * @return a HTML table row with two columns
     */
    public String previewRow(String column1, String column2) {

        return previewRow(column1, column2, true);

    }

    /**
     * Generates a HTML table row with two columns that contain the name on the left side and an a text input or 
     * plain text (based upon the boolean argument flag) on the right side.<p>
     * 
     * The first column includes the given display String, the second column includes an input field with the 
     * id attribute set to <code>column1</code> preset with <code>column2</code>.<p>
     * 
     * @param column1 the string value for the first column
     * @param column2 the string value for the second column 
     * @param inputField if <code>true</code> the <tt>column1</tt> parameter is used as <tt>id</tt> attribute for the input field
     * 
     * @return a HTML table row with two columns
     */
    public String previewRow(String column1, String column2, boolean inputField) {

        String result;
        if (inputField) {
            result = previewRow(column1, column2, column1);
        } else {
            StringBuffer previewRow = new StringBuffer();
            previewRow.append("<tr align=\"left\">\n");
            previewRow.append("<td>\n<b><nobr>");
            previewRow.append(column1);
            previewRow.append("&nbsp;</nobr></b>\n</td>\n");
            previewRow.append("<td>\n");
            previewRow.append(column2);
            previewRow.append("</td>\n");
            // this column is for edit button that is not available here: 
            previewRow.append("<td>&nbsp;</td>\n");
            // this is glue or the button would be stretched
            previewRow.append("<td class=\"maxwidth\">&nbsp;</td>\n");
            previewRow.append("</tr>\n");
            result = previewRow.toString();
        }
        return result;
    }

    /**
     * Sets the value of the request parameter <code>{@link #PARAM_EDITPROPERTY}</code>.<p>
     * 
     * @param paramEditProperty the value of the request parameter <code>{@link #PARAM_EDITPROPERTY}</code> to set
     */
    public void setParamEditProperty(String paramEditProperty) {

        m_paramEditProperty = paramEditProperty;
    }

    /**
     * Sets the value of the request parameter <code>{@link #PARAM_EDITPROPERTY_VALUE}</code>.<p>
     * 
     * @param paramEditPropertyValue the value of the request parameter <code>{@link #PARAM_EDITPROPERTY_VALUE}</code> to set
     */
    public void setParamEditPropertyValue(String paramEditPropertyValue) {

        m_paramEditPropertyValue = paramEditPropertyValue;
    }

    /**
     * @see org.opencms.workplace.galleries.A_CmsGallery#wizardButton()
     */
    public String wizardButton() {

        return button("javascript:wizard();", null, "upload.png", Messages.GUI_TITLE_NEW_0, 0);
    }

    /**
     * @see org.opencms.workplace.galleries.A_CmsGallery#buildGalleryItemListCustomEndCols(org.opencms.file.CmsResource, java.lang.String)
     */
    protected String buildGalleryItemListCustomEndCols(CmsResource res, String tdClass) {

        StringBuffer result = new StringBuffer(64);
        result.append("\t<td class=\"");
        result.append(tdClass);
        result.append("\">");
        String linkTarget;
        try {
            CmsFile file = CmsFile.upgrade(res, getCms());
            linkTarget = new String(file.getContents());
        } catch (CmsException e) {
            linkTarget = "";
        }
        result.append(linkTarget);
        result.append("</td>\n");
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.galleries.A_CmsGallery#buildGalleryItemListHeadline()
     */
    protected String buildGalleryItemListHeadline() {

        StringBuffer headline = new StringBuffer(16);
        headline.append("<tr>");
        headline.append("<td class=\"headline\">&nbsp;</td>");
        headline.append("<td class=\"headline\" width=\"25%\">");
        headline.append(key(Messages.GUI_LABEL_NAME_0));
        headline.append("</td>");
        headline.append("<td class=\"headline\" width=\"45%\">");
        headline.append(key(Messages.GUI_LABEL_TITLE_0));
        headline.append("</td>");
        headline.append("<td class=\"headline\" width=\"30%\">");
        headline.append(key(Messages.GUI_INPUT_LINKTO_0));
        headline.append("</td>");
        headline.append("</tr>");

        return headline.toString();
    }

    /**
     * Returns a list of hit items.<p>
     * 
     * Searches by the title property value, resource name and stored external link.<p> 
     * 
     * @param items a list of resource items
     * @return a list of hit items
     */
    protected List getSearchHits(List items) {

        String searchword = getParamSearchWord().toLowerCase();
        List hitlist = new ArrayList();
        if (items != null) {
            Iterator i = items.iterator();
            while (i.hasNext()) {
                try {
                    CmsResource res = (CmsResource)i.next();
                    String resname = res.getName().toLowerCase();
                    String restitle = getJsp().property(
                        CmsPropertyDefinition.PROPERTY_TITLE,
                        getCms().getSitePath(res),
                        resname).toLowerCase();
                    // get the link    
                    CmsFile file = CmsFile.upgrade(res, getCms());
                    String link = new String(file.getContents()).toLowerCase();

                    if ((restitle.indexOf(searchword) != -1)
                        || (resname.indexOf(searchword) != -1)
                        || (link.indexOf(searchword) != -1)) {
                        // add this resource to the hitlist
                        hitlist.add(res);
                    }
                } catch (CmsException e) {
                    // this should never happen, but in case it does, skip this resource
                }
            }
        }

        return hitlist;
    }

    /**
     * Changes the value of the property title for the specified resource.<p>
     *  
     * @param res the resource to change the property value
     */
    protected void writeTitleProperty(CmsResource res) {

        String resPath = getCms().getSitePath(res);
        String currentPropertyValue = getParamEditPropertyValue();
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
            boolean locked = true;
            CmsLock lock = getCms().getLock(res);
            if (lock.isUnlocked()) {
                // lock resource before operation
                getCms().lockResource(resPath);
                locked = false;
            }
            // write the property to the resource
            getCms().writePropertyObject(resPath, currentProperty);
            if (!locked) {
                // unlock the resource
                getCms().unlockResource(resPath);
            }
        } catch (CmsException e) {
            // writing the property failed, log error
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Changes the given pointer ressource if the current request has <code>{@link org.opencms.workplace.CmsDialog#PARAM_ACTION}</code> 
     * set to <code>{@link A_CmsGallery#DIALOG_EDITPROPERTY}</code>.<p>
     * 
     * @param res the internal pointer resource to modify
     * 
     * @throws CmsException if sth. goes wrong
     */
    private void changePointer(CmsResource res) throws CmsException {

        // don't do anything if this is no edit property submit
        if (DIALOG_EDITPROPERTY.equals(getParamAction())) {
            if (hasWritePermissions()) {
                if (VALUE_EDITPROPERTY_LINK.equals(m_paramEditProperty)) {
                    writePointerLink(res);
                } else if (VALUE_EDITPROPERTY_TITLE.equals(m_paramEditProperty)) {
                    writeTitleProperty(res);
                }
            }
        }

    }

    /**
     * Generates an edit property button for the gallery button bar.<p>
     * 
     * If the current resource is not 'editable' a disabled button will be returned.<p>
     * 
     * @param property the property to edit 
     * 
     * @return an edit property button for the gallery button bar
     * 
     * @see org.opencms.workplace.galleries.A_CmsGallery#editPropertyButton()
     */
    private String editPropertyButton(String property) {

        try {
            if (hasWritePermissions()) {
                return button(
                    "javascript:submitAction('" + property + "');",
                    null,
                    "edit.png",
                    Messages.GUI_INPUT_EDITPROPERTYINFO_0,
                    0);
            }
        } catch (CmsException e) {
            // error checking permissions
            LOG.error(e.getLocalizedMessage(), e);
        }
        return button(null, null, "edit_in.png", "", 0);
    }

    /**
     * Generates a HTML table row with two columns that contain the name on the left side and an a text input or 
     * plain text on the right side with the given <code>id attribute</code> value.<p>
     * 
     * The first column includes the given display String, the second column includes an input field with the 
     * id attribute set to <code>column1</code> preset with <code>column2</code>.<p>
     * 
     * @param column1 the string value for the first column
     * 
     * @param column2 the string value for the input field in the 2nd column 
     * 
     * @param inputFieldId the <code>id attribute</code> value for the input field in the 2nd column
     * 
     * @return a HTML table row with two columns
     */
    private String previewRow(String column1, String column2, String inputFieldId) {

        StringBuffer previewRow = new StringBuffer();
        previewRow.append("<tr align=\"left\">\n");
        previewRow.append("<td>\n<b><nobr>");
        previewRow.append(column1);
        previewRow.append("&nbsp;</nobr></b>\n</td>\n");
        previewRow.append("<td>\n");
        previewRow.append("<input id=\"").append(inputFieldId).append("\" name=\"").append(inputFieldId).append(
            "\" value=\"").append(column2).append("\" size=\"50\"/>");
        previewRow.append("</td>\n");
        previewRow.append(this.editPropertyButton(inputFieldId));
        // this is glue or the button would be stretched
        previewRow.append("<td class=\"maxwidth\">&nbsp;</td>\n");
        previewRow.append("</tr>\n");

        return previewRow.toString();

    }

    /**
     * Writes the current link into the pointer resource. <p>
     * 
     * @param res the pointer resource to change the link of 
     * 
     * @throws CmsException if sth. goes wrong
     */
    private void writePointerLink(CmsResource res) throws CmsException {

        String resPath = getCms().getSitePath(res);
        boolean locked = true;
        CmsLock lock = getCms().getLock(res);
        if (lock.isUnlocked()) {
            // lock resource before operation
            getCms().lockResource(resPath);
            locked = false;
        }
        CmsFile file = CmsFile.upgrade(res, getCms());
        file.setContents(m_paramEditPropertyValue.getBytes());
        checkLock(getCms().getSitePath(res));
        getCms().writeFile(file);
        if (!locked) {
            // unlock the resource
            getCms().unlockResource(resPath);
        }
    }
}