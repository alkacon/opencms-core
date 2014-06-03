/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ade.containerpage.client.impl;

import org.opencms.ade.containerpage.client.I_CmsContainerZIndexManager;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.dom.client.Element;

/**
 * Implementation of the Z index manager for IE.<p>
 * 
 * @since 8.0.0
 */
public class CmsContainerZIndexManager implements I_CmsContainerZIndexManager {

    /** The element ancestor chains by container id. */
    private Map<String, CmsZIndexChain> m_chains = new HashMap<String, CmsZIndexChain>();

    /** The constant which should be added to the bumped z index. */
    public static final int BUMP_OFFSET = org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.constants().css().zIndexHighlighting() + 1;

    /** The maximum z index found in the container ancestors. */
    private int m_maxZIndex;

    /** The container whose ancestor chain's z-index is currently bumped, or null if there is no bumped ancestor chain. */
    private String m_bumped;

    /** The container from which the last drag operation started. */
    private String m_startContainer;

    /**
     * @see org.opencms.ade.containerpage.client.I_CmsContainerZIndexManager#addContainer(java.lang.String, com.google.gwt.dom.client.Element)
     */
    public void addContainer(String name, Element element) {

        CmsZIndexChain chain = CmsZIndexChain.get(element);
        m_chains.put(name, chain);
        m_maxZIndex = Math.max(m_maxZIndex, chain.getMaxZIndex());
    }

    /**
     * Bumps the z indices for the ancestor elements of a given container.<p>
     * 
     * @param containerName the name of the container 
     */
    public void bump(String containerName) {

        reset();
        if (containerName != null) {
            m_chains.get(containerName).bump(BUMP_OFFSET + m_maxZIndex);
            m_bumped = containerName;
        }
    }

    /**
     * @see org.opencms.ade.containerpage.client.I_CmsContainerZIndexManager#clear()
     */
    public void clear() {

        m_chains.clear();
        m_startContainer = null;
        m_bumped = null;
        m_maxZIndex = 0;
    }

    /**
     * @see org.opencms.ade.containerpage.client.I_CmsContainerZIndexManager#enter(java.lang.String)
     */
    public void enter(String containerName) {

        bump(containerName);
    }

    /**
     * @see org.opencms.ade.containerpage.client.I_CmsContainerZIndexManager#leave(java.lang.String)
     */
    public void leave(String containerName) {

        bump(m_startContainer);
    }

    /**
     * Resets the elements with bumped z indices.<p>
     */
    public void reset() {

        if (m_bumped == null) {
            return;
        }
        m_chains.get(m_bumped).reset();
    }

    /**
     * @see org.opencms.ade.containerpage.client.I_CmsContainerZIndexManager#start(java.lang.String)
     */
    public void start(String containerName) {

        m_startContainer = containerName;
        bump(m_startContainer);
    }

    /**
     * @see org.opencms.ade.containerpage.client.I_CmsContainerZIndexManager#stop()
     */
    public void stop() {

        reset();
    }

}
