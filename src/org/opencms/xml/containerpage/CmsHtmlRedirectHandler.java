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

package org.opencms.xml.containerpage;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsDefaultXmlContentHandler;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Locale;

import org.apache.commons.logging.Log;

/**
 * Content handler for HTML redirects.<p>
 */
public class CmsHtmlRedirectHandler extends CmsDefaultXmlContentHandler {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsHtmlRedirectHandler.class);

    /**
     * @see org.opencms.xml.content.CmsDefaultXmlContentHandler#prepareForWrite(org.opencms.file.CmsObject, org.opencms.xml.content.CmsXmlContent, org.opencms.file.CmsFile)
     */
    @Override
    public CmsFile prepareForWrite(CmsObject cms, CmsXmlContent content, CmsFile file) throws CmsException {

        CmsFile result = super.prepareForWrite(cms, content, file);
        try {
            String linkStr = getStringValue(cms, content, "Link");
            String typeStr = getStringValue(cms, content, "Type");

            if ("sublevel".equals(typeStr)) {
                Locale locale = OpenCms.getLocaleManager().getDefaultLocale(cms, file);
                String title = org.opencms.xml.containerpage.Messages.get().getBundle(locale).key(
                    org.opencms.xml.containerpage.Messages.GUI_REDIRECT_SUBLEVEL_TITLE_0);
                CmsProperty titleProp = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, title, null);
                cms.writePropertyObjects(file, Arrays.asList(titleProp));
            } else if (!CmsStringUtil.isEmptyOrWhitespaceOnly(linkStr)) {
                boolean hasScheme = false;
                try {
                    URI uri = new URI(linkStr);
                    hasScheme = uri.getScheme() != null;
                } catch (URISyntaxException e) {
                    LOG.debug(e.getLocalizedMessage(), e);
                }
                if (!hasScheme) {
                    linkStr = cms.getRequestContext().removeSiteRoot(linkStr);
                }
                Locale locale = OpenCms.getLocaleManager().getDefaultLocale(cms, file);
                String title = org.opencms.xml.containerpage.Messages.get().getBundle(locale).key(
                    org.opencms.xml.containerpage.Messages.GUI_REDIRECT_TITLE_1,
                    linkStr);
                CmsProperty titleProp = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, title, null);
                cms.writePropertyObjects(file, Arrays.asList(titleProp));
            }
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return result;
    }

    private String getStringValue(CmsObject cms, CmsXmlContent content, String node) {

        I_CmsXmlContentValue val = content.getValue(node, Locale.ENGLISH);
        if (val == null) {
            return null;
        }
        return val.getStringValue(cms);
    }

}
