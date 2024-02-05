/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsInitException;
import org.opencms.main.OpenCms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Wrapper class for
 * the different types of relations.<p>
 *
 * The possibles values are:<br>
 * <ul>
 *   <li>{@link #HYPERLINK}</li>
 *   <li>{@link #EMBEDDED_IMAGE}</li>
 *   <li>{@link #EMBEDDED_OBJECT}</li>
 *   <li>{@link #XML_STRONG}</li>
 *   <li>{@link #XML_WEAK}</li>
 *   <li>{@link #JSP_STRONG}</li>
 *   <li>{@link #JSP_WEAK}</li>
 *   <li>{@link #OU_RESOURCE}</li>
 *   <li>{@link #CATEGORY}</li>
 *   <li>{@link #XSD}</li>
 * </ul>
 * <p>
 *
 * User defined relation types are also available.<p>
 *
 * @since 6.3.0
 */
public final class CmsRelationType implements Serializable {

    /**
     * Enum representing how relations should be handled while copying resources.<p>
     */
    public enum CopyBehavior {
        /** Copy the relation when copying a resource. */
        copy,

        /** Ignore the relation when copying a resource. */
        ignore;
    }

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

    /** Constant for the category of an <code>OpenCmsVfsFile</code>. */
    public static final CmsRelationType CATEGORY = new CmsRelationType(9, "CATEGORY", false, false, CopyBehavior.copy);

    /** Constant for the <code>&lt;img src=''&gt;</code> tag in a html page/element. */
    public static final CmsRelationType EMBEDDED_IMAGE = new CmsRelationType(2, "IMG", true, true, CopyBehavior.copy);

    /** Constant for the <code>&lt;embed src=''&gt;</code> tag in a html page/element. */
    public static final CmsRelationType EMBEDDED_OBJECT = new CmsRelationType(
        7,
        "OBJECT",
        true,
        true,
        CopyBehavior.copy);

    /** Constant for the <code>&lt;a href=''&gt;</code> tag in a html page/element. */
    public static final CmsRelationType HYPERLINK = new CmsRelationType(1, "A", false, true, CopyBehavior.copy);

    /** Constant for the index content relation, telling the content of a linked resource should be merged into
     * the content of the linking XML.
     */
    public static final CmsRelationType INDEX_CONTENT = new CmsRelationType(
        13,
        "INDEX_CONTENT",
        true,
        true,
        CopyBehavior.copy);

    /** Constant for the all types of links in a jsp file using the <code>link.strong</code> macro. */
    public static final CmsRelationType JSP_STRONG = new CmsRelationType(
        5,
        PREFIX_JSP + VALUE_STRONG,
        true,
        true,
        CopyBehavior.copy);

    /** Constant for the all types of links in a jsp file using the <code>link.weak</code> macro. */
    public static final CmsRelationType JSP_WEAK = new CmsRelationType(
        6,
        PREFIX_JSP + VALUE_WEAK,
        false,
        true,
        CopyBehavior.copy);

    /** Constant for the organizational units resource associations. */
    public static final CmsRelationType OU_RESOURCE = new CmsRelationType(8, "OU", false, false, CopyBehavior.copy);

    /** Constant for the <code>OpenCmsVfsFile</code> values in xml content that were defined as 'strong' links. */
    public static final CmsRelationType XML_STRONG = new CmsRelationType(
        3,
        PREFIX_XML + VALUE_STRONG,
        true,
        true,
        CopyBehavior.copy);

    /** Constant for the <code>OpenCmsVfsFile</code> values in xml content that were defined as 'weak' links. */
    public static final CmsRelationType XML_WEAK = new CmsRelationType(
        4,
        PREFIX_XML + VALUE_WEAK,
        false,
        true,
        CopyBehavior.copy);

    /** Constant for the type of relations between resources which are locale variants. */
    public static final CmsRelationType LOCALE_VARIANT = new CmsRelationType(
        11,
        "LOCALE_VARIANT",
        false,
        false,
        CopyBehavior.ignore);

    /** Constant for the type of relations between a detail content and its detail-only container pages. */
    public static final CmsRelationType DETAIL_ONLY = new CmsRelationType(
        12,
        "DETAIL_ONLY",
        true,
        false,
        CopyBehavior.ignore);

    /** Constant for the weak links from xmlcontent to the used xsd. */
    public static final CmsRelationType XSD = new CmsRelationType(10, "XSD", true, true, CopyBehavior.copy);

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -4060567973007877250L;

    /** Constant indicating the starting mode for user defined relation types. */
    private static final int USER_DEFINED_MODE_LIMIT = 100;

    /** Array constant for all available system relation types. */
    private static final CmsRelationType[] VALUE_ARRAY = {
        HYPERLINK,
        EMBEDDED_IMAGE,
        XML_STRONG,
        XML_WEAK,
        JSP_STRONG,
        JSP_WEAK,
        EMBEDDED_OBJECT,
        OU_RESOURCE,
        CATEGORY,
        XSD,
        LOCALE_VARIANT,
        DETAIL_ONLY,
        INDEX_CONTENT};

    /** The copy behavior. */
    private CopyBehavior m_copyBehavior = CopyBehavior.copy;

    /** Flag to indicate if the relations of this type are parsed from the content or not. */
    private final boolean m_defInContent;

    /** Internal representation. */
    private final int m_id;

    /** Some name for this relation type, ie. for &lt;link&gt; tag representation. */
    private final String m_name;

    /** Flag to indicate if the relations of this type are strong or weak. */
    private final boolean m_strong;

    /**
     * Public constructor for user defined relation types.<p>
     *
     * @param id the id of the relation type
     * @param name the name of the relation
     * @param type the type of relation type, strong or weak
     */
    public CmsRelationType(int id, String name, String type) {

        m_name = name.toUpperCase();
        if (OpenCms.getRunLevel() > OpenCms.RUNLEVEL_2_INITIALIZING) {
            // allow relation type definitions only during startup
            throw new CmsInitException(Messages.get().container(Messages.ERR_RELATION_TYPE_INIT_1, m_name));
        }
        m_strong = type.toUpperCase().equals(VALUE_STRONG);
        m_defInContent = false;
        m_id = USER_DEFINED_MODE_LIMIT + id;
    }

    /**
     * Private constructor for system relation types.<p>
     *
     * @param id the internal representation
     * @param name the name of the relation
     * @param strong if the relation is strong or weak
     * @param defInContent <code>true</code> if the link is defined in the content
     * @param copyBehavior the copy behavior of the content
     */
    private CmsRelationType(int id, String name, boolean strong, boolean defInContent, CopyBehavior copyBehavior) {

        m_id = id;
        m_name = name;
        m_strong = strong;
        m_defInContent = defInContent;
        m_copyBehavior = copyBehavior;
    }

    /**
     * Returns all relation types in the given list that define relations in the content.<p>
     *
     * @param relationTypes the collection of relation types to filter
     *
     * @return a list of {@link CmsRelationType} objects
     */
    public static List<CmsRelationType> filterDefinedInContent(Collection<CmsRelationType> relationTypes) {

        List<CmsRelationType> result = new ArrayList<CmsRelationType>(relationTypes);
        Iterator<CmsRelationType> it = result.iterator();
        while (it.hasNext()) {
            CmsRelationType type = it.next();
            if (!type.isDefinedInContent()) {
                it.remove();
            }
        }
        return result;
    }

    /**
     * Returns all internal defined relation types in the given list.<p>
     *
     * @param relationTypes the collection of relation types to filter
     *
     * @return a list of {@link CmsRelationType} objects
     */
    public static List<CmsRelationType> filterInternal(Collection<CmsRelationType> relationTypes) {

        List<CmsRelationType> result = new ArrayList<CmsRelationType>(relationTypes);
        Iterator<CmsRelationType> it = result.iterator();
        while (it.hasNext()) {
            CmsRelationType type = it.next();
            if (!type.isInternal()) {
                it.remove();
            }
        }
        return result;
    }

    /**
     * Returns all relation types in the given list that are not defined in the content.<p>
     *
     * @param relationTypes the collection of relation types to filter
     *
     * @return a list of {@link CmsRelationType} objects
     */
    public static List<CmsRelationType> filterNotDefinedInContent(Collection<CmsRelationType> relationTypes) {

        List<CmsRelationType> result = new ArrayList<CmsRelationType>(relationTypes);
        Iterator<CmsRelationType> it = result.iterator();
        while (it.hasNext()) {
            CmsRelationType type = it.next();
            if (type.isDefinedInContent()) {
                it.remove();
            }
        }
        return result;
    }

    /**
     * Returns all strong relation types in the given list.<p>
     *
     * @param relationTypes the collection of relation types to filter
     *
     * @return a list of {@link CmsRelationType} objects
     */
    public static List<CmsRelationType> filterStrong(Collection<CmsRelationType> relationTypes) {

        List<CmsRelationType> result = new ArrayList<CmsRelationType>(relationTypes);
        Iterator<CmsRelationType> it = result.iterator();
        while (it.hasNext()) {
            CmsRelationType type = it.next();
            if (!type.isStrong()) {
                it.remove();
            }
        }
        return result;
    }

    /**
     * Returns all user defined relation types in the given list.<p>
     *
     * @param relationTypes the collection of relation types to filter
     *
     * @return a list of {@link CmsRelationType} objects
     */
    public static List<CmsRelationType> filterUserDefined(Collection<CmsRelationType> relationTypes) {

        List<CmsRelationType> result = new ArrayList<CmsRelationType>(relationTypes);
        Iterator<CmsRelationType> it = result.iterator();
        while (it.hasNext()) {
            CmsRelationType type = it.next();
            if (type.isInternal()) {
                it.remove();
            }
        }
        return result;
    }

    /**
     * Returns all weak relation types in the given list.<p>
     *
     * @param relationTypes the collection of relation types to filter
     *
     * @return a list of {@link CmsRelationType} objects
     */
    public static List<CmsRelationType> filterWeak(Collection<CmsRelationType> relationTypes) {

        List<CmsRelationType> result = new ArrayList<CmsRelationType>(relationTypes);
        Iterator<CmsRelationType> it = result.iterator();
        while (it.hasNext()) {
            CmsRelationType type = it.next();
            if (type.isStrong()) {
                it.remove();
            }
        }
        return result;
    }

    /**
     * Returns all relation types.<p>
     *
     * @return a list of {@link CmsRelationType} objects
     */
    public static List<CmsRelationType> getAll() {

        List<CmsRelationType> all = new ArrayList<CmsRelationType>(Arrays.asList(VALUE_ARRAY));
        all.addAll(OpenCms.getResourceManager().getRelationTypes());
        return Collections.unmodifiableList(all);
    }

    /**
     * Returns all relation types for relations defined in the content.<p>
     *
     * @return a list of {@link CmsRelationType} objects
     */
    public static List<CmsRelationType> getAllDefinedInContent() {

        return filterDefinedInContent(getAll());
    }

    /**
     * Returns all internally defined relation types.<p>
     *
     * @return a list of {@link CmsRelationType} objects
     */
    public static List<CmsRelationType> getAllInternal() {

        return Collections.unmodifiableList(Arrays.asList(VALUE_ARRAY));
    }

    /**
     * Returns all relation types for relations that are not defined in the content.<p>
     *
     * @return a list of {@link CmsRelationType} objects
     */
    public static List<CmsRelationType> getAllNotDefinedInContent() {

        return filterNotDefinedInContent(getAll());
    }

    /**
     * Returns all strong relation types.<p>
     *
     * @return a list of {@link CmsRelationType} objects
     */
    public static List<CmsRelationType> getAllStrong() {

        return filterStrong(getAll());
    }

    /**
     * Returns all user defined relation types.<p>
     *
     * @return a list of {@link CmsRelationType} objects
     */
    public static List<CmsRelationType> getAllUserDefined() {

        return OpenCms.getResourceManager().getRelationTypes();
    }

    /**
     * Returns all weak relation types.<p>
     *
     * @return a list of {@link CmsRelationType} objects
     */
    public static List<CmsRelationType> getAllWeak() {

        return filterWeak(getAll());
    }

    /**
     * Parses an <code>int</code> into a relation type.<p>
     *
     * @param id the internal representation number to parse
     *
     * @return the enumeration element
     *
     * @throws CmsIllegalArgumentException if the given value could not be matched against a
     *         <code>{@link CmsRelationType}</code> object.
     */
    public static CmsRelationType valueOf(int id) throws CmsIllegalArgumentException {

        if ((id > 0) && (id <= VALUE_ARRAY.length)) {
            return VALUE_ARRAY[id - 1];
        }
        id -= USER_DEFINED_MODE_LIMIT;
        if ((id >= 0) && (id < getAllUserDefined().size())) {
            return getAllUserDefined().get(id);
        }
        throw new CmsIllegalArgumentException(
            org.opencms.db.Messages.get().container(
                org.opencms.db.Messages.ERR_MODE_ENUM_PARSE_2,
                Integer.valueOf(id),
                CmsRelationType.class.getName()));
    }

    /**
     * Parses an <code>String</code> into a relation type.<p>
     *
     * @param name the relation type name
     *
     * @return the enumeration element
     *
     * @throws CmsIllegalArgumentException if the given value could not be matched against a
     *         <code>{@link CmsRelationType}</code> object
     *
     * @see #valueOfXml(String)
     * @see #valueOfJsp(String)
     */
    public static CmsRelationType valueOf(String name) throws CmsIllegalArgumentException {

        CmsRelationType result = valueOfInternal(name);
        if (result == null) {
            // no type found
            throw new CmsIllegalArgumentException(
                org.opencms.db.Messages.get().container(
                    org.opencms.db.Messages.ERR_MODE_ENUM_PARSE_2,
                    name,
                    CmsRelationType.class.getName()));
        }
        return result;
    }

    /**
     * Parses the given value into a valid enumeration element for a JSP relation type.<p>
     *
     * This should be used to extend Strings like "weak" or "strong" to full relation type descriptors
     * for JSP pages like "JSP_WEAK" or "JSP_STRONG".<p>
     *
     * @param name the name to get the JSP type for
     *
     * @return the JSP enumeration element
     *
     * @see #valueOf(String)
     */
    public static CmsRelationType valueOfJsp(String name) {

        CmsRelationType result = valueOfInternal(name);
        if (result == null) {
            result = valueOf(PREFIX_JSP + name);
        }
        return result;
    }

    /**
     * Parses the given value into a valid enumeration element for a XML relation type.<p>
     *
     * This should be used to extend Strings like "weak" or "strong" to full relation type descriptors
     * for XML documents like "XML_WEAK" or "XML_STRONG".<p>
     *
     * @param name the name to get the XML type for
     *
     * @return the XML enumeration element
     *
     * @see #valueOf(String)
     */
    public static CmsRelationType valueOfXml(String name) {

        CmsRelationType result = valueOfInternal(name);
        if (result == null) {
            result = valueOf(PREFIX_XML + name);
        }
        return result;
    }

    /**
     * Internal parse method.<p>
     *
     * @param name the type to parse
     *
     * @return the enumeration element, or <code>null</code> if no matching element is found
     */
    private static CmsRelationType valueOfInternal(String name) {

        if (name != null) {
            String valueUp = name.toUpperCase();
            for (int i = 0; i < VALUE_ARRAY.length; i++) {
                if (valueUp.equals(VALUE_ARRAY[i].m_name)) {
                    return VALUE_ARRAY[i];
                }
            }
            // deprecated types
            if (valueUp.equals("REFERENCE") || valueUp.equals("XML_REFERENCE")) {
                return XML_WEAK;
            } else if (valueUp.equals("ATTACHMENT") || valueUp.equals("XML_ATTACHMENT")) {
                return XML_STRONG;
            }
            // user defined
            for (int i = 0; i < getAllUserDefined().size(); i++) {
                CmsRelationType type = getAllUserDefined().get(i);
                if (valueUp.equals(type.m_name)) {
                    return type;
                }
            }
        }
        return null;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj instanceof CmsRelationType) {
            return (m_id == ((CmsRelationType)obj).m_id);
        }
        return false;
    }

    /**
     * Gets the 'copy behavior' of the relation type, which is how relations of a resource should be handled when copying that resource.<p>
     *
     * @return the copy behavior of the relation type
     */
    public CopyBehavior getCopyBehavior() {

        return m_copyBehavior;
    }

    /**
     * Returns the internal representation of this type.<p>
     *
     * @return the internal representation of this type
     */
    public int getId() {

        return m_id;
    }

    /**
     * Returns a localized name for the given relation type.<p>
     *
     * @param messages the message bundle to use to resolve the name
     *
     * @return a localized name
     */
    public String getLocalizedName(CmsMessages messages) {

        String nameKey = "GUI_RELATION_TYPE_" + getName() + "_0";
        return messages.key(nameKey);
    }

    /**
     * Returns a localized name for the given relation type.<p>
     *
     * @param locale the locale
     *
     * @return a localized name
     */
    public String getLocalizedName(Locale locale) {

        return getLocalizedName(Messages.get().getBundle(locale));
    }

    /**
     * Returns the type name.<p>
     *
     * @return the type name
     *
     * @see CmsRelationType#valueOf(String)
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the type name for xml output.<p>
     *
     * The short type name of XML or JSP types is only <code>"WEAK"</code> or <code>"STRONG"</code>.
     * For other types the short name is equal to the name.<p>
     *
     * In case you need the full type name, use {@link #getName()}.<p>
     *
     * @return the short type name
     *
     * @see #getName()
     * @see CmsRelationType#valueOfJsp(String)
     * @see CmsRelationType#valueOfXml(String)
     */
    public String getNameForXml() {

        String result;
        switch (getId()) {
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
                result = getName();
        }
        return result;
    }

    /**
     * Returns the string strong or weak.<p>
     *
     * @return the string strong or weak
     *
     * @see #isStrong()
     */
    public String getType() {

        return isStrong() ? VALUE_STRONG : VALUE_WEAK;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_id;
    }

    /**
     * Checks if this relation type is defined in the content of a resource or not.<p>
     *
     * @return <code>true</code> if this relation type is defined in the content of a resource
     */
    public boolean isDefinedInContent() {

        return m_defInContent;
    }

    /**
     * Checks if this is an internal relation type.<p>
     *
     * @return <code>true</code> if this is an internal relation type
     */
    public boolean isInternal() {

        return (getId() < USER_DEFINED_MODE_LIMIT);
    }

    /**
     * Checks if the relation type is strong or weak.<p>
     *
     * @return <code>true</code> if the relation type is strong
     */
    public boolean isStrong() {

        return m_strong;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return m_name;
    }
}
