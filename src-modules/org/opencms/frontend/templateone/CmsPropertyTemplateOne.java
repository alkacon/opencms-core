/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/CmsPropertyTemplateOne.java,v $
 * Date   : $Date: 2004/10/28 15:37:49 $
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
package org.opencms.frontend.templateone;

import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialogSelector;
import org.opencms.workplace.I_CmsDialogHandler;
import org.opencms.workplace.commons.CmsPropertyCustom;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * This Property Dialog is shown specially by xmlpages for OpenCms template one,
 * and for any folders except system folders.<p>
 * 
 * @author Armen Markarian (a.markarian@alkacon.com)
 * @version $Revision: 1.1 $
 */
public class CmsPropertyTemplateOne extends CmsPropertyCustom implements I_CmsDialogHandler {
    
    /** Prefix for the localized keys of the dialog. */
    private static final String C_KEY_PREFIX = "templateonedialog.";
    
    /** 
     * String Array with default properties.<p>
     *  
     * Looping this to create form fields or get http request data and set the properties
     */    
    private static final String[] C_DEFAULT_PROPERTIES = {
        
        I_CmsConstants.C_PROPERTY_TITLE,
        I_CmsConstants.C_PROPERTY_DESCRIPTION
    };
    
    /** 
     * String Array with ebk properties.<p>
     *  
     * Looping this to get http request data and set the properties
     */  
    private static final String[] C_EBK_PROPERTIES = {
        
        CmsTemplateBean.C_PROPERTY_SHOWHEADIMAGE,
        CmsTemplateBean.C_PROPERTY_HEAD_IMGURI,
        CmsTemplateBean.C_PROPERTY_HEAD_IMGLINK,
        CmsTemplateBean.C_PROPERTY_SHOW_HEADNAV,
        CmsTemplateBean.C_PROPERTY_SHOW_NAVLEFT,
        CmsTemplateBean.C_PROPERTY_NAVLEFT_ELEMENTURI,
        CmsTemplateBean.C_PROPERTY_SIDE_URI
    };
    
    /** mode used for switching between different radio types. */
    private static final String C_ENABLE = "enable";
    
    /** mode used for switching between different radio types. */
    private static final String C_INDIVIDUAL = "individual";
    
    /** The module path. */
    private static final String C_MODULE_PATH = "/system/modules/org.opencms.frontend.templateone/";
    
    /** The default parameter value. */
    private static final String C_PARAM_DEFAULT = "";
    
    /** The false parameter value. */
    private static final String C_PARAM_FALSE = "false";
    
    /** The true parameter value. */
    private static final String C_PARAM_TRUE = "true";

    /** The path of the template one main template. */
    private static final String C_TEMPLATE_ONE = "/system/modules/org.opencms.frontend.templateone/templates/main";
    
