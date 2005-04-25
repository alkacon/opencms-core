/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/flex/CmsFlexCacheException.java,v $
 * Date   : $Date: 2005/04/25 09:09:41 $
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
 
package org.opencms.flex;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsThrowable;

/**
 * Signals an error of the Flex cache.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.1 $
 */
public class CmsFlexCacheException extends CmsException implements I_CmsThrowable {

    /**
     * @see CmsException#CmsException(CmsMessageContainer)
     */
    public CmsFlexCacheException(CmsMessageContainer container) {

        super(container);
    }

    /**
     * @see CmsException#CmsException(CmsMessageContainer, Throwable)
     */
    public CmsFlexCacheException(CmsMessageContainer container, Throwable cause) {

        super(container, cause);
    }
    
}
