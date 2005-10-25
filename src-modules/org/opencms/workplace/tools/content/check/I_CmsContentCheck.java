/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/content/check/I_CmsContentCheck.java,v $
 * Date   : $Date: 2005/10/25 15:14:32 $
 * Version: $Revision: 1.1.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.tools.content.check;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;

import java.util.List;

/**
 * This interface defines an OpenCms content check. A content check will  
 * test the content of the properties of all resources inside of OpenCms if they 
 * follow the rules which are defined inside the test implemnting this interface.<p> 
 *
 * @author  Michael Emmerich
 * 
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 6.1.2 
 */

public interface I_CmsContentCheck {

    /** Parameter name for widgets. */
    String PARAMETER = "active";

    /**
     * Main method of the content check. It holds the implementation of the content check.<p>
     * 
     * @param cms the CmsObject
     * @param testResource a CmsContentTestResource containing the results of previous tests
     * @return the updated testResouce containing the results of the content check
     * @throws CmsException if an error occurs 
     */
    CmsContentCheckResource executeContentCheck(CmsObject cms, CmsContentCheckResource testResource)
    throws CmsException;

    /**
     * Defines the name of the parameter which is used by the CmsContentCheckDialog to enable
     * or disable the content check.<p>
     * 
     * @return the name of the dialog parameter.
     */
    String getDialogParameterName();

    /**
     * Gets a list of all required message bundles by this content check.<p>
     * 
     * @return list of message bundle names
     */
    List getMessageBundles();

    /**
     * Gets the name of this content check.<p>
     * 
     * @return name of the content check
     */
    String getName();

    /**
     * Initializer for the content check.<p>
     * 
     * @param cms the current CmsObject
     * @throws CmsException if an error occurs 
     */
    void init(CmsObject cms) throws CmsException;

    /**
     * Signals if this content check is active or not.<p> 
     * 
     * 
     * @return true if this content check is active, false otherwise.
     */
    boolean isActive();

    /**
     * Sets the active flag for the content check.<p>
     * 
     * @param value the value for the active flag
     */
    void setActive(boolean value);

}
