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

package org.opencms.xml.containerpage.mutable;

import org.opencms.xml.containerpage.CmsContainerBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mutable bean representing a container page, for use in programmatically editing container pages.
 */
public class CmsMutableContainerPage {

    /** The list of containers (we just store them as a list instead of a map because the container names may be changed later). */
    private List<CmsMutableContainer> m_containers = new ArrayList<>();

    /**
     * Creates a new instance.
     *
     * @param containers the containers comprising this container page.
     */
    public CmsMutableContainerPage(List<CmsMutableContainer> containers) {

        m_containers = new ArrayList<>(containers);
    }

    /**
     * Converts a CmsContainerPageBean to an instance of this class.
     *
     * @param page the container page bean to convert
     * @return an instance of this class with the data from the container page bean
     */
    public static CmsMutableContainerPage fromImmutable(CmsContainerPageBean page) {

        List<CmsMutableContainer> containers = page.getContainers().values().stream().map(
            container -> CmsMutableContainer.fromImmutable(container)).collect(Collectors.toList());
        return new CmsMutableContainerPage(containers);
    }

    /**
     * Returns the mutable list of containers for this container page.
     *
     * @return the mutable list of containers
     */
    public List<CmsMutableContainer> containers() {

        return m_containers;
    }

    /**
     * Gets all containrs from the container page with a container name matching the given name
     *
     * @param name the name for which to look
     * @return the list of containers matching the given name
     */
    public List<CmsMutableContainer> containers(String name) {

        return m_containers.stream().filter(container -> container.matches(name)).collect(Collectors.toList());
    }

    /**
     * Gets the container with exactly the given container name.
     *
     * @param name the container name
     * @return the container with the given name, or null if none was found
     */
    public CmsMutableContainer exactContainer(String name) {

        return m_containers.stream().filter(container -> name.equals(container.getName())).findFirst().orElse(null);
    }

    /**
     * Gets the first container which has a name matching the given name.
     *
     * @param name the name to match
     * @return the matching container, or null if none was found
     */
    public CmsMutableContainer firstContainer(String name) {

        return m_containers.stream().filter(container -> container.matches(name)).findFirst().orElse(null);
    }

    /**
     * Converts this object to a CmsContainerPageBean.
     *
     * @return the container page bean
     */
    public CmsContainerPageBean toImmutable() {

        List<CmsContainerBean> containers = m_containers.stream().map(cnt -> cnt.toImmutable()).collect(
            Collectors.toList());
        return new CmsContainerPageBean(containers);
    }
}
