/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editor/Attic/CmsMSDHtmlEditor.java,v $
 * Date   : $Date: 2004/01/09 08:30:37 $
 * Version: $Revision: 1.27 $
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

import com.opencms.core.I_CmsConstants;
import com.opencms.flex.jsp.CmsJspActionElement;
import com.opencms.workplace.I_CmsWpConstants;

import org.opencms.main.OpenCms;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Creates the output for editing a CmsDefaultPage with the MS DHTML ActiveX control editor.<p> 
 * 
 * The following editor uses this class:
 * <ul>
 * <li>/jsp/editors/msdhtml/editor_html
 * </ul>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.27 $
 * 
 * @since 5.1.12
 */
public class CmsMSDHtmlEditor extends CmsSimplePageEditor {
    
    /** Constant for the editor type, must be the same as the editors subfolder name in the VFS */
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
     * Manipulates the content String for the different editor views and the save operation.<p>
     * 
     * @param save if set to true, the content parameter is not updated
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
            
            // create a head with stylesheet for template and base URL to display images correctly
            String server = getJsp().getRequest().getScheme() + "://" + getJsp().getRequest().getServerName() + ":" + getJsp().getRequest().getServerPort();
            String head = "<html><head>";
            if (!"".equals(stylesheet)) {
                stylesheet = getJsp().link(stylesheet);
                head += "<link href=\"" + server + stylesheet + "\" rel=\"stylesheet\" type=\"text/css\">";
            }            
            head += "<base href=\"" + server + OpenCms.getOpenCmsContext() + "\"></base></head><body>";
            content =  head + content + "</body></html>";             
        }
        if (!save) {
            // set the content parameter to the modified content
            setParamContent(content);
        }
        return content.trim();
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
