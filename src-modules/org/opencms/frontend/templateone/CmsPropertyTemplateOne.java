/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/CmsPropertyTemplateOne.java,v $
 * Date   : $Date: 2005/10/12 12:46:31 $
 * Version: $Revision: 1.29 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.frontend.templateone;

import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.jsp.util.CmsTemplateContentListItem;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialogSelector;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.I_CmsDialogHandler;
import org.opencms.workplace.commons.CmsPropertyCustom;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * This property dialog is shown specially by files using the OpenCms template one,
 * and for any folders except system folders.<p>
 * 
 * @author Armen Markarian 
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.29 $ 
 * 
 * @since 6.0.0 
 */
public class CmsPropertyTemplateOne extends CmsPropertyCustom implements I_CmsDialogHandler {

    /** 
     * Contains all properties to set with this customized dialog.<p>
     *  
     * Loop this to get the HTTP request data and set the property values.<p>
     */
    private static final String[] ALL_PROPERTIES = {

        CmsTemplateNavigation.PROPERTY_HEADNAV_USE,
        CmsTemplateBean.PROPERTY_SHOWHEADIMAGE,
        CmsTemplateBean.PROPERTY_HEAD_IMGURI,
        CmsTemplateBean.PROPERTY_HEAD_IMGLINK,
        CmsTemplateBean.PROPERTY_HEAD_ELEMENTURI,
        CmsTemplateBean.PROPERTY_SHOW_HEADNAV,
        CmsTemplateBean.PROPERTY_SHOW_NAVLEFT,
        CmsTemplateBean.PROPERTY_NAVLEFT_ELEMENTURI,
        CmsTemplateBean.PROPERTY_SIDE_URI,
        CmsTemplateBean.PROPERTY_CONFIGPATH,
        CmsTemplateBean.PROPERTY_LAYOUT_CENTER,
        CmsTemplateBean.PROPERTY_LAYOUT_RIGHT};

    /** 
     * String Array with default properties.<p>
     *  
     * Loop this to create form fields or get HTTP request data and set the property values.<p>
     */
    private static final String[] DEFAULT_PROPERTIES = {

    CmsPropertyDefinition.PROPERTY_TITLE, CmsPropertyDefinition.PROPERTY_DESCRIPTION};

    /** Mode used for switching between different radio types. */
    private static final String ENABLE = "enable";

    /** Mode used for switching between different radio types. */
    private static final String INDIVIDUAL = "individual";

    /** Prefix for the localized keys of the dialog. */
    private static final String KEY_PREFIX = "templateonedialog.";

    /** The module path. */
    private static final String MODULE_PATH = "/system/modules/org.opencms.frontend.templateone/";

    /** The default parameter value. */
    private static final String PARAM_DEFAULT = "";

    /** The false parameter value. */
    private static final String PARAM_FALSE = "false";

    /** The true parameter value. */
    private static final String PARAM_TRUE = "true";

    /** The path of the "template one" template. */
    private static final String TEMPLATE_ONE = "/system/modules/org.opencms.frontend.templateone/templates/main";

    /** The VFS path to the global configuration files for content lists. */
    private static final String VFS_PATH_CONFIGFILES = CmsWorkplace.VFS_PATH_SYSTEM + "shared/templateone/";

    /** The VFS path to the global configuration files for center content lists. */
    private static final String VFS_PATH_CONFIGFILES_CENTER = VFS_PATH_CONFIGFILES + "center/";

