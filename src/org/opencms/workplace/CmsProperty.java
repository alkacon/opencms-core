/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/CmsProperty.java,v $
 * Date   : $Date: 2004/01/06 17:06:05 $
 * Version: $Revision: 1.28 $
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

import org.opencms.lock.CmsLock;
import org.opencms.main.OpenCms;

import com.opencms.core.CmsException;
import com.opencms.file.CmsPropertydefinition;
import com.opencms.file.CmsResource;
import com.opencms.file.I_CmsResourceType;
import com.opencms.flex.jsp.CmsJspActionElement;
import com.opencms.util.Encoder;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Provides methods for the properties dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/jsp/dialogs/property_standard_html
 * </ul>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.28 $
 * 
 * @since 5.1
 */
public class CmsProperty extends CmsDialog implements I_CmsDialogHandler {
    
    /** The dialog type */
    public static final String DIALOG_TYPE = "property";
    
    /** Value for the action: show edit properties form */
    public static final int ACTION_SHOW_EDIT = 100;
    /** Value for the action: show define property form */
    public static final int ACTION_SHOW_DEFINE = 200;
    /** Value for the action: save edited properties */
    public static final int ACTION_SAVE_EDIT = 300;
    /** Value for the action: save defined property */
    public static final int ACTION_SAVE_DEFINE = 400;
    
    /** Request parameter value for the action: show edit properties form */
    public static final String DIALOG_SHOW_EDIT = "edit";
    /** Request parameter value for the action: show define property form */
    public static final String DIALOG_SHOW_DEFINE = "define";
    /** Request parameter value for the action: show information form */
    public static final String DIALOG_SHOW_DEFAULT = "default";
    
    /** Request parameter value for the action: save edited properties */
    public static final String DIALOG_SAVE_EDIT = "saveedit";
    /** Request parameter value for the action: save defined property */
    public static final String DIALOG_SAVE_DEFINE = "savedefine";
    
    /** Prefix for the input values */
    public static final String PREFIX_VALUE = "value-";
    /** Prefix for the hidden fields */
    public static final String PREFIX_HIDDEN = "hidden-";
    /** Prefix for the use property fields */
    public static final String PREFIX_USEPROPERTY = "use-";
    
    /** Request parameter name for the new property definition */
    public static final String PARAM_NEWPROPERTY = "newproperty";   
    
    /** The URI to the standard property dialog */
    public static final String URI_PROPERTY_DIALOG = C_PATH_DIALOGS + "property_standard.html"; 

    private String m_paramNewproperty;
    
    /**
     * Default constructor needed for dialog handler implementation.<p>
     */
    public CmsProperty() {
        super(null);
    }
    
    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsProperty(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsProperty(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }
    
    /**
     * @see org.opencms.workplace.I_CmsDialogHandler#getDialogUri(java.lang.String, com.opencms.flex.jsp.CmsJspActionElement)
     */
    public String getDialogUri(String resource, CmsJspActionElement jsp) {
        return URI_PROPERTY_DIALOG;
    }
    
    /**
     * @see org.opencms.workplace.I_CmsDialogHandler#getDialogHandler()
     */
    public String getDialogHandler() {
        return CmsDialogSelector.DIALOG_PROPERTY;
    }
    
    /**
     * Returns the value of the new property parameter, 
     * or null if this parameter was not provided.<p>
     * 
     * The new property parameter stores the name of the 
     * new defined property.<p>
     * 
     * @return the value of the new property parameter
     */    
    public String getParamNewproperty() {
        return m_paramNewproperty;
    }

    /**
     * Sets the value of the new property parameter.<p>
     * 
     * @param value the value to set
     */
    public void setParamNewproperty(String value) {
        m_paramNewproperty = value;
    }
    
    /**
     * Returns all possible properties for the current resource type.<p>
     * 
     * @return all property definitions for te resource type
     * @throws CmsException if something goes wrong
     */
    public Vector getPropertyDefinitions() throws CmsException {
        CmsResource res = getCms().readFileHeader(getParamResource());
        I_CmsResourceType type = getCms().getResourceType(res.getType());
        return getCms().readAllPropertydefinitions(type.getResourceTypeName());           
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
            setAction(ACTION_DEFAULT);
            setParamTitle(key("title.property") + ": " + CmsResource.getName(getParamResource()));
        } else if (DIALOG_SHOW_EDIT.equals(getParamAction())) {
            setAction(ACTION_SHOW_EDIT);
            setParamTitle(key("title.editpropertyinfo") + ": " + CmsResource.getName(getParamResource()));                            
        } else if (DIALOG_SHOW_DEFINE.equals(getParamAction())) {
            setAction(ACTION_SHOW_DEFINE);
            setParamTitle(key("title.newpropertydef") + ": " + CmsResource.getName(getParamResource()));
        } else if (DIALOG_SAVE_EDIT.equals(getParamAction())) {
            setAction(ACTION_SAVE_EDIT);
        } else if (DIALOG_SAVE_DEFINE.equals(getParamAction())) {
            setAction(ACTION_SAVE_DEFINE);
        } else { 
            // set the default action               
            setAction(ACTION_DEFAULT); 
            setParamTitle(key("title.property") + ": " + CmsResource.getName(getParamResource()));
        }      
    } 
    
