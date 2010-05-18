/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsSitemapEntryNotFoundException.java,v $
 * Date   : $Date: 2010/05/18 12:58:17 $
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

package org.opencms.xml.sitemap;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;

/**
 * A sitemap entry could not be found.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsSitemapEntryNotFoundException extends CmsException {

    /** Serialization unique id. */
    private static final long serialVersionUID = 2422080348696602713L;

    /**
     * Creates a new localized Exception.<p>
     * 
     * @param entryPath the site path of the not found entry
     */
    public CmsSitemapEntryNotFoundException(String entryPath) {

        this(entryPath, null);
    }

    /**
     * Creates a new localized Exception.<p>
     * 
     * @param entryPath the site path of the not found entry
     * @param sitemapPath the Exception root cause
     */
    public CmsSitemapEntryNotFoundException(String entryPath, String sitemapPath) {

        this(entryPath, sitemapPath, null);
    }

    /**
     * Creates a new localized Exception that also contains a root cause.<p>
     * 
     * @param entryPath the site path of the not found entry
     * @param sitemapPath the Exception root cause
     * @param cause the Exception root cause
     */
    public CmsSitemapEntryNotFoundException(String entryPath, String sitemapPath, Throwable cause) {

        this(sitemapPath != null ? Messages.get().container(
            Messages.ERR_SITEMAP_ELEMENT_NOT_FOUND_2,
            entryPath,
            sitemapPath) : Messages.get().container(Messages.ERR_SITEMAP_ELEMENT_NOT_FOUND_1, entryPath), cause);
    }

    /**
     * Creates a new localized Exception that also contains a root cause.<p>
     * 
     * @param container the localized message container to use
     * @param cause the Exception root cause
     */
    public CmsSitemapEntryNotFoundException(CmsMessageContainer container, Throwable cause) {

        super(container);
        if (cause != null) {
            initCause(cause);
        }
    }

    /**
     * @see org.opencms.main.CmsException#createException(org.opencms.i18n.CmsMessageContainer, java.lang.Throwable)
     */
    @Override
    public CmsException createException(CmsMessageContainer container, Throwable cause) {

        return new CmsSitemapEntryNotFoundException(container, cause);
    }
}
