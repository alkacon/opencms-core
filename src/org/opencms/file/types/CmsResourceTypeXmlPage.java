/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/types/CmsResourceTypeXmlPage.java,v $
 * Date   : $Date: 2004/06/21 09:55:50 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.file.types;

import org.opencms.db.CmsDriverManager;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.loader.CmsXmlPageLoader;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.staticexport.CmsLink;
import org.opencms.staticexport.CmsLinkTable;
import org.opencms.validation.I_CmsHtmlLinkValidatable;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.page.CmsXmlPage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Resource type descriptor for the type "xmlpage".<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 * @since 5.1
 */
public class CmsResourceTypeXmlPage extends A_CmsResourceType implements I_CmsHtmlLinkValidatable {

    /** The type id of this resource type. */
    public static final int C_RESOURCE_TYPE_ID = 10;

    /** The name of this resource type. */
    public static final String C_RESOURCE_TYPE_NAME = "xmlpage";

    /**
     * @see org.opencms.validation.I_CmsHtmlLinkValidatable#findLinks(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    public List findLinks(CmsObject cms, CmsResource resource) {

        List links = new ArrayList();
        CmsFile file = null;
        CmsXmlPage xmlPage = null;
        List locales = null;
        List elementNames = null;
        String elementName = null;
        CmsLinkTable linkTable = null;
        String linkName = null;
        CmsLink link = null;

        try {
            file = cms.readFile(cms.getRequestContext().removeSiteRoot(resource.getRootPath()));
        } catch (CmsException e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error reading file content of " + resource.getRootPath(), e);
            }

            return Collections.EMPTY_LIST;
        }

        try {
            xmlPage = CmsXmlPage.unmarshal(cms, file);
            locales = xmlPage.getLocales();

            // iterate over all languages
            Iterator i = locales.iterator();
            while (i.hasNext()) {
                Locale locale = (Locale)i.next();
                elementNames = xmlPage.getNames(locale);

                // iterate over all body elements per language
                Iterator j = elementNames.iterator();
                while (j.hasNext()) {
                    elementName = (String)j.next();
                    linkTable = xmlPage.getLinkTable(elementName, locale);

                    // iterate over all links inside a body element
                    Iterator k = linkTable.iterator();
                    while (k.hasNext()) {
                        linkName = (String)k.next();
                        link = linkTable.getLink(linkName);

                        // external links are ommitted
                        if (link.isInternal()) {
                            links.add(link.getTarget());
                        }
                    }
                }
            }
        } catch (CmsXmlException e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error processing HTML content of " + resource.getRootPath(), e);
            }

            return Collections.EMPTY_LIST;
        }

        return links;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getCachePropertyDefault()
     */
    public String getCachePropertyDefault() {

        return "element;locale;";
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getLoaderId()
     */
    public int getLoaderId() {

        return CmsXmlPageLoader.C_RESOURCE_LOADER_ID;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getTypeId()
     */
    public int getTypeId() {

        return C_RESOURCE_TYPE_ID;
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#getTypeName()
     */
    public String getTypeName() {

        return C_RESOURCE_TYPE_NAME;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#isDirectEditable()
     */
    public boolean isDirectEditable() {

        return true;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#writeFile(org.opencms.file.CmsObject, CmsDriverManager, CmsFile)
     */
    public CmsFile writeFile(CmsObject cms, CmsDriverManager driverManger, CmsFile resource) throws CmsException {

        // read the xml page, use the encoding set in the property       
        CmsXmlPage xmlPage = CmsXmlPage.unmarshal(cms, resource, false);
        // validate the xml structure before writing the file         
        // an exception will be thrown if the structure is invalid
        xmlPage.validateXmlStructure(new CmsXmlEntityResolver(cms));
        // correct the HTML structure 
        CmsFile correctedFile = xmlPage.correctHtmlStructure(cms);
        // xml is valid if no exception occured
        return super.writeFile(cms, driverManger, correctedFile);
    }
}