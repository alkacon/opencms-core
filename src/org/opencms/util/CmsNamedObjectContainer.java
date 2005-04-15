/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/Attic/CmsNamedObjectContainer.java,v $
 * Date   : $Date: 2005/04/15 13:02:43 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.4 $
 * @since 5.7.3
 */
public class CmsNamedObjectContainer implements I_CmsNamedObjectContainer {

    /**
     * Internal class just for taking care of the positions in the container.<p>
     * 
     * @author Michael Moossen (m.moossen@alkacon.com) 
     * @version $Revision: 1.4 $
     * @since 5.7.3
     */
    private class CmsNamedObjectElement {

        /** Named object. */
        private final I_CmsNamedObject m_object;

        /** Relative position. */
        private final float m_position;

        /**
         * Default Constructor.<p>
         * 
         * @param object the object
         * @param position the relative position
         * 
         */
        public CmsNamedObjectElement(I_CmsNamedObject object, float position) {

            m_object = object;
            m_position = position;
        }

        /**
         * Returns the object.<p>
         *
         * @return the object
         */
        public I_CmsNamedObject getObject() {

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

    /** Map of objects onlz used if uniqueNames flag set. */
    private final Map m_objectsByName = new HashMap();

    /** Flag for managing absolute and relative ordering. */
    private final boolean m_relativeOrdered;

    /** Flag for managing uniqueness check. */
    private final boolean m_uniqueNames;

    /**
     * Default Constructor.<p>
     * 
     * @param uniqueNames if the list show check for unique names
     * @param relativeOrdered if the list show use relative ordering, instead of absolute ordering
     */
    public CmsNamedObjectContainer(boolean uniqueNames, boolean relativeOrdered) {

        m_uniqueNames = uniqueNames;
        m_relativeOrdered = relativeOrdered;
    }

    /**
     * @see org.opencms.util.I_CmsNamedObjectContainer#addNamedObject(I_CmsNamedObject)
     */
    public void addNamedObject(I_CmsNamedObject namedObject) {

        if (m_uniqueNames && m_objectsByName.get(namedObject.getName()) != null) {
            throw new IllegalStateException("This container already contains an object called " + namedObject.getName());
        }
        if (m_relativeOrdered) {
            float pos = 1;
            if (!m_objectList.isEmpty()) {
                pos = ((CmsNamedObjectElement)m_objectList.get(m_objectList.size() - 1)).getPosition() + 1;
            }
            m_objectList.add(new CmsNamedObjectElement(namedObject, pos));
        } else {
            m_objectList.add(namedObject);
        }
        if (m_uniqueNames) {
            m_objectsByName.put(namedObject.getName(), namedObject);
        } else {
            I_CmsNamedObject prevObj = (I_CmsNamedObject)m_objectsByName.get(namedObject.getName());
            if (prevObj == null) {
                List list = new ArrayList();
                list.add(namedObject);
                m_objectsByName.put(namedObject.getName(), new CmsNamedObject(namedObject.getName(), list));
            } else {
                ((List)((CmsNamedObject)prevObj).getObject()).add(namedObject);
            }
        }

    }

    /**
     * @see org.opencms.util.I_CmsNamedObjectContainer#addNamedObject(I_CmsNamedObject, float)
     */
    public void addNamedObject(I_CmsNamedObject namedObject, float position) {

        if (m_uniqueNames && m_objectsByName.get(namedObject.getName()) != null) {
            throw new IllegalStateException("This container already contains an object called " + namedObject.getName());
        }
        if (m_relativeOrdered) {
            int pos = 0;
            Iterator itElems = m_objectList.iterator();
            while (itElems.hasNext()) {
                CmsNamedObjectElement element = (CmsNamedObjectElement)itElems.next();
                if (element.getPosition() > position) {
                    break;
                }
                pos++;
            }
            m_objectList.add(new CmsNamedObjectElement(namedObject, position));
        } else {
            m_objectList.add((int)position, namedObject);
        }
        if (m_uniqueNames) {
            m_objectsByName.put(namedObject.getName(), namedObject);
        } else {
            I_CmsNamedObject prevObj = (I_CmsNamedObject)m_objectsByName.get(namedObject.getName());
            if (prevObj == null) {
                List list = new ArrayList();
                list.add(namedObject);
                m_objectsByName.put(namedObject.getName(), new CmsNamedObject(namedObject.getName(), list));
            } else {
                ((List)((CmsNamedObject)prevObj).getObject()).add(namedObject);
            }
        }

    }

    /**
     * @see org.opencms.util.I_CmsNamedObjectContainer#clear()
     */
    public void clear() {

        m_objectList.clear();
        m_objectsByName.clear();
    }

    /**
     * @see org.opencms.util.I_CmsNamedObjectContainer#elementList()
     */
    public List elementList() {

        if (m_relativeOrdered) {
            List objectList = new ArrayList();
            Iterator itObjs = m_objectList.iterator();
            while (itObjs.hasNext()) {
                CmsNamedObjectElement object = (CmsNamedObjectElement)itObjs.next();
                objectList.add(object.getObject());
            }
            return Collections.unmodifiableList(objectList);
        } else {
            return Collections.unmodifiableList(m_objectList);
        }

    }

    /**
     * @see org.opencms.util.I_CmsNamedObjectContainer#elementList(java.lang.Class)
     */
    public List elementList(Class type) {

        List list = new ArrayList();
        Iterator itElems = elementList().iterator();
        while (itElems.hasNext()) {
            Object element = itElems.next();
            if (type.isInstance(element)) {
                list.add(element);
            }
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * Returns the object with the given name.<p>
     * 
     * if <code>uniqueNames</code> is set to <code>false</code> a <code>{@link CmsNamedObject}</code> 
     * containing a <code>{@link List}</code> is returned.<p>
     * 
     * @see org.opencms.util.I_CmsNamedObjectContainer#getObject(String)
     */
    public I_CmsNamedObject getObject(String name) {

        return (I_CmsNamedObject)m_objectsByName.get(name);
    }
}