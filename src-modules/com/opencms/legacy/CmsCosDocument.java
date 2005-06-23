/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/com/opencms/legacy/Attic/CmsCosDocument.java,v $
 * Date   : $Date: 2005/06/23 10:47:13 $
 * Version: $Revision: 1.6 $
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

package com.opencms.legacy;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.search.A_CmsIndexResource;
import org.opencms.search.documents.I_CmsDocumentFactory;
import org.opencms.search.extractors.CmsExtractionResult;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.util.CmsHtmlExtractor;
import org.opencms.util.CmsStringUtil;

import com.opencms.defaults.master.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

/**
 * Lucene document factory class to extract index data from a cos resource 
 * of any type derived from <code>CmsMasterDataSet</code>.<p>
 * 
 * @version $Revision: 1.6 $ $Date: 2005/06/23 10:47:13 $
 * @author Carsten Weinholz 
 * @author Thomas Weckert  
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsCosDocument implements I_CmsDocumentFactory {

    /** The cos prefix for document keys. */
    public static final String C_DOCUMENT_KEY_PREFIX = "COS";

    /* Matches anything that is not a number, hex-number, uuid or whitespace */
    private static final Pattern C_NON_NUM_UUID_WS = Pattern.compile("[^a-fA-F0-9\\-_\\s]");

    /** The cms object. */
    protected CmsObject m_cms;

    /** Name of the document type. */
    protected String m_name;

    /** Channel of cos document. */
    public static final String DOC_CHANNEL = "channel";

    /** Content id of cos document. */
    public static final String DOC_CONTENT_ID = "contentid";

    /** Content definition of cos document. */
    public static final String DOC_CONTENT_DEFINITION = "contentdefinition";

    /**
     * Creates a new instance of this lucene document factory.<p>
     * 
     * @param cms the cms object
     * @param name name of the documenttype
     */
    public CmsCosDocument(CmsObject cms, String name) {

        m_cms = cms;
        m_name = name;
    }

    /**
     * Returns the raw text content of a given cos resource.<p>
     * The contents of a cos object are accessed using the class <code>CmsMasterDataSet</code>.
     * For indexing purposes, the contents of the arrays <code>m_dataSmall</code>, <code>m_dataMedium</code> 
     * and <code>m_dataBig</code> are collected in a string.
     * 
     * @param cms the cms object
     * @param indexResource the resource
     * @param language the language requested
     * @return the raw text content
     * @throws CmsException if something goes wrong
     */
    public I_CmsExtractionResult extractContent(CmsObject cms, A_CmsIndexResource indexResource, String language)
    throws CmsException {

        CmsMasterDataSet resource = (CmsMasterDataSet)indexResource.getData();
        String result = null;

        try {

            StringBuffer buf = new StringBuffer();

            for (int i = 0; i < resource.m_dataMedium.length; i++) {
                if (resource.m_dataMedium[i] != null && !"".equals(resource.m_dataMedium[i])) {
                    buf.append((i > 0) ? " " : "");
                    buf.append(resource.m_dataMedium[i]);
                }
            }

            for (int i = 0; i < resource.m_dataBig.length; i++) {
                if (resource.m_dataBig[i] != null && !"".equals(resource.m_dataBig[i])) {
                    buf.append((i > 0) ? " " : "");
                    buf.append(resource.m_dataBig[i]);
                }
            }

            for (int i = 0; i < resource.m_dataSmall.length; i++) {
                if (resource.m_dataSmall[i] != null && !"".equals(resource.m_dataSmall[i])) {
                    if (C_NON_NUM_UUID_WS.matcher(resource.m_dataSmall[i]).find()) {
                        buf.append((i > 0) ? " " : "");
                        buf.append(resource.m_dataSmall[i]);
                    }
                }
            }

            result = CmsHtmlExtractor.extractText(buf.toString(), OpenCms.getSystemInfo().getDefaultEncoding());

        } catch (Exception exc) {
            throw new CmsLegacyException("Reading resource " + indexResource.getRootPath() + " failed", exc);
        }

        return new CmsExtractionResult(result);
    }

    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#getDocumentKey(java.lang.String)
     */
    public String getDocumentKey(String resourceType) throws CmsException {

        try {
            return C_DOCUMENT_KEY_PREFIX + ((CmsMasterContent)Class.forName(resourceType).newInstance()).getSubId();
        } catch (Exception exc) {
            throw new CmsLegacyException("Instanciation of resource type class " + resourceType + " failed.", exc);
        }
    }

    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#getDocumentKeys(java.util.List, java.util.List)
     */
    public List getDocumentKeys(List resourceTypes, List mimeTypes) throws CmsException {

        ArrayList keys = new ArrayList();

        try {
            for (Iterator i = resourceTypes.iterator(); i.hasNext();) {

                int id = ((CmsMasterContent)Class.forName((String)i.next()).newInstance()).getSubId();
                for (Iterator j = resourceTypes.iterator(); j.hasNext();) {
                    keys.add(C_DOCUMENT_KEY_PREFIX + id + ":" + (String)j.next());
                }

                keys.add(C_DOCUMENT_KEY_PREFIX + id);
            }
        } catch (Exception exc) {
            throw new CmsLegacyException("Creation of document keys failed.", exc);
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
        CmsMasterDataSet content = (CmsMasterDataSet)resource.getData();
        String value = content.m_title;

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(value)) {
            document.add(Field.Keyword(I_CmsDocumentFactory.DOC_TITLE_KEY, value));
            document.add(Field.UnStored(I_CmsDocumentFactory.DOC_TITLE_INDEXED, value));
            document.add(Field.UnStored(I_CmsDocumentFactory.DOC_META, value));
        }

        document.add(Field.Keyword(I_CmsDocumentFactory.DOC_DATE_CREATED, new Date(content.m_dateCreated)));
        document.add(Field.Keyword(I_CmsDocumentFactory.DOC_DATE_LASTMODIFIED, new Date(content.m_dateLastModified)));

        document.add(Field.Keyword(CmsCosDocument.DOC_CHANNEL, ((CmsCosIndexResource)resource).getChannel()));
        document.add(Field.Keyword(CmsCosDocument.DOC_CONTENT_DEFINITION, ((CmsCosIndexResource)resource)
            .getContentDefinition()));

        String path = m_cms.getRequestContext().removeSiteRoot(resource.getRootPath());
        document.add(Field.UnIndexed(I_CmsDocumentFactory.DOC_PATH, path));
        document.add(Field.UnIndexed(CmsCosDocument.DOC_CONTENT_ID, resource.getId().toString()));

        document
            .add(Field.Text(I_CmsDocumentFactory.DOC_CONTENT, extractContent(cms, resource, language).getContent()));

        return document;
    }
}