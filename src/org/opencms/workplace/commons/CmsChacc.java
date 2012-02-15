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

package org.opencms.workplace.commons;

import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.history.CmsHistoryPrincipal;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.lock.CmsLockFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsAccessControlList;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsPrincipal;
import org.opencms.security.CmsRole;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.widgets.CmsPrincipalWidget;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Provides methods for building the permission settings dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/commons/chacc.jsp
 * </ul>
 * <p>
 * 
 * @since 6.0.0 
 */
public class CmsChacc extends CmsDialog {

    /** Value for the action: add an access control entry. */
    public static final int ACTION_ADDACE = 300;

    /** Value for the action: delete the permissions. */
    public static final int ACTION_DELETE = 200;

    /** Value for the action: set the internal use flag. */
    public static final int ACTION_INTERNALUSE = 400;

    /** Request parameter value for the action: add an access control entry. */
    public static final String DIALOG_ADDACE = "addace";

    /** Request parameter value for the action: delete the permissions. */
    public static final String DIALOG_DELETE = "delete";

    /** Request parameter value for the action: set the internal use flag. */
    public static final String DIALOG_INTERNALUSE = "internaluse";

    /** The dialog type. */
    public static final String DIALOG_TYPE = "chacc";

    /** Request parameter name for the inherit permissions parameter. */
    public static final String PARAM_INHERIT = "inherit";

    /** Request parameter name for the internal use only flag. */
    public static final String PARAM_INTERNAL = "internal";

    /** Request parameter name for the name parameter. */
    public static final String PARAM_NAME = "name";

    /** Request parameter name for the overwrite inherited permissions parameter. */
    public static final String PARAM_OVERWRITEINHERITED = "overwriteinherited";

    /** Request parameter name for the responsible parameter. */
    public static final String PARAM_RESPONSIBLE = "responsible";

    /** Request parameter name for the type parameter. */
    public static final String PARAM_TYPE = "type";

    /** Request parameter name for the view parameter. */
    public static final String PARAM_VIEW = "view";

    /** Constant for the request parameters suffix: allow. */
    public static final String PERMISSION_ALLOW = "allow";

