/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/CmsEditorFrameset.java,v $
 * Date   : $Date: 2005/02/16 11:43:02 $
 * Version: $Revision: 1.3 $
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
package org.opencms.workplace.editors;


import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplaceSettings;

import javax.servlet.http.HttpServletRequest;

/**
 * Helper class to create the editor frameset.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/jsp/editors/editor_html
 * </ul>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.3 $
 * 
 * @since 5.1.12
 */
public class CmsEditorFrameset extends CmsEditor {
    
    /** The title to be displayed in the editor. */
    private String m_paramEditorTitle;
    
    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsEditorFrameset(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
        // fill the parameter values in the get/set methods
        fillParamValues(request);
    }    
    
    /**
     * Deletes the temporary file and unlocks the edited resource when in direct edit mode.<p>
     * 
     * This method is needed in the editor close help frame, which is called when the user presses
     * the "back" button or closes the browser window when editing a page.<p>
     * 
     * @param forceUnlock if true, the resource will be unlocked anyway
     */
    public void actionClear(boolean forceUnlock) {
        // delete the temporary file        
        deleteTempFile();
        if ("true".equals(getParamDirectedit()) || forceUnlock) {
            // unlock the resource when in direct edit mode or force unlock is true
            try {
                getCms().unlockResource(getParamResource());
            } catch (CmsException e) {
                // should usually never happen
                if (OpenCms.getLog(this).isInfoEnabled()) {
                    OpenCms.getLog(this).info(e);
                }
            }
        }
    }
    
    /**
     * @see org.opencms.workplace.editors.CmsEditor#actionExit()
     */
    public final void actionExit() {
        // do nothing
    }
    
    /**
     * @see org.opencms.workplace.editors.CmsEditor#actionSave()
     */
    public final void actionSave() { 
        // do nothing
    }
    
    /**
     * @see org.opencms.workplace.editors.CmsEditor#getEditorResourceUri()
     */
    public final String getEditorResourceUri() {
        // return emtpy String
        return "";
    }
    
    /**
     * @see org.opencms.workplace.editors.CmsEditor#initContent()
     */
    protected final void initContent() {
        // do nothing
    }
    
    /**
     * Returns the editor title.<p>
     *
     * @return the editor title
     */
    public String getParamEditorTitle() {

        if (CmsStringUtil.isEmpty(m_paramEditorTitle)) {
            return key("editor.title.prefix") + " " + getParamResource();
        }
        return m_paramEditorTitle;
    }
    /**
     * Sets the editort title.<p>
     *
     * @param editorTitle the editor title to set
     */
    public void setParamEditorTitle(String editorTitle) {

        m_paramEditorTitle = editorTitle;
    }
}
