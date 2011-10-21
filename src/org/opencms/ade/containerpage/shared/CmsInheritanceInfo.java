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

package org.opencms.ade.containerpage.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class CmsInheritanceInfo implements IsSerializable {

    private boolean m_isNew;
    private String m_key;
    private Boolean m_visibility;
    private boolean m_visibilityInherited;

    public CmsInheritanceInfo() {

    }

    public CmsInheritanceInfo(String key, Boolean visibility, boolean isNew) {

        m_key = key;
        m_visibility = visibility;
        m_isNew = isNew;
    }

    public String getKey() {

        return m_key;
    }

    public Boolean getVisibility() {

        return m_visibility;
    }

    public boolean isNew() {

        return m_isNew;
    }

    public void setIsNew(boolean isNew) {

        m_isNew = isNew;
    }

    public void setKey(String key) {

        m_key = key;
    }

    public void setVisibility(Boolean visibility) {

        m_visibility = visibility;
    }

    public void setVisibilityInherited(boolean visibilityInherited) {

        m_visibilityInherited = visibilityInherited;
    }

}
