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

package org.opencms.ade.containerpage.shared;

import org.opencms.gwt.shared.CmsModelResourceInfo;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Bean containing the needed data when creating a new resource.<p>
 *
 * @since 8.0.3
 */
public class CmsCreateElementData implements IsSerializable {

    /** The created element. */
    private CmsContainerElement m_createdElement;

    /** The list of model resources. */
    private List<CmsModelResourceInfo> m_modelResources;

    /**
     * Returns the created element.<p>
     *
     * @return the created element
     */
    public CmsContainerElement getCreatedElement() {

        return m_createdElement;
    }

    /**
     * Returns the model resources list.<p>
     *
     * @return the model resources list
     */
    public List<CmsModelResourceInfo> getModelResources() {

        return m_modelResources;
    }

    /**
     * Returns if model selection is needed.<p>
     *
     * @return <code>true</code> if model selection is needed
     */
    public boolean needsModelSelection() {

        return (m_modelResources != null) && !m_modelResources.isEmpty();
    }

    /**
     * Sets the created element.<p>
     *
     * @param createdElement the created element to set
     */
    public void setCreatedElement(CmsContainerElement createdElement) {

        m_createdElement = createdElement;
    }

    /**
     * Sets the model resources list.<p>
     *
     * @param modelResources the model resources list to set
     */
    public void setModelResources(List<CmsModelResourceInfo> modelResources) {

        m_modelResources = modelResources;
    }
}
