/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/I_CmsContainerPageNameGenerator.java,v $
 * Date   : $Date: 2010/10/28 07:38:56 $
 * Version: $Revision: 1.1 $
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

package org.opencms.xml.sitemap;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;

/**
 * The interface for classes which generate container page file names for new sitemap entries.<p>
 * 
 * This interface consists of two methods: One for initializing the generator, and one for retrieving
 * the next generated name from the generator. The init() method will be called once, then the getNextTargetPath() 
 * method will be repeatedly called until it generates a path at which a new resource can be created; thus implementations
 * of this class should return different values for subsequent calls of getNextTargetPath().<p>
 *  
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public interface I_CmsContainerPageNameGenerator {

    /**
     * Generates a new target path for the container page.<p>
     * 
     * @return the next generated target path  
     * 
     * @throws CmsException if something goes wrong 
     */
    String getNextName() throws CmsException;

    /**
     * Initializes the container page name generator.<p>
     * 
     * @param cms the current CMS context 
     * @param pattern the file name pattern from the configuration file 
     * @param title the title of the sitemap entry 
     * @param sitePath the sitemap path of the sitemap entry 
     * 
     * @throws CmsException if something goes wrong 
     */
    void init(CmsObject cms, String pattern, String title, String sitePath) throws CmsException;
}
