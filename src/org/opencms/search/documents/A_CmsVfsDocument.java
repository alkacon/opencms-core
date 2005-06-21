/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/documents/A_CmsVfsDocument.java,v $
 * Date   : $Date: 2005/06/21 15:49:58 $
 * Version: $Revision: 1.5 $
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

package org.opencms.search.documents;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.search.A_CmsIndexResource;
import org.opencms.search.CmsIndexException;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

/**
 * Base document factory class for a VFS <code>{@link org.opencms.file.CmsResource}</code>, 
 * just requires a specialized implementation of 
 * <code>{@link I_CmsDocumentFactory#extractContent(CmsObject, A_CmsIndexResource, String)}</code>
 * for text extraction from the binary document content.<p>
 * 
 * @version $Revision: 1.18 
 * 
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 */
public abstract class A_CmsVfsDocument implements I_CmsDocumentFactory {

    /** The vfs prefix for document keys. */
    public static final String C_VFS_DOCUMENT_KEY_PREFIX = "VFS";

    /**
     * Name of the documenttype.
     */
    protected String m_name;

    /**
     * Creates a new instance of this lucene document factory.<p>
     * 
     * @param name name of the documenttype
     */
    public A_CmsVfsDocument(String name) {

        m_name = name;
    }

    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#getDocumentKey(java.lang.String)
     */
    public String getDocumentKey(String resourceType) throws CmsIndexException {

        try {
            return C_VFS_DOCUMENT_KEY_PREFIX
                + ((I_CmsResourceType)Class.forName(resourceType).newInstance()).getTypeId();
        } catch (Exception exc) {
            throw new CmsIndexException(Messages.get().container(
                Messages.ERR_RESOURCE_TYPE_INSTANTIATION_1,
                resourceType), exc);
        }
    }

    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#getDocumentKeys(java.util.List, java.util.List)
     */
    public List getDocumentKeys(List resourceTypes, List mimeTypes) throws CmsException {

        ArrayList keys = new ArrayList();

        if (resourceTypes.contains("*")) {
            ArrayList allTypes = new ArrayList();
            for (Iterator i = OpenCms.getResourceManager().getResourceTypes().iterator(); i.hasNext();) {
                I_CmsResourceType resourceType = (I_CmsResourceType)i.next();
                allTypes.add(resourceType.getTypeName());
            }
            resourceTypes = allTypes;
        }

        try {
            for (Iterator i = resourceTypes.iterator(); i.hasNext();) {

                int id = OpenCms.getResourceManager().getResourceType((String)i.next()).getTypeId();
                for (Iterator j = mimeTypes.iterator(); j.hasNext();) {
                    keys.add(C_VFS_DOCUMENT_KEY_PREFIX + id + ":" + (String)j.next());
                }
                if (mimeTypes.isEmpty()) {
                    keys.add(C_VFS_DOCUMENT_KEY_PREFIX + id);
                }
            }
        } catch (Exception exc) {
            throw new CmsException(Messages.get().container(Messages.ERR_CREATE_DOC_KEY_0), exc);
        }

        return keys;
    }

    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#getName()
     */
    public String getName() {

        return m_name;
    }

