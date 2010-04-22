/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/super_src/org/opencms/gwt/Attic/CmsRpcException.java,v $
 * Date   : $Date: 2010/04/22 14:32:12 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * RPC Exception.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0
 */
public class CmsRpcException extends Exception implements IsSerializable {

    /** Serialization uid. */
    private static final long serialVersionUID = 7582056307629544840L;

    /**
     * Default constructor.<p>
     */
    public CmsRpcException() {

        // empty
    }

    /**
     * Default constructor.<p>
     * 
     * @param message the error message 
     */
    public CmsRpcException(String message) {

        super(message);
    }
}
