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

package org.opencms.ade.contenteditor.client.widgets;

import com.alkacon.acacia.client.I_WidgetFactory;
import com.alkacon.acacia.client.widgets.FormWidgetWrapper;
import com.alkacon.acacia.client.widgets.I_EditWidget;
import com.alkacon.acacia.client.widgets.I_FormEditWidget;

import org.opencms.ade.contenteditor.widgetregistry.client.WidgetRegistry;
import org.opencms.gwt.client.I_CmsHasInit;

import com.google.gwt.dom.client.Element;

/**
 * Factory to generate a location picker widget.<p>
 */
public class CmsLocationPickerWidgetFactory implements I_WidgetFactory, I_CmsHasInit {

    /** The widget name. */
    private static final String WIDGET_NAME = "org.opencms.widgets.CmsLocationPickerWidget";

    /**
     * Initializes this class.<p>
     */
    public static void initClass() {

        WidgetRegistry.getInstance().registerWidgetFactory(WIDGET_NAME, new CmsLocationPickerWidgetFactory());
    }

    /**
     * @see com.alkacon.acacia.client.I_WidgetFactory#createFormWidget(java.lang.String)
     */
    public I_FormEditWidget createFormWidget(String configuration) {

        return new FormWidgetWrapper(new CmsLocationPickerWidget(configuration));
    }

    /**
     * @see com.alkacon.acacia.client.I_WidgetFactory#createInlineWidget(java.lang.String, com.google.gwt.dom.client.Element)
     */
    public I_EditWidget createInlineWidget(String configuration, Element element) {

        return null;
    }
}
