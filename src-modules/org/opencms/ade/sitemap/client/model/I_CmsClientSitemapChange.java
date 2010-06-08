/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/model/Attic/I_CmsClientSitemapChange.java,v $
 * Date   : $Date: 2010/06/08 14:35:17 $
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

package org.opencms.ade.sitemap.client.model;

import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.xml.sitemap.I_CmsSitemapChange;
import org.opencms.xml.sitemap.I_CmsSitemapChange.Type;

/**
 * Stores one change to the sitemap.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 */
public interface I_CmsClientSitemapChange {

    /**
     * Applies the current change to the model.<p>
     * 
     * @param controller the controller
     */
    void applyToModel(CmsSitemapController controller);

    /**
     * Applies the current change to the view.<p>
     * 
     * @param view the view
     */
    void applyToView(CmsSitemapView view);

    /**
     * Returns this change for commit.<p>
     * 
     * @return this change for commit
     */
    I_CmsSitemapChange getChangeForCommit();

    /**
     * Returns this change for undo.<p>
     * 
     * @return this change for undo
     */
    I_CmsClientSitemapChange getChangeForUndo();

    /**
     * Returns the change type.<p>
     * 
     * @return the change type
     */
    Type getType();

    /**
     * Returns the revert of this change for undoing.<p>
     * 
     * @return the revert change
     */
    I_CmsClientSitemapChange revert();
}