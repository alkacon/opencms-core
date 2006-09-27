/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/relations/CmsRelationType.java,v $
 * Date   : $Date: 2006/09/27 10:56:01 $
 * Version: $Revision: 1.1.2.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.relations;

import org.opencms.main.CmsIllegalArgumentException;

import java.io.Serializable;
import java.util.Locale;

/**
 * Wrapper class for
 * the different types of relations.<p>
 * 
 * The possibles values are:<br>
 * <ul>
 *   <li>{@link #HYPERLINK}</li>
 *   <li>{@link #EMBEDDED_IMAGE}</li>
 *   <li>{@link #ATTACHMENT}</li>
 *   <li>{@link #REFERENCE}</li>
 * </ul>
 * <p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.1.2.4 $
 * 
 * @since 6.3.0
 */
public final class CmsRelationType implements Serializable {

    /** Constant for the modified files only view.     */
    public static final CmsRelationType ATTACHMENT = new CmsRelationType("ATTACHMENT", 3);

    /** Constant for the deleted files only view.  */
    public static final CmsRelationType EMBEDDED_IMAGE = new CmsRelationType("IMG", 2);

    /** Constant for the all changes view. */
    public static final CmsRelationType HYPERLINK = new CmsRelationType("A", 1);

    /** Constant for the new files only view. */
    public static final CmsRelationType REFERENCE = new CmsRelationType("REFERENCE", 4);

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -4060567973007877250L;

    /** Array constant for all available align types. */
    private static final CmsRelationType[] VALUE_ARRAY = {HYPERLINK, EMBEDDED_IMAGE, ATTACHMENT, REFERENCE};

    /** Internal representation. */
    private final int m_mode;

    /** For &lt;link&gt; tag representation. */
    private final String m_type;

    /**
     * Private constructor.<p>
     * 
     * @param type the type key value
     * @param mode the internal representation
     */
    private CmsRelationType(String type, int mode) {

        m_type = type;
        m_mode = mode;
    }

    /**
     * Parses an <code>int</code> into an element of this enumeration.<p>
     *
     * @param type the internal representation number to parse
     * 
     * @return the enumeration element
     * 
     * @throws CmsIllegalArgumentException if the given value could not be matched against a 
     *         <code>{@link CmsRelationType}</code> object.
     */
    public static CmsRelationType valueOf(int type) throws CmsIllegalArgumentException {

        if ((type > 0) && (type <= VALUE_ARRAY.length)) {
            return VALUE_ARRAY[type - 1];
        }
        throw new CmsIllegalArgumentException(org.opencms.db.Messages.get().container(
            org.opencms.db.Messages.ERR_MODE_ENUM_PARSE_2,
            new Integer(type),
            CmsRelationType.class.getName()));
    }

    /**
     * Parses an <code>String</code> into an element of this enumeration.<p>
     *
     * @param value the type to parse
     * 
     * @return the enumeration element
     * 
     * @throws CmsIllegalArgumentException if the given value could not be matched against a 
     *         <code>{@link CmsRelationType}</code> object.
     */
    public static CmsRelationType valueOf(String value) throws CmsIllegalArgumentException {

        String valueUp = value.toUpperCase();
        for (int i = 0; i < VALUE_ARRAY.length; i++) {
            if (valueUp.equals(VALUE_ARRAY[i].m_type)) {
                return VALUE_ARRAY[i];
            }
        }
        throw new CmsIllegalArgumentException(org.opencms.db.Messages.get().container(
            org.opencms.db.Messages.ERR_MODE_ENUM_PARSE_2,
            value,
            CmsRelationType.class.getName()));
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj instanceof CmsRelationType) {
            return m_mode == ((CmsRelationType)obj).m_mode;
        }
        return false;
    }

    /**
     * Returns a localized name for the given relation type.<p>
     * 
     * @param locale the locale
     * 
     * @return a localized name
     */
    public String getLocalizedName(Locale locale) {

        String nameKey = null;
        switch (getMode()) {
            // hyperlink
            case 1:
                nameKey = Messages.GUI_RELATION_TYPE_HYPERLINK_0;
                break;
            // embedded image
            case 2:
                nameKey = Messages.GUI_RELATION_TYPE_EMBEDDED_IMAGE_0;
                break;
            // attachment
            case 3:
                nameKey = Messages.GUI_RELATION_TYPE_ATTACHMENT_0;
                break;
            // reference
            case 4:
            default:
                nameKey = Messages.GUI_RELATION_TYPE_REFERENCE_0;
                break;
        }
        return Messages.get().getBundle(locale).key(nameKey);
    }

    /**
     * Returns the internal representation of this type.<p>
     *
     * @return the internal representation of this type
     */
    public int getMode() {

        return m_mode;
    }

    /**
     * Returns the type value.<p>
     * 
     * @return the type value
     */
    public String getType() {

        return m_type;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        return m_mode;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        return m_type;
    }
}