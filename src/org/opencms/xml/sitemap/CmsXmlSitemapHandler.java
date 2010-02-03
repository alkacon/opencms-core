/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsXmlSitemapHandler.java,v $
 * Date   : $Date: 2010/02/03 13:48:15 $
 * Version: $Revision: 1.2 $
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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsDefaultXmlContentHandler;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.LinkedList;
import java.util.Locale;

/**
 * Handles some special situations while saving a sitemap.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 7.6
 */
public class CmsXmlSitemapHandler extends CmsDefaultXmlContentHandler {

    /**
     * @see org.opencms.xml.content.CmsDefaultXmlContentHandler#prepareForWrite(org.opencms.file.CmsObject, org.opencms.xml.content.CmsXmlContent, org.opencms.file.CmsFile)
     */
    @Override
    public CmsFile prepareForWrite(CmsObject cms, CmsXmlContent content, CmsFile file) throws CmsException {

        // be sure every entry has a valid ID
        for (Locale locale : content.getLocales()) {
            String path = CmsXmlSitemap.XmlNode.SiteEntry.name();
            LinkedList<I_CmsXmlContentValue> entries = new LinkedList<I_CmsXmlContentValue>(content.getValues(
                path,
                locale));
            while (!entries.isEmpty()) {
                I_CmsXmlContentValue entry = entries.removeFirst();
                String idPath = CmsXmlUtils.concatXpath(entry.getPath(), CmsXmlSitemap.XmlNode.Id.name());
                I_CmsXmlContentValue id = content.getValue(idPath, locale);
                if (id == null) {
                    id = content.addValue(cms, idPath, locale, 0);
                }
                if (!CmsUUID.isValidUUID(id.getStringValue(cms))) {
                    id.setStringValue(cms, new CmsUUID().toString());
                }
                String subentriesPath = CmsXmlUtils.concatXpath(entry.getPath(), CmsXmlSitemap.XmlNode.SiteEntry.name());
                entries.addAll(content.getValues(subentriesPath, locale));
            }
        }
        file.setContents(content.marshal());
        // do the rest
        return super.prepareForWrite(cms, content, file);
    }
}
