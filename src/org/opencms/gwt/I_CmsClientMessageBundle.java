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

package org.opencms.gwt;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

/**
 * Convenient client message bundle interface.<p>
 *
 * @since 8.0.0
 *
 * @see org.opencms.i18n.I_CmsMessageBundle
 */
public interface I_CmsClientMessageBundle {

    /**
     * Returns the JSON code for this resource bundle and the default locale.<p>
     *
     * @param request the current request to get the default locale from
     *
     * @return the JSON code
     */
    String export(HttpServletRequest request);

    /**
     * Returns the JSON code for this resource bundle and given locale.<p>
     *
     * @param locale the locale to export
     *
     * @return the JSON code
     */
    String export(Locale locale);

    /**
     * Returns the JSON code for this resource bundle and given locale.<p>
     *
     * @param locale the locale to export
     * @param wrapScript <code>true</code> to wrap the data in a script tag
     *
     * @return the JSON code
     */
    String export(Locale locale, boolean wrapScript);

    /**
     * Returns the JSON code for this resource bundle and given locale.<p>
     *
     * @param localeName the name of the locale to export
     *
     * @return the JSON code
     */
    String export(String localeName);

    /**
     * Returns the bundle name for this OpenCms package.<p>
     *
     * @return the bundle name for this OpenCms package
     */
    String getBundleName();

    /**
     * Returns the class of the client implementation.<p>
     *
     * @return the class of the client implementation
     *
     * @throws Exception if something goes wrong
     */
    Class<?> getClientImpl() throws Exception;
}
