/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editor/Attic/CmsMSDHtmlEditor.java,v $
 * Date   : $Date: 2003/11/21 16:42:08 $
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
package org.opencms.workplace.editor;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.flex.jsp.CmsJspActionElement;
import com.opencms.util.Encoder;
import com.opencms.workplace.I_CmsWpConstants;

import org.opencms.page.CmsXmlPage;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

/**
 * Creates the output for editing a resource.<p> 
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
                    
                    // TODO: get the contents of the default body from the temporary file...
                    CmsXmlPage page = new CmsXmlPage(getCms().readFile(this.getParamTempfile()));
                    setParamContent(new String(page.getElementData("Body", "de")));
 
                    //setParamPagetemplate("/system/modules/com.lgt.intranet.modules.frontend/templates/lgt_intranet_main");
                    setParamPagetemplate(getJsp().property(I_CmsConstants.C_PROPERTY_TEMPLATE, getParamTempfile(), ""));
                    //setParamPagetitle("A test title!");
                    setParamPagetitle(getJsp().property(I_CmsConstants.C_PROPERTY_TITLE, getParamTempfile(), ""));
                
            } 
        } catch (CmsException e) {
            // TODO: show error page!
        }
        prepareContent(false);
        System.err.println("Action param: " + getParamAction());
        
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
                stylesheet = getJsp().link(stylesheet);
                stylesheet = getJsp().getRequest().getScheme() + "://" + getJsp().getRequest().getServerName() + ":" + getJsp().getRequest().getServerPort() + stylesheet;
                content = "<html><head><link href=\"" + stylesheet + "\" rel=\"stylesheet\" type=\"text/css\"></head><body>" +  content;
                content += "</body></html>";
            }  
        }
        if (!save) {
            content = Encoder.escapeWBlanks(content, Encoder.C_UTF8_ENCODING);
        }
        setParamContent(content);
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
     * @see org.opencms.workplace.editor.CmsEditor#actionSave()
     */
    public void actionSave() { 
        // TODO: save modified content
    }

}
