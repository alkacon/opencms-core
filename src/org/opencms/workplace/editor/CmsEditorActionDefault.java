/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editor/Attic/CmsEditorActionDefault.java,v $
 * Date   : $Date: 2004/06/10 19:36:45 $
 * Version: $Revision: 1.28 $
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsResourceTypeXmlPage;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceAction;
import org.opencms.workplace.I_CmsWpConstants;
import org.opencms.xml.page.CmsXmlPage;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;

/**
 * Provides a method to perform a user defined action when editing a page.<p> 
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.28 $
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
     * @see org.opencms.workplace.editor.I_CmsEditorActionHandler#editorAction(org.opencms.workplace.editor.CmsDefaultPageEditor, CmsJspActionElement)
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
        params += "&reporttype=simple&directpublish=true&publishsiblings=true";
        params += "&title=" + CmsEncoder.escapeWBlanks(editor.key("messagebox.title.publishresource") + ": " + editor.getParamResource(), CmsEncoder.C_UTF8_ENCODING) + "&closelink=";
        if ("true".equals(editor.getParamDirectedit())) {
            String linkTarget;
            if (!"".equals(editor.getParamBacklink())) {
                linkTarget = jsp.link(editor.getParamBacklink());
            } else {
                linkTarget = jsp.link(editor.getParamResource());
            }
            // append the parameters and the report "ok" button action to the link
            publishLink += params + CmsEncoder.escapeWBlanks(linkTarget, CmsEncoder.C_UTF8_ENCODING);
        } else {
            // append the parameters and the report "ok" button action to the link
            publishLink += params + CmsEncoder.escapeWBlanks(jsp.link(CmsWorkplaceAction.C_JSP_WORKPLACE_URI), CmsEncoder.C_UTF8_ENCODING);
       
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
     * @see org.opencms.workplace.editor.I_CmsEditorActionHandler#getButtonUrl(CmsJspActionElement, java.lang.String)
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
     * @see org.opencms.workplace.editor.I_CmsEditorActionHandler#isButtonActive(CmsJspActionElement, java.lang.String)
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
     * @see org.opencms.workplace.editor.I_CmsEditorActionHandler#getEditMode(org.opencms.file.CmsObject, java.lang.String, java.lang.String, javax.servlet.ServletRequest)
     */
    public String getEditMode(CmsObject cmsObject, String filename, String element, ServletRequest req) {
    
        try {
            
            CmsResource resource = cmsObject.readFileHeader(filename, CmsResourceFilter.ALL);
            int currentProject = cmsObject.getRequestContext().currentProject().getId();
            CmsUUID userId = cmsObject.getRequestContext().currentUser().getId();
            CmsLock lock = cmsObject.getLock(filename);
            boolean locked = !(lock.isNullLock() || (lock.getUserId().equals(userId) && lock.getProjectId() == currentProject));
        
            if (currentProject == I_CmsConstants.C_PROJECT_ONLINE_ID) {
                // don't render direct edit button in online project
                return null;
            } else if (!cmsObject.getResourceType(resource.getType()).isDirectEditable()) {
                // don't render direct edit button for non-editable resources 
                return null;
            } else if (CmsResource.getName(filename).startsWith(org.opencms.main.I_CmsConstants.C_TEMP_PREFIX)) {
                // don't show direct edit button on temporary file
                return C_DIRECT_EDIT_MODE_INACTIVE;
            } else if (!cmsObject.isInsideCurrentProject(resource)) {
                // don't show direct edit button on files not belonging to the current project
                return C_DIRECT_EDIT_MODE_INACTIVE;
            } else if (!cmsObject.hasPermissions(resource, new CmsPermissionSet(I_CmsConstants.C_PERMISSION_WRITE))) {
                // don't show direct edit button on files without write permissions
                if (locked) {
                    return C_DIRECT_EDIT_MODE_DISABLED;
                } else {
                    return C_DIRECT_EDIT_MODE_INACTIVE;
                }
            } else if (locked) {
                return C_DIRECT_EDIT_MODE_DISABLED;
            }
              
            if ((element != null) && (resource.getType() == CmsResourceTypeXmlPage.C_RESOURCE_TYPE_ID)) {
                // check if the desired element is available (in case of xml page)
                CmsXmlPage page = (CmsXmlPage)req.getAttribute(filename);                    
                if (page == null) {
                    // make sure a page is only read once (not every time for each element)
                    page = CmsXmlPage.unmarshal(cmsObject, cmsObject.readFile(filename));
                    req.setAttribute(filename, page);
                }
                List locales = page.getLocales();
                Locale locale;
                if ((locales == null) || (locales.size() == 0)) {
                    locale = (Locale)OpenCms.getLocaleManager().getDefaultLocales(cmsObject, filename).get(0);                    
                } else { 
                    locale = OpenCms.getLocaleManager().getBestMatchingLocale(null, OpenCms.getLocaleManager().getDefaultLocales(cmsObject, filename), locales);
                }
                if (!page.hasElement(element, locale) || !page.isEnabled(element, locale)) {
                    return C_DIRECT_EDIT_MODE_INACTIVE;
                }                
            }

            // otherwise the resource is editable
            return C_DIRECT_EDIT_MODE_ENABLED;
            
        }  catch (CmsException e) {
            if (OpenCms.getLog(this).isWarnEnabled()) {
                OpenCms.getLog(this).warn("Error while calculation edit mode for " + filename, e);
            }
            // something went wrong - so the resource seems not to be editable
            return C_DIRECT_EDIT_MODE_INACTIVE;
        }
    }

}
