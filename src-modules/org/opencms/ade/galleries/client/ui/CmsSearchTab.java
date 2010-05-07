/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/CmsSearchTab.java,v $
 * Date   : $Date: 2010/05/07 13:59:19 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.galleries.client.ui;

import org.opencms.ade.galleries.client.A_CmsTabHandler;
import org.opencms.gwt.client.util.CmsPair;

import java.util.ArrayList;

/**
 * Provides the widget for the full text search tab.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.
 */
public class CmsSearchTab extends A_CmsListTab {

    // TODO:use a different super class!!
    /** Text metrics key. */
    private static final String TM_SEARCH_TAB = "SearchTab";

    /**
     * Constructor.<p>
     */
    public CmsSearchTab() {

        super();
        m_scrollList.truncate(TM_SEARCH_TAB, CmsGalleryDialog.DIALOG_WIDTH);
    }

    @Override
    public A_CmsTabHandler getTabHandler() {

        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    protected ArrayList<CmsPair<String, String>> getSortList() {

        // TODO: Auto-generated method stub
        return new ArrayList<CmsPair<String, String>>();
    }

}