    /**
     * Default constructor needed for dialog handler implementation.<p>
     * 
     * Do not use this constructor on JSP pages.<p>
     */
    public CmsPropertyTemplateOne() {        
        super(null);
    }
    
    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsPropertyTemplateOne(CmsJspActionElement jsp) {
        super(jsp);        
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsPropertyTemplateOne(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        super(new CmsJspActionElement(context, req, res));        
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
        
        StringBuffer result = new StringBuffer();
        
        // check if the properties are editable
        boolean editable =  isEditable();
                
        // create the column heads
        result.append("<table border=\"0\">\n");
        result.append("<tr>\n");
        result.append("\t<td class=\"textbold\">");
        result.append(key("input.property"));
        result.append("</td>\n");
        result.append("\t<td class=\"textbold maxwidth\">");
        result.append(key("label.value"));
        result.append("</td>\n");   
        result.append("\t<td class=\"textbold\" style=\"white-space: nowrap;\">");
        result.append(key("input.usedproperty"));
        result.append("</td>\n");    
        result.append("</tr>\n");
        result.append("<tr><td colspan=\"3\"><span style=\"height: 6px;\"></span></td></tr>\n");
        
        // create the text property input rows from m_defaultProperties
        for (int i=0; i<C_DEFAULT_PROPERTIES.length; i++) {
            result.append(buildPropertyEntry(C_DEFAULT_PROPERTIES[i], key(C_KEY_PREFIX + C_DEFAULT_PROPERTIES[i]), editable));    
        }
        
        // show navigation properties if enabled
        if (showNavigation()) {
            result.append(buildNavigationProperties(editable));
        }
        
        // build head image radio buttons        
        result.append(buildRadioButtons(CmsTemplateBean.C_PROPERTY_SHOWHEADIMAGE, C_INDIVIDUAL, "toggleHeadImageProperties", editable));        
        
        // build image uri search input 
        result.append(buildPropertySearchEntry(CmsTemplateBean.C_PROPERTY_HEAD_IMGURI, C_KEY_PREFIX + CmsTemplateBean.C_PROPERTY_HEAD_IMGURI, editable));
        // build image link search input 
        result.append(buildPropertySearchEntry(CmsTemplateBean.C_PROPERTY_HEAD_IMGLINK, C_KEY_PREFIX + CmsTemplateBean.C_PROPERTY_HEAD_IMGLINK, editable));
        
        // build head navigation radio buttons   
        result.append(buildRadioButtons(CmsTemplateBean.C_PROPERTY_SHOW_HEADNAV, C_ENABLE, null, editable));        
        
        // build navigation tree radio buttons   
        result.append(buildRadioButtons(CmsTemplateBean.C_PROPERTY_SHOW_NAVLEFT, C_ENABLE, null, editable));        
        
        // build navleft element search input 
        result.append(buildPropertySearchEntry(CmsTemplateBean.C_PROPERTY_NAVLEFT_ELEMENTURI, C_KEY_PREFIX + CmsTemplateBean.C_PROPERTY_NAVLEFT_ELEMENTURI, editable));
        // build side uri search input 
        result.append(buildPropertySearchEntry(CmsTemplateBean.C_PROPERTY_SIDE_URI, C_KEY_PREFIX + CmsTemplateBean.C_PROPERTY_SIDE_URI, editable));                        
        result.append("</table>");      
       
        return result.toString();
    }
    
    /**
     * Builds the JavaScript to set the property form values delayed.<p>
     * 
     * The values of the properties are not inserted directly in the &lt;input&gt; tag,
     * because there is a display issue when the property values are very long.
     * This method creates JavaScript to set the property input field values delayed.
     * On the JSP, the code which is created from this method has to be executed delayed after 
     * the creation of the html form, e.g. in the &lt;body&gt; tag with the attribute
     * onload="window.setTimeout('doSet()',50);".<p>
     * 
     * @return the JavaScript to set the property form values delayed
     */
    public String buildSetFormValues() {
        StringBuffer result = new StringBuffer(1024);
        // loop over the default properties
        for (int i=0; i<C_DEFAULT_PROPERTIES.length; i++) {
            String curProperty = C_DEFAULT_PROPERTIES[i];
            // determine the shown value
            String shownValue = "";
            try {
                shownValue = getCms().readPropertyObject(getParamResource(), curProperty, false).getValue();
            } catch (CmsException e) {
                e.printStackTrace();
            }
            if (!CmsStringUtil.isEmpty(shownValue)) {
                // create the JS output for a single property if not empty
                result.append("\tdocument.getElementById(\"");    
                result.append(PREFIX_VALUE);
                result.append(curProperty);
                result.append("\").value = \"");               
                result.append(CmsStringUtil.escapeJavaScript(shownValue));
                result.append("\";\n");
            }
        }
        
        return result.toString();
    }
    
    /**
     * Returns the property value by searching all parent folders.<p>
     *  
     * @param propertydef the property definition
     * 
     * @return the property value by searching all parent folders 
     */
    public String getDefault(String propertydef) {        
        
        try {
            String parentFolder = CmsResource.getParentFolder(getParamResource());
            CmsProperty property = getCms().readPropertyObject(parentFolder, propertydef, true);
            String propertyValue = property.getValue();            
            if (!CmsStringUtil.isEmpty(propertyValue)) {
                return property.getValue();
            }
        } catch (CmsException e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error(e);
            }
        }        
        
        return "";
    }   
    
