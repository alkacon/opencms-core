/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/CmsPropertyCustom.java,v $
 * Date   : $Date: 2004/04/01 10:19:08 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;

import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Provides methods for the customized property dialog.<p> 
 * 
 * This is a special dialog that is used for the different resource types in the workplace.<p>
 * 
 * The following files use this class:
 * <ul>
 * <li>/jsp/dialogs/property_custom.html
 * </ul>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.4 $
 * 
 * @since 5.3.3
 */
public class CmsPropertyCustom extends CmsPropertyAdvanced {
    
    /** Value for the action: edit the properties */
    public static final int ACTION_EDIT = 500;
    
    private boolean m_showNavigation;
    private CmsExplorerTypeSettings m_explorerTypeSettings;
    
    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsPropertyCustom(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsPropertyCustom(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }
    
    /**
     * Performs the edit properties action, will be called by the JSP page.<p>
     * 
     * @param request the HttpServletRequest
     * @throws JspException if problems including sub-elements occur
     */
    public void actionEdit(HttpServletRequest request) throws JspException {
        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
        try {
            // save the changes only if resource is properly locked
            if (isEditable()) {
                performEditOperation(request);    
            }    
        } catch (CmsException e) {
            // Cms error defining property, show error dialog
            setParamErrorstack(e.getStackTraceAsString());
            setParamReasonSuggestion(getErrorSuggestionDefault());
            getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
        } catch (Exception e) {
            // other error defining property, show error dialog
            setParamErrorstack(e.getStackTrace().toString());
            setParamReasonSuggestion(getErrorSuggestionDefault());
            getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
        } 
    }
    
    /**
     * Creates the HTML String for the edit properties form.<p>
     * 
     * @return the HTML output String for the edit properties form
     */
    public String buildEditForm() {
        StringBuffer result = new StringBuffer(2048);
        
        // check if the properties are editable
        boolean editable =  isEditable();
                
        // get all used properties for the resource
        Map activeProperties = null;
        try {
            activeProperties = getCms().readProperties(getParamResource());
        } catch (CmsException e) { 
            // ignore this exception
        }
        
        // create the column heads
        result.append("<table border=\"0\">\n");
        result.append("<tr>\n");
        result.append("\t<td class=\"textbold\">" + key("input.property") + "</td>\n");
        result.append("\t<td class=\"textbold\">" + key("label.value") + "</td>\n");   
        result.append("\t<td class=\"textbold\" style=\"white-space: nowrap;\">" + key("input.usedproperty") + "</td>\n");    
        result.append("</tr>\n");
        result.append("<tr><td><span style=\"height: 6px;\"></span></td></tr>\n");
        
        // create the text property input rows from explorer type settings
        result.append(buildTextInput(editable, activeProperties));
        
        // show navigation properties if enabled in explorer type settings
        if (showNavigation()) {
            result.append(buildNavigationProperties(editable, activeProperties));
        }
        result.append("</table>");       
       
        return result.toString();
    }
       
    /**
     * Builds the HTML code for the special properties of an xmlpage resource.<p>
     * 
     * @param editable indicates if the properties are editable
     * @param activeProperties Map of all active properties of the resource 
     * @return the HTML code for the special properties of a file resource
     */
    protected StringBuffer buildNavigationProperties(boolean editable, Map activeProperties) {
        StringBuffer result = new StringBuffer(1024);
        
        // create "disabled" attribute if properties are not editable
        String disabled = "";
        if (!editable) {
            disabled = " disabled=\"disabled\"";
        }    
        
        // create "add to navigation" checkbox
        result.append(buildTableRowStart(key("input.addtonav")));
        result.append("<input type=\"checkbox\" name=\"enablenav\" id=\"enablenav\" value=\"true\" onClick=\"toggleNav();\"");
        if (activeProperties.containsKey(I_CmsConstants.C_PROPERTY_NAVTEXT) && activeProperties.containsKey(I_CmsConstants.C_PROPERTY_NAVPOS)) {
            result.append(" checked=\"checked\"");
        }
        result.append(disabled + ">");
        result.append("</td>\n");
        result.append("\t<td class=\"textcenter\">");       
        result.append("&nbsp;");
        result.append(buildTableRowEnd());
        
        // create NavText input row
        result.append(buildPropertyEntry(activeProperties, I_CmsConstants.C_PROPERTY_NAVTEXT, key("input.navtitle"), editable));
        
        // create NavPos select box row
        result.append(buildTableRowStart(key("input.insert")));
        synchronized (this) {
            result.append(CmsChnav.buildNavPosSelector(getCms(), getParamResource(), disabled + " class=\"maxwidth noborder\"", getSettings().getMessages()));
        }
        // get the old NavPos value and store it in hidden field
        String navPos = null;
        try {
            navPos = getCms().readProperty(getParamResource(), I_CmsConstants.C_PROPERTY_NAVPOS);
        } catch (CmsException e) {
            // ignore this exception
        }
        if (navPos == null) {
            navPos = "";
        }
        result.append("<input type=\"hidden\" name=\"" + PREFIX_HIDDEN + I_CmsConstants.C_PROPERTY_NAVPOS +  "\" value=\"" + navPos + "\">");
        result.append("</td>\n");
        result.append("\t<td class=\"textcenter\">");       
        result.append("&nbsp;");
        result.append(buildTableRowEnd());
 
        return result;
    }
    
    /**
     * Builds the html for a single text input property row.<p>
     * 
     * @param activeProperties Map of all active properties of the resource
     * @param propertyName the name of the property
     * @param propertyTitle the nice name of the property
     * @param editable indicates if the properties are editable
     * @return the html for a single text input property row
     */
    protected StringBuffer buildPropertyEntry(Map activeProperties, String propertyName, String propertyTitle, boolean editable) {
        StringBuffer result = new StringBuffer(256);
        // create "disabled" attribute if properties are not editable
        String disabled = "";
        if (!editable) {
            disabled = " disabled=\"disabled\"";
        }
        result.append(buildTableRowStart(propertyTitle));
        if (activeProperties.containsKey(propertyName)) {
            // the property is used, so create text field with value, checkbox and hidden field
            String propValue = CmsEncoder.escapeXml((String)activeProperties.get(propertyName));
            propertyName = CmsEncoder.escapeXml(propertyName);
            result.append("<input type=\"text\" class=\"maxwidth\" value=\"");
            result.append(propValue+"\" name=\"" + PREFIX_VALUE + propertyName + "\" id=\"" + PREFIX_VALUE + propertyName + "\"");
            if (editable) {
                result.append(" onKeyup=\"checkValue('" + propertyName + "');\"");
            }
            result.append(disabled+">");
            result.append("<input type=\"hidden\" name=\"" + PREFIX_HIDDEN + propertyName + "\" id=\"" + PREFIX_HIDDEN + propertyName + "\" value=\"" + propValue + "\">");
            result.append("</td>\n");
            result.append("\t<td class=\"textcenter\">");
            result.append("<input type=\"checkbox\" name=\"" + PREFIX_USEPROPERTY + propertyName + "\" id=\"" + PREFIX_USEPROPERTY + propertyName + "\" value=\"true\"");
            result.append(" checked=\"checked\"");
            if (editable) {
                result.append(" onClick=\"toggleDelete('" + propertyName + "');\"");
            }
            result.append(disabled + ">");
        } else {
            // property is not used, create an empty text input field
            result.append("<input type=\"text\" class=\"maxwidth\" ");
            result.append("name=\""+PREFIX_VALUE+propertyName+"\""+disabled+"></td>\n");
            result.append("\t<td class=\"textcenter\">&nbsp;");
        }
        result.append(buildTableRowEnd());
        return result;
    }
    
    /**
     * Builds the HTML for the end of a table row for a single property.<p>
     * 
     * @return the HTML code for a table row end
     */
    protected String buildTableRowEnd() {
        return "</td>\n</tr>\n";
    }
    
    /**
     * Builds the HTML for the start of a table row for a single property.<p>
     * 
     * @param propertyName the name of the current property
     * @return the HTML code for the start of a table row
     */
    protected StringBuffer buildTableRowStart(String propertyName) {
        StringBuffer result = new StringBuffer(96);
        result.append("<tr>\n");
        result.append("\t<td style=\"white-space: nowrap;\" unselectable=\"on\">" + propertyName);
        result.append("</td>\n");
        result.append("\t<td class=\"maxwidth\">");
        return result; 
    }
    
    /**
     * Builds the HTML for the common text input property values stored in the String array "PROPERTIES".<p>
     * 
     * @param editable indicates if the properties are editable
     * @param activeProperties Map of all active properties of the resource
     * @return the HTML code for the common text input fields
     */
    protected StringBuffer buildTextInput(boolean editable, Map activeProperties) {
        StringBuffer result = new StringBuffer(256);        
        Iterator i = getExplorerTypeSettings().getProperties().iterator();
        // iterate over the properties
        while (i.hasNext()) {
            String curProperty = (String)i.next();
            result.append(buildPropertyEntry(activeProperties, curProperty, curProperty, editable));
        }
        return result;
    }
    
    /**
     * Defines a new property.<p>
     * 
     * @param newProperty the name of the new property
     * @return true, if the new property was created, otherwise false
     * @throws CmsException if creation is not successful
     */
    private boolean defineProperty(String newProperty) throws CmsException {
        CmsResource res = getCms().readFileHeader(getParamResource());
        getCms().createPropertydefinition(newProperty, res.getType());
        return true;
    }
    
    /**
     * Builds a button row with an "ok", a "cancel" and an "advanced" button.<p>
     * 
     * @param okAttributes additional attributes for the "ok" button
     * @param cancelAttributes additional attributes for the "cancel" button
     * @param advancedAttributes additional attributes for the "advanced" button
     * @return the button row 
     */
    public String dialogButtonsOkCancelAdvanced(String okAttributes, String cancelAttributes, String advancedAttributes) {
        if (isEditable()) {
            int okButton = BUTTON_OK;
            if (getParamDialogmode() != null && getParamDialogmode().startsWith(MODE_WIZARD)) {
                // in wizard mode, display finish button instead of ok button
                okButton = BUTTON_FINISH;
            }
            return dialogButtons(new int[] {okButton, BUTTON_CANCEL, BUTTON_ADVANCED}, new String[] {okAttributes, cancelAttributes, advancedAttributes});
        } else {
            return dialogButtons(new int[] {BUTTON_CLOSE, BUTTON_ADVANCED}, new String[] {cancelAttributes, advancedAttributes});          
        }
    }
    
    /**
     * Initializes the explorer type settings for the current resource type.<p>
     */
    protected void initExplorerTypeSettings() {
        try {
            CmsResource res = getCms().readFileHeader(getParamResource());        
            String resTypeName = getCms().getResourceType(res.getType()).getResourceTypeName();
            setExplorerTypeSettings(OpenCms.getWorkplaceManager().getEplorerTypeSetting(resTypeName));
            setShowNavigation(getExplorerTypeSettings().isShowNavigation());
        } catch (CmsException e) {
            // error reading file, show error dialog
            getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
            setParamErrorstack(e.getStackTraceAsString());
            setParamReasonSuggestion(getErrorSuggestionDefault());
            try {
                getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
            } catch (JspException exc) {
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error("Error including common error dialog " + C_FILE_DIALOG_SCREEN_ERROR);
                }      
            }
        }
    }
    
    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
        // fill the parameter values in the get/set methods
        fillParamValues(request);
        // get the explorer type settings for the current resource
        initExplorerTypeSettings();
        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        boolean isPopup = Boolean.valueOf(getParamIsPopup()).booleanValue();
        // set the action for the JSP switch 
        if (DIALOG_SHOW_DEFAULT.equals(getParamAction())) {
            // save changed properties and redirect to the default OpenCms dialog
            setAction(ACTION_DEFAULT);
            try {
                actionEdit(request);
                sendCmsRedirect(CmsPropertyAdvanced.URI_PROPERTY_DIALOG + "?" + paramsAsRequest());
            } catch (Exception e) {
                // ignore this exception
            }          
        } else if (DIALOG_SAVE_EDIT.equals(getParamAction())) {
            // save the edited properties
            if (isPopup) {
                setAction(ACTION_CLOSEPOPUP_SAVE);
            } else {
                setAction(ACTION_SAVE_EDIT);
            }
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            // save the edited properties
            if (isPopup) {
                setAction(ACTION_CLOSEPOPUP);
            } else {
                setAction(ACTION_CANCEL);
            }
        } else {                   
            setAction(ACTION_EDIT); 
            String resName = CmsResource.getName(getParamResource());
            if (resName.startsWith(I_CmsConstants.C_TEMP_PREFIX)) {
                resName = resName.substring(1);
            }
            setParamTitle(key("title.property") + ": " + resName);
        }         
    } 

    /**
     * Performs the editing of the resources properties.<p>
     * 
     * @param request the HttpServletRequest
     * @return true, if the properties were successfully changed, otherwise false
     * @throws CmsException if editing is not successful
     */
    protected boolean performEditOperation(HttpServletRequest request) throws CmsException {
        Map activeProperties = getCms().readProperties(getParamResource());
        boolean useTempfileProject = "true".equals(getParamUsetempfileproject());
        try {
            if (useTempfileProject) {
                switchToTempProject();
            }
            // write the common properties defined in the explorer type settings
            Iterator i = getExplorerTypeSettings().getProperties().iterator();
            // iterate over the properties
            while (i.hasNext()) {
                String curProperty = (String)i.next();
                String paramValue = CmsEncoder.decode(request.getParameter(PREFIX_VALUE + curProperty));
                String oldValue = request.getParameter(PREFIX_HIDDEN + curProperty);
                writeProperty(curProperty, paramValue, oldValue, activeProperties);
            }
            
            // write the navigation properties if enabled
            if (showNavigation()) {
                // get the navigation enabled parameter
                String paramValue = request.getParameter("enablenav");
                String oldValue = null;
                if ("true".equals(paramValue)) {
                    // navigation enabled, update params
                    paramValue = request.getParameter("navpos");
                    if (!"-1".equals(paramValue)) {
                        // update the property only when it is different from "-1" (meaning no change)
                        oldValue = request.getParameter(PREFIX_HIDDEN + I_CmsConstants.C_PROPERTY_NAVPOS);
                        writeProperty(I_CmsConstants.C_PROPERTY_NAVPOS, paramValue, oldValue, activeProperties);
                    }
                    paramValue = request.getParameter(PREFIX_VALUE + I_CmsConstants.C_PROPERTY_NAVTEXT);
                    oldValue = request.getParameter(PREFIX_HIDDEN + I_CmsConstants.C_PROPERTY_NAVTEXT);
                    writeProperty(I_CmsConstants.C_PROPERTY_NAVTEXT, paramValue, oldValue, activeProperties);
                } else {
                    // navigation disabled, delete property values
                    writeProperty(I_CmsConstants.C_PROPERTY_NAVPOS, null, null, activeProperties);
                    writeProperty(I_CmsConstants.C_PROPERTY_NAVTEXT, null, null, activeProperties);
                }                  
            }
        } finally {
            if (useTempfileProject) {
                switchToCurrentProject();
            }
        }
        return true;
    }
    
    /**
     * Writes a property value for a resource, if the value was changed.<p>
     * 
     * If a property definition for the resource does not exist,
     * it is automatically created by this method.<p>
     * 
     * @param propName the name of the property
     * @param propValue the new value of the property
     * @param oldValue the old value of the property
     * @param activeProperties all active properties of the resource
     * @throws CmsException if something goes wrong
     */
    protected void writeProperty(String propName, String propValue, String oldValue, Map activeProperties) throws CmsException {
        // check if there is a parameter value for the current property
        boolean emptyParam = true;
        if (propValue != null) {
            if (!"".equals(propValue.trim())) {
                emptyParam = false;
            }
        }
        if (emptyParam) {
            // parameter is empty, check if the property has to be deleted
            if (activeProperties.containsKey(propName)) {
                // lock resource if autolock is enabled
                checkLock(getParamResource());
                
                //getCms().writePropertyObject(getParamResource(), curProperty);
                
                getCms().deleteProperty(getParamResource(), propName);
            }
        } else {
            // parameter is not empty, check if the value has changed
            if (!propValue.equals(oldValue)) {
                try {
                    // lock resource if autolock is enabled
                    checkLock(getParamResource());
                    getCms().writeProperty(getParamResource(), propName, propValue);
                } catch (CmsException e) {
                    if (e.getType() == CmsException.C_NOT_FOUND) {
                        defineProperty(propName);
                        getCms().writeProperty(getParamResource(), propName, propValue);
                    } else {
                        throw e;
                    }
                }     
            }
        }
    }
    
    /**
     * Returns if navigation properties are shown.<p>
     * 
     * @return true, if navigation properties are shown, otherwise false
     */
    public boolean showNavigation() {
        return m_showNavigation;
    }
    
    /**
     * Sets if navigation properties are shown.<p>
     * 
     * @param showNav true, if navigation properties are shown, otherwise false
     */
    public void setShowNavigation(boolean showNav) {
        m_showNavigation = showNav;
    }

    /**
     * Returns the explorer type settings for the current resource type.<p>
     * 
     * @return the explorer type settings for the current resource type
     */
    public CmsExplorerTypeSettings getExplorerTypeSettings() {
        return m_explorerTypeSettings;
    }

    /**
     * Sets the explorer type settings for the current resource type.<p>
     * 
     * @param typeSettings the explorer type settings for the current resource type
     */
    public void setExplorerTypeSettings(CmsExplorerTypeSettings typeSettings) {
        m_explorerTypeSettings = typeSettings;
    }

}
