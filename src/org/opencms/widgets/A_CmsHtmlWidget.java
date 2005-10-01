/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/widgets/A_CmsHtmlWidget.java,v $
 * Date   : $Date: 2005/10/01 20:50:06 $
 * Version: $Revision: 1.1.2.2 $
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
import org.opencms.i18n.CmsEncoder;

import java.util.Map;

/**
 * Provides a widget that creates a rich input field using the matching component, for use on a widget dialog.<p>
 * 
 * The matching component is determined by checking the installed editors for the best matching component to use.<p>
 *
 * @author Andreas Zahner
 * 
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 6.0.1 
 */
public abstract class A_CmsHtmlWidget extends A_CmsWidget {

    /** The configured Html widget options. */
    private CmsHtmlWidgetOption m_htmlWidgetOption;

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
    public A_CmsHtmlWidget(CmsHtmlWidgetOption configuration) {

        super();
        m_htmlWidgetOption = configuration;
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
     * Returns the configured Html widget options.<p>
     * 
     * @return the configured Html widget options
     */
    public CmsHtmlWidgetOption getHtmlWidgetOption() {

        return m_htmlWidgetOption;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#setConfiguration(java.lang.String)
     */
    public void setConfiguration(String configuration) {

        super.setConfiguration(configuration);
        m_htmlWidgetOption = new CmsHtmlWidgetOption(configuration);
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#setEditorValue(org.opencms.file.CmsObject, java.util.Map, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public void setEditorValue(
        CmsObject cms,
        Map formParameters,
        I_CmsWidgetDialog widgetDialog,
        I_CmsWidgetParameter param) {

        String[] values = (String[])formParameters.get(param.getId());
        if ((values != null) && (values.length > 0)) {
            String val = CmsEncoder.decode(values[0], CmsEncoder.ENCODING_UTF_8);
            param.setStringValue(cms, val);
        }
    }

    /**
     * Sets the configured Html widget options.<p>
     * 
     * @param htmlWidgetOption the configured Html widget options
     */
    public void setHtmlWidgetOption(CmsHtmlWidgetOption htmlWidgetOption) {

        m_htmlWidgetOption = htmlWidgetOption;
    }

    /**
     * @see org.opencms.widgets.A_CmsWidget#getConfiguration()
     */
    protected String getConfiguration() {

        if (super.getConfiguration() != null) {
            return super.getConfiguration();
        }
        return CmsHtmlWidgetOption.createConfigurationString(getHtmlWidgetOption());
    }
}