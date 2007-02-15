/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/repository/Attic/CmsRepositoryItemNotFoundException.java,v $
 * Date   : $Date: 2007/02/15 15:54:20 $
 * Version: $Revision: 1.1.4.2 $
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

package org.opencms.repository;

/**
 * Exception thrown if an item could not be found.<p>
 * 
 * @author Peter Bonrad
 * 
 * @version $Revision: 1.1.4.2 $
 * 
 * @since 6.5.6
 */
public class CmsRepositoryItemNotFoundException extends CmsRepositoryException {

    /** The unique serial id for this class. */
    private static final long serialVersionUID = -6704243302339411102L;

    /**
     * Constructs a new instance of this class with <code>null</code> as its
     * detail message.<p>
     */
    public CmsRepositoryItemNotFoundException() {

        super();
    }

    /**
     * Constructs a new instance of this class with the specified detail
     * message.<p>
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method
     */
    public CmsRepositoryItemNotFoundException(String message) {

        super(message);
    }

    /**
     * Constructs a new instance of this class with the specified detail
     * message and root cause.<p>
     *
     * @param message   the detail message. The detail message is saved for
     *                  later retrieval by the {@link #getMessage()} method
     * @param rootCause root failure cause
     */
    public CmsRepositoryItemNotFoundException(String message, Throwable rootCause) {

        super(message, rootCause);
    }

    /**
     * Constructs a new instance of this class with the specified root cause.<p>
     *
     * @param rootCause root failure cause
     */
    public CmsRepositoryItemNotFoundException(Throwable rootCause) {

        super(rootCause);
    }
}
