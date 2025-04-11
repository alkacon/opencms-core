/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.widgets;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.util.CmsMacroResolver;

import java.util.Map;

/**
 * Provides a widget that creates a rich input field using the matching component, for use on a widget dialog.<p>
 *
 * The matching component is determined by checking the installed editors for the best matching component to use.<p>
 *
 * @since 6.0.1
 */
public abstract class A_CmsHtmlWidget extends A_CmsWidget {

    /**
     * Creates a new html editing widget.<p>
     */
    public A_CmsHtmlWidget() {

        // empty constructor is required for class registration
        super();
    }

    /**
     * Creates a new html editing widget with the given configuration.<p>
     *
     * @param configuration the configuration to use
     */
    public A_CmsHtmlWidget(String configuration) {

        super(configuration);
    }

    /**
     * @see org.opencms.widgets.A_CmsWidget#getConfiguration()
     */
    @Override
    public String getConfiguration() {

        return super.getConfiguration();
    }

    /**
     * Creates a CmsHtmlWidgetOption instance from the configuration string.
     *
     * @param cms the current CMS context
     * @return the parsed option bean
     */
    public CmsHtmlWidgetOption parseWidgetOptions(CmsObject cms) {

        String configStr = getConfiguration();
        if (cms != null) {
            CmsMacroResolver resolver = new CmsMacroResolver();
            resolver.setCmsObject(cms);
            configStr = resolver.resolveMacros(configStr);
        }
        return new CmsHtmlWidgetOption(configStr);

    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#setConfiguration(java.lang.String)
     */
    @Override
    public void setConfiguration(String configuration) {

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

        String[] values = formParameters.get(param.getId());
        if ((values != null) && (values.length > 0)) {
            String val = CmsEncoder.decode(values[0], CmsEncoder.ENCODING_UTF_8);
            param.setStringValue(cms, val);
        }
    }

}