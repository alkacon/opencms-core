/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editor/Attic/CmsDialogProperty.java,v $
 * Date   : $Date: 2004/01/14 10:00:04 $
 * Version: $Revision: 1.1 $
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

import com.opencms.core.CmsException;
import com.opencms.file.CmsResource;
import com.opencms.flex.jsp.CmsJspActionElement;
import com.opencms.flex.util.CmsMessages;
import com.opencms.util.Encoder;

import org.opencms.workplace.CmsProperty;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

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
 * @version $Revision: 1.1 $
 * 
 * @since 5.3.0
 */
public class CmsDialogProperty extends CmsProperty {
    
    /** Value for the action: edit the properties */
    public static final int ACTION_EDIT = 500;
    
    /** Stores the property names which should be listed in the edit form */
    public static final String[] PROPERTIES = {"Title", "Keywords", "Description", };
    
    public static final String[] SORT_METHODS = {"A", "T", "G", "D", "S"};    
    public static final String[] PAGE_TYPES = {"default", "catalog", "other"};       
    
    // the special property definition names for files and folders
    public static final String PROP_CATEGORY = "category";
    public static final String PROP_PAGETYPE = "page_type";
    public static final String PROP_SHOWDOCUMENT = "showdocument";
    public static final String PROP_SORTMETHOD = "SortMethod";
    
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
            setParamTitle(key("title.property") + ": " + CmsResource.getName(getParamResource()));
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
        
        // get all used properties for the resource
        Map activeProperties = null;
        try {
            activeProperties = getCms().readProperties(getParamResource());
        } catch (CmsException e) { 
            // ignore this exception
        }
        
        retValue.append("<table border=\"0\">\n");
        retValue.append("<tr>\n");
        retValue.append("\t<td class=\"textbold\">"+key("input.property")+"</td>\n");
        retValue.append("\t<td class=\"textbold\">"+key("label.value")+"</td>\n");   
        retValue.append("\t<td class=\"textbold\" style=\"white-space: nowrap;\">"+key("input.usedproperty")+"</td>\n");    
        retValue.append("</tr>\n");
        retValue.append("<tr>\n\t<td>"+dialogSpacer()+"</td>\n</tr>\n");
        retValue.append(buildTextInput(editable, activeProperties));
        
        //retValue.append(buildPageProperties(editable, activeProperties));
     
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
        CmsMessages wpMessages = new CmsMessages("lgt_dialogs", getSettings().getLanguage());
        
        // create "disabled" attribute if properties are not editable
        String disabled = "";
        if (!editable) {
            disabled = " disabled=\"disabled\"";
        }
        
        // check if the property is already defined        
        boolean pageTypeSet = activeProperties.containsKey(PROP_PAGETYPE);       
        String propValue = null;
        
        // get the current property value
        if (pageTypeSet) {
            propValue = (String)activeProperties.get(PROP_PAGETYPE);
        }
        
        // build the lists for the method selection box
        List methodValues = new ArrayList(6);
        List methodNames = new ArrayList(6);
        // first element is the "nothing selected" case
        methodValues.add("");
        methodNames.add(wpMessages.key("select.notselected"));
        String[] localizedNames = getLocalizedNames(wpMessages.key("pagetype.names"), ",");
        int selectedIndex = 0;
        for (int i=0; i<PAGE_TYPES.length; i++) {
            // add names and values to the lists
            methodValues.add(PAGE_TYPES[i]);
            methodNames.add(localizedNames[i]);
            if (PAGE_TYPES[i].equalsIgnoreCase(propValue)) {
                selectedIndex = (i + 1);
            }
        }
        
        // build the attribute for the javascript depending on state of property
        String checkMethod = "";
        if (pageTypeSet) {
            checkMethod = " onchange=\"checkPageType();\"";
        }
        retValue.append(buildTableRowStart(PROP_PAGETYPE));
        retValue.append(buildSelect("name=\""+PREFIX_VALUE+PROP_PAGETYPE+"\" id=\""+PREFIX_VALUE+PROP_PAGETYPE+"\"" + checkMethod + disabled, methodNames, methodValues, selectedIndex));
        retValue.append("</td>\n");
        retValue.append("\t<td class=\"textcenter\">");       
        // append the checkbox depending on property state
        if (pageTypeSet) {
            retValue.append("<input type=\"hidden\" name=\""+PREFIX_HIDDEN+PROP_PAGETYPE+"\" id=\""+PREFIX_HIDDEN+PROP_PAGETYPE+"\" value=\""+selectedIndex+"\">");
            retValue.append("<input type=\"checkbox\" name=\""+PREFIX_USEPROPERTY+PROP_PAGETYPE+"\" id=\""+PREFIX_USEPROPERTY+PROP_PAGETYPE+"\" value=\"true\"");
            retValue.append(" checked=\"checked\"");
            if (editable) {
                retValue.append(" onClick=\"toggleDeletePageType();\"");
            }
            retValue.append(disabled+">");
        } else {
            retValue.append("&nbsp;");
        }
        retValue.append(buildTableRowEnd());
        
        //      check if the property is already defined        
        boolean showDocumentSet = activeProperties.containsKey(PROP_SHOWDOCUMENT);       
        propValue = null;
        // get the current property value
        if (showDocumentSet) {
            propValue = (String)activeProperties.get(PROP_SHOWDOCUMENT);
        }
        
