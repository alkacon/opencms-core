/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/CmsPropertyAdvanced.java,v $
 * Date   : $Date: 2004/06/28 07:47:32 $
 * Version: $Revision: 1.20 $
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

import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertydefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringSubstitution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Provides methods for the properties dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/jsp/dialogs/property_advanced_html
 * </ul>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.20 $
 * 
 * @since 5.1
 */
public class CmsPropertyAdvanced extends CmsTabDialog implements I_CmsDialogHandler {
    
    /** Value for the action: save defined property. */
    public static final int ACTION_SAVE_DEFINE = 400;
    /** Value for the action: save edited properties. */
    public static final int ACTION_SAVE_EDIT = 300;
    /** Value for the action: show define property form. */
    public static final int ACTION_SHOW_DEFINE = 200;   
    /** Value for the action: show edit properties form. */
    public static final int ACTION_SHOW_EDIT = 100;
    
    /** Constant for the "Define" button in the build button method. */
    public static final int BUTTON_DEFINE = 201;
    /** Constant for the "Finish" button in the build button method. */
    public static final int BUTTON_FINISH = 202;
    
    /** Request parameter value for the action: save defined property. */
    public static final String DIALOG_SAVE_DEFINE = "savedefine";
    /** Request parameter value for the action: save edited properties. */
    public static final String DIALOG_SAVE_EDIT = "saveedit";
    /** Request parameter value for the action: show information form. */
    public static final String DIALOG_SHOW_DEFAULT = "default";
    /** Request parameter value for the action: show define property form. */
    public static final String DIALOG_SHOW_DEFINE = "define";   
    /** Request parameter value for the action: show edit properties form. */
    public static final String DIALOG_SHOW_EDIT = "edit";
    
    /** The dialog type. */
    public static final String DIALOG_TYPE = "property";
    
    /** Value for the dialog mode: new resource wizard. */
    public static final String MODE_WIZARD = "wizard";
    /** Value for the dialog mode: new resource wizard with creation of index page for new folder. */
    public static final String MODE_WIZARD_CREATEINDEX = "wizardcreateindex";
    /** Value for the dialog mode: new resource wizard with index page created in new folder. */
    public static final String MODE_WIZARD_INDEXCREATED = "wizardindexcreated";
    
    /** Key name for the resource panel. */
    public static final String PANEL_RESOURCE = "panel.properties.resource";
    /** Key name for the structure panel. */
    public static final String PANEL_STRUCTURE = "panel.properties.structure";
    
    /** Request parameter name for the new property definition. */
    public static final String PARAM_DIALOGMODE = "dialogmode";
    /** Request parameter name for the new property definition. */
    public static final String PARAM_NEWPROPERTY = "newproperty";   
    
    /** Prefix for the hidden fields. */
    public static final String PREFIX_HIDDEN = "valhidden";
    /** Prefix for the hidden resource value. */
    public static final String PREFIX_RESOURCE = "valresource";
    /** Prefix for the hidden structure value. */
    public static final String PREFIX_STRUCTURE = "valstructure";
    /** Prefix for the use property checkboxes. */
    public static final String PREFIX_USEPROPERTY = "useprop";
    /** Prefix for the input values. */
    public static final String PREFIX_VALUE = "valprop";
    
    /** The URI to the customized property dialog. */
    public static final String URI_PROPERTY_CUSTOM_DIALOG = C_PATH_DIALOGS + "property_custom.html"; 
    
    /** The URI to the standard property dialog. */
    public static final String URI_PROPERTY_DIALOG = C_PATH_DIALOGS + "property_advanced.html";
    
    /** Helper object storing the current editable state of the resource. */
    private Boolean m_isEditable;
    
    /** Helper to determine if the edited resource is a folder. */
    private boolean m_isFolder;
    
    /** Helper stores the mode this dialog is in, because it can be called from "new" wizard. */
    private String m_paramDialogMode;

    /** Request parameter members. */
    private String m_paramNewproperty;
    private String m_paramUseTempfileProject;
    
    /** Stores the values of properties in a String array. */
    private List m_propertyValues;
    
    /** Helper to determine if the user switched the tab views of the dialog. */
    private boolean m_tabSwitched;
    
