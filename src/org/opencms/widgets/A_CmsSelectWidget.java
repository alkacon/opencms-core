/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/widgets/A_CmsSelectWidget.java,v $
 * Date   : $Date: 2005/06/23 11:11:23 $
 * Version: $Revision: 1.5 $
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

package org.opencms.widgets;

import org.opencms.file.CmsObject;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for select widgets.<p>
 *
 * @author Alexander Kandzior 
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.5 $ 
 * 
 * @since 6.0.0 
 * 
 * @see org.opencms.widgets.CmsSelectWidgetOption
 */
public abstract class A_CmsSelectWidget extends A_CmsWidget {

    /** The possible options for the select box. */
    private List m_selectOptions;

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
    public A_CmsSelectWidget(List configuration) {

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
            m_selectOptions = new ArrayList();
        }
        m_selectOptions.add(option);
    }

    /**
     * @see org.opencms.widgets.A_CmsWidget#getConfiguration()
     */
    protected String getConfiguration() {

        if (super.getConfiguration() != null) {
            return super.getConfiguration();
        }
        return CmsSelectWidgetOption.createConfigurationString(m_selectOptions);
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
     * Returns the list of configured select options.<p>
     * 
     * The list elements are of type <code>{@link CmsSelectWidgetOption}</code>.<p>
     * 
     * @return the list of select options
     */
    protected List getSelectOptions() {

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
    protected List parseSelectOptions(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        if (m_selectOptions == null) {
            String configuration = getConfiguration();
            if (configuration == null) {
                // workaround: use the default value to parse the options
                configuration = param.getDefault(cms);
            }
            configuration = CmsMacroResolver.resolveMacros(configuration, cms, widgetDialog.getMessages());
            m_selectOptions = CmsSelectWidgetOption.parseOptions(configuration);
            if (m_selectOptions == Collections.EMPTY_LIST) {
                m_selectOptions = new ArrayList();
            }
        }
        return m_selectOptions;
    }
}