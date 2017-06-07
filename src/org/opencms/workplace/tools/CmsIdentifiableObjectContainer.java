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

package org.opencms.workplace.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of a named object container. <p>
 *
 * It can handle relative or absolute orderings and unique names.<p>
 *
 * @param <T> the type of objects
 *
 * @since 6.0.0
 */
public class CmsIdentifiableObjectContainer<T> {

    /**
     * Internal class just for taking care of the positions in the container.<p>
     *
     * @param <T> the object type
     *
     * @since 6.0.0
     */
    private static class CmsIdObjectElement<T> {

        /** Identifiable object. */
        private final T m_object;

        /** Relative position. */
        private final float m_position;

        /**
         * Default Constructor.<p>
         *
         * @param object the object
         * @param position the relative position
         *
         */
        public CmsIdObjectElement(T object, float position) {

            m_object = object;
            m_position = position;
        }

        /**
         * Returns the object.<p>
         *
         * @return the object
         */
        public T getObject() {

            return m_object;
        }

        /**
         * Returns the position.<p>
         *
         * @return the position
         */
        public float getPosition() {

            return m_position;
        }

    }

    /** Cache for element list. */
    private List<T> m_cache;

    /** List of objects. */
    private final List<T> m_objectList = new ArrayList<T>();

    /** Map of objects only used if uniqueIds flag set. */
    private final Map<String, T> m_objectsById = new HashMap<String, T>();

    /** Map of object lists by id. */
    private final Map<String, List<T>> m_objectsListsById = new HashMap<String, List<T>>();

    /** List of ordered objects. */
    private final List<CmsIdObjectElement<T>> m_orderedObjectList = new ArrayList<CmsIdObjectElement<T>>();

    /** Flag for managing absolute and relative ordering. */
    private final boolean m_relativeOrdered;

    /** Flag for managing uniqueness check. */
    private final boolean m_uniqueIds;

    /**
     * Default Constructor.<p>
     *
     * @param uniqueIds if the list show check for unique ids
     * @param relativeOrdered if the list show use relative ordering, instead of absolute ordering
     */
    public CmsIdentifiableObjectContainer(boolean uniqueIds, boolean relativeOrdered) {

        m_uniqueIds = uniqueIds;
        m_relativeOrdered = relativeOrdered;
    }

    /**
     * Appends the specified object to the end of this container. <p>
     *
     * @param id the object identifier
     * @param idObject the object add to the container
     *
     * @see java.util.List#add(Object)
     */
    public void addIdentifiableObject(String id, T idObject) {

        m_cache = null;
        if (m_uniqueIds && (m_objectsById.get(id) != null)) {
            removeObject(id);
        }
        if (m_relativeOrdered) {
            float pos = 1;
            if (!m_orderedObjectList.isEmpty()) {
                pos = m_orderedObjectList.get(m_orderedObjectList.size() - 1).getPosition() + 1;
            }
            m_orderedObjectList.add(new CmsIdObjectElement<T>(idObject, pos));
        } else {
            m_objectList.add(idObject);
        }
        if (m_uniqueIds) {
            m_objectsById.put(id, idObject);
        } else {
            List<T> prevObj = m_objectsListsById.get(id);
            if (prevObj == null) {
                List<T> list = new ArrayList<T>();
                list.add(idObject);
                m_objectsListsById.put(id, list);
            } else {
                prevObj.add(idObject);
            }
        }

    }

    /**
     * Inserts the specified object at the specified position in this container.<p>
     *
     * Shifts the object currently at that position (if any) and any subsequent
     * objects to the right (adds one to their indices).<p>
     *
     * @param id the object identifier
     * @param idObject the object add to the container
     * @param position the insertion point
     *
     * @see java.util.List#add(int, Object)
     */
    public void addIdentifiableObject(String id, T idObject, float position) {

        m_cache = null;
        if (m_uniqueIds && (m_objectsById.get(id) != null)) {
            removeObject(id);
        }
        if (m_relativeOrdered) {
            int pos = 0;
            Iterator<CmsIdObjectElement<T>> itElems = m_orderedObjectList.iterator();
            while (itElems.hasNext()) {
                CmsIdObjectElement<T> element = itElems.next();
                if (element.getPosition() > position) {
                    break;
                }
                pos++;
            }
            m_orderedObjectList.add(pos, new CmsIdObjectElement<T>(idObject, position));
        } else {
            m_objectList.add((int)position, idObject);
        }
        if (m_uniqueIds) {
            m_objectsById.put(id, idObject);
        } else {
            List<T> prevObj = m_objectsListsById.get(id);
            if (prevObj == null) {
                List<T> list = new ArrayList<T>();
                list.add(idObject);
                m_objectsListsById.put(id, list);
            } else {
                prevObj.add(idObject);
            }
        }

    }

