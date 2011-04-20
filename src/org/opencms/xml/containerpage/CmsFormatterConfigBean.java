/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/containerpage/Attic/CmsFormatterConfigBean.java,v $
 * Date   : $Date: 2011/04/20 07:07:48 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.xml.containerpage;


/**
 * A bean containing formatter configuration data as strings.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 */
public class CmsFormatterConfigBean {

    /** The formatter jsp. */
    private String m_jsp;

    /** The formatter max width. */
    private String m_maxWidth;

    /** The formatter container type. */
    private String m_type;

    /** The formatter width. */
    private String m_width;

    /**
     * Constructor.<p>
     * 
     * @param jsp the formatter jsp 
     * @param type the formatter container type 
     * @param width the formatter width
     * @param maxWidth the formatter max width 
     */
    public CmsFormatterConfigBean(String jsp, String type, String width, String maxWidth) {

        m_jsp = jsp;
        m_type = type;
        m_width = width;
        m_maxWidth = maxWidth;
    }

    /**
     * Returns the formatter jsp.<p>
     * 
     * @return the formatter jsp 
     */
    public String getJsp() {

        return m_jsp;
    }

    /**
     * Gets the maximum width as a string.<p>
     * 
     * @return the maximum width as a string 
     */
    public String getMaxWidth() {

        return m_maxWidth;
    }

    /**
     * Returns the formatter container type.<p>
     * 
     * @return the formatter container type 
     */
    public String getType() {

        return m_type;
    }

    /**
     * Returns the formatter width.<p>
     * 
     * @return the formatter width 
     */
    public String getWidth() {

        return m_width;
    }

}
