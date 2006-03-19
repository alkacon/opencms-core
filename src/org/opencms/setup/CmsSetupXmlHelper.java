/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/Attic/CmsSetupXmlHelper.java,v $
 * Date   : $Date: 2006/03/19 21:48:29 $
 * Version: $Revision: 1.1.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.setup;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

//TODO: add structure modification support
/**
 * Helper class to modify xml files.<p>
 * 
 * This clas does not support adding or removing nodes, just modification of values.<p>
 * 
 * For more info about xpath see: <br>
 * <ul>
 * <li>http://www.w3.org/TR/xpath.html</li>
 * <li>http://www.zvon.org/xxl/XPathTutorial/General/examples.html</li>
 * </ul><p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 6.1.8 
 */
public class CmsSetupXmlHelper {

    /** Optional base path. */
    private String m_basePath = null;

    /** Document cache. */
    private Map m_cache = new HashMap();

    /**
     * Default constructor.<p>
     * 
     * Uses no base path.<p>
     */
    public CmsSetupXmlHelper() {

        // ignore        
    }

    /**
     * Uses an optional base path.<p>
     * 
     * @param basePath the base path to use;
     */
    public CmsSetupXmlHelper(String basePath) {

        m_basePath = basePath;
    }

    /**
     * Discards the changes in the given file.<p>
     * 
     * @param xmlFilename the xml config file (could be relative to the base path)
     */
    public void flush(String xmlFilename) {

        m_cache.remove(xmlFilename);
    }

    /**
     * Discards the changes in all files.<p>
     */
    public void flushAll() {

        m_cache.clear();
    }

    /**
     * Returns the basePath.<p>
     *
     * @return the basePath
     */
    public String getBasePath() {

        return m_basePath;
    }

    /**
     * Returns the value in the given xpath of the given xml file.<p>
     * 
     * @param xmlFile the xml config file
     * @param xPath the xpath to read (should select a single node or attribute)
     * 
     * @return the value in the given xpath of the given xml file, or <code>null</code> if no matching node
     * 
     * @throws CmsXmlException if something goes wrong while reading 
     */
    public String getValue(String xmlFile, String xPath) throws CmsXmlException {

        Document document = getDocument(xmlFile);

        Node node = document.selectSingleNode(xPath);
        if (node != null) {
            // return the value
            return node.getText();
        } else {
            return null;
        }
    }

    /**
     * Sets the given value in the given xpath of the given xml file.<p>
     * 
     * @param xmlFilename the xml config file (could be relative to the base path)
     * @param xPath the xpath to set (should select a single node or attribute)
     * @param value the value to set
     * 
     * @throws CmsXmlException if something goes wrong while reading
     * 
     * @return <code>true</code> if successful
     */
    public boolean setValue(String xmlFilename, String xPath, String value) throws CmsXmlException {

        Document document = getDocument(xmlFilename);

        // do the changes
        Node node = document.selectSingleNode(xPath);
        if (node != null) {
            node.setText(value);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Writes the given file back to disk.<p>
     * 
     * @param xmlFilename the xml config file (could be relative to the base path)
     * 
     * @throws CmsXmlException if something wrong while writing 
     */
    public void write(String xmlFilename) throws CmsXmlException {

        // try to get it from the cache
        Document document = (Document)m_cache.get(xmlFilename);

        if (document != null) {
            try {
                OutputStream out = new FileOutputStream(getFile(xmlFilename));
                CmsXmlUtils.marshal(document, out, "UTF-8");
            } catch (FileNotFoundException e) {
                throw new CmsXmlException(new CmsMessageContainer(null, e.toString()));
            }
        }
    }

    /**
     * Flushes all cached documents.<p>
     * 
     * @throws CmsXmlException if something wrong while writing 
     */
    public void writeAll() throws CmsXmlException {

        Iterator it = new ArrayList(m_cache.keySet()).iterator();
        while (it.hasNext()) {
            String filename = (String)it.next();
            write(filename);
        }
    }

    /**
     * Returns the document for the given filename.<p>
     * It can be new read or come from the document cache.<p>
     * 
     * @param xmlFilename the filename to read
     * 
     * @return the document for the given filename
     * 
     * @throws DocumentException if something goes wrong while reading 
     */
    private Document getDocument(String xmlFilename) throws CmsXmlException {

        // try to get it from the cache
        Document document = (Document)m_cache.get(xmlFilename);

        if (document == null) {
            try {
                document = CmsXmlUtils.unmarshalHelper(
                    new InputSource(new FileReader(getFile(xmlFilename))),
                    new EntityResolver() {

                        /**
                         * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
                         */
                        public InputSource resolveEntity(String publicId, String systemId) {

                            return new InputSource(new StringReader(""));
                        }
                    });
            } catch (FileNotFoundException e) {
                throw new CmsXmlException(new CmsMessageContainer(null, e.toString()));
            }
            // cache the doc
            m_cache.put(xmlFilename, document);
        }
        return document;
    }

    /**
     * Returns a file from a given filename.<p>
     * 
     * @param xmlFilename the file name
     * 
     * @return the file
     */
    private File getFile(String xmlFilename) {

        File file = new File(m_basePath + xmlFilename);
        if (!file.exists() || !file.canRead()) {
            file = new File(xmlFilename);
        }
        return file;
    }

}