    /**
     * @see org.opencms.workplace.I_CmsDialogHandler#getDialogHandler()
     */
    public String getDialogHandler() {
        
        return CmsDialogSelector.DIALOG_PROPERTY;
    }
    
    /**
     * @see org.opencms.workplace.I_CmsDialogHandler#getDialogUri(java.lang.String, CmsJspActionElement)
     */
    public String getDialogUri(String resource, CmsJspActionElement jsp) {
        try {
            CmsResource res = jsp.getCmsObject().readResource(resource, CmsResourceFilter.ALL);
            if (res.getTypeId() == CmsResourceTypeXmlPage.C_RESOURCE_TYPE_ID) {
                if (C_TEMPLATE_ONE.equals(jsp.property("template", resource))) {
                    // display special property dialog for xmlpage types with "template one" as template
                    return C_MODULE_PATH + "dialogs/property.jsp";
                }
                
                return C_PATH_WORKPLACE + "editors/dialogs/property.jsp";
            }
            if (res.isFolder()) {
                if (!res.getRootPath().startsWith(I_CmsConstants.VFS_FOLDER_SYSTEM)) {
                    // display special property dialog for folders. excluse system folders
                    return C_MODULE_PATH + "dialogs/property.jsp";
                }
                
                return C_PATH_WORKPLACE + "editors/dialogs/property.jsp";
            }
            String resTypeName = OpenCms.getResourceManager().getResourceType(res.getTypeId()).getTypeName();
            CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(resTypeName);
            if (settings.isPropertiesEnabled()) {
                // special properties for this type enabled, display customized dialog
                return URI_PROPERTY_CUSTOM_DIALOG;
            }
        } catch (CmsException e) {
            // should usually never happen
            if (OpenCms.getLog(this).isInfoEnabled()) {
                OpenCms.getLog(this).info(e);
            }
        }
        return URI_PROPERTY_DIALOG;
    }
        
