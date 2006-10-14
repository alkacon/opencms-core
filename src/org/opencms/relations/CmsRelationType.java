/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/relations/CmsRelationType.java,v $
 * Date   : $Date: 2006/10/14 08:44:57 $
 * Version: $Revision: 1.1.2.7 $
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
 *   <li>{@link #XML_STRONG}</li>
 *   <li>{@link #XML_WEAK}</li>
 *   <li>{@link #JSP_STRONG}</li>
 *   <li>{@link #JSP_WEAK}</li>
 * </ul>
 * <p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.1.2.7 $
 * 
 * @since 6.3.0
 */
public final class CmsRelationType implements Serializable {

    // the following strings must not be public because they confuse the interface
    // this means we can't sort this class members according to standard
    /** String prefix for 'JSP relations. */
    private static final String PREFIX_JSP = "JSP_";

    /** String prefix for XML relations. */
    private static final String PREFIX_XML = "XML_";

    /** String constant for "STRONG" relations. */
    private static final String VALUE_STRONG = "STRONG";

    /** String constant for "WEAK" relations. */
    private static final String VALUE_WEAK = "WEAK";

    /** Constant for the <code>&lta href=''&gt</code> tag in a html page/element. */
    public static final CmsRelationType HYPERLINK = new CmsRelationType("A", 1);

    /** Constant for the <code>&ltimg src=''&gt</code> tag in a html page/element. */
    public static final CmsRelationType EMBEDDED_IMAGE = new CmsRelationType("IMG", 2);

    /** Constant for the <code>OpenCmsVfsFile</code> values in xml content that were defined as 'strong' links. */
    public static final CmsRelationType XML_STRONG = new CmsRelationType(PREFIX_XML + VALUE_STRONG, 3);

    /** Constant for the <code>OpenCmsVfsFile</code> values in xml content that were defined as 'weak' links. */
    public static final CmsRelationType XML_WEAK = new CmsRelationType(PREFIX_XML + VALUE_WEAK, 4);

    /** Constant for the all types of links in a jsp file using the <code>link.strong</code> macro. */
    public static final CmsRelationType JSP_STRONG = new CmsRelationType(PREFIX_JSP + VALUE_STRONG, 5);

    /** Constant for the all types of links in a jsp file using the <code>link.weak</code> macro. */
    public static final CmsRelationType JSP_WEAK = new CmsRelationType(PREFIX_JSP + VALUE_WEAK, 6);

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -4060567973007877250L;

    /** Array constant for all available align types. */
    private static final CmsRelationType[] VALUE_ARRAY = {
        HYPERLINK,
        EMBEDDED_IMAGE,
        XML_STRONG,
        XML_WEAK,
        JSP_STRONG,
        JSP_WEAK};

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
     *         <code>{@link CmsRelationType}</code> object
     *         
     * @see #valueOfXml(String)
     * @see #valueOfJsp(String)
     */
    public static CmsRelationType valueOf(String value) throws CmsIllegalArgumentException {

        CmsRelationType result = valueOfInternal(value);
        if (result == null) {
            // no type found
            throw new CmsIllegalArgumentException(org.opencms.db.Messages.get().container(
                org.opencms.db.Messages.ERR_MODE_ENUM_PARSE_2,
                value,
                CmsRelationType.class.getName()));
        }
        return result;
    }

    /**
     * Internal parse method.<p>
     * 
     * @param value the type to parse
     * 
     * @return the enumeration element, or <code>null</code> if no matching element is found
     */
    private static CmsRelationType valueOfInternal(String value) {

        if (value != null) {
            String valueUp = value.toUpperCase();
            for (int i = 0; i < VALUE_ARRAY.length; i++) {
                if (valueUp.equals(VALUE_ARRAY[i].m_type)) {
                    return VALUE_ARRAY[i];
                }
            }
            // deprecated types
            if (valueUp.equals("REFERENCE") || valueUp.equals("XML_REFERENCE")) {
                return XML_WEAK;
            } else if (valueUp.equals("ATTACHMENT") || valueUp.equals("XML_ATTACHMENT")) {
                return XML_STRONG;
            }
        }
        return null;
    }

    /**
     * Parses the given value into a valid enumeration element for a JSP relation type.<p>
     * 
     * This should be used to extend Strings like "weak" or "strong" to full relation type descriptors
     * for JSP pages like "JSP_WEAK" or "JSP_STRONG".<p> 
     * 
     * @param value the name to get the JSP type for
     * 
     * @return the JSP enumeration element
     * 
     * @see #valueOf(String)
     */
    public static CmsRelationType valueOfJsp(String value) {

        CmsRelationType result = valueOfInternal(value);
        if (result == null) {
            result = valueOf(PREFIX_JSP + value);
        }
        return result;
    }

    /**
     * Parses the given value into a valid enumeration element for a XML relation type.<p>
     * 
     * This should be used to extend Strings like "weak" or "strong" to full relation type descriptors
     * for XML documents like "XML_WEAK" or "XML_STRONG".<p> 
     * 
     * @param value the name to get the XML type for
     * 
     * @return the XML enumeration element
     * 
     * @see #valueOf(String)
     */
    public static CmsRelationType valueOfXml(String value) {

        CmsRelationType result = valueOfInternal(value);
        if (result == null) {
            result = valueOf(PREFIX_XML + value);
        }
        return result;
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
            case 1: // hyperlink
                nameKey = Messages.GUI_RELATION_TYPE_HYPERLINK_0;
                break;
            case 2: // embedded image
                nameKey = Messages.GUI_RELATION_TYPE_EMBEDDED_IMAGE_0;
                break;
            case 3: // xml strong
                nameKey = Messages.GUI_RELATION_TYPE_XML_STRONG_0;
                break;
            case 4: // xml weak
                nameKey = Messages.GUI_RELATION_TYPE_XML_WEAK_0;
                break;
            case 5: // jsp strong
                nameKey = Messages.GUI_RELATION_TYPE_JSP_STRONG_0;
                break;
            case 6: // jsp weak
                nameKey = Messages.GUI_RELATION_TYPE_JSP_WEAK_0;
                break;
            default:
                return Messages.get().getBundle(locale).key(
                    Messages.GUI_RELATION_TYPE_UNKNOWN_1,
                    new Integer(getMode()));
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
     * Returns the short type name.<p>
     * 
     * The short type name of XML or JSP types is only <code>"WEAK"</code> or <code>"STRONG"</code>.
     * For other types the short name is equal to the full name.<p> 
     * In case you need the full type name, use {@link #getType()}.<p>
     * 
     * @return the short type name
     * 
     * @see #getType()
     * @see CmsRelationType#valueOfJsp(String)
     * @see CmsRelationType#valueOfXml(String)
     */
    public String getTypeShort() {

        String result;
        switch (getMode()) {
            case 3: // xml strong
                result = VALUE_STRONG;
                break;
            case 4: // xml weak
                result = VALUE_WEAK;
                break;
            case 5: // jsp strong
                result = VALUE_STRONG;
                break;
            case 6: // jsp weak
                result = VALUE_WEAK;
                break;
            default:
                result = getType();
        }
        return result;
    }

    /**
     * Returns the full type name.<p>
     * 
     * This will always be the "full" descriptor like e.g. <code>"XML_WEAK"</code>.
     * In case you need only the short form, use {@link #getTypeShort()}.<p>
     * 
     * @return the full type name
     * 
     * @see #getTypeShort()
     * @see CmsRelationType#valueOf(String) 
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