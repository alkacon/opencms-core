/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/containerpage/Attic/CmsContainerBean.java,v $
 * Date   : $Date: 2009/10/13 11:59:42 $
 * Version: $Revision: 1.1.2.1 $
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One container of a container page.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 7.6 
 */
public class CmsContainerBean implements I_CmsContainerBean {

    /** The container elements.*/
    private List<I_CmsContainerElementBean> m_elements;

    /** The maximal number of elements in the container.*/
    private int m_maxElements;

    /** The container name.*/
    private String m_name;

    /** The container type.*/
    private String m_type;

    /** 
     * Creates a new container page bean.<p> 
     * 
     * @param name the container name
     * @param type the container type
     * @param maxElements the maximal number of elements in the container
     **/
    public CmsContainerBean(String name, String type, int maxElements) {

        m_name = name;
        m_type = type;
        m_maxElements = maxElements;
        m_elements = new ArrayList<I_CmsContainerElementBean>();
    }

    /**
     * Adds an element to the container.<p>
     * 
     * @param elem the element to add
     */
    public void addElement(I_CmsContainerElementBean elem) {

        m_elements.add(elem);
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsContainerBean#getElements()
     */
    public List<I_CmsContainerElementBean> getElements() {

        return Collections.unmodifiableList(m_elements);
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsContainerBean#getMaxElements()
     */
    public int getMaxElements() {

        return m_maxElements;
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsContainerBean#getName()
     */
    public String getName() {

        return m_name;
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsContainerBean#getType()
     */
    public String getType() {

        return m_type;
    }

    /**
     * Sets the maximal number of elements in the container.<p>
     *
     * @param maxElements the maximal number of elements to set
     */
    public void setMaxElements(int maxElements) {

        m_maxElements = maxElements;
    }
}
