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

package org.opencms.widgets;

import org.opencms.file.CmsObject;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Provides a widget for a standard HTML form multi select list or a group of check boxes.<p>
 * 
 * Please see the documentation of <code>{@link org.opencms.widgets.CmsSelectWidgetOption}</code> for a description 
 * about the configuration String syntax for the select options.<p>
 *
 * The multi select widget does use the following select options:<ul>
 * <li><code>{@link org.opencms.widgets.CmsSelectWidgetOption#getValue()}</code> for the value of the option
 * <li><code>{@link org.opencms.widgets.CmsSelectWidgetOption#isDefault()}</code> for pre-selecting a specific value 
 * <li><code>{@link org.opencms.widgets.CmsSelectWidgetOption#getOption()}</code> for the display name of the option
 * </ul>
 * <p>
 * 
 * @since 6.0.0 
 */
public class CmsMultiSelectWidget extends A_CmsSelectWidget {

    /** Configuration parameter to set the height from the select widget in pixel. */
    public static final String CONFIGURATION_ASCHECKBOXES = "ascheckboxes";

    /** Configuration parameter to indicate the multi-select needs to be activated by a check box. */
    public static final String CONFIGURATION_REQUIRES_ACTIVATION = "requiresactivation";

    /** Indicates if used html code is a multi selection list or a list of checkboxes. */
    private boolean m_asCheckBoxes;

    /** Flag to indicate if the multi-select needs to be activated by a check box. */
    private boolean m_requiresActivation;

    /**
     * Creates a new select widget.<p>
     */
    public CmsMultiSelectWidget() {

        // empty constructor is required for class registration
        super();
    }

    /**
     * Creates a select widget with the select options specified in the given configuration List.<p>
     * 
     * The list elements must be of type <code>{@link CmsSelectWidgetOption}</code>.<p>
     * 
     * @param configuration the configuration (possible options) for the select widget
     * 
     * @see CmsSelectWidgetOption
     */
    public CmsMultiSelectWidget(List<CmsSelectWidgetOption> configuration) {

        this(configuration, false);
    }

    /**
     * Creates a select widget with the select options specified in the given configuration List.<p>
     * 
     * The list elements must be of type <code>{@link CmsSelectWidgetOption}</code>.<p>
     * 
     * @param configuration the configuration (possible options) for the select widget
     * @param asCheckboxes indicates if used html code is a multi selection list or a list of checkboxes
     * 
     * @see CmsSelectWidgetOption
     */
    public CmsMultiSelectWidget(List<CmsSelectWidgetOption> configuration, boolean asCheckboxes) {

        super(configuration);
        m_asCheckBoxes = asCheckboxes;
    }

    /**
     * Creates a select widget with the specified select options.<p>
     * 
     * @param configuration the configuration (possible options) for the select box
     */
    public CmsMultiSelectWidget(String configuration) {

        super(configuration);
    }

    /**
     * @param cms
     * @param formParameters
     * @param widgetDialog
     * @param param
     */
    public static void setMultiSelectEditorValue(
        CmsObject cms,
        Map<String, String[]> formParameters,
        I_CmsWidgetDialog widgetDialog,
        I_CmsWidgetParameter param) {

        String[] values = formParameters.get(param.getId());
        if ((values != null) && (values.length > 0)) {
            StringBuffer value = new StringBuffer(128);
            for (int i = 0; i < values.length; i++) {
                if (i > 0) {
                    value.append(',');
                }
                value.append(values[i]);
            }
            // set the value
            param.setStringValue(cms, value.toString());
        } else {
            // erase:
            param.setStringValue(cms, "");
        }
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogIncludes(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    @Override
    public String getDialogIncludes(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        return getJSIncludeFile(CmsWorkplace.getSkinUri() + "components/widgets/multiselector.js");
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();
        StringBuffer result = new StringBuffer(16);

        List<CmsSelectWidgetOption> options = parseSelectOptions(cms, widgetDialog, param);
        result.append("<td class=\"xmlTd\">");
        // the configured select widget height start element
        if (!m_asCheckBoxes) {
            if (m_requiresActivation) {
                result.append("<input style=\"vertical-align:middle;\" type=\"checkbox\" id=\"check"
                    + id
                    + "\" name=\"check"
                    + id
                    + "\""
                    + "onclick=toggleMultiSelectWidget(this);"
                    + " />");
                result.append("&nbsp;<label style=\"vertical-align:middle;\" for=\"check" + id + "\">");
                result.append(widgetDialog.getMessages().key(Messages.GUI_MULTISELECT_ACTIVATE_0));
                result.append("</label>&nbsp;");
                // adding hidden input with the current value, because disabled select box value won't be submitted 
                result.append("<input type='hidden' name='").append(id).append("' id='").append(id).append("' value='");
                List<String> values = getSelectedValues(cms, param);
                if (values.size() > 0) {
                    result.append(values.get(0));
                    for (int i = 1; i < values.size(); i++) {
                        result.append(",").append(values.get(i));
                    }
                }
                result.append("' />");
                id = "display" + id;
            }
            result.append("<select multiple size='");
            result.append(options.size());
            result.append("' style=\"vertical-align:middle;\" class=\"xmlInput");
            if (param.hasError()) {
                result.append(" xmlInputError");
            }
            result.append("\" ");
            if (m_requiresActivation) {
                result.append("disabled=\"true\" ");
            }
            result.append("name=\"");
            result.append(id);
            result.append("\" id=\"");
            result.append(id);
            result.append("\">");
        }

        // get select box options from default value String
        List<String> selected = getSelectedValues(cms, param);
        Iterator<CmsSelectWidgetOption> i = options.iterator();
        while (i.hasNext()) {
            CmsSelectWidgetOption option = i.next();
            // create the option
            if (!m_asCheckBoxes) {
                result.append("<option value=\"");
                result.append(option.getValue());
                result.append("\"");
                if (selected.contains(option.getValue())) {
                    result.append(" selected=\"selected\"");
                }
                result.append(">");
                result.append(option.getOption());
                result.append("</option>");
            } else {
                result.append("<input type='checkbox' name='");
                result.append(id);
                result.append("' value='");
                result.append(option.getValue());
                result.append("'");
                if (selected.contains(option.getValue())) {
                    result.append(" checked");
                }
                result.append(">");
                result.append(option.getOption());
                result.append("<br>");
            }
        }
        if (!m_asCheckBoxes) {
            result.append("</select>");
        }
        result.append("</td>");
        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    public I_CmsWidget newInstance() {

        return new CmsMultiSelectWidget(getConfiguration());
    }

    /**
     * @see org.opencms.widgets.A_CmsWidget#setConfiguration(java.lang.String)
     */
    @Override
    public void setConfiguration(String configuration) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(configuration)) {
            int asCheckBoxesIndex = configuration.indexOf(CONFIGURATION_ASCHECKBOXES);
            if (asCheckBoxesIndex != -1) {
                // the height is set
                String asCheckBoxes = configuration.substring(asCheckBoxesIndex
                    + CONFIGURATION_ASCHECKBOXES.length()
                    + 1);
                if (asCheckBoxes.indexOf('|') != -1) {
                    // cut eventual following configuration values
                    asCheckBoxes = asCheckBoxes.substring(0, asCheckBoxes.indexOf('|'));
                }
                m_asCheckBoxes = Boolean.parseBoolean(asCheckBoxes);
            }
            int reqiresActivationIndex = configuration.indexOf(CONFIGURATION_REQUIRES_ACTIVATION);
            if (reqiresActivationIndex != -1) {
                // the height is set
                String requiresActivation = configuration.substring(reqiresActivationIndex
                    + CONFIGURATION_REQUIRES_ACTIVATION.length()
                    + 1);
                if (requiresActivation.indexOf('|') != -1) {
                    // cut eventual following configuration values
                    requiresActivation = requiresActivation.substring(0, requiresActivation.indexOf('|'));
                }
                m_requiresActivation = Boolean.parseBoolean(requiresActivation);
            }
        }
        super.setConfiguration(configuration);
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#setEditorValue(org.opencms.file.CmsObject, java.util.Map, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    @Override
    public void setEditorValue(
        CmsObject cms,
        Map<String, String[]> formParameters,
        I_CmsWidgetDialog widgetDialog,
        I_CmsWidgetParameter param) {

        setMultiSelectEditorValue(cms, formParameters, widgetDialog, param);
    }
}