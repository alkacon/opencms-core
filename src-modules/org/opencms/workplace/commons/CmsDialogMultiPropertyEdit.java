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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Provides methods for the multi file property edit action.
 * <p>
 *
 * The following files use this class:
 * <ul>
 * <li>/commons/property_multifile.jsp
 * </ul>
 * <p>
 *
 * @since 7.5.1
 */
public class CmsDialogMultiPropertyEdit extends CmsDialog {

    /** Value for the action: comment images. */
    public static final int ACTION_MULTIFILEPROPERTYEDIT = 100;

    /** The dialog type. */
    public static final String DIALOG_TYPE = "multifilepropertyedit";

    /** The input field prefix for description property fields. */
    public static final String PREFIX_DESCRIPTION = "desc_";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDialogMultiPropertyEdit.class);

    /**
     * Public constructor with JSP action element.
     * <p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsDialogMultiPropertyEdit(final CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.
     * <p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsDialogMultiPropertyEdit(
        final PageContext context,
        final HttpServletRequest req,
        final HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Performs the comment files action, will be called by the JSP page.
     * <p>
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
     * Returns the HTML for the dialog input form to comment the images.
     * <p>
     *
     * @return the HTML for the dialog input form to comment the images
     */
    public String buildDialogForm() {

        StringBuffer result = new StringBuffer(16384);
        List<CmsResource> resources = getResources();
        //Compute the height:
        int amountOfInputFields = 4 * resources.size();
        int height = amountOfInputFields * 25;
        // add padding for each resource grouping:
        height += resources.size() * 30;
        // add padding for whole dialog box:
        height += 80;
        // limit maximum height:
        height = Math.min(height, 600);

        Iterator<CmsResource> i = resources.iterator();

        result.append("<div style=\"height: ").append(height).append("px; padding: 4px; overflow: auto;\">");
        CmsResource res;
        while (i.hasNext()) {
            res = i.next();
            I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(res);
            // read the default properties for the given file type:
            CmsExplorerTypeSettings settings = getSettingsForType(type.getTypeName());
            List<String> editProperties = settings.getProperties();
            if (editProperties.size() > 0) {
                String iconPath = getSkinUri() + CmsWorkplace.RES_PATH_FILETYPES + settings.getIcon();
                String imageName = res.getName();
                String propertySuffix = "" + imageName.hashCode();
                result.append(dialogBlockStart("<img src=\"" + iconPath + "\"/>&nbsp;" + imageName));
                result.append("<table border=\"0\">\n");

                Iterator<String> itProperties = editProperties.iterator();
                String property;
                while (itProperties.hasNext()) {
                    property = itProperties.next();
                    result.append("<tr>\n");
                    result.append("<td>&nbsp;</td>\n");
                    // build title property input row
                    String title = "";
                    try {
                        title = getCms().readPropertyObject(res, property, false).getValue();
                    } catch (CmsException e) {
                        // log, should never happen
                        if (LOG.isErrorEnabled()) {
                            LOG.error(e.getLocalizedMessage(getLocale()));
                        }
                    }
                    result.append("<td style=\"white-space: nowrap;\" unselectable=\"on\" width=\"15%\">");
                    result.append(property).append(": ");
                    result.append("</td>\n");
                    result.append("<td class=\"maxwidth\">");
                    result.append("<input type=\"text\" class=\"maxwidth\" name=\"");
                    result.append(property);
                    result.append(propertySuffix);
                    result.append("\" value=\"");
                    if (CmsStringUtil.isNotEmpty(title)) {
                        result.append(CmsEncoder.escapeXml(title));
                    }
                    result.append("\"");
                    result.append(">");
                    result.append("</td>\n</tr>\n");
                }
                result.append("</table>\n");
                result.append(dialogBlockEnd());
            }

            if (i.hasNext()) {
                // append spacer if another entry follows
                result.append(dialogSpacer());
            }
        }

        result.append("</div>");

        return result.toString();
    }

    /**
     * Returns the image resources of the gallery folder which are edited in the dialog form.
     * <p>
     *
     * @return the images of the gallery folder which are edited in the dialog form
     */
    protected List<CmsResource> getResources() {

        List<CmsResource> result = Collections.emptyList();
        // get all image resources of the folder
        CmsResourceFilter filter = CmsResourceFilter.DEFAULT;
        try {
            CmsObject cms = getCms();
            result = cms.readResources(getParamResource(), filter, false);

        } catch (CmsException e) {
            // log, should never happen
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(getLocale()));
            }
        }
        return result;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings,
     *      javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(final CmsWorkplaceSettings settings, final HttpServletRequest request) {

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
            setAction(ACTION_MULTIFILEPROPERTYEDIT);
        } else if (DIALOG_LOCKS_CONFIRMED.equals(getParamAction())) {
            setAction(ACTION_LOCKS_CONFIRMED);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else {
            setAction(ACTION_DEFAULT);
            // build title for comment images dialog
            Object[] args = new Object[] {getParamResource()};
            setParamTitle(key(Messages.GUI_MULTIFILE_PROPERTY_TITLE_1, args));
        }
    }

    /**
     * Performs the comment images operation.
     * <p>
     *
     * @return true, if the resources were successfully processed, otherwise false
     * @throws CmsException if commenting is not successful
     */
    protected boolean performDialogOperation() throws CmsException {

        // lock the folder
        checkLock(getParamResource());

        Iterator<CmsResource> i = getResources().iterator();
        // loop over all image resources to change the properties
        CmsResource res;
        while (i.hasNext()) {
            res = i.next();
            String imageName = res.getName();
            String propertySuffix = "" + imageName.hashCode();

            String property;
            I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(res);
            CmsExplorerTypeSettings settings = getSettingsForType(type.getTypeName());
            Iterator<String> itProperties = settings.getProperties().iterator();
            while (itProperties.hasNext()) {
                property = itProperties.next();
                CmsProperty currProperty = getCms().readPropertyObject(res, property, false);
                String newValue = getJsp().getRequest().getParameter(property + propertySuffix);
                writeProperty(res, property, newValue, currProperty);
            }
        }

        // unlock the folder
        CmsObject cms = getCms();
        cms.unlockResource(getParamResource());
        return true;
    }

    /**
     * Writes a property value for a resource, if the value was changed.
     * <p>
     *
     * @param res the resource to write the property to
     * @param propName the name of the property definition
     * @param propValue the new value of the property
     * @param currentProperty the old property object
     * @throws CmsException if something goes wrong
     */
    protected void writeProperty(
        final CmsResource res,
        final String propName,
        final String propValue,
        final CmsProperty currentProperty) throws CmsException {

        CmsProperty prop = currentProperty;
        // check if current property is not the null property
        if (prop.isNullProperty()) {
            // create new property object
            prop = new CmsProperty();
            prop.setName(propName);
        }

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(propValue)) {
            // parameter is empty, determine the value to delete
            boolean writeProperty = false;
            if (prop.getStructureValue() != null) {
                prop.setStructureValue(CmsProperty.DELETE_VALUE);
                prop.setResourceValue(null);
                writeProperty = true;
            } else if (prop.getResourceValue() != null) {
                prop.setResourceValue(CmsProperty.DELETE_VALUE);
                prop.setStructureValue(null);
                writeProperty = true;
            }
            if (writeProperty) {
                // write the updated property object
                getCms().writePropertyObject(getCms().getSitePath(res), prop);
            }
        } else {
            // parameter is not empty, check if the value has changed
            if (!propValue.equals(prop.getValue())) {
                if ((prop.getStructureValue() == null) && (prop.getResourceValue() == null)) {
                    // new property, determine setting from OpenCms workplace configuration
                    if (OpenCms.getWorkplaceManager().isDefaultPropertiesOnStructure()) {
                        prop.setStructureValue(propValue);
                        prop.setResourceValue(null);
                    } else {
                        prop.setResourceValue(propValue);
                        prop.setStructureValue(null);
                    }
                } else if (prop.getStructureValue() != null) {
                    // structure value has to be updated
                    prop.setStructureValue(propValue);
                    prop.setResourceValue(null);
                } else {
                    // resource value has to be updated
                    prop.setResourceValue(propValue);
                    prop.setStructureValue(null);
                }
                // write the updated property object
                getCms().writePropertyObject(getCms().getSitePath(res), prop);
            }
        }
    }

    /**
     * Returns the explorer type settings of the resource type, considering eventual references to another type.<p>
     *
     * @param resTypeName the resource type name
     * @return the explorer type settings of the resource type
     */
    private CmsExplorerTypeSettings getSettingsForType(String resTypeName) {

        // get settings for resource type
        CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(resTypeName);
        if (!settings.hasEditOptions() && CmsStringUtil.isNotEmpty(settings.getReference())) {
            // refers to another resource type, get settings of referred type
            settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(settings.getReference());
        }
        return settings;
    }
}
