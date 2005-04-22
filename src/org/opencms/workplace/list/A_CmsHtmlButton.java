/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/Attic/A_CmsHtmlButton.java,v $
 * Date   : $Date: 2005/04/22 08:38:52 $
 * Version: $Revision: 1.1 $
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

package org.opencms.workplace.list;

/**
 * Default skeleton for an html button.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.1 $
 * @since 5.7.3
 */
public abstract class A_CmsHtmlButton implements I_CmsHtmlButton {

    /** Enabled flag. */
    private boolean m_enabled;

    /** Help text or description. */
    private final String m_helpText;

    /** unique id. */
    private final String m_id;

    /** Display name. */
    private final String m_name;

    /**
     * Default Constructor.<p>
     * 
     * @param id the unique id
     * @param name the display name
     * @param helpText the help text
     * @param enabled if enabled or disabled
     */
    public A_CmsHtmlButton(String id, String name, String helpText, boolean enabled) {

        m_id = id;
        m_name = name;
        m_helpText = helpText;
        m_enabled = enabled;
    }

    /**
     * @see org.opencms.workplace.list.I_CmsHtmlButton#getHelpText()
     */
    public String getHelpText() {

        return m_helpText;
    }

    /**
     * @see org.opencms.workplace.list.I_CmsHtmlButton#getId()
     */
    public String getId() {

        return m_id;
    }

    /**
     * @see org.opencms.util.I_CmsNamedObject#getName()
     */
    public String getName() {

        return m_name;
    }

    /**
     * @see org.opencms.workplace.list.I_CmsHtmlButton#isEnabled()
     */
    public boolean isEnabled() {

        return m_enabled;
    }

    /**
     * @see org.opencms.workplace.list.I_CmsHtmlButton#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {

        m_enabled = enabled;
    }
}