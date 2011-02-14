/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/upload/Attic/CmsUploadException.java,v $
 * Date   : $Date: 2011/02/14 13:05:55 $
 * Version: $Revision: 1.2 $
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

package org.opencms.gwt.upload;

/**
 * An exception that should be thrown if there is any problem during the upload process.<p>
 * 
 * @author  Ruediger Kurz 
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 8.0.0 
 */
public class CmsUploadException extends RuntimeException {

    /** The serial version UID. */
    private static final long serialVersionUID = -4125185020719761746L;

    /**
     * Public constructor with a message String as argument.<p>
     * 
     * @param msg the message for this exception
     */
    public CmsUploadException(String msg) {

        super(msg);
    }
}
