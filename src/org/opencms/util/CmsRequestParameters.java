/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/Attic/CmsRequestParameters.java,v $
 * Date   : $Date: 2005/06/08 12:48:57 $
 * Version: $Revision: 1.1 $
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
 
package org.opencms.util;

import java.util.List;
import java.util.Map;

/** 
 * Stores the parameters and multi part file items (if available) from a HttpServletRequest.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 6.0
 */
public class CmsRequestParameters {
    
    /** The parameters. */
    private Map m_parameters;
    
    /** The multi part file items. */
    private List m_multiPartFileItems;
            
    /**
     * Creates a new instance of the request parameter values.<p>
     * 
     * @param parameters the parameter map
     * @param multiPartFileItems the multi-part file items  
     */
    public CmsRequestParameters(Map parameters, List multiPartFileItems) {

        m_parameters = parameters;
        m_multiPartFileItems = multiPartFileItems;
    }        
    
    /**
     * Returns the parameters.<p>
     *
     * @return the parameters
     */
    public Map getParameters() {

        return m_parameters;
    }        
    
    /**
     * Returns the multi part file items.<p>
     *
     * @return the multi part file items
     */
    public List getMultiPartFileItems() {

        return m_multiPartFileItems;
    }        
}