/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/content/check/I_CmsContentCheck.java,v $
 * Date   : $Date: 2005/10/19 08:33:28 $
 * Version: $Revision: 1.1.2.1 $
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
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.1.0 
 */

public interface I_CmsContentCheck {

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
     * The name of the dialog parameter must be defined within the implementation of this interface, and 
     * there must be corresponding getter and setter methods for it. This is required to use the 
     * widget dialogs to enable this content check.<p>
     * 
     * Exmaple: if the dialog parameter is called "parametercheck", there must be a "isParametercheck" and a 
     * "setParametercheck" implemented in the implementation of this interface.
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
     * Signals if this content check is active or not.<p> 
     * 
     * This is based on the information that is received and set by the getter and setter
     * method of the parameter defined in @see getParameterName().
     * 
     * @return true if this content check is active, false otherwise.
     */
    boolean isActive();

}
