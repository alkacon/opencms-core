/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/input/Attic/I_CmsValidationHandler.java,v $
 * Date   : $Date: 2010/05/06 09:51:37 $
 * Version: $Revision: 1.3 $
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

package org.opencms.gwt.client.ui.input;

/**
 * Interface for notifying an object about a form validation failure or success.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.3 $ 
 * 
 * @since 8.0.0
 * 
 */
public interface I_CmsValidationHandler {

    /**
     * This method is called when a form validation has been completed.<p>
     * 
     * The boolean parameter passed to this method indicates whether there have been validation errors or not.
     * 
     * @param validationSucceeded if true, there were no validation errors
     */
    void onValidationComplete(boolean validationSucceeded);

}
