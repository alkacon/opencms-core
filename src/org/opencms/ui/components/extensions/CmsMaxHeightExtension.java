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

package org.opencms.ui.components.extensions;

import org.opencms.ui.shared.components.CmsMaxHeightState;
import org.opencms.ui.shared.rpc.I_CmsMaxHeightServerRpc;

import java.util.List;

import com.google.common.collect.Lists;
import com.vaadin.server.AbstractExtension;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.AbstractComponent;

/**
 * Allows the use of max height in combination with vaadin layout components.<p>
 */
public class CmsMaxHeightExtension extends AbstractExtension implements I_CmsMaxHeightServerRpc {

    /** The serial version id. */
    private static final long serialVersionUID = 3978957151754705873L;

    /** The extended component. */
    private AbstractComponent m_component;

    /** The list of height change handlers. */
    private List<Runnable> m_heightChangeHandlers = Lists.newArrayList();

    /**
     * Constructor.<p>
     *
     * @param component the component to extend
     * @param maxHeight the max height
     */
    public CmsMaxHeightExtension(AbstractComponent component, int maxHeight) {
        m_component = component;
        extend(component);
        registerRpc(this);
        getState().setMaxHeight(maxHeight);

    }

    /**
     * Adds an action to execute when the height is changed.<p>
     *
     * @param action the action
     */
    public void addHeightChangeHandler(Runnable action) {

        m_heightChangeHandlers.add(action);
    }

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsMaxHeightServerRpc#fixHeight(int)
     */
    public void fixHeight(int height) {

        if (height < 1) {
            m_component.setHeightUndefined();
        } else {
            m_component.setHeight(height, Unit.PIXELS);
        }
        for (Runnable handler : m_heightChangeHandlers) {
            handler.run();
        }
    }

    /**
     * Removes a height change handler.<p>
     *
     * @param action the handler to remove
     */
    public void removeHeightChangeHandler(Runnable action) {

        m_heightChangeHandlers.remove(action);
    }

    /**
     * Updates the maximum height.<p>
     *
     * @param maxHeight the new value for the maximum height
     */
    public void updateMaxHeight(int maxHeight) {

        getState().setMaxHeight(maxHeight);
    }

    /**
     * @see com.vaadin.server.AbstractClientConnector#getState()
     */
    @Override
    protected CmsMaxHeightState getState() {

        return (CmsMaxHeightState)super.getState();
    }

}
