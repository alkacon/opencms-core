/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/content/check/Attic/CmsContentCheckProperetyObject.java,v $
 * Date   : $Date: 2005/10/25 15:14:32 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.tools.content.check;

import java.util.Collections;
import java.util.List;

/**
 * This class encapsulates the configuration of one property check used by the 
 * property content check.<p>
 * 
 * @author Michael Emmerich  
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.1.2 
 */
public class CmsContentCheckProperetyObject {

    /** Constant for type file or folder.*/
    public static final String TYPE_BOTH = "both";

    /** Constant for type file. */
    public static final String TYPE_FILE = "file";

    /** Constant for type folder. */
    public static final String TYPE_FOLDER = "folder";

    /** Flag for checking if the property value is empty. */
    private boolean m_empty;

    /** Flag for checking if the property value contains the filename. */
    private boolean m_filename;

    /** The minimum length of the property value. */
    private int m_length;

    /** The propertyname. */
    private String m_propertyname;

    /** The resourcetype (file or folder). */
    private String m_type;

    /** The property value to check for. */
    private List m_value;

    /** 
     * Constructor, creates a new empty CmsContentCheckProperetyObject.<p>     *
     */
    public CmsContentCheckProperetyObject() {

        m_propertyname = null;
        m_type = TYPE_BOTH;
        m_empty = false;
        m_filename = false;
        m_length = -1;
        m_value = Collections.EMPTY_LIST;
    }

    /**
     * Returns the minimum length.<p>
     *
     * @return the minimum length
     */
    public int getLength() {

        return m_length;
    }

    /**
     * Returns the propertyname.<p>
     *
     * @return the propertyname
     */
    public String getPropertyname() {

        return m_propertyname;
    }

    /**
     * Returns the type.<p>
     *
     * @return the type
     */
    public String getType() {

        return m_type;
    }

    /**
     * Returns the value.<p>
     *
     * @return the value
     */
    public List getValue() {

        return m_value;
    }

    /**
     * Returns the empty flag.<p>
     *
     * @return the empty flag
     */
    public boolean isEmpty() {

        return m_empty;
    }

    /**
     * Returns the filename flag.<p>
     *
     * @return the filename flag
     */
    public boolean isFilename() {

        return m_filename;
    }

    /**
     * Sets the empty flag.<p>
     *
     * @param empty the empty flag to set
     */
    public void setEmpty(boolean empty) {

        m_empty = empty;
    }

    /**
     * Sets the filename flag.<p>
     *
     * @param filename the filename flag to set
     */
    public void setFilename(boolean filename) {

        m_filename = filename;
    }

    /**
     * Sets the minimum length.<p>
     *
     * @param length the minimum length to set
     */
    public void setLength(int length) {

        m_length = length;
    }

    /**
     * Sets the propertyname.<p>
     *
     * @param propertyname the propertyname to set
     */
    public void setPropertyname(String propertyname) {

        m_propertyname = propertyname;
    }

    /**
     * Sets the type.<p>
     *
     * @param type the type to set
     */
    public void setType(String type) {

        m_type = type;
    }

    /**
     * Sets the value.<p>
     *
     * @param value the value to set
     */
    public void setValue(List value) {

        m_value = value;
    }

    /**
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {

        StringBuffer buf = new StringBuffer();
        buf.append(this.getClass().getName());
        buf.append(" [Propertyname=");
        buf.append(m_propertyname);
        buf.append(" Type=");
        buf.append(m_type);
        buf.append(" emptycheck=");
        buf.append(m_empty);
        buf.append(" filenamecheck=");
        buf.append(m_filename);
        buf.append(" min length=");
        buf.append(m_length);
        buf.append(" value=");
        buf.append(m_value);
        buf.append("]");
        return buf.toString();
    }

}