    /**
     * Performs the editing of the resources properties.<p>
     * 
     * @param request the HttpServletRequest
     * @return true, if the properties were successfully changed, otherwise false
     * @throws CmsException if editing is not successful
     */
    protected boolean performEditOperation(HttpServletRequest request) throws CmsException {
        boolean useTempfileProject = Boolean.valueOf(getParamUsetempfileproject()).booleanValue();
        try {
            if (useTempfileProject) {
                switchToTempProject();
            }
            // loop over the default properties
            for (int i=0; i<C_DEFAULT_PROPERTIES.length; i++) {
                String curProperty = C_DEFAULT_PROPERTIES[i];
                String paramValue = CmsEncoder.decode(request.getParameter(PREFIX_VALUE + curProperty));
                String oldValue = request.getParameter(PREFIX_HIDDEN + curProperty);
                writeProperty(curProperty, paramValue, oldValue);
            }
            
            // loop over the ebk properties
            for (int i=0; i<C_EBK_PROPERTIES.length; i++) {
                String curProperty = C_EBK_PROPERTIES[i];
                String paramValue = CmsEncoder.decode(request.getParameter(PREFIX_VALUE + curProperty));
                String oldValue = request.getParameter(PREFIX_HIDDEN + curProperty);                
                writeProperty(curProperty, paramValue, oldValue);
            }
            
            // write the navigation properties if enabled
            if (showNavigation()) {
                // get the navigation enabled parameter
                String paramValue = request.getParameter("enablenav");
                String oldValue = null;
                if (Boolean.valueOf(paramValue).booleanValue()) {
                    // navigation enabled, update params
                    paramValue = request.getParameter("navpos");
                    if (!"-1".equals(paramValue)) {
                        // update the property only when it is different from "-1" (meaning no change)
                        oldValue = request.getParameter(PREFIX_HIDDEN + I_CmsConstants.C_PROPERTY_NAVPOS);
                        writeProperty(I_CmsConstants.C_PROPERTY_NAVPOS, paramValue, oldValue);
                    }
                    paramValue = request.getParameter(PREFIX_VALUE + I_CmsConstants.C_PROPERTY_NAVTEXT);
                    oldValue = request.getParameter(PREFIX_HIDDEN + I_CmsConstants.C_PROPERTY_NAVTEXT);
                    writeProperty(I_CmsConstants.C_PROPERTY_NAVTEXT, paramValue, oldValue);
                } else {
                    // navigation disabled, delete property values
                    writeProperty(I_CmsConstants.C_PROPERTY_NAVPOS, null, null);
                    writeProperty(I_CmsConstants.C_PROPERTY_NAVTEXT, null, null);
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
     * Builds the html for a single radio input property.<p>
     * 
     * @param propertyName the name of the property
     * @param propertyValue the value of the radio
     * @param propertyText the nice name of the property
     * @param JSToggleFunction the javascript toggle function or null
     * @param editable indicates if the properties are editable
     * 
     * @return the html for a single radio input property
     */
    private StringBuffer buildPropertyRadioEntry(String propertyName, String propertyValue, String propertyText, String JSToggleFunction, boolean editable) {
        
        String disabled = "";
        if (!editable) {
            disabled = " disabled=\"disabled\"";
        }
        StringBuffer result = new StringBuffer(256);
        // create "disabled" attribute if properties are not editable
        // to do
        String checked = "";        
        if (getActiveProperties().containsKey(propertyName)) {
            // the property is used, so create text field with checkbox and hidden field
            CmsProperty currentProperty = (CmsProperty)getActiveProperties().get(propertyName);
            
            String propValue = currentProperty.getValue();
            if (propValue != null) {
                propValue = propValue.trim();   
            }
            propValue = CmsEncoder.escapeXml(propValue);
            if (propertyValue.equals(propValue)) {
                checked = " checked=\"checked\"";
            }            
        } else {
            // check radio if param value is the default
            if (propertyValue.equals(C_PARAM_DEFAULT)) {
                checked = " checked=\"checked\"";
            }
        }
        // javascript onclick event 
        String onclick = "";
        if (JSToggleFunction!=null) {
            onclick = "onclick=\""+JSToggleFunction+"();\" "; 
        }
        result.append("<input ");
        result.append(onclick);
        result.append("type=\"radio\" ");
        result.append("name=\"");
        result.append(PREFIX_VALUE);
        result.append(propertyName);
        result.append("\" value=\"");
        result.append(propertyValue);
        result.append("\"");
        result.append(checked);
        result.append(disabled);
        result.append(">");
        result.append("&nbsp;");
        result.append(propertyText);    
        
        return result;
    }    
    
    
    /**
     * Builds the html for a single search text input property row.<p>
     * 
     * @param propertyName the name of the property
     * @param propertyTitle the nice name of the property
     * 
     * @return the html for a single text input property row
     */
    private StringBuffer buildPropertySearchEntry(String propertyName, String propertyTitle, boolean editable) {
        
        StringBuffer result = new StringBuffer(256);
        result.append(buildTableRowStart(key(propertyTitle)));
        String disabled = "";
        if (!editable) {
            disabled = " disabled=\"disabled\"";
        }
        String propValue = "";
        // the property is used, so create text field with checkbox and hidden field
        CmsProperty currentProperty = (CmsProperty)getActiveProperties().get(propertyName);
        if (currentProperty != null) {            
            propValue = currentProperty.getValue();            
            if (CmsStringUtil.isEmpty(propValue)) {
                if (CmsTemplateBean.C_PROPERTY_HEAD_IMGURI.equals(propertyName) || CmsTemplateBean.C_PROPERTY_HEAD_IMGLINK.equals(propertyName)) {
                    String tmp = getDefault(propertyName);
                    if (!CmsStringUtil.isEmpty(tmp)) {
                        propValue = tmp;
                    }
                } 
            } else {
                propValue = propValue.trim();
            }
        }
        
        propValue = CmsEncoder.escapeXml(propValue);
        result.append("<input type=\"text\" class=\"maxwidth\" ");
        result.append("name=\"");
        result.append(PREFIX_VALUE);
        result.append(propertyName);
        result.append("\" id=\"");
        result.append(PREFIX_VALUE);
        result.append(propertyName);
        result.append("\" value=\"");
        result.append(propValue);
        result.append("\"");
        result.append(disabled);
        result.append(">");
        result.append("<input type=\"hidden\" name=\"");
        result.append(PREFIX_HIDDEN);
        result.append(propertyName);
        result.append("\" id=\"");
        result.append(PREFIX_HIDDEN);
        result.append(propertyName);
        result.append("\" value=\"");
        result.append(propValue);
        result.append("\">");
        result.append("</td>\n");
        result.append("<td>");
        result.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n");
        result.append("\t<tr>\n");
        result.append("<td>&nbsp;&nbsp;</td>");
        result.append("<td><a href=\"#\" onclick=\"javascript:top.openTreeWin('copy', true, 'main', '");
        result.append(PREFIX_VALUE);
        result.append(propertyName);
        result.append("', document);\" class=\"button\" title=\"");
        result.append(key("button.search"));
        result.append("\"><img class=\"button\" src=\"");
        result.append(getSkinUri());
        result.append("/buttons/folder.gif\" border=\"0\"></a></td>");
        result.append("</tr>\n");
        result.append("</table>\n");
        result.append("</td>\n");
        result.append("</tr>");        
        
        return result;
    }
    
    /**
     * Builds the HTML for a complete Row with three radio Buttons.<p>
     * 
     * The propertyName will be translated in workplace.properties
     * 
     * Schema: 
     * Radio 1: Default (embedded)
     * Radio 2: Individual or Enable (depends on parameter mode)
     * Radio 3: Disable (embedded)
     * 
     * @param propertyName the name of the current property
     * @param mode the switch mode for the nice name
     * @param JSToggleFunction the javascript function for onclick handling
     * @param editable indicates if the properties are editable
     * 
     * @return the HTML for the row with three radio buttons
     */
    private StringBuffer buildRadioButtons (String propertyName, String mode, String JSToggleFunction, boolean editable) {
        
        StringBuffer result = new StringBuffer(256);        
        // propertyName will be translated in workplace.properties
        result.append(buildTableRowStart(key(C_KEY_PREFIX + propertyName), 2));
        result.append("\t<table border=\"0\">\n");
        result.append("\t<tr>\n");
        result.append("\t<td>\n");
        result.append(buildPropertyRadioEntry(propertyName, C_PARAM_DEFAULT, key(C_KEY_PREFIX + "radio.default"), JSToggleFunction, editable));
        result.append("</td>\n");
        result.append("\t<td>\n");
        result.append(buildPropertyRadioEntry(propertyName, C_PARAM_TRUE, key(C_KEY_PREFIX + "radio."+mode), JSToggleFunction, editable));
        result.append("</td>\n");
        result.append("\t<td>\n");
        result.append(buildPropertyRadioEntry(propertyName, C_PARAM_FALSE, key(C_KEY_PREFIX + "radio.disable"), JSToggleFunction, editable));
        result.append("</td>\n");
        result.append("</tr>\n");
        result.append("</table>\n");
        result.append(buildTableRowEnd());        
        
        return result;
    }
    
    /**
     * Builds the HTML for the start of a table row for a single property with colspan.<p>
     * 
     * Use this e.g. when the checkbox on the right side is not needed
     * 
     * @param propertyName the name of the current property
     * @param colspan the number of colspans
     * @return the HTML code for the start of a table row
     */
    private StringBuffer buildTableRowStart(String propertyName, int colspan) {
        
        StringBuffer result = new StringBuffer(96);
        result.append("<tr>\n");
        result.append("\t<td style=\"white-space: nowrap;\" unselectable=\"on\">");
        result.append(propertyName);
        result.append("</td>\n");
        result.append("\t<td class=\"maxwidth\" colspan=\"");
        result.append(String.valueOf(colspan));
        result.append("\">");
        
        return result; 
    }   
}
