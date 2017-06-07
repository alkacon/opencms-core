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

import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Exports the register client messages into a single JavaScript resource.<p>
 */
public class CmsMessagesService extends CmsGwtService {

    /** The serial version id. */
    private static final long serialVersionUID = 3072608993796119377L;

    /** The client messages to export. */
    private static final I_CmsClientMessageBundle[] CLIENT_MESSGAE_BUNDLES = new I_CmsClientMessageBundle[] {
        ClientMessages.get(),
        org.opencms.ade.containerpage.ClientMessages.get(),
        org.opencms.ade.contenteditor.ClientMessages.get(),
        org.opencms.ade.galleries.ClientMessages.get(),
        org.opencms.ade.postupload.ClientMessages.get(),
        org.opencms.ade.publish.ClientMessages.get(),
        org.opencms.ade.sitemap.ClientMessages.get(),
        org.opencms.ade.upload.ClientMessages.get(),
        org.opencms.gwt.seo.ClientMessages.get()};

    /**
     * @see org.opencms.gwt.CmsGwtService#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    @Override
    public void service(ServletRequest request, ServletResponse response) throws IOException {

        try {
            // Set response's character encoding to the default(*) to avoid ambiguous
            // interpretations of the Servlet spec from different servlet containers.
            // This complies with the Servlet spec.
            // See for example the "Java Servlet Specification Version 3.0":
            // "Servlets should set the locale and the character encoding of a response.
            // [...]
            // If the servlet does not specify a character encoding before the getWriter
            // method of the ServletResponse interface is called or the response is committed,
            // the default ISO-8859-1 is used."
            // (*): the OpenCms configured encoding (defaulting to UTF-8) is favoured over
            // ISO-8859-1 to allow for a wider charset support.
            String characterEncoding = OpenCms.getSystemInfo().getDefaultEncoding();
            response.setCharacterEncoding(characterEncoding);
            response.setContentType("text/javascript");
            Locale locale;
            String localeString = request.getParameter(CmsLocaleManager.PARAMETER_LOCALE);
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(localeString)) {
                locale = CmsLocaleManager.getLocale(localeString);
            } else {
                locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(getCmsObject());
            }
            for (int i = 0; i < CLIENT_MESSGAE_BUNDLES.length; i++) {
                response.getWriter().append(CLIENT_MESSGAE_BUNDLES[i].export(locale, false)).append("\n");
            }
            response.getWriter().flush();
        } finally {
            clearThreadStorage();
        }
    }
}
