/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/documents/Attic/CmsPdfDocument.java,v $
 * Date   : $Date: 2004/02/13 13:41:45 $
 * Version: $Revision: 1.3 $
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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.pdfbox.encryption.DecryptDocument;
import org.pdfbox.exceptions.CryptographyException;
import org.pdfbox.exceptions.InvalidPasswordException;
import org.pdfbox.pdfparser.PDFParser;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;

/**
 * @version $Revision: 1.3 $ $Date: 2004/02/13 13:41:45 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 */
public class CmsPdfDocument extends CmsVfsDocument {

    /**
     * Creates a new instance of a lucene document for PDF files.<p>
     * 
     * @param cms the cms object
     * @param name name of the documenttype
     */
    public CmsPdfDocument (CmsObject cms, String name) {
        super(cms, name);
    }
    
    /**
     * @see org.opencms.search.documents.CmsVfsDocument#getRawContent(org.opencms.search.CmsIndexResource, java.lang.String)
     */
    public String getRawContent(CmsIndexResource indexResource, String language) throws CmsException {

        CmsResource resource = (CmsResource)indexResource.getObject();
        PDDocument pdfDocument = null;
        String rawContent = null;
        
        try {
            CmsFile file = m_cms.readFile(m_cms.getRequestContext().removeSiteRoot(resource.getRootPath()));
            if (!(file.getLength() > 0)) {
                throw new CmsIndexException("Resource " + resource.getRootPath() + " has no content.");
            }
                
            PDFParser parser = new PDFParser(new ByteArrayInputStream(file.getContents()));
            parser.parse();

            pdfDocument = parser.getPDDocument();


            if (pdfDocument.isEncrypted()) {
                DecryptDocument decryptor = new DecryptDocument(pdfDocument);
                //Just try using the default password and move on
                decryptor.decryptDocument("");
            }

            //create a tmp output stream with the size of the content.
            // ByteArrayOutputStream out = new ByteArrayOutputStream();
            // OutputStreamWriter writer = new OutputStreamWriter(out);
            StringWriter writer = new StringWriter();
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.writeText(pdfDocument.getDocument(), writer);
            
            rawContent = writer.toString();
            writer.close(); 
                       
        } catch (CryptographyException exc) {
            throw new CmsIndexException("Decrypting resource " + resource.getRootPath() + " failed.", exc);
        } catch (InvalidPasswordException exc) {
            //they didn't suppply a password and the default of "" was wrong.
            throw new CmsIndexException("Resource " + resource.getRootPath() + " is password protected.", exc);
        } catch (IOException exc) {
            throw new CmsIndexException("Reading resource " + resource.getRootPath() + " failed.", exc);
        } finally {
            try {
                pdfDocument.close();
            } catch (Exception exc) {
                //
            }
        }
        
        return rawContent;
    }
    
    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#newInstance(org.opencms.search.CmsIndexResource, java.lang.String)
     */
    public Document newInstance (CmsIndexResource resource, String language) throws CmsException {
                   
        Document document = super.newInstance(resource, language);
        document.add(Field.Text(I_CmsDocumentFactory.DOC_CONTENT, getRawContent(resource, language)));
        
        return document;
    }
}
