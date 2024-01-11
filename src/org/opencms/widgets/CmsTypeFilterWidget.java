/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsMessages;
import org.opencms.loader.CmsResourceManager;
import org.opencms.main.OpenCms;
import org.opencms.xml.types.A_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.google.common.base.Joiner;

/**
 * A widget for selecting resource types which are direct editable.
 */
public class CmsTypeFilterWidget extends CmsFilterSelectWidget {

    /**
     * Default constructor.<p>
     */
    public CmsTypeFilterWidget() {

        super();
    }

    /**
     * Constructor with a configuration parameter.<p>
     * @param config the configuration string
     */
    public CmsTypeFilterWidget(String config) {

        super();
        super.setConfiguration(createConfiguration());

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

        return CmsSelectWidgetOption.createConfigurationString(
            CmsSelectWidgetOption.parseOptions(createConfiguration()));
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
     * @see org.opencms.widgets.CmsSelectWidget#getWidgetName()
     */
    @Override
    public String getWidgetName() {

        return CmsFilterSelectWidget.class.getName();
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

        return new CmsTypeFilterWidget("");
    }

    /**
     * @see org.opencms.widgets.A_CmsWidget#setConfiguration(java.lang.String)
     */
    @Override
    public void setConfiguration(String config) {

        super.setConfiguration(createConfiguration());
    }

    /**
     * Internal method to create the configuration string from the available types.<p>
     *
     * @return the configuration string
     */
    private String createConfiguration() {

        CmsResourceManager resManager = OpenCms.getResourceManager();
        if (resManager == null) {
            // can happen during the OpenCms startup
            return "";
        }
        List<I_CmsResourceType> resTypes = resManager.getResourceTypes();
        List<String> options = new ArrayList<String>();
        for (I_CmsResourceType resType : resTypes) {
            if (resType.isDirectEditable()) {
                options.add(resType.getTypeName());
            }
        }
        Collections.sort(options);
        return Joiner.on("|").join(options);
    }

}
