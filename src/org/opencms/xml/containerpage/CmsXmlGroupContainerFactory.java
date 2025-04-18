/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.xml.containerpage;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.history.I_CmsHistoryResource;
import org.opencms.i18n.CmsEncoder;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.Messages;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

import javax.servlet.ServletRequest;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.xml.sax.EntityResolver;

/**
 * Provides factory methods to unmarshal (read) an group container object.<p>
 *
 * @since 8.0.0
 */
public final class CmsXmlGroupContainerFactory {

    /**
     * No instances of this class should be created.<p>
     */
    private CmsXmlGroupContainerFactory() {

        // noop
    }

    /**
     * Create a new instance of an group container based on the given default content,
     * that will have all language nodes of the default content and ensures the presence of the given locale.<p>
     *
     * The given encoding is used when marshalling the XML again later.<p>
     *
     * @param cms the current users OpenCms content
     * @param locale the locale to generate the default content for
     * @param modelUri the absolute path to the group container file acting as model
     *
     * @throws CmsException in case the model file is not found or not valid
     *
     * @return the created group container
     */
    public static CmsXmlGroupContainer createDocument(CmsObject cms, Locale locale, String modelUri)
    throws CmsException {

        // create the XML content
        CmsXmlGroupContainer content = new CmsXmlGroupContainer(cms, locale, modelUri);
        // call prepare for use content handler and return the result
        return (CmsXmlGroupContainer)content.getHandler().prepareForUse(cms, content);
    }

    /**
     * Create a new instance of a group container based on the given content definition,
     * that will have one language node for the given locale all initialized with default values.<p>
     *
     * The given encoding is used when marshalling the XML again later.<p>
     *
     * @param cms the current users OpenCms content
     * @param locale the locale to generate the default content for
     * @param encoding the encoding to use when marshalling the XML content later
     * @param contentDefinition the content definition to create the content for
     *
     * @return the created group container
     */
    public static CmsXmlGroupContainer createDocument(
        CmsObject cms,
        Locale locale,
        String encoding,
        CmsXmlContentDefinition contentDefinition) {

        // create the XML content
        CmsXmlGroupContainer content = new CmsXmlGroupContainer(cms, locale, encoding, contentDefinition);
        // call prepare for use content handler and return the result
        return (CmsXmlGroupContainer)content.getHandler().prepareForUse(cms, content);
    }

    /**
     * Factory method to unmarshal (generate) a group container instance from a byte array
     * that contains XML data.<p>
     *
     * When unmarshalling, the encoding is read directly from the XML header of the byte array.
     * The given encoding is used only when marshalling the XML again later.<p>
     *
     * <b>Warning:</b><br/>
     * This method does not support requested historic versions, it always loads the
     * most recent version. Use <code>{@link #unmarshal(CmsObject, CmsResource, ServletRequest)}</code>
     * for history support.<p>
     *
     * @param cms the cms context
     * @param xmlData the XML data in a byte array
     * @param encoding the encoding to use when marshalling the XML content later
     * @param resolver the XML entitiy resolver to use
     *
     * @return a group container instance unmarshalled from the byte array
     *
     * @throws CmsXmlException if something goes wrong
     */
    public static CmsXmlGroupContainer unmarshal(
        CmsObject cms,
        byte[] xmlData,
        String encoding,
        EntityResolver resolver) throws CmsXmlException {

        return unmarshal(cms, CmsXmlUtils.unmarshalHelper(xmlData, resolver), encoding, resolver);
    }

    /**
     * Factory method to unmarshal (read) a group container instance from a OpenCms VFS file
     * that contains XML data.<p>
     *
     * <b>Warning:</b><br/>
     * This method does not support requested historic versions, it always loads the
     * most recent version. Use <code>{@link #unmarshal(CmsObject, CmsResource, ServletRequest)}</code>
     * for history support.<p>
     *
     * @param cms the current cms object
     * @param file the file with the XML data to unmarshal
     *
     * @return a group container instance unmarshalled from the provided file
     *
     * @throws CmsXmlException if something goes wrong
     */
    public static CmsXmlGroupContainer unmarshal(CmsObject cms, CmsFile file) throws CmsXmlException {

        return unmarshal(cms, file, true);
    }

