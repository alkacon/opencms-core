/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/CmsContainerPageBean.java,v $
 * Date   : $Date: 2009/09/09 09:36:52 $
 * Version: $Revision: 1.1.2.2 $
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

package org.opencms.workplace.editors.ade;

import org.opencms.file.CmsResource;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * One locale of a container page.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 7.6 
 */
public class CmsContainerPageBean {

    /** The containers. */
    private Map<String, CmsContainerBean> m_containers;

    /** The locale. */
    private Locale m_locale;

    /** The 'resource type' configuration file. */
    private CmsResource m_resTypeConfig;

    /** The supported types. */
    private Set<String> m_types;

    /** 
     * Creates a new container page bean.<p> 
     * 
     * @param locale the locale
     * @param resTypeConfig the 'resource type' configuration 
     **/
    public CmsContainerPageBean(Locale locale, CmsResource resTypeConfig) {

        m_locale = locale;
        m_resTypeConfig = resTypeConfig;
        m_containers = new HashMap<String, CmsContainerBean>();
        m_types = new HashSet<String>();
    }

    /**
     * Adds a new container to the container page.<p>
     * 
     * @param container the container to add
     */
    public void addContainer(CmsContainerBean container) {

        m_containers.put(container.getName(), container);
        m_types.add(container.getType());
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
     * Returns the 'resource type' configuration.<p>
     *
     * @return the 'resource type' configuration
     */
    public CmsResource getResTypeConfig() {

        return m_resTypeConfig;
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
