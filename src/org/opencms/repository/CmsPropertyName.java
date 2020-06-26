/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.repository;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;

public class CmsPropertyName {

    private String m_namespace;

    private String m_name;

    public CmsPropertyName(String namespace, String name) {

        m_namespace = namespace;
        m_name = name;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CmsPropertyName other = (CmsPropertyName)obj;
        return Objects.equal(m_name, other.m_name) && Objects.equal(m_namespace, other.m_namespace);
    }

    public String getName() {

        return m_name;
    }

    public String getNamespace() {

        return m_namespace;
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(m_namespace).append(m_name).toHashCode();
    }

    @Override
    public String toString() {

        return "{" + m_namespace + "}" + m_name;
    }

}
