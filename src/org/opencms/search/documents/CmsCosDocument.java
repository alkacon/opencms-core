/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/documents/Attic/CmsCosDocument.java,v $
 * Date   : $Date: 2004/02/13 13:41:45 $
 * Version: $Revision: 1.2 $
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
package org.opencms.search.documents;

import org.opencms.main.CmsException;
import org.opencms.search.CmsIndexException;
import org.opencms.search.CmsIndexResource;

import com.opencms.defaults.master.CmsMasterDataSet;
import org.opencms.file.CmsObject;

import java.util.regex.Pattern;

import org.apache.lucene.document.DateField;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

/**
 * @version $Revision: 1.2 $ $Date: 2004/02/13 13:41:45 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 */
public class CmsCosDocument implements I_CmsDocumentFactory {

    /* Matches anything that is not a number, hex-number, uuid or whitespace */
    private static final Pattern C_NON_NUM_UUID_WS = Pattern.compile("[^a-fA-F0-9\\-_\\s]");
    
    /**
     * The cms object
     */
    protected CmsObject m_cms;
    
    /**
     * Name of the documenttype
     */
    protected String m_name;
    
    /**
     * Creates a new instance of a lucene document for CmsResources.<p>
     * 
     * @param cms the cms object
     * @param name name of the documenttype
     */
    public CmsCosDocument(CmsObject cms, String name) {
        m_cms = cms;
        m_name = name;
    }

    /**
     * Returns the raw text content of a given resource.<p>
     * 
     * @param indexResource the resource
     * @param language the language requested
     * @return the raw text content
     * @throws CmsException if something goes wrong
     */
    public String getRawContent(CmsIndexResource indexResource, String language) throws CmsException {        
        
        CmsMasterDataSet resource = (CmsMasterDataSet)indexResource.getObject();
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
            rawContent = extractor.extractText(buf.toString());
            
        } catch (Exception exc) {
            throw new CmsIndexException("Reading resource " + indexResource.getRootPath() + " failed", exc);
        }

        return rawContent;
    }
    
    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#newInstance(org.opencms.search.CmsIndexResource, java.lang.String)
     */
    public Document newInstance (CmsIndexResource resource, String language) throws CmsException {
        
        Document document = new Document();
        CmsMasterDataSet content = (CmsMasterDataSet)resource.getObject();
        String path = m_cms.getRequestContext().removeSiteRoot(resource.getRootPath());
        String value;

        if ((value = content.m_title) != null) {
            document.add(Field.Text(I_CmsDocumentFactory.DOC_TITLE, value));
        }                       

        document.add(Field.Keyword(I_CmsDocumentFactory.DOC_DATE_CREATED, 
            DateField.timeToString(content.m_dateCreated)));
        document.add(Field.Keyword(I_CmsDocumentFactory.DOC_DATE_LASTMODIFIED, 
            DateField.timeToString(content.m_dateLastModified)));

        document.add(Field.Keyword(I_CmsDocumentFactory.DOC_CHANNEL, resource.getChannel()));
        document.add(Field.Keyword(I_CmsDocumentFactory.DOC_CONTENT_DEFINITION, resource.getContentDefinition()));
        document.add(Field.Keyword(I_CmsDocumentFactory.DOC_PATH, path));
        
        document.add(Field.UnIndexed(I_CmsDocumentFactory.DOC_CONTENT_ID, resource.getId().toString()));
        document.add(Field.Text(I_CmsDocumentFactory.DOC_CONTENT, getRawContent(resource, language)));

        return document;
    }
        
    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#getName()
     */
    public String getName() {
        return m_name;
    }
}
