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

package org.opencms.acacia.client.widgets;

import org.opencms.acacia.client.I_CmsWidgetFactory;
import org.opencms.acacia.client.css.I_CmsWidgetsLayoutBundle;
import org.opencms.ade.contenteditor.widgetregistry.client.WidgetRegistry;
import org.opencms.gwt.client.I_CmsHasInit;
import org.opencms.gwt.client.util.CmsDomUtil;

import com.google.gwt.dom.client.Element;

/**
 * Factory to generate basic input widget.<p>
 */
public class CmsCheckBoxWidgetFactory implements I_CmsWidgetFactory, I_CmsHasInit {

    /**
     * Wrapper class for checkboxes, which is needed because the help text needs to be set on the check box widgets themselves.
     */
    class CheckboxWrapper extends CmsFormWidgetWrapper {

        /** The wrapped widget. */
        private CmsCheckboxWidget m_checkBoxWidget;

        /**
         * Creates a new instance.<p>
         */
        public CheckboxWrapper() {

            super(new CmsCheckboxWidget());
            m_checkBoxWidget = (CmsCheckboxWidget)getEditWidget();
        }

        /**
         * @see org.opencms.acacia.client.widgets.CmsFormWidgetWrapper#setWidgetInfo(java.lang.String, java.lang.String)
         */
        @Override
        public void setWidgetInfo(String label, String help) {

            super.setWidgetInfo(label, help);
            m_checkBoxWidget.setTitle(CmsDomUtil.stripHtml(help));
        }
    }

    /** The widget name. */
    private static final String WIDGET_NAME = "org.opencms.widgets.CmsCheckboxWidget";

    /**
     * Initializes this class.<p>
     */
    public static void initClass() {

        WidgetRegistry.getInstance().registerWidgetFactory(WIDGET_NAME, new CmsCheckBoxWidgetFactory());
    }

    /**
     * @see org.opencms.acacia.client.I_CmsWidgetFactory#createFormWidget(java.lang.String)
     */
    public I_CmsFormEditWidget createFormWidget(String configuration) {

        CmsFormWidgetWrapper wrapper = new CheckboxWrapper();
        wrapper.addStyleName(I_CmsWidgetsLayoutBundle.INSTANCE.widgetCss().checkBoxStyle());
        return wrapper;

    }

    /**
     * @see org.opencms.acacia.client.I_CmsWidgetFactory#createInlineWidget(java.lang.String, com.google.gwt.dom.client.Element)
     */
    public I_CmsEditWidget createInlineWidget(String configuration, Element element) {

        return null;
    }

}
