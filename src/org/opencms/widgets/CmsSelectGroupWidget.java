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

package org.opencms.widgets;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.types.A_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.logging.Log;

/**
 * Provides a widget for group selection select boxes.<p>
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
 * @since 8.0.0
 */
public class CmsSelectGroupWidget extends CmsSelectWidget {

    /** Configuration option key: group filter. */
    protected static final String CONFIGURATION_GROUPFILTER = "groupfilter";

    /** Configuration option key: groups. */
    protected static final String CONFIGURATION_GROUPS = "groups";

    /** Configuration option key: include sub OUs. */
    protected static final String CONFIGURATION_INCLUDESUBOUS = "includesubous";

    /** Configuration option key: OU fully qualified name. */
    protected static final String CONFIGURATION_OUFQN = "oufqn";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSelectGroupWidget.class);

    /** The configured group filter to match groups to show in the select box. */
    Pattern m_groupFilter;

    /** The configured group names to show in the select box. */
    List<String> m_groupNames;

    /** Indicates if sub OUs should be included when reading the groups. */
    private boolean m_includeSubOus;

    /** The fully qualified name of the OU to read the groups from. */
    private String m_ouFqn;

    /**
     * Creates a new group select widget.<p>
     */
    public CmsSelectGroupWidget() {

        // empty constructor is required for class registration
        super();
    }

    /**
     * Creates a group select widget with the specified select options.<p>
     *
     * @param configuration the configuration (possible options) for the group select box
     */
    public CmsSelectGroupWidget(String configuration) {

        super(configuration);
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getConfiguration(org.opencms.file.CmsObject, org.opencms.xml.types.A_CmsXmlContentValue, org.opencms.i18n.CmsMessages, org.opencms.file.CmsResource, java.util.Locale)
     */
    @Override
    public String getConfiguration(
        CmsObject cms,
        A_CmsXmlContentValue schemaType,
        CmsMessages messages,
        CmsResource resource,
        Locale contentLocale) {

        parseSelectOptions(cms, messages, schemaType);
        String results = getConfiguration();

        return results;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getCssResourceLinks(org.opencms.file.CmsObject)
     */
    @Override
    public List<String> getCssResourceLinks(CmsObject cms) {

        return null;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getInitCall()
     */
    @Override
    public String getInitCall() {

        return null;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getJavaScriptResourceLinks(org.opencms.file.CmsObject)
     */
    @Override
    public List<String> getJavaScriptResourceLinks(CmsObject cms) {

        return null;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#isInternal()
     */
    @Override
    public boolean isInternal() {

        return true;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    @Override
    public I_CmsWidget newInstance() {

        return new CmsSelectGroupWidget(getConfiguration());
    }

    /**
     * Returns the list of configured select options, parsing the configuration String if required.<p>
     *
     * The list elements are of type <code>{@link CmsSelectWidgetOption}</code>.
     * The configuration String is parsed only once and then stored internally.<p>
     *
     * @param cms the current users OpenCms context
     * @param messages the messages of this dialog
     * @param param the widget parameter of this dialog
     *
     * @return the list of select options
     *
     * @see CmsSelectWidgetOption
     */
    protected List<CmsSelectWidgetOption> parseSelectOptions(
        CmsObject cms,
        CmsMessages messages,
        I_CmsWidgetParameter param) {

        // only create options if not already done
        if (getSelectOptions() == null) {
            // parse widget configuration
            parseConfiguration(cms, messages);
            List<CmsSelectWidgetOption> result = new ArrayList<CmsSelectWidgetOption>();

            if (isUseGroupNames()) {
                // a list of group names is configured, show them
                for (Iterator<String> i = getGroupNames().iterator(); i.hasNext();) {
                    String groupName = i.next();
                    try {
                        // ensure that only existing groups are available in the select box
                        CmsGroup group = cms.readGroup(getOuFqn() + groupName);
                        result.add(new CmsSelectWidgetOption(group.getName(), false, group.getSimpleName()));
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
                        result.add(new CmsSelectWidgetOption(group.getName(), false, group.getSimpleName()));
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
     * @see org.opencms.widgets.A_CmsSelectWidget#parseSelectOptions(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    @Override
    protected List<CmsSelectWidgetOption> parseSelectOptions(
        CmsObject cms,
        I_CmsWidgetDialog widgetDialog,
        I_CmsWidgetParameter param) {

        return parseSelectOptions(cms, widgetDialog.getMessages(), param);
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
    private void parseConfiguration(CmsObject cms, CmsMessages widgetDialog) {

        String configString = "";
        if (widgetDialog != null) {
            configString = CmsMacroResolver.resolveMacros(getConfiguration(), cms, widgetDialog);
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
            m_includeSubOus = Boolean.valueOf(config.get(CONFIGURATION_INCLUDESUBOUS)).booleanValue();
        }
    }
}
