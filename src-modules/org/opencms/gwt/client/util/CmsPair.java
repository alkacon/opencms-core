/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/util/Attic/CmsPair.java,v $
 * Date   : $Date: 2010/03/08 16:47:06 $
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

package org.opencms.gwt.client.util;

/**
 * Generic pair class.<p>
 * 
 * @param <A> type of the first component of the pair
 * @param <B> type of the second component of the pair
 */
public class CmsPair<A, B> {

    private A m_first;
    private B m_second;

    /**
     * Creates a new pair containing two objects.<p>
     * 
     * @param a the first component
     * @param b the second component
     */
    public CmsPair(A a, B b) {

        m_first = a;
        m_second = b;
    }

    /**
     * Returns the first component of the pair.<p>
     * 
     * @return the first component of the pair
     */
    public A getFirst() {

        return m_first;
    }

    /**
     * Returns the second component of the pair.<p>
     * 
     * @return the second component of the pair
     */
    public B getSecond() {

        return m_second;
    }

}
