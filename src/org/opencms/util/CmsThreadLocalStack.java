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

package org.opencms.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Thread local stack.
 *
 * @param <T> the type for the stack elements
 */
public class CmsThreadLocalStack<T> {

    /** The internal ThreadLocal storing the list. */
    private ThreadLocal<List<T>> m_stackVar = ThreadLocal.withInitial(() -> new ArrayList<T>());

    /**
     * Removes and returns the last element pushed onto the stack.
     *
     * @return the last element pushed onto the stack
     */
    public T pop() {

        return getStack().remove(getStack().size() - 1);
    }

    /**
     * Pushes a new element onto the stack.
     *
     * @param data the element to push on the stack
     */
    public void push(T data) {

        getStack().add(data);
    }

    /**
     * Returns the current stack size.
     *
     * @return the current stack size
     */
    public int size() {

        return getStack().size();
    }

    /**
     * Returns the current top of the stack, without removing it.
     * <p>
     * If the stack is empty, null is returned.
     *
     * @return the current top of the stack
     */
    public T top() {

        if (getStack().size() == 0) {
            return null;
        } else {
            return getStack().get(getStack().size() - 1);
        }
    }

    /**
     * Gets the current list stored in the ThreadLocal.
     *
     * @return the current list stored in the ThreadLocal
     */
    private List<T> getStack() {

        return m_stackVar.get();
    }

}
