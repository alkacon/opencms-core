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
 *
 * This file is based on:
 * - org.apache.catalina.servlets.DefaultServlet/Range
 * from the Apache Tomcat project.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencms.webdav;

/**
 * Helper class for the WebDAV servlet. Holds values for a range.<p>
 *
 * @since 6.5.6
 */
public class CmsWebdavRange {

    /** The end of the range. */
    private long m_end;

    /** The length of the range. */
    private long m_length;

    /** The start of the range. */
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
     * Resets this range.<p>
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
     * Validate range.<p>
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
