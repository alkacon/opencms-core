/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.containerpage.inherited;

import org.opencms.ade.containerpage.shared.CmsInheritanceInfo;
import org.opencms.file.CmsResource;
import org.opencms.xml.containerpage.CmsContainerElementBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;

/**
 * The state of an inherited container at a given point in the VFS tree.<p>
 */
public class CmsInheritedContainerState {

    /** The list of configuration beans for each tree level, from top to bottom. */
    private List<CmsContainerConfiguration> m_parentConfigurations = new ArrayList<CmsContainerConfiguration>();

    /**
     * Default constructor.<p>
     */
    public CmsInheritedContainerState() {

    }

    /**
     * Reads the configurations for a root path and its parents from a cache instance and adds them to this state.<p>
     *
     * @param cache the cache instance
     * @param rootPath the root path
     * @param name the name of the container configuration
     */
    public void addConfigurations(CmsContainerConfigurationCache cache, String rootPath, String name) {

        String currentPath = rootPath;
        List<CmsContainerConfiguration> configurations = new ArrayList<CmsContainerConfiguration>();
        CmsContainerConfigurationCacheState state = cache.getState();
        while (currentPath != null) {
            CmsContainerConfiguration configuration = state.getContainerConfiguration(currentPath, name);
            if (configuration == null) {
                configuration = CmsContainerConfiguration.emptyConfiguration();
            }
            configuration.setPath(currentPath);
            configurations.add(configuration);
            currentPath = CmsResource.getParentFolder(currentPath);
        }
        Collections.reverse(configurations);
        for (CmsContainerConfiguration configuration : configurations) {
            if (configuration != null) {
                addConfiguration(configuration);
            }
        }
    }

