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

package org.opencms.jsp.decorator;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * CmsDecorationBundle, contains a map of merged CmsDEcorationMaps.<p>
 *
 * The map inside the decoration bundle uses the decoration as keys and CmsDecorationObjects as values.<p>
 *
 * A decoration bundle contains either all decoarions for one locale (similar to a resource bundle), or
 * is locale independend. If its a locale independend bundle, the included locale is set to null.
 *
 *
 * @since 6.1.3
 */
public class CmsDecorationBundle {

    /** The bundle map. */
    private Map<String, CmsDecorationObject> m_bundle;

    /** The locale of this bundle. */
    private Locale m_locale;

    /**
     * Constructor, creates a new, empty CmsDecorationBundle.<p>
     */
    public CmsDecorationBundle() {

        m_bundle = new HashMap<String, CmsDecorationObject>();
        m_locale = null;
    }

    /**
     * Constructor, creates a new CmsDecorationBundle for a given locale.<p>
     *
     * @param locale the locale of this bundle or null
     */
    public CmsDecorationBundle(Locale locale) {

        m_bundle = new HashMap<String, CmsDecorationObject>();
        m_locale = locale;

    }

    /**
     * Gets an object from the decoration bundle.<p>
     * @param key the key of the object ot get
     * @return the value matching the key or null.
     */
    public Object get(Object key) {

        return m_bundle.get(adjustKey(key.toString()));
    }

    /**
     * Gets the map of all decoarion bundle entries.<p>
     * @return map of all decoarion bundle entries
     */
    public Map<String, CmsDecorationObject> getAll() {

        return m_bundle;
    }

    /**
     * Gets the locale of this decoration bundle.<p>
     * @return locale of the decoration bundle
     */
    public Locale getLocale() {

        return m_locale;
    }

    /**
     * Gets the keyset of the decoration bundle map.<p>
     * @return keyset of the decoration bundle map
     */
    public Set<String> keySet() {

        return m_bundle.keySet();
    }

    /**
     * Stores an obiect in the decoration bundle.<p>
     * @param key the key of the object to store
     * @param value the value of the object to store
     */
    public void put(String key, CmsDecorationObject value) {

        m_bundle.put(key, value);
    }

    /**
     * Puts a complete map of objects into bundle.<p>
     * @param map the map to put into the bundle
     */
    public void putAll(Map<String, CmsDecorationObject> map) {

        m_bundle.putAll(map);
    }

    /**
     * Sets the locale of the decoration bundle.<p>
     * @param locale the locale to set
     */
    public void setLocale(Locale locale) {

        m_locale = locale;
    }

    /**
     * Adjusts the key for the decoration.<p>
     * The following adjustments are made:
     * <ul>
     * <li>&nbsp; is replaced with space</li>
     * <li>multiple spaces are replaced with a single space</li>
     * </ul>
     * @param key the key to adjust
     * @return the adjusted key
     */
    private String adjustKey(String key) {

        // replace the &nbsp; with spaces
        key = key.replaceAll("&nbsp;", " ");
        // now eleiminate all double spaces
        int keyLen;
        do {
            keyLen = key.length();
            key = key.replaceAll("  ", " ");
        } while (key.length() != keyLen);
        return key;
    }
}
