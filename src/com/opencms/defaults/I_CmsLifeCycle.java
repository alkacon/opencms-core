/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/defaults/Attic/I_CmsLifeCycle.java,v $
* Date   : $Date: 2003/07/14 12:49:42 $
* Version: $Revision: 1.3 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package com.opencms.defaults;

import com.opencms.file.CmsObject;

/**
 * Defines methods to start-up and shut-down module classes that are registerd in the OpenCms registry.
 * 
 * @author Hanjo Riege
 * @version 1.0
 */

public interface I_CmsLifeCycle {

    /**
     * This method is called when OpenCms starts.
     *
     * @param cms a first cms-object.
     */
    void startUp(CmsObject cms);

    /**
     * This method is called when OpenCms ends.
     */
    void shutDown();

}