    /**
     * Factory method to unmarshal (read) a group container instance from a OpenCms VFS file
     * that contains XML data, using wither the encoding set
     * in the XML file header, or the encoding set in the VFS file property.<p>
     *
     * If you are not sure about the implications of the encoding issues,
     * use {@link #unmarshal(CmsObject, CmsFile)} instead.<p>
     *
     * <b>Warning:</b><br/>
     * This method does not support requested historic versions, it always loads the
     * most recent version. Use <code>{@link #unmarshal(CmsObject, CmsResource, ServletRequest)}</code>
     * for history support.<p>
     *
     * @param cms the current cms object
     * @param file the file with the XML data to unmarshal
     * @param keepEncoding if <code>true</code>, the encoding specified in the XML header is used,
     *    otherwise the encoding from the VFS file property is used
     *
     * @return a group container instance unmarshalled from the provided file
     *
     * @throws CmsXmlException if something goes wrong
     */
    public static CmsXmlGroupContainer unmarshal(CmsObject cms, CmsFile file, boolean keepEncoding)
    throws CmsXmlException {

        // check the cache
        CmsXmlGroupContainer content = getCache(cms, file, keepEncoding);
        if (content != null) {
            return content;
        }

        // not found in cache, read as normally
        byte[] contentBytes = file.getContents();
        String filename = cms.getSitePath(file);

        String encoding = null;
        try {
            encoding = cms.readPropertyObject(
                filename,
                CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING,
                true).getValue();
        } catch (CmsException e) {
            // encoding will be null
        }
        if (encoding == null) {
            encoding = OpenCms.getSystemInfo().getDefaultEncoding();
        } else {
            encoding = CmsEncoder.lookupEncoding(encoding, null);
            if (encoding == null) {
                throw new CmsXmlException(Messages.get().container(Messages.ERR_XMLCONTENT_INVALID_ENC_1, filename));
            }
        }

        if (contentBytes.length > 0) {
            // content is initialized
            if (keepEncoding) {
                // use the encoding from the content
                content = unmarshal(cms, contentBytes, encoding, new CmsXmlEntityResolver(cms));
            } else {
                // use the encoding from the file property
                // this usually only triggered by a save operation
                try {
                    String contentStr = new String(contentBytes, encoding);
                    content = unmarshal(cms, contentStr, encoding, new CmsXmlEntityResolver(cms));
                } catch (UnsupportedEncodingException e) {
                    // this will not happen since the encoding has already been validated
                    throw new CmsXmlException(
                        Messages.get().container(Messages.ERR_XMLCONTENT_INVALID_ENC_1, filename));
                }
            }
        } else {
            // content is empty
            content = new CmsXmlGroupContainer(
                cms,
                DocumentHelper.createDocument(),
                encoding,
                new CmsXmlEntityResolver(cms));
        }

        // set the file
        content.setFile(file);
        // call prepare for use content handler and return the result
        CmsXmlGroupContainer xmlGroupContainer = (CmsXmlGroupContainer)content.getHandler().prepareForUse(cms, content);

        // set the cache
        setCache(cms, xmlGroupContainer, keepEncoding);

        return xmlGroupContainer;
    }

    /**
     * Factory method to unmarshal (read) a group container instance from a OpenCms VFS resource
     * that contains XML data.<p>
     *
     * <b>Warning:</b><br/>
     * This method does not support requested historic versions, it always loads the
     * most recent version. Use <code>{@link #unmarshal(CmsObject, CmsResource, ServletRequest)}</code>
     * for history support.<p>
     *
     * @param cms the current cms object
     * @param resource the resource with the XML data to unmarshal
     *
     * @return a group container instance unmarshalled from the provided resource
     *
     * @throws CmsException if something goes wrong
     */
    public static CmsXmlGroupContainer unmarshal(CmsObject cms, CmsResource resource) throws CmsException {

        // check the cache
        CmsXmlGroupContainer content = getCache(cms, resource, true);
        if (content != null) {
            return content;
        }

        content = unmarshal(cms, cms.readFile(resource), true);

        // set the cache
        setCache(cms, content, true);

        return content;
    }

