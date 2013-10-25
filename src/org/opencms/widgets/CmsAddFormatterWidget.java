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
import org.opencms.ade.configuration.formatters.CmsFormatterChangeSet;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.I_CmsFormatterBean;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentRootLocation;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;

/**
 * Widget used to select a formatter to add.<p>
 * 
 * Please note that this widget assumes the resource being edited is a sitemap configuration, and will not work correctly in a different context.
 */
public class CmsAddFormatterWidget extends CmsSelectWidget {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsAddFormatterWidget.class);

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
     * @see org.opencms.widgets.A_CmsSelectWidget#parseSelectOptions(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    @Override
    protected List<CmsSelectWidgetOption> parseSelectOptions(
        CmsObject cms,
        I_CmsWidgetDialog widgetDialog,
        I_CmsWidgetParameter param) {

        String path = getResourcePath(cms, widgetDialog);

        try {
            cms = OpenCms.initCmsObject(cms);
            cms.getRequestContext().setSiteRoot("");
            CmsADEConfigData adeConfig = OpenCms.getADEManager().lookupConfiguration(cms, path);
            if (adeConfig.parent() != null) {
                adeConfig = adeConfig.parent();
            }
            Map<CmsUUID, I_CmsFormatterBean> inactiveFormatters = adeConfig.getInactiveFormatters();

            Set<String> added = new HashSet<String>();
            List<CmsSelectWidgetOption> options = Lists.newArrayList();
            for (I_CmsFormatterBean formatterBean : inactiveFormatters.values()) {
                CmsSelectWidgetOption option = new CmsSelectWidgetOption(
                    formatterBean.getId(),
                    false,
                    formatterBean.getNiceName());
                added.add(option.getValue());
                options.add(option);
            }

            Set<String> types = adeConfig.getTypesWithModifiableFormatters();
            Set<String> activeTypes = adeConfig.getTypesWithActiveSchemaFormatters();
            Set<String> inactiveTypes = new HashSet<String>(types);
            inactiveTypes.removeAll(activeTypes);

            for (String inactiveType : inactiveTypes) {
                CmsSelectWidgetOption option = new CmsSelectWidgetOption(
                    CmsFormatterChangeSet.keyForType(inactiveType),
                    false,
                    "Schema: " + inactiveType);
                added.add(option.getValue());
                options.add(option);
            }

            try {
                CmsResource content = cms.readResource(path);
                CmsFile contentFile = cms.readFile(content);
                CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(cms, contentFile);
                CmsConfigurationReader reader = new CmsConfigurationReader(cms);
                CmsXmlContentRootLocation root = new CmsXmlContentRootLocation(xmlContent, Locale.ENGLISH);
                Set<String> addFormatters = reader.parseAddFormatters(root);
                for (String addFormatter : addFormatters) {
                    CmsSelectWidgetOption option = new CmsSelectWidgetOption(addFormatter, false, addFormatter);
                    if (!added.contains(option.getValue())) {
                        options.add(option);
                    }
                }

            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            return options;
        } catch (CmsException e) {
            // should never happen 
            LOG.error(e.getLocalizedMessage(), e);
            return null;
        }
    }
}