    /**
     * Resets the container.<p>
     */
    public void clear() {

        m_cache = null;
        m_objectList.clear();
        m_objectsById.clear();
        m_orderedObjectList.clear();
        m_objectsListsById.clear();
    }

    /**
     * Returns the list of objects.<p>
     *
     * @return the a list of <code>{@link Object}</code>s.
     */
    public List<T> elementList() {

        if (m_cache != null) {
            return m_cache;
        }
        if (m_relativeOrdered) {
            List<T> objectList = new ArrayList<T>();
            Iterator<CmsIdObjectElement<T>> itObjs = m_orderedObjectList.iterator();
            while (itObjs.hasNext()) {
                CmsIdObjectElement<T> object = itObjs.next();
                objectList.add(object.getObject());
            }
            m_cache = Collections.unmodifiableList(objectList);
        } else {
            m_cache = Collections.unmodifiableList(m_objectList);
        }
        return m_cache;
    }

    /**
     * Returns the object with the given id.<p>
     *
     * If <code>uniqueIds</code> is set to <code>false</code> an <code>{@link Object}</code>
     * containing a <code>{@link List}</code> with all the objects with the given id is returned.<p>
     *
     * If the container no contains any object with the given id, <code>null</code> is returned.<p>
     *
     * @param id the id of the object
     *
     * @return the object if found, or <code>null</code>
     *
     * @see java.util.Map#get(Object)
     */
    public T getObject(String id) {

        if (!m_uniqueIds) {
            throw new UnsupportedOperationException("Not supported for not unique ids");
        }
        return m_objectsById.get(id);

    }

    /**
     * Returns the list of objects with the given id.<p>
     *
     * @param id the object id
     *
     * @return the list of objects if found, or <code>null</code>
     */
    public List<T> getObjectList(String id) {

        if (m_uniqueIds) {
            throw new UnsupportedOperationException("Not supported for unique ids");
        }
        return m_objectsListsById.get(id);
    }

    /**
     * Removes an object with the given id.<p>
     *
     * if <code>m_uniqueIds</code> is set, it will remove at most one object.
     * otherwise it will remove all elements with the given id.<p>
     *
     * @param id the id of the object to remove
     */
    public synchronized void removeObject(String id) {

        m_cache = null;
        if (m_relativeOrdered) {
            if (m_uniqueIds) {
                Object o = getObject(id);
                Iterator<CmsIdObjectElement<T>> itObjs = m_orderedObjectList.iterator();
                while (itObjs.hasNext()) {
                    CmsIdObjectElement<T> object = itObjs.next();
                    if (object.getObject() == o) {
                        itObjs.remove();
                        break;
                    }
                }
                m_objectsById.remove(id);
            } else {
                Iterator<T> itRemove = m_objectsListsById.get(id).iterator();
                while (itRemove.hasNext()) {
                    T o = itRemove.next();
                    Iterator<CmsIdObjectElement<T>> itObjs = m_orderedObjectList.iterator();
                    while (itObjs.hasNext()) {
                        CmsIdObjectElement<T> object = itObjs.next();
                        if (object.getObject() == o) {
                            itObjs.remove();
                            break;
                        }
                    }
                }
                m_orderedObjectList.remove(id);
            }
        } else {
            Object o = getObject(id);
            m_objectList.remove(o);
            if (m_uniqueIds) {
                m_objectsById.remove(id);
            } else {
                m_objectsListsById.remove(id);
            }
        }
    }
}
