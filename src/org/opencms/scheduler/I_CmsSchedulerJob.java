/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/scheduler/Attic/I_CmsSchedulerJob.java,v $
 * Date   : $Date: 2004/07/05 15:35:12 $
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
 
package org.opencms.scheduler;

import org.opencms.file.CmsObject;

import org.apache.commons.collections.ExtendedProperties;

/**
 * Identifies a class that can be scheduled with the OpenCms scheduler.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 *  
 * @version $Revision: 1.1 $
 * @since 5.3
 */
public interface I_CmsSchedulerJob {

    /**
     * This method will be called when the scheduled job is executed.<p>
     * 
     * Depending on the configuration of the job, a new instance of 
     * this class will be instanciated every time the job is run, 
     * or a new instance will be generated only the first time the 
     * job is run, and re-used afterwards.<p>
     * 
     * @param cms will be initialized with the configured users cms context
     * @param parameters the configured parameters
     * 
     * @return a String that will be written to the OpenCms logfile
     *  
     * @throws Exception if something goes wrong
     * 
     * @see CmsSchedulerEntry#setReuseInstance(boolean)
     */
    String launch(CmsObject cms, ExtendedProperties parameters) throws Exception;    
}
