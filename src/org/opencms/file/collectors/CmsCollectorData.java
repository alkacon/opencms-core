/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/collectors/CmsCollectorData.java,v $
 * Date   : $Date: 2005/03/23 19:08:22 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
 
package org.opencms.file.collectors;

/**
 * Data structure for the collector, parsed from the collector parameters.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * 
 * @version $Revision: 1.2 $
 * @since 5.7.2
 */
public class CmsCollectorData {

    /** The display count. */
    private int m_count;

    /** The absolute file name. */
    private String m_fileName;

    /** The file type id. */
    private int m_type;

    /**
     * Creates a new collector data set.<p>
     * 
     * @param data the data to parse
     */
    public CmsCollectorData(String data) {

        if (data == null) {
            throw new IllegalArgumentException(
                "Collector requires a parameter in the form '/sites/default/myfolder/file_${number}.html|11|4'");
        }

        int pos1 = data.indexOf('|');
        if (pos1 == -1) {
            throw new IllegalArgumentException("Malformed collector parameter '" + data + "'");
        }

        int pos2 = data.indexOf('|', pos1 + 1);
        if (pos2 == -1) {
            pos2 = data.length();
            m_count = 0;
        } else {
            m_count = Integer.valueOf(data.substring(pos2 + 1)).intValue();
        }

        m_fileName = data.substring(0, pos1);
        m_type = Integer.valueOf(data.substring(pos1 + 1, pos2)).intValue();
    }

    /**
     * Returns the count.<p>
     *
     * @return the count
     */
    public int getCount() {

        return m_count;
    }

    /**
     * Returns the file name.<p>
     *
     * @return the file name
     */
    public String getFileName() {

        return m_fileName;
    }

    /**
     * Returns the type.<p>
     *
     * @return the type
     */
    public int getType() {

        return m_type;
    }
}
