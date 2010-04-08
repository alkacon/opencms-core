/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/publish/shared/Attic/CmsPublishStatus.java,v $
 * Date   : $Date: 2010/04/08 07:30:07 $
 * Version: $Revision: 1.2 $
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Class which represents the status of a publish request.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsPublishStatus implements IsSerializable {

    /** The list of resources which, when published, would lead to broken links. */
    private List<CmsClientPublishResourceBean> m_problemResources = new ArrayList<CmsClientPublishResourceBean>();

    /**
     * Constructor.<p>
     */
    public CmsPublishStatus() {

        // default constructor, do nothing
    }

    /**
     * Returns the list of resources which, when published, would lead to broken links.<p>
     *  
     * @return a list of resource beans 
     */
    public List<CmsClientPublishResourceBean> getProblemResources() {

        return Collections.unmodifiableList(m_problemResources);
    }

    /** 
     * Checks whether there are any problem resources.<p>
     * 
     * @return true if there are any problem resources 
     */
    public boolean hasProblem() {

        return !m_problemResources.isEmpty();
    }

    /**
     * Sets the problem resources for this bean.<p>
     * 
     * @param resources a list of resource beans 
     */
    public void setProblemResources(List<CmsClientPublishResourceBean> resources) {

        m_problemResources = resources;
    }
}