    /**
     * Creates the HTML String for the list of set properties of the selected resource.<p>
     * 
     * @return the HTML output String of the property list
     * @throws JspException if problems including sub-elements occur
     */
    public String buildPropertiesList() throws JspException {
        StringBuffer retValue = new StringBuffer(256);
        Map properties = null;
        try {
            properties = getCms().readProperties(getParamResource());
        } catch (CmsException e) {
            // error getting properties, show error dialog
            setParamErrorstack(e.getStackTraceAsString());
            String message = "Error reading properties from resource " + getParamResource();
            setParamMessage(message + key("error.message." + getParamDialogtype()));
            setParamReasonSuggestion(getErrorSuggestionDefault());
            getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
        }        
        
        Iterator i = properties.keySet().iterator();
        boolean setProperties = i.hasNext();
        
        // create table if properties are present
        if (setProperties) {
            retValue.append("<table border=\"0\">\n");
        }
        
        // iterate over all set properties
        while (i.hasNext()) {
            String key = Encoder.escapeXml((String)i.next());           
            String value = Encoder.escapeXml((String)properties.get(key));
            retValue.append("<tr>\n\t<td>");
            retValue.append(key+":</td>\n");
            retValue.append("\t<td>"+value+"</td>\n");
            retValue.append("</tr>\n");
        }
        
        // close table if properties are present
        if (setProperties) {
            retValue.append("</table>");
        } else {
            // no propertis present, show error message
            retValue.append(key("error.message.noprop"));
        }
        
        return retValue.toString();
    }
    
    /**
     * Creates the HTML String for the active properties overview of the current resource.<p>
     * 
     * @return the HTML output String for active properties of the resource
     */
    public String buildActivePropertiesList() {
        StringBuffer retValue = new StringBuffer(256);
        Vector propertyDef = new Vector();
        try {
            propertyDef = getPropertyDefinitions();
        } catch (CmsException e) {
            // ignore
        }
        
        for (int i=0; i<propertyDef.size(); i++) {
            CmsPropertydefinition curProperty = (CmsPropertydefinition)propertyDef.elementAt(i);
            retValue.append(Encoder.escapeXml(curProperty.getName()));
            if ((i+1) < propertyDef.size()) {
                retValue.append("<br>");            
            }
        }
        
        return retValue.toString();
    }
    
