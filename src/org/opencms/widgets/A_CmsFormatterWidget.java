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
import org.opencms.ade.configuration.formatters.CmsFormatterConfigurationCacheState;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplaceMessages;
import org.opencms.xml.containerpage.I_CmsFormatterBean;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;

/**
 * Abstract superclass for widgets used to enable or disable formatters.<p>
 */
public abstract class A_CmsFormatterWidget extends CmsSelectWidget {

    /**
     * Comparator used to sort formatter beans in the order in which they should be displayed in the selection.<p>
     */
    public static class FormatterSelectComparator implements Comparator<I_CmsFormatterBean> {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(I_CmsFormatterBean first, I_CmsFormatterBean second) {

            return ComparisonChain.start().compare(first.getResourceTypeName(), second.getResourceTypeName()).compare(
                first.getRank(),
                second.getRank()).result();
        }
    }

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(A_CmsFormatterWidget.class);

    /**
     * Creates a widget option corresponding to a formatter bean for an external formatter.<p>
     * 
     * @param cms the current CMS context 
     * @param formatter the formatter bean 
     * 
     * @return the select option which was created 
     */
    public static CmsSelectWidgetOption getWidgetOptionForFormatter(CmsObject cms, I_CmsFormatterBean formatter) {

        String name = formatter.getNiceName()
            + " ["
            + formatter.getResourceTypeName()
            + "]  "
            + " ("
            + formatter.getJspRootPath()
            + ")";
        CmsSelectWidgetOption option = new CmsSelectWidgetOption(formatter.getId(), false, name);
        return option;
    }

    /**
     * Creates a widget option for a resource type.<p>
     * 
     * @param cms the current CMS context 
     * @param typeName the type for which we want a widget option
     *  
     * @return the created widget option 
     */
    public static CmsSelectWidgetOption getWidgetOptionForType(CmsObject cms, String typeName) {

        String niceTypeName = typeName;
        try {
            Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
            niceTypeName = CmsWorkplaceMessages.getResourceTypeName(locale, typeName);
        } catch (Exception e) {
            // resource type name will be used as a fallback
        }
        CmsSelectWidgetOption option = new CmsSelectWidgetOption(
            CmsFormatterChangeSet.keyForType(typeName),
            false,
            getMessage(cms, Messages.GUI_SCHEMA_FORMATTER_OPTION_1, niceTypeName));
        return option;
    }

    /**
     * Gets a message string.<p>
     * 
     * @param cms the CMS context 
     * @param message the message key  
     * @param args the message arguments 
     * 
     * @return the message string 
     */
    static String getMessage(CmsObject cms, String message, Object... args) {

        Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        return Messages.get().getBundle(locale).key(message, args);
    }

    /** 
     * Gets the options corresponding to external (non-schema) formatters.<p>
     * 
     * @param cms the CMS context 
     * @param config the ADE configuration 
     * 
     * @return the select widget options for the external formatters 
     */
    protected abstract List<CmsSelectWidgetOption> getFormatterOptions(CmsObject cms, CmsADEConfigData config);

    /** 
     * Gets the values which have already been selected in the edited resource on the VFS.<p>
     * 
     * @param reader a sitemap configuration reader 
     * @param content the unmarshalled content
     * 
     * @return the set of values which have already been selected 
     */
    protected abstract Set<String> getSelectedInFile(CmsConfigurationReader reader, CmsXmlContent content);

    /**
     * Gets the options corresponding to the schemas which define formatters.<p>
     * 
     * @param cms the current CMS context 
     * @param config the ADE configuration
     *  
     * @return the select widget options for the content types with formatters in the schema 
     */
    protected abstract List<CmsSelectWidgetOption> getTypeOptions(CmsObject cms, CmsADEConfigData config);

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
            Set<String> added = new HashSet<String>();
            List<CmsSelectWidgetOption> options = Lists.newArrayList();
            options.add(new CmsSelectWidgetOption("", true, getMessage(cms, Messages.GUI_FORMATTER_EMPTY_SELECTION_0)));
            List<CmsSelectWidgetOption> formatterOptions = getFormatterOptions(cms, adeConfig);
            options.addAll(formatterOptions);
            List<CmsSelectWidgetOption> typeOptions = getTypeOptions(cms, adeConfig);
            options.addAll(typeOptions);
            for (CmsSelectWidgetOption option : options) {
                added.add(option.getValue());
            }
            try {
                CmsResource content = cms.readResource(path);
                CmsFile contentFile = cms.readFile(content);
                CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(cms, contentFile);
                CmsConfigurationReader reader = new CmsConfigurationReader(cms);
                Set<String> selected = getSelectedInFile(reader, xmlContent);
                for (String formatterKey : selected) {
                    String title = formatterKey;
                    if (CmsUUID.isValidUUID(formatterKey)) {
                        CmsFormatterConfigurationCacheState cacheState = OpenCms.getADEManager().getCachedFormatters(
                            cms.getRequestContext().getCurrentProject().isOnlineProject());
                        CmsUUID mapKey = new CmsUUID(formatterKey);
                        I_CmsFormatterBean formatter = cacheState.getFormatters().get(mapKey);
                        if (formatter != null) {
                            title = A_CmsFormatterWidget.getWidgetOptionForFormatter(cms, formatter).getOption();
                        }
                    }
                    CmsSelectWidgetOption option = new CmsSelectWidgetOption(formatterKey, false, title);
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
