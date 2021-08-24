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

package org.opencms.xml.xml2json.renderer;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.configuration.CmsFormatterUtils;
import org.opencms.ade.containerpage.shared.CmsFormatterConfig;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsContainerBean;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.CmsXmlContainerPage;
import org.opencms.xml.containerpage.CmsXmlContainerPageFactory;
import org.opencms.xml.containerpage.I_CmsFormatterBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.commons.logging.Log;

/**
 * Used for rendering container pages as a JSON structure.
 */
public class CmsJsonRendererContainerPage {

    /**
     * Tree node wrapper for a container.
     */
    public class ContainerNode {

        /** The container bean. */
        private CmsContainerBean m_container;

        /** List of nodes corresponding to container elements. */
        private List<ElementNode> m_elements = new ArrayList<>();

        /**
         * Creates a new node for the given container.
         *
         * @param container the container bean
         */
        public ContainerNode(CmsContainerBean container) {

            m_container = container;
        }

        /**
         * Adds a container element subnode.
         *
         * @param elemNode the container element node
         */
        public void add(ElementNode elemNode) {

            m_elements.add(elemNode);
        }

        /**
         * Gets the container bean.
         *
         * @return the container bean
         */
        public CmsContainerBean getContainer() {

            return m_container;
        }

        /**
         * Gets the nodes corresponding to the container elements.
         *
         * @return the nodes for the container elements
         */
        public List<ElementNode> getElements() {

            return Collections.unmodifiableList(m_elements);
        }

        /**
         * Gets the container name.
         *
         * @return the container name
         */
        public String getName() {

            return m_container.getName();
        }

        /**
         * Returns the container type.
         *
         * @return the container type
         */
        public String getType() {

            return m_container.getType();
        }

        /**
         * Returns whether this container is a detail only container.
         *
         * @return whether a detail only container or not
         */
        public boolean isDetailOnlyContainer() {

            return m_container.isDetailOnly();
        }

        /**
         * Returns whether this container is a nested container.
         *
         * @return whether a nested container or not
         */
        public boolean isNestedContainer() {

            return m_container.isNestedContainer();
        }

        /**
         * Returns whether this container is a root container.
         *
         * @return whether a root container or not
         */
        public boolean isRootContainer() {

            return m_container.isRootContainer();
        }
    }

    /**
     * Tree node wrapper around a container element.
     */
    public class ElementNode {

        /** The map of sub-containers by name. */
        private Map<String, ContainerNode> m_containers = new HashMap<>();

        /** The element. */
        private CmsContainerElementBean m_element;

        /** The parent container. */
        private ContainerNode m_parentContainerNode;

        /**
         * Creates a new element node.
         *
         * @param elementBean the container element bean
         * @param parentContainerNode the parent container node
         */
        public ElementNode(CmsContainerElementBean elementBean, ContainerNode parentContainerNode) {

            m_element = elementBean;
            m_parentContainerNode = parentContainerNode;
        }

        /**
         * Adds the container node as a nested container for the element.
         *
         * @param containerNode the container node
         */
        public void add(ContainerNode containerNode) {

            m_containers.put(containerNode.getName(), containerNode);
        }

        /**
         * Gets the nested containers for this element as a map, with container names as keys.
         *
         * @return the map of nested containers
         */
        public Map<String, ContainerNode> getContainers() {

            return Collections.unmodifiableMap(m_containers);
        }

        /**
         * Gets the container element bean which this node is wrapping.
         *
         * @return the container element bean
         */
        public CmsContainerElementBean getElement() {

            return m_element;
        }

