/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/editors/fckeditor/CmsFCKEditorConfiguration.java,v $
 * Date   : $Date: 2011/03/23 14:53:09 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.editors.fckeditor;

import javax.servlet.http.HttpSession;

/**
 * The configuration is passed to the external FCKeditor Javascript configuration file.<p>
 * 
 * This is done by using the current users session and storing an instance of the configuration object in the session.<p>
 *
 * @author  Andreas Zahner 
 * 
 * @version $Revision: 1.7 $ 
 * 
 * @since 6.1.7
 */
public class CmsFCKEditorConfiguration {

    /** Session attribute name to use when storing an object instance in the session. */
    public static final String SESSION_ATTRIBUTE = "__fckeditorconfig";

    /** The path of the edited resource. */
    private String m_resourcePath;

    /** The URI of the CSS style sheet to use for the editor. */
    private String m_uriStyleSheet;

    /**
     * Constructor without parameters.<p>
     */
    public CmsFCKEditorConfiguration() {

        // nothing to do here
    }

    /**
     * Returns the configuration object stored in the given session.<p>
     * 
     * Before returning the found object, the attribute is removed from the given session.<p>
     * 
     * @param session the session containing the configuration
     * @return the configuration object stored in the given session or a new configuration object if no object was found
     */
    public static CmsFCKEditorConfiguration getConfiguration(HttpSession session) {

        Object o = session.getAttribute(SESSION_ATTRIBUTE);
        if (o != null && o instanceof CmsFCKEditorConfiguration) {
            session.removeAttribute(SESSION_ATTRIBUTE);
            return (CmsFCKEditorConfiguration)o;
        }
        return new CmsFCKEditorConfiguration();
    }

    /**
     * Returns the path of the edited resource.<p>
     * 
     * @return the path of the edited resource
     */
    public String getResourcePath() {

        return m_resourcePath;
    }

    /**
     * Returns the URI of the CSS style sheet to use for the editor.<p>
     * 
     * @return the URI of the CSS style sheet to use for the editor
     */
    public String getUriStyleSheet() {

        return m_uriStyleSheet;
    }

    /**
     * Stores the configuration in the given session.<p>
     * 
     * @param session the session to store the configuration
     */
    public void setConfiguration(HttpSession session) {

        session.setAttribute(SESSION_ATTRIBUTE, this);
    }

    /**
     * Sets the path of the edited resource.<p>
     * 
     * @param resourcePath the path of the edited resource
     */
    public void setResourcePath(String resourcePath) {

        m_resourcePath = resourcePath;
    }

    /**
     * Sets the URI of the CSS style sheet to use for the editor.<p>
     * 
     * @param uriStyleSheet the URI of the CSS style sheet to use for the editor
     */
    public void setUriStyleSheet(String uriStyleSheet) {

        m_uriStyleSheet = uriStyleSheet;
    }

}