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

package org.opencms.pdftools;

import org.opencms.file.CmsObject;
import org.opencms.pdftools.dtds.FailingEntityResolver;
import org.opencms.pdftools.dtds.XhtmlEntityResolver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.util.XRLog;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

/**
 * This class uses the flying-saucer library to convert an XHTML document to a PDF document.<p>
 */
public class CmsPdfConverter {

    static {
        // send logging from flyingsaucer to opencms log
        System.getProperties().setProperty("xr.util-logging.loggingEnabled", "true");
        XRLog.setLoggingEnabled(true);
        XRLog.setLoggerImpl(new CmsXRLogAdapter());
    }

    /** Entity resolver which loads cached DTDs instead of fetching DTDs from the web. */
    private EntityResolver m_entityResolver = new XhtmlEntityResolver(new FailingEntityResolver());

    /**
     * Creates a new instance.<p>
     */
    public CmsPdfConverter() {

        // do nothing
    }

    /**
     * Converts XHTML data to a PDF document.<p>
     *
     * @param cms the current CMS context
     * @param xhtmlData the XHTML as a byte array
     * @param uri the uri to use for error messages in the XML parser
     *
     * @return the PDF data as a byte array
     *
     * @throws Exception if something goes wrong
     */
    public byte[] convertXhtmlToPdf(CmsObject cms, byte[] xhtmlData, String uri) throws Exception {

        Document doc = readDocument(xhtmlData);
        ITextRenderer renderer = new ITextRenderer();
        CmsPdfUserAgent userAgent = new CmsPdfUserAgent(cms);
        userAgent.setSharedContext(renderer.getSharedContext());
        renderer.getSharedContext().setUserAgentCallback(userAgent);
        renderer.setDocument(doc, uri);
        renderer.layout();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        renderer.createPDF(out);
        return out.toByteArray();
    }

    /**
     * Reads an XHTML document from a byte array.<p>
     *
     * @param xhtmlData the XHTML data
     * @return the document which was read from the data
     *
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private Document readDocument(byte[] xhtmlData) throws ParserConfigurationException, SAXException, IOException {

        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setValidating(false);
        docBuilderFactory.setNamespaceAware(true);
        DocumentBuilder docbuilder = docBuilderFactory.newDocumentBuilder();
        // use special entity resolver so we don't fetch the DTDs from w3.org, which would be slow
        docbuilder.setEntityResolver(m_entityResolver);
        Document doc = docbuilder.parse(new ByteArrayInputStream(xhtmlData));
        return doc;
    }
}
