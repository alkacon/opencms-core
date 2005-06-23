/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/validation/Attic/I_CmsXmlDocumentLinkValidatable.java,v $
 * Date   : $Date: 2005/06/23 10:47:25 $
 * Version: $Revision: 1.4 $
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

package org.opencms.validation;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;

import java.util.List;

/**
 * HTML link validation of a Cms file is enabled if the file's resource type implements the 
 * interface I_CmsHtmlLinkValidatable. Cms files with resource types that do not implement 
 * this interface don't get validated on broken links for example when a project gets published. 
 * Thus, this interface serves to identify "validatable" Cms files in the OpenCms VFS.<p>
 * 
 * HTML links are considered as href attribs in anchor tags and src attribs in image tags.<p>
 * 
 * @author Thomas Weckert  
 * 
 * @version $Revision: 1.4 $ 
 * 
 * @since 6.0.0 
 */
public interface I_CmsXmlDocumentLinkValidatable {

    /**
     * Returns a list with the Cms URIs of all linked resources (either via href attribs or img 
     * tags) in the (body) content of the specified Cms resource.<p>
     * 
     * Implementations of this method must return an empty list, or better 
     * {@link java.util.Collections#EMPTY_LIST}, if no links are found at all.<p>
     * 
     * Second, implementations of this method are responsible to filter out any "external" URLs 
     * pointing to targets outside the OpenCms VFS (http, https, mailto, ftp etc.) from the result 
     * list.<p>
     * 
     * @param cms the current user's Cms object
     * @param resource a CmsResource with links
     * @return a list with the URIs of all linked resources (either via href attribs or img tags)
     */
    List findLinks(CmsObject cms, CmsResource resource);

}
