/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editor/Attic/CmsDialogProperty.java,v $
 * Date   : $Date: 2004/02/13 13:41:45 $
 * Version: $Revision: 1.11 $
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
package org.opencms.workplace.editor;

import org.opencms.file.CmsResource;
import com.opencms.workplace.CmsHelperMastertemplates;

import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.workplace.CmsChnav;
import org.opencms.workplace.CmsProperty;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Provides methods for the editor property dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/jsp/editors/dialogs/property.html
 * </ul>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.11 $
 * 
 * @since 5.3.0
 */
public class CmsDialogProperty extends CmsProperty {
    
    /** Value for the action: edit the properties */
    public static final int ACTION_EDIT = 500;
    
    /** Stores the property names which should be listed in the edit form */
    public static final String[] PROPERTIES = {I_CmsConstants.C_PROPERTY_TITLE, I_CmsConstants.C_PROPERTY_KEYWORDS, I_CmsConstants.C_PROPERTY_DESCRIPTION };
    
    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsDialogProperty(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsDialogProperty(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
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
        if (DIALOG_SHOW_DEFAULT.equals(getParamAction())) {
            // redirect to the default OpenCms dialog
            setAction(ACTION_DEFAULT);
            try {
                getCms().getRequestContext().getResponse().sendCmsRedirect(CmsProperty.URI_PROPERTY_DIALOG + "?" + paramsAsRequest());
            } catch (Exception e) {
                // ignore this exception
            }          
        } else if (DIALOG_SAVE_EDIT.equals(getParamAction())) {
            // save the edited properties
            setAction(ACTION_SAVE_EDIT);
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
     * Creates the HTML String for the edit properties form.<p>
     * 
     * @return the HTML output String for the edit properties form
     */
    public String buildEditForm() {
        StringBuffer retValue = new StringBuffer(2048);
        
        // check if the properties are editable
        boolean editable =  isEditable();
        // create "disabled" attribute if properties are not editable
        String disabled = "";
        if (!editable) {
            disabled = " disabled=\"disabled\"";
        }    
        
        // get all used properties for the resource
        Map activeProperties = null;
        try {
            activeProperties = getCms().readProperties(getParamResource());
        } catch (CmsException e) { 
            // ignore this exception
        }
        
        retValue.append("<table border=\"0\">\n");
        retValue.append("<tr>\n");
        retValue.append("\t<td class=\"textbold\">" + key("input.property") + "</td>\n");
        retValue.append("\t<td class=\"textbold\">" + key("label.value") + "</td>\n");   
        retValue.append("\t<td class=\"textbold\" style=\"white-space: nowrap;\">" + key("input.usedproperty") + "</td>\n");    
        retValue.append("</tr>\n");
        retValue.append("<tr><td><span style=\"height: 6px;\"></span></td></tr>\n");
        
        // create template select box row
        retValue.append(buildTableRowStart(key("input.template")));
        retValue.append(buildSelectTemplates("name=\"" + I_CmsConstants.C_PROPERTY_TEMPLATE + "\" class=\"maxwidth noborder\"" + disabled));
        retValue.append("</td>\n");
        retValue.append("\t<td class=\"textcenter\">");       
        retValue.append("&nbsp;");
        retValue.append(buildTableRowEnd());
        
        // create the text property input rows
        retValue.append(buildTextInput(editable, activeProperties));
        
        retValue.append(buildPageProperties(editable, activeProperties));
     
        retValue.append("</table>");       
       
        return retValue.toString();
    }
       
    /**
     * Builds the HTML code for the special properties of an xmlpage resource.<p>
     * 
     * @param editable indicates if the properties are editable
     * @param activeProperties Map of all active properties of the resource 
     * @return the HTML code for the special properties of a file resource
     */
    private StringBuffer buildPageProperties(boolean editable, Map activeProperties) {
        StringBuffer retValue = new StringBuffer(1024);
        
        // create "disabled" attribute if properties are not editable
        String disabled = "";
        if (!editable) {
            disabled = " disabled=\"disabled\"";
        }    
        
        // create "add to navigation" checkbox
        retValue.append(buildTableRowStart(key("input.addtonav")));
        retValue.append("<input type=\"checkbox\" name=\"enablenav\" id=\"enablenav\" value=\"true\" onClick=\"toggleNav();\"");
        if (activeProperties.containsKey(I_CmsConstants.C_PROPERTY_NAVTEXT) && activeProperties.containsKey(I_CmsConstants.C_PROPERTY_NAVPOS)) {
            retValue.append(" checked=\"checked\"");
        }
        retValue.append(">");
        retValue.append("</td>\n");
        retValue.append("\t<td class=\"textcenter\">");       
        retValue.append("&nbsp;");
        retValue.append(buildTableRowEnd());
        
        // create NavText input row
        retValue.append(buildPropertyEntry(activeProperties, I_CmsConstants.C_PROPERTY_NAVTEXT, key("input.navtitle"), editable));
        
        // create NavPos select box row
        retValue.append(buildTableRowStart(key("input.insert")));
        synchronized (this) {
            retValue.append(CmsChnav.buildNavPosSelector(getCms(), getParamResource(), disabled + " class=\"maxwidth noborder\"", getSettings().getMessages()));
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
        retValue.append("<input type=\"hidden\" name=\"" + PREFIX_HIDDEN + I_CmsConstants.C_PROPERTY_NAVPOS +  "\" value=\"" + navPos + "\">");
        retValue.append("</td>\n");
        retValue.append("\t<td class=\"textcenter\">");       
        retValue.append("&nbsp;");
        retValue.append(buildTableRowEnd());
 
        return retValue;
    }
    
    /**
     * Builds the html for the page template select box.<p>
     * 
     * @param attributes optional attributes for the &lt;select&gt; tag
     * @return the html for the page template select box
     */
    public String buildSelectTemplates(String attributes) {
        Vector names = new Vector();
        Vector values = new Vector();
        Integer selectedValue = new Integer(-1);
        String currentTemplate = null;
        try {
            currentTemplate = getCms().readProperty(getParamResource(), I_CmsConstants.C_PROPERTY_TEMPLATE, true);
            selectedValue = CmsHelperMastertemplates.getTemplates(getCms(), names, values, currentTemplate, -1);
        } catch (CmsException e) {
            // ignore this exception
        }
        if (currentTemplate == null) {
            currentTemplate = "";
        }        
        if (selectedValue.intValue() == -1) {
            // no template found -> use the given one
            // first clean the vectors
            names.removeAllElements();
            values.removeAllElements();
            // now add the current template
            String name = currentTemplate;
            try { 
                // read the title of this template
                name = getCms().readProperty(name, I_CmsConstants.C_PROPERTY_TITLE);
            } catch (CmsException exc) {
                // ignore this exception - the title for this template was not readable
            }
            names.add(name);
            values.add(currentTemplate);
        }

        String hiddenField = "<input type=\"hidden\" name=\"" + PREFIX_HIDDEN + I_CmsConstants.C_PROPERTY_TEMPLATE +  "\" value=\"" + currentTemplate + "\">";
        return buildSelect(attributes, names, values, selectedValue.intValue(), false) + hiddenField;
    }
    
    /**
     * Builds the HTML for the start of a table row for a single property.<p>
     * 
     * @param propertyName the name of the current property
     * @return the HTML code for the start of a table row
     */
    private StringBuffer buildTableRowStart(String propertyName) {
        StringBuffer retValue = new StringBuffer(96);
        retValue.append("<tr>\n");
        retValue.append("\t<td style=\"white-space: nowrap;\" unselectable=\"on\">" + propertyName);
        retValue.append("</td>\n");
        retValue.append("\t<td class=\"maxwidth\">");
        return retValue; 
    }
    
    /**
     * Builds the HTML for the end of a table row for a single property.<p>
     * 
     * @return the HTML code for a table row end
     */
    private String buildTableRowEnd() {
        return "</td>\n</tr>\n";
    }
    
    /**
     * Builds the HTML for the common text input property values stored in the String array "PROPERTIES".<p>
     * 
     * @param editable indicates if the properties are editable
     * @param activeProperties Map of all active properties of the resource
     * @return the HTML code for the common text input fields
     */
    private StringBuffer buildTextInput(boolean editable, Map activeProperties) {
        StringBuffer retValue = new StringBuffer(256);        
        // iterate over the array
        for (int i=0; i<PROPERTIES.length; i++) {
            retValue.append(buildPropertyEntry(activeProperties, PROPERTIES[i], PROPERTIES[i], editable));
        }
        return retValue;
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
    private StringBuffer buildPropertyEntry(Map activeProperties, String propertyName, String propertyTitle, boolean editable) {
        StringBuffer retValue = new StringBuffer(256);
        // create "disabled" attribute if properties are not editable
        String disabled = "";
        if (!editable) {
            disabled = " disabled=\"disabled\"";
        }
        retValue.append(buildTableRowStart(propertyTitle));
        if (activeProperties.containsKey(propertyName)) {
            // the property is used, so create text field with value, checkbox and hidden field
            String propValue = CmsEncoder.escapeXml((String)activeProperties.get(propertyName));
            propertyName = CmsEncoder.escapeXml(propertyName);
            retValue.append("<input type=\"text\" class=\"maxwidth\" value=\"");
            retValue.append(propValue+"\" name=\""+PREFIX_VALUE+propertyName+"\" id=\""+PREFIX_VALUE+propertyName+"\"");
            if (editable) {
                retValue.append(" onKeyup=\"checkValue('"+propertyName+"');\"");
            }
            retValue.append(disabled+">");
            retValue.append("<input type=\"hidden\" name=\""+PREFIX_HIDDEN+propertyName+"\" id=\""+PREFIX_HIDDEN+propertyName+"\" value=\""+propValue+"\">");
            retValue.append("</td>\n");
            retValue.append("\t<td class=\"textcenter\">");
            retValue.append("<input type=\"checkbox\" name=\""+PREFIX_USEPROPERTY+propertyName+"\" id=\""+PREFIX_USEPROPERTY+propertyName+"\" value=\"true\"");
            retValue.append(" checked=\"checked\"");
            if (editable) {
                retValue.append(" onClick=\"toggleDelete('"+propertyName+"');\"");
            }
            retValue.append(disabled+">");
        } else {
            // property is not used, create an empty text input field
            retValue.append("<input type=\"text\" class=\"maxwidth\" ");
            retValue.append("name=\""+PREFIX_VALUE+propertyName+"\""+disabled+"></td>\n");
            retValue.append("\t<td class=\"textcenter\">&nbsp;");
        }
        retValue.append(buildTableRowEnd());
        return retValue;
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
            // return to the explorer view 
            closeDialog();         
        } catch (CmsException e) {
            // error defining property, show error dialog
            setParamErrorstack(e.getStackTraceAsString());
            setParamReasonSuggestion(getErrorSuggestionDefault());
            getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
        } 
    }

    /**
     * Performs the editing of the resources properties.<p>
     * 
     * @param request the HttpServletRequest
     * @return true, if the properties were successfully changed, otherwise false
     * @throws CmsException if editing is not successful
     */
    private boolean performEditOperation(HttpServletRequest request) throws CmsException {
        Map activeProperties = getCms().readProperties(getParamResource());
        boolean useTempfileProject = "true".equals(getParamUsetempfileproject());
        try {
            if (useTempfileProject) {
                switchToTempProject();
            }
            // write the common properties defined by the String array
            for (int i=0; i<PROPERTIES.length; i++) {
                String paramValue = request.getParameter(PREFIX_VALUE + PROPERTIES[i]);
                String oldValue = request.getParameter(PREFIX_HIDDEN + PROPERTIES[i]);
                writeProperty(PROPERTIES[i], paramValue, oldValue, activeProperties);
            }
                
            // write special file properties
            
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
            
            // get the template parameter
            paramValue = request.getParameter(I_CmsConstants.C_PROPERTY_TEMPLATE);
            oldValue = request.getParameter(PREFIX_HIDDEN + I_CmsConstants.C_PROPERTY_TEMPLATE);
            writeProperty(I_CmsConstants.C_PROPERTY_TEMPLATE, paramValue, oldValue, activeProperties);
            if (paramValue != null && !paramValue.equals(oldValue)) {
                // template has changed, refresh editor window
                if (getParamOkFunctions() != null && getParamOkFunctions().startsWith("window.close()")) {
                    setParamOkFunctions("window.opener.doTemplSubmit(1);");
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
    private void writeProperty(String propName, String propValue, String oldValue, Map activeProperties) throws CmsException {
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

}
