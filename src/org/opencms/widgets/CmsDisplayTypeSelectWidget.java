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
import org.opencms.ade.configuration.CmsResourceTypeConfig;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessages;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.loader.CmsTemplateLoaderFacade;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceMessages;
import org.opencms.xml.containerpage.I_CmsFormatterBean;
import org.opencms.xml.types.A_CmsXmlContentValue;
import org.opencms.xml.types.CmsXmlDisplayFormatterValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Sets;

/**
 * Widget to select a type and formatter combination.<p>
 */
public class CmsDisplayTypeSelectWidget extends CmsSelectWidget {

    /**
     * Formatter select option.<p>
     */
    class FormatterOption {

        /** the display type. */
        String m_displayType;

        /** The option key. */
        String m_key;

        /** The option label. */
        String m_label;

        /** The formatter rank. */
        int m_rank;

        /** Position of resource type in the sitemap config. */
        Integer m_typePosition;

        /** The type name. */
        String m_typeName;

        /**
         * Constructor.<p>
         *
         * @param key the option key
         * @param typeName the type name
         * @param displayType the display type
         * @param label the option label
         * @param rank the formatter rank
         * @param typePos the position of the type in the sitemap config
         */
        FormatterOption(String key, String typeName, String displayType, String label, int rank, Integer typePos) {

            m_key = key;
            m_typeName = typeName;
            m_displayType = displayType;
            m_label = label;
            m_rank = rank;
            m_typePosition = typePos;
        }

        /**
         * Gets the position of the type in the sitemap configuration.
         *
         * @return the position of the type in the sitemap configuration
         */
        public Integer getTypePosition() {

            return m_typePosition;
        }
    }

    /** Name of the sitemap attribute to control whether the old or new way of collecting formatter options should be used. */
    public static final String ATTR_USE_CONFIG_FORMATTERS = "list.use.configured.formatters";

