/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/CmsChnav.java,v $
 * Date   : $Date: 2004/02/04 17:18:07 $
 * Version: $Revision: 1.13 $
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
package org.opencms.workplace;

import org.opencms.locale.CmsEncoder;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;
import com.opencms.flex.jsp.CmsJspActionElement;
import com.opencms.flex.jsp.CmsJspNavBuilder;
import com.opencms.flex.jsp.CmsJspNavElement;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Provides methods for the change navigation dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/jsp/dialogs/chnav_html
 * </ul>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.13 $
 * 
 * @since 5.1
 */
public class CmsChnav extends CmsDialog {
    
    /** The dialog type */
    public static final String DIALOG_TYPE = "chnav";
    
    /** The debug flag */
    public static final int C_DEBUG = 1;
    
    /** Value for the action: change the navigation */
    public static final int ACTION_CHNAV = 100;
    
    /** Request parameter name for the navigation text */
    public static final String PARAM_NAVTEXT = "navtext";   
    /** Request parameter name for the navigation position */
    public static final String PARAM_NAVPOS = "navpos"; 

    private String m_paramNavtext;
    private String m_paramNavpos;    
    
    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsChnav(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsChnav(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }
    
    /**
     * Returns the value of the navigation text parameter, 
     * or null if this parameter was not provided.<p>
     * 
     * The navigation text parameter defines the new value for 
     * the NavText property.<p>
     * 
     * @return the value of the target parameter
     */    
    public String getParamNavtext() {
        return m_paramNavtext;
    }
    
    /**
     * Sets the value of the navigation text parameter.<p>
     * 
     * @param value the value to set
     */
    public void setParamNavtext(String value) {
        m_paramNavtext = value;
    }
    
    /**
     * Returns the value of the navigation position parameter, 
     * or null if this parameter was not provided.<p>
     * 
     * The navigation position parameter defines the new value for 
     * the NavPos property.<p>
     * 
     * @return the value of the target parameter
     */    
    public String getParamNavpos() {
        return m_paramNavpos;
    }

    /**
     * Sets the value of the navigation position parameter.<p>
     * 
     * @param value the value to set
     */
    public void setParamNavpos(String value) {
        m_paramNavpos = value;
    }
    
    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
        // fill the parameter values in the get/set methods
        fillParamValues(request);
        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        // set the action for the JSP switch 
        if (DIALOG_TYPE.equals(getParamAction())) {
            setAction(ACTION_CHNAV);                            
        } else {                        
            setAction(ACTION_DEFAULT);
            // build title for chnav dialog     
            setParamTitle(key("explorer.context.chnav") + ": " + CmsResource.getName(getParamResource()));
        }
    }
    
    /**
     * Returns the NavText property value of the current resource.<p>
     * 
     * @return the NavText property value of the current resource
     */
    public String getCurrentNavText() {
        try {
            String navText = getCms().readProperty(getParamResource(), I_CmsConstants.C_PROPERTY_NAVTEXT);
            if (navText == null) {
                navText = "";
            }
            return navText;
        } catch (CmsException e) {
            return "";
        }
        
    }
        
    /**
     * Performs the navigation change.<p>
     * 
     * @throws JspException if including a JSP subelement is not successful
     */
    public void actionChangeNav() throws JspException {
        
        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
        
        // get request parameters
        String filename = getParamResource();
        String newText = getParamNavtext();
        String selectedPosString = getParamNavpos();
        
        try { 
            // lock resource if autolock is enabled
            checkLock(getParamResource());
            // save the new NavText if not null
            if (newText != null) {
                getCms().writeProperty(filename, I_CmsConstants.C_PROPERTY_NAVTEXT, newText);
            }
            
            // determine the selected position
            float selectedPos = -1;
            try {
                selectedPos = Float.parseFloat(selectedPosString);
            } catch (Exception e) {
                // ignore
            }
            
            // only update the position if a change is requested
            if (selectedPos != -1) {
                getCms().writeProperty(filename, I_CmsConstants.C_PROPERTY_NAVPOS, selectedPosString);            
            }
        } catch (CmsException e) {
            // error during chnav, show error dialog
            setParamErrorstack(e.getStackTraceAsString());
            setParamMessage(key("error.message." + getParamDialogtype()));
            setParamReasonSuggestion(getErrorSuggestionDefault());
            getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
        
        }
        // chnav operation was successful, return to explorer filelist
        getJsp().include(C_FILE_EXPLORER_FILELIST);
    }
    
