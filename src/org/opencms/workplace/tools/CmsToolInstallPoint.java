/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/tools/Attic/CmsToolInstallPoint.java,v $
 * Date   : $Date: 2005/02/17 12:44:32 $
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

package org.opencms.workplace.tools;

/**
 * This class defines an installation point for an administration tool.<p>
 * 
 * The <code>position</code> is a number for relative ordering, 
 * following the small-number-first rule.<p> 
 * 
 * @author <a href="mailto:m.moossen@alkacon.com">Michael Moossen</a> 
 * @version $Revision: 1.2 $
 * @since 6.0
 */
public class CmsToolInstallPoint {

    private static final String C_GROUP_SEPARATOR = "@";
    private static final String C_POSITION_SEPARATOR = "#";
    private final String m_group;

    private final String m_path;
    private final float m_position;

    /**
     * Ctor for decomposing a installation point from a single parameter.<p>
     * 
     * The parameter should be a comma-separated list of valid installation 
     * points. An installation point has 3 parts:<br>
     * <ul>
     *  <li><code>path</code>: the abstract path to install the tool.</li>
     *  <li><code>group</code>: the group in that path.</li>
     *  <li><code>position</code>: the relative position in that group.</li>
     * </ul><p>
     * 
     * The <code>group</code> can use macros, for i18n.
     * 
     * @param iPoint the composed installation point
     */
    public CmsToolInstallPoint(String iPoint) {

        int pos1 = iPoint.indexOf(C_GROUP_SEPARATOR);
        int pos2 = iPoint.indexOf(C_POSITION_SEPARATOR);
        m_path = iPoint.substring(0, pos1);
        m_group = iPoint.substring(pos1 + 1, pos2);
        m_position = Float.parseFloat(iPoint.substring(pos2 + 1));
    }

    /**
     * Default Ctor.<p>
     * 
     * @param path the path to install an admin tool
     * @param group the group in that path
     * @param position the position in that group
     */
    public CmsToolInstallPoint(String path, String group, float position) {

        m_path = path;
        m_group = group;
        m_position = position;
    }

    /**
     * Returns the group.<p>
     *
     * @return the group
     */
    public String getGroup() {

        return m_group;
    }

    /**
     * Returns the path.<p>
     *
     * @return the path
     */
    public String getPath() {

        return m_path;
    }

    /**
     * Returns the position.<p>
     *
     * @return the position
     */
    public float getPosition() {

        return m_position;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        return m_path + C_GROUP_SEPARATOR + m_group + C_POSITION_SEPARATOR + m_position;
    }
}