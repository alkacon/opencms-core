/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editor/Attic/CmsMSDHtmlEditor.java,v $
 * Date   : $Date: 2004/02/06 17:10:51 $
 * Version: $Revision: 1.36 $
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

import org.opencms.i18n.CmsEncoder;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringSubstitution;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Creates the output for editing a CmsDefaultPage with the MS DHTML ActiveX control editor.<p> 
 * 
 * The following editor uses this class:
 * <ul>
 * <li>/jsp/editors/msdhtml/editor_html
 * </ul>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.36 $
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
        int indexBodyStart = content.toLowerCase().indexOf("<body>");
        if ("edit".equals(getParamEditormode()) || save) {
            // editor is in text mode or content should be saved
            if (indexBodyStart != -1) {
                // cut tags which are unwanted for text editor
                content = content.substring(indexBodyStart + 6);
                content = content.substring(0, content.toLowerCase().indexOf("</body>"));
            }
            // remove unwanted "&amp;" from links
            content = filterAnchors(content);
        } else {
            // editor is in html mode, add tags for stylesheet
            String currentTemplate = null;
            String stylesheet = "";
            try {
                currentTemplate = getCms().readProperty(getParamResource(), I_CmsConstants.C_PROPERTY_TEMPLATE, true);
            } catch (CmsException e) {
                // ignore this exception
            }
            if (currentTemplate != null) {
                // read the stylesheet from the template property
                stylesheet = getJsp().property(I_CmsConstants.C_PROPERTY_TEMPLATE, currentTemplate, "");
            }
            
            if (indexBodyStart != -1) {
                // first delete the old tags
                content = content.substring(indexBodyStart + 6);
                content = content.substring(0, content.toLowerCase().indexOf("</body>"));
            }
            
            // remove unwanted "&amp;" from links
            content = filterAnchors(content);
            
            // create a head with stylesheet for template and base URL to display images correctly
            String server = getJsp().getRequest().getScheme() + "://" + getJsp().getRequest().getServerName() + ":" + getJsp().getRequest().getServerPort();
            StringBuffer head = new StringBuffer(content.length() + 1024);
            head.append("<html><head>");
            if (!"".equals(stylesheet)) {
                stylesheet = getJsp().link(stylesheet);
                head.append("<link href=\"" + server + stylesheet + "\" rel=\"stylesheet\" type=\"text/css\">");
            }            
            head.append("<base href=\"" + server + OpenCms.getOpenCmsContext() + "\"></base></head><body>");
            content =  head + content + "</body></html>";             
        }
        if (!save) {
            // set the content parameter to the modified content
            setParamContent(content);
        } else {
            // escape special characters for saving
            // TODO: escape only if required because of encoding settings
            content = CmsEncoder.escapeNonAscii(content);
        }
        return content.trim();
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
    
    /**
     * @see org.opencms.workplace.editor.CmsEditor#getEditorResourceUri()
     */
    public final String getEditorResourceUri() {
        return getSkinUri() + "editors/" + EDITOR_TYPE + "/";   
    }
    
    /**
     * Filters the content String and removes unwanted "&amp;" Strings from anchor "href" or "src" attributes.<p>
     * 
     * These unwanted "&amp;" Strings are produced by the MS DHTML editing control.<p>
     * 
     * @param content the content of the editor
     * @return filtered content
     */
    private final String filterAnchors(String content) {
        String anchor = null;
        String newAnchor = null;
        
        // regex pattern to find all src attribs in img tags, plus all href attribs in anchor tags
        // don't forget to update the group index on the matcher after changing the regex below!
        int flags = Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL;        
        Pattern pattern = Pattern.compile("<(img|a)(\\s+)(.*?)(src|href)=(\"|\')(.*?)(\"|\')(.*?)>", flags);

        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            anchor = matcher.group(6);
            newAnchor = CmsStringSubstitution.substitute(anchor, "&amp;", "&");
            if (anchor.length() != newAnchor.length()) {
                // substitute only if anchor length has changed
                content = CmsStringSubstitution.substitute(content, anchor, newAnchor);
            }
        }
        return content;
    }

}
