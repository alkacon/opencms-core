/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editor/Attic/CmsSimpleEditor.java,v $
 * Date   : $Date: 2003/11/26 15:13:27 $
 * Version: $Revision: 1.2 $
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
import com.opencms.file.CmsFile;
import com.opencms.flex.jsp.CmsJspActionElement;
import com.opencms.util.Encoder;

import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsWorkplaceAction;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

/**
 * Creates the output for editing a resource.<p> 
 * 
 * This class is extended by the LEdit class, so be careful when changing anything.<p>
 * 
 * The following files use this class:
 * <ul>
 * <li>/jsp/editors/simple/editor_html</li>
 * </ul>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.2 $
 * 
 * @since 5.1.12
 */
public class CmsSimpleEditor extends CmsEditor {
    
    public static final String EDITOR_TYPE = "simple";

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsSimpleEditor(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
        // fill the parameter values in the get/set methods
        fillParamValues(request);
        // set the dialog type
        setParamDialogtype(EDITOR_TYPE);
        
        // set the action for the JSP switch 
        if (EDITOR_SAVE.equals(getParamAction())) {
            setAction(ACTION_SAVE);
        } else if (EDITOR_SAVEEXIT.equals(getParamAction())) {
            setAction(ACTION_SAVEEXIT);         
        } else if (EDITOR_EXIT.equals(getParamAction())) {
            setAction(ACTION_EXIT);
        } else if (EDITOR_SHOW.equals(getParamAction())) {
            setAction(ACTION_SHOW);
        } else {
            // initial call of editor
            setAction(ACTION_DEFAULT);
            initContent();
        }      
        setParamContent(Encoder.escapeWBlanks(getParamContent(), Encoder.C_UTF8_ENCODING));        
    }
    
    /**
     * Initializes the editor content when openening the editor for the first time.<p>
     */
    public void initContent() {
        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
        // get the default encoding
        String encoding = getCms().getRequestContext().getEncoding();
        String content = "";
        
        try {
            // Read file encoding from the property of the temporary file 
            encoding = getCms().readProperty(getParamResource(), I_CmsConstants.C_PROPERTY_CONTENT_ENCODING, true, encoding);
            CmsFile editFile = getCms().readFile(getParamResource());
            try {
                content = new String(editFile.getContents(), encoding);
            } catch (UnsupportedEncodingException e) {
                content = new String(editFile.getContents());
            }
        } catch (CmsException e) {
            // reading of file contents failed, show error dialog
            setParamErrorstack(e.getStackTraceAsString());
            setParamTitle(key("error.title.editorread"));
            setParamMessage(key("error.message.editorread"));
            String reasonSuggestion = key("error.reason.editorread") + "<br>\n" + key("error.suggestion.editorread") + "\n";
            setParamReasonSuggestion(reasonSuggestion);
            // log the error 
            String errorMessage = "Error while reading file " + getParamResource() + ": " + e;
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error(errorMessage, e);
            }
            try {
                // include the common error dialog
                getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
            } catch (JspException exc) {
                // inclusion of error page failed, ignore
            }
        }
        setParamContent(content);
    }
    
    /**
     * Performs the exit editor action.<p>
     * 
     * @see org.opencms.workplace.editor.CmsEditor#actionExit()
     */
    public void actionExit() throws IOException {    
        // redirect to the workplace explorer view
        getJsp().getResponse().sendRedirect(getJsp().link(CmsWorkplaceAction.C_JSP_WORKPLACE_URI));
    }

    /**
     * Performs the save content ation.<p>
     * 
     * @see org.opencms.workplace.editor.CmsEditor#actionSave()
     */
    public void actionSave() throws JspException {
        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
        CmsFile editFile = null;
        try {
            editFile = getCms().readFile(getParamResource());
            String decodedContent = Encoder.unescape(getParamContent(), Encoder.C_UTF8_ENCODING);
            // Read file encoding from the property of the temporary file 
            String encoding = getCms().getRequestContext().getEncoding();
            encoding = getCms().readProperty(getParamResource(), I_CmsConstants.C_PROPERTY_CONTENT_ENCODING, true, encoding);
            try {
                editFile.setContents(decodedContent.getBytes(encoding));
            } catch (UnsupportedEncodingException e) {
                editFile.setContents(decodedContent.getBytes());
            }
            getCms().writeFile(editFile);
        } catch (CmsException e) {
            // error during saving, show error dialog
            setParamErrorstack(e.getStackTraceAsString());
            setParamTitle(key("error.title.editorsave"));
            setParamMessage(key("error.message.editorsave"));
            String reasonSuggestion = key("error.reason.editorsave") + "<br>\n" + key("error.suggestion.editorsave") + "\n";
            setParamReasonSuggestion(reasonSuggestion);
            getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
        }
    }

}
