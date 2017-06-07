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

package org.opencms.db.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.pool.BasePoolableObjectFactory;

/**
 * Implementation of the jpa pool entity manager factory.<p>
 *
 * @since 8.0.0
 */
public class CmsPoolEntityManagerFactory extends BasePoolableObjectFactory {

    /** EntityManagerFactory which creates EntityManager instances. */
    protected EntityManagerFactory m_emFactory;

    /**
     * Public constructor.<p>
     *
     * @param emFactory the entity manager factory
     */
    public CmsPoolEntityManagerFactory(EntityManagerFactory emFactory) {

        m_emFactory = emFactory;
    }

    /**
     * @see org.apache.commons.pool.BasePoolableObjectFactory#destroyObject(java.lang.Object)
     */
    @Override
    public void destroyObject(Object obj) {

        EntityManager em = (EntityManager)obj;
        em.close();
    }

    /**
     * @see org.apache.commons.pool.BasePoolableObjectFactory#makeObject()
     */
    @Override
    public Object makeObject() {

        return m_emFactory.createEntityManager();
    }

    /**
     * @see org.apache.commons.pool.BasePoolableObjectFactory#passivateObject(java.lang.Object)
     */
    @Override
    public void passivateObject(Object obj) {

        EntityManager em = (EntityManager)obj;
        em.clear();
    }
}
