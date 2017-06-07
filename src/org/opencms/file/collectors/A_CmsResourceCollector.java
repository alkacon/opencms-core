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

package org.opencms.file.collectors;

import org.opencms.ade.contenteditor.shared.CmsEditorConstants;
import org.opencms.ade.publish.CmsCollectorPublishListHelper;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.I_CmsContentLoadCollectorInfo;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;

/**
 * Provides some helpful base implementations for resource collector classes.<p>
 *
 * @since 6.0.0
 */
public abstract class A_CmsResourceCollector implements I_CmsResourceCollector {

    /** The template file separator string for creating a new resource in direct edit mode,
     *  can be used to append an explicit template file name in {@link #getCreateParam(CmsObject, String, String)}. */
    public static final String SEPARATOR_TEMPLATEFILE = "::";

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(A_CmsResourceCollector.class);

    /** The collector order of this collector. */
    protected int m_order;

    /** The name of the configured default collector. */
    private String m_defaultCollectorName;

    /** The default collector parameters. */
    private String m_defaultCollectorParam;

    /** The hash code of this collector. */
    private int m_hashcode;

    /**
     * Constructor to initialize some default values.<p>
     */
    public A_CmsResourceCollector() {

        m_hashcode = getClass().getName().hashCode();
    }

    /**
     * Creates a new content collector resource.<p>
     *
     * @param cms the cms context
     * @param newLink the new resource link
     * @param locale the content locale
     * @param referenceResource the reference resource
     * @param modelFile the model file
     * @param mode the optional creation mode (can be null)
     * @param postCreateHandlerClass optional class name of class which is invoked after the content has been created (can be null)
     * @return the new file name
     *
     * @throws CmsException if something goes wrong
     */
    public static String createResourceForCollector(
        CmsObject cms,
        String newLink,
        Locale locale,
        String referenceResource,
        String modelFile,
        String mode,
        String postCreateHandlerClass) throws CmsException {

        // get the collector used to create the new content
        int pos = newLink.indexOf('|');
        String collectorName = newLink.substring(0, pos);
        String collectorParams = newLink.substring(pos + 1);

        String param;
        String templateFileName;

        pos = collectorParams.indexOf(A_CmsResourceCollector.SEPARATOR_TEMPLATEFILE);
        if (pos != -1) {
            // found an explicit template file name to use for the new resource, use it
            param = collectorParams.substring(0, pos);
            templateFileName = collectorParams.substring(pos + A_CmsResourceCollector.SEPARATOR_TEMPLATEFILE.length());
        } else {
            // no template file name was specified, use given resource name as template file
            param = collectorParams;
            templateFileName = referenceResource;
        }

        // get the collector used for calculating the next file name
        I_CmsResourceCollector collector = OpenCms.getResourceManager().getContentCollector(collectorName);
        String newFileName = "";
        // one resource serves as a "template" for the new resource
        CmsResource templateResource = cms.readResource(templateFileName, CmsResourceFilter.IGNORE_EXPIRATION);
        CmsXmlContent newContent = null;
        int typeId;
        CmsObject cloneCms = OpenCms.initCmsObject(cms);
        cloneCms.getRequestContext().setRequestTime(CmsResource.DATE_RELEASED_EXPIRED_IGNORE);
        // the reference resource may be a folder in case of creating for an empty collector list
        if (!templateResource.isFolder()) {
            typeId = templateResource.getTypeId();
            CmsFile templateFile = cms.readFile(templateResource);
            CmsXmlContent template = CmsXmlContentFactory.unmarshal(cloneCms, templateFile);
            // now create a new XML content based on the templates content definition
            newContent = CmsXmlContentFactory.createDocument(
                cms,
                locale,
                template.getEncoding(),
                template.getContentDefinition());
        } else {
            typeId = collector.getCreateTypeId(cloneCms, collectorName, collectorParams);
        }
        // IMPORTANT: calculation of the name MUST be done here so the file name is ensured to be valid
        newFileName = collector.getCreateLink(cms, collectorName, param);

        boolean isCopy = StringUtils.equalsIgnoreCase(mode, CmsEditorConstants.MODE_COPY);
        if (isCopy) {
            modelFile = referenceResource;
        }
        boolean useModelFile = false;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(modelFile)) {
            cms.getRequestContext().setAttribute(CmsRequestContext.ATTRIBUTE_MODEL, modelFile);
            useModelFile = true;
        }
        // now create the resource, fill it with the marshalled XML and write it back to the VFS
        cms.createResource(newFileName, typeId);
        // re-read the created resource
        CmsFile newFile = cms.readFile(newFileName, CmsResourceFilter.ALL);
        if (!useModelFile && (newContent != null)) {
            newFile.setContents(newContent.marshal());
            // write the file with the updated content
            cloneCms.writeFile(newFile);
        }
        I_CmsCollectorPostCreateHandler handler = getPostCreateHandler(postCreateHandlerClass);
        handler.onCreate(cms, newFile, isCopy);
        return newFileName;

    }

    /**
     * Instantiates a post-create handler given a class name (which may actually be null).<p>
     *
     * If the given name is null or does not refer to a valid post-create handler class, a default implementation
     * will be returned.<p>
     *
     * @param name the class name of the post-create handler class
     *
     * @return a post-create handler instance
     */
    public static I_CmsCollectorPostCreateHandler getPostCreateHandler(String name) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(name)) {
            return new CmsDefaultPostCreateHandler();
        }
        try {
            Class<?> handlerClass = Class.forName(name);
            if (I_CmsCollectorPostCreateHandler.class.isAssignableFrom(handlerClass)) {
                I_CmsCollectorPostCreateHandler handler = (I_CmsCollectorPostCreateHandler)handlerClass.newInstance();
                return handler;
            } else {
                LOG.error("Post-create handler class does not implement I_CmsPostCreateHandler: '" + name + "'");
                return new CmsDefaultPostCreateHandler();
            }
        } catch (Exception e) {
            LOG.error("Problem using post-create handler: '" + name + "'," + e.getLocalizedMessage(), e);
            return new CmsDefaultPostCreateHandler();
        }
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(I_CmsResourceCollector obj) {

        if (obj == this) {
            return 0;
        }
        return getOrder() - obj.getOrder();
    }

    /**
     * Two collectors are considered to be equal if they are sharing the same
     * implementation class.<p>
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof I_CmsResourceCollector) {
            return getClass().getName().equals(obj.getClass().getName());
        }
        return false;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCreateLink(org.opencms.file.CmsObject)
     */
    public String getCreateLink(CmsObject cms) throws CmsException, CmsDataAccessException {

        checkParams();
        return getCreateLink(cms, getDefaultCollectorName(), getDefaultCollectorParam());
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCreateParam(org.opencms.file.CmsObject)
     */
    public String getCreateParam(CmsObject cms) throws CmsDataAccessException {

        checkParams();
        return getCreateParam(cms, getDefaultCollectorName(), getDefaultCollectorParam());
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCreateTypeId(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    @SuppressWarnings("unused")
    public int getCreateTypeId(CmsObject cms, String collectorName, String param) throws CmsException {

        // overwrite to allow creation of new items
        return -1;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getDefaultCollectorName()
     */
    public String getDefaultCollectorName() {

        return m_defaultCollectorName;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getDefaultCollectorParam()
     */
    public String getDefaultCollectorParam() {

        return m_defaultCollectorParam;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getOrder()
     */
    public int getOrder() {

        return m_order;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsCollectorPublishListProvider#getPublishResources(org.opencms.file.CmsObject, org.opencms.gwt.shared.I_CmsContentLoadCollectorInfo)
     */
    public Set<CmsResource> getPublishResources(final CmsObject cms, final I_CmsContentLoadCollectorInfo info)
    throws CmsException {

        int collectorLimit = NumberUtils.toInt(
            OpenCms.getADEManager().getParameters(cms).get(CmsGwtConstants.COLLECTOR_PUBLISH_LIST_LIMIT),
            DEFAULT_LIMIT);
        CmsCollectorPublishListHelper helper = new CmsCollectorPublishListHelper(cms, info, collectorLimit);
        return helper.getPublishListFiles();

    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getResults(org.opencms.file.CmsObject)
     */
    public List<CmsResource> getResults(CmsObject cms) throws CmsDataAccessException, CmsException {

        checkParams();
        return getResults(cms, getDefaultCollectorName(), getDefaultCollectorParam());
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_hashcode;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#setDefaultCollectorName(java.lang.String)
     */
    public void setDefaultCollectorName(String collectorName) {

        m_defaultCollectorName = collectorName;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#setDefaultCollectorParam(java.lang.String)
     */
    public void setDefaultCollectorParam(String param) {

        m_defaultCollectorParam = param;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#setOrder(int)
     */
    public void setOrder(int order) {

        m_order = order;
    }

    /**
     * Checks if the required parameters have been set.<p>
     *
     * @see #setDefaultCollectorName(String)
     * @see #setDefaultCollectorParam(String)
     */
    protected void checkParams() {

        if ((m_defaultCollectorName == null) || (m_defaultCollectorParam == null)) {
            throw new CmsIllegalArgumentException(
                Messages.get().container(
                    Messages.ERR_COLLECTOR_DEFAULTS_INVALID_2,
                    m_defaultCollectorName,
                    m_defaultCollectorParam));
        }
    }

    /**
     * Returns the link to create a new XML content item in the folder pointed to by the parameter.<p>
     *
     * @param cms the current CmsObject
     * @param data the collector data to use
     *
     * @return the link to create a new XML content item in the folder
     *
     * @throws CmsException if something goes wrong
     *
     * @since 7.0.2
     */
    protected String getCreateInFolder(CmsObject cms, CmsCollectorData data) throws CmsException {

        return OpenCms.getResourceManager().getNameGenerator().getNewFileName(cms, data.getFileName(), 4);
    }

    /**
     * Returns the link to create a new XML content item in the folder pointed to by the parameter.<p>
     *
     * @param cms the current CmsObject
     * @param param the folder name to use
     *
     * @return the link to create a new XML content item in the folder
     *
     * @throws CmsException if something goes wrong
     */
    protected String getCreateInFolder(CmsObject cms, String param) throws CmsException {

        return getCreateInFolder(cms, new CmsCollectorData(param));
    }

    /**
     * Shrinks a List to fit a maximum size.<p>
     *
     * @param result a List
     * @param maxSize the maximum size of the List
     *
     * @return the reduced list
     */
    protected List<CmsResource> shrinkToFit(List<CmsResource> result, int maxSize) {

        if ((maxSize > 0) && (result.size() > maxSize)) {
            // cut off all items > count
            result = result.subList(0, maxSize);
        }

        return result;
    }

    /**
     * Shrinks a List to fit a maximum size.<p>
     *
     * @param result a List
     * @param maxSize the maximum size of the List
     * @param explicitNumResults the value of the numResults parameter given to the getResults method (this overrides maxSize if it is positive)
     *
     * @return the reduced list
     */
    protected List<CmsResource> shrinkToFit(List<CmsResource> result, int maxSize, int explicitNumResults) {

        return shrinkToFit(result, explicitNumResults > 0 ? explicitNumResults : maxSize);
    }
}