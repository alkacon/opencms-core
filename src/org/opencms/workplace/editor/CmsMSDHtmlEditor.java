/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editor/Attic/CmsMSDHtmlEditor.java,v $
 * Date   : $Date: 2003/11/26 16:07:17 $
 * Version: $Revision: 1.7 $
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
import com.opencms.core.I_CmsSession;
import com.opencms.flex.jsp.CmsJspActionElement;
import com.opencms.util.Encoder;
import com.opencms.workplace.I_CmsWpConstants;

import org.opencms.main.OpenCms;
import org.opencms.page.CmsXmlPage;
import org.opencms.workplace.CmsWorkplaceAction;
import org.opencms.page.CmsDefaultPage;
import org.opencms.page.CmsXmlPage;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

/**
 * Creates the output for editing a resource.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/jsp/editors/editor_html
 * </ul>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.7 $
 * 
 * @since 5.1.12
 */
public class CmsMSDHtmlEditor extends CmsEditor {
    
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
        try {
            // set the action for the JSP switch 
            if (EDITOR_SAVE.equals(getParamAction())) {
                setAction(ACTION_SAVE);
            } else if (EDITOR_SAVEEXIT.equals(getParamAction())) {
                setAction(ACTION_SAVEEXIT);         
            } else if (EDITOR_EXIT.equals(getParamAction())) {
                setAction(ACTION_EXIT);
            } else if (EDITOR_CHANGE_BODY.equals(getParamAction())) {
                setAction(ACTION_CHANGE_BODY);
            } else if (EDITOR_CHANGE_TEMPLATE.equals(getParamAction())) {
                actionChangeTemplate();
                setAction(ACTION_SHOW);
            } else if (EDITOR_SHOW.equals(getParamAction())) {
                setAction(ACTION_SHOW);
            } else if (EDITOR_PREVIEW.equals(getParamAction())) {
                setAction(ACTION_PREVIEW);
            } else {
                // initial call of editor
                setAction(ACTION_DEFAULT);
                
                    setParamTempfile(createTempFile());

                    CmsDefaultPage page = (CmsDefaultPage)CmsXmlPage.newInstance(getCms(), getCms().readFile(this.getParamTempfile()));
                    setParamContent(new String(page.getElementData("body", "en")));
 
                    //setParamPagetemplate("/system/modules/com.lgt.intranet.modules.frontend/templates/lgt_intranet_main");
                    setParamPagetemplate(getJsp().property(I_CmsConstants.C_PROPERTY_TEMPLATE, getParamTempfile(), ""));                    
                    setParamPagetitle(getJsp().property(I_CmsConstants.C_PROPERTY_TITLE, getParamTempfile(), ""));
                    
                    
                
            } 
        } catch (CmsException e) {
            // TODO: show error page!
        }
        prepareContent(false);              
    }
    
    /**
     * Manipulates the content String for the different editor views and the save operation.<p>
     * 
     * @param save if set to true, the result String is not escaped
     */
    public void prepareContent(boolean save) {
        String content = getParamContent();
        boolean isBrowserNS = BROWSER_NS.equals(getBrowserType());
        if ("edit".equals(getParamEditormode()) || isBrowserNS || save) {
            // editor is in text mode or content should be saved
            if (content.indexOf("<body>") != -1) {
                // cut tags which are unwanted for text editor
                content = content.substring(content.indexOf("<body>") + 6);
                content = content.substring(0, content.indexOf("</body>"));
            }           
        } else {
            // editor is in html mode, add tags for stylesheet
            String stylesheet = getJsp().property(I_CmsConstants.C_PROPERTY_TEMPLATE, getParamPagetemplate(), "");
            if (content.indexOf("<body>") != -1) {
                // first delete the old tags
                content = content.substring(content.indexOf("<body>") + 6);
                content = content.substring(0, content.indexOf("</body>"));
            }      
            if (!"".equals(stylesheet)) {
                // create a head with stylesheet for template and base URL to display images
                String server = getJsp().getRequest().getScheme() + "://" + getJsp().getRequest().getServerName() + ":" + getJsp().getRequest().getServerPort();
                stylesheet = getJsp().link(stylesheet);
                String head = "<html><head><link href=\"" + server + stylesheet + "\" rel=\"stylesheet\" type=\"text/css\">";
                head += "<base href=\"" + server + OpenCms.getOpenCmsContext() + "\"></base></head><body>";
                content =  head + content;
                content += "</body></html>";
            }  
        }
        if (!save) {
            // escape the content String if it is not saved
            content = Encoder.escapeWBlanks(content, Encoder.C_UTF8_ENCODING); 
        }
        setParamContent(content);
    }
    
    public String buildSelectBody() throws CmsException {
        Vector names = new Vector();
        Vector values = new Vector();
        I_CmsSession session = getCms().getRequestContext().getSession(true);
        String currentBodySection = getParamBodyelement();
//            String bodyClassName = (String)parameters.get("bodyclass");
//            String tempBodyFilename = (String)session.getValue("te_tempbodyfile");
//            Object tempObj = CmsTemplateClassManager.getClassInstance(bodyClassName);
//            CmsXmlTemplate bodyElementClassObject = (CmsXmlTemplate)tempObj;
//            CmsXmlTemplateFile bodyTemplateFile = bodyElementClassObject.getOwnTemplateFile(cms,
//                    tempBodyFilename, C_BODY_ELEMENT, parameters, null);
//            Vector allBodys = bodyTemplateFile.getAllSections();
//            int loop = 0;
//            int currentBodySectionIndex = 0;
//            int numBodys = allBodys.size();
//            for(int i = 0;i < numBodys;i++) {
//                String bodyname = (String)allBodys.elementAt(i);
//                String encodedBodyname = Encoder.escapeXml(bodyname);
//                if(bodyname.equals(currentBodySection)) {
//                    currentBodySectionIndex = loop;
//                }
//                values.addElement(encodedBodyname);
//                names.addElement(encodedBodyname);
//                loop++;
//            }
        return "";
    }
    
    
    /**
     * Builds the html String for the editor views available in the template editor screens.<p>
     * 
     * @param attributes optional attributes for the &lt;select&gt; tag
     * @return the html for the editorview selectbox
     * @throws CmsException if something goes wrong
     */
    public String buildSelectViews(String attributes) throws CmsException {
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
        if ("script".equals(getParamBodyelement())) {
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
    
    /**
     * Performs the change body element action.<p>
     */
    public void actionChangeBodyElement() {

    }
    
    /**
     * Performs the exit editor action and deletes the temporary file.<p>
     * 
     * @see org.opencms.workplace.editor.CmsEditor#actionExit()
     */
    public void actionExit() throws CmsException, IOException {
        // switch to the temporary file project
        switchToTempProject();
        try {
            // delete the temporary file
            getCms().deleteResource(getParamTempfile(), I_CmsConstants.C_DELETE_OPTION_IGNORE_VFS_LINKS);
        } catch (CmsException e) {
            // ignore this exception
        }
    
        // switch back to the current project
        switchToCurrentProject();
    
        // now redirect to the workplace explorer view
        getJsp().getResponse().sendRedirect(getJsp().link(CmsWorkplaceAction.C_JSP_WORKPLACE_URI));   
    }

    /**
     * @see org.opencms.workplace.editor.CmsEditor#actionSave()
     */
    public void actionSave() { 
        // TODO: save modified content
        try {
            CmsDefaultPage page = (CmsDefaultPage)CmsXmlPage.newInstance(getCms(), getCms().readFile(this.getParamTempfile()));
            page.setElementData("body", "de", getParamContent().getBytes());
            getCms().writeFile(page.marshal());
            commitTempFile();
        } catch (CmsException e) {
            
        }       
    }
    
    /**
     * @see org.opencms.workplace.editor.CmsEditor#initContent()
     */
    public void initContent() {
        // TODO: initialize content of editor properly
        try {
            CmsDefaultPage page = (CmsDefaultPage)CmsXmlPage.newInstance(getCms(), getCms().readFile(this.getParamTempfile()));
            setParamContent(new String(page.getElementData("body", "en")));

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
        //setParamBodyelement("");
        setParamPagetemplate(getJsp().property(I_CmsConstants.C_PROPERTY_TEMPLATE, getParamTempfile(), ""));                    
        setParamPagetitle(getJsp().property(I_CmsConstants.C_PROPERTY_TITLE, getParamTempfile(), ""));
    }

}
