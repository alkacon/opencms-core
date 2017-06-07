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
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.explorer.CmsNewResource;
import org.opencms.workplace.list.A_CmsListResourceTypeDialog;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListOrderEnum;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * The change resource type dialog handles the change of a resource type of a single VFS file.<p>
 *
 * The following files use this class:
 * <ul>
 * <li>/commons/chtype.jsp
 * </ul>
 * <p>
 *
 * @since 6.0.0
 */
public class CmsChtype extends A_CmsListResourceTypeDialog {

    /** Dialog action: show advanced list (for workplace VFS managers). */
    public static final int ACTION_ADVANCED = 555;

    /** The dialog type.<p> */
    public static final String DIALOG_TYPE = "chtype";

    /** Session attribute to store advanced mode. */
    public static final String SESSION_ATTR_ADVANCED = "ocms_chtype_adv";

    /** Flag indicating if dialog is in advanced mode. */
    private boolean m_advancedMode;

    /** The available resource types as String, if set. */
    private String m_availableResTypes;

    /** Flag indicating if resource types to select are limited. */
    private boolean m_limitedRestypes;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsChtype(CmsJspActionElement jsp) {

        super(
            jsp,
            A_CmsListResourceTypeDialog.LIST_ID,
            Messages.get().container(Messages.GUI_CHTYPE_PLEASE_SELECT_0),
            A_CmsListResourceTypeDialog.LIST_COLUMN_NAME,
            CmsListOrderEnum.ORDER_ASCENDING,
            null);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsChtype(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Uploads the specified file and replaces the VFS file.<p>
     *
     * @throws JspException if inclusion of error dialog fails
     */
    public void actionChtype() throws JspException {

        int plainId;
        try {
            plainId = OpenCms.getResourceManager().getResourceType(
                CmsResourceTypePlain.getStaticTypeName()).getTypeId();
        } catch (CmsLoaderException e) {
            // this should really never happen
            plainId = CmsResourceTypePlain.getStaticTypeId();
        }
        try {
            int newType = plainId;
            try {
                // get new resource type id from request
                newType = Integer.parseInt(getParamSelectedType());
            } catch (NumberFormatException nf) {
                throw new CmsException(Messages.get().container(Messages.ERR_GET_RESTYPE_1, getParamSelectedType()));
            }
            // check the resource lock state
            checkLock(getParamResource());
            // change the resource type
            getCms().chtype(getParamResource(), newType);
            // close the dialog window
            actionCloseDialog();
        } catch (Throwable e) {
            // error changing resource type, show error dialog
            includeErrorpage(this, e);
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#actionDialog()
     */
    @Override
    public void actionDialog() throws JspException, ServletException, IOException {

        if (getAction() == ACTION_OK) {
            actionChtype();
            return;
        } else if (getAction() == ACTION_ADVANCED) {
            refreshList();
            return;
        }

        super.actionDialog();
    }

    /**
     * Builds a default button row with a continue and cancel button.<p>
     *
     * Override this to have special buttons for your dialog.<p>
     *
     * @return the button row
     */
    @Override
    public String dialogButtons() {

        return dialogButtonsOkAdvancedCancel(
            " onclick=\"submitChtype(form);\"",
            " onclick=\"submitAdvanced(form);\"",
            null);
    }

    /**
     * Builds a button row with an optional "ok", "advanced" and a "cancel" button.<p>
     *
     * @param okAttrs optional attributes for the ok button
     * @param advancedAttrs optional attributes for the advanced button
     * @param cancelAttrs optional attributes for the cancel button
     * @return the button row
     */
    public String dialogButtonsOkAdvancedCancel(String okAttrs, String advancedAttrs, String cancelAttrs) {

        if (!m_advancedMode && m_limitedRestypes && OpenCms.getRoleManager().hasRole(getCms(), CmsRole.VFS_MANAGER)) {
            return dialogButtons(
                new int[] {BUTTON_OK, BUTTON_ADVANCED, BUTTON_CANCEL},
                new String[] {okAttrs, advancedAttrs, cancelAttrs});
        } else {
            return dialogButtonsOkCancel(okAttrs, cancelAttrs);
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListResourceTypeDialog#getParamSelectedType()
     */
    @Override
    public String getParamSelectedType() {

        String item = super.getParamSelectedType();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(item)) {

            // determine resource type id of resource to change
            try {
                CmsResource res = getCms().readResource(getParamResource(), CmsResourceFilter.ALL);
                return Integer.toString(res.getTypeId());
            } catch (CmsException e) {
                // do nothing
            }
        }

        return item;
    }

    /**
     * Returns the html code to add directly before the list inside the form element.<p>
     *
     * @return the html code to add directly before the list inside the form element
     */
    @Override
    protected String customHtmlBeforeList() {

        StringBuffer result = new StringBuffer(256);

        result.append(dialogBlockStart(null));
        result.append(key(Messages.GUI_LABEL_TITLE_0));
        result.append(": ");
        result.append(getJsp().property("Title", getParamResource(), ""));
        result.append("<br>");
        result.append(key(Messages.GUI_LABEL_STATE_0));
        result.append(": ");
        try {
            result.append(getState());
        } catch (CmsException e) {
            // not so important ... just go on
        }
        result.append("<br>");
        result.append(key(Messages.GUI_LABEL_PERMALINK_0));
        result.append(": ");
        result.append(OpenCms.getLinkManager().getPermalink(getCms(), getParamResource()));
        result.append(dialogBlockEnd());
        result.append(dialogSpacer());

        return result.toString();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#customHtmlStart()
     */
    @Override
    protected String customHtmlStart() {

        StringBuffer result = new StringBuffer(256);
        result.append(super.customHtmlStart());

        result.append("<script type='text/javascript'>\n");

        result.append("function submitAdvanced(theForm) {\n");
        result.append("\ttheForm.action.value = \"" + CmsNewResource.DIALOG_ADVANCED + "\";\n");
        result.append("\ttheForm.submit();\n");
        result.append("}\n\n");

        result.append("function submitChtype(theForm) {\n");
        result.append("\ttheForm.action.value = \"" + DIALOG_OK + "\";\n");
        result.append("\ttheForm.submit();\n");
        result.append("}\n\n");

        result.append("</script>");

        return result.toString();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    @Override
    protected List<CmsListItem> getListItems() throws CmsException {

        List<CmsListItem> ret = new ArrayList<CmsListItem>();

        // fill the list with available resource types if they are limited
        List<String> availableResTypes = new ArrayList<String>();
        if (!m_advancedMode && m_limitedRestypes) {
            if (m_availableResTypes.indexOf(CmsNewResource.DELIM_PROPERTYVALUES) > -1) {
                availableResTypes = CmsStringUtil.splitAsList(
                    m_availableResTypes,
                    CmsNewResource.DELIM_PROPERTYVALUES,
                    true);
            } else {
                availableResTypes = CmsStringUtil.splitAsList(
                    m_availableResTypes,
                    CmsProperty.VALUE_LIST_DELIMITER,
                    true);
            }
        }

        // get current Cms object
        CmsObject cms = getCms();

        // determine resource type id of resource to change
        CmsResource res = cms.readResource(getParamResource(), CmsResourceFilter.ALL);

        // get all available explorer type settings
        List<CmsExplorerTypeSettings> resTypes = OpenCms.getWorkplaceManager().getExplorerTypeSettings();
        boolean isFolder = res.isFolder();

        // loop through all visible resource types
        for (int i = 0; i < resTypes.size(); i++) {
            boolean changeable = false;

            // get explorer type settings for current resource type
            CmsExplorerTypeSettings settings = resTypes.get(i);

            // only if settings is a real resource type
            boolean isResourceType;
            I_CmsResourceType type = new CmsResourceTypePlain();
            try {
                type = OpenCms.getResourceManager().getResourceType(settings.getName());
                isResourceType = true;
            } catch (CmsLoaderException e) {
                isResourceType = false;
            }

            if (isResourceType) {
                // first check if types are limited
                if (!m_advancedMode && m_limitedRestypes && (availableResTypes.indexOf(type.getTypeName()) == -1)) {
                    // this resource type is not in the list of available types, skip it
                    continue;
                }

                int resTypeId = OpenCms.getResourceManager().getResourceType(settings.getName()).getTypeId();
                // determine if this resTypeId is changeable by currentResTypeId

                // changeable is true if current resource is a folder and this resource type also
                if (isFolder && OpenCms.getResourceManager().getResourceType(resTypeId).isFolder()) {
                    changeable = true;
                } else if (!isFolder && !OpenCms.getResourceManager().getResourceType(resTypeId).isFolder()) {

                    // changeable is true if current resource is NOT a folder and this resource type also NOT
                    changeable = true;
                }

                if (changeable) {

                    // determine if this resource type is editable for the current user
                    CmsPermissionSet permissions = settings.getAccess().getPermissions(cms, res);
                    if (!permissions.requiresWritePermission() || !permissions.requiresControlPermission()) {

                        // skip resource types without required write or create permissions
                        continue;
                    }

                    // add found setting to list
                    CmsListItem item = getList().newItem(Integer.toString(resTypeId));
                    item.set(LIST_COLUMN_NAME, key(settings.getKey()));
                    item.set(
                        LIST_COLUMN_ICON,
                        "<img src=\""
                            + getSkinUri()
                            + CmsWorkplace.RES_PATH_FILETYPES
                            + settings.getIcon()
                            + "\" style=\"width: 16px; height: 16px;\" />");
                    ret.add(item);
                }
            }
        }

        return ret;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // first call of dialog
        setAction(ACTION_DEFAULT);

        super.initWorkplaceRequestValues(settings, request);

        // check the required permissions to change the resource type
        if (!checkResourcePermissions(CmsPermissionSet.ACCESS_WRITE, false)) {
            // no write permissions for the resource, set cancel action to close dialog
            setParamAction(DIALOG_CANCEL);
        }

        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        // set the action for the JSP switch
        if (DIALOG_OK.equals(getParamAction())) {
            // ok button pressed, change file type
            setAction(ACTION_OK);
            getJsp().getRequest().getSession(true).removeAttribute(SESSION_ATTR_ADVANCED);
        } else if (DIALOG_LOCKS_CONFIRMED.equals(getParamAction())) {
            setAction(ACTION_LOCKS_CONFIRMED);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            // cancel button pressed
            setAction(ACTION_CANCEL);
            getJsp().getRequest().getSession(true).removeAttribute(SESSION_ATTR_ADVANCED);
        } else {
            // build title for change file type dialog
            setParamTitle(key(Messages.GUI_CHTYPE_1, new Object[] {CmsResource.getName(getParamResource())}));
        }

        // get session attribute storing if we are in advanced mode
        String sessionAttr = (String)request.getSession(true).getAttribute(SESSION_ATTR_ADVANCED);
        if (CmsNewResource.DIALOG_ADVANCED.equals(getParamAction()) || (sessionAttr != null)) {
            // advanced mode to display all possible resource types
            if (sessionAttr == null) {
                // set attribute that we are in advanced mode
                request.getSession(true).setAttribute(SESSION_ATTR_ADVANCED, "true");
                setAction(ACTION_ADVANCED);
            }
            m_advancedMode = true;
        } else {
            // check for presence of property limiting the new resource types to create
            String newResTypesProperty = "";
            try {
                newResTypesProperty = getCms().readPropertyObject(
                    getParamResource(),
                    CmsPropertyDefinition.PROPERTY_RESTYPES_AVAILABLE,
                    true).getValue();
            } catch (CmsException e) {
                // ignore this exception, this is a minor issue
            }
            if (CmsStringUtil.isNotEmpty(newResTypesProperty)
                && !newResTypesProperty.equals(CmsNewResource.VALUE_DEFAULT)) {
                m_limitedRestypes = true;
                m_availableResTypes = newResTypesProperty;
            }
        }
    }

}
