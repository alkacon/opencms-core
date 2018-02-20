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

package org.opencms.ade.containerpage.shared;

import org.opencms.gwt.shared.CmsListInfoBean;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Holds both a bean containing edit handler dialog options and a resource info bean for a selected resource.<p>
 */
public class CmsDialogOptionsAndInfo implements IsSerializable {

    /** The list info bean for the selected resource. */
    private CmsListInfoBean m_info;

    /** The dialog option bean. */
    private CmsDialogOptions m_options;

    /**
     * Creates a new instance.<p>
     *
     * @param options the dialog option bean
     * @param info the list info bean for the selected resource
     */
    public CmsDialogOptionsAndInfo(CmsDialogOptions options, CmsListInfoBean info) {

        super();
        m_options = options;
        m_info = info;
    }

    /**
     * Default constructor for serialization.<p>
     */
    protected CmsDialogOptionsAndInfo() {
        // hidden default constructor for serialization
    }

    /**
     * Gets the list info bean for the selected resource.<p>
     *
     * @return the list info bean
     */
    public CmsListInfoBean getInfo() {

        return m_info;
    }

    /**
     * Gets the dialog option bean.<p>
     *
     * @return the dialog option bean
     */
    public CmsDialogOptions getOptions() {

        return m_options;
    }

}
