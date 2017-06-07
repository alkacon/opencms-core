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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.importexport;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.page.CmsXmlPage;

import java.util.Iterator;
import java.util.Locale;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 * Converts legacy pages (OpenCms 5 and earlier) to XML pages (OpenCms 6).<p>
 *
 * @since 6.0.0
 *
 * @deprecated no longer in use
 */
@Deprecated
public final class CmsXmlPageConverter {

    /**
     * Constructor made private to avoid class instanciation.<p>
     */
    private CmsXmlPageConverter() {

        // noop
    }

    /**
     * Converts the contents of a page into an xml page.<p>
     *
     * @param cms the cms object
     * @param content the content used with xml templates
     * @param locale the locale of the body element(s)
     * @param encoding the encoding to the xml page
     * @return the xml page content or null if conversion failed
     * @throws CmsImportExportException if the body content or the XMLTEMPLATE element were not found
     * @throws CmsXmlException if there is an error reading xml contents from the byte array into a document
     */
    @SuppressWarnings("unchecked")
    public static CmsXmlPage convertToXmlPage(CmsObject cms, byte[] content, Locale locale, String encoding)
    throws CmsImportExportException, CmsXmlException {

        CmsXmlPage xmlPage = null;

        Document page = CmsXmlUtils.unmarshalHelper(content, null);

        Element xmltemplate = page.getRootElement();
        if ((xmltemplate == null) || !"XMLTEMPLATE".equals(xmltemplate.getName())) {
            throw new CmsImportExportException(Messages.get().container(Messages.ERR_NOT_FOUND_ELEM_XMLTEMPLATE_0));
        }

        // get all edittemplate nodes
        Iterator<Element> i = xmltemplate.elementIterator("edittemplate");
        boolean useEditTemplates = true;
        if (!i.hasNext()) {
            // no edittemplate nodes found, get the template nodes
            i = xmltemplate.elementIterator("TEMPLATE");
            useEditTemplates = false;
        }

        // now create the XML page
        xmlPage = new CmsXmlPage(locale, encoding);

        while (i.hasNext()) {
            Element currentTemplate = i.next();
            String bodyName = currentTemplate.attributeValue("name");
            if (CmsStringUtil.isEmpty(bodyName)) {
                // no template name found, use the parameter body name
                bodyName = "body";
            }
            String bodyContent = null;

            if (useEditTemplates) {
                // no content manipulation needed for edittemplates
                bodyContent = currentTemplate.getText();
            } else {
                // parse content for TEMPLATEs
                StringBuffer contentBuffer = new StringBuffer();
                for (Iterator<Node> k = currentTemplate.nodeIterator(); k.hasNext();) {
                    Node n = k.next();
                    if (n.getNodeType() == Node.CDATA_SECTION_NODE) {
                        contentBuffer.append(n.getText());
                        continue;
                    } else if (n.getNodeType() == Node.ELEMENT_NODE) {
                        if ("LINK".equals(n.getName())) {
                            contentBuffer.append(OpenCms.getSystemInfo().getOpenCmsContext());
                            contentBuffer.append(n.getText());
                            continue;
                        }
                    }
                }
                bodyContent = contentBuffer.toString();
            }

            if (bodyContent == null) {
                throw new CmsImportExportException(Messages.get().container(Messages.ERR_BODY_CONTENT_NOT_FOUND_0));
            }

            bodyContent = CmsStringUtil.substitute(
                bodyContent,
                CmsStringUtil.MACRO_OPENCMS_CONTEXT,
                OpenCms.getSystemInfo().getOpenCmsContext());

            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(bodyContent)) {
                xmlPage.addValue(bodyName, locale);
                xmlPage.setStringValue(cms, bodyName, locale, bodyContent);
            }
        }

        return xmlPage;

    }
}