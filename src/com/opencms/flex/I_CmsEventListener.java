/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/Attic/I_CmsEventListener.java,v $
 * Date   : $Date: 2002/07/01 11:54:48 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002  The OpenCms Group
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
 *
 * First created on 18. April 2002, 14:59
 */


package com.opencms.flex;

/**
 * Description of the class CmsListener here.
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.2 $
 */
public interface I_CmsEventListener {

    public static int EVENT_LOGIN_USER = 1;

    public static int EVENT_PUBLISH_PROJECT = 2;

    public static int EVENT_PUBLISH_RESOURCE = 3;

    public static int EVENT_STATIC_EXPORT = 4;

    /**
     * Acknowledge the occurrence of the specified event.
     *
     * @param event CmsEvent that has occurred
     */
    public void cmsEvent(CmsEvent event);
}

