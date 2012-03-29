/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.commons;

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.I_CmsDialogHandler;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Provides methods for the customized property dialog.<p> 
 * 
 * This is a special dialog that is used for the different resource types in the workplace.<p>
 * For the xmlpage resource type, this class is extended in the editor subpackage.<p>
 * 
 * The following files use this class:
 * <ul>
 * <li>/commons/property_custom.jsp
 * </ul>
 * <p>
 * 
 * @since 6.0.0 
 */
public class CmsPropertyCustom extends CmsPropertyAdvanced {

    /** Value for the action: edit the properties. */
    public static final int ACTION_EDIT = 500;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPropertyCustom.class);

    /** Helper object holding the information about the customized properties. */
    private CmsExplorerTypeSettings m_explorerTypeSettings;

    /** Flag to determine if navigation properties are shown. */
    private boolean m_showNavigation;

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
        getJsp().getRequest().setAttribute(SESSION_WORKPLACE_CLASS, this);
        try {
            // save the changes only if resource is properly locked
            if (isEditable()) {
                performEditOperation(request);
            }
        } catch (Throwable e) {
            // Cms error defining property, show error dialog
            includeErrorpage(this, e);
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
        boolean editable = isEditable();

        // create the column heads
        result.append("<table border=\"0\" style=\"width:100%\">\n");
        result.append("<tr>\n");
        result.append("\t<td class=\"textbold\" nowrap>");
        result.append(key(Messages.GUI_PROPERTY_0));
        result.append("</td>\n");
        result.append("\t<td class=\"textbold\">");
        result.append(key(Messages.GUI_PROPERTY_VALUE_0));
        result.append("</td>\n");
        // empty column for the checkboxes 
        result.append("\t<td class=\"textbold\" style=\"white-space: nowrap;\">");
        result.append("&nbsp;");
        result.append("</td>\n");
        result.append("</tr>\n");
        result.append("<tr><td><span style=\"height: 6px;\"></span></td></tr>\n");

        // create the text property input rows from explorer type settings
        result.append(buildTextInput(editable));

        // show navigation properties if enabled in explorer type settings
        if (showNavigation()) {
            result.append(buildNavigationProperties(editable));
        }
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
        Iterator i = getExplorerTypeSettings().getProperties().iterator();
        // iterate over the customized properties
        while (i.hasNext()) {
            String curProperty = (String)i.next();
            if (getActiveProperties().containsKey(curProperty)) {
                CmsProperty property = (CmsProperty)getActiveProperties().get(curProperty);
                String propValue = property.getValue();
                if (propValue != null) {
                    propValue = propValue.trim();
                    propValue = CmsStringUtil.escapeJavaScript(propValue);
                    // create the JS output for a single property
                    result.append("\tdocument.getElementById(\"");
                    result.append(PREFIX_VALUE);
                    result.append(curProperty);
                    result.append("\").value = \"");
                    result.append(propValue);
                    result.append("\";\n");
                }
            }
        }
        // check if the navigation text property value has to be added
        if (showNavigation() && getActiveProperties().containsKey(CmsPropertyDefinition.PROPERTY_NAVTEXT)) {
            CmsProperty property = (CmsProperty)getActiveProperties().get(CmsPropertyDefinition.PROPERTY_NAVTEXT);
            String propValue = property.getValue();
            if (propValue != null) {
                propValue = propValue.trim();
                propValue = CmsStringUtil.escapeJavaScript(propValue);
                // create the JS output for a single property
                result.append("\tdocument.getElementById(\"");
                result.append(PREFIX_VALUE);
                result.append(CmsPropertyDefinition.PROPERTY_NAVTEXT);
                result.append("\").value = \"");
                result.append(propValue);
                result.append("\";\n");
            }
        }
        return result.toString();
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
            if ((getParamDialogmode() != null) && getParamDialogmode().startsWith(MODE_WIZARD)) {
                // in wizard mode, display finish button instead of ok button
                okButton = BUTTON_FINISH;
            }
            // hide "advanced" button 
            if (isHideButtonAdvanced()) {
                return dialogButtons(new int[] {okButton, BUTTON_CANCEL}, new String[] {okAttributes, cancelAttributes});
            }
            // show "advanced" button
            return dialogButtons(new int[] {okButton, BUTTON_CANCEL, BUTTON_ADVANCED}, new String[] {
                okAttributes,
                cancelAttributes,
                advancedAttributes});
        } else {
            // hide "advanced" button 
            if (isHideButtonAdvanced()) {
                return dialogButtons(new int[] {BUTTON_CLOSE}, new String[] {cancelAttributes});
            }
            // show "advanced" button
            return dialogButtons(new int[] {BUTTON_CLOSE, BUTTON_ADVANCED}, new String[] {
                cancelAttributes,
                advancedAttributes});
        }
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

    /**
     * Sets if navigation properties are shown.<p>
     * 
     * @param showNav true, if navigation properties are shown, otherwise false
     */
    public void setShowNavigation(boolean showNav) {

        m_showNavigation = showNav;
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
     * Builds the HTML code for the special properties of an xmlpage resource.<p>
     * 
     * @param editable indicates if the properties are editable
     * @return the HTML code for the special properties of a file resource
     */
    protected StringBuffer buildNavigationProperties(boolean editable) {

        StringBuffer result = new StringBuffer(1024);

        // create "disabled" attribute if properties are not editable
        String disabled = "";
        if (!editable) {
            disabled = " disabled=\"disabled\"";
        }

        // create "add to navigation" checkbox
        result.append(buildTableRowStart(key(Messages.GUI_PROPERTY_ADD_TO_NAV_0)));
        result.append("<input type=\"checkbox\" name=\"enablenav\" id=\"enablenav\" value=\"true\" onClick=\"toggleNav();\"");
        if (getActiveProperties().containsKey(CmsPropertyDefinition.PROPERTY_NAVTEXT)
            || getActiveProperties().containsKey(CmsPropertyDefinition.PROPERTY_NAVPOS)) {
            result.append(" checked=\"checked\"");
        }
        result.append(disabled);
        result.append(">");
        result.append("</td>\n");
        result.append("\t<td class=\"textcenter\">");
        result.append("&nbsp;");
        result.append(buildTableRowEnd());

        // create NavText input row
        result.append(buildPropertyEntry(
            CmsPropertyDefinition.PROPERTY_NAVTEXT,
            key(Messages.GUI_LABEL_NAVTEXT_0),
            editable));

        // create NavPos select box row
        result.append(buildTableRowStart(key(Messages.GUI_CHNAV_INSERT_AFTER_0)));
        result.append(CmsChnav.buildNavPosSelector(getCms(), getParamResource(), disabled
            + " id=\"navpos\" class=\"maxwidth noborder\"", getMessages()));
        // get the old NavPos value and store it in hidden field
        String navPos = null;
        try {
            navPos = getCms().readPropertyObject(getParamResource(), CmsPropertyDefinition.PROPERTY_NAVPOS, false).getValue();
        } catch (CmsException e) {
            // should usually never happen
            if (LOG.isInfoEnabled()) {
                LOG.info(e.getLocalizedMessage());
            }
        }
        if (navPos == null) {
            navPos = "";
        }
        result.append("<input type=\"hidden\" name=\"");
        result.append(PREFIX_HIDDEN);
        result.append(CmsPropertyDefinition.PROPERTY_NAVPOS);
        result.append("\" value=\"");
        result.append(navPos);
        result.append("\">");
        result.append("</td>\n");
        result.append("\t<td class=\"textcenter\">");
        result.append("&nbsp;");
        result.append(buildTableRowEnd());

        return result;
    }

    /**
     * Builds the html for a single text input property row.<p>
     * 
     * The html does not include the value of the created property,
     * the values are set delayed (see buildSetFormValues() for details).<p>
     * 
     * @param propertyName the name of the property
     * @param propertyTitle the nice name of the property
     * @param editable indicates if the properties are editable
     * @return the html for a single text input property row
     */
    protected StringBuffer buildPropertyEntry(String propertyName, String propertyTitle, boolean editable) {

        StringBuffer result = new StringBuffer(256);
        // create "disabled" attribute if properties are not editable
        String disabled = "";
        if (!editable) {
            disabled = " disabled=\"disabled\"";
        }
        result.append(buildTableRowStart(propertyTitle));
        // the property is used, so create text field with checkbox and hidden field
        CmsProperty currentProperty = (CmsProperty)getActiveProperties().get(propertyName);
        String propValue = "";
        if (currentProperty != null) {
            propValue = currentProperty.getValue();
        }
        if (propValue != null) {
            propValue = propValue.trim();
        }
        propValue = CmsEncoder.escapeXml(propValue);

        // create text input field
        result.append("<input type=\"text\" class=\"maxwidth\"");
        result.append(" name=\"");
        result.append(PREFIX_VALUE);
        result.append(propertyName);
        result.append("\" id=\"");
        result.append(PREFIX_VALUE);
        result.append(propertyName);
        result.append("\"");
        if (editable) {
            result.append(" onKeyup=\"checkValue('");
            result.append(propertyName);
            result.append("');\"");
        }
        result.append(disabled);
        result.append(">");

        // create hidden field for value
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
        result.append("\t<td class=\"propertydialog-checkboxcell\">");

        // create activate/deactivate checkbox
        result.append("<input type=\"checkbox\" name=\"");
        result.append(PREFIX_USEPROPERTY);
        result.append(propertyName);
        result.append("\" id=\"");
        result.append(PREFIX_USEPROPERTY);
        result.append(propertyName);
        result.append("\" value=\"true\"");
        if (CmsStringUtil.isNotEmpty(propValue)) {
            result.append(" checked=\"checked\"");
        }
        if (editable) {
            result.append(" onClick=\"toggleDelete('");
            result.append(propertyName);
            result.append("');\"");
        }
        result.append(disabled + ">");

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
        result.append("\t<td style=\"white-space: nowrap;\" unselectable=\"on\">");
        result.append(propertyName);
        result.append("</td>\n");
        result.append("\t<td class=\"maxwidth\">");
        return result;
    }

    /**
     * Builds the HTML for the common text input property values stored in the String array "PROPERTIES".<p>
     * 
     * @param editable indicates if the properties are editable
     * @return the HTML code for the common text input fields
     */
    protected StringBuffer buildTextInput(boolean editable) {

        StringBuffer result = new StringBuffer(256);
        Iterator i = getExplorerTypeSettings().getProperties().iterator();
        // iterate over the properties
        while (i.hasNext()) {
            String curProperty = (String)i.next();
            result.append(buildPropertyEntry(curProperty, curProperty, editable));
        }
        return result;
    }

    /**
     * Initializes the explorer type settings for the current resource type.<p>
     */
    protected void initExplorerTypeSettings() {

        try {
            CmsResource res = getCms().readResource(getParamResource(), CmsResourceFilter.ALL);
            if (res.isFolder()) {
                if (!getParamResource().endsWith("/")) {
                    // append folder separator to resource name
                    setParamResource(getParamResource() + "/");
                }
            }
            String resTypeName = OpenCms.getResourceManager().getResourceType(res.getTypeId()).getTypeName();
            // get settings for resource type
            setExplorerTypeSettings(getSettingsForType(resTypeName));
            setShowNavigation(getExplorerTypeSettings().isShowNavigation());
        } catch (Throwable e) {
            // error reading file, show error dialog
            try {
                includeErrorpage(this, e);
            } catch (JspException exc) {
                LOG.error(Messages.get().getBundle().key(
                    Messages.LOG_ERROR_INCLUDE_FAILED_1,
                    FILE_DIALOG_SCREEN_ERRORPAGE));
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
                sendForward(CmsPropertyAdvanced.URI_PROPERTY_DIALOG, paramsAsParameterMap());
                return;
            } catch (Exception e) {
                // should usually never happen
                if (LOG.isInfoEnabled()) {
                    LOG.info(e.getLocalizedMessage());
                }
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
            if (CmsResource.isTemporaryFileName(resName)) {
                resName = resName.substring(1);
            }
            setParamTitle(key(Messages.GUI_PROPERTIES_1, new Object[] {resName}));
        }
    }

    /**
     * Checks the optional parameters of the handler configuration. <p> 
     * 
     * Decides if the "advanced" button should be shown or not. 
     * The "advanced" button is shown if no parameters are given (default) 
     * or the "hideadvanced" attribute is set to false. The "advanced" button
     * is hidden only, if "hideadvanced" is true and the user is not a member 
     * of the specified user groups.  
     * 
     * @return  false if the "advanced" button is shown (default) <br>
     *          true if the "advanced" button is hidden
     */
    protected boolean isHideButtonAdvanced() {

        I_CmsDialogHandler handler = OpenCms.getWorkplaceManager().getDialogHandler(getDialogHandler());
        CmsParameterConfiguration handlerParams = handler.getConfiguration();
        if ((handlerParams != null) && handlerParams.containsKey(PARAM_HIDEADVANCED)) {
            // checks if "hideadvanced" is set to true
            boolean isHideAdvancedSet = false;
            List<String> hAdvanced = handlerParams.getList(PARAM_HIDEADVANCED);
            if (!hAdvanced.isEmpty()) {
                isHideAdvancedSet = Boolean.valueOf(hAdvanced.get(0)).booleanValue();
            }
            if (isHideAdvancedSet) {
                // if user has the role root admin
                if (OpenCms.getRoleManager().hasRole(getCms(), CmsRole.ROOT_ADMIN)) {
                    return false;
                }
                if (handlerParams.containsKey(PARAM_SHOWGROUP)) {
                    // check if user is one of the configured groups
                    CmsUser currentUser = getCms().getRequestContext().getCurrentUser();
                    List<String> confGroups = handlerParams.getList(PARAM_SHOWGROUP);
                    for (String groupName : confGroups) {
                        try {
                            if (getCms().userInGroup(currentUser.getName(), groupName)) {
                                return false;
                            }
                        } catch (CmsException e) {
                            LOG.error(e.getLocalizedMessage(), e);
                        }
                    }
                }
                return true;
            }
        }
        return false;
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
            // write the common properties defined in the explorer type settings
            Iterator i = getExplorerTypeSettings().getProperties().iterator();
            // iterate over the properties
            while (i.hasNext()) {
                String curProperty = (String)i.next();
                String paramValue = request.getParameter(PREFIX_VALUE + curProperty);
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
                    if (!"-1".equals(paramValue) && !String.valueOf(Float.MAX_VALUE).equals(paramValue)) {
                        // update the property only when it is different from "-1" (meaning no change)
                        oldValue = request.getParameter(PREFIX_HIDDEN + CmsPropertyDefinition.PROPERTY_NAVPOS);
                        writeProperty(CmsPropertyDefinition.PROPERTY_NAVPOS, paramValue, oldValue);
                    }
                    paramValue = request.getParameter(PREFIX_VALUE + CmsPropertyDefinition.PROPERTY_NAVTEXT);
                    oldValue = request.getParameter(PREFIX_HIDDEN + CmsPropertyDefinition.PROPERTY_NAVTEXT);
                    writeProperty(CmsPropertyDefinition.PROPERTY_NAVTEXT, paramValue, oldValue);
                } else {
                    // navigation disabled, delete property values
                    writeProperty(CmsPropertyDefinition.PROPERTY_NAVPOS, null, null);
                    writeProperty(CmsPropertyDefinition.PROPERTY_NAVTEXT, null, null);
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
     * @throws CmsException if something goes wrong
     */
    protected void writeProperty(String propName, String propValue, String oldValue) throws CmsException {

        // get the current property object
        CmsProperty currentProperty = (CmsProperty)getActiveProperties().get(propName);
        if (currentProperty == null) {
            // new property, create new property object
            currentProperty = new CmsProperty();
            currentProperty.setName(propName);
        } else {
            // clone the property, because the original property is frozen
            currentProperty = currentProperty.cloneAsProperty();
        }

        // check if there is a parameter value for the current property
        boolean emptyParam = true;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(propValue)) {
            emptyParam = false;
        }

        if (emptyParam) {
            // parameter is empty, check if the property has to be deleted
            if (getActiveProperties().containsKey(propName)) {
                // lock resource if autolock is enabled
                checkLock(getParamResource());
                // determine the value to delete
                if (currentProperty.getStructureValue() != null) {
                    currentProperty.setStructureValue(CmsProperty.DELETE_VALUE);
                    currentProperty.setResourceValue(null);
                } else {
                    currentProperty.setResourceValue(CmsProperty.DELETE_VALUE);
                    currentProperty.setStructureValue(null);
                }
                // write the updated property object
                getCms().writePropertyObject(getParamResource(), currentProperty);
            }
        } else {
            // parameter is not empty, check if the value has changed
            if (!propValue.equals(oldValue)) {
                // lock resource if autolock is enabled
                checkLock(getParamResource());
                if ((currentProperty.getStructureValue() == null) && (currentProperty.getResourceValue() == null)) {
                    // new property, determine setting from OpenCms workplace configuration
                    if (OpenCms.getWorkplaceManager().isDefaultPropertiesOnStructure()) {
                        currentProperty.setStructureValue(propValue);
                        currentProperty.setResourceValue(null);
                    } else {
                        currentProperty.setResourceValue(propValue);
                        currentProperty.setStructureValue(null);
                    }
                } else if (currentProperty.getStructureValue() != null) {
                    // structure value has to be updated
                    currentProperty.setStructureValue(propValue);
                    currentProperty.setResourceValue(null);
                } else {
                    // resource value has to be updated
                    currentProperty.setResourceValue(propValue);
                    currentProperty.setStructureValue(null);
                }
                // set auto-creation of the property to true
                currentProperty.setAutoCreatePropertyDefinition(true);
                // write the updated property object
                getCms().writePropertyObject(getParamResource(), currentProperty);
            }
        }
    }
}