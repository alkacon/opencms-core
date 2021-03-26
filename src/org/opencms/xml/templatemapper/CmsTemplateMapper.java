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
import org.opencms.loader.CmsTemplateContext;
import org.opencms.loader.CmsTemplateContextManager;
import org.opencms.loader.I_CmsTemplateContextProvider;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsContainerBean;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.CmsGroupContainerBean;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    /** The logger instance for this class. */
    static final Log LOG = CmsLog.getLog(CmsTemplateMapper.class);

    /** Flag which controls whether this is enabled. */
    protected boolean m_enabled;

    /** The path to the mapper configuration. */
    protected String m_configPath;

    /** Flag to enable mode for saving. */
    private boolean m_forSave;

    /**
     * Creates a new instance.<p>
     *
     * @param configPath the template mapper configuration VFS path
     */
    public CmsTemplateMapper(String configPath) {

        if (configPath != null) {
            m_enabled = true;
            m_configPath = configPath;
        } else {
            m_enabled = false;
        }
    }

    /**
     * Hidden default constructor, because this is a singleton.<p>
     */
    private CmsTemplateMapper() {

        m_enabled = true;
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

        return new CmsTemplateMapper(getTemplateMapperConfig(request));
    }

    /**
     * Checks if the selected template context is "templatemapper".
     *
     * @param request the current request
     * @return true if the selected template context is "templatemapper"
     */
    public static String getTemplateMapperConfig(ServletRequest request) {

        String result = null;
        CmsTemplateContext templateContext = (CmsTemplateContext)request.getAttribute(
            CmsTemplateContextManager.ATTR_TEMPLATE_CONTEXT);
        if (templateContext != null) {
            I_CmsTemplateContextProvider provider = templateContext.getProvider();
            if (provider instanceof I_CmsTemplateMappingContextProvider) {
                result = ((I_CmsTemplateMappingContextProvider)provider).getMappingConfigurationPath(
                    templateContext.getKey());
            }
        }
        return result;
    }

    /**
     * Sets the for-save mode.<p>
     *
     * @param forSave true if for-save mode should be enabled
     */
    public void setForSave(boolean forSave) {

        m_forSave = forSave;
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
                CmsContainerElementBean newElement = transformContainerElement(cms, config, element);
                if (newElement != null) {
                    newElements.add(newElement);
                }
            }
            CmsContainerBean newContainer = container.copyWithNewElements(newElements);
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
        return transformContainerElement(cms, config, input);

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
            CmsContainerElementBean newElement = transformContainerElement(cms, config, element);
            if (newElement != null) {
                newElements.add(newElement);
            }
        }
        Set<String> transformedTypes = new HashSet<>();
        Set<String> oldTypes = input.getTypes();
        if (oldTypes == null) {
            oldTypes = new HashSet<>();
        }
        for (String type : oldTypes) {
            String newType = config.getMappedElementGroupType(type);
            if (newType == null) {
                newType = type;
            }
            transformedTypes.add(newType);
        }

        CmsGroupContainerBean result = new CmsGroupContainerBean(
            input.getTitle(),
            input.getDescription(),
            newElements,
            transformedTypes);
        return result;
    }

    /**
     * Helper method to transform a single container element.<p>
     * @param cms the CMS context
     * @param config the configuration
     * @param element the container element to be transformed
     *
     * @return the transformed bean
     */
    protected CmsContainerElementBean transformContainerElement(
        CmsObject cms,
        CmsTemplateMapperConfiguration config,
        CmsContainerElementBean element) {

        if (m_forSave) {
            try {
                element.initResource(cms);
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
                return null;
            }
        }
        Map<String, String> settings = element.getIndividualSettings();
        if (settings == null) {
            settings = new HashMap<>();
        }
        Map<String, String> newSettings = new HashMap<>();
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            String key = entry.getKey();
            if (CmsTemplateContextInfo.SETTING.equals(key)) {
                continue;
            }
            String value = entry.getValue();
            if (value == null) {
                continue;
            }
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
        if ((formatterId == null) && m_forSave) {
            try {
                if (element.isGroupContainer(cms)) {
                    // ID for group-container.jsp
                    formatterId = new CmsUUID("e7029fa2-761e-11e0-bd7f-9ffeadaf4d46");
                    newElement.setFormatterId(formatterId);
                } else {
                    if (OpenCms.getResourceManager().matchResourceType("function", element.getResource().getTypeId())) {
                        formatterId = new CmsUUID("087ba7c9-e7fc-4336-acb8-d3416a4eb1fd");
                        newElement.setFormatterId(formatterId);
                    }
                }
            } catch (CmsException e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
        }
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

        if (m_configPath == null) {
            m_configPath = OpenCms.getSystemInfo().getConfigFilePath(cms, "template-mapping.xml");
        }

        return (CmsTemplateMapperConfiguration)(CmsVfsMemoryObjectCache.getVfsMemoryObjectCache().loadVfsObject(
            cms,
            m_configPath,
            new Transformer() {

                @Override
                public Object transform(Object input) {

                    try {
                        CmsFile file = cms.readFile(m_configPath, CmsResourceFilter.IGNORE_EXPIRATION);
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
