/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/publish/shared/Attic/CmsPublishOptionsAndProjects.java,v $
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

import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A bean that contains both publish options and a map of projects.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0
 */
public class CmsPublishOptionsAndProjects implements IsSerializable {

    /** The publish options. */
    private CmsClientPublishOptions m_options;

    /** The map of projects. */
    private Map<String, String> m_projects;

    /**
     * Default constructor.<p>
     * 
     */
    public CmsPublishOptionsAndProjects() {

        // default constructor, do nothing
    }

    /** 
     * Creates a new instance.<p>
     * 
     * @param options the publish options 
     * @param projects the map of projects 
     */
    public CmsPublishOptionsAndProjects(CmsClientPublishOptions options, Map<String, String> projects) {

        m_options = options;
        m_projects = projects;
    }

    /**
     * Returns the publish options.<p>
     * 
     * @return the publish options
     */
    public CmsClientPublishOptions getOptions() {

        return m_options;
    }

    /**
     * Returns the map of projects.<p>
     * 
     * @return the map of projects 
     */
    public Map<String, String> getProjects() {

        return m_projects;
    }
}
