/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.loader;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Resource loaders that implement this method can easily provide the contents of a selected targe element as
 * a String.<p>
 *
 * @since 6.2.0
 */
public interface I_CmsResourceStringDumpLoader {

    /**
     * Dumps the processed content of the the requested file (and it's sub-elements) to a String.<p>
     *
     * This is a special form of <code>{@link I_CmsResourceLoader#dump(CmsObject, CmsResource, String, Locale, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}</code>
     * where the result is required in a String, for example for printing it to a writer.<p>
     *
     * @param cms used to access the OpenCms VFS
     * @param resource the requested resource in the VFS
     * @param element the element in the file to display
     * @param locale the locale to display
     * @param req the servlet request
     * @param res the servlet response
     *
     * @return the content of the processed file as a String
     *
     * @throws ServletException might be thrown by the servlet environment
     * @throws IOException might be thrown by the servlet environment
     * @throws CmsException in case of errors acessing OpenCms functions
     *
     * @see I_CmsResourceLoader#dump(CmsObject, CmsResource, String, Locale, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    String dumpAsString(
        CmsObject cms,
        CmsResource resource,
        String element,
        Locale locale,
        ServletRequest req,
        ServletResponse res) throws ServletException, IOException, CmsException;
}