    /**
     * Builds the HTML for the select box of the navigation position.<p>
     * 
     * @param cms the CmsObject
     * @param filename the current file
     * @param attributes optional attributes for the &lt;select&gt; tag, do not add the "name" atribute!
     * @param messages the localized workplace messages
     * @return the HTML for a navigation position select box
     */
    public static String buildNavPosSelector(CmsObject cms, String filename, String attributes, CmsWorkplaceMessages messages) {
        ArrayList navList = new ArrayList();
        ArrayList options = new ArrayList();
        ArrayList values = new ArrayList();
        
        // get current file navigation element
        CmsJspNavElement curNav = CmsJspNavBuilder.getNavigationForResource(cms, filename);
        
        // get the parent folder of the current file
        filename = CmsResource.getParentFolder(filename);
        
        // get navigation of the current folder
        navList = CmsJspNavBuilder.getNavigationForFolder(cms, filename);
        float maxValue = 0;
        float nextPos = 0;
        
        // calculate value for the first navigation position
        float firstValue = 1;
        try {
            CmsJspNavElement ne = (CmsJspNavElement)navList.get(0);
            maxValue = ne.getNavPosition();
        } catch (Exception e) {
            // ignore
        }
        
        if (maxValue != 0) {
            firstValue = maxValue / 2;
        }
        
        // add the first entry: before first element
        options.add(messages.key("input.firstelement"));
        values.add(firstValue+"");      
        
        // show all present navigation elements in box
        for (int i=0; i<navList.size(); i++) {
            CmsJspNavElement ne = (CmsJspNavElement)navList.get(i);
            String navText = ne.getNavText();
            float navPos = ne.getNavPosition();
            // get position of next nav element
            nextPos = navPos + 2;
            if ((i+1) < navList.size()) {
                nextPos = ((CmsJspNavElement)navList.get(i+1)).getNavPosition();
            }
            // calculate new position of current nav element
            float newPos = (navPos + nextPos) / 2;
            
            // check new maxValue of positions and increase it
            if (navPos > maxValue) {
                maxValue = navPos;
            }
            
            // if the element is the current file, mark it in selectbox
            if (curNav.getNavText().equals(navText) && curNav.getNavPosition() == navPos) {
                options.add(CmsEncoder.escapeHtml(messages.key("input.currentposition")+" ["+ne.getFileName()+"]"));
                values.add("-1");
            } else {
                options.add(CmsEncoder.escapeHtml(navText+" ["+ne.getFileName()+"]"));
                values.add(newPos+"");
            }
        }
        
        // add the entry: at the last position
        options.add(messages.key("input.lastelement"));
        values.add((maxValue+1)+"");
        
        // add the entry: no change
        options.add(messages.key("input.nochange"));
        values.add("-1");
        
        if (attributes != null && !"".equals(attributes.trim())) {
            attributes = " " + attributes;
        } else {
            attributes = "";
        }
        CmsDialog wp = new CmsDialog(null);
        return wp.buildSelect("name=\""+PARAM_NAVPOS+"\"" + attributes, options, values, values.size()-1, true);  
    }
    
    /**
     * Builds the HTML for the select box of the navigation position.<p>
     * 
     * @return the HTML for a navigation position select box
     */
    public String buildNavPosSelector() {
        synchronized (this) {
            return buildNavPosSelector(getCms(), getParamResource(), null, getSettings().getMessages());
        }
    }

}
