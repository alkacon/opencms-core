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

package org.opencms.ui.apps.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import com.google.common.collect.Maps;
import com.vaadin.ui.Component;

public class CmsReflectionAppAttributeProvider {

    public static Map<String, Object> getComponentAttributes(Component component) {

        Class<?> cls = component.getClass();
        Map<String, Object> result = Maps.newHashMap();
        for (Method method : cls.getDeclaredMethods()) {
            ProvidesAppAttribute annotation = method.getAnnotation(ProvidesAppAttribute.class);
            if (annotation != null) {
                Object val = null;
                try {
                    val = method.invoke(component);
                    result.put(annotation.name(), val);
                } catch (InvocationTargetException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof Exception) {
                        throw new RuntimeException(cause);
                    } else if (cause instanceof RuntimeException) {
                        throw (RuntimeException)cause;
                    } else if (cause instanceof Error) {
                        throw (Error)cause;
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return result;

    }

}
