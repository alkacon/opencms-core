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

package org.opencms.db.jpa.persistence;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * This data access object represents a counter entry inside the table "cms_counters".<p>
 * 
 * @since 8.0.0
 */
@Entity
@Table(name = "CMS_COUNTERS")
public class CmsDAOCounters {

    /** The counter.*/
    @Basic
    @Column(name = "COUNTER")
    private int m_counter;

    /** The name. */
    @Id
    @Column(name = "NAME")
    private String m_name;

    /**
     * The default constructor.<p>
     */
    public CmsDAOCounters() {

        // noop
    }

    /**
     * A public constructor for generating a new contents object with an unique id.<p>
     * 
     * @param name the name
     */
    public CmsDAOCounters(String name) {

        m_name = name;
    }

    /**
     * Returns the count.<p>
     * 
     * @return the count
     */
    public int getCounter() {

        return m_counter;
    }

    /**
     * Returns the name.<p>
     * 
     * @return the name
     */
    public String getName() {

        return m_name;
    }

    /**
     * Sets the count.<p>
     * 
     * @param counter the count to set
     */
    public void setCounter(int counter) {

        this.m_counter = counter;
    }

    /**
     * Sets the name.<p>
     * 
     * @param name the name to set
     */
    public void setName(String name) {

        this.m_name = name;
    }
}