/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/cron/Attic/I_CmsCronJob.java,v $
 * Date   : $Date: 2004/02/13 13:41:46 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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
package org.opencms.cron;

import org.opencms.file.CmsObject;

/**
 * This interface identifies an Object that can be started as a cronjob.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com) 
 * @version $Revision: 1.2 $ $Date: 2004/02/13 13:41:46 $
 * @since 5.1.12
 */
public interface I_CmsCronJob {

    /**
     * The CmsCronScheduler launchs this method to do the job on the specified
     * class.<p>
     * 
     * @param cms the CmsObject to get access to the cms
     * @param parameter a String parameter that was defined in the cron entry
     * @return a String or null - if a string is returned, this will be written to the logfile
     * @throws Exception if something goes wrong
     */
    String launch(CmsObject cms, String parameter) throws Exception;

}