    /**
     * Creates the HTML String for the edit properties form.<p>
     * 
     * @return the HTML output String for the edit properties form
     */
    public String buildEditForm() {
        StringBuffer retValue = new StringBuffer(1024);
        
        // get all properties for the resource
        Vector propertyDef = new Vector();
        try {
            propertyDef = getPropertyDefinitions();
        } catch (CmsException e) {
            // ignore
        }
        
        // get all used properties for the resource
        Map activeProperties = null;
        try {
            activeProperties = getCms().readProperties(getParamResource());
        } catch (CmsException e) {
            // ignore
        }
        boolean present = false;
        if (propertyDef.size() > 0) {
            present = true; 
        }
        
        if (present) {
            // there are properties defined for this resource, build the form list
            retValue.append("<table border=\"0\">\n");
            retValue.append("<tr>\n");
            retValue.append("\t<td class=\"textbold\">"+key("input.property")+"</td>\n");
            retValue.append("\t<td class=\"textbold\">"+key("label.value")+"</td>\n");
            retValue.append("\t<td class=\"textbold\" style=\"white-space: nowrap;\">"+key("input.usedproperty")+"</td>\n");            
            retValue.append("</tr>\n");
            retValue.append("<tr>\n\t<td>"+dialogSpacer()+"</td>\n</tr>\n");
            for (int i=0; i<propertyDef.size(); i++) {
                CmsPropertydefinition curProperty = (CmsPropertydefinition)propertyDef.elementAt(i);
                String propName = Encoder.escapeXml(curProperty.getName());
                retValue.append("<tr>\n");
                retValue.append("\t<td style=\"white-space: nowrap;\">"+propName);
                retValue.append("</td>\n");
                retValue.append("\t<td class=\"maxwidth\">");
                if (activeProperties.containsKey(curProperty.getName())) {
                    // the property is used, so create text field with value, checkbox and hidden field
                    String propValue = Encoder.escapeXml((String)activeProperties.get(curProperty.getName()));
                    retValue.append("<input type=\"text\" class=\"maxwidth\" value=\"");
                    retValue.append(propValue+"\" name=\""+PREFIX_VALUE+propName+"\" id=\""+PREFIX_VALUE+propName+"\" onKeyup=\"checkValue('"+propName+"');\">");
                    retValue.append("<input type=\"hidden\" name=\""+PREFIX_HIDDEN+propName+"\" id=\""+PREFIX_HIDDEN+propName+"\" value=\""+propValue+"\"></td>\n");
                    retValue.append("\t<td class=\"textcenter\">");
                    retValue.append("<input type=\"checkbox\" name=\""+PREFIX_USEPROPERTY+propName+"\" id=\""+PREFIX_USEPROPERTY+propName+"\" value=\"true\"");
                    retValue.append(" checked=\"checked\" onClick=\"toggleDelete('"+propName+"');\">");
                    retValue.append("</td>\n");
                } else {
                    // property is not used, create an empty text input field
                    retValue.append("<input type=\"text\" class=\"maxwidth\" ");
                    retValue.append("name=\""+PREFIX_VALUE+propName+"\"></td>\n");
                    retValue.append("\t<td>&nbsp;</td>");
                }
                retValue.append("</tr>\n");
            }
            retValue.append("</table>");
            
        } else {
            // there are no properties defined for this resource, show nothing
            retValue.append("no props defined!");
        }
        
        return retValue.toString();
    }
    
    /**
     * Creates the HTML String for the buttons "edit" and "define properties" depending on the lock state of the resource.<p>
     *  
     * @return the HTML output String for the buttons
     */
    public String buildActionButtons() {       
        if (isEditable()) {
            StringBuffer retValue = new StringBuffer(256);
            String dialogLink = getJsp().link(URI_PROPERTY_DIALOG);
            
            retValue.append("<table border=\"0\">\n");
            retValue.append("<tr>\n\t<td>\n");
            
            //  create button to switch to the edit properties window
            setParamAction(DIALOG_SHOW_EDIT);
            retValue.append("<form action=\""+dialogLink+"\" method=\"post\" class=\"nomargin\" name=\"define\">\n");
            retValue.append(paramsAsHidden());
            retValue.append("<input type=\"submit\" class=\"dialogbutton\" style=\"margin-left: 0;\" name=\"ok\" value=\""+key("button.edit")+"\">\n");
            retValue.append("</form>\n");
            
            retValue.append("\t</td>\n");
            retValue.append("\t<td>\n");
            
            
            // create button to switch to the define property window
            setParamAction(DIALOG_SHOW_DEFINE);
            retValue.append("<form action=\""+dialogLink+"\" method=\"post\" class=\"nomargin\" name=\"define\">\n");
            retValue.append(paramsAsHidden());
            retValue.append("<input type=\"submit\" class=\"dialogbutton\" name=\"ok\" value=\""+key("button.newpropertydef")+"\">\n");
            retValue.append("</form>\n");
            
            retValue.append("\t</td>\n</tr>\n");
            retValue.append("</table>");
            
            return retValue.toString();
        } else {
            // resource is not locked, don't display edit buttons
            return "";
        }
    }
    
