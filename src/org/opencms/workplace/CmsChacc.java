/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/CmsChacc.java,v $
 * Date   : $Date: 2003/09/15 10:51:14 $
 * Version: $Revision: 1.20 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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
package org.opencms.workplace;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsResource;
import com.opencms.flex.jsp.CmsJspActionElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsAccessControlList;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsUUID;

/**
 * Provides methods for building the permission settings dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/jsp/chacc_html
 * </ul>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.20 $
 * 
 * @since 5.1
 */
public class CmsChacc extends CmsDialog {
    
    public static final String DIALOG_TYPE = "chacc";
    
    public static final String DIALOG_SET = "set";
    public static final String DIALOG_DELETE = "delete";
    public static final String DIALOG_ADDACE = "addace";
    
    // always start individual action id's with 100 to leave enough room for more default actions
    public static final int ACTION_SET = 100;
    public static final int ACTION_DELETE = 200;
    public static final int ACTION_ADDACE = 300;
    
    private String m_paramType;
    private String m_paramName;
    
    public static final String PERMISSION_ALLOW = "allow";
    public static final String PERMISSION_DENY = "deny";    
    
    /** Stores eventual error message Strings */
    private ArrayList m_errorMessages = new ArrayList(); 
    
    /** The possible types of new access control entries */
    private String[] m_types = {"group", "user"};
    
    /** The possible localized types of new access control entries */
    private String[] m_typesLocalized = new String[2];
    
    /** The possible type values of access control entries */
    private int[] m_typesInt = {I_CmsConstants.C_ACCESSFLAGS_GROUP, I_CmsConstants.C_ACCESSFLAGS_USER};
    
    /** Indicates if forms are editable by current user */
    private boolean m_editable;
    
    /** Indicates if inheritance flags are set as hidden fields for resource folders */
    private boolean m_inherit;
    
    /** PermissionSet of the current user for the resource */
    private CmsPermissionSet m_curPermissions;
    
    /** Stores all possible permission keys of a permission set */
    private Set m_permissionKeys = CmsPermissionSet.getPermissionKeys();
    
    
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
     * Sets the value of the name parameter.<p>
     * 
     * @param value the value to set
     */
    public void setParamName(String value) {
        m_paramName = value;
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
     * Sets the value of the type parameter.<p>
     * 
     * @param value the value to set
     */
    public void setParamType(String value) {
        m_paramType = value;
    }
    
    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {        
        // fill the parameter values in the get/set methods
        fillParamValues(request);
        
        // set the detail mode of the "inherited" list view
        String detail = request.getParameter("view");
        if (detail != null) {
            settings.setPermissionDetailView(detail);
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
        } else {                        
            setAction(ACTION_DEFAULT);
        }
        
        // build the title for chacc dialog     
        setParamTitle(key("title.chmod") + ": " + CmsResource.getName(getParamResource()));              
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
        setInherit(false);
                 
        try {      
            // get the current users' permissions
            setCurPermissions(getCms().getPermissions(getParamResource(), userName));
    
            // check if the current resource is a folder
            CmsResource resource = getCms().readFileHeader(getParamResource());
            if (resource.isFolder()) {
                setInherit(true);
            }
        } catch (CmsException e) { }

        // check the current users permission to change access control entries
        try {
            if (getCms().isAdmin() || ((m_curPermissions.getAllowedPermissions() & I_CmsConstants.C_PERMISSION_CONTROL) > 0
                && !((m_curPermissions.getDeniedPermissions() & I_CmsConstants.C_PERMISSION_CONTROL) > 0))) {
                    setEditable(true);
            }
        } catch (CmsException e) { }
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
            getCms().rmacc(file, type, name);
            return true;
        } catch (CmsException e) {
            m_errorMessages.add(key("dialog.permission.error.remove"));
            return false;
        }
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
        } catch (Exception e) { }
               
