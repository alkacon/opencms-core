/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/CmsChacc.java,v $
 * Date   : $Date: 2003/06/25 16:12:50 $
 * Version: $Revision: 1.1 $
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
import com.opencms.file.CmsFolder;
import com.opencms.file.CmsResource;
import com.opencms.flex.jsp.CmsJspActionElement;
import com.opencms.flex.util.CmsUUID;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsAccessControlList;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.I_CmsPrincipal;

/**
 * Provides methods for building the permission settings dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/jsp/chacc_html
 * </ul>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 5.1
 */
public class CmsChacc extends CmsWorkplace {
    
    /** Stores the URL to the JSP which uses this class */
    private String m_chaccUrl;
    
    /** Stores eventual error message Strings */
    private ArrayList m_errorMessages = new ArrayList(); 
    
    /** The possible types of new access control entries */
    private String[] m_types = {"Group", "User"};
    
    /** The possible type values of access control entries */
    private int[] m_typesInt = {I_CmsConstants.C_ACCESSFLAGS_GROUP, I_CmsConstants.C_ACCESSFLAGS_USER};
    
    /** Indicates if forms are editable by current user */
    private boolean m_editable;
    
    /** Indicates if inheritance flags are displayed for resource */
    private boolean m_showinherit;
    
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
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected synchronized void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
        // the file which is viewed/modified
        String fileUri = (String)request.getParameter("file");
        if (fileUri != null) {
            settings.setFileUri(fileUri);
        }
        // the detail mode of the "inherited" list
        String detail = (String)request.getParameter("view");
        if (detail != null) {
            settings.setDetailView(detail);
        }                      
    }
    
    /**
     * Initializes some member variables to display the form with the right options for the current user.<p>
     * 
     * This method must be called after initWorkplaceRequestValues().<p>
     *  
     * @param request the Http Servlet request
     */
    public void init(HttpServletRequest request) {
        // the URL of the chacc JSP
        setChaccUrl(getCms().getLinkSubstitution("/system/workplace/jsp/chacc.html"));
        
        // the currently viewed file
        String file = getSettings().getFileUri();

        // the current user name
        String userName = getCms().getRequestContext().currentUser().getName();
        
        // set flags to show editable or non editable entries
        setEditable(false);
        setShowInherit(false);
                 
        try {      
            // get the current users' permissions
            setCurPermissions(getCms().getPermissions(file, userName));
    
            // check if the current resource is a folder
            CmsResource resource = getCms().readFileHeader(file);
            if (resource.isFolder()) {
                setShowInherit(true);
            }
        } catch (CmsException e) {}

        // check the current users permission to change access control entries
        if ((m_curPermissions.getAllowedPermissions() & I_CmsConstants.C_PERMISSION_CONTROL) > 0
            && !((m_curPermissions.getDeniedPermissions() & I_CmsConstants.C_PERMISSION_CONTROL) > 0)) {
                setEditable(true);
        }
    }
    
    /**
     * Removes a present access control entry from a resource.<p>
     * 
     * @param request the http request
     * @return true if the ace was successfully removed, otherwise false
     */
    private boolean removeAce(HttpServletRequest request) {
        String file = getSettings().getFileUri();
        String name = (String)request.getParameter("name");
        String type = (String)request.getParameter("type");
        try {
            getCms().rmacc(file, type, name);
            return true;
        } catch (CmsException e) {
            return false;
        }
    }
    
    /**
     * Adds a new access control entry to a resource.<p>
     * 
     * @param request the http request
     * @return true if a new ace was created, otherwise false
     */
    private boolean addNewAce(HttpServletRequest request) {
        String file = getSettings().getFileUri();
        String name = (String)request.getParameter("name");
        String type = (String)request.getParameter("type");
        if (checkNewEntry(name, type)) {
            try {
                getCms().chacc(file, type, name, "");
                return true;
            }
            catch (CmsException e) {
                m_errorMessages.add("Failed to create a new access control entry.");
            }
        }
        return false;
    }

    /**
     * Check method to validate the user input when creating a new access control entry.<p>
     * 
     * @param name the name of the new user/group
     * @param type the type of the new entry
     * @return true if everything is ok, otherwise false
     */
    protected boolean checkNewEntry(String name, String type) {
        m_errorMessages.clear();
        boolean inArray = false;
        String[] allTypes = getTypes();
        for (int i=0; i<allTypes.length; i++) {
            if (allTypes[i].equals(type)) {
                inArray = true;
                break;
            }
        }
        if (!inArray) {
            m_errorMessages.add("Please select the type of the new entry: \"Group\" or \"User\".");
        }
        if (name == null || "".equals(name.trim())) {
            m_errorMessages.add("Please enter a group or user name.");
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
    private boolean modifyAce(HttpServletRequest request) {      
        String file = getSettings().getFileUri();  
        
        // get request parameters
        String name = (String)request.getParameter("name");
        String type = (String)request.getParameter("type");
        String inherit = (String)request.getParameter("inherit");   
        String overWriteInherited = (String)request.getParameter("overwriteinherited");   
        
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
                param = (String)request.getParameter(value+"allow");
                paramInt = Integer.parseInt(param);
                allowValue |= paramInt;
            } catch (Exception e) {}
            try {           
                param = (String)request.getParameter(value+"deny");
                paramInt = Integer.parseInt(param);
                denyValue |= paramInt;
            } catch (Exception e) {}
            
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
            }
            else {
                flags &= ~I_CmsConstants.C_ACCESSFLAGS_INHERIT;
            }
            
            // modify the ace flags to determine overwriting of inherited ace
            if ("true".equals(overWriteInherited)) {
                flags |= I_CmsConstants.C_ACCESSFLAGS_OVERWRITE;
            }
            else {
                flags &= ~I_CmsConstants.C_ACCESSFLAGS_OVERWRITE;
            }
            
            // try to change the access entry           
            getCms().chacc(file, type, name, allowValue, denyValue, flags);
            return true;
        }
        catch (CmsException e) {
            return false;
        }       
    }
    
    /**
     * @see #buildPermissionEntryForm(CmsObject, CmsPermissionSet, boolean, boolean).<p>
     *
     * @param id the UUID of the principal of the permission set
     * @param curSet the current permission set 
     * @param editable boolean to determine if the form is editable
     * @param showinherit boolean to determine if the "inherit" checkbox should be displayed
     * @return String with HTML code of the form
     */
    public StringBuffer buildPermissionEntryForm(CmsUUID id, CmsPermissionSet curSet, boolean editable, boolean showinherit) {
        String fileName = getSettings().getFileUri();
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
            } catch (CmsException exc) {}}
            CmsResource res = getCms().readFileHeader(fileName);
            CmsUUID fileId = res.getFileId();
            CmsAccessControlEntry entry = new CmsAccessControlEntry(fileId, id, curSet, flags);
            return buildPermissionEntryForm(entry, editable, showinherit);
        } catch (CmsException e) {
            return new StringBuffer("");
        }
    }
    
    /**
     * Creates an HTML input form for the current access control entry.<p>
     * 
     * @param entry the current access control entry
     * @param editable boolean to determine if the form is editable
     * @param showinherit boolean to determine if the "inherit" checkbox should be displayed
     * @return String with HTML code of the form
     */
    private StringBuffer buildPermissionEntryForm(CmsAccessControlEntry entry, boolean editable, boolean showinherit) {
        StringBuffer retValue = new StringBuffer("");
        
        // get name and type of current entry
        String name = "";
        try {
            name = getCms().lookupPrincipal(entry.getPrincipal()).getName();
        } catch (CmsException e) {}
        String type = getEntryType(entry.getFlags());
        
        // get all permissions of the current entry
        CmsPermissionSet permissions = entry.getPermissions();
        
        // build String for disabled check boxes
        String disabled = "";
        if (!editable) {
            disabled = " disabled=\"disabled\"";
        }
        
        // build the headings
        retValue.append("<tr>\n");
        retValue.append("\t<td colspan=\"2\"><b>"+type+": "+name+"</b></td>\n");
        
        // show "delete" button if entry is editable
        if (editable) {
            retValue.append("\t<form action=\""+getChaccUrl()+"\" method=\"post\" name=\"delete"+type+name+entry.getResource()+"\">\n");
            retValue.append("\t<input type=\"hidden\" name=\"name\" value=\""+name+"\">\n");    
            retValue.append("\t<input type=\"hidden\" name=\"type\" value=\""+type+"\">\n");
            retValue.append("\t<input type=\"hidden\" name=\"action\" value=\"delete\">\n");
            retValue.append("\t<td align=\"right\"><input class=\"button\" width=\"100\" type=\"submit\" value=\"Delete\"></td>\n");
            retValue.append("\t</form>\n");
        }
        else {
            retValue.append("\t<td>&nbsp;</td>\n");
        }
        retValue.append("</tr>\n<tr>\n");
        retValue.append("\t<td>Permission</td>\n\t<td>Allowed</td>\n\t<td>Denied</td>\n</tr>\n");
        
        // build the form depending on editable flag
        if (editable) {
            retValue.append("\t<form action=\""+getChaccUrl()+"\" method=\"post\" name=\"set"+type+name+entry.getResource()+"\">\n");
            retValue.append("\t<input type=\"hidden\" name=\"name\" value=\""+name+"\">\n");    
            retValue.append("\t<input type=\"hidden\" name=\"type\" value=\""+type+"\">\n");
            retValue.append("\t<input type=\"hidden\" name=\"action\" value=\"set\">\n");
            if (showinherit) {
                retValue.append("\t<input type=\"hidden\" name=\"inherit\" value=\"true\"\n");
            }
        }
        else {
            retValue.append("\t<form>\n");
        }
        Iterator i = m_permissionKeys.iterator();
        
        // show all possible permissions in the form
        while (i.hasNext()) {
            String key = (String)i.next();
            int value = CmsPermissionSet.getPermissionValue(key);
            String keyMessage = getSettings().getMessages().key(key);
            retValue.append("<tr>\n");
            retValue.append("\t<td><b>"+keyMessage+"</b></td>\n");
            retValue.append("\t<td><input type=\"checkbox\" name=\""+value+"allow\" value=\""+value+"\""+disabled);
            if (isAllowed(permissions, value)) {
                retValue.append(" checked=\"checked\"");
            }
            retValue.append("></td>\n");
            retValue.append("\t<td><input type=\"checkbox\" name=\""+value+"deny\" value=\""+value+"\""+disabled);
            if (isDenied(permissions, value)) {
                retValue.append(" checked=\"checked\"");
            }
            retValue.append("></td>\n");
            retValue.append("</tr>\n");
        }  
        
        // show inheritance checkbox only on folders
        if (showinherit) {
            retValue.append("<tr>\n");
            retValue.append("\t<td>&nbsp;</td>\n");
            retValue.append("\t<td><input type=\"checkbox\" name=\"overwriteinherited\" value=\"true\""+disabled);
            if (isOverWritingInherited(entry.getFlags())) {
                retValue.append(" checked=\"checked\"");           
            }
            retValue.append(">&nbsp;overwrite inherited</td>\n"); 
            retValue.append("\t<td>&nbsp;</td>\n");
            retValue.append("</tr>\n");    
        }                 
            
        // show "set" button depending on editable value 
        if (editable) {
            retValue.append("<tr>\n");
            retValue.append("\t<td colspan=\"3\" align=\"center\"><input class=\"button\" width=\"100\" type=\"submit\" value=\"Set\"></td>\n");
            retValue.append("</tr>\n");         
        }
     
        // close the form
        retValue.append("</form>\n");
        return retValue;
    }
    
    /**
     * Builds a StringBuffer with HTML code to show a list of all inherited access control entries.<p>
     * 
     * @param entries ArrayList with all entries to show for the long view
     * @return StringBuffer with HTML code for all entries
     */
    private StringBuffer buildInheritedList(ArrayList entries) {       
        StringBuffer retValue = new StringBuffer("");
        String view = getSettings().getDetailView();       
        Iterator i;

        // display the long view
        if ("long".equals(view)) {
            i = entries.iterator();
            while (i.hasNext()) {
                CmsAccessControlEntry curEntry = (CmsAccessControlEntry)i.next();
                retValue.append(buildPermissionEntryForm(curEntry, false, false));
                String connectedResource = getConnectedResource(curEntry);
                if (connectedResource != null) {                    
                    retValue.append("<tr><td colspan=\"3\">inherited from  ");
                    retValue.append(connectedResource);
                    retValue.append("</td></tr>\n");
                }
            }
        }
        
        // show the short view
        else {
            try {
                CmsAccessControlList acList = getCms().getAccessControlList(getSettings().getFileUri(), true);
                Set principalSet = acList.getPrincipals();
                i = principalSet.iterator();
                while (i.hasNext()) {
                    CmsUUID principalId = (CmsUUID)i.next();                   
                    I_CmsPrincipal principal = getCms().lookupPrincipal(principalId);
                    CmsPermissionSet permissions = acList.getPermissions(principal);
                    retValue.append(buildPermissionEntryForm(principalId, permissions, false, false));
                }
            } catch (CmsException e) {}
        }
        return retValue;
    }
    
    /**
     * Builds a StringBuffer with HTML code for the own access control entries of a resource.<p>
     * 
     * @param entries all access control entries for the resource
     * @param editable boolean to determine if the entries are editable for the current user
     * @param showinherit boolean to determine if the inheritance flag should be set and the overwrite inherited checkbox should be shown
     * @return StringBuffer with HTML code for all entries
     */
    private StringBuffer buildOwnList(ArrayList entries) {
        StringBuffer retValue = new StringBuffer("");
        Iterator i = entries.iterator();

        while (i.hasNext()) {
            CmsAccessControlEntry curEntry = (CmsAccessControlEntry)i.next();
            retValue.append(buildPermissionEntryForm(curEntry, m_editable, m_showinherit)); 
        }
        return retValue;
    }
    
    /**
     * Builds a String with HTML code to display the inherited and own access control entries of a resource.<p>
     * 
     * @param editable boolean to determine if the entries are editable for the current user
     * @param showinherit boolean to determine if the inheritance flag should be set and the overwrite inherited checkbox should be shown
     * @return String with HTML code for inherited and own entries of the current resource
     */
    public String buildRightsList() {
        StringBuffer retValue = new StringBuffer("");
        
        // create header and detail selector for inherited entries
        retValue.append("<tr>\n");
        retValue.append("\t<td colspan=\"3\"><h2>Inherited permissions</h2></td>\n");
        retValue.append("</tr>\n");
        retValue.append("<tr>\n");
        retValue.append("\t<td><b>Select view:</b></td>\n");
        
        String selectedView = getSettings().getDetailView();
                
        retValue.append("<form action=\""+getChaccUrl()+"\" method=\"post\" name=\"selectshortview\">\n");
        retValue.append("\t<input type=\"hidden\" name=\"view\" value=\"short\">\n");
        retValue.append("\t<td><input type=\"submit\" class=\"button\" width=\"80\" type=\"button\" value=\"Short\"");
        if (!"long".equals(selectedView)) {
            retValue.append(" disabled=\"disabled\"");
        }
        retValue.append("></td>\n");
        retValue.append("</form>\n");

        retValue.append("<form action=\""+getChaccUrl()+"\" method=\"post\" name=\"selectlongview\">\n");
        retValue.append("\t<input type=\"hidden\" name=\"view\" value=\"long\">\n");
        retValue.append("\t<td><input type=\"submit\" class=\"button\" width=\"80\" type=\"button\" value=\"Long\"");
        if ("long".equals(selectedView)) {
            retValue.append(" disabled=\"disabled\"");
        }
        retValue.append("></td>\n");
        retValue.append("</form>\n");

        retValue.append("</tr>\n");

        // get all access control entries of the current file
        Vector allEntries = new Vector();
        try {
            allEntries = getCms().getAccessControlEntries(getSettings().getFileUri(), true);
        } catch (CmsException e) {}

        // create new ArrayLists in which inherited and non inherited entries are stored
        ArrayList ownEntries = new ArrayList(0);
        ArrayList inheritedEntries = new ArrayList(0);

        for (int i=0; i<allEntries.size(); i++) {
            CmsAccessControlEntry curEntry = (CmsAccessControlEntry)allEntries.elementAt(i);
    
            if (curEntry.isInherited()) {
                // add the entry to the inherited rights list for the "long" view
                if ("long".equals(getSettings().getDetailView())) {       
                    inheritedEntries.add((CmsAccessControlEntry)curEntry);
                }
            }
    
            else {
                // add the entry to the own rights list
                ownEntries.add((CmsAccessControlEntry)curEntry);
            }
        }


        retValue.append(buildInheritedList(inheritedEntries));

        retValue.append("<tr>\n");
        retValue.append("\t<td colspan=\"3\"><h2>Resource permissions</h2></td>\n");
        retValue.append("</tr>");

        retValue.append(buildOwnList(ownEntries)); 
        return retValue.toString();
    }
    
    /**
     * Builds a String with HTML code to display the form to add a new access control entry for the current resource.<p>
     * 
     * @param editable boolean to determine if the form is shown for the current user
     * @return HTML String with the form
     */
    public String buildAddForm() {
        StringBuffer retValue = new StringBuffer("");
        
        // only display form if current user has the "control" right
        if (m_editable) { 
            retValue.append("<tr>\n");
            retValue.append("\t<td colspan=\"3\"><h2>Add a user/group access control entry</h2></td>\n");
            retValue.append("</tr>\n");

            // get all possible entry types
            ArrayList options = new ArrayList();
            for (int i=0; i<getTypes().length; i++) {
                options.add(getTypes()[i]);
            }            

            // create the input form
            retValue.append("<form action=\""+getChaccUrl()+"\" method=\"post\" name=\"add\">\n");
            retValue.append("<input type=\"hidden\" name=\"action\" value=\"addACE\">\n");
            retValue.append("<tr>\n");
            retValue.append("\t<td>"+buildSelect("name=\"type\"", options, options, -1)+"</td>\n");
            retValue.append("\t<td colspan=\"2\"><input type=\"text\" size=\"20\" name=\"name\" value=\"\">&nbsp;<input type=\"submit\" value=\"Add\"></td>\n");
            retValue.append("</tr>\n");
            retValue.append("</form>\n");        
        }
        return retValue.toString();
    }
    
    public String buildErrorMessages() {
        StringBuffer retValue = new StringBuffer("");
        String errorMessages = getErrorMessagesString();
        if (!"".equals(errorMessages)) {
            retValue.append("<tr><td colspan=\"3\">");
            retValue.append(errorMessages);
            retValue.append("</td></tr>\n");
        }
        return retValue.toString();
    }
    
    public boolean performAction(HttpServletRequest request) {
        String action = (String)request.getParameter("action");
        if (action == null && "".equals(action)) {
            return true;
        }
        if ("set".equals(action)) {
            return modifyAce(request);
        }
        if ("delete".equals(action)) {
            return removeAce(request);
        }
        if ("addACE".equals(action)) {
            return addNewAce(request);
        }
        return true;
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
     * Check if the current permissions are inherited.<p>
     * 
     * @param flags value of all flags of the current entry
     * @return true if permissions are inherited, otherwise false 
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
     * Returns a String with all error messages occuring when trying to add a new access control entry.<p>
     * 
     * @return String with error messages, separated by &lt;br&gt;
     */
    public String getErrorMessagesString() {
        StringBuffer errors = new StringBuffer("");
        Iterator i = m_errorMessages.iterator();
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
     * @return the resource name of the corresponding resource
     */
    protected String getConnectedResource(CmsAccessControlEntry entry) {
        // TODO: make this work and return the absolute path!
        CmsUUID resId = entry.getResource();
        try {
            CmsFolder folder = getCms().readFolder(resId, false);
            return folder.getAbsolutePath();
        } catch (CmsException e) {
            // return null;
            return resId.toString();
        }
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
    protected void setShowInherit(boolean value) {
        m_showinherit = value;
    }
    
    /**
     * Returns the current inheritance flag for the resource.<p>
     * 
     * @return true to show the checkbox, otherwise false
     */
    protected boolean getShowInherit() {
        return m_showinherit;
    }
    
    /**
     * Returns the URL of the corresponding workplace JSP (is set in init() method).<p>
     * 
     * @return the URL of the workplace JSP 
     */
    protected String getChaccUrl() {
        return m_chaccUrl;
    }
    
    /**
     * Sets the URL of the corresponding workplace JSP.
     * This is set in the init() method.<p>
     * 
     * @param value the URL of the workplace JSP
     */
    protected void setChaccUrl(String value) {
        m_chaccUrl = value;
    }
    
}
