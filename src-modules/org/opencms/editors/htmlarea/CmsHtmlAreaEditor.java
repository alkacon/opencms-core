/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/editors/htmlarea/Attic/CmsHtmlAreaEditor.java,v $
 * Date   : $Date: 2005/09/29 12:48:27 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 
package org.opencms.editors.htmlarea;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;
import org.opencms.workplace.editors.CmsEditorDisplayOptions;
import org.opencms.workplace.editors.CmsSimplePageEditor;
import org.opencms.workplace.galleries.A_CmsGallery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Creates the output for editing a page with the open source HtmlArea editor.<p> 
 * 
 * The following editor uses this class:
 * <ul>
 * <li>/editors/htmlarea/editor.jsp
 * </ul>
 * <p>
 *
 * @author  Andreas Zahner 
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.0.1 
 */
public class CmsHtmlAreaEditor extends CmsSimplePageEditor {

    /** Constant for the editor type, must be the same as the editors subfolder name in the VFS. */
    private static final String EDITOR_TYPE = "htmlarea";

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsHtmlAreaEditor(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * @see org.opencms.workplace.editors.CmsDefaultPageEditor#buildGalleryButtons(CmsEditorDisplayOptions, int, Properties)
     */
    public String buildGalleryButtons(CmsEditorDisplayOptions options, int buttonStyle, Properties displayOptions) {

        StringBuffer result = new StringBuffer();       
        Iterator i = OpenCms.getWorkplaceManager().getGalleries().keySet().iterator();
        
        while (i.hasNext()) {
            String galleryType = (String)i.next();
            String galleryName = galleryType.replaceFirst("gallery", "");
            if (options.showElement("gallery." + galleryName, displayOptions)) {
                // gallery is shown, create button code
                result.append("config.registerButton(\"");
                result.append(galleryType);
                result.append("\", \"");
                result.append(key("button." + galleryName + "list"));
                result.append("\", __editor.imgURL(\"../../editors/htmlarea/images/opencms/");
                result.append(galleryType);
                result.append(".gif\"), false, function(e) { openGallery(\'");
                result.append(galleryType);
                result.append("\'); });\n");
            }
        }
   
        return result.toString();
    }
    
    /**
     * Returns the configuration String for the gallery button row in HtmlArea.<p>
     * 
     * @param options the display configuration for the editor
     * @param displayOptions the display options for the editor
     * @return the html String for the gallery buttons
     */
    public String buildGalleryButtonRow(CmsEditorDisplayOptions options, Properties displayOptions) {

        StringBuffer result = new StringBuffer();
        Map galleryMap = OpenCms.getWorkplaceManager().getGalleries();
        List galleries = new ArrayList(galleryMap.size());
        Map typeMap = new HashMap(galleryMap.size());
        
        Iterator i = galleryMap.keySet().iterator();
        while (i.hasNext()) {
            String key = (String)i.next();
            A_CmsGallery currGallery = (A_CmsGallery)galleryMap.get(key);
            galleries.add(currGallery);
            // put the type name to the type Map
            typeMap.put(currGallery, key);
        }
        
        // sort the found galleries by their order
        Collections.sort(galleries);
        
        for (int k=0; k<galleries.size(); k++) {
            A_CmsGallery currGallery = (A_CmsGallery)galleries.get(k);
            String galleryType = (String)typeMap.get(currGallery);
            if (options.showElement("gallery." + galleryType.replaceFirst("gallery", ""), displayOptions)) {
                // gallery is shown, build row configuration String
                if (result.length() == 0) {
                    result.append(", \"separator\"");
                }
                result.append(", \"" + galleryType + "\"");
            }
        }
        return result.toString();
    }
    
    /**
     * @see org.opencms.workplace.editors.CmsEditor#getEditorResourceUri()
     */
    public String getEditorResourceUri() {

        return getSkinUri() + "editors/" + EDITOR_TYPE + "/";
    }

}
