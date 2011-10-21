/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.containerpage.inherited;

import org.opencms.file.CmsResource;

import java.util.Locale;

public class CmsInheritanceReference {

    private Locale m_locale;

    private String m_name;

    private CmsResource m_resource;

    private String m_title;

    public CmsInheritanceReference(String name, String title, CmsResource res, Locale locale) {

        m_title = title;
        m_name = name;
        m_resource = res;
        m_locale = locale;
    }

    public Locale getLocale() {

        return m_locale;
    }

    public String getName() {

        return m_name;
    }

    public CmsResource getResource() {

        return m_resource;
    }

    public String getTitle() {

        return m_title;
    };

}
