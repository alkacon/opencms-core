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

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.logging.Log;

/**
 * Provides a widget for group selection multi select boxes.<p>
 * 
 * This widget is configurable with the following options:<p>
 * <ul>
 * <li><code>groupfilter</code>: regular expression to filter available groups</li>
 * <li><code>groups</code>: comma separated list of group names to show in the select box. <b>Note</b>:
 *     if this configuration option if used,
 *     <code>groupfilter</code> and <code>includesubous</code> are <i>not</i> considered anymore.</li>
 * <li><code>includesubous</code>: boolean flag to indicate if sub OUs should be scanned for groups to select</li>
 * <li><code>oufqn</code>: the fully qualified name of the OU to read the groups from</li>
 * </ul>
 * To map the selected group to a permission to set, use the following mapping configuration:<p>
 * <code>&lt;mapping element="..." mapto="permission:GROUP:+r+v|GROUP.ALL_OTHERS:|GROUP.Projectmanagers:+r+v+w+c" /&gt;</code><p>
 * This means that the +r+v permission is written for the principal <code>GROUP</code> on the resource.
 * Additionally two permissions are written as default: for <code>ALL_OTHERS</code>, no allowed permission is set,
 * for <code>Projectmanagers</code>, "+r+v+w+c" is set.<p>
 * 
 * @author Mario Jaeger
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.2
 */
public class CmsMultiSelectGroupWidget extends CmsSelectGroupWidget {

