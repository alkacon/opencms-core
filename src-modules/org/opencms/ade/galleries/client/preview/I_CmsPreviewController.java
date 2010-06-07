/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/preview/Attic/I_CmsPreviewController.java,v $
 * Date   : $Date: 2010/06/07 08:07:40 $
 * Version: $Revision: 1.2 $
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

package org.opencms.ade.galleries.client.preview;

import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;

/**
 * Interface for the preview controller.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 8.0.0
 */
public interface I_CmsPreviewController {

    /**
     * Checks if further user input is required and other wise sets the selected resource via the provided integrator functions <code>setLink</code> and <code>setImage</code>.
     * Returning <code>true</code> when all data has been set and the dialog should be closed.
     * 
     * @return <code>true</code> when all data has been set and the dialog should be closed
     */
    boolean closeGalleryDialog();

    /**
     * Selects the resource.<p>
     * 
     * @param dialogMode the gallery mode
     */
    void select(GalleryMode dialogMode);
}