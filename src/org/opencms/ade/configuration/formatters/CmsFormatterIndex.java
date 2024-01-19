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

import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.I_CmsFormatterBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.HashMultimap;

/**
 * Helper class for keeping track of which keys map to which formatters, and which formatters are active,
 * when evaluating the sitemap configuration.
 *
 * <p>Formatters can now have multiple keys, which makes overriding them in more specific sitemap/master configurations more complex.
 * Let X and Y be active sitemap/master configurations for the currently requested page, with Y being more specific than X. If X adds a formatter F1
 * with keys A, B, C and Y adds a different formatter F2 with an overlapping set of keys C, D, E, then as a result, formatter F1
 * should be disabled and formatter F2 should be used for the keys A, B, C, D, E (even though it does not have A and B
 * as configured keys).
 *
 * <p>You use an instance of this class by adding/removing formatters in the order these operations are defined by the sitemap configuration
 * and finally calling the getFormattersWithAdditionalKeys() method at the end, which augments the formatters it returns by adding the appropriate keys.
 */
public class CmsFormatterIndex {

    /** Table which maps ids to formatter keys. */
    private HashMultimap<CmsUUID, String> m_keysById = HashMultimap.create();

    /** Table which maps formatter keys to ids. */
    private HashMultimap<String, CmsUUID> m_idsByKey = HashMultimap.create();

    /** Map of formatters by id. */
    private Map<CmsUUID, I_CmsFormatterBean> m_formattersById = new HashMap<>();

    /**
     * Adds the given formatter.
     *
     * <p>If there are any direct or indirect overlaps with the keys of already added formatters, these
     * formatters will be removed and their keys mapped to the new formatter.
     *
     * @param formatter the formatter to add
     */
    public void addFormatter(I_CmsFormatterBean formatter) {

        String id = formatter.getId();
        if (CmsUUID.isValidUUID(id)) {
            CmsUUID uuid = new CmsUUID(id);

            Set<String> relatedKeys = new HashSet<>();
            Set<CmsUUID> relatedIds = new HashSet<>();
            collectRelatedKeysAndIds(formatter.getAllKeys(), relatedKeys, relatedIds);

            for (CmsUUID relatedId : relatedIds) {
                m_keysById.removeAll(relatedId);
                m_formattersById.remove(relatedId);
            }
            for (String relatedKey : relatedKeys) {
                m_idsByKey.removeAll(relatedKey);
            }

            m_formattersById.put(uuid, formatter);
            for (String relatedKey : relatedKeys) {
                m_idsByKey.put(relatedKey, uuid);
            }
            m_keysById.putAll(uuid, relatedKeys);
        }
    }

    /**
     * Gets the final map of active formatters, with their formatter keys replaced by the total set of keys under which they should be available.
     *
     * @return the map of formatters by id
     */
    public Map<CmsUUID, I_CmsFormatterBean> getFormattersWithAdditionalKeys() {

        Map<CmsUUID, I_CmsFormatterBean> result = new HashMap<>();
        for (Map.Entry<CmsUUID, I_CmsFormatterBean> entry : m_formattersById.entrySet()) {
            CmsUUID id = entry.getKey();
            Set<String> keys = m_keysById.get(id);
            I_CmsFormatterBean formatter = entry.getValue();
            Optional<I_CmsFormatterBean> formatterWithKeys = formatter.withKeys(keys);
            if (formatterWithKeys.isPresent()) {
                result.put(id, formatterWithKeys.orElse(null));
            }
        }
        return result;
    }

    /**
     * Removes the formatter with the given id.
     *
     * @param id the formatter id
     */
    public void remove(CmsUUID id) {

        Set<String> keys = m_keysById.removeAll(id);
        for (String key : keys) {
            m_idsByKey.remove(key, id);
        }
        m_formattersById.remove(id);
    }

    /**
     * Removes all formatters matching the given predicate
     *
     * @param condition the condition to use for checking which formatters should be removed
     */
    public void removeIf(Predicate<I_CmsFormatterBean> condition) {

        Set<CmsUUID> toRemove = new HashSet<>();
        for (Map.Entry<CmsUUID, I_CmsFormatterBean> entry : m_formattersById.entrySet()) {
            if (condition.test(entry.getValue())) {
                toRemove.add(entry.getKey());
            }
        }
        toRemove.forEach(id -> remove(id));
    }

    /**
     * Collects all related keys and IDs which need to be removed or updated when adding a new formatter with a given set of keys.
     *
     * @param initialKeys the keys of the new formatter
     * @param visitedKeys the set in which the related keys should be stored
     * @param visitedIds the set in which the related IDs should be stored
     */
    private void collectRelatedKeysAndIds(
        Collection<String> initialKeys,
        Set<String> visitedKeys,
        Set<CmsUUID> visitedIds) {

        visitedKeys.clear();
        visitedIds.clear();
        List<String> todo = new ArrayList<>(initialKeys);
        while (todo.size() > 0) {
            String key = todo.remove(todo.size() - 1);
            if (visitedKeys.contains(key)) {
                continue;
            }
            visitedKeys.add(key);
            for (CmsUUID id : m_idsByKey.get(key)) {
                visitedIds.add(id);
                for (String relatedKey : m_keysById.get(id)) {
                    todo.add(relatedKey);
                }
            }
        }
    }

}
