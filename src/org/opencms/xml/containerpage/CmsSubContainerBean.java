/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/containerpage/Attic/CmsSubContainerBean.java,v $
 * Date   : $Date: 2010/10/12 06:54:08 $
 * Version: $Revision: 1.4 $
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

package org.opencms.xml.containerpage;

import java.util.List;
import java.util.Set;

/**
 * A sub container.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.4 $
 * 
 * @since 7.9.1
 */
public class CmsSubContainerBean {

    /** The sub container description. */
    private String m_description;

    /** The sub container elements.*/
    private List<CmsContainerElementBean> m_elements;

    /** The sub container title. */
    private String m_title;

    /** The supported container types. */
    private Set<String> m_types;

    /**
     * Creates a new sub container bean.<p>
     * 
     * @param title the sub container title
     * @param description the sub container description
     * @param elements the sub container elements
     * @param types the supported container types
     */
    public CmsSubContainerBean(
        String title,
        String description,
        List<CmsContainerElementBean> elements,
        Set<String> types) {

        m_title = title;
        m_description = description;
        m_elements = elements;
        m_types = types;
    }

    /**
     * Returns the description.<p>
     *
     * @return the description
     */
    public String getDescription() {

        return m_description;
    }

    /**
     * Returns the elements.<p>
     *
     * @return the elements
     */
    public List<CmsContainerElementBean> getElements() {

        return m_elements;
    }

    /**
     * Returns the title.<p>
     *
     * @return the title
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * Returns the types.<p>
     *
     * @return the types
     */
    public Set<String> getTypes() {

        return m_types;
    }
}
