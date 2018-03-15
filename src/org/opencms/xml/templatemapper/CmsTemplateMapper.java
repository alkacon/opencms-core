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

package org.opencms.xml.templatemapper;

import org.opencms.ade.containerpage.shared.CmsFormatterConfig;
import org.opencms.cache.CmsVfsMemoryObjectCache;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResourceFilter;
import org.opencms.gwt.shared.CmsTemplateContextInfo;
import org.opencms.jsp.util.CmsJspStandardContextBean.TemplateBean;
import org.opencms.loader.CmsTemplateContextManager;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsContainerBean;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.CmsGroupContainerBean;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;

import org.apache.commons.collections.Transformer;
import org.apache.commons.logging.Log;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;

/**
 * Responsible for mapping formatters, containers and settings to different formatters, containers and settings according to
 * the configuration file /system/config/template-mapping.xml.<p>
 *
 */
public final class CmsTemplateMapper {

    /** The configuration file path. */
    private static final String CONFIG_PATH = "/system/config/template-mapping.xml";

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsTemplateMapper.class);

    /** Flag which controls whether this is enabled. */
    private boolean m_enabled;

    /**
     * Hidden default constructor, because this is a singleton.<p>
     */
    private CmsTemplateMapper() {

        m_enabled = true;
    }

    /**
     * Creates a new instance.<p>
     *
     * @param enabled true if the mapper should be enabled
     */
    private CmsTemplateMapper(boolean enabled) {

        m_enabled = enabled;
    }

    /**
     * Gets a template mapper.
     *
     * @return a template mapper
     */
    public static CmsTemplateMapper get() {

        return new CmsTemplateMapper();
    }

    /**
     * Gets the template mapper for the current request.<p>
     *
     * @param request the current request
     *
     * @return the template mapper
     */
    public static CmsTemplateMapper get(ServletRequest request) {

        return new CmsTemplateMapper(isTemplateMapperContext(request));
    }

    /**
     * Checks if the selected template context is "templatemapper".
     *
     * @param request the current request
     * @return true if the selected template context is "templatemapper"
     */
    public static boolean isTemplateMapperContext(ServletRequest request) {

        TemplateBean ctx = (TemplateBean)request.getAttribute(CmsTemplateContextManager.ATTR_TEMPLATE_BEAN);
        boolean enabled = (ctx != null) && "templatemapper".equals(ctx.getName());
        return enabled;
    }

    /**
     * Transforms a container page bean.<p>
     *
     * @param cms the current CMS context
     * @param input the bean to be transformed
     * @param rootPath the root path of the page
     *
     * @return the transformed bean
     */
    public CmsContainerPageBean transformContainerpageBean(CmsObject cms, CmsContainerPageBean input, String rootPath) {

        CmsTemplateMapperConfiguration config = getConfiguration(cms);
        if ((config == null) || !config.isEnabledForPath(rootPath)) {
            return input;
        }
        List<CmsContainerBean> newContainers = new ArrayList<>();
        for (CmsContainerBean container : input.getContainers().values()) {
            List<CmsContainerElementBean> elements = container.getElements();
            List<CmsContainerElementBean> newElements = new ArrayList<>();
            for (CmsContainerElementBean element : elements) {
                CmsContainerElementBean newElement = transformContainerElement(config, element);
                newElements.add(newElement);
            }
            CmsContainerBean newContainer = new CmsContainerBean(
                container.getName(),
                container.getType(),
                container.getParentInstanceId(),
                container.isRootContainer(),
                newElements);
            newContainers.add(newContainer);
        }
        CmsContainerPageBean result = new CmsContainerPageBean(newContainers);
        return result;
    }

    /**
     * Transforms a container element bean used for detail elements.<p>
     *
     * @param cms the current CMS context
     * @param input the bean to be transformed
     * @param rootPath the root path of the page
     *
     * @return the transformed bean
     */
    public CmsContainerElementBean transformDetailElement(
        CmsObject cms,
        CmsContainerElementBean input,
        String rootPath) {

        CmsTemplateMapperConfiguration config = getConfiguration(cms);
        if ((config == null) || !config.isEnabledForPath(rootPath)) {
            return input;
        }
        return transformContainerElement(config, input);

    }

    /**
     * Transforms a group container bean.<p>
     *
     * @param cms the current CMS context
     * @param input the input bean to be transformed
     * @param rootPath the root path of the container page
     *
     * @return the transformed bean
     */
    public CmsGroupContainerBean transformGroupContainer(CmsObject cms, CmsGroupContainerBean input, String rootPath) {

        CmsTemplateMapperConfiguration config = getConfiguration(cms);
        if ((config == null) || !config.isEnabledForPath(rootPath)) {
            return input;
        }
        List<CmsContainerElementBean> newElements = new ArrayList<>();
        for (CmsContainerElementBean element : input.getElements()) {
            CmsContainerElementBean newElement = transformContainerElement(config, element);
            newElements.add(newElement);
        }
        CmsGroupContainerBean result = new CmsGroupContainerBean(
            input.getTitle(),
            input.getDescription(),
            newElements,
            input.getTypes());
        return result;
    }

    /**
     * Helper method to transform a single container element.<p>
     *
     * @param config the configuration
     * @param element the container element to be transformed
     *
     * @return the transformed bean
     */
    protected CmsContainerElementBean transformContainerElement(
        CmsTemplateMapperConfiguration config,
        CmsContainerElementBean element) {

        Map<String, String> settings = element.getIndividualSettings();
        if (settings == null) {
            settings = new HashMap<>();
        }
        Map<String, String> newSettings = new HashMap<>(settings);
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            String key = entry.getKey();
            if (CmsTemplateContextInfo.SETTING.equals(key)) {
                continue;
            }
            String value = entry.getValue();
            String newValue = value;
            if (key.startsWith(CmsFormatterConfig.FORMATTER_SETTINGS_KEY)) {
                if (CmsUUID.isValidUUID(value)) {
                    String newId = config.getMappedFormatterConfiguration(value);
                    if (newId != null) {
                        newValue = newId;
                    }
                } else if (value.startsWith(CmsFormatterConfig.SCHEMA_FORMATTER_ID)) {
                    String schemaFormatterIdStr = value.substring(CmsFormatterConfig.SCHEMA_FORMATTER_ID.length());
                    if (CmsUUID.isValidUUID(schemaFormatterIdStr)) {
                        CmsUUID schemaFormatterId = new CmsUUID(schemaFormatterIdStr);
                        CmsUUID mappedFormatterId = config.getMappedFormatterJspId(schemaFormatterId);
                        if (mappedFormatterId != null) {
                            newValue = CmsFormatterConfig.SCHEMA_FORMATTER_ID + mappedFormatterId;
                        }
                    }
                }
            }
            newSettings.put(key, newValue);
        }
        CmsContainerElementBean newElement = element.clone();
        newElement.updateIndividualSettings(newSettings);
        CmsUUID formatterId = element.getFormatterId();
        CmsUUID mappedFormatterJspId = config.getMappedFormatterJspId(formatterId);
        if (mappedFormatterJspId != null) {
            newElement.setFormatterId(mappedFormatterJspId);
        }
        return newElement;
    }

    /**
     * Loads the configuration file, using  CmsVfsMemoryObjectCache for caching.
     *
     * @param cms the CMS context
     * @return the template mapper configuration
     */
    private CmsTemplateMapperConfiguration getConfiguration(final CmsObject cms) {

        if (!m_enabled) {
            return CmsTemplateMapperConfiguration.EMPTY_CONFIG;
        }

        return (CmsTemplateMapperConfiguration)(CmsVfsMemoryObjectCache.getVfsMemoryObjectCache().loadVfsObject(
            cms,
            CONFIG_PATH,
            new Transformer() {

                @Override
                public Object transform(Object input) {

                    try {
                        CmsFile file = cms.readFile(CONFIG_PATH, CmsResourceFilter.IGNORE_EXPIRATION);
                        SAXReader saxBuilder = new SAXReader();
                        try (ByteArrayInputStream stream = new ByteArrayInputStream(file.getContents())) {
                            Document document = saxBuilder.read(stream);
                            CmsTemplateMapperConfiguration config = new CmsTemplateMapperConfiguration(cms, document);
                            return config;
                        }
                    } catch (Exception e) {
                        LOG.warn(e.getLocalizedMessage(), e);
                        return new CmsTemplateMapperConfiguration(); // empty configuration, does not do anything
                    }

                }
            }));
    }

}
