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

package org.opencms.db.timing;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Invocation handler used to measure method calls durations.<p>
 */
public class CmsProfilingInvocationHandler implements InvocationHandler {

    /** The profiling handler to which we send the data. */
    private I_CmsProfilingHandler m_profilingHandler;

    /** The target object which we are proxying. */
    private Object m_target;

    /**
     * Creates a new handler instance.<p>
     *
     * @param target the object we are proxying
     * @param timingConsumer the handler to which we send the measured data
     */
    public CmsProfilingInvocationHandler(Object target, I_CmsProfilingHandler timingConsumer) {

        m_target = target;
        m_profilingHandler = timingConsumer;
    }

    /**
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        long start = 0;
        long end = 0;
        try {
            start = System.nanoTime();
            return method.invoke(m_target, args);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause != null) {
                throw cause;
            } else {
                throw new RuntimeException(e);
            }
        } catch (Throwable e) {
            throw e;
        } finally {
            end = System.nanoTime();
            long nanos = end - start;
            String key = method.toString();
            m_profilingHandler.putTime(key, nanos);
        }
    }

}
