/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editor/Attic/CmsEditorActionDefault.java,v $
 * Date   : $Date: 2004/01/22 10:39:36 $
 * Version: $Revision: 1.11 $
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
package org.opencms.workplace.editor;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;
import com.opencms.flex.jsp.CmsJspActionElement;
import com.opencms.util.Encoder;
import com.opencms.workplace.I_CmsWpConstants;

import org.opencms.lock.CmsLock;
import org.opencms.main.OpenCms;
import org.opencms.page.CmsXmlPage;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceAction;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;
import com.opencms.flex.jsp.CmsJspActionElement;
import com.opencms.util.Encoder;
import com.opencms.workplace.I_CmsWpConstants;

import java.io.IOException;

import javax.servlet.jsp.JspException;

/**
 * Provides a method to perform a user defined action when editing a page.<p> 
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.11 $
 * 
 * @since 5.3.0
 */
public class CmsEditorActionDefault implements I_CmsEditorActionHandler {

    /**
     * Default constructor needed for editor action handler implementation.<p>
     */
    public CmsEditorActionDefault() {
        // empty constructor
    }
    
    /**
     * @see org.opencms.workplace.editor.I_CmsEditorActionHandler#editorAction(org.opencms.workplace.editor.CmsDefaultPageEditor, com.opencms.flex.jsp.CmsJspActionElement)
     */
    public void editorAction(CmsDefaultPageEditor editor, CmsJspActionElement jsp) throws IOException, JspException {
        // save the edited content
        editor.actionSave();
        // delete temporary file and unlock resource in direct edit mode
        editor.actionClear(true);
        // create the publish link to redirect to
        String publishLink = jsp.link(I_CmsWpConstants.C_VFS_PATH_WORKPLACE + "jsp/dialogs/publishresource.html");
        // define the parameters which are necessary for publishing the resource 
        String params = "?resource=" + editor.getParamResource() + "&action=" + CmsDialog.DIALOG_CONFIRMED;
        params += "&title=" + Encoder.escapeWBlanks(editor.key("messagebox.title.publishresource") + ": " + editor.getParamResource(), Encoder.C_UTF8_ENCODING) + "&oklink=";
        if ("true".equals(editor.getParamDirectedit())) {
            String linkTarget;
            if (!"".equals(editor.getParamBacklink())) {
                linkTarget = jsp.link(editor.getParamBacklink());
            } else {
                linkTarget = jsp.link(editor.getParamResource());
            }
            // append the parameters and the report "ok" button action to the link
            publishLink += params + Encoder.escapeWBlanks("onclick=\"location.href('" + linkTarget + "');\"", Encoder.C_UTF8_ENCODING);
        } else {
            // append the parameters and the report "ok" button action to the link
            publishLink += params + Encoder.escapeWBlanks("onclick=\"location.href('" + jsp.link(CmsWorkplaceAction.C_JSP_WORKPLACE_URI) + "');\"", Encoder.C_UTF8_ENCODING);
       
        }
        // redirect to the publish dialog with all necessary parameters
        jsp.getResponse().sendRedirect(publishLink); 
    }
    
    /**
     * @see org.opencms.workplace.editor.I_CmsEditorActionHandler#getButtonName()
     */
    public String getButtonName() {
        return "explorer.context.publish";
    }
    
    /**
     * @see org.opencms.workplace.editor.I_CmsEditorActionHandler#getButtonUrl(com.opencms.flex.jsp.CmsJspActionElement, java.lang.String)
     */
    public String getButtonUrl(CmsJspActionElement jsp, String resourceName) {
        // get the button image
        String button = I_CmsWpConstants.C_VFS_PATH_WORKPLACE + "skins/modern/buttons/publish";
        if (!isButtonActive(jsp, resourceName)) {
            // show disabled button if not active
            button += "_in";
        }
        return jsp.link(button);
    }
    
    /**
     * @see org.opencms.workplace.editor.I_CmsEditorActionHandler#isButtonActive(com.opencms.flex.jsp.CmsJspActionElement, java.lang.String)
     */
    public boolean isButtonActive(CmsJspActionElement jsp, String resourceName) {
        try {
            //get the lock type of the resource
            org.opencms.lock.CmsLock lock = jsp.getCmsObject().getLock(resourceName);
            int lockType = lock.getType();
            if (lockType == CmsLock.C_TYPE_INHERITED || lockType == CmsLock.C_TYPE_SHARED_INHERITED) {
                //lock is inherited, unlocking & publishing not possible, so disable button 
                return false;
            }
            if (!jsp.getCmsObject().isManagerOfProject()) {
                // user has no right to publish the resource, so disable the button
                return false;
            }
        } catch (CmsException e) {
            // getting the lock went wrong, disable button
            return false;
        }
        return true;
    }
    
    /**
     * @see org.opencms.workplace.editor.I_CmsEditorActionHandler#getEditMode(com.opencms.file.CmsObject, java.lang.String, org.opencms.page.CmsXmlPage, java.lang.String)
     */
    public String getEditMode(CmsObject cmsObject, String filename, CmsXmlPage page, String element) {
    
        try {
            
            CmsResource res = cmsObject.readFileHeader(filename);
            int currentProject = cmsObject.getRequestContext().currentProject().getId();
            CmsUUID userId = cmsObject.getRequestContext().currentUser().getId();
            CmsLock lock = cmsObject.getLock(filename);
            boolean locked = !(lock.isNullLock() || (lock.getUserId().equals(userId) && lock.getProjectId() == currentProject));
        
            if (currentProject == I_CmsConstants.C_PROJECT_ONLINE_ID) {
                // don't render edit area in online project
                return null;
            } else if (!cmsObject.getResourceType(res.getType()).isDirectEditable()) {
                // don't render edit area for non-editable resources 
                return null;
            } else if (CmsResource.getName(filename).startsWith(com.opencms.core.I_CmsConstants.C_TEMP_PREFIX)) {
                // don't show edit area on temporary file
                return C_EDITMODE_INACTIVE;
            } else if (!cmsObject.isInsideCurrentProject(res)) {
                // don't show edit area on files not belonging to the current project
                return C_EDITMODE_INACTIVE;
            } else if (!cmsObject.hasPermissions(res, new CmsPermissionSet(I_CmsConstants.C_PERMISSION_WRITE))) {
                // don't show edit area on files without write permissions
                if (locked) {
                    return C_EDITMODE_DISABLED;
                } else {
                    return C_EDITMODE_INACTIVE;
                }
            } else if (locked) {
                return C_EDITMODE_DISABLED;
            }
  
            // check if the desired element is available (in case of xml page)
            if (page != null && element != null) {
                String localeName = OpenCms.getLocaleManager().getBestMatchingLocaleName(null, OpenCms.getLocaleManager().getDefaultLocaleNames(cmsObject, filename), page.getLanguages());
                if (!page.hasElement(element, localeName) || !page.isEnabled(element, localeName)) {
                    return C_EDITMODE_INACTIVE;
                }
            }

            // otherwise the resource is editable
            return C_EDITMODE_ENABLED;
            
        }  catch (CmsException exc) {
            
            // something went wrong - so the resource seems not to be editable
            return C_EDITMODE_INACTIVE;
        }
    }

}
