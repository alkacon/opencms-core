/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/Attic/CmsMSDHtmlEditor.java,v $
 * Date   : $Date: 2004/10/07 10:45:17 $
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
package org.opencms.workplace.editors;

import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsHtmlConverter;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.I_CmsWpConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Creates the output for editing a CmsDefaultPage with the MS DHTML control editor.<p> 
 * 
 * The following editor uses this class:
 * <ul>
 * <li>/editors/msdhtml/editor.jsp
 * </ul>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.2 $
 * 
 * @since 5.1.12
 */
public class CmsMSDHtmlEditor extends CmsSimplePageEditor {
    
    /** regex pattern to find all src attribs in img tags, plus all href attribs in anchor tags. */
    private static final Pattern C_REGEX_LINKS = Pattern.compile("<(img|a)(\\s+)(.*?)(src|href)=(\"|\')(.*?)(\"|\')(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    
    /** Constant for the editor type, must be the same as the editors subfolder name in the VFS. */
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
     * Builds the html String for the editor views available in the editor screens.<p>
     * 
     * @param attributes optional attributes for the &lt;select&gt; tag
     * @return the html for the editorview selectbox
     */
    public final String buildSelectViews(String attributes) {
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
        int browserId = 0;
        int loop = 1;
        int allowedEditors = I_CmsWpConstants.C_SELECTBOX_EDITORVIEWS_ALLOWED[browserId];
        if ("script".equals(getParamElementname())) {
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
     * @see org.opencms.workplace.editors.CmsEditor#getEditorResourceUri()
     */
    public final String getEditorResourceUri() {
        return getSkinUri() + "editors/" + EDITOR_TYPE + "/";   
    }
    
    /**
     * Manipulates the content String for the different editor views and the save operation.<p>
     * 
     * @param save if set to true, the content parameter is not updated
     * @return the prepared content String
     */
    protected String prepareContent(boolean save) {
        String content = getParamContent();
        // extract content of <body>...</body> tag
        content = CmsStringUtil.extractHtmlBody(content);
        // remove unwanted "&amp;" from links
        content = filterAnchors(content);
       
        // ensure all chars in the content are valid for the selected encoding
        content = CmsEncoder.adjustHtmlEncoding(content, getFileEncoding());
        
        int warning = 0;
        //CmsHtmlConverter converter = new CmsHtmlConverter(getFileEncoding(), 2);
        //content = converter.convertToStringSilent(content);
                
        if (! ("edit".equals(getParamEditormode()) || save)) {
            // editor is in html mode, add tags for stylesheet
            String stylesheet = getUriStyleSheet();                      
            
            // create a head with stylesheet for template and base URL to display images correctly
            String server = getJsp().getRequest().getScheme() + "://" + getJsp().getRequest().getServerName() + ":" + getJsp().getRequest().getServerPort();
            StringBuffer result = new StringBuffer(content.length() + 1024);
            result.append("<html><head>");
            if (!"".equals(stylesheet)) {
                stylesheet = getJsp().link(stylesheet);
                result.append("<link href=\"");
                result.append(server);
                result.append(stylesheet);
                result.append("\" rel=\"stylesheet\" type=\"text/css\">");
            }            
            result.append("<base href=\"");
            result.append(server);
            result.append(OpenCms.getSystemInfo().getOpenCmsContext());
            result.append("\"></base></head><body>");
            result.append(content);
            result.append("</body></html>");
            content = result.toString();   
          }
        if (!save) {
            // set the content parameter to the modified content
            setParamContent(content);
        }
        return content.trim();
    }  
    
    /**
     * Filters the content String and removes unwanted "&amp;" Strings from anchor "href" or "src" attributes.<p>
     * 
     * These unwanted "&amp;" Strings are produced by the MS DHTML editing control.<p>
     * 
     * @param content the content of the editor
     * @return filtered content
     */
    private String filterAnchors(String content) {
        String anchor = null;
        String newAnchor = null;
        
        // don't forget to update the group index on the matcher after changing the regex below!      
        Matcher matcher = C_REGEX_LINKS.matcher(content);
        while (matcher.find()) {
            anchor = matcher.group(6);
            newAnchor = CmsStringUtil.substitute(anchor, "&amp;", "&");
            if (anchor.length() != newAnchor.length()) {
                // substitute only if anchor length has changed
                content = CmsStringUtil.substitute(content, anchor, newAnchor);
            }
        }
        return content;
    }

}
