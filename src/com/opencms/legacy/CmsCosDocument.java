/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/legacy/Attic/CmsCosDocument.java,v $
 * Date   : $Date: 2004/10/28 13:20:53 $
 * Version: $Revision: 1.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.search.CmsIndexException;
import org.opencms.search.documents.CmsHtmlExtractor;
import org.opencms.search.documents.I_CmsDocumentFactory;

import com.opencms.defaults.master.*;

import java.util.regex.Pattern;

import org.apache.lucene.document.DateField;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

/**
 * Lucene document factory class to extract index data from a cos resource 
 * of any type derived from <code>CmsMasterDataSet</code>.<p>
 * 
 * @version $Revision: 1.6 $ $Date: 2004/10/28 13:20:53 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsCosDocument implements I_CmsCosDocumentFactory {

    /* Matches anything that is not a number, hex-number, uuid or whitespace */
    private static final Pattern C_NON_NUM_UUID_WS = Pattern.compile("[^a-fA-F0-9\\-_\\s]");
    
    /** The cms object. */
    protected CmsObject m_cms;
    
    /** Name of the document type. */
    protected String m_name;
    
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
     * @param indexResource the resource
     * @param language the language requested
     * @return the raw text content
     * @throws CmsException if something goes wrong
     */
    public String getRawContent(A_CmsIndexResource indexResource, String language) throws CmsException {        
        
        CmsMasterDataSet resource = (CmsMasterDataSet)indexResource.getData();
        String rawContent = null;
        
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
            
            CmsHtmlExtractor extractor = new CmsHtmlExtractor();
            rawContent = extractor.extractText(buf.toString(), OpenCms.getSystemInfo().getDefaultEncoding());
            
        } catch (Exception exc) {
            throw new CmsIndexException("Reading resource " + indexResource.getRootPath() + " failed", exc);
        }

        return rawContent;
    }
    
    /**
     * Generates a new lucene document instance from contents of the given resource.<p>
     * 
     * @see org.opencms.search.documents.I_CmsDocumentFactory#newInstance(org.opencms.search.A_CmsIndexResource, java.lang.String)
     */
    public Document newInstance (A_CmsIndexResource resource, String language) throws CmsException {
        
        Document document = new Document();
        CmsMasterDataSet content = (CmsMasterDataSet)resource.getData();
        String path = m_cms.getRequestContext().removeSiteRoot(resource.getRootPath());
        String value;

        if ((value = content.m_title) != null) {
            document.add(Field.Text(I_CmsDocumentFactory.DOC_TITLE, value));
        }                       

        document.add(Field.Keyword(I_CmsDocumentFactory.DOC_DATE_CREATED, 
            DateField.timeToString(content.m_dateCreated)));
        document.add(Field.Keyword(I_CmsDocumentFactory.DOC_DATE_LASTMODIFIED, 
            DateField.timeToString(content.m_dateLastModified)));

        document.add(Field.Keyword(I_CmsCosDocumentFactory.DOC_CHANNEL, ((CmsCosIndexResource)resource).getChannel()));
        document.add(Field.Keyword(I_CmsCosDocumentFactory.DOC_CONTENT_DEFINITION, ((CmsCosIndexResource)resource).getContentDefinition()));
        document.add(Field.Keyword(I_CmsDocumentFactory.DOC_PATH, path));
        
        document.add(Field.UnIndexed(I_CmsCosDocumentFactory.DOC_CONTENT_ID, resource.getId().toString()));
        document.add(Field.Text(I_CmsDocumentFactory.DOC_CONTENT, getRawContent(resource, language)));

        return document;
    }
        
    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#getName()
     */
    public String getName() {
        return m_name;
    }

    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#getDocumentKey(java.lang.String)
     */
    public String getDocumentKey(String resourceType) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return "COS" + ((CmsMasterContent)Class.forName(resourceType).newInstance()).getSubId();
    }
}
