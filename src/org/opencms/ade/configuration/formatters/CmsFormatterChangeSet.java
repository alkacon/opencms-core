/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ade.configuration.formatters;

import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.I_CmsFormatterBean;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class represents the changes which can be made to formatters in a sitemap configuration file.<p>
 */
public class CmsFormatterChangeSet {

    /** The prefix used for types in the Add/RemoveFormatter fields in the configuration. */
    public static final String PREFIX_TYPE = "type_";

    /** A map which indicates whether schema formatters for a type (which is the key) should be added (value=true) or removed (value=False). */
    private Map<String, Boolean> m_typeUpdateSet = new HashMap<String, Boolean>();

    /** A map which indicates whether a formatter (whose id is the key) should be added (value=true) or removed (value= false). */
    private Map<CmsUUID, Boolean> m_updateSet = new HashMap<CmsUUID, Boolean>();

    /**
     * Creates an empty formatter change set.<p>
     */
    public CmsFormatterChangeSet() {

        // do nothing
    }

    /** 
     * Creates a new formatter change set.<p>
     * 
     * @param toRemove the formatter keys to remove 
     * @param toAdd the formatter keys to add 
     */
    public CmsFormatterChangeSet(Collection<String> toRemove, Collection<String> toAdd) {

        this();
        initialize(toRemove, toAdd);
    }

    /** 
     * Produces the key for a given resource type.<p>
     * 
     * @param typeName the resource type name 
     * @return the key to use 
     */
    public static String keyForType(String typeName) {

        return "type_" + typeName;
    }

    /**
     * Applies this change set to a list of external (non schema-based)  formatters.<p>
     * 
     * @param formatters the map of formatters to which the changes should be applied 
     * @param externalFormatters the formatter collection which should be used to add formatters which are not already present in 'formatters'
     */
    public void applyToFormatters(
        Map<CmsUUID, I_CmsFormatterBean> formatters,
        CmsFormatterConfigurationCacheState externalFormatters) {

        for (Map.Entry<CmsUUID, Boolean> updateEntry : m_updateSet.entrySet()) {
            CmsUUID key = updateEntry.getKey();
            Boolean value = updateEntry.getValue();
            if (value.booleanValue()) {
                I_CmsFormatterBean addedFormatter = externalFormatters.getFormatters().get(key);
                if (addedFormatter != null) {
                    formatters.put(key, addedFormatter);
                }
            } else {
                formatters.remove(key);
            }
        }
    }

    /**
     * Applies the changes (addition or removal of schema formatters) to a set of resource type names, 
     * adding resource types for which schema formatters should be added and removing those for which 
     * schema formatters should be removed.<p>
     *  
     * @param types the set of types to apply the changes to 
     */
    public void applyToTypes(Set<String> types) {

        for (Map.Entry<String, Boolean> typeUpdateEntry : m_typeUpdateSet.entrySet()) {
            String typeName = typeUpdateEntry.getKey();
            Boolean add = typeUpdateEntry.getValue();
            if (add.booleanValue()) {
                types.add(typeName);
            } else {
                types.remove(typeName);
            }
        }
    }

    /** 
     * Initializes this formatter change set with the values from the sitemap configuration.<p>
     * 
     * @param toRemove the keys for the formatters to remove 
     * @param toAdd the keys for the formatters to add 
     */
    private void initialize(Collection<String> toRemove, Collection<String> toAdd) {

        for (String removeKey : toRemove) {
            if (CmsUUID.isValidUUID(removeKey)) {
                m_updateSet.put(new CmsUUID(removeKey), Boolean.FALSE);
            } else if (removeKey.startsWith(PREFIX_TYPE)) {
                m_typeUpdateSet.put(removePrefix(removeKey), Boolean.FALSE);
            }
        }
        for (String addKey : toAdd) {
            if (CmsUUID.isValidUUID(addKey)) {
                m_updateSet.put(new CmsUUID(addKey), Boolean.TRUE);
            } else if (addKey.startsWith(PREFIX_TYPE)) {
                m_typeUpdateSet.put(removePrefix(addKey), Boolean.TRUE);
            }
        }
    }

    /** 
     * Removes a prefix from the given key.<p>
     * 
     * @param key the key
     *  
     * @return the key with the prefix removed 
     */
    private String removePrefix(String key) {

        if (key.startsWith(PREFIX_TYPE)) {
            return key.substring(PREFIX_TYPE.length());
        }
        return key;
    }

}
