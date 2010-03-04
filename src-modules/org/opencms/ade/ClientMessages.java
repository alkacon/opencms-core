/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/Attic/ClientMessages.java,v $
 * Date   : $Date: 2010/03/04 14:07:34 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ade;

import org.opencms.gwt.A_CmsClientMessageBundle;
import org.opencms.gwt.I_CmsClientMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p> 
 * 
 * Intended only for test cases.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0
 */
public final class ClientMessages extends A_CmsClientMessageBundle {

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.ade.clientmessages";

    /** Name of the corresponding client class. */
    private static final String CLIENT_INSTANCE = "org.opencms.ade.client.Messages";

    /** Internal instance. */
    private static ClientMessages INSTANCE;

    /**
     * Hides the public constructor for this utility class.<p>
     */
    private ClientMessages() {

        // hide the constructor
    }

    /**
     * Returns the client message instance.<p>
     * 
     * @return the client message instance
     */
    public static I_CmsClientMessageBundle get() {

        if (INSTANCE == null) {
            INSTANCE = new ClientMessages();
        }
        return INSTANCE;
    }

    /**
     * @see org.opencms.gwt.I_CmsClientMessageBundle#getBundleName()
     */
    public String getBundleName() {

        return BUNDLE_NAME;
    }

    /**
     * @see org.opencms.gwt.I_CmsClientMessageBundle#getClientImpl()
     */
    public Class<?> getClientImpl() throws Exception {

        return Class.forName(CLIENT_INSTANCE);
    }
}
