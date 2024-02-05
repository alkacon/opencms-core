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

import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.PrintfFormat;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Provides methods for the rename images dialog.<p>
 *
 * The following files use this class:
 * <ul>
 * <li>/commons/renameimages.jsp
 * </ul>
 * <p>
 *
 * @since 6.1.3
 */
public class CmsRenameImages extends CmsDialog {

    /** Value for the action: rename images. */
    public static final int ACTION_RENAMEIMAGES = 100;

    /** The dialog type. */
    public static final String DIALOG_TYPE = "renameimages";

    /** Selectbox option for decimal places selection: 1 place. */
    public static final String OPTION_DECIMALPLACES_1 = "1 (1, 2, ..., 9)";

    /** Selectbox option for decimal places selection: 2 places. */
    public static final String OPTION_DECIMALPLACES_2 = "2 (01, 02, ..., 99)";

    /** Selectbox option for decimal places selection: 3 places. */
    public static final String OPTION_DECIMALPLACES_3 = "3 (001, 002, ..., 999)";

    /** Selectbox option for decimal places selection: 4 places. */
    public static final String OPTION_DECIMALPLACES_4 = "4 (0001, 0002, ..., 9999)";

    /** Request parameter name for the counter places. */
    public static final String PARAM_PLACES = "places";

    /** Request parameter name for the image prefix. */
    public static final String PARAM_PREFIX = "prefix";

    /** Request parameter name for the remove title flag. */
    public static final String PARAM_REMOVETITLE = "removetitle";

    /** Request parameter name for the start count. */
    public static final String PARAM_STARTCOUNT = "startcount";

    /** Dialog parameter. */
    private String m_paramPlaces;

    /** Dialog parameter. */
    private String m_paramPrefix;

    /** Dialog parameter. */
    private String m_paramRemovetitle;

