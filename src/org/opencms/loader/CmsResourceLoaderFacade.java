/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/loader/Attic/CmsResourceLoaderFacade.java,v $
 * Date   : $Date: 2003/12/12 12:16:42 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.loader;

import com.opencms.file.CmsFile;

/**
 * Facade object that contains a resource loader and a file of the cms.<p>
 * 
 * This object is used by resource loaders that are not "top-level" enabled.
 * For such resource loaders, the top level loader to trigger is selected by 
 * the template of the file. The template is defined using the template property.
 * Depending on the implementation of the loader to be actually used, 
 * the file to set in the loader facade is either the file requersted by the user,
 * or the template file.<p>
 * 
 * <i>Note:</i> Currently the complete logic which loader and template to select is hardcoded in 
 * the class {@link org.opencms.loader.CmsLoaderManager}. If additional loaders are
 * added on a regular basis, it might be worthwhile to make this logic configurable 
 * so that no core change is required for new loaders.<p>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 * @since 5.3
 */
public class CmsResourceLoaderFacade {

    /** The resource loader */
    private I_CmsResourceLoader m_loader;
    
    /** The file to load in the OpenCms VFS */
    private CmsFile m_file;
        
    /**
     * Creates a new resource loader facade.<p>
     * 
     * @param loader the loader to use
     * @param file the file to use
     */
    public CmsResourceLoaderFacade(I_CmsResourceLoader loader, CmsFile file) {
        m_loader = loader;
        m_file = file;
    }
        
    /**
     * Returns the file.<p>
     * 
     * @return the file
     */
    public CmsFile getFile() {
        return m_file;
    }

    /**
     * Returns the loader.<p>
     * 
     * @return the loader
     */
    public I_CmsResourceLoader getLoader() {
        return m_loader;
    }

}