    /**
     * Gets a list of container element beans which represent the state of the inherited container.<p>
     *
     * The container element beans returned will have additional information available via the getInheritanceInfo method.<p>
     *
     * @param includeHidden if true, hidden elements will be included in the result list
     *
     * @return the elements for this container state
     */
    public List<CmsContainerElementBean> getElements(boolean includeHidden) {

        if (m_parentConfigurations.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, CmsContainerElementBean> elementsByKey = new HashMap<String, CmsContainerElementBean>();
        for (CmsContainerConfiguration bean : m_parentConfigurations) {
            elementsByKey.putAll(bean.getNewElements());
        }

        List<CmsContainerElementBean> result = new ArrayList<CmsContainerElementBean>();

        CmsContainerConfiguration lastElement = m_parentConfigurations.get(m_parentConfigurations.size() - 1);
        Set<String> newKeys = new HashSet<String>();
        for (Map.Entry<String, CmsContainerElementBean> entry : lastElement.getNewElements().entrySet()) {
            String key = entry.getKey();
            newKeys.add(key);
        }
        Set<String> keysUsed = new HashSet<String>();
        Map<String, String> pathsByKey = new HashMap<String, String>();

        // STEP 1: Get first defined ordering
        List<String> ordering = null;
        for (CmsContainerConfiguration configuration : Lists.reverse(m_parentConfigurations)) {
            if (configuration.getOrdering() != null) {
                ordering = configuration.getOrdering();
                break;
            }
        }
        if (ordering == null) {
            ordering = new ArrayList<String>();
        }
        // STEP 2: Get elements which are referenced by the ordering
        for (String key : ordering) {
            CmsContainerElementBean element = elementsByKey.get(key);
            if ((element != null) && !keysUsed.contains(key)) {
                CmsContainerElementBean elementToAdd = CmsContainerElementBean.cloneWithSettings(
                    element,
                    element.getIndividualSettings());
                CmsInheritanceInfo info = new CmsInheritanceInfo();
                info.setKey(key);
                elementToAdd.setInheritanceInfo(info);
                result.add(elementToAdd);
                keysUsed.add(key);
            }
        }
        // STEP 3: Add 'new' elements from parents; also fill pathsByKey
        for (int i = 0; i < (m_parentConfigurations.size()); i++) {
            CmsContainerConfiguration currentConfig = m_parentConfigurations.get(i);
            for (Map.Entry<String, CmsContainerElementBean> entry : currentConfig.getNewElementsInOrder().entrySet()) {
                String key = entry.getKey();
                pathsByKey.put(key, currentConfig.getPath());
                if (!keysUsed.contains(key)) {
                    CmsContainerElementBean elementToAdd = CmsContainerElementBean.cloneWithSettings(
                        entry.getValue(),
                        entry.getValue().getIndividualSettings());
                    CmsInheritanceInfo info = new CmsInheritanceInfo();
                    info.setKey(key);
                    elementToAdd.setInheritanceInfo(info);
                    result.add(elementToAdd);
                }
            }
        }
        // STEP 4: Determine visibility and new-ness
        for (CmsContainerElementBean resultElement : result) {
            CmsInheritanceInfo info = resultElement.getInheritanceInfo();
            String key = info.getKey();
            List<Boolean> visibilities = getVisibilities(key);
            computeVisibility(visibilities, info);
            info.setIsNew(newKeys.contains(info.getKey()));
        }

        List<CmsContainerElementBean> resultWithoutHidden = new ArrayList<CmsContainerElementBean>();
        List<CmsContainerElementBean> hiddenElements = new ArrayList<CmsContainerElementBean>();
        for (CmsContainerElementBean resultElement : result) {
            CmsInheritanceInfo info = resultElement.getInheritanceInfo();
            if (!info.isVisible()) {
                hiddenElements.add(resultElement);
            } else {
                resultWithoutHidden.add(resultElement);
            }
        }
        result = resultWithoutHidden;
        if (includeHidden) {
            result.addAll(hiddenElements);
        }
        for (CmsContainerElementBean elementBean : result) {
            CmsInheritanceInfo info = elementBean.getInheritanceInfo();
            String path = pathsByKey.get(info.getKey());
            info.setPath(path);
        }

        return result;
    }

    /**
     * Gets the keys of new elements.<p>
     *
     * @return a set containing the keys of the new elements
     */
    public Set<String> getNewElementKeys() {

        Set<String> result = new HashSet<String>();
        for (CmsContainerConfiguration configuration : m_parentConfigurations) {
            result.addAll(configuration.getNewElements().keySet());
        }
        return result;
    }

    /**
     * Checks whether an element with the given key is actually defined in this inherited container state.<p>
     *
     * @param key the key for which the check should be performed
     *
     * @return true if an element with the key has been defined in this state
     */
    public boolean hasElementWithKey(String key) {

        for (CmsContainerConfiguration configuration : m_parentConfigurations) {
            if (configuration.getNewElements().containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a configuration bean for a new tree level.<p>
     *
     * @param configuration the configuration bean
     */
    protected void addConfiguration(CmsContainerConfiguration configuration) {

        m_parentConfigurations.add(configuration);

    }

    /**
     * Gets the list of visibilities for a given key in all the tree levels.<p>
     *
     * @param key the key for which the visibilities should be returned
     *
     * @return the list of visibilities, from top to bottom
     */
    protected List<Boolean> getVisibilities(String key) {

        List<Boolean> result = new ArrayList<Boolean>();
        for (CmsContainerConfiguration config : m_parentConfigurations) {
            result.add(config.getVisibility().get(key));
        }
        return result;
    }

    /**
     * Computes the visibility for an element.<p>
     *
     * @param visibilities the visibilities for the element in the sequence of parent configurations.<p>
     *
     * @param info the object in which the visibility should be stored
     */
    void computeVisibility(List<Boolean> visibilities, CmsInheritanceInfo info) {

        boolean visible = true;
        boolean inherited = true;
        boolean parentVisible = true;
        for (Boolean visibility : visibilities) {
            parentVisible = visible;
            if (visibility == Boolean.TRUE) {
                visible = true;
                inherited = false;
            } else if (visibility == Boolean.FALSE) {
                visible = false;
                inherited = false;
            } else {
                inherited = true;
            }
        }
        info.setVisible(visible);
        info.setVisibilityInherited(inherited);
        info.setParentVisible(parentVisible);
    }
}
