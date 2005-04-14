/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/tools/A_CmsToolHandler.java,v $
 * Date   : $Date: 2005/04/14 13:11:15 $
 * Version: $Revision: 1.3 $
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helper class to build easily other admin tool handlers.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.3 $
 * @since 5.7.3
 */
public abstract class A_CmsToolHandler implements I_CmsToolHandler {

    /** The args separator in the property value.<p> */
    public static final String C_ARGS_SEPARATOR = "|";

    /** The name of the only-admin parameter.<p> */
    public static final String C_PARAM_ONLYADMIN = "onlyadmin";

    /** The name of the only-offline parameter.<p> */
    public static final String C_PARAM_ONLYOFFLINE = "onlyoffline";

    /** The property for the args.<p> */
    public static final String C_PROPERTY_DEFINITION = "admintoolhandler-args";

    /** The arg-name and arg-value separator.<p> */
    public static final String C_VALUE_SEPARATOR = ":";

    private String m_helpText;
    private String m_iconPath;
    private String m_smallIconPath;
    private List m_installPoints = new ArrayList();
    private String m_link;
    private String m_name;

    /**
     * Adds an install points.<p>
     *
     * @param installPoint the install point to add
     */
    public void addInstallPoint(CmsToolInstallPoint installPoint) {

        m_installPoints.add(installPoint);
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
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getSmallIconPath()
     */
    public String getSmallIconPath() {

        return m_smallIconPath;
    }
    
    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#getInstallPoints()
     */
    public List getInstallPoints() {

        return Collections.unmodifiableList(m_installPoints);
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
     * @see org.opencms.workplace.tools.I_CmsToolHandler#isEnabled(org.opencms.file.CmsObject)
     */
    public boolean isEnabled(CmsObject cms) {

        return true;
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
     * Sets the small icon path.<p>
     *
     * @param smallIconPath the samll icon path to set
     */
    public void setSmallIconPath(String smallIconPath) {

        m_smallIconPath = smallIconPath;
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

}