    /**
     * Default constructor needed for dialog handler implementation.<p>
     * 
     * Do not use this constructor on JSP pages.<p>
     */
    public CmsPropertyAdvanced() {
        super(null);
    }
    
    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsPropertyAdvanced(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsPropertyAdvanced(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Transforms a list of CmsProperty objects with structure and resource values into a map with
     * CmsProperty object values keyed by property keys.<p>
     * 
     * @param list a list of CmsProperty objects
     * @return a map with CmsPropery object values keyed by property keys
     */
    public static Map getPropertyMap(List list) {
        Map result = null;
        String key = null;
        CmsProperty property = null;
    
        if (list == null || list.size() == 0) {
            return Collections.EMPTY_MAP;
        }
    
        result = new HashMap();
    
        // choose the fastest method to iterate the list
        if (list instanceof RandomAccess) {
            for (int i = 0, n = list.size(); i < n; i++) {
                property = (CmsProperty)list.get(i);
                key = property.getKey();
                result.put(key, property);
            }
        } else {
            Iterator i = list.iterator();
            while (i.hasNext()) {
                property = (CmsProperty)i.next();
                key = property.getKey();
                result.put(key, property);
            }
        }
    
        return result;
    }
    
    /**
     * Used to close the current JSP dialog.<p>
     * 
     * This method overwrites the close dialog method in the super class,
     * because in case a new folder is created, after this dialog a new xml page might be created.<p>
     *  
     * It tries to include the URI stored in the workplace settings.
     * This URI is determined by the frame name, which has to be set 
     * in the framename parameter.<p>
     * 
     * @throws JspException if including an element fails
     */
    public void actionCloseDialog() throws JspException {
        if (getAction() == ACTION_SAVE_EDIT && MODE_WIZARD_CREATEINDEX.equals(getParamDialogmode())) {
            // special case: a new xmlpage resource will be created in wizard mode after closing the dialog
            String newFolder = getParamResource();
            if (!newFolder.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
                newFolder += I_CmsConstants.C_FOLDER_SEPARATOR;
            }
            // set the current explorer resource to the new created folder
            getSettings().setExplorerResource(newFolder);

            String newUri = OpenCms.getWorkplaceManager().getExplorerTypeSetting(CmsResourceTypeXmlPage.C_RESOURCE_TYPE_NAME).getNewResourceUri();
            newUri += "?" + PARAM_DIALOGMODE + "=" + MODE_WIZARD_CREATEINDEX;
            try {
                // redirect to new xmlpage dialog
                sendCmsRedirect(C_PATH_DIALOGS + newUri);
                return;
            } catch (Exception e) {
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error("Error redirecting to new xmlpage dialog " + C_PATH_DIALOGS + newUri);
                }      
            }
        } else if (getAction() == ACTION_SAVE_EDIT && MODE_WIZARD.equals(getParamDialogmode())) {
            // set request attribute to reload the folder tree after creating a folder in wizard mode
            try {
                CmsResource res = getCms().readResource(getParamResource(), CmsResourceFilter.ALL);
                if (res.isFolder()) {
                    List folderList = new ArrayList(1);
                    folderList.add(CmsResource.getParentFolder(getParamResource()));
                    getJsp().getRequest().setAttribute(C_REQUEST_ATTRIBUTE_RELOADTREE, folderList);
                }
            } catch (CmsException e) {
                // ignore
            }
        } else if (MODE_WIZARD_INDEXCREATED.equals(getParamDialogmode())) {
            // set request attribute to reload the folder tree after creating an xml page in a new created folder in wizard mode
            getSettings().setExplorerResource(CmsResource.getParentFolder(CmsResource.getParentFolder(getParamResource())));
            List folderList = new ArrayList(1);
            folderList.add(CmsResource.getParentFolder(CmsResource.getParentFolder(getParamResource())));
            getJsp().getRequest().setAttribute(C_REQUEST_ATTRIBUTE_RELOADTREE, folderList);
        }
        super.actionCloseDialog();
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
            sendCmsRedirect(getJsp().getRequestContext().getUri()+"?"+paramsAsRequest());              
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
     * Deletes the current resource if the dialog is in wizard mode.<p>
     * 
     * If the dialog is not in wizard mode, the resource is not deleted.<p>
     * 
     * @throws JspException if including the error page fails
     */
    public void actionDeleteResource() throws JspException {
        if (getParamDialogmode() != null && getParamDialogmode().startsWith(MODE_WIZARD)) {
            // only delete resource if dialog mode is a wizard mode
            try {
                getCms().deleteResource(getParamResource(), I_CmsConstants.C_DELETE_OPTION_PRESERVE_SIBLINGS);
            } catch (CmsException e) {
                // error deleting the resource, show error dialog
                getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
                setParamErrorstack(e.getStackTraceAsString());
                setParamReasonSuggestion(getErrorSuggestionDefault());
                getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
            }
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
            if (isEditable()) {
                performEditOperation(request);
            }         
        } catch (CmsException e) {
            // error editing property, show error dialog
            setParamErrorstack(e.getStackTraceAsString());
            setParamReasonSuggestion(getErrorSuggestionDefault());
            getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
        } 
    }
    
    /**
     * Creates the HTML String for the active properties overview of the current resource.<p>
     * 
     * @return the HTML output String for active properties of the resource
     */
    public String buildActivePropertiesList() {
        StringBuffer retValue = new StringBuffer(256);
        List propertyDef = new ArrayList();
        try {
            // get all property definitions
            propertyDef = getCms().readAllPropertydefinitions();
        } catch (CmsException e) {
            // ignore this exception
        }
        
        Iterator j = propertyDef.iterator();
        int i=0;
        while (j.hasNext()) {
            CmsPropertydefinition curProperty = (CmsPropertydefinition)j.next();
            retValue.append(CmsEncoder.escapeXml(curProperty.getName()));
            if ((i+1) < propertyDef.size()) {
                retValue.append("<br>");            
            }
            i++;
        }
        
        return retValue.toString();
    }
    
    /**
     * Creates the HTML String for the edit properties form.<p>
     * 
     * The values of the form are set delayed, have a look at the
     * JavaDoc of buildSetFormValues() for a detailed description.<p>
     * 
     * @return the HTML output String for the edit properties form
     */
    public String buildEditForm() {
        StringBuffer result = new StringBuffer(4096);
        
        // get currently active tab
        String activeTab = getActiveTabName();
        
        // initialize "disabled" attribute for the input fields
        String disabled = "";
        if (!isEditable()) {
            disabled = " disabled=\"disabled\"";
        }
        
        // get all properties for the resource
        List properties = getPropertyValues();
        
        // check for presence of property definitions, should always be true
        if (properties.size() > 0) {
            // there are properties defined for this resource, build the form list
            result.append("<table border=\"0\">\n");
            result.append("<tr>\n");
            result.append("\t<td class=\"textbold\">"+key("input.property")+"</td>\n");
            result.append("\t<td class=\"textbold\">"+key("label.value")+"</td>\n");
            result.append("\t<td class=\"textbold\" style=\"white-space: nowrap;\">"+key("input.usedproperty")+"</td>\n");            
            result.append("</tr>\n");
            result.append("<tr><td colspan=\"3\"><span style=\"height: 6px;\"></span></td></tr>\n");

            // show all possible properties for the resource
            Iterator i = properties.iterator();
            while (i.hasNext()) {
                String[] curProp = (String[])i.next();
                // create a single property row
                result.append(buildPropertyRow(curProp[0], curProp[1], curProp[2], curProp[3], disabled, activeTab));
            }
            result.append("</table>"); 
        } else {
            // there are no properties defined for this resource, show nothing (should never happen)
            result.append("No properties defined!");
        }      
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
        // get currently active tab
        String activeTab = getActiveTabName();
        // get structure panel name
        String structurePanelName = key(PANEL_STRUCTURE);
        Iterator i = getPropertyValues().iterator();
        while (i.hasNext()) {
            String[] curProp = (String[])i.next();  
            // determine the shown value
            String shownValue = curProp[1];
            // in "shared properties" form, show resource value if no structure value is set
            if (structurePanelName.equals(activeTab) && "".equals(curProp[2]) && !"".equals(curProp[3])) {
                shownValue = curProp[3];
            }
            if (!"".equals(shownValue)) {
                // create the JS output for a single property if not empty
                result.append("\tdocument.getElementById(\"");    
                result.append(PREFIX_VALUE + curProp[0] + "\").value = \"");               
                result.append(CmsStringSubstitution.escapeJavaScript(shownValue));
                result.append("\";\n");
            }

        }
        return result.toString();
    }
    
    /**
     * Builds a button row with an "Ok", a "Cancel" and a "Define" button.<p>
     * 
     * @return the button row
     */
    public String dialogButtonsOkCancelDefine() {
        if (isEditable()) {
            int okButton = BUTTON_OK;
            if (getParamDialogmode() != null && getParamDialogmode().startsWith(MODE_WIZARD)) {
                // in wizard mode, display finish button instead of ok button
                okButton = BUTTON_FINISH;
            }
            return dialogButtons(new int[] {okButton, BUTTON_CANCEL, BUTTON_DEFINE}, new String[] {null, null, "onclick=\"definePropertyForm();\""});
        } else {
            return dialogButtons(new int[] {BUTTON_CLOSE}, new String[] {null});
            
        }
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
                // display special property dialog for xmlpage types
                return C_PATH_WORKPLACE + "editors/dialogs/property.html";
            }
            String resTypeName = OpenCms.getResourceManager().getResourceType(res.getTypeId()).getTypeName();
            CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(resTypeName);
            if (settings.isPropertiesEnabled()) {
                // special properties for this type enabled, display customized dialog
                return URI_PROPERTY_CUSTOM_DIALOG;
            }
        } catch (CmsException e) {
            // ignore this exception
        }
        return URI_PROPERTY_DIALOG;
    }
    
