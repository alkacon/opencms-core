/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.jsp.util;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.containerpage.shared.CmsFormatterConfig;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.flex.CmsFlexController;
import org.opencms.jsp.CmsJspTagContainer;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsContainerBean;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.CmsFormatterConfiguration;
import org.opencms.xml.containerpage.I_CmsFormatterBean;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * Wrapper for using container pages in JSPs.
 */
public class CmsJspContainerPageWrapper {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspContainerPageWrapper.class);

    /** The wrapped container page bean. */
    private CmsContainerPageBean m_page;

    /** The CMS context. */
    private CmsObject m_cms;

    /**
     * Creates a new instance.
     *
     * @param page the container page to wrap
     */
    public CmsJspContainerPageWrapper(CmsObject cms, CmsContainerPageBean page) {

        m_cms = cms;
        m_page = page;

    }

    /**
     * Checks if the page contains an element with a given id.
     *
     * @param elementId the element id to check
     * @return true if the page contains the element
     */
    public boolean containsElement(CmsUUID elementId) {

        return m_page.containsElement(elementId);
    }

    /**
     * Gets the containers.
     *
     * @return the containers
     */
    public Map<String, CmsContainerBean> getContainers() {

        return m_page.getContainers();
    }

    /**
     * Returns a map which tracks which element ids are part of the page.
     *
     * @return a map from element ids to their page membership
     */
    public Map<CmsUUID, Boolean> getContainsElement() {

        return m_page.getContainsElement();
    }

    /**
     * Gets the element ids.
     *
     * @return the container element ids
     */
    public List<CmsUUID> getElementIds() {

        return m_page.getElementIds();
    }

    /**
     * Gets the container elements
     * @return the container elements
     */
    public List<CmsContainerElementBean> getElements() {

        return m_page.getElements();
    }

    /**
     * Gets the container names.
     *
     * @return the container names
     */
    public List<String> getNames() {

        return m_page.getNames();
    }

    /**
     * Gets the element settings for the element with the specific instance id.
     *
     * <p>The returned map contains the setting names as keys and the corresponding setting values as wrapper objects.
     *
     * @param elementInstanceId the element instance id
     * @return the map of setting wrappers
     */
    public Map<?, ?> getSettingsForElement(String elementInstanceId) {

        for (CmsContainerBean container : m_page.getContainers().values()) {
            for (CmsContainerElementBean element : container.getElements()) {
                if (Objects.equals(element.getInstanceId(), elementInstanceId)) {
                    String containerName = container.getName();
                    Map<String, String> settings = element.getSettings();
                    I_CmsFormatterBean formatter = null;
                    CmsADEConfigData config = OpenCms.getADEManager().lookupConfigurationWithCache(
                        m_cms,
                        m_cms.getRequestContext().addSiteRoot(m_cms.getRequestContext().getUri()));

                    if (settings != null) {
                        String formatterConfigId = settings.get(
                            CmsFormatterConfig.getSettingsKeyForContainer(containerName));
                        I_CmsFormatterBean dynamicFmt = config.findFormatter(formatterConfigId);
                        if (dynamicFmt != null) {
                            formatter = dynamicFmt;
                        }
                    }
                    if (formatter == null) {
                        try {
                            CmsResource resource = m_cms.readResource(
                                element.getId(),
                                CmsResourceFilter.ignoreExpirationOffline(m_cms));
                            CmsFormatterConfiguration formatters = config.getFormatters(m_cms, resource);
                            int width = -2;
                            try {
                                width = Integer.parseInt(container.getWidth());
                            } catch (Exception e) {
                                LOG.debug(e.getLocalizedMessage(), e);
                            }
                            formatter = formatters.getDefaultSchemaFormatter(container.getType(), width);
                        } catch (CmsException e1) {
                            if (LOG.isWarnEnabled()) {
                                LOG.warn(e1.getLocalizedMessage(), e1);
                            }
                        } catch (Exception e) {
                            LOG.error(e.getLocalizedMessage(), e);
                        }
                    }

                    final I_CmsFormatterBean finalFormatter = formatter;

                    Map<String, CmsXmlContentProperty> formatterSettingsConfig = OpenCms.getADEManager().getFormatterSettings(
                        m_cms,
                        config,
                        formatter,
                        element.getResource(),
                        m_cms.getRequestContext().getLocale(),
                        null);
                    Set<String> keys = new HashSet<>(element.getSettings().keySet());
                    Map<String, CmsJspElementSettingValueWrapper> result = new HashMap<>();
                    for (String key : keys) {
                        String value = element.getSettings().get(key);
                        boolean exists;
                        if (finalFormatter != null) {
                            exists = (formatterSettingsConfig.get(key) != null)
                                || finalFormatter.getSettings(config).containsKey(key);
                        } else {
                            exists = value != null;
                        }
                        result.put(
                            key,
                            new CmsJspElementSettingValueWrapper(m_cms, element.getSettings().get(key), exists));
                    }
                    return result;
                }
            }
        }
        return CmsJspContentAccessBean.CONSTANT_NULL_VALUE_WRAPPER_MAP;
    }

    public Set<String> getTypes() {

        return m_page.getTypes();
    }

    /**
     * Renders the element in the container with the given name or name prefix.
     * @param context the context bean
     * @param name the container name or name prefix
     * @return the rendered HTML
     */
    public String renderContainer(CmsJspStandardContextBean context, String name) {

        CmsContainerBean container = findContainer(name);
        if (container == null) {
            return null;
        }
        return render(context, container);
    }

    /**
     * Helper method for locating a container with the given name or name prefix.
     * @param name the name or name prefix
     * @return the container, or null if none were found
     */
    private CmsContainerBean findContainer(String name) {

        CmsContainerBean result = m_page.getContainers().get(name);
        if (result == null) {
            for (Map.Entry<String, CmsContainerBean> entry : m_page.getContainers().entrySet()) {
                if (entry.getKey().endsWith("-" + name)) {
                    result = entry.getValue();
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Renders the elements from the given container as HTML and returns it.
     *
     * @param context the context bean
     * @param container the container whose elements should be rendered
     * @return the HTML of the container elements, without a surrounding element
     */
    private String render(CmsJspStandardContextBean context, CmsContainerBean container) {

        CmsFlexController controller = CmsFlexController.getController(context.getRequest());
        CmsObject m_cms = context.getCmsObject();
        CmsContainerBean oldContainer = context.getContainer();
        CmsContainerElementBean oldElement = context.getRawElement();
        CmsContainerPageBean oldPage = context.getPage();
        boolean oldForceDisableEdit = context.isForceDisableEditMode();
        Locale locale = m_cms.getRequestContext().getLocale();
        context.getRequest();
        try {
            context.setContainer(container);
            context.setPage(m_page);
            // The forceDisableEditMode flag may be incorrectly cached in the standard
            // context bean copies stored in flex cache entries, but it doesn't matter since edit mode is never
            // active in the Online project anyway
            context.setForceDisableEditMode(true);

            int containerWidth = -1;
            try {
                containerWidth = Integer.parseInt(container.getWidth());
            } catch (Exception e) {
                LOG.debug(e.getLocalizedMessage(), e);
            }
            CmsADEConfigData adeConfig = context.getSitemapConfigInternal();
            StringBuilder buffer = new StringBuilder();
            for (CmsContainerElementBean element : container.getElements()) {

                try {
                    element.initResource(m_cms);
                    I_CmsFormatterBean formatterBean = CmsJspTagContainer.ensureValidFormatterSettings(
                        m_cms,
                        element,
                        adeConfig,
                        container.getName(),
                        container.getType(),
                        containerWidth);
                    element.initSettings(m_cms, adeConfig, formatterBean, locale, controller.getCurrentRequest(), null);
                    context.setElement(element);
                    CmsResource formatterRes = m_cms.readResource(
                        formatterBean.getJspStructureId(),
                        CmsResourceFilter.IGNORE_EXPIRATION);
                    byte[] formatterOutput = OpenCms.getResourceManager().getLoader(formatterRes).dump(
                        m_cms,
                        formatterRes,
                        null,
                        locale,
                        controller.getCurrentRequest(),
                        controller.getCurrentResponse());
                    String encoding = controller.getCurrentResponse().getEncoding();
                    String formatterOutputStr = new String(formatterOutput, encoding);
                    buffer.append(formatterOutputStr);
                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            String resultHtml = buffer.toString();
            return resultHtml;
        } finally {
            context.setPage(oldPage);
            context.setContainer(oldContainer);
            context.setElement(oldElement);
            context.setForceDisableEditMode(oldForceDisableEdit);
        }

    }

}
