/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/page/CmsXmlPageFactory.java,v $
 * Date   : $Date: 2005/06/22 10:38:29 $
 * Version: $Revision: 1.11 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.xml.page;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.i18n.CmsEncoder;
import org.opencms.loader.CmsXmlContentLoader;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

import javax.servlet.ServletRequest;

import org.apache.commons.logging.Log;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.xml.sax.EntityResolver;

/**
 * Provides factory methods to unmarshal (read) an XML page object.<p> 
 *
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.11 $
 * @since 5.5.0
 */
public final class CmsXmlPageFactory {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsXmlPageFactory.class);

    /**
     * No instances of this class should be created.<p> 
     */
    private CmsXmlPageFactory() {

        // noop
    }

    /**
     * Creates a valid XML page document,
     * containing one empty element in the given locale.<p>
     * 
     * @param locale the locale to create the XML page for
     * 
     * @return a valid XML page document
     */
    public static Document createDocument(Locale locale) {

        Document doc = DocumentHelper.createDocument();
        Element pages = doc.addElement(CmsXmlPage.NODE_PAGES);
        pages.add(I_CmsXmlSchemaType.XSI_NAMESPACE);
        pages.addAttribute(
            I_CmsXmlSchemaType.XSI_NAMESPACE_ATTRIBUTE_NO_SCHEMA_LOCATION,
            CmsXmlPage.C_XMLPAGE_XSD_SYSTEM_ID);

        Element page = pages.addElement(CmsXmlPage.NODE_PAGE);
        page.addAttribute(CmsXmlPage.ATTRIBUTE_LANGUAGE, locale.toString());

        return doc;
    }

    /**
     * Creates a valid XML page String representation,
     * containing one empty element in the given locale.<p>
     * 
     * @param locale the locale to create the XML page for
     * @param encoding the encoding to use when creating the String from the XML 
     * 
     * @return a valid XML page document as a String
     */
    public static String createDocument(Locale locale, String encoding) {

        try {
            return CmsXmlUtils.marshal(createDocument(locale), encoding);
        } catch (CmsXmlException e) {
            // this should never happen
            LOG.error(Messages.get().key(Messages.ERR_XML_PAGE_FACT_CREATE_DOC_0), e);
            return null;
        }
    }

    /**
     * Factory method to unmarshal (read) a XML page instance from a byte array
     * that contains XML data.<p>
     * 
     * When unmarshalling, the encoding is read directly from the XML header. 
     * The given encoding is used only when marshalling the XML again later.<p>
     * 
     * @param xmlData the XML data in a byte array
     * @param encoding the encoding to use when marshalling the XML page later
     * @param resolver the XML entitiy resolver to use
     * @return a XML page instance unmarshalled from the byte array
     * @throws CmsXmlException if something goes wrong
     */
    public static CmsXmlPage unmarshal(byte[] xmlData, String encoding, EntityResolver resolver) throws CmsXmlException {

        return new CmsXmlPage(CmsXmlUtils.unmarshalHelper(xmlData, resolver), encoding);
    }

    /**
     * Factory method to unmarshal (read) a XML page instance from a OpenCms VFS file
     * that contains XML data.<p>
     * 
     * @param cms the current cms object
     * @param file the file with the XML data to unmarshal
     * @return a XML page instance unmarshalled from the provided file
     * @throws CmsXmlException if something goes wrong
     */
    public static CmsXmlPage unmarshal(CmsObject cms, CmsFile file) throws CmsXmlException {

        return CmsXmlPageFactory.unmarshal(cms, file, true);
    }

    /**
     * Factory method to unmarshal (read) a XML page instance from a OpenCms VFS file
     * that contains XML data, using wither the encoding set
     * in the XML file header, or the encoding set in the VFS file property.<p>
     * 
     * If you are not sure about the implications of the encoding issues, 
     * use {@link #unmarshal(CmsObject, CmsFile)} instead.<p>
     * 
     * @param cms the current cms object
     * @param file the file with the XML data to unmarshal
     * @param keepEncoding if true, the encoding spefified in the XML header is used, 
     *    otherwise the encoding from the VFS file property is used
     * @return a XML page instance unmarshalled from the provided file
     * @throws CmsXmlException if something goes wrong
     */
    public static CmsXmlPage unmarshal(CmsObject cms, CmsFile file, boolean keepEncoding) throws CmsXmlException {

        byte[] content = file.getContents();

        String fileName = cms.getSitePath(file);
        boolean allowRelative = false;
        try {
            allowRelative = Boolean.valueOf(
                cms.readPropertyObject(fileName, CmsXmlPage.C_PROPERTY_ALLOW_RELATIVE, false).getValue()).booleanValue();
        } catch (CmsException e) {
            // allowRelative will be false
        }

        String encoding = null;
        try {
            encoding = cms.readPropertyObject(fileName, CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING, true).getValue();
        } catch (CmsException e) {
            // encoding will be null 
        }
        if (encoding == null) {
            encoding = OpenCms.getSystemInfo().getDefaultEncoding();
        } else {
            encoding = CmsEncoder.lookupEncoding(encoding, null);
            if (encoding == null) {
                throw new CmsXmlException(Messages.get().container(Messages.ERR_XML_PAGE_FACT_INVALID_ENC_1, fileName));
            }
        }

        CmsXmlPage newPage;
        if (content.length > 0) {
            // content is initialized
            if (keepEncoding) {
                // use the encoding from the content
                newPage = unmarshal(content, encoding, new CmsXmlEntityResolver(cms));
            } else {
                // use the encoding from the file property
                // this usually only triggered by a save operation                
                try {
                    String contentStr = new String(content, encoding);
                    newPage = unmarshal(contentStr, encoding, new CmsXmlEntityResolver(cms));
                } catch (UnsupportedEncodingException e) {
                    // this will not happen since the encodig has already been validated
                    throw new CmsXmlException(Messages.get().container(
                        Messages.ERR_XML_PAGE_FACT_INVALID_ENC_1,
                        fileName), e);
                }
            }
        } else {
            // content is empty
            newPage = new CmsXmlPage(cms.getRequestContext().getLocale(), encoding);
        }

        newPage.setFile(file);
        newPage.setAllowRelativeLinks(allowRelative);

        return newPage;
    }

    /**
     * Factory method to unmarshal (read) a XML page instance from
     * a resource, using the request attributes as cache.<p>
     * 
     * @param cms the current OpenCms context object
     * @param resource the resource to unmarshal
     * @param req the current request
     * 
     * @return the unmarshaled xmlpage, or null if the given resource was not of type {@link CmsResourceTypeXmlPage}
     * 
     * @throws CmsException in something goes wrong
     */
    public static CmsXmlPage unmarshal(CmsObject cms, CmsResource resource, ServletRequest req) throws CmsException {

        String rootPath = resource.getRootPath();

        if (resource.getTypeId() != CmsResourceTypeXmlPage.getStaticTypeId()) {
            // sanity check: resource must be of type XML page
            throw new CmsXmlException(Messages.get().container(
                Messages.ERR_XML_PAGE_FACT_NO_XMLPAGE_TYPE_1,
                cms.getSitePath(resource)));
        }

        // try to get the requested page form the current request attributes 
        CmsXmlPage page = (CmsXmlPage)req.getAttribute(rootPath);

        if (page == null) {
            // unmarshal XML structure from the file content
            page = unmarshal(cms, CmsFile.upgrade(resource, cms));
            // store the page that was read as request attribute for future read requests
            req.setAttribute(rootPath, page);
        }

        return page;
    }

    /**
     * Factory method to unmarshal (read) a XML document instance from
     * a filename in the VFS, using the request attributes as cache.<p>
     * 
     * @param cms the current OpenCms context object
     * @param filename the filename of the resource to unmarshal
     * @param req the current request
     * 
     * @return the unmarshaled xml document, or null if the given resource was not of type {@link I_CmsXmlDocument}
     * 
     * @throws CmsException in something goes wrong
     */
    public static I_CmsXmlDocument unmarshal(CmsObject cms, String filename, ServletRequest req) throws CmsException {

        // add site root to filename
        String rootPath = cms.getRequestContext().addSiteRoot(filename);

        // try to get the requested page form the current request attributes
        I_CmsXmlDocument doc = (I_CmsXmlDocument)req.getAttribute(rootPath);

        if (doc != null) {
            return doc;
        }

        // always use "ignore expiration" filter, date validity must be checked before calling this if required
        CmsFile file = cms.readFile(filename, CmsResourceFilter.IGNORE_EXPIRATION);

        if (file.getTypeId() == CmsResourceTypeXmlPage.getStaticTypeId()) {
            // file is of type XML page
            doc = CmsXmlPageFactory.unmarshal(cms, file);
        } else if ((OpenCms.getResourceManager().getLoader(file) instanceof CmsXmlContentLoader)) {
            // file is of type XML content
            doc = CmsXmlContentFactory.unmarshal(cms, file);
        } else {
            // sanity check: file type not an A_CmsXmlDocument
            throw new CmsXmlException(Messages.get().container(Messages.ERR_XML_PAGE_FACT_NO_XML_DOCUMENT_1, file));
        }

        // store the page that was read as request attribute for future read requests
        req.setAttribute(rootPath, doc);

        return doc;
    }

    /**
     * Factory method to unmarshal (read) a XML page instance from a String
     * that contains XML data.<p>
     * 
     * When unmarshalling, the encoding is read directly from the XML header. 
     * The given encoding is used only when marshalling the XML again later.<p>
     * 
     * @param xmlData the XML data in a String
     * @param encoding the encoding to use when marshalling the XML page later
     * @param resolver the XML entitiy resolver to use
     * @return a XML page instance unmarshalled from the String
     * @throws CmsXmlException if something goes wrong
     */
    public static CmsXmlPage unmarshal(String xmlData, String encoding, EntityResolver resolver) throws CmsXmlException {

        return new CmsXmlPage(CmsXmlUtils.unmarshalHelper(xmlData, resolver), encoding);
    }
}