    /** Configuration parameter name to use all available groups as default. */
    public static final String CONFIGURATION_DEFAULT_ALL = "defaultall";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsMultiSelectGroupWidget.class);

    /** Indicates if used html code is a multi selection list or a list of checkboxes. */
    private boolean m_asCheckBoxes;

    /** Flag indicating if to use all available groups as default. */
    private boolean m_defaultAllAvailable;

    /** Indicates if sub OUs should be included when reading the groups. */
    private boolean m_includeSubOus;

    /** The fully qualified name of the OU to read the groups from. */
    private String m_ouFqn;

    /** Flag to indicate if the multi-select needs to be activated by a check box. */
    private boolean m_requiresActivation;

    /**
     * Creates a new group select widget.<p>
     */
    public CmsMultiSelectGroupWidget() {

        // empty constructor is required for class registration
        super();
    }

    /**
     * Creates a group select widget with the specified select options.<p>
     * 
     * @param configuration the configuration (possible options) for the group select box
     */
    public CmsMultiSelectGroupWidget(String configuration) {

        super(configuration);
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
    public CmsMultiSelectGroupWidget(String configuration, boolean asCheckboxes) {

        super(configuration);
        m_asCheckBoxes = asCheckboxes;
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
    @Override
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();
        StringBuffer result = new StringBuffer(16);
        String height = getHeight();
        List<CmsSelectWidgetOption> options = parseSelectOptions(cms, widgetDialog, param);
        result.append("<td class=\"xmlTd\">");
        // the configured select widget height start element
        if (m_asCheckBoxes && CmsStringUtil.isNotEmptyOrWhitespaceOnly(height)) {
            result.append("<div style=\"height: " + height + "; overflow: auto;\">");
        }
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
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(height)) {
                result.append("<select style=\"height: " + height + ";\" multiple size='");
            } else {
                result.append("<select multiple size='");
            }
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
        // the configured select widget height end element
        if (m_asCheckBoxes && CmsStringUtil.isNotEmptyOrWhitespaceOnly(height)) {
            result.append("</div>");
        }
        result.append("</td>");

        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    @Override
    public I_CmsWidget newInstance() {

        return new CmsMultiSelectGroupWidget(getConfiguration());
    }

    /**
     * @see org.opencms.widgets.A_CmsWidget#setConfiguration(java.lang.String)
     */
    @Override
    public void setConfiguration(String configuration) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(configuration)) {
            int asCheckBoxesIndex = configuration.indexOf(CmsMultiSelectWidget.CONFIGURATION_ASCHECKBOXES);
            if (asCheckBoxesIndex != -1) {
                // the height is set
                String asCheckBoxes = configuration.substring(asCheckBoxesIndex
                    + CmsMultiSelectWidget.CONFIGURATION_ASCHECKBOXES.length()
                    + 1);
                if (asCheckBoxes.indexOf('|') != -1) {
                    // cut eventual following configuration values
                    asCheckBoxes = asCheckBoxes.substring(0, asCheckBoxes.indexOf('|'));
                }
                m_asCheckBoxes = Boolean.parseBoolean(asCheckBoxes);
            }
            int reqiresActivationIndex = configuration.indexOf(CmsMultiSelectWidget.CONFIGURATION_REQUIRES_ACTIVATION);
            if (reqiresActivationIndex != -1) {
                // the height is set
                String requiresActivation = configuration.substring(reqiresActivationIndex
                    + CmsMultiSelectWidget.CONFIGURATION_REQUIRES_ACTIVATION.length()
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
     * @see org.opencms.widgets.A_CmsWidget#setEditorValue(org.opencms.file.CmsObject, java.util.Map, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    @Override
    public void setEditorValue(
        CmsObject cms,
        Map<String, String[]> formParameters,
        I_CmsWidgetDialog widgetDialog,
        I_CmsWidgetParameter param) {

        CmsMultiSelectWidget.setMultiSelectEditorValue(cms, formParameters, widgetDialog, param);
    }

    /**
     * Returns the select options for the widget, generated from the configured input fields of the XML content.<p>
     * 
     * @see org.opencms.widgets.A_CmsSelectWidget#parseSelectOptions(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    @Override
    protected List<CmsSelectWidgetOption> parseSelectOptions(
        CmsObject cms,
        I_CmsWidgetDialog widgetDialog,
        I_CmsWidgetParameter param) {

        // only create options if not already done
        if (getSelectOptions() == null) {
            // parse widget configuration
            parseConfiguration(cms, widgetDialog);
            List<CmsSelectWidgetOption> result = new ArrayList<CmsSelectWidgetOption>();

            if (isUseGroupNames()) {
                // a list of group names is configured, show them
                for (Iterator<String> i = getGroupNames().iterator(); i.hasNext();) {
                    String groupName = i.next();
                    try {
                        // ensure that only existing groups are available in the select box
                        CmsGroup group = cms.readGroup(getOuFqn() + groupName);
                        result.add(new CmsSelectWidgetOption(
                            group.getName(),
                            m_defaultAllAvailable,
                            group.getSimpleName()));
                    } catch (CmsException e) {
                        // error reading the group by name, simply skip it
                    }
                }
            } else {
                // read the groups from an optionally configured OU and filter them if configured 
                try {
                    List<CmsGroup> groups = OpenCms.getOrgUnitManager().getGroups(cms, getOuFqn(), isIncludeSubOus());
                    for (Iterator<CmsGroup> i = groups.iterator(); i.hasNext();) {
                        CmsGroup group = i.next();
                        if (isUseGroupFilter()) {
                            // check if group name matches the given regular expression
                            if (!getGroupFilter().matcher(group.getSimpleName()).matches()) {
                                continue;
                            }
                        }
                        result.add(new CmsSelectWidgetOption(
                            group.getName(),
                            m_defaultAllAvailable,
                            group.getSimpleName()));
                    }
                } catch (CmsException e) {
                    // error reading the groups
                }

            }
            setSelectOptions(result);
        }
        return getSelectOptions();
    }

    /**
     * Returns the configured group filter to match groups to show in the select box.<p>
     * 
     * @return the configured group filter to match groups to show in the select box
     */
    private Pattern getGroupFilter() {

        return m_groupFilter;
    }

    /**
     * Returns the configured group names to show in the select box.<p>
     * 
     * @return configured group names to show in the select box
     */
    private List<String> getGroupNames() {

        return m_groupNames;
    }

    /**
     * Returns the fully qualified name of the OU to read the groups from.<p>
     * 
     * @return the fully qualified name of the OU to read the groups from
     */
    private String getOuFqn() {

        return m_ouFqn;
    }

    /**
     * Returns if sub OUs should be considered when filtering the groups.<p>
     * 
     * @return <code>true</code> if sub OUs should be considered, otherwise <code>false</code>
     */
    private boolean isIncludeSubOus() {

        return m_includeSubOus;
    }

    /**
     * Returns if a group filter is configured to match groups to show in the select box.<p>
     * 
     * @return <code>true</code> if a group filter is configured, otherwise <code>false</code>
     */
    private boolean isUseGroupFilter() {

        return getGroupFilter() != null;
    }

    /**
     * Returns if group names are configured to show in the select box.<p>
     * 
     * @return <code>true</code> if group names are configured, otherwise <code>false</code>
     */
    private boolean isUseGroupNames() {

        return getGroupNames() != null;
    }

    /**
     * Parses the widget configuration string.<p>
     * 
     * @param cms the current users OpenCms context
     * @param widgetDialog the dialog of this widget
     */
    private void parseConfiguration(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        String configString = CmsMacroResolver.resolveMacros(getConfiguration(), cms, widgetDialog.getMessages());
        Map<String, String> config = CmsStringUtil.splitAsMap(configString, "|", "=");
        // get the list of group names to show
        String groups = config.get(CONFIGURATION_GROUPS);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(groups)) {
            m_groupNames = CmsStringUtil.splitAsList(groups, ',', true);
        }
        // get the regular expression to filter the groups
        String filter = config.get(CONFIGURATION_GROUPFILTER);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(filter)) {
            try {
                m_groupFilter = Pattern.compile(filter);
            } catch (PatternSyntaxException e) {
                // log pattern syntax errors
                LOG.error(Messages.get().getBundle().key(Messages.LOG_ERR_WIDGET_SELECTGROUP_PATTERN_1, filter));
            }
        }
        // get the OU to read the groups from
        m_ouFqn = config.get(CONFIGURATION_OUFQN);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_ouFqn)) {
            m_ouFqn = "";
        } else if (!m_ouFqn.endsWith(CmsOrganizationalUnit.SEPARATOR)) {
            m_ouFqn += CmsOrganizationalUnit.SEPARATOR;
        }
        // set the flag to include sub OUs
        m_includeSubOus = Boolean.parseBoolean(config.get(CONFIGURATION_INCLUDESUBOUS));
        m_defaultAllAvailable = Boolean.parseBoolean(config.get(CONFIGURATION_DEFAULT_ALL));
    }
}
