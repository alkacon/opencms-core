/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workflow/Attic/CmsWorkflowException.java,v $
 * Date   : $Date: 2006/08/19 13:40:50 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workflow;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;

/**
 * Base exception thrown by workflow methods.<p>
 * 
 * @author Carsten Weinholz
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 7.0.0
 */
public class CmsWorkflowException extends CmsException {

    /** The serialVersionUID. */
    static final long serialVersionUID = 2511647913859073304L;

    /**
     * Creates a new localized exception.<p>
     * 
     * @param container the localized message container to use
     */
    public CmsWorkflowException(CmsMessageContainer container) {

        super(container);
    }
}