/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/tools/CmsIdentifiableObjectContainer.java,v $
 * Date   : $Date: 2011/03/23 14:50:33 $
 * Version: $Revision: 1.8 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.8 $ 
 * 
 * @since 6.0.0 
 */
public class CmsIdentifiableObjectContainer {

    /**
     * Internal class just for taking care of the positions in the container.<p>
     * 
     * @author Michael Moossen  
     * 
     * @version $Revision: 1.8 $
     * 
     * @since 6.0.0
     */
    private static class CmsIdObjectElement {

        /** Identifiable object. */
        private final Object m_object;

        /** Relative position. */
        private final float m_position;

        /**
         * Default Constructor.<p>
         * 
         * @param object the object
         * @param position the relative position
         * 
         */
        public CmsIdObjectElement(Object object, float position) {

            m_object = object;
            m_position = position;
        }

        /**
         * Returns the object.<p>
         *
         * @return the object
         */
        public Object getObject() {

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
    private List m_cache;

    /** List of objects. */
    private final List m_objectList = new ArrayList();

    /** Map of objects only used if uniqueIds flag set. */
    private final Map m_objectsById = new HashMap();

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
    public void addIdentifiableObject(String id, Object idObject) {

        m_cache = null;
        if (m_uniqueIds && (m_objectsById.get(id) != null)) {
            removeObject(id);
        }
        if (m_relativeOrdered) {
            float pos = 1;
            if (!m_objectList.isEmpty()) {
                pos = ((CmsIdObjectElement)m_objectList.get(m_objectList.size() - 1)).getPosition() + 1;
            }
            m_objectList.add(new CmsIdObjectElement(idObject, pos));
        } else {
            m_objectList.add(idObject);
        }
        if (m_uniqueIds) {
            m_objectsById.put(id, idObject);
        } else {
            Object prevObj = m_objectsById.get(id);
            if (prevObj == null) {
                List list = new ArrayList();
                list.add(idObject);
                m_objectsById.put(id, list);
            } else {
                ((List)prevObj).add(idObject);
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
    public void addIdentifiableObject(String id, Object idObject, float position) {

        m_cache = null;
        if (m_uniqueIds && (m_objectsById.get(id) != null)) {
            removeObject(id);
        }
        if (m_relativeOrdered) {
            int pos = 0;
            Iterator itElems = m_objectList.iterator();
            while (itElems.hasNext()) {
                CmsIdObjectElement element = (CmsIdObjectElement)itElems.next();
                if (element.getPosition() > position) {
                    break;
                }
                pos++;
            }
            m_objectList.add(pos, new CmsIdObjectElement(idObject, position));
        } else {
            m_objectList.add((int)position, idObject);
        }
        if (m_uniqueIds) {
            m_objectsById.put(id, idObject);
        } else {
            Object prevObj = m_objectsById.get(id);
            if (prevObj == null) {
                List list = new ArrayList();
                list.add(idObject);
                m_objectsById.put(id, list);
            } else {
                ((List)prevObj).add(idObject);
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
    }

    /**
     * Returns the list of objects.<p>
     *
     * @return the a list of <code>{@link Object}</code>s.
     */
    public List elementList() {

        if (m_cache != null) {
            return m_cache;
        }
        if (m_relativeOrdered) {
            List objectList = new ArrayList();
            Iterator itObjs = m_objectList.iterator();
            while (itObjs.hasNext()) {
                CmsIdObjectElement object = (CmsIdObjectElement)itObjs.next();
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
    public Object getObject(String id) {

        return m_objectsById.get(id);
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
                Iterator itObjs = m_objectList.iterator();
                while (itObjs.hasNext()) {
                    CmsIdObjectElement object = (CmsIdObjectElement)itObjs.next();
                    if (object.getObject() == o) {
                        itObjs.remove();
                        break;
                    }
                }
                m_objectsById.remove(id);
            } else {
                Iterator itRemove = ((List)getObject(id)).iterator();
                while (itRemove.hasNext()) {
                    Object o = itRemove.next();
                    Iterator itObjs = m_objectList.iterator();
                    while (itObjs.hasNext()) {
                        CmsIdObjectElement object = (CmsIdObjectElement)itObjs.next();
                        if (object.getObject() == o) {
                            itObjs.remove();
                            break;
                        }
                    }
                }
                m_objectsById.remove(id);
            }
        } else {
            Object o = getObject(id);
            m_objectList.remove(o);
            m_objectsById.remove(id);
        }
    }
}
