/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/util/CmsJspNavigationBean.java,v $
 * Date   : $Date: 2011/04/11 14:11:09 $
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

package org.opencms.jsp.util;

import org.opencms.file.CmsObject;
import org.opencms.jsp.CmsJspNavBuilder;
import org.opencms.jsp.CmsJspNavElement;
import org.opencms.jsp.CmsJspTagNavigation;

import java.util.ArrayList;
import java.util.List;

public class CmsJspNavigationBean {

    /** The OpenCms user context. */
    protected CmsObject m_cms;

    /** The optional start level for the navigation. */
    protected int m_startLevel;

    /** The optional end level for the navigation. */
    protected int m_endLevel;

    /** The optional resource for the navigation. */
    protected String m_resource;

    /** The optional parameter for the navigation. */
    protected String m_param;

    /** The selected navigation type. */
    protected CmsJspTagNavigation.TypeUse m_type;

    /** The result items from the navigation. */
    protected List<CmsJspNavElement> m_items;

    /**
     * Base constructor.<p>
     * 
     * @param cms the current users OpenCms context to build the navigation for
     * @param type the navigation type to generate
     * @param startLevel the optional start level
     * @param endLevel the optional end level
     * @param resource the optional resource for the navigation
     * @param param the optional parameter for the navigation
     */
    public CmsJspNavigationBean(
        CmsObject cms,
        CmsJspTagNavigation.TypeUse type,
        int startLevel,
        int endLevel,
        String resource,
        String param) {

        m_cms = cms;
        m_type = type;
        m_startLevel = startLevel;
        m_endLevel = endLevel;
        m_resource = resource;
        m_param = param;
    }

    /**
     * Returns the list of selected navigation elements.<p>
     * 
     * @return the list of selected navigation elements
     */
    public List<CmsJspNavElement> getItems() {

        if (m_items == null) {
            // generate a navigation builder
            CmsJspNavBuilder builder = new CmsJspNavBuilder(m_cms);
            switch (m_type) {
                // calculate the results based on the given parameters
                case FOR_FOLDER:
                    if (m_startLevel == Integer.MIN_VALUE) {
                        // no start level set
                        if (m_resource == null) {
                            m_items = builder.getNavigationForFolder();
                        } else {
                            m_items = builder.getNavigationForFolder(m_resource);
                        }
                    } else {
                        // start level is set
                        if (m_resource == null) {
                            m_items = builder.getNavigationForFolder(m_startLevel);
                        } else {
                            m_items = builder.getNavigationForFolder(m_resource, m_startLevel);
                        }
                    }
                    break;
                case FOR_SITE:
                    if (m_resource == null) {
                        m_items = builder.getSiteNavigation();
                    } else {
                        m_items = builder.getSiteNavigation(m_resource, m_startLevel);
                    }
                    break;
                case BREAD_CRUMB:
                    if (m_resource != null) {
                        // resource is set
                        m_items = builder.getNavigationBreadCrumb(
                            m_resource,
                            m_startLevel,
                            m_endLevel,
                            Boolean.valueOf(m_param).booleanValue());
                    } else {
                        if (m_startLevel == Integer.MIN_VALUE) {
                            // no start level
                            m_items = builder.getNavigationBreadCrumb();
                        } else {
                            if (m_endLevel == Integer.MIN_VALUE) {
                                m_items = builder.getNavigationBreadCrumb(m_startLevel, m_endLevel);
                            } else {
                                m_items = builder.getNavigationBreadCrumb(
                                    m_startLevel,
                                    Boolean.valueOf(m_param).booleanValue());
                            }
                        }
                    }
                    break;
                case TREE_FOR_FOLDER:
                    if (m_resource == null) {
                        m_items = builder.getNavigationTreeForFolder(m_startLevel, m_endLevel);
                    } else {
                        m_items = builder.getNavigationTreeForFolder(m_resource, m_startLevel, m_endLevel);
                    }
                    break;
                case FOR_RESOURCE:
                default:
                    List<CmsJspNavElement> items = new ArrayList<CmsJspNavElement>(1);
                    if (m_resource == null) {
                        items.add(builder.getNavigationForResource());
                    } else {
                        items.add(builder.getNavigationForResource(m_resource));
                    }
                    m_items = items;
                    break;
            }
        }
        return m_items;
    }
}
