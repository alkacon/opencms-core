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
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessages;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.xml.content.I_CmsXmlContentHandler.DisplayType;
import org.opencms.xml.types.A_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Base class for select widgets.<p>
 * 
 * @since 6.0.0 
 * 
 * @see org.opencms.widgets.CmsSelectWidgetOption
 */
public abstract class A_CmsSelectWidget extends A_CmsWidget implements I_CmsADEWidget {

    /** Configuration parameter to set the height from the select widget in pixel. */
    public static final String CONFIGURATION_HEIGHT = "height";

    /** The select widget height in pixel. */
    private String m_height;

    /** The possible options for the select box. */
    private List<CmsSelectWidgetOption> m_selectOptions;

    /**
     * Creates a new select widget.<p>
     */
    public A_CmsSelectWidget() {

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
    public A_CmsSelectWidget(List<CmsSelectWidgetOption> configuration) {

        super();
        m_selectOptions = configuration;
    }

    /**
     * Creates a select widget with the select options specified in the given configuration String.<p>
     * 
     * Please see <code>{@link CmsSelectWidgetOption}</code> for a description of the syntax 
     * of the configuration String.<p>
     * 
     * @param configuration the configuration (possible options) for the select widget
     * 
     * @see CmsSelectWidgetOption
     */
    public A_CmsSelectWidget(String configuration) {

        super(configuration);
    }

    /**
     * Adds a new select option to this widget.<p>
     * 
     * @param option the select option to add
     */
    public void addSelectOption(CmsSelectWidgetOption option) {

        if (m_selectOptions == null) {
            m_selectOptions = new ArrayList<CmsSelectWidgetOption>();
        }
        m_selectOptions.add(option);
    }

    /**
     * @see org.opencms.widgets.A_CmsWidget#getConfiguration()
     */
    @Override
    public String getConfiguration() {

        if (super.getConfiguration() != null) {
            return super.getConfiguration();
        }
        return CmsSelectWidgetOption.createConfigurationString(m_selectOptions);
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getConfiguration(org.opencms.file.CmsObject, org.opencms.xml.types.A_CmsXmlContentValue, org.opencms.i18n.CmsMessages, org.opencms.file.CmsResource, java.util.Locale)
     */
    public String getConfiguration(
        CmsObject cms,
        A_CmsXmlContentValue schemaType,
        CmsMessages messages,
        CmsResource resource,
        Locale contentLocale) {

        String result = "";
        CmsDummyWidgetDialog widgetDialog = new CmsDummyWidgetDialog(messages.getLocale(), messages);
        widgetDialog.setResource(resource);
        List<CmsSelectWidgetOption> options = parseSelectOptions(cms, widgetDialog, schemaType);
        Iterator<CmsSelectWidgetOption> it = options.iterator();
        int i = 0;
        while (it.hasNext()) {
            CmsSelectWidgetOption option = it.next();
            if (i > 0) {
                result += "|";
            }
            result += option.toString();
            i++;
        }
        return result;
    }

    /**
     * Returns a list of CSS resources required by the widget.<p>
     * 
     * @param cms the current OpenCms context
     * 
     * @return the required CSS resource links
     */
    public List<String> getCssResourceLinks(CmsObject cms) {

        return null;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getDefaultDisplayType()
     */
    public DisplayType getDefaultDisplayType() {

        return DisplayType.singleline;
    }

    /**
     * Returns the java script initialization call.<p>
     * 
     * @return the java script initialization call
     */
    public String getInitCall() {

        return null;
    }

    /**
     * Returns a list of java script resources required by the widget.<p>
     * 
     * @param cms the current OpenCms context
     * 
     * @return the required java script resource links
     */
    public List<String> getJavaScriptResourceLinks(CmsObject cms) {

        return null;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getWidgetName()
     */
    public String getWidgetName() {

        return A_CmsSelectWidget.class.getName();
    }

    /**
     * Returns if this is an internal widget.<p>
     * Only widgets belonging to the OpenCms core should be marked as internal.<p>
     * 
     * @return <code>true</code> if this is an internal widget
     */
    public boolean isInternal() {

        return false;
    }

    /**
     * @see org.opencms.widgets.A_CmsWidget#setConfiguration(java.lang.String)
     */
    @Override
    public void setConfiguration(String configuration) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(configuration)) {
            int heightIndex = configuration.indexOf(CONFIGURATION_HEIGHT);
            if (heightIndex != -1) {
                // the height is set
                String height = configuration.substring(heightIndex + CONFIGURATION_HEIGHT.length() + 1);
                if (height.indexOf('|') != -1) {
                    // cut eventual following configuration values
                    height = height.substring(0, height.indexOf('|'));
                }
                m_height = height;
            }
        }
        super.setConfiguration(configuration);
    }

    /**
     * Gets the configured select widget height.<p>
     * 
     * @return the configured select widget height
     */
    protected String getHeight() {

        return m_height;
    }

    /** 
     * Gets the resource path for the given dialog.<p>
     * @param cms TODO
     * @param dialog the dialog 
     * 
     * @return the resource path 
     */
    protected String getResourcePath(CmsObject cms, I_CmsWidgetDialog dialog) {

        String result = null;
        if (dialog instanceof CmsDummyWidgetDialog) {
            result = ((CmsDummyWidgetDialog)dialog).getResource().getRootPath();
        } else if (dialog instanceof CmsDialog) {
            result = ((CmsDialog)dialog).getParamResource();
            if (result != null) {
                result = cms.getRequestContext().addSiteRoot(result);
            }
        }
        return result;
    }

    /**
     * Returns the currently selected value of the select widget.<p>
     * 
     * If a value is found in the given parameter, this is used. Otherwise 
     * the default value of the select options are used. If there is neither a parameter value
     * nor a default value, <code>null</code> is returned.<p> 
     * 
     * @param cms the current users OpenCms context
     * @param param the widget parameter of this dialog
     * 
     * @return the currently selected value of the select widget
     */
    protected String getSelectedValue(CmsObject cms, I_CmsWidgetParameter param) {

        String paramValue = param.getStringValue(cms);
        if (CmsStringUtil.isEmpty(paramValue)) {
            CmsSelectWidgetOption option = CmsSelectWidgetOption.getDefaultOption(m_selectOptions);
            if (option != null) {
                paramValue = option.getValue();
            }
        }
        return paramValue;
    }

    /**
     * Returns the currently selected values of the select widget.<p>
     * 
     * If a value is found in the given parameter, this is used. Otherwise 
     * the default value of the select options are used. If there is neither a parameter value
     * nor a default value, <code>null</code> is used.<p> 
     * 
     * @param cms the current users OpenCms context
     * @param param the widget parameter of this dialog
     * 
     * @return a list of the currently selected values of the select widget
     */
    protected List<String> getSelectedValues(CmsObject cms, I_CmsWidgetParameter param) {

        List<String> values = new ArrayList<String>();
        String paramValue = param.getStringValue(cms);
        if (CmsStringUtil.isEmpty(paramValue)) {
            Iterator<CmsSelectWidgetOption> itOptions = CmsSelectWidgetOption.getDefaultOptions(m_selectOptions).iterator();
            while (itOptions.hasNext()) {
                CmsSelectWidgetOption option = itOptions.next();
                values.add(option.getValue());
            }
        } else {
            values.addAll(CmsStringUtil.splitAsList(paramValue, ',', true));
        }
        return values;
    }

    /**
     * Returns the list of configured select options.<p>
     * 
     * The list elements are of type <code>{@link CmsSelectWidgetOption}</code>.<p>
     * 
     * @return the list of select options
     */
    protected List<CmsSelectWidgetOption> getSelectOptions() {

        return m_selectOptions;
    }

    /**
     * Returns the list of configured select options, parsing the configuration String if required.<p>
     * 
     * The list elements are of type <code>{@link CmsSelectWidgetOption}</code>.
     * The configuration String is parsed only once and then stored internally.<p>
     * 
     * @param cms the current users OpenCms context
     * @param widgetDialog the dialog of this widget
     * @param param the widget parameter of this dialog
     * 
     * @return the list of select options
     * 
     * @see CmsSelectWidgetOption
     */
    protected List<CmsSelectWidgetOption> parseSelectOptions(
        CmsObject cms,
        I_CmsWidgetDialog widgetDialog,
        I_CmsWidgetParameter param) {

        if (m_selectOptions == null) {
            String configuration = getConfiguration();
            if (configuration == null) {
                // workaround: use the default value to parse the options
                configuration = param.getDefault(cms);
            }
            configuration = CmsMacroResolver.resolveMacros(configuration, cms, widgetDialog.getMessages());
            m_selectOptions = CmsSelectWidgetOption.parseOptions(configuration);
            if (m_selectOptions == Collections.EMPTY_LIST) {
                m_selectOptions = new ArrayList<CmsSelectWidgetOption>();
            }
        }
        return m_selectOptions;
    }

    /**
     * Sets the list of configured select options.<p>
     * 
     * The list elements must be of type <code>{@link CmsSelectWidgetOption}</code>.<p>
     * 
     * @param selectOptions the list of select options to set
     */
    protected void setSelectOptions(List<CmsSelectWidgetOption> selectOptions) {

        m_selectOptions = new ArrayList<CmsSelectWidgetOption>();
        m_selectOptions.addAll(selectOptions);
    }

}