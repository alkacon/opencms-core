/*
 * File   : $Source: /alkacon/cvs/opencms/src-components/org/opencms/webdav/Attic/CmsWebdavRange.java,v $
 * Date   : $Date: 2007/01/12 17:24:42 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.webdav;

/**
 * Helper class for the WebDAV servlet. Holds values for a range.
 * 
 * @author Peter Bonrad
 */
public class CmsWebdavRange {

    private long m_end;
    private long m_length;
    private long m_start;

    /**
     * Returns the end.<p>
     *
     * @return the end
     */
    public long getEnd() {

        return m_end;
    }

    /**
     * Returns the length.<p>
     *
     * @return the length
     */
    public long getLength() {

        return m_length;
    }

    /**
     * Returns the start.<p>
     *
     * @return the start
     */
    public long getStart() {

        return m_start;
    }

    /**
     * Resets this range.
     *
     */
    public void recycle() {

        m_start = 0;
        m_end = 0;
        m_length = 0;
    }

    /**
     * Sets the end.<p>
     *
     * @param end the end to set
     */
    public void setEnd(long end) {

        m_end = end;
    }

    /**
     * Sets the length.<p>
     *
     * @param length the length to set
     */
    public void setLength(long length) {

        m_length = length;
    }

    /**
     * Sets the start.<p>
     *
     * @param start the start to set
     */
    public void setStart(long start) {

        m_start = start;
    }

    /**
     * Validate range.
     * 
     * @return true if the actual range is valid otherwise false
     */
    public boolean validate() {

        if (m_end >= m_length) {
            m_end = m_length - 1;
        }

        return ((m_start >= 0) && (m_end >= 0) && (m_start <= m_end) && (m_length > 0));
    }

}