    /**
     * Returns the value of the dialogmode parameter, 
     * or null if this parameter was not provided.<p>
     * 
     * The dialogmode parameter stores the different modes of the property dialog,
     * e.g. for displaying other buttons in the new resource wizard.<p>
     * 
     * @return the value of the usetempfileproject parameter
     */    
    public String getParamDialogmode() {
        return m_paramDialogMode;
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
     * Returns the value of the usetempfileproject parameter, 
     * or null if this parameter was not provided.<p>
     * 
     * The usetempfileproject parameter stores if the file resides 
     * in the temp file project.<p>
     * 
     * @return the value of the usetempfileproject parameter
     */    
    public String getParamUsetempfileproject() {
        return m_paramUseTempfileProject;
    }
    
    /**
     * @see org.opencms.workplace.CmsTabDialog#getTabParameterOrder()
     */
    public List getTabParameterOrder() {
        ArrayList orderList = new ArrayList(2);
        orderList.add("tabstr");
        orderList.add("tabres");
        return orderList;
    }
    
    /**
     * @see org.opencms.workplace.CmsTabDialog#getTabs()
     */
    public List getTabs() {
        ArrayList tabList = new ArrayList(2);
        if (OpenCms.getWorkplaceManager().isEnableAdvancedPropertyTabs()) {
            // tabs are enabled, show both tabs except for folders
            if (m_isFolder) {
                // resource is a folder, show only the configured tab
                if (OpenCms.getWorkplaceManager().isDefaultPropertiesOnStructure()) {
                    tabList.add(key(PANEL_STRUCTURE));
                } else {
                    tabList.add(key(PANEL_RESOURCE));
                }
            } else {
                // resource is no folder, show both tabs
                tabList.add(key(PANEL_STRUCTURE));
                tabList.add(key(PANEL_RESOURCE));
            }
        } else {
            // tabs are disabled, show only the configured tab
            if (OpenCms.getWorkplaceManager().isDefaultPropertiesOnStructure()) {
                tabList.add(key(PANEL_STRUCTURE));
            } else {
                tabList.add(key(PANEL_RESOURCE));
            }
        }
        return tabList;
    }

    /**
     * Sets the value of the dialogmode parameter.<p>
     * 
     * @param value the value to set
     */
    public void setParamDialogmode(String value) {
        m_paramDialogMode = value;
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
     * Sets the value of the usetempfileproject parameter.<p>
     * 
     * @param value the value to set
     */
    public void setParamUsetempfileproject(String value) {
        m_paramUseTempfileProject = value;
    }
    
    /**
     * @see org.opencms.workplace.CmsDialog#dialogButtonsHtml(java.lang.StringBuffer, int, java.lang.String)
     */
    protected void dialogButtonsHtml(StringBuffer result, int button, String attribute) {
        attribute = appendDelimiter(attribute);

        switch (button) {
        case BUTTON_DEFINE :
            result.append("<input name=\"define\" type=\"button\" value=\"");
            result.append(key("button.newpropertydef"));
            result.append("\" class=\"dialogbutton\"");
            result.append(attribute);
            result.append(">\n");
            break;
        case BUTTON_FINISH :
            result.append("<input name=\"finish\" type=\"submit\" value=\"");
            result.append(key("button.endwizard"));
            result.append("\" class=\"dialogbutton\"");
            result.append(attribute);
            result.append(">\n");
            break;
        default :
            super.dialogButtonsHtml(result, button, attribute);
        }
    }
       

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
        // fill the parameter values in the get/set methods
        fillParamValues(request);
        
        // get the active tab from request parameter or display first tab
        getActiveTab();
        
        // check the resource type of the edited resource
        m_isFolder = false;
        try {
            CmsResource resource = getCms().readResource(getParamResource(), CmsResourceFilter.ALL);
            if (resource.isFolder()) {
                m_isFolder = true;
                if (!getParamResource().endsWith("/")) {
                    // append folder separator to resource name
                    setParamResource(getParamResource() + "/");
                }
            }
        } catch (CmsException e) {
            // error reading resource, log the error
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error reading file " + getParamResource());
            }
        }
        
        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        boolean isPopup = Boolean.valueOf(getParamIsPopup()).booleanValue();
        // set the action for the JSP switch 
        if (DIALOG_SHOW_DEFINE.equals(getParamAction())) {
            setAction(ACTION_SHOW_DEFINE);
            setParamTitle(key("title.newpropertydef") + ": " + CmsResource.getName(getParamResource()));
        } else if (DIALOG_SAVE_EDIT.equals(getParamAction())) {
            if (isPopup) {
                setAction(ACTION_CLOSEPOPUP_SAVE);
            } else {
                setAction(ACTION_SAVE_EDIT);
            }
        } else if (DIALOG_SAVE_DEFINE.equals(getParamAction())) {
            setAction(ACTION_SAVE_DEFINE);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            if (isPopup) {
                setAction(ACTION_CLOSEPOPUP);
            } else {
                setAction(ACTION_CANCEL);
            }
        } else { 
            // set the default action: show edit form  
            setAction(ACTION_DEFAULT);
            if (!isEditable()) {
                setParamTitle(key("title.property") + ": " + CmsResource.getName(getParamResource()));
            } else {
                setParamTitle(key("title.editpropertyinfo") + ": " + CmsResource.getName(getParamResource()));
            }
            // check if the user switched a tab
            m_tabSwitched = false;
            if (DIALOG_SWITCHTAB.equals(getParamAction())) {
                m_tabSwitched = true;
            }
        }      
    }
    
