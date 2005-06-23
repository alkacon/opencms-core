/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/Attic/CmsIdentifiableObjectContainer.java,v $
 * Date   : $Date: 2005/06/23 11:11:24 $
 * Version: $Revision: 1.6 $
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

package org.opencms.util;

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
 * @version $Revision: 1.6 $ 
 * 
 * @since 6.0.0 
 */
public class CmsIdentifiableObjectContainer implements I_CmsIdentifiableObjectContainer {

    /**
     * Internal class just for taking care of the positions in the container.<p>
     * 
     * @author Michael Moossen  
     * @version $Revision: 1.6 $
     * @since 5.7.3
     */
    private class CmsIdObjectElement {

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

    /** List of objects. */
    private final List m_objectList = new ArrayList();

    /** Map of objects onlz used if uniqueIds flag set. */
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
     * @see org.opencms.util.I_CmsIdentifiableObjectContainer#addIdentifiableObject(java.lang.String, java.lang.Object)
     */
    public void addIdentifiableObject(String id, Object idObject) {

        if (m_uniqueIds && m_objectsById.get(id) != null) {
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
     * @see org.opencms.util.I_CmsIdentifiableObjectContainer#addIdentifiableObject(java.lang.String, java.lang.Object, float)
     */
    public void addIdentifiableObject(String id, Object idObject, float position) {

        if (m_uniqueIds && m_objectsById.get(id) != null) {
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
     * @see org.opencms.util.I_CmsIdentifiableObjectContainer#clear()
     */
    public void clear() {

        m_objectList.clear();
        m_objectsById.clear();
    }

    /**
     * @see org.opencms.util.I_CmsIdentifiableObjectContainer#elementList()
     */
    public List elementList() {

        if (m_relativeOrdered) {
            List objectList = new ArrayList();
            Iterator itObjs = m_objectList.iterator();
            while (itObjs.hasNext()) {
                CmsIdObjectElement object = (CmsIdObjectElement)itObjs.next();
                objectList.add(object.getObject());
            }
            return Collections.unmodifiableList(objectList);
        } else {
            return Collections.unmodifiableList(m_objectList);
        }

    }

    /**
     * Returns the object with the given id.<p>
     * 
     * If <code>uniqueIds</code> is set to <code>false</code> an <code>{@link Object}</code> 
     * containing a <code>{@link List}</code> with all the objects with the given id is returned.<p>
     * 
     * If the container no contains any object with the given id, <code>null</code> is returned.<p>
     * 
     * @see org.opencms.util.I_CmsIdentifiableObjectContainer#getObject(String)
     */
    public Object getObject(String id) {

        return m_objectsById.get(id);
    }

    /**
     * Removes an object with the given id.<p>
     * 
     * Only works if the <code>{@link #m_uniqueIds}</code> is set.<p>
     * 
     * @param id the id of the object to remove
     */
    private void removeObject(String id) {

        if (m_relativeOrdered) {
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
            Object o = getObject(id);
            m_objectList.remove(o);
            m_objectsById.remove(id);
        }
    }
}