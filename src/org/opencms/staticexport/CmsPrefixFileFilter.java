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

package org.opencms.staticexport;

import java.io.File;
import java.io.FileFilter;

/**
 * Implements the file filter used to guess the right suffix of a deleted jsp file.<p>
 *
 * @since 8.0.0
 */
public class CmsPrefixFileFilter implements FileFilter {

    /** The base file. */
    private String m_baseName;

    /**
     * Creates a new instance of this filter.<p>
     *
     * @param fileName the base file to compare with.
     */
    public CmsPrefixFileFilter(String fileName) {

        m_baseName = fileName + ".";
    }

    /**
     * Accepts the given file if its name starts with the name of of the base file (without extension)
     * and ends with the extension.<p>
     *
     * @see java.io.FileFilter#accept(java.io.File)
     */
    public boolean accept(File f) {

        return f.getName().startsWith(m_baseName)
            && (f.getName().length() > m_baseName.length())
            && (f.getName().indexOf('.', m_baseName.length()) < 0);
    }
}
