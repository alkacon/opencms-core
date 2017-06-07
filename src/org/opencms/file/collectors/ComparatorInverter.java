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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file.collectors;

import org.opencms.file.I_CmsResource;

import java.util.Comparator;

/**
 * Wrapper around a comparator that inverts comparison results which may e.g. be
 * used to invert sort orders. <p>
 *
 * This is used to create <code>{@link java.util.SortedSet}</code> instances that may
 * sort in different order (ascending vs. descending).<p>
 *
 * <table border="1">
 * <tr>
 * <th>Internal comparator result</th>
 * <th>Transformed result</th>
 * </tr>
 * <tr>
 * <td>-1</td>
 * <td>+1</td>
 * </tr>
 * <tr>
 * <td>0</td>
 * <td>0</td>
 * </tr>
 * <tr>
 * <td>+1</td>
 * <td>-1</td>
 * </tr>
 * </table>
 * <p>
 *
 * @since 7.0.3
 *
 */
public final class ComparatorInverter implements Comparator<I_CmsResource> {

    /** The comparator to invert. */
    private Comparator<I_CmsResource> m_delegate;

    /**
     * Creates a comparator that will invert the result of the given comparator.
     * <p>
     *
     * @param toInvert the comparator to invert results of
     */
    public ComparatorInverter(Comparator<I_CmsResource> toInvert) {

        m_delegate = toInvert;
    }

    /**
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(I_CmsResource o1, I_CmsResource o2) {

        int result = m_delegate.compare(o1, o2);
        return -result;
    }
}