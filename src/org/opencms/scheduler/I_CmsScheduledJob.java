/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/scheduler/I_CmsScheduledJob.java,v $
 * Date   : $Date: 2005/06/22 10:38:11 $
 * Version: $Revision: 1.6 $
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
 
package org.opencms.scheduler;

import org.opencms.file.CmsObject;

import java.util.Map;

/**
 * Identifies a class that can be scheduled with the OpenCms scheduler.<p>
 * 
 * @author Alexander Kandzior 
 *  
 * @version $Revision: 1.6 $
 * @since 5.3
 */
public interface I_CmsScheduledJob {

    /**
     * This method will be called when this scheduled job is executed.<p>
     * 
     * Depending on the configuration of this job, a new instance of 
     * the configured class will be instanciated every time the job is launched, 
     * or a new instance will be generated only the first time the 
     * job is launched, and re-used afterwards.<p>
     * 
     * The result String will be written to the OpenCms logfile in the 
     * <code>org.opencms.scheduler.CmsScheduleManager</code> channel,
     * on <code>INFO</code> log level.<p>
     * 
     * @param cms will be initialized with the configured users cms context
     * @param parameters the configured parameters
     * 
     * @return a String that will be written to the OpenCms logfile
     *  
     * @throws Exception if something goes wrong
     * 
     * @see CmsScheduledJobInfo
     * @see CmsScheduledJobInfo#setReuseInstance(boolean)
     */
    String launch(CmsObject cms, Map parameters) throws Exception;    
}
