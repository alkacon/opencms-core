/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/CmsDialog.java,v $
 * Date   : $Date: 2003/07/04 07:25:16 $
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
 *  
*/
package org.opencms.workplace;

import com.opencms.core.CmsException;
import com.opencms.file.CmsResource;
import com.opencms.flex.jsp.CmsJspActionElement;

import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides methods for building the dialog windows of OpenCms.<p> 
 * 
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.3 $
 * 
 * @since 5.1
 */
public class CmsDialog extends CmsWorkplace {

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsDialog(CmsJspActionElement jsp) {
        super(jsp);
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
    }
    
    /**
     * Builds the outer dialog window border.<p>
     * 
     * @param segment the HTML segment (START / END)
     * @return a dialog window start / end segment
     */
    public String dialog(int segment) {
        return dialog(segment, null);
    }
    
    /**
     * Builds the outer dialog window border.<p>
     * 
     * @param segment the HTML segment (START / END)
     * @param attrs optional additional attributes for the opening dialog table
     * @return a dialog window start / end segment
     */
    public String dialog(int segment, String attrs) {
        if (attrs != null) {
            attrs = " " + attrs;
        } else {
            attrs = "";
        }
        if (segment == HTML_START) {
            return "<table class=\"dialog\" cellpadding=\"0\" cellspacing=\"0\""+attrs+">\n"                 + "<tr><td>\n<table class=\"dialogbox\" cellpadding=\"0\" cellspacing=\"0\">\n"                 + "<tr><td>";
        } else {
            return "</td></tr></table>\n</td></tr></table>\n<p>&nbsp;</p>";
        }
    }
    
    /**
     * Builds the content area of the dialog window.<p>
     * 
     * @param segment the HTML segment (START / END)
     * @return a content area start / end segment
     */
    public String dialogContent(int segment) {
        return dialogContent(segment, null);
    }
    
    /**
     * Builds the content area of the dialog window.<p>
     * 
     * @param segment the HTML segment (START / END)
     * @param title the title String for the dialog window
     * @return a content area start / end segment
     */
    public String dialogContent(int segment, String title) {
        if (segment == HTML_START) {
            StringBuffer retValue = new StringBuffer(512);
            if (title != null && !"".equals(title)) {
                retValue.append("<div class=\"dialoghead\" unselectable=\"on\">");
                retValue.append(title);
                retValue.append("</div>");
            }
            retValue.append("<div class=\"dialogcontent\" unselectable=\"on\">\n");
            retValue.append("<!-- dialogcontent start -->\n");
            return retValue.toString();
        } else {
            return "<!-- dialogcontent end -->\n</div>";
        }
    }
    
    /**
     * Builds a block with 3D border and optional subheadline in the dialog content area.<p>
     * 
     * @param segment the HTML segment (START / END)
     * @return 3D block start / end segment
     */
    public String dialogBlock(int segment) {
        return dialogBlock(segment, null, false);
    }
    
    /**
     * Builds a block with 3D border and optional subheadline in the dialog content area.<p>
     * 
     * @param segment the HTML segment (START / END)
     * @param headline the headline String for the block
     * @return 3D block start / end segment
     */
    public String dialogBlock(int segment, String headline) {
        return dialogBlock(segment, headline, false);
    }
    
    /**
     * Builds a block with 3D border and optional subheadline in the dialog content area.<p>
     * 
     * @param segment the HTML segment (START / END)
     * @param headline the headline String for the block
     * @param error if true, an error block will be created
     * @return 3D block start / end segment
     */
    public String dialogBlock(int segment, String headline, boolean error) {
        if (segment == HTML_START) {
            StringBuffer retValue = new StringBuffer(512);
            String errorStyles = "";
            if (error) {
                errorStyles = " dialogerror textbold";
            }
            retValue.append("<!-- 3D block start -->\n");
            retValue.append("<div class=\"dialogblockborder\" unselectable=\"on\">\n");
            retValue.append("<div class=\"dialogblock"+errorStyles+"\" unselectable=\"on\">\n");
            if (headline != null && !"".equals(headline)) {
                retValue.append("<span class=\"dialogblockhead"+errorStyles+"\" unselectable=\"on\">");
                retValue.append(headline);
                retValue.append("</span>\n");
            }
            return retValue.toString();
        } else {
            return "</div>\n</div>\n<!-- 3D block end -->";
        }
    }
    
    /**
     * Builds a white box in the dialog content area.<p>
     * 
     * @param segment the HTML segment (START / END)
     * @return the white box start / end segment
     */
    public String dialogWhiteBox(int segment) {
        if (segment == HTML_START) {
            return "<!-- white box start -->\n"
                + "<div class=\"dialoginnerboxborder\" unselectable=\"on\">\n"
                + "<div class=\"dialoginnerbox\" unselectable=\"on\">\n";
        } else {
            return "</div>\n</div>\n<!-- white box end -->\n";
        }        
    }
    
    /**
     * Builds the button row under the dialog content area (without the buttons!).<p>
     * 
     * @param segment the HTML segment (START / END)
     * @return the button row start / end segment
     */
    public String dialogButtonRow(int segment) {
        if (segment == HTML_START) {
            return "<!-- button row start -->\n<div class=\"dialogbuttons\" unselectable=\"on\">\n";
        } else {
            return "</div>\n<!-- button row end -->\n";
        }
    }
    
    /**
     * Builds a subheadline in the dialog content area.<p>
     * 
     * @param headline the desired headline string
     * @return a subheadline element
     */
    public String dialogSubheadline(String headline) {
        StringBuffer retValue = new StringBuffer(128);
        retValue.append("<div class=\"dialogsubheader\" unselectable=\"on\">");
        retValue.append(headline);
        retValue.append("</div>\n");
        return retValue.toString();
    }
    
    /**
     * Builds a horizontal separator line in the dialog content area.<p>
     * 
     * @return a separator element
     */
    public String dialogSeparator() {
        return "<div class=\"dialogseparator\" unselectable=\"on\"></div>";
    }
    
    /**
     * Builds a dialog line without break (display: block).<p>
     * 
     * @param segment the HTML segment (START / END)
     * @return a row start / end segment
     */
    public String dialogRow(int segment) {
        if (segment == HTML_START) {
            return "<div class=\"dialogrow\">";
        } else {
            return "</div>\n";
        }
    }
    
    /**
     * Gets a formatted file state string.<p>
     * 
     * @param file the CmsResource
     * @return formatted state string
     */
    public String getState(CmsResource file) {  
        if(file.inProject(getCms().getRequestContext().currentProject())) {
            int state = file.getState();
            return key("explorer.state" + state);
        } else {
            return key("explorer.statenip");
        }
    }
    
    /**
     * Gets a formatted file state string.<p>
     * 
     * @return formatted state string
     * @throws CmsException if something goes wrong
     */
    public String getState() throws CmsException {  
        CmsResource file = getCms().readFileHeader(getSettings().getFileUri());
        return getState(file);
    }

}