    /**
     * Factory method to unmarshal (read) a group container instance from
     * a resource, using the request attributes as cache.<p>
     *
     * @param cms the current OpenCms context object
     * @param resource the resource to unmarshal
     * @param req the current request
     *
     * @return the unmarshaled xml content
     *
     * @throws CmsException in something goes wrong
     * @throws CmsLoaderException if no loader for the given <code>resource</code> type ({@link CmsResource#getTypeId()}) is available
     * @throws CmsXmlException if the given <code>resource</code> is not of type group container
     */
    public static CmsXmlGroupContainer unmarshal(CmsObject cms, CmsResource resource, ServletRequest req)
    throws CmsXmlException, CmsLoaderException, CmsException {

        String rootPath = resource.getRootPath();

        // try to get the requested content from the current request attribute
        // this is also necessary for historic versions that have been loaded
        CmsXmlGroupContainer content = (CmsXmlGroupContainer)req.getAttribute(rootPath);

        if (content == null) {
            // unmarshal XML structure from the file content
            content = unmarshal(cms, resource);
            // store the content as request attribute for future read requests
            req.setAttribute(rootPath, content);
        }

        // return the result
        return content;
    }

    /**
     * Factory method to unmarshal (generate) a group container instance from a XML document.<p>
     *
     * The given encoding is used when marshalling the XML again later.<p>
     *
     * <b>Warning:</b><br/>
     * This method does not support requested historic versions, it always loads the
     * most recent version. Use <code>{@link #unmarshal(CmsObject, CmsResource, ServletRequest)}</code>
     * for history support.<p>
     *
     * @param cms the cms context, if <code>null</code> no link validation is performed
     * @param document the XML document to generate the group container from
     * @param encoding the encoding to use when marshalling the group container later
     * @param resolver the XML entity resolver to use
     *
     * @return a group container instance unmarshalled from the String
     */
    public static CmsXmlGroupContainer unmarshal(
        CmsObject cms,
        Document document,
        String encoding,
        EntityResolver resolver) {

        CmsXmlGroupContainer content = new CmsXmlGroupContainer(cms, document, encoding, resolver);
        // call prepare for use content handler and return the result
        return (CmsXmlGroupContainer)content.getHandler().prepareForUse(cms, content);
    }

    /**
     * Factory method to unmarshal (generate) a group container instance from a String
     * that contains XML data.<p>
     *
     * The given encoding is used when marshalling the XML again later.<p>
     *
     * <b>Warning:</b><br/>
     * This method does not support requested historic versions, it always loads the
     * most recent version. Use <code>{@link #unmarshal(CmsObject, CmsResource, ServletRequest)}</code>
     * for history support.<p>
     *
     * @param cms the cms context, if <code>null</code> no link validation is performed
     * @param xmlData the XML data in a String
     * @param encoding the encoding to use when marshalling the group container later
     * @param resolver the XML entity resolver to use
     *
     * @return a group container instance unmarshalled from the String
     *
     * @throws CmsXmlException if something goes wrong
     */
    public static CmsXmlGroupContainer unmarshal(
        CmsObject cms,
        String xmlData,
        String encoding,
        EntityResolver resolver) throws CmsXmlException {

        // create the XML content object from the provided String
        return unmarshal(cms, CmsXmlUtils.unmarshalHelper(xmlData, resolver), encoding, resolver);
    }

    /**
     * Gets the ADE cache from the ADE manager.<p>
     *
     * @return the ADE cache
     */
    private static CmsADECache getCache() {

        return OpenCms.getADEManager().getCache();
    }

    /**
     * Returns the cached group container.<p>
     *
     * @param cms the cms context
     * @param resource the group container resource
     * @param keepEncoding if to keep the encoding while unmarshalling
     *
     * @return the cached group container, or <code>null</code> if not found
     */
    private static CmsXmlGroupContainer getCache(CmsObject cms, CmsResource resource, boolean keepEncoding) {

        if (resource instanceof I_CmsHistoryResource) {
            return null;
        }
        return getCache().getCacheGroupContainer(
            getCache().getCacheKey(resource.getStructureId(), keepEncoding),
            cms.getRequestContext().getCurrentProject().isOnlineProject());
    }

    /**
     * Stores the given group container in the cache.<p>
     *
     * @param cms the cms context
     * @param xmlGroupContainer the group container to cache
     * @param keepEncoding if the encoding was kept while unmarshalling
     */
    private static void setCache(CmsObject cms, CmsXmlGroupContainer xmlGroupContainer, boolean keepEncoding) {

        if (xmlGroupContainer.getFile() instanceof I_CmsHistoryResource) {
            return;
        }
        boolean online = cms.getRequestContext().getCurrentProject().isOnlineProject();
        getCache().setCacheGroupContainer(
            getCache().getCacheKey(xmlGroupContainer.getFile().getStructureId(), keepEncoding),
            xmlGroupContainer,
            online);
    }

}