    /**
     * Returns whether the properties are editable or not depending on the lock state of the resource.<p>
     * 
     * @return true if properties are editable, otherwise false
     */
    public boolean isEditable() {
        String resourceName = getParamResource();
        CmsResource file = null;
        CmsLock lock = null;
    
        try {
            file = getCms().readFileHeader(resourceName);
            // check if resource is a folder
            if (file.isFolder()) {
                resourceName += "/";            
            }
        } catch (CmsException e) {
            // ignore
        }
    
        try {
            // get the lock for the resource
            lock = getCms().getLock(resourceName);
        } catch (CmsException e) {
            lock = CmsLock.getNullLock();
        
            if (OpenCms.getLog(this).isErrorEnabled()) { 
                OpenCms.getLog(this).error("Error getting lock state for resource " + resourceName, e);
            }             
        }
    
        if (!lock.isNullLock()) {
            // determine if resource is editable...
            if (lock.getType() != CmsLock.C_TYPE_SHARED_EXCLUSIVE && lock.getType() != CmsLock.C_TYPE_SHARED_INHERITED
                    && lock.getUserId().equals(getCms().getRequestContext().currentUser().getId())
                    && getCms().getRequestContext().currentProject().getId() == lock.getProjectId()) {
                // lock is not shared and belongs to the current user in the current project, so properties are editable
                return true;
            }
        } else if (getSettings().getAutoLockResources()) {
            return true;
        }
        // lock is null or belongs to other user and/or project, properties are not editable
        return false;
    }

    /**
     * Performs the define property action, will be called by the JSP page.<p>
     * 
     * @throws JspException if problems including sub-elements occur
     */
    public void actionDefine() throws JspException {
        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
        try {
            performDefineOperation();
            // set the request parameters before returning to the overview
            setParamAction(DIALOG_SHOW_DEFAULT);
            setParamNewproperty(null);
            getCms().getRequestContext().getResponse().sendCmsRedirect(getJsp().getRequestContext().getUri()+"?"+paramsAsRequest());              
        } catch (CmsException e) {
            // error defining property, show error dialog
            setParamErrorstack(e.getStackTraceAsString());
            setParamMessage(key("error.message.newprop"));
            setParamReasonSuggestion(getErrorSuggestionDefault());
            getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
         
        } catch (IOException exc) {
            getJsp().include(C_FILE_EXPLORER_FILELIST);
        }
    }
    
    /**
     * Performs the definition of a new property.<p>
     * 
     * @return true, if the new property was created, otherwise false
     * @throws CmsException if creation is not successful
     */
    private boolean performDefineOperation() throws CmsException {
        CmsResource res = getCms().readFileHeader(getParamResource());
        String newProperty = getParamNewproperty();
        if (newProperty != null && !"".equals(newProperty.trim())) {
            getCms().createPropertydefinition(newProperty, res.getType());
            return true;
        } else {
            throw new CmsException("You entered an invalid property name", CmsException.C_BAD_NAME); 
        } 
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
            performEditOperation(request);
            // set the request parameters before returning to the overview
            setParamAction(DIALOG_SHOW_DEFAULT);
            getCms().getRequestContext().getResponse().sendCmsRedirect(getJsp().getRequestContext().getUri()+"?"+paramsAsRequest());              
        } catch (CmsException e) {
            // error defining property, show error dialog
            setParamErrorstack(e.getStackTraceAsString());
            setParamReasonSuggestion(getErrorSuggestionDefault());
                getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
     
            } catch (IOException exc) {
                getJsp().include(C_FILE_EXPLORER_FILELIST);
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
        Vector propertyDef = getPropertyDefinitions();
        Map activeProperties = getCms().readProperties(getParamResource());
        boolean lockChecked = false;
        
        // check all property definitions of the resource for new values
        for (int i=0; i<propertyDef.size(); i++) {
            CmsPropertydefinition curProperty = (CmsPropertydefinition)propertyDef.elementAt(i);
            String propName = Encoder.escapeXml(curProperty.getName());
            String paramValue = request.getParameter(PREFIX_VALUE+propName);
                        
            // check if there is a parameter value for the current property
            boolean emptyParam = true;
            if (paramValue != null) {
                if (!"".equals(paramValue.trim())) {
                    emptyParam = false;
                }
            }
            if (emptyParam) {
                // parameter is empty, check if the property has to be deleted
                if (activeProperties.containsKey(curProperty.getName())) {
                    if (!lockChecked) {
                        // lock resource if autolock is enabled
                        checkLock(getParamResource());
                        lockChecked = true;
                    }
                    getCms().deleteProperty(getParamResource(), curProperty.getName());
                }
            } else {
                // parameter is not empty, check if the value has changed
                String oldValue = request.getParameter(PREFIX_HIDDEN+propName);
                if (!paramValue.equals(oldValue)) {
                    if (!lockChecked) {
                        // lock resource if autolock is enabled
                        checkLock(getParamResource());
                        lockChecked = true;
                    }
                    getCms().writeProperty(getParamResource(), curProperty.getName(), paramValue);
                }
            }
        }     
        return true;
    }
    
}
