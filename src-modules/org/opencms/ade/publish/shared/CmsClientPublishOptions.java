/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/publish/shared/Attic/CmsClientPublishOptions.java,v $
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

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A bean representing the options for retrieving a publish list from the server.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsClientPublishOptions implements IsSerializable {

    /** The project id. */
    private String m_project;

    /** The flag to include related resources. */
    private boolean m_publishRelated;

    /** The flag to include sibling resources. */
    private boolean m_publishSiblings;

    /** 
     * The default constructor.<p>
     * 
     */
    public CmsClientPublishOptions() {

    }

    /**
     * Creates a new instance. <p>
     * 
     * @param project the project id
     * @param publishRelated the flag to include related resources
     * @param publishSiblings the flag to include sibling resources 
     */
    public CmsClientPublishOptions(String project, boolean publishRelated, boolean publishSiblings) {

        super();
        m_project = project;
        m_publishRelated = publishRelated;
        m_publishSiblings = publishSiblings;
    }

    /**
     * Gets the project id.<p>
     * 
     * @return the project id 
     */
    public String getProject() {

        return m_project;
    }

    /**
     * Gets the flag to include related resources. <p>
     * 
     * @return true if related resources should be included 
     */
    public boolean isIncludeRelated() {

        return m_publishRelated;
    }

    /**
     * Gets the flag to include sibling resources. <p>
     * 
     * @return true if sibling resources should be included
     */
    public boolean isIncludeSiblings() {

        return m_publishSiblings;
    }

    /** 
     * Sets the flag to include related resources.<p>
     * 
     * @param publishRelated the new value of the flag
     */
    public void setIncludeRelated(boolean publishRelated) {

        m_publishRelated = publishRelated;
    }

    /**
     * Sets the flag to include sibling resources.<p>
     * 
     * @param publishSiblings the new value of the flag
     */
    public void setIncludeSiblings(boolean publishSiblings) {

        m_publishSiblings = publishSiblings;
    }

    /**
     * Sets the project id.<p>
     * 
     * @param project the project id
     */
    public void setProject(String project) {

        m_project = project;
    }
}
