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
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;

public class CmsInheritedContainerState {

    private List<CmsContainerConfiguration> m_parentConfigurations = new ArrayList<CmsContainerConfiguration>();

    public CmsInheritedContainerState() {

    }

    public void addConfigurations(I_CmsContainerConfigurationCache cache, String rootPath, String name, Locale locale) {

        String currentPath = rootPath;
        List<CmsContainerConfiguration> configurations = new ArrayList<CmsContainerConfiguration>();
        synchronized (cache) {
            while (currentPath != null) {
                CmsContainerConfiguration configuration = cache.getContainerConfiguration(currentPath, name, locale);
                if (configuration == null) {
                    configuration = CmsContainerConfiguration.emptyConfiguration();
                }
                configuration.setPath(currentPath);
                configurations.add(configuration);
                currentPath = CmsResource.getParentFolder(currentPath);
            }
        }
        Collections.reverse(configurations);
        for (CmsContainerConfiguration configuration : configurations) {
            if (configuration != null) {
                addConfiguration(configuration);
            }
        }
    }

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
        Set<String> keysUsed = new HashSet<String>();

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
            if (element != null) {
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
        // STEP 3: Add 'new' elements from parents
        for (int i = 0; i < (m_parentConfigurations.size()); i++) {
            CmsContainerConfiguration currentConfig = m_parentConfigurations.get(i);
            for (Map.Entry<String, CmsContainerElementBean> entry : currentConfig.getNewElements().entrySet()) {
                String key = entry.getKey();
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
        // STEP 4: Determine visibility
        for (CmsContainerElementBean resultElement : result) {
            CmsInheritanceInfo info = resultElement.getInheritanceInfo();
            String key = info.getKey();
            List<Boolean> visibilities = getVisibilities(key);
            computeVisibility(visibilities, info);
        }
        List<CmsContainerElementBean> resultWithoutHidden = new ArrayList<CmsContainerElementBean>();
        List<CmsContainerElementBean> hiddenElements = new ArrayList<CmsContainerElementBean>();
        for (CmsContainerElementBean resultElement : result) {
            CmsInheritanceInfo info = resultElement.getInheritanceInfo();
            if (info.getVisibility() == Boolean.FALSE) {
                hiddenElements.add(resultElement);
            } else {
                resultWithoutHidden.add(resultElement);
            }
        }
        result = resultWithoutHidden;
        if (includeHidden) {
            result.addAll(hiddenElements);
        }
        return result;
    }

    protected void addConfiguration(CmsContainerConfiguration configuration) {

        m_parentConfigurations.add(configuration);

    }

    protected List<Boolean> getVisibilities(String key) {

        List<Boolean> result = new ArrayList<Boolean>();
        for (CmsContainerConfiguration config : m_parentConfigurations) {
            result.add(config.getVisibility().get(key));
        }
        return result;
    }

    void computeVisibility(List<Boolean> visibilities, CmsInheritanceInfo info) {

        boolean visible = true;
        boolean inherited = true;
        for (Boolean visibility : visibilities) {
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
        info.setVisibility(visible ? Boolean.TRUE : Boolean.FALSE);
        info.setVisibilityInherited(inherited);
    }
}
