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
import org.opencms.ade.contenteditor.widgetregistry.client.WidgetRegistry;
import org.opencms.gwt.client.I_CmsHasInit;
import org.opencms.gwt.shared.attributeselect.I_CmsAttributeSelectData;
import org.opencms.gwt.shared.attributeselect.I_CmsAttributeSelectFactory;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;

/**
 * Widget factory for creating attribute select widgets.
 */
public class CmsAttributeSelectWidgetFactory implements I_CmsWidgetFactory, I_CmsHasInit {

    /** The factory used to encode/decode data related to the attribute select widget. */
    public static final I_CmsAttributeSelectFactory DATA_FACTORY = GWT.create(I_CmsAttributeSelectFactory.class);

    /** The widget name under which the factory is registered. */
    public static final String WIDGET_NAME = "org.opencms.widgets.CmsAttributeSelectWidget";

    /**
     * Initializes this class.<p>
     */
    public static void initClass() {

        WidgetRegistry.getInstance().registerWidgetFactory(WIDGET_NAME, new CmsAttributeSelectWidgetFactory());
    }

    /**
     * @see org.opencms.acacia.client.I_CmsWidgetFactory#createFormWidget(java.lang.String)
     */
    public I_CmsFormEditWidget createFormWidget(String configuration) {

        I_CmsAttributeSelectData data = AutoBeanCodex.decode(
            DATA_FACTORY,
            I_CmsAttributeSelectData.class,
            configuration).as();

        return new CmsFormWidgetWrapper(new CmsAttributeSelectWidget(data));
    }

    /**
     * @see org.opencms.acacia.client.I_CmsWidgetFactory#createInlineWidget(java.lang.String, com.google.gwt.dom.client.Element)
     */
    public I_CmsEditWidget createInlineWidget(String configuration, Element element) {

        // unsupported
        return null;
    }

}
