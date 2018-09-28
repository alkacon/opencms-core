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

package org.opencms.module;

/**
 * Status after updating a module.<p>
 */
public class CmsReplaceModuleInfo {

    /** The module. */
    private CmsModule m_module;

    /** Indicates whether module updater or old delete/import process was used. */
    private boolean m_usedUpdater;

    /**
     * Creates a new instance.<p>
     *
     * @param module the module
     * @param usedUpdater true if the module updater was used
     */
    public CmsReplaceModuleInfo(CmsModule module, boolean usedUpdater) {

        super();
        m_module = module;
        m_usedUpdater = usedUpdater;
    }

    /**
     * Gets the module.<p>
     *
     * @return the module
     */
    public CmsModule getModule() {

        return m_module;
    }

    /**
     * Returns true if the module updater was used.<p>
     *
     * @return true if the module updater was used
     */
    public boolean usedUpdater() {

        return m_usedUpdater;
    }

}