    /** The VFS path to the global configuration files for right content lists. */
    private static final String VFS_PATH_CONFIGFILES_RIGHT = VFS_PATH_CONFIGFILES + "right/";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPropertyTemplateOne.class);

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
        getJsp().getRequest().setAttribute(SESSION_WORKPLACE_CLASS, this);
        try {
            // save the changes only if resource is properly locked
            if (isEditable()) {
                performEditOperation(request);
            }
        } catch (Throwable e) {
            // Cms checked error defining property, show error dialog
            includeErrorpage(this, e);
        }
    }

    /**
     * Build the html for a property checkbox.<p>
     * 
     * @param propertyName the property name
     * @param propertyValue the property value
     * @param propertyText the property text
     * 
     * @return the html for a property checkbox
     */
    public String buildCheckBox(String propertyName, String propertyValue, String propertyText) {

        StringBuffer checkbox = new StringBuffer();
        checkbox.append(buildTableRowStart(key(propertyText)));
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
            if (propertyValue.equals(PARAM_DEFAULT)) {
                checked = " checked=\"checked\"";
            }
        }
        checkbox.append("<input type=\"checkbox\" name=\"");
        checkbox.append(PREFIX_VALUE);
        checkbox.append(propertyName);
        checkbox.append("\" value=\"");
        checkbox.append(propertyValue);
        checkbox.append("\"");
        checkbox.append(checked);
        checkbox.append(">");
        checkbox.append(buildTableRowEnd());

        return checkbox.toString();
    }

    /**
     * Creates the HTML String for the edit properties form.<p>
     * 
     * @return the HTML output String for the edit properties form
     */
    public String buildEditForm() {

        StringBuffer result = new StringBuffer();

        // check if the properties are editable
        boolean editable = isEditable();

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
        for (int i = 0; i < DEFAULT_PROPERTIES.length; i++) {
            result.append(buildPropertyEntry(
                DEFAULT_PROPERTIES[i],
                key(KEY_PREFIX + DEFAULT_PROPERTIES[i]),
                editable));
        }

        // show navigation properties
        result.append(buildNavigationProperties(editable));

        // build head nav checkbox
        result.append(buildCheckBox(CmsTemplateNavigation.PROPERTY_HEADNAV_USE, PARAM_TRUE, KEY_PREFIX
            + CmsTemplateNavigation.PROPERTY_HEADNAV_USE));

        // build head image radio buttons        
        result.append(buildRadioButtons(
            CmsTemplateBean.PROPERTY_SHOWHEADIMAGE,
            INDIVIDUAL,
            "toggleHeadImageProperties",
            editable));

        // build image uri search input 
        result.append(buildPropertySearchEntry(CmsTemplateBean.PROPERTY_HEAD_IMGURI, KEY_PREFIX
            + CmsTemplateBean.PROPERTY_HEAD_IMGURI, editable));
        // build image link search input 
        result.append(buildPropertySearchEntry(CmsTemplateBean.PROPERTY_HEAD_IMGLINK, KEY_PREFIX
            + CmsTemplateBean.PROPERTY_HEAD_IMGLINK, editable));
        
        // build head element search input 
        result.append(buildPropertySearchEntry(CmsTemplateBean.PROPERTY_HEAD_ELEMENTURI, KEY_PREFIX
            + CmsTemplateBean.PROPERTY_HEAD_ELEMENTURI, editable));

        // build head navigation radio buttons   
        result.append(buildRadioButtons(CmsTemplateBean.PROPERTY_SHOW_HEADNAV, ENABLE, null, editable));

        // build navigation tree radio buttons   
        result.append(buildRadioButtons(CmsTemplateBean.PROPERTY_SHOW_NAVLEFT, ENABLE, null, editable));

        // build navleft element search input 
        result.append(buildPropertySearchEntry(CmsTemplateBean.PROPERTY_NAVLEFT_ELEMENTURI, KEY_PREFIX
            + CmsTemplateBean.PROPERTY_NAVLEFT_ELEMENTURI, editable));
        // build side uri search input 
        result.append(buildPropertySearchEntry(CmsTemplateBean.PROPERTY_SIDE_URI, KEY_PREFIX
            + CmsTemplateBean.PROPERTY_SIDE_URI, editable));

        // build center layout selector
        result.append(buildPropertySelectbox(
            CmsTemplateContentListItem.DISPLAYAREA_CENTER,
            CmsTemplateBean.PROPERTY_LAYOUT_CENTER,
            KEY_PREFIX + CmsTemplateBean.PROPERTY_LAYOUT_CENTER,
            editable));
        // build right layout selector
        result.append(buildPropertySelectbox(
            CmsTemplateContentListItem.DISPLAYAREA_RIGHT,
            CmsTemplateBean.PROPERTY_LAYOUT_RIGHT,
            KEY_PREFIX + CmsTemplateBean.PROPERTY_LAYOUT_RIGHT,
            editable));

        // build configuration path search input 
        result.append(buildPropertySearchEntry(CmsTemplateBean.PROPERTY_CONFIGPATH, KEY_PREFIX
            + CmsTemplateBean.PROPERTY_CONFIGPATH, editable));

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
        for (int i = 0; i < DEFAULT_PROPERTIES.length; i++) {
            String curProperty = DEFAULT_PROPERTIES[i];
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
            if (LOG.isErrorEnabled()) {
                LOG.error(e);
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
            String template = jsp.getCmsObject().readPropertyObject(
                res,
                CmsPropertyDefinition.PROPERTY_TEMPLATE,
                true).getValue("");
            if (!res.isFolder()
                && res.getTypeId() != CmsResourceTypeBinary.getStaticTypeId()
                && res.getTypeId() != CmsResourceTypePlain.getStaticTypeId()
                && res.getTypeId() != CmsResourceTypeImage.getStaticTypeId()) {
                // file is no plain text, binary or image type, check "template" property
                if (TEMPLATE_ONE.equals(template)) {
                    // display special property dialog for files with "template one" as template
                    return MODULE_PATH + "dialogs/property.jsp";
                } else if (res.getTypeId() == CmsResourceTypeXmlPage.getStaticTypeId()) {
                    // show xmlpage property dialog for xmlpages not using "template one" as template
                    return PATH_WORKPLACE + "editors/dialogs/property.jsp";
                }
            }
            if (res.isFolder()
                && TEMPLATE_ONE.equals(template)
                && !res.getRootPath().startsWith(CmsResource.VFS_FOLDER_SYSTEM)) {
                // display special property dialog also for folders but exclude the system folders
                return MODULE_PATH + "dialogs/property.jsp";
            }
            String resTypeName = OpenCms.getResourceManager().getResourceType(res.getTypeId()).getTypeName();
            // get settings for resource type
            CmsExplorerTypeSettings settings = getSettingsForType(resTypeName);
            if (settings.isPropertiesEnabled()) {
                // special properties for this type enabled, display customized dialog
                return URI_PROPERTY_CUSTOM_DIALOG;
            }
        } catch (CmsException e) {
            // should usually never happen
            if (LOG.isErrorEnabled()) {
                LOG.error(e);
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
            for (int i = 0; i < DEFAULT_PROPERTIES.length; i++) {
                String curProperty = DEFAULT_PROPERTIES[i];
                String paramValue = request.getParameter(PREFIX_VALUE + curProperty);
                String oldValue = request.getParameter(PREFIX_HIDDEN + curProperty);
                writeProperty(curProperty, paramValue, oldValue);
            }

            // loop over all properties
            for (int i = 0; i < ALL_PROPERTIES.length; i++) {
                String curProperty = ALL_PROPERTIES[i];
                String paramValue = request.getParameter(PREFIX_VALUE + curProperty);
                String oldValue = request.getParameter(PREFIX_HIDDEN + curProperty);
                writeProperty(curProperty, paramValue, oldValue);
            }

            // write the navigation properties

            // get the navigation enabled parameter
            String paramValue = request.getParameter("enablenav");
            String oldValue = null;
            if (Boolean.valueOf(paramValue).booleanValue()) {
                // navigation enabled, update params
                paramValue = request.getParameter("navpos");
                if (!"-1".equals(paramValue)) {
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
    private StringBuffer buildPropertyRadioEntry(
        String propertyName,
        String propertyValue,
        String propertyText,
        String JSToggleFunction,
        boolean editable) {

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
            if (propertyValue.equals(PARAM_DEFAULT)) {
                checked = " checked=\"checked\"";
            }
        }
        // javascript onclick event 
        String onclick = "";
        if (JSToggleFunction != null) {
            onclick = "onclick=\"" + JSToggleFunction + "();\" ";
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
     * @param editable indicates if the properties are editable
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
                if (CmsTemplateBean.PROPERTY_HEAD_IMGURI.equals(propertyName)
                    || CmsTemplateBean.PROPERTY_HEAD_IMGLINK.equals(propertyName)) {
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
        result.append("<input type=\"text\" style=\"width: 99%\" class=\"maxwidth\" ");
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
        result.append("/buttons/folder.png\" border=\"0\"></a></td>");
        result.append("</tr>\n");
        result.append("</table>\n");
        result.append("</td>\n");
        result.append("</tr>");

        return result;
    }

    /**
     * Builds the html for a single selectbox property row to select the list layout.<p>
     * 
     * @param listType determines the content list type, "center" or "right"
     * @param propertyName the name of the property
     * @param propertyTitle the nice name of the property
     * @param editable indicates if the properties are editable
     * 
     * @return the html for a single text input property row
     */
    private StringBuffer buildPropertySelectbox(
        String listType,
        String propertyName,
        String propertyTitle,
        boolean editable) {

        StringBuffer result = new StringBuffer(128);
        result.append(buildTableRowStart(key(propertyTitle)));
        String disabled = "";
        if (!editable) {
            disabled = " disabled=\"disabled\"";
        }
        String propValue = "";
        // get property object from active properties
        CmsProperty currentProperty = (CmsProperty)getActiveProperties().get(propertyName);
        String inheritedValue = "";
        if (currentProperty != null) {
            // property value is directly set on resource
            propValue = currentProperty.getValue();
            inheritedValue = getDefault(propertyName);
        } else {
            // property is not set on resource
            propValue = getDefault(propertyName);
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(propValue)) {
                propValue = "";
            }
            inheritedValue = propValue;
        }

        List resources = getConfigurationFiles(listType);
        List options = new ArrayList(resources.size() + 1);
        List values = new ArrayList(resources.size() + 1);
        int selectedIndex = 0;

        // add the "none" option manually to selectbox
        options.add(key(KEY_PREFIX + "nolayout"));
        if ("".equals(propValue)
            || ("".equals(inheritedValue))
            || (CmsTemplateBean.PROPERTY_VALUE_NONE.equals(inheritedValue))) {
            // value is not set anywhere or is inherited from ancestor folder
            values.add("");
        } else {
            values.add(CmsTemplateBean.PROPERTY_VALUE_NONE);
        }

        for (int i = 0; i < resources.size(); i++) {
            // loop all found resources defining the layout
            CmsResource res = (CmsResource)resources.get(i);
            String path = getCms().getSitePath(res);
            // determine description to show for layout
            String description = "";
            try {
                description = getCms().readPropertyObject(path, CmsPropertyDefinition.PROPERTY_DESCRIPTION, false).getValue(
                    path);
            } catch (CmsException e) {
                // should never happen
                if (LOG.isErrorEnabled()) {
                    LOG.error(e);
                }
            }
            // try to find a localized key for the description property value
            String localized = key(description);
            if (localized.startsWith(CmsMessages.UNKNOWN_KEY_EXTENSION)) {
                localized = description;
            }
            options.add(localized);
            // check if this item is selected
            if (path.equals(propValue)) {
                selectedIndex = i + 1;
            }
            // determine value to add
            if (path.equals(inheritedValue)) {
                // the current path is like inherited path, so do not write property in this case
                path = "";
            }
            values.add(path);
        }
        // create select tag attributes
        StringBuffer attrs = new StringBuffer(4);
        attrs.append("name=\"").append(PREFIX_VALUE).append(propertyName).append("\"");
        attrs.append(" class=\"maxwidth\"");
        attrs.append(disabled);
        // create the select box
        result.append(buildSelect(attrs.toString(), options, values, selectedIndex));

        // build the hidden field with old property value
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
    private StringBuffer buildRadioButtons(String propertyName, String mode, String JSToggleFunction, boolean editable) {

        StringBuffer result = new StringBuffer(256);
        // propertyName will be translated in workplace.properties
        result.append(buildTableRowStart(key(KEY_PREFIX + propertyName), 2));
        result.append("\t<table border=\"0\">\n");
        result.append("\t<tr>\n");
        result.append("\t<td>\n");
        result.append(buildPropertyRadioEntry(
            propertyName,
            PARAM_DEFAULT,
            key(KEY_PREFIX + "radio.default"),
            JSToggleFunction,
            editable));
        result.append("</td>\n");
        result.append("\t<td>\n");
        result.append(buildPropertyRadioEntry(
            propertyName,
            PARAM_TRUE,
            key(KEY_PREFIX + "radio." + mode),
            JSToggleFunction,
            editable));
        result.append("</td>\n");
        result.append("\t<td>\n");
        result.append(buildPropertyRadioEntry(
            propertyName,
            PARAM_FALSE,
            key(KEY_PREFIX + "radio.disable"),
            JSToggleFunction,
            editable));
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

    /**
     * Returns the layout configuration files for the specified list type.<p>
     *
     * @param listType the type of the layout list, "center" or "right"
     * @return the layout configuration files for the specified list type
     */
    private List getConfigurationFiles(String listType) {

        List result = new ArrayList();
        String configFolder;
        if (listType.equals(CmsTemplateContentListItem.DISPLAYAREA_CENTER)) {
            configFolder = VFS_PATH_CONFIGFILES_CENTER;
        } else {
            configFolder = VFS_PATH_CONFIGFILES_RIGHT;
        }
        try {
            result = getCms().readResources(configFolder, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
        } catch (CmsException e) {
            // error reading resources
            if (LOG.isErrorEnabled()) {
                LOG.error(e);
            }
        }
        return result;
    }
}