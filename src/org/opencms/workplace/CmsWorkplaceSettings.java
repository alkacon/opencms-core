/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/CmsWorkplaceSettings.java,v $
 * Date   : $Date: 2003/06/06 16:47:10 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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
package org.opencms.workplace;


/**
 * Object to conveniently access and modify the state of the workplace for a user,
 * will be stored in the session of a user.<p>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 5.1
 */
public class CmsWorkplaceSettings {
    
    private String m_language;
    private CmsWorkplaceMessages m_messages;
    
    /**
     * Constructor, only package visible.<p>
     */
    CmsWorkplaceSettings() {}
    
    /**
     * Returns the currently selected user language.<p>
     * 
     * @return the currently selected user language
     */
    public String getLanguage() {
        return m_language;
    }

    /**
     * Sets the selected user language.<p>
     * 
     * @param value the selected user language
     */
    public void setLanguage(String value) {
        m_language = value;
    }

    /**
     * Returns the initialized workplace messages for the current user.<p>
     * 
     * @return the initialized workplace messages for the current user
     */
    public CmsWorkplaceMessages getMessages() {
        return m_messages;
    }

    /**
     * Sets the workplace messages for the current user.<p>
     * 
     * @param messages the workplace messages for the current user
     */
    public void setMessages(CmsWorkplaceMessages messages) {
        m_messages = messages;
    }

}
