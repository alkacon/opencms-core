/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/containerpage/Attic/CmsContainerPageBean.java,v $
 * Date   : $Date: 2009/10/13 11:59:40 $
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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Describes one locale of a container page.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 7.6
 */
public class CmsContainerPageBean implements I_CmsContainerPageBean {

    /** The containers. */
    private Map<String, I_CmsContainerBean> m_containers;

    /** The locale. */
    private Locale m_locale;

    /** The supported types. */
    private Set<String> m_types;

    /** 
     * Creates a new container page bean.<p> 
     * 
     * @param locale the locale
     **/
    public CmsContainerPageBean(Locale locale) {

        m_locale = locale;
        m_containers = new HashMap<String, I_CmsContainerBean>();
        m_types = new HashSet<String>();
    }

    /**
     * Adds a new container to the container page.<p>
     * 
     * @param container the container to add
     */
    public void addContainer(I_CmsContainerBean container) {

        m_containers.put(container.getName(), container);
        m_types.add(container.getType());
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsContainerPageBean#getContainers()
     */
    public Map<String, I_CmsContainerBean> getContainers() {

        return Collections.unmodifiableMap(m_containers);
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsContainerPageBean#getLocale()
     */
    public Locale getLocale() {

        return m_locale;
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsContainerPageBean#getTypes()
     */
    public Set<String> getTypes() {

        return Collections.unmodifiableSet(m_types);
    }
}
