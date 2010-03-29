/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/publish/shared/Attic/CmsPublishGroups.java,v $
 * Date   : $Date: 2010/03/29 08:47:35 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.publish.shared;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Bean class which holds a publish resource list subdivided into groups.
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsPublishGroups implements IsSerializable {

    /** The groups of resources to be published, indexed by group title. */
    private Map<String, List<CmsClientPublishResourceBean>> m_groups = new LinkedHashMap<String, List<CmsClientPublishResourceBean>>();

    /**
     * Creates a new CmsPublishGroups instance.<p>
     */
    public CmsPublishGroups() {

        // default constructor, do nothing
    }

    /**
     * Adds a new publish group.<p>
     * 
     * @param header the title of the group  
     * @param group the list of resource beans in the group
     */
    public void addGroup(String header, List<CmsClientPublishResourceBean> group) {

        m_groups.put(header, group);
    }

    /**
     * Gets the publish groups.<p>
     * 
     * @return the publish groups.
     */
    public Map<String, List<CmsClientPublishResourceBean>> getGroups() {

        return m_groups;
    }
}
