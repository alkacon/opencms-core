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

package org.opencms.ui.util;

import com.vaadin.ui.Component;

/**
 * Container wrapping a single UI component.<p>
 *
 * This seemingly useless class is used as a typesafe way to add component-valued fields to component classes which use the Vaadin declarative UI mechanism
 * when you don't want them to be bound automatically to something in the declarative UI HTML file.
 * We could just use arrays of size 1 instead, but using a custom class is probably easier to understand.
 *
 * @param <T> the tpye of the wrapped component
 */
public class CmsComponentField<T extends Component> {

    /** The wrapped component. */
    private T m_value;

    /**
     * Convenience method for creating a new instance.<p>
     *
     * @return the new instance
     */
    public static <U extends Component> CmsComponentField<U> newInstance() {

        return new CmsComponentField<U>();
    }

    /**
     * Gets the wrapped component.<p>
     *
     * @return the wrapped component
     */
    public T get() {

        return m_value;
    }

    /**
     * Sets the wrapped component.<p>
     *
     * @param value the wrapped component
     */
    public void set(T value) {

        m_value = value;
    }

}
