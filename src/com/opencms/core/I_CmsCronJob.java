/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/I_CmsCronJob.java,v $
* Date   : $Date: 2001/11/15 16:41:21 $
* Version: $Revision: 1.1 $
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

package com.opencms.core;

import com.opencms.file.*;

/**
 * This interface marks a Class that can be started as a cronjob.
 */
public interface I_CmsCronJob {

    /**
     * The CmsCronScheduler launchs this method to do the job on the specified
     * class.
     * @param cmsobject the CmsObject to get access to the cms.
     * @returns a string or null - if a string is returned, this will be
     * written to the logfile.
     */
    public String launch(CmsObject cms);
}