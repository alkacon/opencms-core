/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/tools/A_CmsToolHandler.java,v $
 * Date   : $Date: 2005/04/29 16:05:53 $
 * Version: $Revision: 1.7 $
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

import org.opencms.file.CmsObject;

/**
 * Helper class to build easily other admin tool handlers.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.7 $
 * @since 5.7.3
 */
public abstract class A_CmsToolHandler implements I_CmsToolHandler {

    /** Args separator in the property value.<p> */
    public static final String C_ARGS_SEPARATOR = "|";

    /** Property for the args.<p> */
    public static final String C_PROPERTY_DEFINITION = "admintoolhandler-args";

    /** Arg-name and arg-value separator.<p> */
    public static final String C_VALUE_SEPARATOR = ":";

    /** Group to be included in. */
    private String m_group;

    /** Help text or description. */
    private String m_helpText;

    /** Icon path (32x32). */
    private String m_iconPath;

    /** Link pointer. */
    private String m_link;

    /** Display name. */
    private String m_name;

    /** Tool path to install in. */
    private String m_path;

    /** Relative position in group. */
    private float m_position;

    /** Small icon path (16x16). */
    private String m_smallIconPath;

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getGroup()
     */
    public String getGroup() {

        return m_group;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getHelpText()
     */
    public String getHelpText() {

        return m_helpText;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getIconPath()
     */
    public String getIconPath() {

        return m_iconPath;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getLink()
     */
    public String getLink() {

        return m_link;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getName()
     */
    public String getName() {

        return m_name;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getPath()
     */
    public String getPath() {

        return m_path;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getPosition()
     */
    public float getPosition() {

        return m_position;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getSmallIconPath()
     */
    public String getSmallIconPath() {

        return m_smallIconPath;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#isEnabled(org.opencms.file.CmsObject)
     */
    public boolean isEnabled(CmsObject cms) {

        return true;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#isVisible(org.opencms.file.CmsObject)
     */
    public boolean isVisible(CmsObject cms) {

        return true;
    }

    /**
     * Sets the group.<p>
     *
     * @param group the group to set
     */
    public void setGroup(String group) {

        m_group = group;
    }

    /**
     * Sets the help text.<p>
     *
     * @param helpText the help text to set
     */
    public void setHelpText(String helpText) {

        m_helpText = helpText;
    }

    /**
     * Sets the icon path.<p>
     *
     * @param iconPath the icon path to set
     */
    public void setIconPath(String iconPath) {

        m_iconPath = iconPath;
    }

    /**
     * Sets the link.<p>
     *
     * @param link the link to set
     */
    public void setLink(String link) {

        m_link = link;
    }

    /**
     * Sets the name.<p>
     *
     * @param name the name to set
     */
    public void setName(String name) {

        m_name = name;
    }

    /**
     * Sets the path.<p>
     *
     * @param path the path to set
     */
    public void setPath(String path) {

        m_path = path;
    }

    /**
     * Sets the position.<p>
     *
     * @param position the position to set
     */
    public void setPosition(float position) {

        m_position = position;
    }

    /**
     * Sets the small icon path.<p>
     *
     * @param smallIconPath the samll icon path to set
     */
    public void setSmallIconPath(String smallIconPath) {

        m_smallIconPath = smallIconPath;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        return m_path + " - " + m_group + " - " + m_position;
    }
    
}