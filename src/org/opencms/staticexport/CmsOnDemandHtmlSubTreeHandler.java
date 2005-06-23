/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/CmsOnDemandHtmlSubTreeHandler.java,v $
 * Date   : $Date: 2005/06/23 11:11:28 $
 * Version: $Revision: 1.9 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.staticexport;

import org.opencms.util.CmsFileUtil;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

/**
 * Concrete OnDemandExportHandler.<p>
 * 
 * The published files and folders are purged, as also all the html files in each subtree.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.9 $ 
 * 
 * @since 6.0.0 
 * 
 * @see I_CmsStaticExportHandler
 */
public class CmsOnDemandHtmlSubTreeHandler extends A_CmsOnDemandStaticExportHandler {

    /**
     * @see org.opencms.staticexport.A_CmsOnDemandStaticExportHandler#getRelatedFilesToPurge(java.lang.String)
     */
    protected List getRelatedFilesToPurge(String exportFileName) {

        FileFilter htmlFilter = new FileFilter() {

            /**
             * Accepts only html files
             */
            public boolean accept(File file) {

                return file.isFile() && (file.getName().endsWith(".html") || file.getName().endsWith(".htm"));
            }

        };

        return CmsFileUtil.getFiles(exportFileName, htmlFilter, true);
    }

}