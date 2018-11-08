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

package org.opencms.ade.containerpage;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsContainerBean;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.I_CmsFormatterBean;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * This is a simple helper class to more easily produce container page beans to be used as detail-only containers.<p>
 *
 *
 * To use this helper, you will need to set the type or width of each container and then add the resources to be used
 * as container elements. The type or width must be set because this helper tries to automatically determine a default formatter
 * for each container element.<p>
 *
 * Finally, call the build() method to produce the desired container page bean
 *
 * Element settings and nested containers are currently not supported.
 *
 */
public class CmsDetailOnlyContainerPageBuilder {

    /**
     * Bean containing the information for a single container.<p>
     */
    public static class ContainerInfo {

        /** The list of resources to use as container elements. */
        private List<CmsResource> m_elements = Lists.newArrayList();

        /** The container name. */
        private String m_name;

        /** Container type. */
        private String m_type;

        /** Container width. */
        private String m_width;

        /**
         * Creates a new instance.<p>
         *
         * @param name the container name
         */
        public ContainerInfo(String name) {

            m_name = name;
        }

        /**
         * Adds a container element resource.<p>
         *
         * @param resource the container element resource
         */
        public void addResource(CmsResource resource) {

            m_elements.add(resource);
        }

        /**
         * Gets the effective container type.<p>
         *
         * @return the effective container type
         */
        public String getEffectiveType() {

            if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_type)) {
                return m_name;
            } else {
                return m_type;
            }
        }

        /**
         * Gets the effective container width.
         *
         * @return the effective container width
         */
        public int getEffectiveWidth() {

            try {
                return Integer.parseInt(m_width);
            } catch (Exception e) {
                return -1;
            }
        }

        /**
         * Gets the container name.<p>
         *
         * @return the container name
         */
        public String getName() {

            return m_name;
        }

        /**
         * Gets the container element resources.
         *
         * @return the container element resources
         */
        public List<CmsResource> getResources() {

            return m_elements;
        }

        /**
         * Returns the type.<p>
         *
         * @return the type
         */
        public String getType() {

            return m_type;
        }

        /**
         * Returns the width.<p>
         *
         * @return the width
         */
        public String getWidth() {

            return m_width;
        }

        /**
         * Sets the type.<p>
         *
         * @param type the type to set
         */
        public void setType(String type) {

            m_type = type;
        }

        /**
         * Sets the width.<p>
         *
         * @param width the width to set
         */
        public void setWidth(String width) {

            m_width = width;
        }
    }

    /** The current CMS context. */
    private CmsObject m_cms;

    /** The sitemap configuration to use for automatically trying to determine the correct formatter. */
    private CmsADEConfigData m_config;

    /** The map of container info beans, with container names as keys. */
    private TreeMap<String, ContainerInfo> m_containerInfos = Maps.newTreeMap();

    /**
     * Creates a new instance.<p>
     *
     * @param cms the current CMS context
     * @param config the sitemap configuration which should be used to determine the
     */
    public CmsDetailOnlyContainerPageBuilder(CmsObject cms, CmsADEConfigData config) {

        m_cms = cms;
        m_config = config;
    }

    /**
     * Adds a resource to a container as an element.<p>
     *
     * @param name the container name
     * @param resource the resource to add as a container elementv
     */
    public void addContainerElement(String name, CmsResource resource) {

        getContainerInfo(name).getResources().add(resource);
    }

    /**
     * Builds the container page bean.<p>
     *
     * @return the container page bean
     */
    public CmsContainerPageBean build() {

        List<CmsContainerBean> containers = Lists.newArrayList();
        for (String containerName : m_containerInfos.keySet()) {
            CmsContainerBean containerBean = buildContainerBean(m_containerInfos.get(containerName));
            containers.add(containerBean);
        }
        return new CmsContainerPageBean(containers);
    }

    /**
     * Sets the type of a container.<p>
     *
     * @param name the container name
     * @param type the container type
     */
    public void setContainerType(String name, String type) {

        getContainerInfo(name).setType(type);
    }

    /**
     * Sets the width of a container.<p>
     *
     * @param name the container name
     * @param width the container width
     */
    public void setContainerWidth(String name, String width) {

        getContainerInfo(name).setWidth(width);
    }

    /**
     * Builds a container bean.<p>
     *
     * @param cnt the object containing the information to store in the container bean
     * @return the container bean
     */
    private CmsContainerBean buildContainerBean(ContainerInfo cnt) {

        List<CmsContainerElementBean> elements = Lists.newArrayList();
        for (CmsResource resource : cnt.getResources()) {
            CmsContainerElementBean elementBean = buildContainerElementBean(cnt, resource);
            elements.add(elementBean);
        }
        CmsContainerBean result = new CmsContainerBean(cnt.getName(), cnt.getType(), null, true, elements);
        return result;
    }

    /**
     * Builds the container element bean for a resource.<p>
     *
     * @param cnt the container for the element resource
     * @param resource the resource
     *
     * @return the container element bean
     */
    private CmsContainerElementBean buildContainerElementBean(ContainerInfo cnt, CmsResource resource) {

        I_CmsFormatterBean formatter = m_config.getFormatters(m_cms, resource).getDefaultFormatter(
            cnt.getEffectiveType(),
            cnt.getEffectiveWidth());
        CmsUUID formatterId = formatter.getJspStructureId();
        CmsContainerElementBean elementBean = new CmsContainerElementBean(
            resource.getStructureId(),
            formatterId,
            new HashMap<String, String>(),
            false);
        return elementBean;
    }

    /**
     * Gets the container information for a given container, creating it if it doesn't already exist.<p>
     *
     * @param name the container name
     * @return the container info object
     */
    private ContainerInfo getContainerInfo(String name) {

        if (!m_containerInfos.containsKey(name)) {
            m_containerInfos.put(name, new ContainerInfo(name));
        }
        return m_containerInfos.get(name);
    }

}
