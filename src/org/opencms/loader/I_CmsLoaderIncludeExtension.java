/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/loader/I_CmsLoaderIncludeExtension.java,v $
 * Date   : $Date: 2005/02/17 12:43:47 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.main.CmsException;

import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Allows extensions to the default include mechanism, 
 * these might be required for the handling of special resource types.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.2 $
 * @since 5.3
 * @see org.opencms.jsp.CmsJspTagInclude#includeTagAction(javax.servlet.jsp.PageContext, String, String, boolean, Map, ServletRequest, ServletResponse)
 */
public interface I_CmsLoaderIncludeExtension {

    /**
     * The extension method for the include tag action.<p>
     * 
     * @param target the target for the include, might be <code>null</code>
     * @param element the element to select form the target might be <code>null</code>
     * @param editable the flag to indicate if the target is editable
     * @param paramMap a map of parameters for the include, can be modified, might be <code>null</code>
     * @param req the current request
     * @param res the current response
     * @throws CmsException in case something goes wrong
     * @return the modified target URI
     */
    String includeExtension(String target, String element, boolean editable, Map paramMap, ServletRequest req, ServletResponse res) throws CmsException;
}