        if (checkNewEntry(name, arrayPosition)) {
            try {
                getCms().chacc(file, getTypes()[arrayPosition], name, "");
                return true;
            } catch (CmsException e) {
                m_errorMessages.add(key("dialog.permission.error.add"));
            }
        }
        return false;
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
        String inherit = request.getParameter("inherit");   
        String overWriteInherited = request.getParameter("overwriteinherited");   
        
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
            } catch (Exception e) { }
            try {           
                param = request.getParameter(value+PERMISSION_DENY);
                paramInt = Integer.parseInt(param);
                denyValue |= paramInt;
            } catch (Exception e) { }
            
        }
       
        // get the current Ace to get the current ace flags
        try {
            Vector allEntries = getCms().getAccessControlEntries(file, false);
            int flags = 0;           
            for (int k=0; k< allEntries.size(); k++) {
                CmsAccessControlEntry curEntry = (CmsAccessControlEntry)allEntries.elementAt(k);
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
            
            // try to change the access entry           
            getCms().chacc(file, type, name, allowValue, denyValue, flags);
            return true;
        } catch (CmsException e) {
            m_errorMessages.add(key("dialog.permission.error.modify"));
            return false;
        }       
    }
    
    /**
     * @see #buildPermissionEntryForm(CmsAccessControlEntry, boolean, boolean, String).<p>
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
                } catch (CmsException exc) { }
            }
            CmsResource res = getCms().readFileHeader(fileName);
            CmsUUID fileId = res.getFileId();
            CmsAccessControlEntry entry = new CmsAccessControlEntry(fileId, id, curSet, flags);
            return buildPermissionEntryForm(entry, editable, extendedView, null);
        } catch (CmsException e) {
            return new StringBuffer("");
        }
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
        StringBuffer retValue = new StringBuffer("");
        
        // get name and type of the current entry
        String name = "";
        try {
            name = getCms().lookupPrincipal(entry.getPrincipal()).getName();
        } catch (CmsException e) { }
        String type = getEntryType(entry.getFlags());
        
        // set the parameters for the hidden fields
        setParamType(type);
        setParamName(name);
        
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
        retValue.append(dialogRow(HTML_START));
        if (extendedView) {
            // for extended view, add toggle symbol and link to output
            retValue.append("<a href=\"javascript:toggleDetail('"+type+name+entry.getResource()+"');\">");
            retValue.append("<img src=\""+getSkinUri()+"buttons/plus.gif\" class=\"noborder\" id=\"ic-"+type+name+entry.getResource()+"\"></a>");
        }
        retValue.append("<img src=\""+getSkinUri()+"buttons/");
        retValue.append(typeImg);
        retValue.append("_sm.gif\" class=\"noborder\" width=\"16\" height=\"16\" alt=\"");       
        retValue.append(typeLocalized);
        retValue.append("\" title=\"");      
        retValue.append(typeLocalized);
        retValue.append("\">&nbsp;<span class=\"textbold\">");
        retValue.append(name);
        retValue.append("</span>");
        
        if (extendedView) {
            // for extended view, add short permissions and hidden div
            retValue.append("&nbsp;("+entry.getPermissions().getPermissionString()+")");
            retValue.append(dialogRow(HTML_END));
            // show the resource from which the ace is inherited if present
            if (inheritRes != null && !"".equals(inheritRes)) {
                retValue.append("<div class=\"dialogpermissioninherit\">"+key("dialog.permission.list.inherited")+"  ");
                retValue.append(inheritRes);
                retValue.append("</div>\n");
            }
            retValue.append("<div id =\""+type+name+entry.getResource()+"\" class=\"hide\">");
        } else {
            retValue.append(dialogRow(HTML_END));
        }
        
        retValue.append("<table class=\"dialogpermissiondetails\">\n");
        
        // build the form depending on the editable flag
        if (editable) {
            retValue.append("<form action=\""+getDialogUri()+"\" method=\"post\" class=\"nomargin\" name=\"set"+type+name+entry.getResource()+"\">\n");
            // set parameters to show correct hidden input fields
            setParamAction(DIALOG_SET);
            retValue.append(paramsAsHidden());
            // inherit permissions on folders
            if (getInherit()) {
                retValue.append("<input type=\"hidden\" name=\"inherit\" value=\"true\"\n");
            }
        } else {
            retValue.append("<form class=\"nomargin\">\n");
        }
        
        // build headings for permission descriptions
        retValue.append("<tr>\n");
        retValue.append("\t<td class=\"dialogpermissioncell\"><span class=\"textbold\" unselectable=\"on\">"+key("dialog.permission.list.permission")+"</span></td>\n");
        retValue.append("\t<td class=\"dialogpermissioncell textcenter\"><span class=\"textbold\" unselectable=\"on\">"+key("dialog.permission.list.allowed")+"</span></td>\n");
        retValue.append("\t<td class=\"dialogpermissioncell textcenter\"><span class=\"textbold\" unselectable=\"on\">"+key("dialog.permission.list.denied")+"</span></td>\n");
        retValue.append("</tr>");
        
        Iterator i = m_permissionKeys.iterator();
        
        // show all possible permissions in the form
        while (i.hasNext()) {
            String key = (String)i.next();
            int value = CmsPermissionSet.getPermissionValue(key);
            String keyMessage = getSettings().getMessages().key(key);
            retValue.append("<tr>\n");
            retValue.append("\t<td class=\"dialogpermissioncell\">"+keyMessage+"</td>\n");
            retValue.append("\t<td class=\"dialogpermissioncell textcenter\"><input type=\"checkbox\" name=\""+value+PERMISSION_ALLOW+"\" value=\""+value+"\""+disabled);
            if (isAllowed(permissions, value)) {
                retValue.append(" checked=\"checked\"");
            }
            retValue.append("></td>\n");
            retValue.append("\t<td class=\"dialogpermissioncell textcenter\"><input type=\"checkbox\" name=\""+value+PERMISSION_DENY+"\" value=\""+value+"\""+disabled);
            if (isDenied(permissions, value)) {
                retValue.append(" checked=\"checked\"");
            }
            retValue.append("></td>\n");
            retValue.append("</tr>\n");
        }  
        
        // show overwrite checkbox and buttons only for editable entries
        if (editable) {
        
            // show overwrite inherited checkbox
            retValue.append("<tr>\n");
            retValue.append("\t<td class=\"dialogpermissioncell\">"+key("dialog.permission.list.overwrite")+"</td>\n");
            retValue.append("\t<td class=\"dialogpermissioncell textcenter\"><input type=\"checkbox\" name=\"overwriteinherited\" value=\"true\""+disabled);
            if (isOverWritingInherited(entry.getFlags())) {
                retValue.append(" checked=\"checked\"");           
            }
            retValue.append("></td>\n"); 
            retValue.append("\t<td class=\"dialogpermissioncell\">&nbsp;</td>\n");
            retValue.append("</tr>\n");    
                 
            
            // show "set" and "delete" buttons    
            retValue.append("<tr>\n");
            retValue.append("\t<td>&nbsp;</td>\n");
            retValue.append("\t<td class=\"textcenter\"><input class=\"dialogbutton\" type=\"submit\" value=\""+key("button.submit")+"\"></form></td>\n");           
            retValue.append("\t<td class=\"textcenter\">\n");
            // build the form for the "delete" button            
            retValue.append("\t\t<form class=\"nomargin\" action=\""+getDialogUri()+"\" method=\"post\" name=\"delete"+type+name+entry.getResource()+"\">\n");
            // set parameters to show correct hidden input fields
            setParamAction(DIALOG_DELETE);
            retValue.append(paramsAsHidden());
            retValue.append("\t\t<input class=\"dialogbutton\" type=\"submit\" value=\""+key("button.delete")+"\">\n");
            retValue.append("\t\t</form>\n");            
            retValue.append("\t</td>\n");
            retValue.append("</tr>\n");         
        } else {
            // close the form
            retValue.append("</form>\n");
        }
   
        retValue.append("</table>\n");
        if (extendedView) {
            // close the hidden div for extended view
            retValue.append("</div>");
        }
          
        return retValue;
    }
    
    /**
     * Builds a StringBuffer with HTML code to show a list of all inherited access control entries.<p>
     * 
     * @param entries ArrayList with all entries to show for the long view
     * @param parents Map of parent resources needed to get the connected resources for the detailed view
     * @return StringBuffer with HTML code for all entries
     */
    private StringBuffer buildInheritedList(ArrayList entries, Map parents) {       
        StringBuffer retValue = new StringBuffer("");
        String view = getSettings().getPermissionDetailView();       
        Iterator i;

        // display the long view
        if ("long".equals(view)) {
            i = entries.iterator();
            while (i.hasNext()) {
                CmsAccessControlEntry curEntry = (CmsAccessControlEntry)i.next();
                // build the list with enabled extended view and resource name
                retValue.append(buildPermissionEntryForm(curEntry, false, true, getConnectedResource(curEntry, parents)));
            }
        } else {
            // show the short view, use an ACL to build the list
            try {
                // get the inherited ACL of the parent folder 
                String parentUri = com.opencms.file.CmsResource.getParentFolder(getParamResource());
                CmsAccessControlList acList = getCms().getAccessControlList(parentUri, true);
                Set principalSet = acList.getPrincipals();
                i = principalSet.iterator();
                while (i.hasNext()) {
                    CmsUUID principalId = (CmsUUID)i.next();                   
                    I_CmsPrincipal principal = getCms().lookupPrincipal(principalId);
                    CmsPermissionSet permissions = acList.getPermissions(principal);
                    // build the list with enabled extended view only
                    retValue.append(buildPermissionEntryForm(principalId, permissions, false, true));
                }
            } catch (CmsException e) { }
        }
        return retValue;
    }
    
    /**
     * Builds a StringBuffer with HTML code for the access control entries of a resource.<p>
     * 
     * @param entries all access control entries for the resource
     * @return StringBuffer with HTML code for all entries
     */
    private StringBuffer buildResourceList(ArrayList entries) {
        StringBuffer retValue = new StringBuffer(1024);
        Iterator i = entries.iterator();
        boolean entriesPresent = false;
        
        // only add white box if there are entries!
        if (i.hasNext()) {
            entriesPresent = true;
            // create headline for resource entries
            retValue.append(dialogSubheadline(key("dialog.permission.headline.resource")));
            retValue.append(dialogWhiteBox(HTML_START));
        }
        
        // list all entries
        while (i.hasNext()) {
            CmsAccessControlEntry curEntry = (CmsAccessControlEntry)i.next();
            retValue.append(buildPermissionEntryForm(curEntry, this.getEditable(), false, null));
            if (i.hasNext()) {
                retValue.append(dialogSeparator()); 
            }
        }
        
        // only close white box if there are entries!
        if (entriesPresent) {
            retValue.append(dialogWhiteBox(HTML_END));
        }
        return retValue;
    }
    
    /**
     * Builds a String with HTML code to display the inherited and own access control entries of a resource.<p>
     * 
     * @return HTML code for inherited and own entries of the current resource
     */
    public String buildRightsList() {
        StringBuffer retValue = new StringBuffer(2048);
        
        // create headline for inherited entries
        retValue.append(dialogSubheadline(key("dialog.permission.headline.inherited")));
        
        // create detail view selector 
        retValue.append("<table border=\"0\">\n<tr>\n");
        retValue.append("\t<td>"+key("dialog.permission.viewselect")+"</td>\n");
        String selectedView = getSettings().getPermissionDetailView();   
        retValue.append("\t<form action=\""+getDialogUri()+"\" method=\"post\" name=\"selectshortview\">\n");            
        retValue.append("\t<td>\n");
        retValue.append("\t<input type=\"hidden\" name=\"view\" value=\"short\">\n");
        // set parameters to show correct hidden input fields
        setParamAction(null);
        retValue.append(paramsAsHidden());
        retValue.append("\t<input  type=\"submit\" class=\"dialogbutton\" value=\""+key("button.short")+"\"");
        if (!"long".equals(selectedView)) {
            retValue.append(" disabled=\"disabled\"");
        }
        retValue.append(">\n");
        retValue.append("\t</td>\n");
        retValue.append("\t</form>\n\t<form action=\""+getDialogUri()+"\" method=\"post\" name=\"selectlongview\">\n");
        retValue.append("\t<td>\n");
        retValue.append("\t<input type=\"hidden\" name=\"view\" value=\"long\">\n");
        retValue.append(paramsAsHidden());
        retValue.append("\t<input type=\"submit\" class=\"dialogbutton\" value=\""+key("button.long")+"\"");
        if ("long".equals(selectedView)) {
            retValue.append(" disabled=\"disabled\"");
        }
        retValue.append(">\n");
        retValue.append("\t</td>\n\t</form>\n");
        retValue.append("</tr>\n</table>\n");

        // get all access control entries of the current file
        Vector allEntries = new Vector();
        try {
            allEntries = getCms().getAccessControlEntries(getParamResource(), true);
        } catch (CmsException e) { }
        
        // store all parent folder ids together with path in a map
        Map parents = new HashMap();
        String path = CmsResource.getParentFolder(getParamResource());
        List parentResources = new ArrayList();
        try {
            // get all parent folders of the current file
            parentResources = getCms().readPath(path, false);
        } catch (CmsException e) { }
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
            CmsAccessControlEntry curEntry = (CmsAccessControlEntry)allEntries.elementAt(i);
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
        retValue.append(dialogWhiteBox(HTML_START));
        retValue.append(buildInheritedList(inheritedEntries, parents));       
        retValue.append(dialogWhiteBox(HTML_END));
        
        // create the add user/group form
        retValue.append(buildAddForm());

        // create the resource entries box
        retValue.append(buildResourceList(ownEntries));
        
        return retValue.toString();
    }
    
    /**
     * Builds a String with HTML code to display the form to add a new access control entry for the current resource.<p>
     * 
     * @return HTML String with the form
     */
    private String buildAddForm() {
        StringBuffer retValue = new StringBuffer(256);
        
        // only display form if the current user has the "control" right
        if (getEditable()) { 
            retValue.append(dialogSpacer());
            retValue.append(dialogBlockStart(key("dialog.permission.headline.add")));

            // get all possible entry types
            ArrayList options = new ArrayList();
            ArrayList optionValues = new ArrayList();
            for (int i=0; i<getTypes().length; i++) {
                options.add(getTypesLocalized()[i]);
                optionValues.add(Integer.toString(i));
            }            

            // create the input form for adding an ace
            retValue.append("<form action=\""+getDialogUri()+"\" method=\"post\" name=\"add\" class=\"nomargin\">\n");
            // set parameters to show correct hidden input fields
            setParamAction(DIALOG_ADDACE);
            setParamType(null);
            setParamName(null);
            retValue.append(paramsAsHidden());
            retValue.append("<table border=\"0\" width=\"100%\">\n");
            retValue.append("<tr>\n");
            retValue.append("\t<td>"+buildSelect("name=\"type\"", options, optionValues, -1)+"</td>\n");
            retValue.append("\t<td class=\"maxwidth\"><input type=\"text\" class=\"maxwidth\" name=\"name\" value=\"\"></td>\n");            
            retValue.append("\t<td><input class=\"dialogbutton\" style=\"width: 60px;\" type=\"button\" value=\""+key("button.search")+"\" onClick=\"javascript:openDialogWin('chaccbrowser.html','UserGroup');\"></td>\n");
            retValue.append("\t<td><input class=\"dialogbutton\" type=\"submit\" value=\""+key("input.add")+"\"></td>\n");
            retValue.append("</tr>\n");
            retValue.append("</form>\n");
            retValue.append("</table>\n");          
            
            retValue.append(dialogBlockEnd());      
        }
        return retValue.toString();
    }
    
    /**
     * Builds a String with HTML code to display the users access rights for the current resource.<p>
     * 
     * @return HTML String with the access rights of the current user
     */
    public String buildCurrentPermissions() {
        return buildPermissionEntryForm(getSettings().getUser().getId(), getCurPermissions(), false, false).toString();
    }
    
    /**
     * Returns the error messages if something went wrong.<p>
     *  
     * @return all error messages
     */
    public String buildErrorMessages() {
        StringBuffer retValue = new StringBuffer("");
        String errorMessages = getErrorMessagesString();
        if (!"".equals(errorMessages)) {
            retValue.append(dialogBlock(HTML_START, key("dialog.permission.error.headline"), true));
            retValue.append(errorMessages);
            retValue.append(dialogBlockEnd());
        }
        return retValue.toString();
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
            if ((flags & I_CmsConstants.C_ACCESSFLAGS_OVERWRITE) > 0) {
                return true;
            }
            return false;
        }
    
    /**
     * Returns a String with all error messages occuring when trying to add a new access control entry.<p>
     * 
     * @return String with error messages, separated by &lt;br&gt;
     */
    public String getErrorMessagesString() {
        StringBuffer errors = new StringBuffer("");
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
     * Returns a list with all error messages which occured when trying to add a new access control entry.<p>
     * 
     * @return List of error message Strings
     */
    public ArrayList getErrorMessages() {
        return m_errorMessages;
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
     * Returns a String array with the possible entry types.<p>
     * 
     * @return the possible types
     */
    protected String[] getTypes() {
        return m_types;
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
     * Returns an int array with possible entry types.<p>
     * 
     * @return the possible types as int array
     */
    protected int[] getTypesInt() {
        return m_typesInt;
    }
    
    /**
     * Determines the type of the current access control entry.<p>
     * 
     * @param flags the value of the current flags
     * @return String representation of the ace type
     */
    protected String getEntryType(int flags) {
        for (int i=0; i<getTypes().length; i++) {
            if ((flags & getTypesInt()[i]) > 0) return getTypes()[i];
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
            if ((flags & getTypesInt()[i]) > 0) return i;
        }
        return -1;
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
     * Returns the current users permission set on the resource.<p>
     * 
     * @return the users permission set
     */
    public CmsPermissionSet getCurPermissions() {
        return m_curPermissions;
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
     * Returns the current editable flag for the user to change ACEs.<p>
     * 
     * @return true if user can edit the permissions, otherwise false
     */
    protected boolean getEditable() {
        return m_editable;
    }
    
    /**
     * Sets the flag if the access control entry is inherited and can overwrite inherited permissions.
     * This is set in the init() method.<p>
     * 
     * @param value set to true for folders, otherwise false
     */
    protected void setInherit(boolean value) {
        m_inherit = value;
    }
    
    /**
     * Returns the current inheritance flag for the resource.<p>
     * 
     * @return true to show the checkbox, otherwise false
     */
    protected boolean getInherit() {
        return m_inherit;
    }
      
}
