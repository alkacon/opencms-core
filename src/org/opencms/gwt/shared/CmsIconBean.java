/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.gwt.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A bean carrying icon CSS class information.<p>
 */
public class CmsIconBean implements I_CmsHasIconClasses, IsSerializable {

    /** The big icon rules. */
    private String m_bigIconClasses;

    /** The small icon rules. */
    private String m_smallIconClasses;

    /**
     * @see org.opencms.gwt.shared.I_CmsHasIconClasses#getBigIconClasses()
     */
    public String getBigIconClasses() {

        return m_bigIconClasses;
    }

    /**
     * @see org.opencms.gwt.shared.I_CmsHasIconClasses#getSmallIconClasses()
     */
    public String getSmallIconClasses() {

        return m_smallIconClasses;
    }

    /**
     * Sets the big icon CSS classes.<p>
     *
     * @param bigIconClasses the big icon CSS classes to set
     * @return this object
     */
    public CmsIconBean setBigIconClasses(String bigIconClasses) {

        m_bigIconClasses = bigIconClasses;
        return this;
    }

    /**
     * Sets the small icon CSS classes.<p>
     *
     * @param smallIconClasses the small icon CSS classes to set
     */
    public void setSmallIconClasses(String smallIconClasses) {

        m_smallIconClasses = smallIconClasses;
    }
}
