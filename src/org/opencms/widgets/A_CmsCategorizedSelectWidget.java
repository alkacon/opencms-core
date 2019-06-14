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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.gwt.shared.categorizedselect.I_CmsCategorizedSelectData;
import org.opencms.gwt.shared.categorizedselect.I_CmsCategorizedSelectDataFactory;
import org.opencms.i18n.CmsMessages;
import org.opencms.xml.content.I_CmsXmlContentHandler.DisplayType;
import org.opencms.xml.types.A_CmsXmlContentValue;

import java.util.List;
import java.util.Locale;

import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

/**
 * Abstract base class for categorized select widgets.
 *
 * <p>A categorized select widget consists of two select boxes, one for selecting the actual value and one
 * for filtering the choices available in the first select box. All data for the choices is loaded initially, when
 * the widget is initialized.
 */
public abstract class A_CmsCategorizedSelectWidget extends A_CmsWidget implements I_CmsADEWidget {

    /** Factory for creating the data for the client-side widget. */
    public static final I_CmsCategorizedSelectDataFactory DATA_FACTORY = AutoBeanFactorySource.create(
        I_CmsCategorizedSelectDataFactory.class);

    /**
     * Instantiates a new a cms categorized select widget.
     */
    public A_CmsCategorizedSelectWidget() {

        super();
    }

    /**
     * Instantiates a new a cms categorized select widget.
     *
     * @param config the config
     */
    public A_CmsCategorizedSelectWidget(String config) {

        super(config);
    }

    /**
     * Gets the configuration.
     *
     * @param cms the cms
     * @param contentValue the content value
     * @param messages the messages
     * @param resource the resource
     * @param contentLocale the content locale
     * @return the configuration
     * @see org.opencms.widgets.I_CmsADEWidget#getConfiguration(org.opencms.file.CmsObject, org.opencms.xml.types.A_CmsXmlContentValue, org.opencms.i18n.CmsMessages, org.opencms.file.CmsResource, java.util.Locale)
     */
    public String getConfiguration(
        CmsObject cms,
        A_CmsXmlContentValue contentValue,
        CmsMessages messages,
        CmsResource resource,
        Locale contentLocale) {

        AutoBean<I_CmsCategorizedSelectData> autoBean = getData(cms, contentValue, messages, resource, contentLocale);
        return AutoBeanCodex.encode(autoBean).getPayload();

    }

    /**
     * Gets the css resource links.
     *
     * @param cms the cms
     * @return the css resource links
     * @see org.opencms.widgets.I_CmsADEWidget#getCssResourceLinks(org.opencms.file.CmsObject)
     */
    public List<String> getCssResourceLinks(CmsObject cms) {

        return null;
    }

    /**
     * Gets the default display type.
     *
     * @return the default display type
     * @see org.opencms.widgets.I_CmsADEWidget#getDefaultDisplayType()
     */
    public DisplayType getDefaultDisplayType() {

        return DisplayType.singleline;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        return null;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getInitCall()
     */
    public String getInitCall() {

        return null;
    }

    /**
     * Gets the java script resource links.
     *
     * @param cms the cms
     * @return the java script resource links
     * @see org.opencms.widgets.I_CmsADEWidget#getJavaScriptResourceLinks(org.opencms.file.CmsObject)
     */
    public List<String> getJavaScriptResourceLinks(CmsObject cms) {

        return null;
    }

    /**
     * Gets the widget name.
     *
     * @return the widget name
     * @see org.opencms.widgets.I_CmsADEWidget#getWidgetName()
     */
    public String getWidgetName() {

        return "org.opencms.widgets.CmsCategorizedSelectWidget";

    }

    /**
     * Checks if is internal.
     *
     * @return true, if is internal
     * @see org.opencms.widgets.I_CmsADEWidget#isInternal()
     */
    public boolean isInternal() {

        return true;
    }

    /**
     * Gets the select option data for the widget.
     *
     * <p>This uses the GWT AutoBean mechanism for serialization. Use the DATA_FACTORY member
     * for creating the relevant AutoBeans.
     *
     * @param cms the cms
     * @param contentValue the content value
     * @param messages the messages
     * @param resource the resource
     * @param contentLocale the content locale
     * @return the data
     */
    protected abstract AutoBean<I_CmsCategorizedSelectData> getData(
        CmsObject cms,
        A_CmsXmlContentValue contentValue,
        CmsMessages messages,
        CmsResource resource,
        Locale contentLocale);

}
