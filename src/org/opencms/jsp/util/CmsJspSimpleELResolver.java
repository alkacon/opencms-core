/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.jsp.util;

import jakarta.el.ELContext;
import jakarta.servlet.jsp.el.ScopedAttributeELResolver;

/**
 * Does basically the same as ScopedAttributeELResolver, but always marks top-level properties as resolved
 * even if no value was found.
 * <p>This is meant to bypass ImportELResolver (in Jakarta EE 10 and later), which comes later in the resolver chain and would be normally triggered later for an unresolved
 * property, but incurs a heavy performance penalty on Jetty, especially in combination with the Mercury template,
 * where potentially undefined top-level variables are used a lot.
 * <p>Using this means that the functionality provided by ImportELResolver will not be available in OpenCms JSPs.
 */
public class CmsJspSimpleELResolver extends ScopedAttributeELResolver {

    /**
     * @see jakarta.servlet.jsp.el.ScopedAttributeELResolver#getCommonPropertyType(jakarta.el.ELContext, java.lang.Object)
     */
    @Override
    public Class<String> getCommonPropertyType(ELContext context, Object base) {

        return super.getCommonPropertyType(context, base);

    }

    /**
     * @see jakarta.servlet.jsp.el.ScopedAttributeELResolver#getType(jakarta.el.ELContext, java.lang.Object, java.lang.Object)
     */
    @Override
    public Class<Object> getType(ELContext context, Object base, Object property) {

        if (base == null) {
            Class<Object> result = super.getType(context, base, property);
            context.setPropertyResolved(true);
            return result;
        }
        return null;

    }

    /**
     * @see jakarta.servlet.jsp.el.ScopedAttributeELResolver#getValue(jakarta.el.ELContext, java.lang.Object, java.lang.Object)
     */
    @Override
    public Object getValue(ELContext context, Object base, Object property) {

        if (base == null) {
            Object result = super.getValue(context, base, property);
            context.setPropertyResolved(true);
            return result;
        }
        return null;
    }

    /**
     * @see jakarta.servlet.jsp.el.ScopedAttributeELResolver#isReadOnly(jakarta.el.ELContext, java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {

        if (base == null) {
            boolean result = super.isReadOnly(context, base, property);
            context.setPropertyResolved(true);
            return result;
        }
        return false;
    }

    /**
     * @see jakarta.servlet.jsp.el.ScopedAttributeELResolver#setValue(jakarta.el.ELContext, java.lang.Object, java.lang.Object, java.lang.Object)
     */
    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) {

        if (base == null) {
            super.setValue(context, base, property, value);
            context.setPropertyResolved(true);
        }
    }
}