    /** Constant for the request parameters suffix: deny. */
    public static final String PERMISSION_DENY = "deny";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsChacc.class);

    private static final String UNKNOWN_TYPE = "Unknown";

    /** PermissionSet of the current user for the resource. */
    private CmsPermissionSet m_curPermissions;

    /** Indicates if forms are editable by current user. */
    private boolean m_editable;

    /** Stores eventual error message Strings. */
    private List m_errorMessages = new ArrayList();

    /** Indicates if inheritance flags are set as hidden fields for resource folders. */
    private boolean m_inherit;

    /** The name parameter. */
    private String m_paramName;

    /** The type parameter. */
    private String m_paramType;

    private String m_paramUuid;

    /** Stores all possible permission keys of a permission set. */
    private Set m_permissionKeys = CmsPermissionSet.getPermissionKeys();

    /** Marks if the inherited permissions information should be displayed. */
    private boolean m_showInheritedPermissions;

    /** The possible types of new access control entries. */
    private String[] m_types = {
        I_CmsPrincipal.PRINCIPAL_GROUP,
        I_CmsPrincipal.PRINCIPAL_USER,
        CmsRole.PRINCIPAL_ROLE,
        CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_NAME,
        CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_NAME};

    /** The possible type values of access control entries. */
    private int[] m_typesInt = {
        CmsAccessControlEntry.ACCESS_FLAGS_GROUP,
        CmsAccessControlEntry.ACCESS_FLAGS_USER,
        CmsAccessControlEntry.ACCESS_FLAGS_ROLE,
        CmsAccessControlEntry.ACCESS_FLAGS_ALLOTHERS,
        CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE_ALL};

    /** The possible localized types of new access control entries. */
    private String[] m_typesLocalized = new String[5];

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsChacc(CmsJspActionElement jsp) {

        super(jsp);
        m_errorMessages.clear();
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsChacc(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Builds a detail view selector.<p>
     * 
     * @param wp the dialog object
     * @return the HTML code for the detail view selector
     */
    public static String buildSummaryDetailsButtons(CmsDialog wp) {

        StringBuffer result = new StringBuffer(512);
        // create detail view selector 
        result.append("<table border=\"0\">\n<tr>\n\t<td>");
        result.append(wp.key(Messages.GUI_PERMISSION_SELECT_VIEW_0));
        result.append("</td>\n");
        String selectedView = wp.getSettings().getPermissionDetailView();
        result.append("\t<form action=\"").append(wp.getDialogUri()).append(
            "\" method=\"post\" name=\"selectshortview\">\n");
        result.append("\t<td>\n");
        result.append("\t<input type=\"hidden\" name=\"");
        result.append(PARAM_VIEW);
        result.append("\" value=\"short\">\n");
        // set parameters to show correct hidden input fields
        wp.setParamAction(null);
        result.append(wp.paramsAsHidden());
        result.append("\t<input  type=\"submit\" class=\"dialogbutton\" value=\"").append(
            wp.key(Messages.GUI_LABEL_SUMMARY_0)).append("\"");
        if (!"long".equals(selectedView)) {
            result.append(" disabled=\"disabled\"");
        }
        result.append(">\n");
        result.append("\t</td>\n");
        result.append("\t</form>\n\t<form action=\"").append(wp.getDialogUri()).append(
            "\" method=\"post\" name=\"selectlongview\">\n");
        result.append("\t<td>\n");
        result.append("\t<input type=\"hidden\" name=\"");
        result.append(PARAM_VIEW);
        result.append("\" value=\"long\">\n");
        result.append(wp.paramsAsHidden());
        result.append("\t<input type=\"submit\" class=\"dialogbutton\" value=\"").append(
            wp.key(Messages.GUI_LABEL_DETAILS_0)).append("\"");
        if ("long".equals(selectedView)) {
            result.append(" disabled=\"disabled\"");
        }
        result.append(">\n");
        result.append("\t</td>\n\t</form>\n");
        result.append("</tr>\n</table>\n");
        return result.toString();
    }

    /**
     * Adds a new access control entry to the resource.<p>
     * 
     * @return true if a new ace was created, otherwise false
     */
    public boolean actionAddAce() {

        String file = getParamResource();
        String name = getParamName();
        String type = getParamType();
        int arrayPosition = -1;
        try {
            arrayPosition = Integer.parseInt(type);
        } catch (Exception e) {
            // can usually be ignored
            if (LOG.isInfoEnabled()) {
                LOG.info(e.getLocalizedMessage());
            }
        }

        if (checkNewEntry(name, arrayPosition)) {
            String permissionString = "";
            if (getInheritOption() && getSettings().getUserSettings().getDialogPermissionsInheritOnFolder()) {
                // inherit permissions on folders if setting is enabled
                permissionString = "+i";
            }
            try {
                // lock resource if autolock is enabled
                checkLock(getParamResource());
                if (name.equals(key(Messages.GUI_LABEL_ALLOTHERS_0))) {
                    getCms().chacc(
                        file,
                        getTypes(false)[arrayPosition],
                        CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_NAME,
                        permissionString);
                } else if (name.equals(key(Messages.GUI_LABEL_OVERWRITEALL_0))) {
                    getCms().chacc(
                        file,
                        getTypes(false)[arrayPosition],
                        CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_NAME,
                        permissionString);
                } else {
                    if (getTypes(false)[arrayPosition].equalsIgnoreCase(CmsRole.PRINCIPAL_ROLE)) {
                        // if role, first check if we have to translate the role name  
                        CmsRole role = CmsRole.valueOfRoleName(name);
                        if (role == null) {
                            // we need translation
                            Iterator it = CmsRole.getSystemRoles().iterator();
                            while (it.hasNext()) {
                                role = (CmsRole)it.next();
                                if (role.getName(getLocale()).equalsIgnoreCase(name)) {
                                    name = role.getRoleName();
                                    break;
                                }
                            }
                        }
                    }
                    getCms().chacc(file, getTypes(false)[arrayPosition], name, permissionString);
                }
                return true;
            } catch (CmsException e) {
                m_errorMessages.add(e.getMessage());
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        return false;
    }

    /**
     * Modifies the Internal Use flag of a resource.<p>
     * @param request the Http servlet request
     * 
     * @return true if the operation was was successfully removed, otherwise false
     */
    public boolean actionInternalUse(HttpServletRequest request) {

        String internal = request.getParameter(PARAM_INTERNAL);

        CmsResource resource;
        boolean internalValue = false;
        if (internal != null) {
            internalValue = true;
        }
        try {
            resource = getCms().readResource(getParamResource(), CmsResourceFilter.ALL);

            int flags = resource.getFlags();

            if (internalValue) {
                flags |= CmsResource.FLAG_INTERNAL;
            } else {
                flags &= ~CmsResource.FLAG_INTERNAL;
            }

            getCms().lockResource(getParamResource());
            getCms().chflags(getParamResource(), flags);

        } catch (CmsException e) {
            m_errorMessages.add(key(Messages.ERR_MODIFY_INTERNAL_FLAG_0));
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            return false;
        }
        return true;
    }

    /**
     * Modifies a present access control entry for a resource.<p>
     * 
     * @param request the Http servlet request
     * @return true if the modification worked, otherwise false 
     */
    public boolean actionModifyAce(HttpServletRequest request) {

        String file = getParamResource();

        // get request parameters
        String name = getParamName();
        String type = getParamType();
        String inherit = request.getParameter(PARAM_INHERIT);
        String overWriteInherited = request.getParameter(PARAM_OVERWRITEINHERITED);
        String responsible = request.getParameter(PARAM_RESPONSIBLE);

        // get the new permissions
        Set permissionKeys = CmsPermissionSet.getPermissionKeys();
        int allowValue = 0;
        int denyValue = 0;
        String key, param;
        int value, paramInt;

        Iterator i = permissionKeys.iterator();
        // loop through all possible permissions 
        while (i.hasNext()) {
            key = (String)i.next();
            value = CmsPermissionSet.getPermissionValue(key);
            // set the right allowed and denied permissions from request parameters
            try {
                param = request.getParameter(value + PERMISSION_ALLOW);
                paramInt = Integer.parseInt(param);
                allowValue |= paramInt;
            } catch (Exception e) {
                // can usually be ignored
                if (LOG.isInfoEnabled()) {
                    LOG.info(e.getLocalizedMessage());
                }
            }
            try {
                param = request.getParameter(value + PERMISSION_DENY);
                paramInt = Integer.parseInt(param);
                denyValue |= paramInt;
            } catch (Exception e) {
                // can usually be ignored
                if (LOG.isInfoEnabled()) {
                    LOG.info(e.getLocalizedMessage());
                }
            }
        }

        // get the current Ace to get the current ace flags
        try {
            List allEntries = getCms().getAccessControlEntries(file, false);
            int flags = 0;
            for (int k = 0; k < allEntries.size(); k++) {
                CmsAccessControlEntry curEntry = (CmsAccessControlEntry)allEntries.get(k);
                String curType = getEntryType(curEntry.getFlags(), false);
                I_CmsPrincipal p;
                try {
                    p = CmsPrincipal.readPrincipalIncludingHistory(getCms(), curEntry.getPrincipal());
                } catch (CmsException e) {
                    p = null;
                }
                if (((p != null) && p.getName().equals(name) && curType.equals(type))) {
                    flags = curEntry.getFlags();
                    break;
                } else if (p == null) {
                    // check if it is the case of a role
                    CmsRole role = CmsRole.valueOfId(curEntry.getPrincipal());
                    if ((role != null) && name.equals(role.getRoleName())) {
                        flags = curEntry.getFlags();
                        break;
                    } else if ((curEntry.getPrincipal().equals(CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_ID) && name.equals(CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_NAME))
                        || (curEntry.getPrincipal().equals(CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_ID) && name.equals(CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_NAME))) {
                        flags = curEntry.getFlags();
                        break;
                    }
                }
            }

            // modify the ace flags to determine inheritance of the current ace
            if (Boolean.valueOf(inherit).booleanValue()) {
                flags |= CmsAccessControlEntry.ACCESS_FLAGS_INHERIT;
            } else {
                flags &= ~CmsAccessControlEntry.ACCESS_FLAGS_INHERIT;
            }

            // modify the ace flags to determine overwriting of inherited ace
            if (Boolean.valueOf(overWriteInherited).booleanValue()) {
                flags |= CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE;
            } else {
                flags &= ~CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE;
            }

            if (Boolean.valueOf(responsible).booleanValue()) {
                flags |= CmsAccessControlEntry.ACCESS_FLAGS_RESPONSIBLE;
            } else {
                flags &= ~CmsAccessControlEntry.ACCESS_FLAGS_RESPONSIBLE;
            }

            // lock resource if autolock is enabled
            checkLock(getParamResource());
            // try to change the access entry   
            if (name.equals(CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_ID.toString())) {
                getCms().chacc(
                    file,
                    type,
                    CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_NAME,
                    allowValue,
                    denyValue,
                    flags);
            } else if (name.equals(CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_ID.toString())) {
                getCms().chacc(
                    file,
                    type,
                    CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_NAME,
                    allowValue,
                    denyValue,
                    flags);
            } else {
                getCms().chacc(file, type, name, allowValue, denyValue, flags);
            }
            return true;
        } catch (CmsException e) {
            m_errorMessages.add(key(Messages.ERR_CHACC_MODIFY_ENTRY_0));
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            return false;
        }
    }

    /**
     * Removes a present access control entry from the resource.<p>
     * 
     * @return true if the ace was successfully removed, otherwise false
     */
    public boolean actionRemoveAce() {

        String file = getParamResource();
        String name = getParamName();
        String type = getParamType();
        String uuid = getParamUuid();
        try {
            // lock resource if autolock is enabled
            checkLock(getParamResource());
            // check if it is the case of a role
            CmsRole role = CmsRole.valueOfGroupName(name);
            if (role != null) {
                // translate the internal group name to a role name
                name = role.getFqn();
            }
            try {
                getCms().rmacc(file, type, name);
            } catch (CmsException e) {
                LOG.warn(e.getLocalizedMessage(), e);
                getCms().rmacc(file, type, uuid);
            }
            return true;
        } catch (CmsException e) {
            m_errorMessages.add(key(Messages.ERR_CHACC_DELETE_ENTRY_0));
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            return false;
        }
    }

    /**
     * Builds a String with HTML code to display the users access rights for the current resource.<p>
     * 
     * @return HTML String with the access rights of the current user
     */
    public String buildCurrentPermissions() {

        StringBuffer result = new StringBuffer(dialogToggleStart(
            key(Messages.GUI_PERMISSION_USER_0),
            "userpermissions",
            getSettings().getUserSettings().getDialogExpandUserPermissions()));
        result.append(dialogWhiteBoxStart());
        try {
            result.append(buildPermissionEntryForm(
                getSettings().getUser().getId(),
                buildPermissionsForCurrentUser(),
                false,
                false));
        } catch (CmsException e) {
            // should never happen
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        result.append(dialogWhiteBoxEnd());
        result.append("</div>\n");
        return result.toString();
    }

    /**
     * Returns the error messages if something went wrong.<p>
     *  
     * @return all error messages
     */
    public String buildErrorMessages() {

        StringBuffer result = new StringBuffer(8);
        String errorMessages = getErrorMessagesString();
        if (!"".equals(errorMessages)) {
            result.append(dialogBlock(HTML_START, key(Messages.GUI_PERMISSION_ERROR_0), true));
            result.append("<font color='red'>").append(errorMessages).append("</font>");
            result.append(dialogBlockEnd());
        }
        return result.toString();
    }

    /**
     * Builds a String with HTML code to display the responsibles of a resource.<p>
     * 
     * @param show true the responsible list is open
     * @return HTML code for the responsibles of the current resource
     */
    public String buildResponsibleList(boolean show) {

        List parentResources = new ArrayList();
        Map responsibles = new HashMap();
        CmsObject cms = getCms();
        try {
            // get all parent folders of the current file
            parentResources = cms.readPath(getParamResource(), CmsResourceFilter.IGNORE_EXPIRATION);
        } catch (CmsException e) {
            // can usually be ignored
            if (LOG.isInfoEnabled()) {
                LOG.info(e.getLocalizedMessage());
            }
        }

        String resourceRootPath = cms.getRequestContext().addSiteRoot(getParamResource());
        String site = cms.getRequestContext().getSiteRoot();
        try {
            cms.getRequestContext().setSiteRoot("");
            Iterator i = parentResources.iterator();
            while (i.hasNext()) {
                CmsResource resource = (CmsResource)i.next();
                try {
                    String rootPath = resource.getRootPath();
                    Iterator entries = cms.getAccessControlEntries(rootPath, false).iterator();
                    while (entries.hasNext()) {
                        CmsAccessControlEntry ace = (CmsAccessControlEntry)entries.next();
                        if (ace.isResponsible()) {
                            try {
                                responsibles.put(
                                    CmsPrincipal.readPrincipalIncludingHistory(cms, ace.getPrincipal()),
                                    rootPath);
                            } catch (CmsDbEntryNotFoundException e) {
                                responsibles.put(ace.getPrincipal(), rootPath);
                            }
                        }
                    }
                } catch (CmsException e) {
                    // can usually be ignored
                    if (LOG.isInfoEnabled()) {
                        LOG.info(e.getLocalizedMessage());
                    }
                }
            }
            if (responsibles.size() == 0) {
                return key(Messages.GUI_AVAILABILITY_NO_RESPONSIBLES_0);
            }
            StringBuffer result = new StringBuffer(512);
            result.append(dialogToggleStart(key(Messages.GUI_AVAILABILITY_RESPONSIBLES_0), "responsibles", show));

            result.append(dialogWhiteBoxStart());
            i = responsibles.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry entry = (Map.Entry)i.next();
                String name;
                String ou = null;
                String image;
                if (entry.getKey() instanceof I_CmsPrincipal) {
                    I_CmsPrincipal principal = (I_CmsPrincipal)entry.getKey();
                    if (principal instanceof CmsHistoryPrincipal) {
                        if (principal.isGroup()) {
                            name = ((CmsHistoryPrincipal)principal).getDescription()
                                + " ("
                                + principal.getSimpleName()
                                + ")";
                            image = "commons/group.png";
                        } else {
                            name = ((CmsHistoryPrincipal)principal).getName();
                            image = "commons/user.png";
                        }
                    } else if (principal instanceof CmsGroup) {
                        name = ((CmsGroup)principal).getDescription(getLocale())
                            + " ("
                            + principal.getSimpleName()
                            + ")";
                        image = "commons/group.png";
                    } else {
                        name = ((CmsUser)principal).getFullName();
                        image = "commons/user.png";
                    }
                    ou = principal.getOuFqn();
                } else {
                    // check if it is the case of a role
                    CmsRole role = CmsRole.valueOfId((CmsUUID)entry.getKey());
                    if (role != null) {
                        name = role.getName(getLocale());
                        image = "commons/role.png";
                    } else {
                        name = entry.getKey().toString();
                        image = "explorer/project_none.gif";
                    }
                }
                result.append("<div class=\"dialogrow\"><img src=\"");
                result.append(getSkinUri());
                result.append(image);
                result.append("\" class='noborder' width='16' height='16' alt='Principal' title='Principal'>&nbsp;<span class=\"textbold\">");
                result.append(name);
                result.append("</span>");
                if ("long".equals(getSettings().getPermissionDetailView())) {
                    String resourceName = (String)entry.getValue();
                    if (!resourceRootPath.equals(resourceName)) {
                        result.append("<div class=\"dialogpermissioninherit\">");
                        result.append(key(Messages.GUI_PERMISSION_INHERITED_FROM_1, new Object[] {resourceName}));
                        result.append("</div>");
                    }
                }
                try {
                    if ((ou != null)
                        && (OpenCms.getOrgUnitManager().getOrganizationalUnits(getCms(), "", true).size() > 1)) {
                        result.append("<br>");
                        result.append("<img src='").append(getSkinUri()).append(
                            "explorer/project_none.gif' class='noborder' width='16' height='16' >");
                        result.append("<img src='").append(getSkinUri()).append(
                            "explorer/project_none.gif' class='noborder' width='16' height='16' >");
                        result.append("&nbsp;");
                        try {
                            result.append(OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), ou).getDisplayName(
                                getLocale()));
                        } catch (CmsException e) {
                            result.append(ou);
                        }
                    }
                } catch (CmsException e) {
                    // should never happen
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
                result.append("</div>\n");
            }
            result.append(dialogWhiteBoxEnd());
            result.append("</div>\n");
            return result.toString();
        } finally {
            cms.getRequestContext().setSiteRoot(site);
        }
    }

    /**
     * Builds a String with HTML code to display the inherited and own access control entries of a resource.<p>
     * 
     * @return HTML code for inherited and own entries of the current resource
     */
    public String buildRightsList() {

        StringBuffer result = new StringBuffer(dialogToggleStart(
            key(Messages.GUI_PERMISSION_BEQUEATH_SUBFOLDER_0),
            "inheritedpermissions",
            getSettings().getUserSettings().getDialogExpandInheritedPermissions() || getShowInheritedPermissions()));

        // store all parent folder ids together with path in a map
        Map parents = new HashMap();
        String path = CmsResource.getParentFolder(getParamResource());
        List parentResources = new ArrayList();
        try {
            // get all parent folders of the current file
            parentResources = getCms().readPath(path, CmsResourceFilter.IGNORE_EXPIRATION);
        } catch (CmsException e) {
            // can usually be ignored
            if (LOG.isInfoEnabled()) {
                LOG.info(e.getLocalizedMessage());
            }
        }
        Iterator k = parentResources.iterator();
        while (k.hasNext()) {
            // add the current folder to the map
            CmsResource curRes = (CmsResource)k.next();
            parents.put(curRes.getResourceId(), curRes.getRootPath());
        }

        // create new ArrayLists in which inherited and non inherited entries are stored
        ArrayList ownEntries = new ArrayList();
        try {
            Iterator itAces = getCms().getAccessControlEntries(getParamResource(), false).iterator();
            while (itAces.hasNext()) {
                CmsAccessControlEntry curEntry = (CmsAccessControlEntry)itAces.next();
                if (!curEntry.isInherited()) {
                    // add the entry to the own rights list
                    ownEntries.add(curEntry);
                }
            }
        } catch (CmsException e) {
            // can usually be ignored
            if (LOG.isInfoEnabled()) {
                LOG.info(e.getLocalizedMessage());
            }
        }

        ArrayList inheritedEntries = new ArrayList();
        try {
            Iterator itAces = getCms().getAccessControlEntries(path, true).iterator();
            while (itAces.hasNext()) {
                CmsAccessControlEntry curEntry = (CmsAccessControlEntry)itAces.next();
                // add the entry to the inherited rights list for the "long" view
                if ("long".equals(getSettings().getPermissionDetailView())) {
                    inheritedEntries.add(curEntry);
                }
            }
        } catch (CmsException e) {
            // can usually be ignored
            if (LOG.isInfoEnabled()) {
                LOG.info(e.getLocalizedMessage());
            }
        }

        // now create the inherited entries box
        result.append(dialogWhiteBox(HTML_START));
        result.append(buildInheritedList(inheritedEntries, parents));
        result.append(dialogWhiteBox(HTML_END));

        // close div that toggles visibility of inherited permissions
        result.append("</div>");

        // create the add user/group form
        result.append(buildAddForm());

        // create the resource entries box
        result.append(buildResourceList(ownEntries));

        return result.toString();
    }

    /**
     * Returns the current users permission set on the resource.<p>
     * 
     * @return the users permission set
     */
    public CmsPermissionSet getCurPermissions() {

        return m_curPermissions;
    }

    /**
     * Returns a list with all error messages which occurred when trying to add a new access control entry.<p>
     * 
     * @return List of error message Strings
     */
    public List getErrorMessages() {

        return m_errorMessages;
    }

    /**
     * Returns a String with all error messages occuring when trying to add a new access control entry.<p>
     * 
     * @return String with error messages, separated by &lt;br&gt;
     */
    public String getErrorMessagesString() {

        StringBuffer errors = new StringBuffer(8);
        Iterator i = getErrorMessages().iterator();
        while (i.hasNext()) {
            errors.append((String)i.next());
            if (i.hasNext()) {
                errors.append("<br>");
            }
        }
        return errors.toString();
    }

    /**
     * Returns the value of the name parameter, 
     * or null if this parameter was not provided.<p>
     * 
     * The name parameter stores the name of the group or user.<p>
     * 
     * @return the value of the name parameter
     */
    public String getParamName() {

        return m_paramName;
    }

    /**
     * Returns the value of the type parameter, 
     * or null if this parameter was not provided.<p>
     * 
     * The type parameter stores the type of an ace (group or user).<p>
     * 
     * @return the value of the type parameter
     */
    public String getParamType() {

        return m_paramType;
    }

    public String getParamUuid() {

        return m_paramUuid;
    }

    /**
     * Returns if the inherited permissions information should be displayed.<p>
     *
     * @return true if the inherited permissions information should be displayed, otherwise false
     */
    public boolean getShowInheritedPermissions() {

        return m_showInheritedPermissions;
    }

    /**
     * @see org.opencms.workplace.CmsDialog#htmlStart()
     */
    public String htmlStart() {

        StringBuffer result = new StringBuffer(256);
        result.append(super.htmlStart());
        result.append((new CmsPrincipalWidget().getDialogIncludes(getCms(), null)));
        result.append("<script type='text/javascript' >");
        result.append("typeField = '").append(PARAM_TYPE).append("';");
        result.append("</script>");
        return result.toString();
    }

    /**
     * Initializes some member variables to display the form with the right options for the current user.<p>
     * 
     * This method must be called after initWorkplaceRequestValues().<p>
     */
    public void init() {

        // the current user name
        String userName = getSettings().getUser().getName();

        if (m_typesLocalized[0] == null) {
            m_typesLocalized[0] = key(Messages.GUI_LABEL_GROUP_0);
            m_typesLocalized[1] = key(Messages.GUI_LABEL_USER_0);
            m_typesLocalized[2] = key(Messages.GUI_LABEL_ROLE_0);
            m_typesLocalized[3] = key(Messages.GUI_LABEL_ALLOTHERS_0);
            m_typesLocalized[4] = key(Messages.GUI_LABEL_OVERWRITEALL_0);
        }

        // set flags to show editable or non editable entries
        setEditable(false);
        setInheritOption(false);
        String resName = getParamResource();

        try {
            // get the current users' permissions
            setCurPermissions(getCms().getPermissions(getParamResource(), userName));

            // check if the current resource is a folder
            CmsResource resource = getCms().readResource(getParamResource(), CmsResourceFilter.ALL);
            if (resource.isFolder()) {
                // only folders have the inherit option activated
                setInheritOption(true);
                if (!resName.endsWith("/")) {
                    // append manually a "/" to folder name to avoid issues with check if resource is in project
                    resName += "/";
                }
            }
        } catch (CmsException e) {
            // can usually be ignored
            if (LOG.isInfoEnabled()) {
                LOG.info(e.getLocalizedMessage());
            }
        }

        // check the current users permission to change access control entries
        if ((!getCms().getRequestContext().getCurrentProject().isOnlineProject() && getCms().isInsideCurrentProject(
            resName))
            && (OpenCms.getRoleManager().hasRole(getCms(), CmsRole.VFS_MANAGER) || (((m_curPermissions.getAllowedPermissions() & CmsPermissionSet.PERMISSION_CONTROL) > 0) && !((m_curPermissions.getDeniedPermissions() & CmsPermissionSet.PERMISSION_CONTROL) > 0)))) {
            if (isBlockingLocked()) {
                m_errorMessages.add(key(Messages.ERR_PERMISSION_BLOCKING_LOCKS_0));
            } else {
                setEditable(true);
            }
        }
    }

    /**
     * Sets the value of the name parameter.<p>
     * 
     * @param value the value to set
     */
    public void setParamName(String value) {

        m_paramName = value;
    }

    /**
     * Sets the value of the type parameter.<p>
     * 
     * @param value the value to set
     */
    public void setParamType(String value) {

        m_paramType = value;
    }

    public void setParamUuid(String uuid) {

        m_paramUuid = uuid;
    }

    /**
     * Validates the user input when creating a new access control entry.<p>
     * 
     * @param name the name of the new user/group
     * @param arrayPosition the position in the types array
     * 
     * @return true if everything is ok, otherwise false
     */
    protected boolean checkNewEntry(String name, int arrayPosition) {

        m_errorMessages.clear();
        boolean inArray = false;
        if (getTypes(false)[arrayPosition] != null) {
            inArray = true;
        }
        if (!inArray) {
            m_errorMessages.add(key(Messages.ERR_PERMISSION_SELECT_TYPE_0));
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(name)) {
            m_errorMessages.add(key(Messages.ERR_MISSING_GROUP_OR_USER_NAME_0));
        }
        if (m_errorMessages.size() > 0) {
            return false;
        }
        return true;
    }

    /**
     * Returns the resource on which the specified access control entry was set.<p>
     * 
     * @param entry the current access control entry
     * @param parents the parent resources to determine the connected resource
     * @return the resource name of the corresponding resource
     */
    protected String getConnectedResource(CmsAccessControlEntry entry, Map parents) {

        CmsUUID resId = entry.getResource();
        String resName = (String)parents.get(resId);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(resName)) {
            return resName;
        }
        return resId.toString();
    }

    /**
     * Returns the current editable flag for the user to change ACEs.<p>
     * 
     * @return true if user can edit the permissions, otherwise false
     */
    protected boolean getEditable() {

        return m_editable;
    }

    /**
     * Determines the type of the current access control entry.<p>
     * 
     * @param flags the value of the current flags
     * @param all to include all types, or just user and groups 
     * 
     * @return String representation of the ace type
     */
    protected String getEntryType(int flags, boolean all) {

        for (int i = 0; i < getTypes(all).length; i++) {
            if ((flags & getTypesInt()[i]) > 0) {
                return getTypes(all)[i];
            }
        }
        return UNKNOWN_TYPE;
    }

    /**
     * Determines the int type of the current access control entry.<p>
     * 
     * @param flags the value of the current flags
     * @return int representation of the ace type as int
     */
    protected int getEntryTypeInt(int flags) {

        for (int i = 0; i < getTypesInt().length; i++) {
            if ((flags & getTypesInt()[i]) > 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns if the access control entry can be inherited to subfolders and can overwrite inherited permissions.<p>
     * 
     * @return true to show the checkbox, otherwise false
     */
    protected boolean getInheritOption() {

        return m_inherit;
    }

    /**
     * Returns a String array with the possible entry types.<p>
     * 
     * @param all to include all types, or just user, groups and roles 
     * 
     * @return the possible types
     */
    protected String[] getTypes(boolean all) {

        if (!all) {
            String[] array = new String[3];
            return Arrays.asList(m_types).subList(0, 3).toArray(array);
        }
        return m_types;
    }

    /**
     * Returns an int array with possible entry types.<p>
     * 
     * @return the possible types as int array
     */
    protected int[] getTypesInt() {

        return m_typesInt;
    }

    /**
     * Returns a String array with the possible localized entry types.<p>
     * 
     * @return the possible localized types
     */
    protected String[] getTypesLocalized() {

        return m_typesLocalized;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);

        // set the detail mode of the "inherited" list view
        String detail = request.getParameter(PARAM_VIEW);
        if (detail != null) {
            settings.setPermissionDetailView(detail);
            setShowInheritedPermissions(true);
        }

        // determine which action has to be performed
        if (DIALOG_TYPE.equals(getParamAction())) {
            setAction(ACTION_DEFAULT);
        } else if (DIALOG_SET.equals(getParamAction())) {
            setAction(ACTION_SET);
        } else if (DIALOG_DELETE.equals(getParamAction())) {
            setAction(ACTION_DELETE);
        } else if (DIALOG_ADDACE.equals(getParamAction())) {
            setAction(ACTION_ADDACE);
        } else if (DIALOG_LOCKS_CONFIRMED.equals(getParamAction())) {
            setAction(ACTION_LOCKS_CONFIRMED);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else if (DIALOG_INTERNALUSE.equals(getParamAction())) {
            setAction(ACTION_INTERNALUSE);
        } else {
            setAction(ACTION_DEFAULT);
            // build the title for chacc dialog     
            setParamTitle(key(Messages.GUI_PERMISSION_CHANGE_1, new Object[] {CmsResource.getName(getParamResource())}));
        }

    }

    /**
     * Checks if a certain permission of a permission set is allowed.<p>
     * 
     * @param p the current CmsPermissionSet
     * @param value the int value of the permission to check
     * @return true if the permission is allowed, otherwise false
     */
    protected boolean isAllowed(CmsPermissionSet p, int value) {

        if ((p.getAllowedPermissions() & value) > 0) {
            return true;
        }
        return false;
    }

    /**
     * Checks if a certain permission of a permission set is denied.<p>
     * 
     * @param p the current CmsPermissionSet
     * @param value the int value of the permission to check
     * @return true if the permission is denied, otherwise false
     */
    protected boolean isDenied(CmsPermissionSet p, int value) {

        if ((p.getDeniedPermissions() & value) > 0) {
            return true;
        }
        return false;
    }

    /**
     * Check if the current permissions are overwriting the inherited ones.<p>
     * 
     * @param flags value of all flags of the current entry
     * @return true if permissions are overwriting the inherited ones, otherwise false 
     */
    protected boolean isOverWritingInherited(int flags) {

        if ((flags & CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE) > 0) {
            return true;
        }
        return false;
    }

    /**
     * Check if the user is a responsible for the resource.<p>
     * 
     * @param flags value of all flags of the current entry
     * @return true if user is responsible for the resource, otherwise false 
     */
    protected boolean isResponsible(int flags) {

        if ((flags & CmsAccessControlEntry.ACCESS_FLAGS_RESPONSIBLE) > 0) {
            return true;
        }
        return false;
    }

    /**
     * Sets the current users permissions on the resource.
     * This is set in the init() method.<p>
     * 
     * @param value the CmsPermissionSet
     */
    protected void setCurPermissions(CmsPermissionSet value) {

        m_curPermissions = value;
    }

    /**
     * Sets the editable flag for the forms.
     * This is set in the init() method.<p>
     * 
     * @param value true if user can edit the permissions, otherwise false
     */
    protected void setEditable(boolean value) {

        m_editable = value;
    }

    /**
     * Sets if the access control entry can be inherited to subfolders and can overwrite inherited permissions.<p>
     * 
     * This is set in the init() method.<p>
     * 
     * @param value set to true for folders, otherwise false
     */
    protected void setInheritOption(boolean value) {

        m_inherit = value;
    }

    /**
     * Sets if the inherited permissions information should be displayed.<p>
     *
     * @param showInheritedPermissions true if the inherited permissions information should be displayed, otherwise false
     */
    protected void setShowInheritedPermissions(boolean showInheritedPermissions) {

        m_showInheritedPermissions = showInheritedPermissions;
    }

    /**
     * Builds a String with HTML code to display the form to add a new access control entry for the current resource.<p>
     * 
     * @return HTML String with the form
     */
    private String buildAddForm() {

        StringBuffer result = new StringBuffer(256);

        // only display form if the current user has the "control" right
        if (getEditable()) {
            result.append(dialogSpacer());
            result.append(dialogBlockStart(key(Messages.GUI_PERMISSION_ADD_ACE_0)));

            // get all possible entry types
            ArrayList options = new ArrayList();
            ArrayList optionValues = new ArrayList();
            for (int i = 0; i < (getTypes(false).length - (1 * (isRoleEditable() ? 0 : 1))); i++) {
                options.add(getTypesLocalized()[i]);
                optionValues.add(Integer.toString(i));
            }

            // create the input form for adding an ace
            result.append("<form action=\"").append(getDialogUri()).append(
                "\" method=\"post\" name=\"add\" class=\"nomargin\">\n");
            // set parameters to show correct hidden input fields
            setParamAction(DIALOG_ADDACE);
            setParamType(null);
            setParamName(null);
            result.append(paramsAsHidden());
            result.append("<table border=\"0\" width=\"100%\">\n");
            result.append("<tr>\n");
            result.append("\t<td>").append(buildSelect("name=\"" + PARAM_TYPE + "\"", options, optionValues, -1)).append(
                "</td>\n");
            result.append("\t<td class=\"maxwidth\"><input type=\"text\" class=\"maxwidth\" name=\"");
            result.append(PARAM_NAME);
            result.append("\" value=\"\"></td>\n");

            result.append("<td><span style='display: block; height: 1px; width: 10px;'/></td>");

            result.append(button(
                new CmsPrincipalWidget().getButtonJs(PARAM_NAME, "add"),
                null,
                "principal",
                org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_SEARCH_0,
                getSettings().getUserSettings().getEditorButtonStyle()));

            result.append(button(
                "javascript:document.forms['add'].submit();",
                null,
                "new",
                Messages.GUI_LABEL_ADD_0,
                getSettings().getUserSettings().getEditorButtonStyle()));

            result.append("</tr>\n");
            result.append("</form>\n");
            result.append("</table>\n");

            result.append(dialogBlockEnd());
        }
        return result.toString();
    }

    /**
     * Builds a StringBuffer with HTML code to show a list of all inherited access control entries.<p>
     * 
     * @param entries ArrayList with all entries to show for the long view
     * @param parents Map of parent resources needed to get the connected resources for the detailed view
     * @return StringBuffer with HTML code for all entries
     */
    private StringBuffer buildInheritedList(ArrayList entries, Map parents) {

        StringBuffer result = new StringBuffer(32);
        String view = getSettings().getPermissionDetailView();

        // display the long view
        if ("long".equals(view)) {
            Iterator i = entries.iterator();
            while (i.hasNext()) {
                CmsAccessControlEntry curEntry = (CmsAccessControlEntry)i.next();
                // build the list with enabled extended view and resource name
                result.append(buildPermissionEntryForm(curEntry, false, true, getConnectedResource(curEntry, parents)));
            }
        } else {
            // show the short view, use an ACL to build the list
            try {
                // get the inherited ACL of the parent folder 
                CmsAccessControlList acList = getCms().getAccessControlList(
                    CmsResource.getParentFolder(getParamResource()),
                    false);
                Iterator i = acList.getPrincipals().iterator();
                while (i.hasNext()) {
                    CmsUUID principalId = (CmsUUID)i.next();
                    if (!principalId.equals(CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_ID)) {
                        CmsPermissionSet permissions = acList.getPermissions(principalId);
                        // build the list with enabled extended view only
                        result.append(buildPermissionEntryForm(principalId, permissions, false, true));
                    }
                }
            } catch (CmsException e) {
                // can usually be ignored
                if (LOG.isInfoEnabled()) {
                    LOG.info(e.getLocalizedMessage());
                }
            }
        }
        return result;
    }

    /**
     * Builds a String with HTML code to display the form to add a new access control entry for the current resource.<p>
     * 
     * @return HTML String with the form
     */
    private String buildInternalForm() {

        StringBuffer result = new StringBuffer(128);

        CmsResource resource = null;
        boolean internal = false;

        // try to read the internal flag from the resource
        try {
            resource = getCms().readResource(getParamResource(), CmsResourceFilter.ALL);
            internal = resource.isInternal();
        } catch (CmsException e) {
            // an error occurred reading the resource 
            LOG.error(e.getLocalizedMessage());
        }

        if ((resource != null) && (resource.isFile())) {
            // only show internal check box on files
            result.append("<form action=\"").append(getDialogUri()).append(
                "\" method=\"post\" name=\"internal\" class=\"nomargin\">\n");
            result.append("<table border=\"0\" width=\"100%\">\n");
            result.append("<tr>\n");
            result.append("\t<td class=\"dialogpermissioncell\">").append(key(Messages.GUI_PERMISSION_INTERNAL_0));
            result.append(" <input type=\"checkbox\" name=\"");
            result.append(PARAM_INTERNAL);
            result.append("\" value=\"true\"");
            if (internal) {
                result.append(" checked=\"checked\"");
            }
            if (!getEditable()) {
                result.append(" disabled=\"disabled\"");
            }
            result.append(" ></td>\n");
            if (getEditable()) {
                result.append("<td><input  type=\"submit\" class=\"dialogbutton\" value=\"").append(
                    key(Messages.GUI_LABEL_SET_0)).append("\">");
            }
            result.append("</td>\n");
            result.append("</tr>\n");
            result.append("</table>\n");
            setParamAction(DIALOG_INTERNALUSE);
            setParamType(null);
            setParamName(null);
            result.append(paramsAsHidden());
            result.append("</form>\n");
        }
        return result.toString();

    }

    /**
     * Creates an HTML input form for the current access control entry.<p>
     * 
     * @param entry the current access control entry
     * @param editable boolean to determine if the form is editable
     * @param extendedView boolean to determine if the view is selectable with DHTML
     * @param inheritRes the resource name from which the ace is inherited
     * @return StringBuffer with HTML code of the form
     */
    private StringBuffer buildPermissionEntryForm(
        CmsAccessControlEntry entry,
        boolean editable,
        boolean extendedView,
        String inheritRes) {

        StringBuffer result = new StringBuffer(512);

        // get name and type of the current entry
        I_CmsPrincipal principal;
        try {
            principal = CmsPrincipal.readPrincipalIncludingHistory(getCms(), entry.getPrincipal());
        } catch (CmsException e) {
            principal = null;
        }

        String id = (principal != null) ? principal.getName() : entry.getPrincipal().toString();
        String name;
        String ou = null;
        int flags = 0;
        if ((principal != null) && (principal instanceof CmsHistoryPrincipal)) {
            // there is a history principal entry, handle it
            if (principal.isGroup()) {
                String niceName = OpenCms.getWorkplaceManager().translateGroupName(principal.getName(), false);
                name = key(org.opencms.security.Messages.GUI_ORGUNIT_DISPLAY_NAME_2, new Object[] {
                    ((CmsHistoryPrincipal)principal).getDescription(),
                    niceName});
                ou = CmsOrganizationalUnit.getParentFqn(id);
                flags = CmsAccessControlEntry.ACCESS_FLAGS_GROUP;
            } else {
                name = ((CmsHistoryPrincipal)principal).getName();
                ou = CmsOrganizationalUnit.getParentFqn(id);
                flags = CmsAccessControlEntry.ACCESS_FLAGS_USER;
            }
        } else if ((principal != null) && principal.isGroup()) {
            String niceName = OpenCms.getWorkplaceManager().translateGroupName(principal.getName(), false);
            name = key(
                org.opencms.security.Messages.GUI_ORGUNIT_DISPLAY_NAME_2,
                new Object[] {((CmsGroup)principal).getDescription(getLocale()), niceName});
            ou = CmsOrganizationalUnit.getParentFqn(id);
            flags = CmsAccessControlEntry.ACCESS_FLAGS_GROUP;
        } else if ((principal != null) && principal.isUser()) {
            name = ((CmsUser)principal).getFullName();
            ou = CmsOrganizationalUnit.getParentFqn(id);
            flags = CmsAccessControlEntry.ACCESS_FLAGS_USER;
        } else if ((id != null) && id.equals(CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_ID.toString())) {
            name = key(Messages.GUI_LABEL_ALLOTHERS_0);
            flags = CmsAccessControlEntry.ACCESS_FLAGS_ALLOTHERS;
        } else if ((id != null) && id.equals(CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_ID.toString())) {
            name = key(Messages.GUI_LABEL_OVERWRITEALL_0);
            flags = CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE_ALL;
        } else {
            // check if it is the case of a role
            CmsRole role = CmsRole.valueOfId(entry.getPrincipal());
            if (role != null) {
                name = role.getName(getLocale());
                id = role.getRoleName();
                flags = CmsAccessControlEntry.ACCESS_FLAGS_ROLE;
            } else {
                name = entry.getPrincipal().toString();
            }
        }

        if ((flags > 0) && ((entry.getFlags() & flags) == 0)) {
            // the flag is set to the wrong principal type
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle(getLocale()).key(Messages.ERR_INVALID_ACE_1, entry.toString()));
            }
            entry = new CmsAccessControlEntry(
                entry.getResource(),
                entry.getPrincipal(),
                entry.getAllowedPermissions(),
                entry.getDeniedPermissions(),
                (entry.getFlags() | flags));
        } else if (entry.getFlags() < CmsAccessControlEntry.ACCESS_FLAGS_USER) {
            // the flag is set to NO principal type
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle(getLocale()).key(Messages.ERR_INVALID_ACE_1, entry.toString()));
            }
            entry = new CmsAccessControlEntry(
                entry.getResource(),
                entry.getPrincipal(),
                entry.getAllowedPermissions(),
                entry.getDeniedPermissions(),
                (entry.getFlags() | CmsAccessControlEntry.ACCESS_FLAGS_GROUP));
        }

        String type = getEntryType(entry.getFlags(), false);

        if (id == null) {
            id = "";
        }

        // set the parameters for the hidden fields
        setParamType(type);
        setParamName(id);
        setParamUuid(entry.getPrincipal().toString());

        // set id value for html attributes
        String idValue = type + id + entry.getResource();

        // get the localized type label
        int typeInt = getEntryTypeInt(entry.getFlags());
        String typeLocalized = UNKNOWN_TYPE;
        if (typeInt >= 0) {
            typeLocalized = getTypesLocalized()[typeInt];
        }

        // determine the right image to display
        String typeImg = getTypes(true)[0];
        if (typeInt >= 0) {
            typeImg = getEntryType(entry.getFlags(), true).toLowerCase();
        }

        // get all permissions of the current entry
        CmsPermissionSet permissions = entry.getPermissions();

        // build String for disabled check boxes
        String disabled = "";
        if (!editable || (typeInt < 0)) {
            disabled = " disabled=\"disabled\"";
        }

        // build the heading
        if (!id.equals(CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_ID.toString())) {
            result.append(dialogRow(HTML_START));
            if (extendedView) {
                // for extended view, add toggle symbol and link to output
                result.append("<a href=\"javascript:toggleDetail('").append(idValue).append("');\">");
                result.append("<img src=\"").append(getSkinUri()).append(
                    "commons/plus.png\" class=\"noborder\" id=\"ic-").append(idValue).append("\"></a>");
            } else {
                result.append("<img src='").append(getSkinUri()).append(
                    "explorer/project_none.gif' class='noborder' width='16' height='16' >");
            }
            result.append("<img src=\"").append(getSkinUri()).append("commons/");
            result.append(typeImg);
            result.append(".png\" class=\"noborder\" width=\"16\" height=\"16\" alt=\"");
            result.append(typeLocalized);
            result.append("\" title=\"");
            result.append(typeLocalized);
            result.append("\">&nbsp;<span class=\"textbold\">");
            result.append(name);
            result.append("</span>");
            if (extendedView) {
                // for extended view, add short permissions
                result.append("&nbsp;(").append(entry.getPermissions().getPermissionString()).append(")");
            }
            try {
                if ((ou != null) && (OpenCms.getOrgUnitManager().getOrganizationalUnits(getCms(), "", true).size() > 1)) {
                    result.append("<br>");
                    result.append("<img src='").append(getSkinUri()).append(
                        "explorer/project_none.gif' class='noborder' width='16' height='16' >");
                    result.append("<img src='").append(getSkinUri()).append(
                        "explorer/project_none.gif' class='noborder' width='16' height='16' >");
                    result.append("&nbsp;");
                    try {
                        result.append(OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), ou).getDisplayName(
                            getLocale()));
                    } catch (CmsException e) {
                        result.append(ou);
                    }
                }
            } catch (CmsException e) {
                // should never happen
                if (LOG.isInfoEnabled()) {
                    LOG.info(e.getLocalizedMessage());
                }
            }
            result.append(dialogRow(HTML_END));
            if (extendedView) {
                // show the resource from which the ace is inherited if present
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(inheritRes)) {
                    result.append("<div class=\"dialogpermissioninherit\">");
                    result.append("<img src='").append(getSkinUri()).append(
                        "explorer/project_none.gif' class='noborder' width='16' height='16' >");
                    result.append("&nbsp;");
                    result.append(key(Messages.GUI_PERMISSION_INHERITED_FROM_1, new Object[] {inheritRes}));
                    result.append("</div>\n");
                }
                result.append("<div id =\"").append(idValue).append("\" class=\"hide\">");
            }
            result.append("<table class=\"dialogpermissiondetails\">\n");

            // build the form depending on the editable flag
            if (editable) {
                result.append("<form action=\"").append(getDialogUri()).append(
                    "\" method=\"post\" class=\"nomargin\" name=\"set").append(idValue).append("\">\n");
                // set parameters to show correct hidden input fields
                setParamAction(DIALOG_SET);
                result.append(paramsAsHidden());
            } else {
                result.append("<form class=\"nomargin\">\n");
            }

            // build headings for permission descriptions
            result.append("<tr>\n");
            result.append("\t<td class=\"dialogpermissioncell\"><span class=\"textbold\" unselectable=\"on\">");
            result.append(key(Messages.GUI_PERMISSION_0)).append("</span></td>\n");
            result.append("\t<td class=\"dialogpermissioncell textcenter\"><span class=\"textbold\" unselectable=\"on\">");
            result.append(key(Messages.GUI_PERMISSION_ALLOWED_0)).append("</span></td>\n");
            result.append("\t<td class=\"dialogpermissioncell textcenter\"><span class=\"textbold\" unselectable=\"on\">");
            result.append(key(Messages.GUI_PERMISSION_DENIED_0)).append("</span></td>\n");
            result.append("</tr>");

            Iterator i = m_permissionKeys.iterator();

            // show all possible permissions in the form
            while (i.hasNext()) {
                String key = (String)i.next();
                int value = CmsPermissionSet.getPermissionValue(key);
                String keyMessage = key(key);
                result.append("<tr>\n");
                result.append("\t<td class=\"dialogpermissioncell\">").append(keyMessage).append("</td>\n");
                result.append("\t<td class=\"dialogpermissioncell textcenter\"><input type=\"checkbox\" name=\"");
                result.append(value).append(PERMISSION_ALLOW).append("\" value=\"").append(value).append("\"").append(
                    disabled);
                if (isAllowed(permissions, value)) {
                    result.append(" checked=\"checked\"");
                }
                result.append("></td>\n");
                result.append("\t<td class=\"dialogpermissioncell textcenter\"><input type=\"checkbox\" name=\"");
                result.append(value).append(PERMISSION_DENY).append("\" value=\"").append(value).append("\"").append(
                    disabled);
                if (isDenied(permissions, value)) {
                    result.append(" checked=\"checked\"");
                }
                result.append("></td>\n");
                result.append("</tr>\n");
            }

            // show overwrite check box and buttons only for editable entries
            if (editable) {
                // do not show the responsible option for the 'all others' ace
                if (!id.equals(CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_ID.toString())) {
                    // show owner check box
                    result.append("<tr>\n");
                    result.append("\t<td class=\"dialogpermissioncell\">").append(key(Messages.GUI_LABEL_RESPONSIBLE_0)).append(
                        "</td>\n");
                    result.append("\t<td class=\"dialogpermissioncell textcenter\">");
                    result.append("<input type=\"checkbox\" name=\"").append(PARAM_RESPONSIBLE).append(
                        "\" value=\"true\"").append(disabled);
                    if (isResponsible(entry.getFlags())) {
                        result.append(" checked=\"checked\"");
                    }
                    result.append("></td>\n");
                    result.append("\t<td class=\"dialogpermissioncell\">&nbsp;</td>\n");
                    result.append("</tr>\n");
                }
                // show overwrite inherited check box
                result.append("<tr>\n");
                result.append("\t<td class=\"dialogpermissioncell\">").append(
                    key(Messages.GUI_PERMISSION_OVERWRITE_INHERITED_0)).append("</td>\n");
                result.append("\t<td class=\"dialogpermissioncell textcenter\">");
                result.append("<input type=\"checkbox\" name=\"").append(PARAM_OVERWRITEINHERITED).append(
                    "\" value=\"true\"").append(disabled);
                if (isOverWritingInherited(entry.getFlags())) {
                    result.append(" checked=\"checked\"");
                }
                result.append("></td>\n");
                result.append("\t<td class=\"dialogpermissioncell\">&nbsp;</td>\n");
                result.append("</tr>\n");

                // show inherit permissions check box on folders
                if (getInheritOption()) {
                    result.append("<tr>\n");
                    result.append("\t<td class=\"dialogpermissioncell\">").append(
                        key(Messages.GUI_PERMISSION_INHERIT_ON_SUBFOLDERS_0)).append("</td>\n");
                    result.append("\t<td class=\"dialogpermissioncell textcenter\">");
                    result.append("<input type=\"checkbox\" name=\"").append(PARAM_INHERIT).append("\" value=\"true\"").append(
                        disabled);
                    if (entry.isInheriting()) {
                        result.append(" checked=\"checked\"");
                    }
                    result.append("></td>\n");
                    result.append("\t<td class=\"dialogpermissioncell\">&nbsp;</td>\n");
                    result.append("</tr>\n");
                }

                // show "set" and "delete" buttons    
                result.append("<tr>\n");
                result.append("\t<td>&nbsp;</td>\n");
                result.append("\t<td class=\"textcenter\"><input class=\"dialogbutton\" type=\"submit\" value=\"").append(
                    key(Messages.GUI_LABEL_SET_0)).append("\"></form></td>\n");
                result.append("\t<td class=\"textcenter\">\n");
                // build the form for the "delete" button            
                result.append("\t\t<form class=\"nomargin\" action=\"").append(getDialogUri()).append(
                    "\" method=\"post\" name=\"delete").append(idValue).append("\">\n");
                // set parameters to show correct hidden input fields
                setParamAction(DIALOG_DELETE);
                result.append(paramsAsHidden());
                result.append("\t\t<input class=\"dialogbutton\" type=\"submit\" value=\"").append(
                    key(Messages.GUI_LABEL_DELETE_0)).append("\">\n");
                result.append("\t\t</form>\n");
                result.append("\t</td>\n");
                result.append("</tr>\n");
            } else {
                // close the form
                result.append("</form>\n");
            }

            result.append("</table>\n");
            if (extendedView) {
                // close the hidden div for extended view
                result.append("</div>");
            }
        } else {
            result.append(dialogRow(HTML_START));

            result.append("<table style='margin-left: 13px;' class=\"dialogpermissiondetails\">\n");
            // build headings for permission descriptions
            result.append("<tr>\n");
            result.append("\t<td style=\"width: 280px;\"><span class=\"textbold\" unselectable=\"on\">");

            result.append("<img src=\"").append(getSkinUri()).append("commons/");
            result.append(typeImg);
            result.append(".png\" class=\"noborder\" width=\"16\" height=\"16\" alt=\"");
            result.append(typeLocalized);
            result.append("\" title=\"");
            result.append(typeLocalized);
            result.append("\">&nbsp;<span class=\"textbold\">");
            result.append(name);
            result.append("</span></td>\n");
            result.append("\t<td class=\"dialogpermissioncell textcenter\"><span class=\"textbold\" unselectable=\"on\">");
            if (editable) {
                // build the form for the "delete" button            
                result.append("\t\t<form class=\"nomargin\" action=\"").append(getDialogUri()).append(
                    "\" method=\"post\" name=\"delete").append(idValue).append("\">\n");
                // set parameters to show correct hidden input fields
                setParamAction(DIALOG_DELETE);
                result.append(paramsAsHidden());
                result.append("\t\t<input class=\"dialogbutton\" type=\"submit\" value=\"").append(
                    key(Messages.GUI_LABEL_DELETE_0)).append("\">\n");
                result.append("\t\t</form>\n");
            }
            result.append("</td>\n");
            result.append("</tr>");
            result.append("</table>\n");
            result.append(dialogRow(HTML_END));

        }
        return result;
    }

    /**
     * @see #buildPermissionEntryForm(CmsAccessControlEntry, boolean, boolean, String)
     *
     * @param id the UUID of the principal of the permission set
     * @param curSet the current permission set 
     * @param editable boolean to determine if the form is editable
     * @param extendedView boolean to determine if the view is selectable with DHTML
     * @return String with HTML code of the form
     */
    private StringBuffer buildPermissionEntryForm(
        CmsUUID id,
        CmsPermissionSet curSet,
        boolean editable,
        boolean extendedView) {

        String fileName = getParamResource();
        int flags = 0;
        try {
            I_CmsPrincipal p;
            try {
                p = CmsPrincipal.readPrincipalIncludingHistory(getCms(), id);
            } catch (CmsException e) {
                p = null;
            }
            if ((p != null) && p.isGroup()) {
                flags = CmsAccessControlEntry.ACCESS_FLAGS_GROUP;
            } else if ((p != null) && p.isUser()) {
                flags = CmsAccessControlEntry.ACCESS_FLAGS_USER;
            } else if ((p == null) && id.equals(CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_ID)) {
                flags = CmsAccessControlEntry.ACCESS_FLAGS_ALLOTHERS;
            } else if ((p == null) && id.equals(CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_ID)) {
                flags = CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE_ALL;
            } else {
                // check if it is the case of a role
                CmsRole role = CmsRole.valueOfId(id);
                if (role != null) {
                    flags = CmsAccessControlEntry.ACCESS_FLAGS_ROLE;
                }
            }

            CmsResource res = getCms().readResource(fileName, CmsResourceFilter.ALL);
            CmsAccessControlEntry entry = new CmsAccessControlEntry(res.getResourceId(), id, curSet, flags);
            return buildPermissionEntryForm(entry, editable, extendedView, null);
        } catch (CmsException e) {
            // can usually be ignored
            if (LOG.isInfoEnabled()) {
                LOG.info(e.getLocalizedMessage());
            }
            return new StringBuffer("");
        }
    }

    /**
     * Returns the actual real permissions (including role, and any other special check) for the current user.<p>
     * 
     * @return the actual real permissions for the current user
     * 
     * @throws CmsException if something goes wrong 
     */
    private CmsPermissionSet buildPermissionsForCurrentUser() throws CmsException {

        CmsResourceUtil resUtil = new CmsResourceUtil(getCms(), getCms().readResource(
            getParamResource(),
            CmsResourceFilter.ALL));
        return resUtil.getPermissionSet();
    }

    /**
     * Builds a StringBuffer with HTML code for the access control entries of a resource.<p>
     * 
     * @param entries all access control entries for the resource
     * @return StringBuffer with HTML code for all entries
     */
    private StringBuffer buildResourceList(ArrayList entries) {

        StringBuffer result = new StringBuffer(256);
        Iterator i = entries.iterator();
        boolean hasEntries = i.hasNext();

        if (hasEntries || !getInheritOption()) {
            // create headline for resource entries
            result.append(dialogSubheadline(key(Messages.GUI_PERMISSION_TITLE_0)));
        }

        // create the internal form
        result.append(buildInternalForm());

        if (hasEntries) {
            // only create output if entries are present
            result.append(dialogSpacer());
            // open white box
            result.append(dialogWhiteBox(HTML_START));

            // list all entries
            while (i.hasNext()) {
                CmsAccessControlEntry curEntry = (CmsAccessControlEntry)i.next();
                result.append(buildPermissionEntryForm(curEntry, getEditable(), false, null));
                if (i.hasNext()) {
                    result.append(dialogSeparator());
                }
            }

            // close white box
            result.append(dialogWhiteBox(HTML_END));
        }
        return result;
    }

    /**
     * Returns if the requested resource if blocking locked.<p>
     * 
     * @return <code>true</code> if the resource is blocking locked
     */
    private boolean isBlockingLocked() {

        boolean result = true;
        CmsLockFilter blockingFilter = CmsLockFilter.FILTER_ALL;
        blockingFilter = blockingFilter.filterNotLockableByUser(getCms().getRequestContext().getCurrentUser());
        try {
            List<String> blocking = getCms().getLockedResources(getParamResource(), blockingFilter);
            result = blocking.size() > 0;
        } catch (CmsException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getLocalizedMessage(), e);
            }
        }
        return result;
    }

    /**
     * Checks if the current user has the vfs manager role for the current select resource.<p> 
     * 
     * @return <code>true</code> if the current user has the vfs manager role for the current select resource
     */
    private boolean isRoleEditable() {

        return OpenCms.getRoleManager().hasRoleForResource(getCms(), CmsRole.VFS_MANAGER, getParamResource())
            && (getParamResource().startsWith(CmsWorkplace.VFS_PATH_SYSTEM) && getParamResource().startsWith(
                CmsWorkplace.VFS_PATH_SYSTEM));
    }
}