    /**
     * Returns whether the properties are editable or not depending on the lock state of the resource and the current project.<p>
     * 
     * @return true if properties are editable, otherwise false
     */
    protected boolean isEditable() {
        if (m_isEditable == null) {  
            
            if (getCms().getRequestContext().currentProject().getId() == I_CmsConstants.C_PROJECT_ONLINE_ID) {
                // we are in the online project, no editing allowed
                m_isEditable = new Boolean(false);
                
            } else {
                // we are in an offline project, check lock state
                String resourceName = getParamResource();
                CmsResource file = null;
                CmsLock lock = null;
                try {
                    file = getCms().readResource(resourceName, CmsResourceFilter.ALL);
                    // check if resource is a folder
                    if (file.isFolder()) {
                        resourceName += "/";            
                    }
                } catch (CmsException e) {
                    // ignore this exception
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
                            && lock.getUserId().equals(getCms().getRequestContext().currentUser().getId())) {
                        // lock is not shared and belongs to the current user
                        if (getCms().getRequestContext().currentProject().getId() == lock.getProjectId() 
                                || "true".equals(getParamUsetempfileproject())) {
                            // resource is locked in the current project or the tempfileproject is used
                            m_isEditable = new Boolean(true);
                            return m_isEditable.booleanValue();
                        }
                    }
                } else if (OpenCms.getWorkplaceManager().autoLockResources()) {
                    m_isEditable = new Boolean(true);
                    return m_isEditable.booleanValue();
                }
                // lock is null or belongs to other user and/or project, properties are not editable
                m_isEditable = new Boolean(false);
            }
        }
        
