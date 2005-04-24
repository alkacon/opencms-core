/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/CmsChacc.java,v $
 * Date   : $Date: 2005/04/24 11:20:31 $
 * Version: $Revision: 1.9 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
package org.opencms.workplace.commons;

import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsAccessControlList;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsRole;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Provides methods for building the permission settings dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/commons/chacc.jsp
 * </ul>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.9 $
 * 
 * @since 5.1
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
    /** Request parameter name for the overwrite inherited permissions parameter. */
    public static final String PARAM_OVERWRITEINHERITED = "overwriteinherited";
    /** Request parameter name for the view parameter. */
    public static final String PARAM_VIEW = "view";
    
    /** Constant for the request parameters suffix: allow. */
    public static final String PERMISSION_ALLOW = "allow";
    /** Constant for the request parameters suffix: deny. */
    public static final String PERMISSION_DENY = "deny";    
    
    /** PermissionSet of the current user for the resource. */
    private CmsPermissionSet m_curPermissions;
    
    /** Indicates if forms are editable by current user. */
    private boolean m_editable;
    
    /** Stores eventual error message Strings. */
    private ArrayList m_errorMessages = new ArrayList(); 
    
    /** Indicates if inheritance flags are set as hidden fields for resource folders. */
    private boolean m_inherit;
    
    private String m_paramName;  
    private String m_paramType;
    
    /** Stores all possible permission keys of a permission set. */
    private Set m_permissionKeys = CmsPermissionSet.getPermissionKeys();
    
    /** Marks if the inherited permissions information should be displayed. */
    private boolean m_showInheritedPermissions;
    
    /** The possible types of new access control entries. */
    private String[] m_types = {"group", "user"};
    
    /** The possible type values of access control entries. */
    private int[] m_typesInt = {I_CmsConstants.C_ACCESSFLAGS_GROUP, I_CmsConstants.C_ACCESSFLAGS_USER};
    
    /** The possible localized types of new access control entries. */
    private String[] m_typesLocalized = new String[2];
    
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
            if (OpenCms.getLog(this).isInfoEnabled()) {
                OpenCms.getLog(this).info(e);
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
                getCms().chacc(file, getTypes()[arrayPosition], name, permissionString);
                return true;
            } catch (CmsException e) {
                m_errorMessages.add(key("dialog.permission.error.add"));
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
                if ((flags & I_CmsConstants.C_ACCESS_INTERNAL_READ) == 0) {
                    flags += I_CmsConstants.C_ACCESS_INTERNAL_READ;
                }
            } else {
                if ((flags & I_CmsConstants.C_ACCESS_INTERNAL_READ) > 0) {
                    flags -= I_CmsConstants.C_ACCESS_INTERNAL_READ;
                }
            }
            
            getCms().lockResource(getParamResource());
            getCms().chflags(getParamResource(), flags);

        } catch (CmsException e) {       
              m_errorMessages.add(key("dialog.permission.error.internal"));
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
                param = request.getParameter(value+PERMISSION_ALLOW);
                paramInt = Integer.parseInt(param);
                allowValue |= paramInt;
            } catch (Exception e) {
                // can usually be ignored
                if (OpenCms.getLog(this).isInfoEnabled()) {
                    OpenCms.getLog(this).info(e);
                }
            }
            try {           
                param = request.getParameter(value+PERMISSION_DENY);
                paramInt = Integer.parseInt(param);
                denyValue |= paramInt;
            } catch (Exception e) {
                // can usually be ignored
                if (OpenCms.getLog(this).isInfoEnabled()) {
                    OpenCms.getLog(this).info(e);
                }
            }
            
        }
       
        // get the current Ace to get the current ace flags
        try {
            List allEntries = getCms().getAccessControlEntries(file, false);
            int flags = 0;           
            for (int k=0; k< allEntries.size(); k++) {
                CmsAccessControlEntry curEntry = (CmsAccessControlEntry)allEntries.get(k);
                String curType = getEntryType(curEntry.getFlags());
                String curName = getCms().lookupPrincipal(curEntry.getPrincipal()).getName();
                if (curName.equals(name) && curType.equals(type)) {
                    flags = curEntry.getFlags();
                    break;
                }
            }
            
            // modify the ace flags to determine inheritance of the current ace
            if ("true".equals(inherit)) {
                flags |= I_CmsConstants.C_ACCESSFLAGS_INHERIT;
            } else {
                flags &= ~I_CmsConstants.C_ACCESSFLAGS_INHERIT;
            }
            
            // modify the ace flags to determine overwriting of inherited ace
            if ("true".equals(overWriteInherited)) {
                flags |= I_CmsConstants.C_ACCESSFLAGS_OVERWRITE;
            } else {
                flags &= ~I_CmsConstants.C_ACCESSFLAGS_OVERWRITE;
            }
            
            // lock resource if autolock is enabled
            checkLock(getParamResource());            
            // try to change the access entry           
            getCms().chacc(file, type, name, allowValue, denyValue, flags);
            return true;
        } catch (CmsException e) {
            m_errorMessages.add(key("dialog.permission.error.modify"));
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
        try {
            // lock resource if autolock is enabled
            checkLock(getParamResource());
            getCms().rmacc(file, type, name);
            return true;
        } catch (CmsException e) {
            m_errorMessages.add(key("dialog.permission.error.remove"));
            return false;
        }
    }
    
    /**
     * Builds a String with HTML code to display the users access rights for the current resource.<p>
     * 
     * @return HTML String with the access rights of the current user
     */
    public String buildCurrentPermissions() {
        
        StringBuffer result = new StringBuffer(64);
        // set icon and style class to use: hide user permissions
        String image = "plus.gif";
        String styleClass = "hide";
        if (getSettings().getUserSettings().getDialogExpandUserPermissions()) {
            // show user permissions
            image = "minus.gif";
            styleClass = "show";
        }
              
        result.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n");
        result.append("<tr>\n");
        result.append("\t<td style=\"vertical-align: bottom; padding-bottom: 2px;\"><a href=\"javascript:toggleDetail('userpermissions');\"><img src=\"");
        result.append(getSkinUri());
        result.append("buttons/");
        result.append(image);
        result.append("\" class=\"noborder\" id=\"ic-userpermissions\"></a></td>\n");
        result.append("\t<td>");
        result.append(dialogSubheadline(key("dialog.permission.headline.user")));
        result.append("</td>\n");
        result.append("</tr>\n");
        result.append("</table>\n");

        result.append("<div class=\"");
        result.append(styleClass);
        result.append("\" id=\"userpermissions\">\n");
        result.append(dialogWhiteBoxStart());
        result.append(buildPermissionEntryForm(getSettings().getUser().getId(), getCurPermissions(), false, false));
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
            result.append(dialogBlock(HTML_START, key("dialog.permission.error.headline"), true));
            result.append(errorMessages);
            result.append(dialogBlockEnd());
        }
        return result.toString();
    }
    
    /**
     * Builds a String with HTML code to display the inherited and own access control entries of a resource.<p>
     * 
     * @return HTML code for inherited and own entries of the current resource
     */
    public String buildRightsList() {
        
        StringBuffer result = new StringBuffer(1024);
        
        // set icon and style class to use: hide inherited permissions
        String image = "plus.gif";
        String styleClass = "hide";
        if (getSettings().getUserSettings().getDialogExpandInheritedPermissions() || getShowInheritedPermissions()) {
            // show inherited permissions if configured in settings or if the view detail was switched
            image = "minus.gif";
            styleClass = "show";
        }
        
        // create headline for inherited entries
        result.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n");
        result.append("<tr>\n");
        result.append("\t<td style=\"vertical-align: bottom; padding-bottom: 2px;\"><a href=\"javascript:toggleDetail('inheritedpermissions');\"><img src=\"");
        result.append(getSkinUri());
        result.append("buttons/");
        result.append(image);
        result.append("\" class=\"noborder\" id=\"ic-inheritedpermissions\"></a></td>\n");
        result.append("\t<td>");
        result.append(dialogSubheadline(key("dialog.permission.headline.inherited")));
        result.append("</td>\n");
        result.append("</tr>\n");
        result.append("</table>\n");
        
        // open div that toggles visibility of inherited permissions
        result.append("<div class=\"");
        result.append(styleClass);
        result.append("\" id=\"inheritedpermissions\">\n");
        
        // create detail view selector 
        result.append("<table border=\"0\">\n<tr>\n");
        result.append("\t<td>");
        result.append(key("dialog.permission.viewselect"));
        result.append("</td>\n");
        String selectedView = getSettings().getPermissionDetailView();   
        result.append("\t<form action=\"").append(getDialogUri()).append("\" method=\"post\" name=\"selectshortview\">\n");            
        result.append("\t<td>\n");
        result.append("\t<input type=\"hidden\" name=\"");
        result.append(PARAM_VIEW);
        result.append("\" value=\"short\">\n");
        // set parameters to show correct hidden input fields
        setParamAction(null);
        result.append(paramsAsHidden());
        result.append("\t<input  type=\"submit\" class=\"dialogbutton\" value=\"").append(key("button.short")).append("\"");
        if (!"long".equals(selectedView)) {
            result.append(" disabled=\"disabled\"");
        }
        result.append(">\n");
        result.append("\t</td>\n");
        result.append("\t</form>\n\t<form action=\"").append(getDialogUri()).append("\" method=\"post\" name=\"selectlongview\">\n");
        result.append("\t<td>\n");
        result.append("\t<input type=\"hidden\" name=\"");
        result.append(PARAM_VIEW);
        result.append("\" value=\"long\">\n");
        result.append(paramsAsHidden());
        result.append("\t<input type=\"submit\" class=\"dialogbutton\" value=\"").append(key("button.long")).append("\"");
        if ("long".equals(selectedView)) {
            result.append(" disabled=\"disabled\"");
        }
        result.append(">\n");
        result.append("\t</td>\n\t</form>\n");
        result.append("</tr>\n</table>\n");

        // get all access control entries of the current file
        List allEntries = new ArrayList();
        try {
            allEntries = getCms().getAccessControlEntries(getParamResource(), true);
        } catch (CmsException e) {
            // can usually be ignored
            if (OpenCms.getLog(this).isInfoEnabled()) {
                OpenCms.getLog(this).info(e);
            }
        }
        
        // store all parent folder ids together with path in a map
        Map parents = new HashMap();
        String path = CmsResource.getParentFolder(getParamResource());
        List parentResources = new ArrayList();
        try {
            // get all parent folders of the current file
            parentResources = getCms().readPath(path, CmsResourceFilter.IGNORE_EXPIRATION);
        } catch (CmsException e) {
            // can usually be ignored
            if (OpenCms.getLog(this).isInfoEnabled()) {
                OpenCms.getLog(this).info(e);
            }
        }
        Iterator k = parentResources.iterator();
        while (k.hasNext()) {
            // add the current folder to the map
            CmsResource curRes = (CmsResource)k.next();
            parents.put(curRes.getResourceId(), curRes.getRootPath());
        }        

        // create new ArrayLists in which inherited and non inherited entries are stored
        ArrayList ownEntries = new ArrayList(0);
        ArrayList inheritedEntries = new ArrayList(0);

        for (int i=0; i<allEntries.size(); i++) {
            CmsAccessControlEntry curEntry = (CmsAccessControlEntry)allEntries.get(i);
            if (curEntry.isInherited()) {
                // add the entry to the inherited rights list for the "long" view
                if ("long".equals(getSettings().getPermissionDetailView())) {       
                    inheritedEntries.add(curEntry);
                }
            } else {
                // add the entry to the own rights list
                ownEntries.add(curEntry);
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
     * Returns a list with all error messages which occured when trying to add a new access control entry.<p>
     * 
     * @return List of error message Strings
     */
    public ArrayList getErrorMessages() {
        
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
    
    /**
     * Initializes some member variables to display the form with the right options for the current user.<p>
     * 
     * This method must be called after initWorkplaceRequestValues().<p>
     */
    public void init() {

        // the current user name
        String userName = getSettings().getUser().getName();
        
        if (m_typesLocalized[0] == null) {
            m_typesLocalized[0] = key("label.group");
            m_typesLocalized[1] = key("label.user");
        }
        
        // set flags to show editable or non editable entries
        setEditable(false);
        setInheritOption(false);
                 
        try {      
            // get the current users' permissions
            setCurPermissions(getCms().getPermissions(getParamResource(), userName));
    
            // check if the current resource is a folder
            CmsResource resource = getCms().readResource(getParamResource(), CmsResourceFilter.ALL);
            if (resource.isFolder()) {
                // only folders have the inherit option activated
                setInheritOption(true);
            }
        } catch (CmsException e) { 
            // can usually be ignored
            if (OpenCms.getLog(this).isInfoEnabled()) {
                OpenCms.getLog(this).info(e);
            }
        }

        // check the current users permission to change access control entries
        if ((!getCms().getRequestContext().currentProject().isOnlineProject() 
            && getCms().isInsideCurrentProject(getParamResource()))
            && (getCms().hasRole(CmsRole.VFS_MANAGER) 
            || ((m_curPermissions.getAllowedPermissions() & CmsPermissionSet.PERMISSION_CONTROL) > 0 
                && !((m_curPermissions.getDeniedPermissions() & CmsPermissionSet.PERMISSION_CONTROL) > 0)))) {
            setEditable(true);
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

    /**
     * Check method to validate the user input when creating a new access control entry.<p>
     * 
     * @param name the name of the new user/group
     * @param arrayPosition the position in the types array
     * @return true if everything is ok, otherwise false
     */
    protected boolean checkNewEntry(String name, int arrayPosition) {
        
        m_errorMessages.clear();
        boolean inArray = false;
        if (getTypes()[arrayPosition] != null) {
            inArray = true;
        }
        if (!inArray) {
            m_errorMessages.add(key("dialog.permission.error.type"));
        }
        if (name == null || "".equals(name.trim())) {
            m_errorMessages.add(key("dialog.permission.error.name"));
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
        if (resName != null && !"".equals(resName)) {
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
     * @return String representation of the ace type
     */
    protected String getEntryType(int flags) {
        
        for (int i=0; i<getTypes().length; i++) {
            if ((flags & getTypesInt()[i]) > 0) {
                return getTypes()[i];
            }
        }
        return "Unknown";
    }
    
    /**
     * Determines the int type of the current access control entry.<p>
     * 
     * @param flags the value of the current flags
     * @return int representation of the ace type as int
     */
    protected int getEntryTypeInt(int flags) {
        
        for (int i=0; i<getTypes().length; i++) {
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
     * Returns if the inherited permissions information should be displayed.<p>
     *
     * @return true if the inherited permissions information should be displayed, otherwise false
     */
    protected boolean getShowInheritedPermissions() {

        return m_showInheritedPermissions;
    }
    
    /**
     * Returns a String array with the possible entry types.<p>
     * 
     * @return the possible types
     */
    protected String[] getTypes() {
        
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
        } else if (DIALOG_CANCEL.equals(getParamAction())) {          
            setAction(ACTION_CANCEL);
        } else if (DIALOG_INTERNALUSE.equals(getParamAction())) {          
            setAction(ACTION_INTERNALUSE);            
        } else {                        
            setAction(ACTION_DEFAULT);
            // build the title for chacc dialog     
            setParamTitle(key("title.chmod") + ": " + CmsResource.getName(getParamResource()));
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
     * Check if the current permissions are inherited to subresources.<p>
     * 
     * @param flags value of all flags of the current entry
     * @return true if permissions are inherited to subresources, otherwise false 
     */
    protected boolean isInheriting(int flags) {
        
        if ((flags & I_CmsConstants.C_ACCESSFLAGS_INHERIT) > 0) {
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
        
        if ((flags & I_CmsConstants.C_ACCESSFLAGS_OVERWRITE) > 0) {
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
            result.append(dialogBlockStart(key("dialog.permission.headline.add")));

            // get all possible entry types
            ArrayList options = new ArrayList();
            ArrayList optionValues = new ArrayList();
            for (int i=0; i<getTypes().length; i++) {
                options.add(getTypesLocalized()[i]);
                optionValues.add(Integer.toString(i));
            }            

            // create the input form for adding an ace
            result.append("<form action=\"").append(getDialogUri()).append("\" method=\"post\" name=\"add\" class=\"nomargin\">\n");
            // set parameters to show correct hidden input fields
            setParamAction(DIALOG_ADDACE);
            setParamType(null);
            setParamName(null);
            result.append(paramsAsHidden());
            result.append("<table border=\"0\" width=\"100%\">\n");
            result.append("<tr>\n");
            result.append("\t<td>").append(buildSelect("name=\"type\"", options, optionValues, -1)).append("</td>\n");
            result.append("\t<td class=\"maxwidth\"><input type=\"text\" class=\"maxwidth\" name=\"name\" value=\"\"></td>\n");            
            result.append("\t<td><input class=\"dialogbutton\" style=\"width: 60px;\" type=\"button\" value=\"");
            result.append(key("button.search")).append("\" onClick=\"javascript:openDialogWin('chaccbrowser.jsp','UserGroup');\"></td>\n");
            result.append("\t<td><input class=\"dialogbutton\" type=\"submit\" value=\"").append(key("input.add")).append("\"></td>\n");
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
        Iterator i;

        // display the long view
        if ("long".equals(view)) {
            i = entries.iterator();
            while (i.hasNext()) {
                CmsAccessControlEntry curEntry = (CmsAccessControlEntry)i.next();
                // build the list with enabled extended view and resource name
                result.append(buildPermissionEntryForm(curEntry, false, true, getConnectedResource(curEntry, parents)));
            }
        } else {
            // show the short view, use an ACL to build the list
            try {
                // get the inherited ACL of the parent folder 
                CmsAccessControlList acList = getCms().getAccessControlList(getParamResource(), true);
                Set principalSet = acList.getPrincipals();
                i = principalSet.iterator();
                while (i.hasNext()) {
                    CmsUUID principalId = (CmsUUID)i.next();                   
                    I_CmsPrincipal principal = getCms().lookupPrincipal(principalId);
                    CmsPermissionSet permissions = acList.getPermissions(principal);
                    // build the list with enabled extended view only
                    result.append(buildPermissionEntryForm(principalId, permissions, false, true));
                }
            } catch (CmsException e) {
                // can usually be ignored
                if (OpenCms.getLog(this).isInfoEnabled()) {
                    OpenCms.getLog(this).info(e);
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
            internal = ((resource.getFlags() & I_CmsConstants.C_ACCESS_INTERNAL_READ) > 0);
        } catch (CmsException e) {
            // an error occured reading the resource
            if (OpenCms.getLog(this).isErrorEnabled()) { 
                OpenCms.getLog(this).error(". Accessing resource: " + getParamResource() + " : " + e);
            }  
        }
 
        if ((resource != null) && (resource.isFile())) {
            // only show internal checkbox on files
            result.append("<form action=\"").append(getDialogUri()).append("\" method=\"post\" name=\"internal\" class=\"nomargin\">\n");        
            result.append("<table border=\"0\" width=\"100%\">\n");
            result.append("<tr>\n");
            result.append("\t<td class=\"dialogpermissioncell\">").append(key("dialog.permission.internal"));
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
                result.append("<td><input  type=\"submit\" class=\"dialogbutton\" value=\"").append(key("button.submit")).append("\">");
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
    private StringBuffer buildPermissionEntryForm(CmsAccessControlEntry entry, boolean editable, boolean extendedView, String inheritRes) {
        
        StringBuffer result = new StringBuffer(8);
        
        // get name and type of the current entry
        String name = getCms().lookupPrincipal(entry.getPrincipal()).getName();
        String type = getEntryType(entry.getFlags());
        
        if (name == null) {
            name = "";
        }
        
        // set the parameters for the hidden fields
        setParamType(type);
        setParamName(name);
        
        // set id value for html attributes
        String idValue = type + name + entry.getResource();
        
        // get the localized type label
        String typeLocalized = getTypesLocalized()[getEntryTypeInt(entry.getFlags())];
        
        // determine the right image to display
        String typeImg = getEntryType(entry.getFlags()).toLowerCase();
        
        // get all permissions of the current entry
        CmsPermissionSet permissions = entry.getPermissions();
        
        // build String for disabled check boxes
        String disabled = "";
        if (!editable) {
            disabled = " disabled=\"disabled\"";
        }
        
        // build the heading
        result.append(dialogRow(HTML_START));
        if (extendedView) {
            // for extended view, add toggle symbol and link to output
            result.append("<a href=\"javascript:toggleDetail('").append(idValue).append("');\">");
            result.append("<img src=\"").append(getSkinUri()).append("buttons/plus.gif\" class=\"noborder\" id=\"ic-").append(idValue).append("\"></a>");
        }
        result.append("<img src=\"").append(getSkinUri()).append("buttons/");
        result.append(typeImg);
        result.append("_sm.gif\" class=\"noborder\" width=\"16\" height=\"16\" alt=\"");       
        result.append(typeLocalized);
        result.append("\" title=\"");      
        result.append(typeLocalized);
        result.append("\">&nbsp;<span class=\"textbold\">");
        result.append(name);
        result.append("</span>");
        
        if (extendedView) {
            // for extended view, add short permissions and hidden div
            result.append("&nbsp;(").append(entry.getPermissions().getPermissionString()).append(")");
            result.append(dialogRow(HTML_END));
            // show the resource from which the ace is inherited if present
            if (inheritRes != null && !"".equals(inheritRes)) {
                result.append("<div class=\"dialogpermissioninherit\">").append(key("dialog.permission.list.inherited")).append("  ");
                result.append(inheritRes);
                result.append("</div>\n");
            }
            result.append("<div id =\"").append(idValue).append("\" class=\"hide\">");
        } else {
            result.append(dialogRow(HTML_END));
        }
        
        result.append("<table class=\"dialogpermissiondetails\">\n");
        
        // build the form depending on the editable flag
        if (editable) {
            result.append("<form action=\"").append(getDialogUri()).append("\" method=\"post\" class=\"nomargin\" name=\"set").append(idValue).append("\">\n");
            // set parameters to show correct hidden input fields
            setParamAction(DIALOG_SET);
            result.append(paramsAsHidden());
        } else {
            result.append("<form class=\"nomargin\">\n");
        }
        
        // build headings for permission descriptions
        result.append("<tr>\n");
        result.append("\t<td class=\"dialogpermissioncell\"><span class=\"textbold\" unselectable=\"on\">");
        result.append(key("dialog.permission.list.permission")).append("</span></td>\n");
        result.append("\t<td class=\"dialogpermissioncell textcenter\"><span class=\"textbold\" unselectable=\"on\">");
        result.append(key("dialog.permission.list.allowed")).append("</span></td>\n");
        result.append("\t<td class=\"dialogpermissioncell textcenter\"><span class=\"textbold\" unselectable=\"on\">");
        result.append(key("dialog.permission.list.denied")).append("</span></td>\n");
        result.append("</tr>");
        
        Iterator i = m_permissionKeys.iterator();
        
        // show all possible permissions in the form
        while (i.hasNext()) {
            String key = (String)i.next();
            int value = CmsPermissionSet.getPermissionValue(key);
            String keyMessage = getSettings().getMessages().key(key);
            result.append("<tr>\n");
            result.append("\t<td class=\"dialogpermissioncell\">").append(keyMessage).append("</td>\n");
            result.append("\t<td class=\"dialogpermissioncell textcenter\"><input type=\"checkbox\" name=\"");
            result.append(value).append(PERMISSION_ALLOW).append("\" value=\"").append(value).append("\"").append(disabled);
            if (isAllowed(permissions, value)) {
                result.append(" checked=\"checked\"");
            }
            result.append("></td>\n");
            result.append("\t<td class=\"dialogpermissioncell textcenter\"><input type=\"checkbox\" name=\"");
            result.append(value).append(PERMISSION_DENY).append("\" value=\"").append(value).append("\"").append(disabled);
            if (isDenied(permissions, value)) {
                result.append(" checked=\"checked\"");
            }
            result.append("></td>\n");
            result.append("</tr>\n");
        }  
        
        // show overwrite checkbox and buttons only for editable entries
        if (editable) {
        
            // show overwrite inherited checkbox
            result.append("<tr>\n");
            result.append("\t<td class=\"dialogpermissioncell\">").append(key("dialog.permission.list.overwrite")).append("</td>\n");
            result.append("\t<td class=\"dialogpermissioncell textcenter\">");
            result.append("<input type=\"checkbox\" name=\"").append(PARAM_OVERWRITEINHERITED).append("\" value=\"true\"").append(disabled);
            if (isOverWritingInherited(entry.getFlags())) {
                result.append(" checked=\"checked\"");           
            }
            result.append("></td>\n"); 
            result.append("\t<td class=\"dialogpermissioncell\">&nbsp;</td>\n");
            result.append("</tr>\n");   

            // show inherit permissions checkbox on folders
            if (getInheritOption()) {
                result.append("<tr>\n");
                result.append("\t<td class=\"dialogpermissioncell\">").append(key("dialog.permission.list.inherit")).append("</td>\n");
                result.append("\t<td class=\"dialogpermissioncell textcenter\">");
                result.append("<input type=\"checkbox\" name=\"").append(PARAM_INHERIT).append("\" value=\"true\"").append(disabled);
                if (isInheriting(entry.getFlags())) {
                    result.append(" checked=\"checked\"");           
                }
                result.append("></td>\n"); 
                result.append("\t<td class=\"dialogpermissioncell\">&nbsp;</td>\n");
                result.append("</tr>\n");   
            }    
            
            // show "set" and "delete" buttons    
            result.append("<tr>\n");
            result.append("\t<td>&nbsp;</td>\n");
            result.append("\t<td class=\"textcenter\"><input class=\"dialogbutton\" type=\"submit\" value=\"").append(key("button.submit")).append("\"></form></td>\n");           
            result.append("\t<td class=\"textcenter\">\n");
            // build the form for the "delete" button            
            result.append("\t\t<form class=\"nomargin\" action=\"").append(getDialogUri()).append("\" method=\"post\" name=\"delete").append(idValue).append("\">\n");
            // set parameters to show correct hidden input fields
            setParamAction(DIALOG_DELETE);
            result.append(paramsAsHidden());
            result.append("\t\t<input class=\"dialogbutton\" type=\"submit\" value=\"").append(key("button.delete")).append("\">\n");
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
    private StringBuffer buildPermissionEntryForm(CmsUUID id, CmsPermissionSet curSet, boolean editable, boolean extendedView) {
        
        String fileName = getParamResource();
        int flags = 0;
        try {
            // TODO: a more elegant way to determine user/group of current id
            try {
                getCms().readGroup(id);
                flags = I_CmsConstants.C_ACCESSFLAGS_GROUP;
            } catch (CmsException e) {
                try {
                    getCms().readUser(id);
                    flags = I_CmsConstants.C_ACCESSFLAGS_USER;
                } catch (CmsException exc) {
                    // can usually be ignored
                    if (OpenCms.getLog(this).isInfoEnabled()) {
                        OpenCms.getLog(this).info(e);
                    }
                }
            }
            CmsResource res = getCms().readResource(fileName, CmsResourceFilter.ALL);
            CmsAccessControlEntry entry = new CmsAccessControlEntry(res.getResourceId(), id, curSet, flags);
            return buildPermissionEntryForm(entry, editable, extendedView, null);
        } catch (CmsException e) {
            // can usually be ignored
            if (OpenCms.getLog(this).isInfoEnabled()) {
                OpenCms.getLog(this).info(e);
            }         
            return new StringBuffer("");
        }
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
        
        if (hasEntries || ! getInheritOption()) {
            // create headline for resource entries
            result.append(dialogSubheadline(key("dialog.permission.headline.resource")));
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
                result.append(buildPermissionEntryForm(curEntry, this.getEditable(), false, null));
                if (i.hasNext()) {
                    result.append(dialogSeparator()); 
                }
            }
            
            // close white box
            result.append(dialogWhiteBox(HTML_END));
        }
        return result;
    }
      
}