    /**
     * Generates a new lucene document instance from contents of the given resource.<p>
     * 
     * @see org.opencms.search.documents.I_CmsDocumentFactory#newInstance(org.opencms.file.CmsObject, org.opencms.search.A_CmsIndexResource, java.lang.String)
     */
    public Document newInstance(CmsObject cms, A_CmsIndexResource resource, String language) throws CmsException {

        Document document = new Document();
        CmsResource res = (CmsResource)resource.getData();
        String path = cms.getRequestContext().removeSiteRoot(resource.getRootPath());

        // extract the content from the resource
        I_CmsExtractionResult content = extractContent(cms, resource, language);
        String text = mergeMetaInfo(content);
        if (text != null) {
            document.add(Field.Text(I_CmsDocumentFactory.DOC_CONTENT, text));
        }

        StringBuffer meta = new StringBuffer(512);
        String value;
        Field field;

        // add the title from the property
        value = cms.readPropertyObject(path, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue();
        if (CmsStringUtil.isNotEmpty(value)) {
            value = value.trim();
            if (value.length() > 0) {
                // add title as keyword, required for sorting
                field = Field.Keyword(I_CmsDocumentFactory.DOC_TITLE_KEY, value);
                // title keyword field should not affect the boost factor
                field.setBoost(0);
                document.add(field);
                // add title again as indexed field for searching
                document.add(Field.UnStored(I_CmsDocumentFactory.DOC_TITLE_INDEXED, value));
                meta.append(value);
                meta.append(" ");
            }
        }
        // add the keywords from the property
        value = cms.readPropertyObject(path, CmsPropertyDefinition.PROPERTY_KEYWORDS, false).getValue();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(value)) {
            document.add(Field.Text(I_CmsDocumentFactory.DOC_KEYWORDS, value));
            meta.append(value);
            meta.append(" ");
        }
        // add the description from the property
        value = cms.readPropertyObject(path, CmsPropertyDefinition.PROPERTY_DESCRIPTION, false).getValue();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(value)) {
            document.add(Field.Text(I_CmsDocumentFactory.DOC_DESCRIPTION, value));
            meta.append(value);
            meta.append(" ");
        }
        // add the collected meta information
        String metaInf = meta.toString();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(metaInf)) {
            document.add(Field.UnStored(I_CmsDocumentFactory.DOC_META, metaInf));
        }

        // add the category of the file (this is searched so the value can also be attached on a folder)
        value = cms.readPropertyObject(path, CmsPropertyDefinition.PROPERTY_SEARCH_CATEGORY, true).getValue();
        if (CmsStringUtil.isNotEmpty(value)) {
            // all categorys are internally stored lower case
            value = value.trim().toLowerCase();
            if (value.length() > 0) {
                field = Field.Keyword(I_CmsDocumentFactory.DOC_CATEGORY, value);
                field.setBoost(0);
                document.add(field);
            }
        }

        // add the document root path, optimized for use with a phrase query
        String rootPath = CmsSearchIndex.rootPathRewrite(resource.getRootPath());
        field = Field.Text(I_CmsDocumentFactory.DOC_ROOT, rootPath);
        // set boost of 0 to root path field, since root path should have no effect on search result score 
        field.setBoost(0);
        document.add(field);
        // root path is stored again in "plain" format, but not for indexing since I_CmsDocumentFactory.DOC_ROOT is used for that
        document.add(Field.UnIndexed(I_CmsDocumentFactory.DOC_PATH, resource.getRootPath()));

        // add date of creation and last modification as keywords (for sorting)
        field = Field.Keyword(I_CmsDocumentFactory.DOC_DATE_CREATED, new Date(res.getDateCreated()));
        field.setBoost(0);
        document.add(field);
        field = Field.Keyword(I_CmsDocumentFactory.DOC_DATE_LASTMODIFIED, new Date(res.getDateLastModified()));
        field.setBoost(0);
        document.add(field);

        // special field for VFS documents - add a marker so that the document can be identified as VFS resource
        document.add(Field.UnIndexed(I_CmsDocumentFactory.DOC_TYPE, C_VFS_DOCUMENT_KEY_PREFIX));

        float boost = 1.0f;
        // note that the priority property IS searched, so you can easily flag whole folders as "high" or "low"
        if ((value = cms.readPropertyObject(path, CmsPropertyDefinition.PROPERTY_SEARCH_PRIORITY, true).getValue()) != null) {
            value = value.trim().toLowerCase();
            if (value.equals(I_CmsDocumentFactory.SEARCH_PRIORITY_MAX_VALUE)) {
                boost = 2.0f;
            } else if (value.equals(I_CmsDocumentFactory.SEARCH_PRIORITY_HIGH_VALUE)) {
                boost = 1.5f;
            } else if (value.equals(I_CmsDocumentFactory.SEARCH_PRIORITY_LOW_VALUE)) {
                boost = 0.5f;
            }
        }
        // set document boost factor
        document.setBoost(boost);

        return document;
    }

    /**
     * Returns a String created out of the content and the most important meta information in the given 
     * extraction result.<p>
     * 
     * OpenCms uses it's own properties for the text "Title" etc. field, this method ensures
     * the most important document meta information can still be found as part of the content.<p> 
     * 
     * @param extractedContent the extraction result to merge
     * 
     * @return a String created out of the most important meta information in the given map and the content
     */
    protected String mergeMetaInfo(I_CmsExtractionResult extractedContent) {

        Map metaInfo = extractedContent.getMetaInfo();
        String content = extractedContent.getContent();

        if (((metaInfo == null) || (metaInfo.size() == 0)) && (CmsStringUtil.isEmpty(content))) {
            return null;
        }

        StringBuffer result = new StringBuffer(4096);
        if (metaInfo != null) {
            String meta;
            meta = (String)metaInfo.get(I_CmsExtractionResult.META_TITLE);
            if (CmsStringUtil.isNotEmpty(meta)) {
                result.append(meta);
                result.append('\n');
            }
            meta = (String)metaInfo.get(I_CmsExtractionResult.META_SUBJECT);
            if (CmsStringUtil.isNotEmpty(meta)) {
                result.append(meta);
                result.append('\n');
            }
            meta = (String)metaInfo.get(I_CmsExtractionResult.META_KEYWORDS);
            if (CmsStringUtil.isNotEmpty(meta)) {
                result.append(meta);
                result.append('\n');
            }
            meta = (String)metaInfo.get(I_CmsExtractionResult.META_COMMENTS);
            if (CmsStringUtil.isNotEmpty(meta)) {
                result.append(meta);
                result.append('\n');
            }
        }

        if (content != null) {
            result.append(content);
        }

        return result.toString();
    }

    /**
     * Upgrades the given resource to a {@link CmsFile} with content.<p>
     * 
     * @param cms the current users OpenCms context
     * @param resource the resource to upgrade
     * 
     * @return the given resource upgraded to a {@link CmsFile} with content
     * 
     * @throws CmsException if the resource could not be read 
     * @throws CmsIndexException if the resource has no content
     */
    protected CmsFile readFile(CmsObject cms, CmsResource resource) throws CmsException, CmsIndexException {

        CmsFile file = CmsFile.upgrade(resource, cms);
        if (file.getLength() <= 0) {
            throw new CmsIndexException(Messages.get().container(Messages.ERR_NO_CONTENT_1, resource.getRootPath()));
        }
        return file;
    }
}