        /**
         * Returns the parent container node.
         *
         * @return the parent container node
         */
        public ContainerNode getParentContainerNode() {

            return m_parentContainerNode;
        }

    }

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJsonRendererContainerPage.class);

    /** The CMS context used for VFS operations. */
    private CmsObject m_cms;

    /** The container page. */
    private CmsResource m_page;

    /** The property filter. */
    private Predicate<String> m_propFilter;

    /**
     * Creates a new renderer instance.
     *
     * @param cms the CMS context
     * @param page the container page to render
     * @param propertyFilter the property filter
     */
    public CmsJsonRendererContainerPage(CmsObject cms, CmsResource page, Predicate<String> propertyFilter) {

        m_cms = cms;
        m_page = page;
        m_propFilter = propertyFilter;
    }

    /**
     * Builds a tree from the given container page bean.<p>
     *
     * The returned tree consists of container nodes (which have children corresponding the container elements) and element nodes
     * (which have children corresponding to the nested containers of the element). The root of the tree is a dummy element node
     * which does not correspond to any element in the page, but just acts as a container for the top-level containers of the page.
     *
     * @param page the container page bean
     * @param rootPath the root path of the container page
     * @return the dummy root element node
     */
    public ElementNode buildTree(CmsContainerPageBean page, String rootPath) {

        Map<String, ElementNode> elementsByInstanceId = new HashMap<>();
        List<ContainerNode> containerNodes = new ArrayList<>();
        CmsADEConfigData adeConfig = OpenCms.getADEManager().lookupConfiguration(m_cms, rootPath);

        for (CmsContainerBean container : page.getContainers().values()) {
            ContainerNode containerNode = new ContainerNode(container);
            containerNodes.add(containerNode);
            for (CmsContainerElementBean cachedElementBean : container.getElements()) {
                CmsContainerElementBean elementBean = cachedElementBean.clone();
                try {
                    elementBean.initResource(m_cms);
                } catch (CmsException e) {
                    // Skip elements whose resources can't be read
                    LOG.warn(e.getLocalizedMessage(), e);
                    continue;
                }
                ElementNode elemNode = new ElementNode(elementBean, containerNode);
                if (elementBean.getInstanceId() != null) {
                    elementsByInstanceId.put(elementBean.getInstanceId(), elemNode);
                }
                I_CmsFormatterBean formatter = getFormatter(m_cms, container, elementBean, adeConfig);
                elementBean.initSettings(m_cms, adeConfig, formatter, Locale.ENGLISH, null, new HashMap<>());
                containerNode.add(elemNode);
            }
        }

        ElementNode rootElement = new ElementNode(null, null); // Dummy element containing all the top-level containers
        for (ContainerNode containerNode : containerNodes) {
            CmsContainerBean container = containerNode.getContainer();
            String parentId = container.getParentInstanceId();
            ElementNode parentElement = CmsStringUtil.isEmpty(parentId) ? null : elementsByInstanceId.get(parentId);
            if (parentElement == null) {
                rootElement.add(containerNode);
            } else {
                parentElement.add(containerNode);
            }

        }
        return rootElement;

    }

    /**
     * Renders the JSON for the container page.
     *
     * @return the JSON for the container page
     * @throws Exception if something goes wrong
     */
    public Object renderJson() throws Exception {

        CmsFile file = m_cms.readFile(m_page);
        CmsXmlContainerPage page = CmsXmlContainerPageFactory.unmarshal(m_cms, file);
        CmsContainerPageBean pageBean = page.getContainerPage(m_cms);
        ElementNode root = buildTree(pageBean, file.getRootPath());
        return elementToJson(root);
    }

    /**
     * Renders a container node as JSON.
     *
     * @param containerNode the container node
     * @return the JSON for the node
     * @throws JSONException if something goes wrong with JSON processing
     */
    JSONObject containerToJson(ContainerNode containerNode) throws JSONException {

        JSONObject result = new JSONObject();
        result.put("name", containerNode.getName());
        result.put("type", containerNode.getType());
        result.put("isDetailOnlyContainer", containerNode.isDetailOnlyContainer());
        result.put("isNestedContainer", containerNode.isNestedContainer());
        result.put("isRootContainer", containerNode.isRootContainer());
        JSONArray elemJson = new JSONArray();
        for (ElementNode elemNode : containerNode.getElements()) {
            elemJson.put(elementToJson(elemNode));
        }
        result.put("elements", elemJson);
        return result;
    }

    /**
     * Renders an element node as JSON.
     *
     * @param elementNode the element node
     * @return the JSON for the element
     * @throws JSONException if something goes wrong
     */
    JSONObject elementToJson(ElementNode elementNode) throws JSONException {

        JSONObject result = new JSONObject();
        if (elementNode.getElement() != null) {
            result.put("path", elementNode.getElement().getResource().getRootPath());
            // new container page format has property formatterKey
            String formatterKey = CmsFormatterUtils.getFormatterKey(
                elementNode.getParentContainerNode().getName(),
                elementNode.getElement());
            result.put("formatterKey", formatterKey);
            JSONObject settings = new JSONObject();
            for (Map.Entry<String, String> entry : elementNode.getElement().getSettings().entrySet()) {
                // formatterSettings and element_instance_id setting have become obsolete in the new container page format
                if (entry.getKey().startsWith("formatterSettings") || entry.getKey().equals("element_instance_id")) {
                    continue;
                }
                settings.put(entry.getKey(), entry.getValue());
            }
            result.put("settings", settings);
        }
        JSONArray containers = new JSONArray();
        for (ContainerNode containerNode : elementNode.getContainers().values()) {
            containers.put(containerToJson(containerNode));
        }
        result.put("containers", containers);
        return result;
    }

    /**
     * Helper method for getting the formatter bean for a container element.
     *
     * <p>This only relies on the container element data itself since container width/type are not stored with the container,
     * so it may return null if no formatter is set as an element setting.
     *
     * @param cms the CMS context
     * @param container the container in which the element is located
     * @param elementBean  the element bean
     * @param adeConfig the ADE configuration
     * @return the formatter bean
     */
    private I_CmsFormatterBean getFormatter(
        CmsObject cms,
        CmsContainerBean container,
        CmsContainerElementBean elementBean,
        CmsADEConfigData adeConfig) {

        Collection<I_CmsFormatterBean> formatterList = adeConfig.getCachedFormatters().getFormattersForType(
            OpenCms.getResourceManager().getResourceType(elementBean.getResource()).getTypeName(),
            false);
        Map<CmsUUID, I_CmsFormatterBean> formatters = new HashMap<>();
        for (I_CmsFormatterBean formatter : formatterList) {
            formatters.put(new CmsUUID(formatter.getId()), formatter);
        }

        Map<String, String> settings = elementBean.getIndividualSettings();
        I_CmsFormatterBean result = null;

        String forKeyWithContainer = settings.get(CmsFormatterConfig.FORMATTER_SETTINGS_KEY + container.getName());
        String forKeyWithoutContainer = settings.get(CmsFormatterConfig.FORMATTER_SETTINGS_KEY);
        for (String formatterId : new String[] {forKeyWithContainer, forKeyWithoutContainer}) {
            if (CmsUUID.isValidUUID(formatterId)) {
                result = formatters.get(new CmsUUID(formatterId));
                break;
            }
        }
        CmsUUID elementJspId = elementBean.getFormatterId();
        if ((result == null) && (elementJspId != null)) {
            LOG.warn(
                "Formatter id not found for element "
                    + elementBean.getResource().getRootPath()
                    + "  in "
                    + m_page.getRootPath());

            for (I_CmsFormatterBean bean : formatters.values()) {
                if (bean.getJspStructureId().equals(elementJspId)) {
                    result = bean;
                    break;
                }
            }
        }
        return result;

    }

}
