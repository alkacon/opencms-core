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

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.configuration.CmsConfigurationReader;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsMacroFormatterBean;
import org.opencms.xml.containerpage.I_CmsFormatterBean;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentRootLocation;

import java.util.ArrayList;
import java.util.Collections;
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
public class CmsAddFormatterWidget extends A_CmsFormatterWidget {

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
     * Returns all values that can be selected in the widget.
     * @param cms the current CMS object
     * @param rootPath the root path to the currently edited xml file (sitemap config)
     * @param allRemoved flag, indicating if all inheritedly available formatters should be disabled
     * @return all values that can be selected in the widget.
     */
    public static List<String> getSelectOptionValues(CmsObject cms, String rootPath, boolean allRemoved) {

        try {
            cms = OpenCms.initCmsObject(cms);
            cms.getRequestContext().setSiteRoot("");
            CmsADEConfigData adeConfig = OpenCms.getADEManager().lookupConfiguration(cms, rootPath);
            if (adeConfig.parent() != null) {
                adeConfig = adeConfig.parent();
            }

            List<CmsSelectWidgetOption> options = getFormatterOptionsStatic(cms, adeConfig, rootPath, allRemoved);
            List<CmsSelectWidgetOption> typeOptions = getTypeOptionsStatic(cms, adeConfig, allRemoved);
            options.addAll(typeOptions);
            List<String> result = new ArrayList<String>(options.size());
            for (CmsSelectWidgetOption o : options) {
                result.add(o.getValue());
            }
            return result;
        } catch (CmsException e) {
            // should never happen
            LOG.error(e.getLocalizedMessage(), e);
            return null;
        }

    }

    /**
     * Gets the options corresponding to external (non-schema) formatters.<p>
     *
     * @param cms the CMS context
     * @param config the ADE configuration
     * @param rootPath the root path of the edited file
     * @param allRemoved flag, indicating if all inheritedly available formatters should be disabled
     *
     * @return the select widget options for the external formatters
     */
    protected static List<CmsSelectWidgetOption> getFormatterOptionsStatic(
        CmsObject cms,
        CmsADEConfigData config,
        String rootPath,
        boolean allRemoved) {

        Map<CmsUUID, I_CmsFormatterBean> inactiveFormatters = config.getInactiveFormatters();
        List<CmsSelectWidgetOption> result = Lists.newArrayList();
        List<I_CmsFormatterBean> formatters = Lists.newArrayList(inactiveFormatters.values());
        if (allRemoved) {
            Map<CmsUUID, I_CmsFormatterBean> activeFormatters = config.getActiveFormatters();
            formatters.addAll(Lists.newArrayList(activeFormatters.values()));
        }
        Collections.sort(formatters, new A_CmsFormatterWidget.FormatterSelectComparator());
        for (I_CmsFormatterBean formatterBean : formatters) {

            if (formatterBean instanceof CmsMacroFormatterBean) {
                boolean systemOrShared = formatterBean.getLocation().startsWith(CmsResource.VFS_FOLDER_SYSTEM + "/")
                    || formatterBean.getLocation().startsWith(OpenCms.getSiteManager().getSharedFolder());
                if (!systemOrShared) {
                    String formatterSubSite = CmsResource.getParentFolder(
                        CmsResource.getParentFolder(CmsResource.getParentFolder(formatterBean.getLocation())));
                    String subSite = CmsResource.getParentFolder(CmsResource.getParentFolder(rootPath));
                    if (subSite.equals(formatterSubSite) || !subSite.startsWith(formatterSubSite)) {
                        // we ignore macro formatters that are defined in the current sub site (they are added automatically), or not within a parent site
                        continue;
                    }
                }
            }
            CmsSelectWidgetOption option = getWidgetOptionForFormatter(cms, formatterBean);
            result.add(option);
        }
        return result;

    }

    /**
     * Gets the options corresponding to the schemas which define formatters.<p>
     *
     * @param cms the current CMS context
     * @param adeConfig the ADE configuration
     * @param allRemoved flag, indicating if all inheritedly available formatters should be disabled
     *
     * @return the select widget options for the content types with formatters in the schema
     */
    protected static List<CmsSelectWidgetOption> getTypeOptionsStatic(
        CmsObject cms,
        CmsADEConfigData adeConfig,
        boolean allRemoved) {

        List<CmsSelectWidgetOption> result = Lists.newArrayList();
        Set<String> types = adeConfig.getTypesWithModifiableFormatters();
        Set<String> inactiveTypes = new HashSet<String>(types);
        if (!allRemoved) {
            Set<String> activeTypes = adeConfig.getTypesWithActiveSchemaFormatters();
            inactiveTypes.removeAll(activeTypes);
        }
        for (String inactiveType : inactiveTypes) {
            CmsSelectWidgetOption option = getWidgetOptionForType(cms, inactiveType);
            result.add(option);
        }
        return result;

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
     * @see org.opencms.widgets.A_CmsFormatterWidget#getFormatterOptions(org.opencms.file.CmsObject, org.opencms.ade.configuration.CmsADEConfigData, java.lang.String, boolean)
     */
    @Override
    protected List<CmsSelectWidgetOption> getFormatterOptions(
        CmsObject cms,
        CmsADEConfigData config,
        String rootPath,
        boolean allRemoved) {

        return getFormatterOptionsStatic(cms, config, rootPath, allRemoved);

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
     * @see org.opencms.widgets.A_CmsFormatterWidget#getTypeOptions(org.opencms.file.CmsObject, org.opencms.ade.configuration.CmsADEConfigData, boolean)
     */
    @Override
    protected List<CmsSelectWidgetOption> getTypeOptions(
        CmsObject cms,
        CmsADEConfigData adeConfig,
        boolean allRemoved) {

        return getTypeOptionsStatic(cms, adeConfig, allRemoved);

    }

}
