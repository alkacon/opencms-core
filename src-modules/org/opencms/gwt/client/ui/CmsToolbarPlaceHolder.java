/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsToolbarPlaceHolder.java,v $
 * Date   : $Date: 2011/05/03 10:48:53 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides a place holder for the tool-bar.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 */
public class CmsToolbarPlaceHolder extends Composite {

    /**
     * @see com.google.gwt.uibinder.client.UiBinder
     */
    interface I_CmsToolbarPlaceHolderUiBinder extends UiBinder<Widget, CmsToolbarPlaceHolder> {
        // GWT interface, nothing to do here
    }

    /** The ui-binder instance for this class. */
    private static I_CmsToolbarPlaceHolderUiBinder uiBinder = GWT.create(I_CmsToolbarPlaceHolderUiBinder.class);

    /**
     * Constructor.<p>
     */
    public CmsToolbarPlaceHolder() {

        initWidget(uiBinder.createAndBindUi(this));
    }
}
