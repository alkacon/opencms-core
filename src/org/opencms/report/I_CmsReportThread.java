/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/report/I_CmsReportThread.java,v $
 * Date   : $Date: 2005/06/23 11:11:28 $
 * Version: $Revision: 1.5 $
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

package org.opencms.report;

import org.opencms.util.CmsUUID;

/**
 * Identifies a class that can be used as a report thread .<p>
 * 
 * @author Michael Emmerich
 *  
 * @version $Revision: 1.5 $ 
 * 
 * @since 6.0.0 
 */
public interface I_CmsReportThread {

    /**
     * Returns the OpenCms UUID of this report thread.<p>
     * 
     * @return the OpenCms UUID of this report thread
     */
    CmsUUID getUUID();

    /**
     * Starts the report thread.<p>
     */
    void start();

}
