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

package org.opencms.ui.apps;

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.Maps;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.v7.ui.Label;

/**
 * An abstract subclass of A_CmsWorkplaceApp which provides an additional way for the main component of an app
 * (the widget returned by getComponentForState) to influence the app layout of the app itself (i.e. the parts outside the main component).
 *
 *
 * This is done using two methods:
 *
 * getAttributesForComponent is used to extract additiona
 *
 */
public abstract class A_CmsAttributeAwareApp extends A_CmsWorkplaceApp {

    /**Attribute for info. */
    public static final String ATTR_INFO_COMPONENT = "ATTR_INFO_COMPONENT";

    /**Attribute for full height. */
    public static final String ATTR_MAIN_HEIGHT_FULL = "ATTR_MAIN_HEIGHT_FULL";

    /**vaadin component. */
    private Component m_infoComponent;

    /**
     * Opens the requested sub view.<p>
     *
     * @param state the state
     * @param updateState <code>true</code> to update the state URL token
     */
    @Override
    public void openSubView(String state, boolean updateState) {

        if (updateState) {
            CmsAppWorkplaceUi.get().changeCurrentAppState(state);
        }
        Component comp = getComponentForState(state);
        if (comp != null) {
            updateMainComponent(comp);
        } else {
            m_rootLayout.setMainContent(new Label("Malformed path, tool not available for path: " + state));
            updateAppAttributes(Collections.<String, Object> emptyMap());
        }
        updateSubNav(getSubNavEntries(state));
        updateBreadCrumb(getBreadCrumbForState(state));

    }

    /**
     * Replaces the app's  main component with the given component.<p>
     *
     * This also handles the attributes for the component, just as if the given component was returned by an app's
     * getComponentForState method.
     *
     * @param comp the component to set as the main component
     */
    public void updateMainComponent(Component comp) {

        comp.setSizeFull();
        m_rootLayout.setMainContent(comp);
        Map<String, Object> attributes = getAttributesForComponent(comp);
        updateAppAttributes(attributes);
    }

    /**
     * Gets the attributes from a given component.<p>
     *
     * @param component to read attributes from
     * @return map of attributes
     */
    protected Map<String, Object> getAttributesForComponent(Component component) {

        Map<String, Object> result = Maps.newHashMap();
        if (component instanceof AbstractComponent) {
            AbstractComponent abstractComp = (AbstractComponent)component;
            if (abstractComp.getData() instanceof Map) {
                Map<?, ?> map = (Map<?, ?>)abstractComp.getData();
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (entry.getKey() instanceof String) {
                        result.put((String)(entry.getKey()), entry.getValue());
                    }
                }
            }
        }
        return result;
    }

    /**
     * Handles the attributes.<p>
     *
     * @param attributes to set
     */
    protected void updateAppAttributes(Map<String, Object> attributes) {

        m_rootLayout.setMainHeightFull(Boolean.TRUE.equals(attributes.get(ATTR_MAIN_HEIGHT_FULL)));

        Object infoComponentObj = attributes.get(ATTR_INFO_COMPONENT);
        if (m_infoComponent != null) {
            m_infoLayout.removeComponent(m_infoComponent);
        }
        if (infoComponentObj instanceof Component) {
            m_infoComponent = (Component)infoComponentObj;
            m_infoLayout.addComponent(m_infoComponent);
        }
    }
}
