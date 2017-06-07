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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.commons;

import com.alkacon.simapi.Simapi;

import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.loader.CmsImageScaler;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.awt.Color;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Provides methods for the comment images dialog on image gallery folders.<p>
 *
 * The following files use this class:
 * <ul>
 * <li>/commons/commentimages.jsp
 * </ul>
 * <p>
 *
 * @since 6.1.3
 */
public class CmsCommentImages extends CmsDialog {

    /** Value for the action: comment images. */
    public static final int ACTION_COMMENTIMAGES = 100;

    /** The dialog type. */
    public static final String DIALOG_TYPE = "commentimages";

    /** The input field prefix for description property fields. */
    public static final String PREFIX_DESCRIPTION = "desc_";

    /** The input field prefix for title property fields. */
    public static final String PREFIX_TITLE = "title_";

    /** The height of the dialog thumbnails. */
    public static final int THUMB_HEIGHT = 150;

    /** The width of the dialog thumbnails. */
    public static final int THUMB_WIDTH = 200;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsCommentImages.class);

    /** The image scaler object used in the dialog input form. */
    private CmsImageScaler m_imageScaler;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsCommentImages(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsCommentImages(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Performs the comment images action, will be called by the JSP page.<p>
     *
     * @throws JspException if problems including sub-elements occur
     */
    public void actionCommentImages() throws JspException {

        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(SESSION_WORKPLACE_CLASS, this);
        try {
            performDialogOperation();
            // if no exception is caused comment operation was successful
            actionCloseDialog();
        } catch (Throwable e) {
            // error during rename images, show error dialog
            includeErrorpage(this, e);
        }
    }

    /**
     * Returns the HTML for the dialog input form to comment the images.<p>
     *
     * @return the HTML for the dialog input form to comment the images
     */
    public String buildDialogForm() {

        StringBuffer result = new StringBuffer(16384);
        Iterator<CmsResource> i = getImages().iterator();

        result.append("<div style=\"height: 450px; padding: 4px; overflow: auto;\">");

        while (i.hasNext()) {
            CmsResource res = i.next();
            String imageName = res.getName();
            String propertySuffix = "" + imageName.hashCode();
            result.append(dialogBlockStart(imageName));
            result.append("<table border=\"0\">\n");
            result.append("<tr>\n\t<td style=\"vertical-align: top;\">");
            // create image tag
            result.append("<img src=\"");
            StringBuffer link = new StringBuffer(256);
            link.append(getCms().getSitePath(res));
            link.append(getImageScaler().toRequestParam());
            result.append(getJsp().link(link.toString()));
            result.append("\" border=\"0\" alt=\"\" width=\"");
            result.append(getImageScaler().getWidth());
            result.append("\" height=\"");
            result.append(getImageScaler().getHeight());
            result.append("\">");

            result.append("</td>\n");
            result.append("\t<td class=\"maxwidth\" style=\"vertical-align: top;\">\n");

            result.append("\t\t<table border=\"0\">\n");

            // build title property input row
            String title = "";
            try {
                title = getCms().readPropertyObject(res, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue();
            } catch (CmsException e) {
                // log, should never happen
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(getLocale()));
                }
            }
            result.append("\t\t<tr>\n\t\t\t<td style=\"white-space: nowrap;\" unselectable=\"on\">");
            result.append(key(Messages.GUI_LABEL_TITLE_0));
            result.append(":</td>\n\t\t\t<td class=\"maxwidth\">");
            result.append("<input type=\"text\" class=\"maxwidth\" name=\"");
            result.append(PREFIX_TITLE);
            result.append(propertySuffix);
            result.append("\" value=\"");
            if (CmsStringUtil.isNotEmpty(title)) {
                result.append(CmsEncoder.escapeXml(title));
            }
            result.append("\">");
            result.append("</td>\n\t\t</tr>\n");

            // build description property input row
            String description = "";
            try {
                description = getCms().readPropertyObject(
                    res,
                    CmsPropertyDefinition.PROPERTY_DESCRIPTION,
                    false).getValue();
            } catch (CmsException e) {
                // log, should never happen
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(getLocale()));
                }
            }
            result.append(
                "\t\t<tr>\n\t\t\t<td style=\"white-space: nowrap; vertical-align: top;\" unselectable=\"on\">");
            result.append(key(Messages.GUI_LABEL_DESCRIPTION_0));
            result.append(":</td>\n\t\t\t<td style=\"vertical-align: top; height: 110px;\">");
            result.append("<textarea rows=\"8\" class=\"maxwidth\" style=\"overflow: auto;\" name=\"");
            result.append(PREFIX_DESCRIPTION);
            result.append(propertySuffix);
            result.append("\">");
            if (CmsStringUtil.isNotEmpty(description)) {
                result.append(CmsEncoder.escapeXml(description));
            }
            result.append("</textarea>");
            result.append("</td>\n\t\t</tr>\n");

            result.append("\t\t</table>\n");

            result.append("</td>\n</tr>\n");
            result.append("</table>\n");
            result.append(dialogBlockEnd());

            if (i.hasNext()) {
                // append spacer if another entry follows
                result.append(dialogSpacer());
            }
        }

        result.append("</div>");

        return result.toString();
    }

    /**
     * Returns the image resources of the gallery folder which are edited in the dialog form.<p>
     *
     * @return the images of the gallery folder which are edited in the dialog form
     */
    protected List<CmsResource> getImages() {

        // get all image resources of the folder
        int imageId;
        try {
            imageId = OpenCms.getResourceManager().getResourceType(
                CmsResourceTypeImage.getStaticTypeName()).getTypeId();
        } catch (CmsLoaderException e1) {
            // should really never happen
            LOG.warn(e1.getLocalizedMessage(), e1);
            imageId = CmsResourceTypeImage.getStaticTypeId();
        }
        CmsResourceFilter filter = CmsResourceFilter.IGNORE_EXPIRATION.addRequireType(imageId);
        try {
            return getCms().readResources(getParamResource(), filter, false);
        } catch (CmsException e) {
            // log, should never happen
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(getLocale()));
            }
            return Collections.emptyList();
        }
    }

    /**
     * Returns the initialized image scaler object used to generate thumbnails for the dialog form.<p>
     *
     * @return the initialized image scaler object used to generate thumbnails for the dialog form
     */
    protected CmsImageScaler getImageScaler() {

        if (m_imageScaler == null) {
            // not initialized, create image scaler with default settings
            m_imageScaler = new CmsImageScaler();
            m_imageScaler.setWidth(THUMB_WIDTH);
            m_imageScaler.setHeight(THUMB_HEIGHT);
            m_imageScaler.setRenderMode(Simapi.RENDER_SPEED);
            m_imageScaler.setColor(new Color(0, 0, 0));
            m_imageScaler.setType(1);
        }
        return m_imageScaler;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);

        // check the required permissions to rename the resource
        if (!checkResourcePermissions(CmsPermissionSet.ACCESS_WRITE, false)) {
            // no write permissions for the resource, set cancel action to close dialog
            setParamAction(DIALOG_CANCEL);
        }

        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        // set the action for the JSP switch
        if (DIALOG_TYPE.equals(getParamAction())) {
            setAction(ACTION_COMMENTIMAGES);
        } else if (DIALOG_LOCKS_CONFIRMED.equals(getParamAction())) {
            setAction(ACTION_LOCKS_CONFIRMED);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else {
            setAction(ACTION_DEFAULT);
            // build title for comment images dialog
            Object[] args = new Object[] {getParamResource()};
            setParamTitle(key(Messages.GUI_COMMENTIMAGES_TITLE_1, args));
        }
    }

    /**
     * Performs the comment images operation.<p>
     *
     * @return true, if the resources were successfully processed, otherwise false
     * @throws CmsException if commenting is not successful
     */
    protected boolean performDialogOperation() throws CmsException {

        // lock the image gallery folder
        checkLock(getParamResource());

        Iterator<CmsResource> i = getImages().iterator();
        // loop over all image resources to change the properties
        while (i.hasNext()) {
            CmsResource res = i.next();
            String imageName = res.getName();
            String propertySuffix = "" + imageName.hashCode();

            // update the title property
            CmsProperty titleProperty = getCms().readPropertyObject(res, CmsPropertyDefinition.PROPERTY_TITLE, false);
            String newValue = getJsp().getRequest().getParameter(PREFIX_TITLE + propertySuffix);
            writeProperty(res, CmsPropertyDefinition.PROPERTY_TITLE, newValue, titleProperty);

            // update the description property
            CmsProperty descProperty = getCms().readPropertyObject(
                res,
                CmsPropertyDefinition.PROPERTY_DESCRIPTION,
                false);
            newValue = getJsp().getRequest().getParameter(PREFIX_DESCRIPTION + propertySuffix);
            writeProperty(res, CmsPropertyDefinition.PROPERTY_DESCRIPTION, newValue, descProperty);
        }

        return true;
    }

    /**
     * Writes a property value for a resource, if the value was changed.<p>
     *
     * @param res the resource to write the property to
     * @param propName the name of the property definition
     * @param propValue the new value of the property
     * @param currentProperty the old property object
     * @throws CmsException if something goes wrong
     */
    protected void writeProperty(CmsResource res, String propName, String propValue, CmsProperty currentProperty)
    throws CmsException {

        // check if current property is not the null property
        if (currentProperty.isNullProperty()) {
            // create new property object
            currentProperty = new CmsProperty();
            currentProperty.setName(propName);
        }

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(propValue)) {
            // parameter is empty, determine the value to delete
            boolean writeProperty = false;
            if (currentProperty.getStructureValue() != null) {
                currentProperty.setStructureValue(CmsProperty.DELETE_VALUE);
                currentProperty.setResourceValue(null);
                writeProperty = true;
            } else if (currentProperty.getResourceValue() != null) {
                currentProperty.setResourceValue(CmsProperty.DELETE_VALUE);
                currentProperty.setStructureValue(null);
                writeProperty = true;
            }
            if (writeProperty) {
                // write the updated property object
                getCms().writePropertyObject(getCms().getSitePath(res), currentProperty);
            }
        } else {
            // parameter is not empty, check if the value has changed
            if (!propValue.equals(currentProperty.getValue())) {
                if ((currentProperty.getStructureValue() == null) && (currentProperty.getResourceValue() == null)) {
                    // new property, determine setting from OpenCms workplace configuration
                    if (OpenCms.getWorkplaceManager().isDefaultPropertiesOnStructure()) {
                        currentProperty.setStructureValue(propValue);
                        currentProperty.setResourceValue(null);
                    } else {
                        currentProperty.setResourceValue(propValue);
                        currentProperty.setStructureValue(null);
                    }
                } else if (currentProperty.getStructureValue() != null) {
                    // structure value has to be updated
                    currentProperty.setStructureValue(propValue);
                    currentProperty.setResourceValue(null);
                } else {
                    // resource value has to be updated
                    currentProperty.setResourceValue(propValue);
                    currentProperty.setStructureValue(null);
                }
                // write the updated property object
                getCms().writePropertyObject(getCms().getSitePath(res), currentProperty);
            }
        }
    }

}