    /** The match display types key. */
    public static final String MATCH_TYPES_KEY = "matchTypes";

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDisplayTypeSelectWidget.class);

    /** The display types split regex. */
    private static final String TYPES_SPLITTER = " *, *";

    /** The display container type configuration. */
    private String m_displayContainerTypeConfig;

    /** Flag indicating display types should be matched. */
    private boolean m_matchTypes;

    /** The configuration String. */
    private String m_config;

    /**
     * @see org.opencms.widgets.A_CmsSelectWidget#getConfiguration()
     */
    @Override
    public String getConfiguration() {

        return m_config;
    }

    /**
     * @see org.opencms.widgets.A_CmsSelectWidget#getConfiguration(org.opencms.file.CmsObject, org.opencms.xml.types.A_CmsXmlContentValue, org.opencms.i18n.CmsMessages, org.opencms.file.CmsResource, java.util.Locale)
     */
    @Override
    public String getConfiguration(
        CmsObject cms,
        A_CmsXmlContentValue schemaType,
        CmsMessages messages,
        CmsResource resource,
        Locale contentLocale) {

        List<FormatterOption> options = getFormatterOptions(cms, resource);
        JSONObject config = new JSONObject();
        try {
            String path;
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(schemaType.getPath())) {
                path = CmsStringUtil.joinPaths(schemaType.getPath(), schemaType.getName());
            } else {
                path = schemaType.getName();
            }

            config.put("valuePath", path);
            config.put("matchTypes", m_matchTypes);
            config.put("emptyLabel", messages.key(Messages.GUI_DISPLAYTYPE_SELECT_0));
            JSONArray optionArray = new JSONArray();
            for (FormatterOption option : options) {
                JSONObject opt = new JSONObject();
                try {
                    opt.put("value", option.m_key);
                    opt.put("label", option.m_label);
                    opt.put("displayType", option.m_displayType);
                    optionArray.put(opt);
                } catch (JSONException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            config.put("options", optionArray);
        } catch (JSONException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return config.toString();
    }

    /**
     * @see org.opencms.widgets.CmsSelectWidget#getWidgetName()
     */
    @Override
    public String getWidgetName() {

        return CmsDisplayTypeSelectWidget.class.getName();
    }

    /**
     * @see org.opencms.widgets.CmsSelectWidget#newInstance()
     */
    @Override
    public I_CmsWidget newInstance() {

        return new CmsDisplayTypeSelectWidget();
    }

    /**
     * @see org.opencms.widgets.A_CmsSelectWidget#setConfiguration(java.lang.String)
     */
    @Override
    public void setConfiguration(String configuration) {

        m_config = configuration;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(configuration)) {
            String[] conf = configuration.split("\\|");
            for (int i = 0; i < conf.length; i++) {
                if (MATCH_TYPES_KEY.equals(conf[i])) {
                    m_matchTypes = true;
                } else {
                    m_displayContainerTypeConfig = conf[i];
                }

            }
        }
    }

    /**
     * @see org.opencms.widgets.A_CmsSelectWidget#parseSelectOptions(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    @Override
    protected List<CmsSelectWidgetOption> parseSelectOptions(
        CmsObject cms,
        I_CmsWidgetDialog widgetDialog,
        I_CmsWidgetParameter param) {

        CmsResource resource = null;
        List<CmsSelectWidgetOption> result = new ArrayList<CmsSelectWidgetOption>();
        try {
            if (widgetDialog instanceof CmsDummyWidgetDialog) {
                resource = ((CmsDummyWidgetDialog)widgetDialog).getResource();
            } else if (widgetDialog instanceof CmsDialog) {
                String sitePath = ((CmsDialog)widgetDialog).getParamResource();
                if (sitePath != null) {
                    resource = cms.readResource(sitePath);
                }
            }
            for (FormatterOption option : getFormatterOptions(cms, resource)) {
                result.add(new CmsSelectWidgetOption(option.m_key, false, option.m_label));
            }
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return result;
    }

    /**
     * Evaluates the display type of the given formatter.<p>
     *
     * @param formatter the formatter configuration bean
     *
     * @return the display type
     */
    private String getDisplayType(I_CmsFormatterBean formatter) {

        return formatter.getDisplayType();
    }

    /**
     * Returns the available formatter options.<p>
     *
     * @param cms the cms context
     * @param resource the edited resource
     *
     * @return the formatter options
     */
    private List<FormatterOption> getFormatterOptions(CmsObject cms, CmsResource resource) {

        List<FormatterOption> options = new ArrayList<FormatterOption>();
        Set<String> containerTypes = new HashSet<>();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_displayContainerTypeConfig)) {
            String types = null;

            if (CmsPropertyDefinition.PROPERTY_TEMPLATE_DISPLAY_TYPES.equals(m_displayContainerTypeConfig)) {
                try {
                    CmsProperty prop = cms.readPropertyObject(
                        resource,
                        CmsPropertyDefinition.PROPERTY_TEMPLATE_DISPLAY_TYPES,
                        true);
                    String propValue = prop.getValue();
                    if (!CmsStringUtil.isEmptyOrWhitespaceOnly(propValue)) {
                        types = propValue;
                    } else {
                        // look up template property
                        try {
                            CmsTemplateLoaderFacade loaderFacade = OpenCms.getResourceManager().getTemplateLoaderFacade(
                                cms,
                                null,
                                resource,
                                CmsPropertyDefinition.PROPERTY_TEMPLATE);
                            CmsResource template = loaderFacade.getLoaderStartResource();
                            if (template != null) {
                                prop = cms.readPropertyObject(
                                    template,
                                    CmsPropertyDefinition.PROPERTY_TEMPLATE_DISPLAY_TYPES,
                                    false);
                                propValue = prop.getValue();
                                if (!CmsStringUtil.isEmptyOrWhitespaceOnly(propValue)) {
                                    types = propValue;
                                }
                            }
                        } catch (Exception ex) {
                            LOG.debug(ex.getMessage(), ex);
                        }
                    }
                } catch (CmsException e) {
                    LOG.warn(e.getLocalizedMessage(), e);
                }
            } else {
                types = m_displayContainerTypeConfig;
            }
            if (types != null) {
                containerTypes.addAll(Arrays.asList(types.split(TYPES_SPLITTER)));
            }
        }
        CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(cms, resource.getRootPath());

        if (config != null) {
            List<CmsResourceTypeConfig> typeConfigs = config.getResourceTypes();
            Map<String, Integer> typePositions = new HashMap<>();
            int index = 0;
            for (CmsResourceTypeConfig type : typeConfigs) {
                typePositions.put(type.getTypeName(), Integer.valueOf(index));
                index += 1;
            }
            boolean useConfiguredFormatters = Boolean.parseBoolean(
                config.getAttribute(ATTR_USE_CONFIG_FORMATTERS, "false"));
            Locale wpLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);

            for (I_CmsFormatterBean formatter : config.getDisplayFormatters(cms)) {

                if (useConfiguredFormatters) {
                    boolean inactive = (formatter.getId() == null)
                        || !config.getActiveFormatters().containsKey(new CmsUUID(formatter.getId()));
                    if (inactive) {
                        continue;
                    }
                } else {
                    if (!containerTypes.isEmpty()) {
                        if (Sets.intersection(containerTypes, formatter.getContainerTypes()).isEmpty()) {
                            continue;
                        }
                    }
                }
                for (String typeName : formatter.getResourceTypeNames()) {
                    String label = formatter.getNiceName(wpLocale)
                        + " ("
                        + CmsWorkplaceMessages.getResourceTypeName(wpLocale, typeName)
                        + ")";
                    options.add(
                        new FormatterOption(
                            typeName + CmsXmlDisplayFormatterValue.SEPARATOR + formatter.getKeyOrId(),
                            typeName,
                            getDisplayType(formatter),
                            label,
                            formatter.getRank(),
                            typePositions.get(typeName)));
                }
            }
        }

        // Formatters for types in sitemap config come first, in the order defined there, then sorted by formatter rank.
        // After that, formatters for types not in the sitemap config, ordered by type, then rank.
        Collections.sort(
            options,
            (
                f1,
                f2) -> ComparisonChain.start().compare(
                    f1.getTypePosition(),
                    f2.getTypePosition(),
                    Comparator.nullsLast(Comparator.naturalOrder())).compare(f1.m_typeName, f2.m_typeName).compare(
                        f2.m_rank,
                        f1.m_rank).compare(f1.m_label, f2.m_label).result()

        );
        return options;
    }
}