        retValue.append(buildTableRowStart(PROP_SHOWDOCUMENT));
        retValue.append("<input type=\"radio\" name=\""+PREFIX_VALUE+PROP_SHOWDOCUMENT+"\" id=\""+PREFIX_VALUE+PROP_SHOWDOCUMENT+"---1\" value=\"true\"" + disabled);
        if ("true".equalsIgnoreCase(propValue)) {
            retValue.append(" checked=\"checked\"");
        }
        retValue.append(">"+wpMessages.key("showdocument.yes")+"&nbsp;");
        retValue.append("<input type=\"radio\" name=\""+PREFIX_VALUE+PROP_SHOWDOCUMENT+"\" id=\""+PREFIX_VALUE+PROP_SHOWDOCUMENT+"---2\" value=\"false\"" + disabled);
        if ("false".equalsIgnoreCase(propValue)) {
            retValue.append(" checked=\"checked\"");
        }
        retValue.append(">"+wpMessages.key("showdocument.no")+"");
        retValue.append("</td>\n");
        retValue.append("\t<td class=\"textcenter\">");
        // append the checkbox depending on property state
        if (showDocumentSet) {
            retValue.append("<input type=\"hidden\" name=\""+PREFIX_HIDDEN+PROP_SHOWDOCUMENT+"\" id=\""+PREFIX_HIDDEN+PROP_SHOWDOCUMENT+"\" value=\""+propValue+"\">");
            retValue.append("<input type=\"checkbox\" name=\""+PREFIX_USEPROPERTY+PROP_SHOWDOCUMENT+"\" id=\""+PREFIX_USEPROPERTY+PROP_SHOWDOCUMENT+"\" value=\"true\"");
            retValue.append(" checked=\"checked\"");
            if (editable) {
                retValue.append(" onClick=\"toggleDeleteShowDocument();\"");
            }
            retValue.append(disabled+">");
        } else {
            retValue.append("&nbsp;");
        }
        retValue.append(buildTableRowEnd());
        
        return retValue;
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
        retValue.append("\t<td style=\"white-space: nowrap;\">" + propertyName);
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
     * Builds a String array of localized names for the select boxes.<p>
     * 
     * @param nameString the localized String
     * @param delim the delimiter
     * @return String array with localized names
     */
    private String[] getLocalizedNames(String nameString, String delim) {
        String[] localizedNames = new String[10];
        StringTokenizer T = new StringTokenizer(nameString, delim);
        int counter = 0;
        while (T.hasMoreTokens()) {
            localizedNames[counter] = T.nextToken();
            counter++;
        }
        return localizedNames;
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
        
        // create "disabled" attribute if properties are not editable
        String disabled = "";
        if (!editable) {
            disabled = " disabled=\"disabled\"";
        }
        
        // iterate over the array
        for (int i=0; i<PROPERTIES.length; i++) {
            String propName = Encoder.escapeXml(PROPERTIES[i]);
            retValue.append(buildTableRowStart(propName));
            if (activeProperties.containsKey(PROPERTIES[i])) {
                // the property is used, so create text field with value, checkbox and hidden field
                String propValue = Encoder.escapeXml((String)activeProperties.get(PROPERTIES[i]));
                retValue.append("<input type=\"text\" class=\"maxwidth\" value=\"");
                retValue.append(propValue+"\" name=\""+PREFIX_VALUE+propName+"\" id=\""+PREFIX_VALUE+propName+"\"");
                if (editable) {
                    retValue.append(" onKeyup=\"checkValue('"+propName+"');\"");
                }
                retValue.append(disabled+">");
                retValue.append("<input type=\"hidden\" name=\""+PREFIX_HIDDEN+propName+"\" id=\""+PREFIX_HIDDEN+propName+"\" value=\""+propValue+"\">");
                retValue.append("</td>\n");
                retValue.append("\t<td class=\"textcenter\">");
                retValue.append("<input type=\"checkbox\" name=\""+PREFIX_USEPROPERTY+propName+"\" id=\""+PREFIX_USEPROPERTY+propName+"\" value=\"true\"");
                retValue.append(" checked=\"checked\"");
                if (editable) {
                    retValue.append(" onClick=\"toggleDelete('"+propName+"');\"");
                }
                retValue.append(disabled+">");
            } else {
                // property is not used, create an empty text input field
                retValue.append("<input type=\"text\" class=\"maxwidth\" ");
                retValue.append("name=\""+PREFIX_VALUE+propName+"\""+disabled+"></td>\n");
                retValue.append("\t<td class=\"textcenter\">&nbsp;");
            }
            retValue.append(buildTableRowEnd());
        }
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
            
            // get all needed params for the page type
            String paramValue = request.getParameter(PREFIX_VALUE + PROP_PAGETYPE);
            String oldPos = request.getParameter(PREFIX_HIDDEN + PROP_PAGETYPE);
            String oldValue = "";
            if (oldPos != null && !"".equals(oldPos)) {
                int position = Integer.parseInt(oldPos);
                if (position > 0) {
                    oldValue = PAGE_TYPES[position - 1];
                }
            }
            // write the page type property
            writeProperty(PROP_PAGETYPE, paramValue, oldValue, activeProperties);
            
            // get params for showdocument ans write property
            paramValue = request.getParameter(PREFIX_VALUE + PROP_SHOWDOCUMENT);
            oldValue = request.getParameter(PREFIX_HIDDEN + PROP_SHOWDOCUMENT);
            writeProperty(PROP_SHOWDOCUMENT, paramValue, oldValue, activeProperties);         
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
