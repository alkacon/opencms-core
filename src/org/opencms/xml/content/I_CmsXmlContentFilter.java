/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/content/Attic/I_CmsXmlContentFilter.java,v $
 * Date   : $Date: 2004/08/03 07:19:04 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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
package org.opencms.xml.content;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;

import java.util.List;

/**
 * A filter that generates list of XML content object from the VFS.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 * @since 5.5.0
 */
public interface I_CmsXmlContentFilter {
    
    /**
     * Returns the link that must be executed when a user clicks on the direct edit
     * "new" button on a list created by the named filter.<p> 
     * 
     * @param cms the current CmsObject 
     * @param filterName the name of the filter to use
     * @param param an optional filter parameter
     * @return the link to execute after a "new" button was clicked
     * @throws CmsException if something goes wrong
     */
    String getCreateLink(CmsObject cms, String filterName, String param) throws CmsException; 
    
    /** 
     * Returns a list of {@link CmsXmlContent} Objects that are 
     * gathered in the VFS using the named filter.<p>
     * 
     * @param cms the current CmsObject 
     * @param filterName the name of the filter to use
     * @param param an optional filter parameter
     * @return a list of CmsXmlContent objects
     * @throws CmsException if something goes wrong
     * 
     */
    List getFilterResults(CmsObject cms, String filterName, String param) throws CmsException; 
}
