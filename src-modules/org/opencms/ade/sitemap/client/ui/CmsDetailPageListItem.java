/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/ui/Attic/CmsDetailPageListItem.java,v $
 * Date   : $Date: 2010/12/17 08:45:30 $
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

package org.opencms.ade.sitemap.client.ui;

import org.opencms.ade.sitemap.shared.CmsResourceTypeInfo;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.CmsListItemWidget;

/**
 * A list item widget class which also contains a resource type info bean, for use in creating detail pages.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsDetailPageListItem extends CmsListItem {

    /** The resource type info bean. */
    private CmsResourceTypeInfo m_typeInfo;

    /**
     * Creates a new list item with a given resource type info bean.<p>
     * 
     * @param content the content for the list item widget 
     * @param typeInfo the resource type info bean 
     */
    public CmsDetailPageListItem(CmsListItemWidget content, CmsResourceTypeInfo typeInfo) {

        super(content);
        m_typeInfo = typeInfo;
    }

    /**
     * Returns the resource type information bean.<p>
     * 
     * @return the resource type info bean
     */
    public CmsResourceTypeInfo getResourceTypeInfo() {

        return m_typeInfo;
    }
}
