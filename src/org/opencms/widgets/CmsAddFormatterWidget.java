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

package org.opencms.widgets;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.configuration.CmsConfigurationReader;
import org.opencms.file.CmsObject;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.I_CmsFormatterBean;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentRootLocation;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;

/**
 * Widget used to select a formatter to add.<p>
 * 
 * Please note that this widget assumes the resource being edited is a sitemap configuration, and will not work correctly in a different context.
 */
public class CmsAddFormatterWidget extends A_CmsFormatterWidget {

    /**
     * Default constructor.<p>
     */
    public CmsAddFormatterWidget() {

        super();
    }

    /**
     * Constructor with a configuration parameter.<p>
     * @param config the configuration string 
     */
    public CmsAddFormatterWidget(String config) {

        super();
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
     * @see org.opencms.widgets.CmsSelectWidget#newInstance()
     */
    @Override
    public I_CmsWidget newInstance() {

        return new CmsAddFormatterWidget();
    }

    /**
     * @see org.opencms.widgets.A_CmsFormatterWidget#getFormatterOptions(org.opencms.file.CmsObject, org.opencms.ade.configuration.CmsADEConfigData)
     */
    @Override
    protected List<CmsSelectWidgetOption> getFormatterOptions(CmsObject cms, CmsADEConfigData config) {

        Map<CmsUUID, I_CmsFormatterBean> inactiveFormatters = config.getInactiveFormatters();
        List<CmsSelectWidgetOption> result = Lists.newArrayList();
        List<I_CmsFormatterBean> formatters = Lists.newArrayList(inactiveFormatters.values());
        Collections.sort(formatters, new A_CmsFormatterWidget.FormatterSelectComparator());
        for (I_CmsFormatterBean formatterBean : formatters) {
            CmsSelectWidgetOption option = getWidgetOptionForFormatter(cms, formatterBean);
            result.add(option);
        }
        return result;
    }

    /**
     * @see org.opencms.widgets.A_CmsFormatterWidget#getSelectedInFile(org.opencms.ade.configuration.CmsConfigurationReader, org.opencms.xml.content.CmsXmlContent)
     */
    @Override
    protected Set<String> getSelectedInFile(CmsConfigurationReader reader, CmsXmlContent content) {

        CmsXmlContentRootLocation root = new CmsXmlContentRootLocation(content, Locale.ENGLISH);
        Set<String> addFormatters = reader.parseAddFormatters(root);
        return addFormatters;
    }

    /**
     * @see org.opencms.widgets.A_CmsFormatterWidget#getTypeOptions(org.opencms.file.CmsObject, org.opencms.ade.configuration.CmsADEConfigData)
     */
    @Override
    protected List<CmsSelectWidgetOption> getTypeOptions(CmsObject cms, CmsADEConfigData adeConfig) {

        List<CmsSelectWidgetOption> result = Lists.newArrayList();
        Set<String> types = adeConfig.getTypesWithModifiableFormatters();
        Set<String> activeTypes = adeConfig.getTypesWithActiveSchemaFormatters();
        Set<String> inactiveTypes = new HashSet<String>(types);
        inactiveTypes.removeAll(activeTypes);
        for (String inactiveType : inactiveTypes) {
            CmsSelectWidgetOption option = getWidgetOptionForType(cms, inactiveType);
            result.add(option);
        }
        return result;

    }

}
