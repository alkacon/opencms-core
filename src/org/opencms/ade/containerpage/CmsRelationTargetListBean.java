/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

import org.opencms.file.CmsResource;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Helper bean used to collect a list of resources (usually relation targets) and keep track of whether any of them
 * have the "changed" state.<p>
 */
public class CmsRelationTargetListBean implements IsSerializable {

    /** True if any of the resources have been changed. */
    private boolean m_changed;

    /** The already processed structure IDs, used to eliminate duplicates. */
    private transient Set<CmsUUID> m_processedIds = new HashSet<CmsUUID>();

    /** The collected resources. */
    private List<CmsResource> m_resources = new ArrayList<CmsResource>();

    /**
     * Creates a new instance.<p>
     */
    public CmsRelationTargetListBean() {

    }

    /**
     * Adds a new resource.<p>
     *
     * @param resource the resource to add
     */
    public void add(CmsResource resource) {

        if (!m_processedIds.contains(resource.getStructureId())) {
            m_resources.add(resource);
            m_processedIds.add(resource.getStructureId());
            m_changed |= !(resource.getState().isUnchanged());
        }
    }

    /**
     * Gets the list of resources which have been added.<p>
     *
     * @return the list of added resource
     */
    public List<CmsResource> getResources() {

        return Collections.unmodifiableList(m_resources);
    }

    /**
     * Returns true if any of the added resources have been changed.<p>
     *
     * @return true if any of the resources have been changed
     */
    public boolean isChanged() {

        return m_changed;
    }

}
