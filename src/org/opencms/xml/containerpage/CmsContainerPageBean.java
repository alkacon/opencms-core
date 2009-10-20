/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/containerpage/Attic/CmsContainerPageBean.java,v $
 * Date   : $Date: 2009/10/20 07:38:53 $
 * Version: $Revision: 1.1.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Describes one locale of a container page.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 7.6
 */
public class CmsContainerPageBean {

    /** The template container element parameter name. */
    public static final String TEMPLATE_ELEMENT_PARAMETER = "id";

    /** The template container type. */
    public static final String TYPE_TEMPLATE = "template";

    /** The containers. */
    private Map<String, CmsContainerBean> m_containers;

    /** The locale. */
    private Locale m_locale;

    /** The supported types. */
    private Set<String> m_types;

    /** 
     * Creates a new container page bean.<p> 
     * 
     * @param locale the locale
     * @param containers the containers
     **/
    public CmsContainerPageBean(Locale locale, List<CmsContainerBean> containers) {

        m_locale = locale;
        m_containers = new HashMap<String, CmsContainerBean>();
        m_types = new HashSet<String>();
        for (CmsContainerBean container : containers) {
            m_containers.put(container.getName(), container);
            m_types.add(container.getType());
        }
    }

    /**
     * Returns the containers.<p>
     *
     * @return the containers
     */
    public Map<String, CmsContainerBean> getContainers() {

        return Collections.unmodifiableMap(m_containers);
    }

    /**
     * Returns the locale.<p>
     *
     * @return the locale
     */
    public Locale getLocale() {

        return m_locale;
    }

    /**
     * Returns the types.<p>
     *
     * @return the types
     */
    public Set<String> getTypes() {

        return Collections.unmodifiableSet(m_types);
    }
}