        return m_isEditable.booleanValue();
    }
    
    /**
     * Builds the html for a single property entry row.<p>
     * 
     * The output depends on the currently active tab (shared or individual properties)
     * and on the present values of the current property.<p>
     * 
     * The values of the property are not inserted directly in the &lt;input&gt; tag,
     * because there is a display issue when the property values are very long.
     * Have a look at buildSetFormValues() for a detailed description.<p>
     * 
     * @param propName the name of the property
     * @param propValue the displayed value of the property
     * @param valueStructure the structure value of the property
     * @param valueResource the resource value of the property
     * @param disabled contains attribute String to disable the fields
     * @param activeTab the name of the currently active dialog tab
     * @return the html for a single property entry row
     */
    private StringBuffer buildPropertyRow(String propName, String propValue, String valueStructure, String valueResource, String disabled, String activeTab) {
        StringBuffer result = new StringBuffer(256);
        String structurePanelName = key(PANEL_STRUCTURE);
        String inputAttrs = "class=\"maxwidth\"";
        if (structurePanelName.equals(activeTab)) {
            // in "shared properties" form, show resource value if no structure value is set
            if ("".equals(valueStructure) && !"".equals(valueResource)) {
                inputAttrs = "class=\"dialogmarkedfield\"";
            }
        }
        result.append("<tr>\n");
        result.append("\t<td style=\"white-space: nowrap;\">"+propName);
        result.append("</td>\n");
        result.append("\t<td class=\"maxwidth\">");     
        result.append("<input type=\"text\" ");
        result.append(inputAttrs + " name=\""+PREFIX_VALUE+propName+"\" id=\""+PREFIX_VALUE+propName+"\"");
        result.append(" onFocus=\"deleteResEntry('" + propName + "', '"+activeTab+"');\"");
        result.append(" onBlur=\"checkResEntry('" + propName + "', '"+activeTab+"');\" onKeyup=\"checkValue('"+propName+"', '"+activeTab+"');\"" + disabled + ">");
        result.append("<input type=\"hidden\" name=\""+PREFIX_STRUCTURE+propName+"\" id=\""+PREFIX_STRUCTURE+propName+"\" value=\""+CmsEncoder.escapeXml(valueStructure)+"\">");
        result.append("<input type=\"hidden\" name=\""+PREFIX_RESOURCE+propName+"\" id=\""+PREFIX_RESOURCE+propName+"\" value=\""+CmsEncoder.escapeXml(valueResource)+"\"></td>\n");
        result.append("\t<td class=\"textcenter\">");
        if (!"".equals(propValue)) {
            // show checkbox only for non empty value
            String prefix = PREFIX_RESOURCE;
            if (structurePanelName.equals(activeTab)) {
                prefix = PREFIX_STRUCTURE;
            }
            result.append("<input type=\"checkbox\" name=\""+PREFIX_USEPROPERTY+propName+"\" id=\""+PREFIX_USEPROPERTY+propName+"\" value=\"true\"" + disabled);
            result.append(" checked=\"checked\" onClick=\"toggleDelete('"+propName+"', '"+prefix+"', '"+activeTab+"');\">");
        } else {
            result.append("&nbsp;");
        }
        result.append("</td>\n");
        result.append("</tr>\n");
        return result;
    }
    
    /**
     * Creates a list of String arrays containing the property names and values.<p>
     * 
     * The list items consist of the following Strings:
     * <ol>
     * <li>The name of the property</li>
     * <li>The currently active property value</li>
     * <li>The value of the structure</li>
     * <li>The value of the resource</li>
     * </ol>
     * 
     * @return the list of property values in display order
     */
    private List getPropertyValues() {
        // check if list has to be generated
        if (m_propertyValues == null) {
            
            // get currently active tab
            String activeTab = getActiveTabName();
            String structurePanelName = key(PANEL_STRUCTURE);
            
            // get all properties for the resource
            List propertyDef = new ArrayList();
            try {
                propertyDef = getCms().readAllPropertydefinitions();
            } catch (CmsException e) {
                // ignore
            }
            m_propertyValues = new ArrayList(propertyDef.size());
    
            // get all used properties for the resource
            Map activeProperties = null;
            if (!m_tabSwitched) {
                try {
                    activeProperties = CmsPropertyAdvanced.getPropertyMap(getCms().readPropertyObjects(getParamResource(), false));
                } catch (CmsException e) {
                    activeProperties = new HashMap();
                }
            }
            
            // iterate over all possible properties for the resource
            Iterator i = propertyDef.iterator();
            while (i.hasNext()) {
                CmsPropertydefinition currentPropertyDef = (CmsPropertydefinition)i.next();
                String propName = CmsEncoder.escapeXml(currentPropertyDef.getName());
                String propValue = "";
                String valueStructure = "";
                String valueResource = "";
                if (m_tabSwitched) {
                    // switched the tab, get values from hidden fields
                    if (structurePanelName.equals(activeTab)) {
                        // structure form
                        propValue = getJsp().getRequest().getParameter(PREFIX_STRUCTURE + propName);
                        valueStructure = getJsp().getRequest().getParameter(PREFIX_STRUCTURE + propName);
                        if (!isEditable()) {
                            // values from disabled fields are not posted
                            valueResource = getJsp().getRequest().getParameter(PREFIX_RESOURCE + propName);
                        } else {
                            valueResource = getJsp().getRequest().getParameter(PREFIX_VALUE + propName);
                        }
                    } else {
                        // resource form
                        propValue = getJsp().getRequest().getParameter(PREFIX_RESOURCE + propName);
                        if (!isEditable()) {
                            // values from disabled fields are not posted
                            valueStructure = getJsp().getRequest().getParameter(PREFIX_STRUCTURE + propName);
                        } else {
                            valueStructure = getJsp().getRequest().getParameter(PREFIX_VALUE + propName);
                        }
                        valueResource = getJsp().getRequest().getParameter(PREFIX_RESOURCE + propName);
                        if (valueStructure != null && !"".equals(valueStructure.trim()) && valueStructure.equals(valueResource)) {
                            // the resource value was shown in the input field, set structure value to empty String
                            valueStructure = "";
                        }
                    }
                } else {
                    // initial call of edit form, get property values from database 
                    CmsProperty currentProperty = (CmsProperty)activeProperties.get(propName);
                    if (currentProperty == null) {
                        currentProperty = new CmsProperty();
                    }                    
                    if (structurePanelName.equals(activeTab)) {
                        // show the structure properties
                        propValue = currentProperty.getStructureValue();
                    } else {
                        // show the resource properties
                        propValue = currentProperty.getResourceValue();
                    }
                    valueStructure = currentProperty.getStructureValue();
                    valueResource = currentProperty.getResourceValue();
                    // check values for null
                    if (propValue == null) {
                        propValue = "";
                    }
                    if (valueStructure == null) {
                        valueStructure = "";
                    }
                    if (valueResource == null) {
                        valueResource = "";
                    }
                }
                // remove unnecessary blanks from values
                propValue = propValue.trim();
                valueStructure = valueStructure.trim();
                valueResource = valueResource.trim();
                String[] property = new String[] {propName,
                        propValue,
                        valueStructure,
                        valueResource};
                m_propertyValues.add(property);
            }
        }
        // return the filled list
        return m_propertyValues;
    }
    
    /**
     * Performs the definition of a new property.<p>
     * 
     * @return true, if the new property was created, otherwise false
     * @throws CmsException if creation is not successful
     */
    private boolean performDefineOperation() throws CmsException {
        boolean useTempfileProject = "true".equals(getParamUsetempfileproject());
        try {
            if (useTempfileProject) {
                switchToTempProject();
            }
            String newProperty = getParamNewproperty();
            if (newProperty != null && !"".equals(newProperty.trim())) {
                getCms().createPropertydefinition(newProperty);
                return true;
            } else {
                throw new CmsException("You entered an invalid property name", CmsException.C_BAD_NAME); 
            } 
        } finally {
            if (useTempfileProject) {
                switchToCurrentProject();
            }
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
        List propertyDef = getCms().readAllPropertydefinitions();
        boolean useTempfileProject = "true".equals(getParamUsetempfileproject());
        try {
            if (useTempfileProject) {
                switchToTempProject();
            }
            Map activeProperties = getPropertyMap(getCms().readPropertyObjects(getParamResource(), false));
            String activeTab = getActiveTabName();
            List propertiesToWrite = new ArrayList();
            
            // check all property definitions of the resource for new values
            Iterator i = propertyDef.iterator();
            while (i.hasNext()) {
                CmsPropertydefinition curPropDef = (CmsPropertydefinition)i.next();
                String propName = CmsEncoder.escapeXml(curPropDef.getName());
                String valueStructure = null;
                String valueResource = null;                
                
                if (key(PANEL_STRUCTURE).equals(activeTab)) {
                    // get parameters from the structure tab
                    valueStructure = request.getParameter(PREFIX_VALUE + propName);
                    valueResource = request.getParameter(PREFIX_RESOURCE + propName);
                    if (valueStructure != null && !"".equals(valueStructure.trim()) && valueStructure.equals(valueResource)) {
                        // the resource value was shown/entered in input field, set structure value to empty String
                        valueStructure = "";
                    }
                } else {
                    // get parameters from the resource tab
                    valueStructure = request.getParameter(PREFIX_STRUCTURE + propName);
                    valueResource = request.getParameter(PREFIX_VALUE + propName);
                }
                
                // check values for blanks and null
                if (valueStructure != null) {
                    valueStructure = valueStructure.trim();
                } 
                
                if (valueResource != null) {
                    valueResource = valueResource.trim();
                }
                
                // create new CmsProperty object to store
                CmsProperty newProperty = new CmsProperty();
                newProperty.setKey(curPropDef.getName());
                newProperty.setStructureValue(valueStructure);
                newProperty.setResourceValue(valueResource);
                
                // get the old property values
                CmsProperty oldProperty = (CmsProperty)activeProperties.get(curPropDef.getName());
                if (oldProperty == null) {
                    // property was not set, create new empty property object
                    oldProperty = new CmsProperty();
                    oldProperty.setKey(curPropDef.getName());
                }
                
                boolean writeStructureValue = false;
                boolean writeResourceValue = false;
                String oldValue = oldProperty.getStructureValue();
                String newValue = newProperty.getStructureValue();

                // write the structure value if the existing structure value is not null and we want to delete the structure value
                writeStructureValue = (oldValue != null && newProperty.deleteStructureValue());
                // or if we want to write a value which is neither the delete value or an empty value
                writeStructureValue |= !newValue.equals(oldValue) && !"".equalsIgnoreCase(newValue) && !CmsProperty.C_DELETE_VALUE.equalsIgnoreCase(newValue);
                // set the structure value explicitly to null to leave it as is in the database
                if (!writeStructureValue) {
                    newProperty.setStructureValue(null);
                }                
                
                oldValue = oldProperty.getResourceValue();
                newValue = newProperty.getResourceValue();

                // write the resource value if the existing resource value is not null and we want to delete the resource value
                writeResourceValue = (oldValue != null && newProperty.deleteResourceValue());
                // or if we want to write a value which is neither the delete value or an empty value
                writeResourceValue |= !newValue.equals(oldValue) && !"".equalsIgnoreCase(newValue) && !CmsProperty.C_DELETE_VALUE.equalsIgnoreCase(newValue);
                // set the resource value explicitly to null to leave it as is in the database
                if (!writeResourceValue) {
                    newProperty.setResourceValue(null);
                }
                
                if (writeStructureValue || writeResourceValue) {
                    // add property to list only if property values have changed
                    propertiesToWrite.add(newProperty);                    
                }  
            }
            if (propertiesToWrite.size() > 0) {
                // lock resource if autolock is enabled
                checkLock(getParamResource());
                //write the new property values
                getCms().writePropertyObjects(getParamResource(), propertiesToWrite);
            }
        } finally {
            if (useTempfileProject) {
                switchToCurrentProject();
            }
        }
        return true;
    }
    
}