    /** Dialog parameter. */
    private String m_paramStartcount;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsRenameImages(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsRenameImages(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Performs the rename images action, will be called by the JSP page.<p>
     *
     * @throws JspException if problems including sub-elements occur
     */
    public void actionRenameImages() throws JspException {

        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(SESSION_WORKPLACE_CLASS, this);
        try {
            if (performDialogOperation()) {
                // if no exception is caused and "true" is returned rename operation was successful
                actionCloseDialog();
            } else {
                // "false" returned, display "please wait" screen
                getJsp().include(FILE_DIALOG_SCREEN_WAIT);
            }
        } catch (Throwable e) {
            // error during rename images, show error dialog
            includeErrorpage(this, e);
        }
    }

    /**
     * Returns information about the image count of the selected gallery folder.<p>
     *
     * @return information about the image count of the selected gallery folder
     */
    public String buildImageInformation() {

        // count all image resources of the gallery folder
        int count = 0;
        try {
            int imageId = OpenCms.getResourceManager().getResourceType(
                CmsResourceTypeImage.getStaticTypeName()).getTypeId();
            CmsResourceFilter filter = CmsResourceFilter.IGNORE_EXPIRATION.addRequireType(imageId);
            List<CmsResource> images = getCms().readResources(getParamResource(), filter, false);
            count = images.size();
        } catch (CmsException e) {
            // ignore this exception
        }

        Object[] args = new Object[] {getParamResource(), Integer.valueOf(count)};
        return key(Messages.GUI_RENAMEIMAGES_INFO_IMAGECOUNT_2, args);
    }

    /**
     * Builds the html for the default copy folder mode select box.<p>
     *
     * @param htmlAttributes optional html attributes for the &lgt;select&gt; tag
     * @return the html for the default copy folder mode select box
     */
    public String buildSelectPlaces(String htmlAttributes) {

        List<String> options = new ArrayList<String>(4);
        options.add(OPTION_DECIMALPLACES_1);
        options.add(OPTION_DECIMALPLACES_2);
        options.add(OPTION_DECIMALPLACES_3);
        options.add(OPTION_DECIMALPLACES_4);
        List<String> values = new ArrayList<String>(4);
        values.add("1");
        values.add("2");
        values.add("3");
        values.add("4");
        int selectedIndex = 2;
        if (getAction() != ACTION_DEFAULT) {
            selectedIndex = values.indexOf(getParamPlaces());
        }
        return buildSelect(htmlAttributes, options, values, selectedIndex);
    }

    /**
     * Returns the default prefix shown when opening the dialog.<p>
     *
     * @return the default prefix shown when opening the dialog
     */
    public String getDefaultPrefix() {

        return key(Messages.GUI_RENAMEIMAGES_DEFAULT_PREFIX_0);
    }

    /**
     * Returns the default start count shown when opening the dialog.<p>
     *
     * @return the default start count shown when opening the dialog
     */
    public String getDefaultStartcount() {

        return "1";
    }

    /**
     * Returns the value of the places parameter.<p>
     *
     * @return the value of the places parameter
     */
    public String getParamPlaces() {

        return m_paramPlaces;
    }

    /**
     * Returns the value of the prefix parameter.<p>
     *
     * @return the value of the prefix parameter
     */
    public String getParamPrefix() {

        return m_paramPrefix;
    }

    /**
     * Returns the value of the remove title parameter.<p>
     *
     * @return the value of the remove title parameter
     */
    public String getParamRemovetitle() {

        return m_paramRemovetitle;
    }

    /**
     * Returns the value of the startcount parameter.<p>
     *
     * @return the value of the startcount parameter
     */
    public String getParamStartcount() {

        return m_paramStartcount;
    }

    /**
     * Sets the value of the places parameter.<p>
     *
     * @param paramPlaces the value of the places parameter
     */
    public void setParamPlaces(String paramPlaces) {

        m_paramPlaces = paramPlaces;
    }

    /**
     * Sets the value of the prefix parameter.<p>
     *
     * @param paramPrefix the value of the prefix parameter
     */
    public void setParamPrefix(String paramPrefix) {

        m_paramPrefix = paramPrefix;
    }

    /**
     * Sets the value of the remove title parameter.<p>
     *
     * @param paramRemovetitle the value of the remove title parameter
     */
    public void setParamRemovetitle(String paramRemovetitle) {

        m_paramRemovetitle = paramRemovetitle;
    }

    /**
     * Sets the value of the startcount parameter.<p>
     *
     * @param paramStartcount the value of the startcount parameter
     */
    public void setParamStartcount(String paramStartcount) {

        m_paramStartcount = paramStartcount;
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
            setAction(ACTION_RENAMEIMAGES);
        } else if (DIALOG_WAIT.equals(getParamAction())) {
            setAction(ACTION_WAIT);
        } else if (DIALOG_LOCKS_CONFIRMED.equals(getParamAction())) {
            setAction(ACTION_LOCKS_CONFIRMED);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else {
            setAction(ACTION_DEFAULT);
            // build title for rename images dialog
            Object[] args = new Object[] {getParamResource()};
            setParamTitle(key(Messages.GUI_RENAMEIMAGES_TITLE_1, args));
        }
    }

    /**
     * Performs the rename images operation.<p>
     *
     * @return true, if the resources were successfully renamed, otherwise false
     * @throws CmsException if renaming is not successful
     */
    protected boolean performDialogOperation() throws CmsException {

        // display "please wait" screen before renaming the images
        if (!DIALOG_WAIT.equals(getParamAction())) {
            // return false, this will trigger the "please wait" screen
            return false;
        }

        // lock the image gallery folder
        checkLock(getParamResource());

        // get all image resources of the folder
        int imageId = OpenCms.getResourceManager().getResourceType(
            CmsResourceTypeImage.getStaticTypeName()).getTypeId();
        CmsResourceFilter filter = CmsResourceFilter.IGNORE_EXPIRATION.addRequireType(imageId);
        List<CmsResource> images = getCms().readResources(getParamResource(), filter, false);

        // determine start count
        int count = 1;
        try {
            count = Integer.parseInt(getParamStartcount());
        } catch (Exception e) {
            // ignore this exception
        }

        // create number printer instance
        PrintfFormat numberFormat = new PrintfFormat("%0." + getParamPlaces() + "d");

        // create image galler folder name
        String folder = getParamResource();
        if (!folder.endsWith("/")) {
            folder += "/";
        }

        Iterator<CmsResource> i = images.iterator();
        // loop over all image resource to change
        while (i.hasNext()) {
            CmsResource res = i.next();
            String oldName = CmsResource.getName(res.getRootPath());
            CmsProperty titleProperty = getCms().readPropertyObject(res, CmsPropertyDefinition.PROPERTY_TITLE, false);
            String oldTitle = titleProperty.getValue();

            // store image name suffix
            int lastDot = oldName.lastIndexOf('.');
            String suffix = "";
            String oldNameWithoutSuffix = oldName;
            if (lastDot > -1) {
                suffix = oldName.substring(lastDot);
                oldNameWithoutSuffix = oldName.substring(0, lastDot);
            }

            // determine new image name
            String newName = "";
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getParamPrefix()) && !"null".equals(getParamPrefix())) {
                newName += getParamPrefix();
            }
            // create image number
            String imageNumber = numberFormat.sprintf(count);
            newName += imageNumber + suffix;

            if (!newName.equals(oldName)) {
                // only rename resources which have a new resource name
                if (getCms().existsResource(folder + newName, CmsResourceFilter.ALL)) {
                    // target resource exists, interrupt & show error
                    throw new CmsException(
                        Messages.get().container(
                            Messages.ERR_MOVE_FAILED_TARGET_EXISTS_2,
                            getCms().getSitePath(res),
                            folder + newName));
                }

                // determine the new title property value
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(oldTitle)) {
                    if (oldTitle.equals(oldNameWithoutSuffix)) {
                        if (Boolean.valueOf(getParamRemovetitle()).booleanValue()) {
                            // remove the title property value
                            if (oldTitle.equals(titleProperty.getStructureValue())) {
                                titleProperty.setStructureValue(CmsProperty.DELETE_VALUE);
                            }
                            if (oldTitle.equals(titleProperty.getResourceValue())) {
                                titleProperty.setResourceValue(CmsProperty.DELETE_VALUE);
                            }
                        } else {
                            // set the title property to the new resource name
                            if (oldTitle.equals(titleProperty.getStructureValue())) {
                                titleProperty.setStructureValue(getParamPrefix() + imageNumber);
                            } else if (oldTitle.equals(titleProperty.getResourceValue())) {
                                titleProperty.setResourceValue(getParamPrefix() + imageNumber);
                            }
                        }
                        // write changed title property
                        getCms().writePropertyObject(getCms().getSitePath(res), titleProperty);
                    }
                }

                // now rename the resource
                getCms().renameResource(folder + oldName, folder + newName);
            }

            // increase image counter
            count++;
        }

        return true;
    }

}
