/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editor/Attic/CmsMSDHtmlEditor.java,v $
 * Date   : $Date: 2003/12/10 14:22:56 $
 * Version: $Revision: 1.18 $
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
import com.opencms.flex.jsp.CmsJspActionElement;
import com.opencms.workplace.I_CmsWpConstants;

import org.opencms.main.OpenCms;
import org.opencms.page.CmsDefaultPage;
import org.opencms.page.CmsXmlPage;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

/**
 * Creates the output for editing a CmsDefaultPage with the MS DHTML ActiveX control editor.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/jsp/editors/msdhtml/editor_html
 * </ul>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.18 $
 * 
 * @since 5.1.12
 */
public class CmsMSDHtmlEditor extends CmsDefaultPageEditor {
    
    public static final String EDITOR_TYPE = "msdhtml";

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsMSDHtmlEditor(CmsJspActionElement jsp) {
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
        
        // Initialize a page object from the temporary file
        if (getParamTempfile() != null && !"null".equals(getParamTempfile())) {
            try {
                m_page = (CmsDefaultPage)CmsXmlPage.newInstance(getCms(), getCms().readFile(this.getParamTempfile()));
            } catch (CmsException e) {
                // error during initialization
                try {
                    showErrorPage(this, e, "read");
                } catch (JspException exc) {
                    // ignore this exception
                }
            }
        }

        // set the action for the JSP switch 
        if (EDITOR_SAVE.equals(getParamAction())) {
            setAction(ACTION_SAVE);
        } else if (EDITOR_SAVEEXIT.equals(getParamAction())) {
            setAction(ACTION_SAVEEXIT);         
        } else if (EDITOR_EXIT.equals(getParamAction())) {
            setAction(ACTION_EXIT);
        } else if (EDITOR_CHANGE_BODY.equals(getParamAction())) {
            setAction(ACTION_SHOW);
            actionChangeBodyElement();
        } else if (EDITOR_CHANGE_TEMPLATE.equals(getParamAction())) {
            setAction(ACTION_SHOW);
            actionChangeTemplate();
        } else if (EDITOR_NEW_BODY.equals(getParamAction())) {
            setAction(ACTION_SHOW);            
            actionNewBody();
        } else if (EDITOR_SHOW.equals(getParamAction())) {
            setAction(ACTION_SHOW);
        } else if (EDITOR_PREVIEW.equals(getParamAction())) {
            setAction(ACTION_PREVIEW);
        } else {
            // initial call of editor, initialize page and page parameters
            setAction(ACTION_DEFAULT);
            try {
                // lock resource if autolock is enabled in configuration
                checkLock(getParamResource());
                // create the temporary file
                setParamTempfile(createTempFile());
                // initialize a page object from the created temporary file
                m_page = (CmsDefaultPage)CmsXmlPage.newInstance(getCms(), getCms().readFile(this.getParamTempfile()));
            } catch (CmsException e) {
                // error during initialization
                try {
                    showErrorPage(this, e, "read");
                } catch (JspException exc) {
                    // ignore this exception
                }
            }
            // set the initial body language & name if not given in request parameters
            if (getParamBodylanguage() == null) {
                initBodyElementLanguage();
            }
            if (getParamBodyname() == null) {
                initBodyElementName();
            }
            // initialize the editor content
            initContent();
            // set template and page title  
            setParamPagetemplate(getJsp().property(I_CmsConstants.C_PROPERTY_TEMPLATE, getParamTempfile(), ""));                    
            setParamPagetitle(getJsp().property(I_CmsConstants.C_PROPERTY_TITLE, getParamTempfile(), ""));
        } 
        
        // prepare the content String for the editor
        prepareContent(false);
    }
    
    
    
    /**
     * Manipulates the content String for the different editor views and the save operation.<p>
     * 
     * @param save if set to true, the result String is not escaped and the content parameter is not updated
     * @return the prepared content String
     */
    protected String prepareContent(boolean save) {
        String content = getParamContent();
        String contentLowerCase = content.toLowerCase();
        int indexBodyStart = contentLowerCase.indexOf("<body>");
        boolean isBrowserNS = BROWSER_NS.equals(getBrowserType());
        if ("edit".equals(getParamEditormode()) || isBrowserNS || save) {
            // editor is in text mode or content should be saved
            if (indexBodyStart != -1) {
                // cut tags which are unwanted for text editor
                content = content.substring(indexBodyStart + 6);
                contentLowerCase = contentLowerCase.substring(indexBodyStart + 6);
                content = content.substring(0, contentLowerCase.indexOf("</body>"));
            }           
        } else {
            // editor is in html mode, add tags for stylesheet
            String stylesheet = getJsp().property(I_CmsConstants.C_PROPERTY_TEMPLATE, getParamPagetemplate(), "");
            if (indexBodyStart != -1) {
                // first delete the old tags
                content = content.substring(indexBodyStart + 6);
                contentLowerCase = contentLowerCase.substring(indexBodyStart + 6);
                content = content.substring(0, contentLowerCase.indexOf("</body>"));
            }      
            if (!"".equals(stylesheet)) {
                // create a head with stylesheet for template and base URL to display images correctly
                String server = getJsp().getRequest().getScheme() + "://" + getJsp().getRequest().getServerName() + ":" + getJsp().getRequest().getServerPort();
                stylesheet = getJsp().link(stylesheet);
                String head = "<html><head><link href=\"" + server + stylesheet + "\" rel=\"stylesheet\" type=\"text/css\">";
                head += "<base href=\"" + server + OpenCms.getOpenCmsContext() + "\"></base></head><body>";
                content =  head + content;
                content += "</body></html>";
            }  
        }
        if (!save) {
            // set the content parameter to the escaped content
            setParamContent(content);
        }
        return content;
    }  
    
    /**
     * @see org.opencms.workplace.editor.CmsEditor#getEditorResourceUri()
     */
    public final String getEditorResourceUri() {
        return getSkinUri() + "editors/" + EDITOR_TYPE + "/";   
    }
    
    /**
     * Builds the html String for the editor views available in the editor screens.<p>
     * 
     * @param attributes optional attributes for the &lt;select&gt; tag
     * @return the html for the editorview selectbox
     */
    public String buildSelectViews(String attributes) {
        Vector names = new Vector();
        Vector values = new Vector();
        // get the available views fron the constant
        String[] contents = I_CmsWpConstants.C_SELECTBOX_EDITORVIEWS;
        for (int i = 0; i < contents.length; i++) {
            String value = contents[i];
            values.addElement(value);
            String s = key("select." + value);
            if ((s == null) || s.startsWith("???")) {
                s = value;
            }
            names.addElement(s);
        }
        int browserId;
        String browser = getBrowserType();
        if (BROWSER_IE.equals(browser)) {
            browserId = 0;
        } else {
            browserId = 1;
        }
        int loop = 1;
        int allowedEditors = I_CmsWpConstants.C_SELECTBOX_EDITORVIEWS_ALLOWED[browserId];
        if ("script".equals(getParamBodyname())) {
            allowedEditors = allowedEditors & 510;
        }
        List namesFinal = new ArrayList(names.size());
        List valuesFinal = new ArrayList(values.size());
        for (int i = 0; i < names.size(); i++) {
            if ((allowedEditors & loop) > 0) {
                valuesFinal.add(values.elementAt(i));
                namesFinal.add(names.elementAt(i));
            }
            loop <<= 1;
        }
        int currentIndex = valuesFinal.indexOf(getParamEditormode());
        return buildSelect(attributes, namesFinal, valuesFinal, currentIndex, false);
    }

}
