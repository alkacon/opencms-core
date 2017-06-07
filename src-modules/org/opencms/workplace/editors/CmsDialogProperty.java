/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.editors;

import org.opencms.file.CmsPropertyDefinition;
import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.commons.CmsPropertyCustom;
import org.opencms.workplace.explorer.CmsNewResourceXmlPage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Provides methods for the special xmlpage property dialog.<p>
 *
 * This is a special dialog that is used for xmlpages in the workplace and the editors.<p>
 * Uses methods from the customized property dialog where possible.<p>
 *
 * The following files use this class:
 * <ul>
 * <li>/jsp/editors/dialogs/property.html
 * </ul>
 * <p>
 *
 * @since 6.0.0
 */
public class CmsDialogProperty extends CmsPropertyCustom {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDialogProperty.class);

    /** Flag indicating if the template property was changed. */
    private boolean m_templateChanged;

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
     * Creates the HTML String for the edit properties form.<p>
     *
     * @return the HTML output String for the edit properties form
     */
    @Override
    public String buildEditForm() {

        CmsMessages messages = Messages.get().getBundle(getLocale());
        StringBuffer retValue = new StringBuffer(2048);

        // check if the properties are editable
        boolean editable = isEditable();
        // create "disabled" attribute if properties are not editable
        String disabled = "";
        if (!editable) {
            disabled = " disabled=\"disabled\"";
        }

        retValue.append("<table border=\"0\">\n");
        retValue.append("<tr>\n");
        retValue.append("\t<td class=\"textbold\">" + messages.key(Messages.GUI_INPUT_PROPERTY_0) + "</td>\n");
        retValue.append("\t<td class=\"textbold\">" + messages.key(Messages.GUI_LABEL_VALUE_0) + "</td>\n");
        retValue.append(
            "\t<td class=\"textbold\" style=\"white-space: nowrap;\">"
                + messages.key(Messages.GUI_INPUT_USEDPROPERTY_0)
                + "</td>\n");
        retValue.append("</tr>\n");
        retValue.append("<tr><td><span style=\"height: 6px;\"></span></td></tr>\n");

        // create template select box row
        retValue.append(buildTableRowStart(messages.key(Messages.GUI_INPUT_TEMPLATE_0)));
        retValue.append(
            buildSelectTemplates(
                "name=\"" + CmsPropertyDefinition.PROPERTY_TEMPLATE + "\" class=\"maxwidth noborder\"" + disabled));
        retValue.append("</td>\n");
        retValue.append("\t<td class=\"textcenter\">");
        retValue.append("&nbsp;");
        retValue.append(buildTableRowEnd());

        // create the text property input rows
        retValue.append(buildTextInput(editable));

        // show navigation properties if enabled in explorer type settings
        if (showNavigation()) {
            retValue.append(buildNavigationProperties(editable));
        }

        retValue.append("</table>");

        return retValue.toString();
    }

    /**
     * Builds the html for the page template select box.<p>
     *
     * @param attributes optional attributes for the &lt;select&gt; tag
     * @return the html for the page template select box
     */
    public String buildSelectTemplates(String attributes) {

        List<String> options = new ArrayList<String>();
        List<String> values = new ArrayList<String>();
        int selectedValue = -1;
        String currentTemplate = null;
        TreeMap<String, String> templates = null;
        try {
            // read the current template
            currentTemplate = getCms().readPropertyObject(
                getParamResource(),
                CmsPropertyDefinition.PROPERTY_TEMPLATE,
                true).getValue();
            // get all available templates
            templates = CmsNewResourceXmlPage.getTemplates(getCms(), getParamResource());
        } catch (CmsException e) {
            // ignore this exception
            if (LOG.isInfoEnabled()) {
                LOG.info(Messages.get().getBundle().key(Messages.LOG_READ_TEMPLATE_FAILED_0), e);
            }
        }
        if (currentTemplate == null) {
            currentTemplate = "";
        }
        if (templates == null) {
            // no valid template found, use only current one
            addCurrentTemplate(currentTemplate, options, values);
        } else {
            boolean found = false;
            // templates found, create option and value lists
            Iterator<Entry<String, String>> i = templates.entrySet().iterator();
            int counter = 0;
            while (i.hasNext()) {
                Entry<String, String> entry = i.next();
                String key = entry.getKey();
                String path = entry.getValue();
                if (currentTemplate.equals(path)) {
                    // mark the currently selected template
                    selectedValue = counter;
                    found = true;
                }
                options.add(key);
                values.add(path);
                counter++;
            }
            if (!found) {
                // current template was not found among module templates, add current template as option
                addCurrentTemplate(currentTemplate, options, values);
                selectedValue = 0;
            }
        }

        String hiddenField = "<input type=\"hidden\" name=\""
            + PREFIX_HIDDEN
            + CmsPropertyDefinition.PROPERTY_TEMPLATE
            + "\" value=\""
            + currentTemplate
            + "\">";
        return buildSelect(attributes, options, values, selectedValue, false) + hiddenField;
    }

    /**
     * Returns if the template property was changed.<p>
     *
     * @return true if the template property was changed, otherwise false
     */
    public boolean hasTemplateChanged() {

        return m_templateChanged;
    }

    /**
     * Performs the editing of the resources properties.<p>
     *
     * @param request the HttpServletRequest
     * @return true, if the properties were successfully changed, otherwise false
     * @throws CmsException if editing is not successful
     */
    @Override
    protected boolean performEditOperation(HttpServletRequest request) throws CmsException {

        boolean useTempfileProject = Boolean.valueOf(getParamUsetempfileproject()).booleanValue();
        try {
            if (useTempfileProject) {
                switchToTempProject();
            }
            // write the common properties defined in the explorer type settings
            Iterator<String> i = getExplorerTypeSettings().getProperties().iterator();
            // iterate over the properties
            while (i.hasNext()) {
                String curProperty = i.next();
                String paramValue = request.getParameter(PREFIX_VALUE + curProperty);
                String oldValue = request.getParameter(PREFIX_HIDDEN + curProperty);
                writeProperty(curProperty, paramValue, oldValue);
            }

            // write special file properties
            String paramValue = null;
            String oldValue = null;

            // write the navigation properties if enabled
            if (showNavigation()) {
                // get the navigation enabled parameter
                paramValue = request.getParameter("enablenav");
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
            }

            // get the template parameter
            paramValue = request.getParameter(CmsPropertyDefinition.PROPERTY_TEMPLATE);
            oldValue = request.getParameter(PREFIX_HIDDEN + CmsPropertyDefinition.PROPERTY_TEMPLATE);
            writeProperty(CmsPropertyDefinition.PROPERTY_TEMPLATE, paramValue, oldValue);
            if ((paramValue != null) && !paramValue.equals(oldValue)) {
                // template has changed, refresh editor window
                m_templateChanged = true;
            }

        } finally {
            if (useTempfileProject) {
                switchToCurrentProject();
            }
        }
        return true;
    }

    /**
     * Adds the currently selected template value to the option and value list.<p>
     *
     * @param currentTemplate the currently selected template to add
     * @param options the option list
     * @param values the value list
     */
    private void addCurrentTemplate(String currentTemplate, List<String> options, List<String> values) {

        CmsMessages messages = Messages.get().getBundle(getLocale());

        // template was not found in regular template folders, add current template value
        if (CmsStringUtil.isEmpty(currentTemplate)) {
            // current template not available, add "please select" value
            options.add(0, "--- " + messages.key(Messages.GUI_PLEASE_SELECT_0) + " ---");
            values.add(0, "");
        } else {
            // current template was set to some value, add this value to the selection
            String name = null;
            try {
                // read the title of the current template
                name = getCms().readPropertyObject(
                    currentTemplate,
                    CmsPropertyDefinition.PROPERTY_TITLE,
                    false).getValue();
            } catch (CmsException e) {
                // ignore this exception - the title for this template was not readable
                if (LOG.isInfoEnabled()) {
                    LOG.info(messages.key(Messages.LOG_READ_TITLE_PROP_FAILED_1, currentTemplate), e);
                }
            }
            if (name == null) {
                name = currentTemplate;
            }
            options.add(0, "* " + name);
            values.add(0, currentTemplate);
        }
    }
}