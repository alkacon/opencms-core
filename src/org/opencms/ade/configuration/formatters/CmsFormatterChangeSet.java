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

import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsFunctionFormatterBean;
import org.opencms.xml.containerpage.CmsXmlDynamicFunctionHandler;
import org.opencms.xml.containerpage.I_CmsFormatterBean;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * This class represents the changes which can be made to formatters in a sitemap configuration file.<p>
 */
public class CmsFormatterChangeSet implements Cloneable {

    /** The prefix used for types in the Add/RemoveFormatter fields in the configuration. */
    public static final String PREFIX_TYPE = "type_";

    /** The debug path to identify the configuration where this is coming from. */
    @SuppressWarnings("unused")
    private String m_debugPath;

    /** The set of structure ids of added functions. */
    private Set<CmsUUID> m_functions;

    /** Ids of functions to remove. */
    private Set<CmsUUID> m_functionsToRemove = new HashSet<>();

    /** The path pattern to match formatters accessible from the current site. */
    private Pattern m_pathPattern;

    /** A flag, indicating if all formatters that are not explicitly added should be removed. */
    private boolean m_removeAllNonExplicitlyAdded;

    /** True if functions are removed. */
    private boolean m_removeFunctions;

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
     * @param siteRoot the site root of the current config
     * @param removeAllNonExplicitlyAdded flag, indicating if all formatters that are not explicitly added should be removed
     * @param removeFunctions if true, all functions are removed
     * @param functions the set of functions to enable
     * @param functionsToRemove the set of functions to remove
     */
    public CmsFormatterChangeSet(
        Collection<String> toRemove,
        Collection<String> toAdd,
        String siteRoot,
        boolean removeAllNonExplicitlyAdded,
        boolean removeFunctions,
        Set<CmsUUID> functions,
        Set<CmsUUID> functionsToRemove) {

        this();
        m_removeFunctions = removeFunctions;
        m_functions = functions;
        m_functionsToRemove = functionsToRemove != null ? functionsToRemove : new HashSet<>();

        initialize(toRemove, toAdd, siteRoot, removeAllNonExplicitlyAdded);

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
     * @param formatterIndex the collection of formatters on which this change set should operate
     * @param externalFormatters the formatter collection which should be used to add formatters which are not already present in 'formatters'
     */
    public void applyToFormatters(
        CmsFormatterIndex formatterIndex,
        CmsFormatterConfigurationCacheState externalFormatters) {

        formatterIndex.removeIf(formatter -> {
            if ((formatter instanceof CmsFunctionFormatterBean) && m_removeFunctions) {
                return true;
            }
            if (!(formatter instanceof CmsFunctionFormatterBean) && m_removeAllNonExplicitlyAdded) {
                return true;
            }
            return false;

        });

        Map<CmsUUID, Boolean> updateSetWithFunctions = new HashMap<>(m_updateSet);
        for (CmsUUID id : m_functionsToRemove) {
            updateSetWithFunctions.put(id, Boolean.FALSE);
        }

        if (m_functions != null) {
            for (CmsUUID id : m_functions) {
                updateSetWithFunctions.put(id, Boolean.TRUE);
            }
        }

        for (Map.Entry<CmsUUID, Boolean> updateEntry : updateSetWithFunctions.entrySet()) {
            CmsUUID id = updateEntry.getKey();
            Boolean value = updateEntry.getValue();
            if (value.booleanValue()) {
                I_CmsFormatterBean addedFormatter = externalFormatters.getFormatters().get(id);
                if (addedFormatter != null) {
                    formatterIndex.addFormatter(addedFormatter);
                }
            } else {
                formatterIndex.remove(id);
            }
        }

        if (m_pathPattern != null) {
            formatterIndex.removeIf(formatter -> {
                String location = formatter.getLocation();
                return (location != null) && !m_pathPattern.matcher(location).matches();
            });
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

        if (m_removeAllNonExplicitlyAdded) {
            types.removeIf(type -> !CmsXmlDynamicFunctionHandler.TYPE_FUNCTION.equals(type));
        }
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
     * Creates a clone of this object, but with the 'remove all functions/formatters' flags set to false.
     *
     * @return a clone which disables removall of all functions/formatters
     */
    public CmsFormatterChangeSet cloneWithNoRemovals() {

        try {
            CmsFormatterChangeSet result = (CmsFormatterChangeSet)clone();
            result.m_removeAllNonExplicitlyAdded = false;
            result.m_removeFunctions = false;
            return result;
        } catch (CloneNotSupportedException e) {
            return null;
        }

    }

    /**
     * Sets the debug path.
     *
     * @param debugPath the debug path
     */
    public void setDebugPath(String debugPath) {

        m_debugPath = debugPath;
    }

    /**
     * Initializes this formatter change set with the values from the sitemap configuration.<p>
     *
     * @param toRemove the keys for the formatters to remove
     * @param toAdd the keys for the formatters to add
     * @param siteRoot the site root of the current config
     * @param removeAllNonExplicitlyAdded flag, indicating if all formatters that are not explicitly added should be removed
     */
    private void initialize(
        Collection<String> toRemove,
        Collection<String> toAdd,
        String siteRoot,
        boolean removeAllNonExplicitlyAdded) {

        m_removeAllNonExplicitlyAdded = removeAllNonExplicitlyAdded;

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
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(siteRoot)) {
            if (!siteRoot.endsWith("/")) {
                siteRoot += "/";
            }
            String regex = "^(/system/|" + OpenCms.getSiteManager().getSharedFolder() + "|" + siteRoot + ").*";

            m_pathPattern = Pattern.compile(regex);
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
