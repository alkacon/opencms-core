/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.xml.content;

import org.opencms.loader.I_CmsFileNameGenerator;

import java.util.Iterator;

/**
 * Name generator which appends a numeric suffix to a given base string.<p>
 * 
 * @since 8.0.0 
 */
public class CmsNumberSuffixNameSequence implements Iterator<String> {

    /** The base name from which the names should be generated. */
    private String m_baseName;

    /** The counter which keeps track of how often the next() method has been called. */
    private int m_counter;

    /**
     * Creates a new instance.<p>
     * 
     * @param str the base name which should be used for generating the names 
     */
    public CmsNumberSuffixNameSequence(String str) {

        m_baseName = str;
    }

    /**
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {

        return true;
    }

    /**
     * @see java.util.Iterator#next()
     */
    public String next() {

        String result = m_baseName;
        if (m_counter > 0) {
            String numberSuffix = I_CmsFileNameGenerator.NUMBER_FORMAT.sprintf(m_counter);
            result = m_baseName + "-" + numberSuffix;
        }
        m_counter += 1;
        return result;
    }

    /**
     * @see java.util.Iterator#remove()
     */
    public void remove() {

        throw new UnsupportedOperationException();
    }
}