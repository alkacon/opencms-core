/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/documents/Attic/CmsPdfDocument.java,v $
 * Date   : $Date: 2005/03/04 13:42:45 $
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
package org.opencms.search.documents;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.search.A_CmsIndexResource;
import org.opencms.search.CmsIndexException;

import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import org.pdfbox.encryption.DocumentEncryption;
import org.pdfbox.exceptions.CryptographyException;
import org.pdfbox.exceptions.InvalidPasswordException;
import org.pdfbox.pdfparser.PDFParser;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;

/**
 * Lucene document factory class to extract index data from a cms resource 
 * containing Adobe pdf data.<p>
 * 
 * @version $Revision: 1.11 $ $Date: 2005/03/04 13:42:45 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 */
public class CmsPdfDocument extends CmsVfsDocument {

    /**
     * Creates a new instance of this lucene document factory.<p>
     * 
     * @param name name of the documenttype
     */
    public CmsPdfDocument (String name) {
        super(name);
    }
    
    /**
     * Returns the raw text content of a given vfs resource containing Adobe pdf data.<p>
     * 
     * @see org.opencms.search.documents.CmsVfsDocument#getRawContent(org.opencms.file.CmsObject, org.opencms.search.A_CmsIndexResource, java.lang.String)
     */
    public String getRawContent(CmsObject cms, A_CmsIndexResource indexResource, String language) throws CmsException {

        CmsResource resource = (CmsResource)indexResource.getData();
        PDDocument pdfDocument = null;
        String rawContent = null;
        
        try {
            CmsFile file = cms.readFile(cms.getRequestContext().removeSiteRoot(resource.getRootPath()), CmsResourceFilter.IGNORE_EXPIRATION);
            if (!(file.getLength() > 0)) {
                throw new CmsIndexException("Resource " + resource.getRootPath() + " has no content.");
            }
                
            PDFParser parser = new PDFParser(new ByteArrayInputStream(file.getContents()));
            parser.parse();
            pdfDocument = parser.getPDDocument();
      
            if (pdfDocument.isEncrypted()) {
                DocumentEncryption decryptor = new DocumentEncryption(pdfDocument);
                // try using the default password
                decryptor.decryptDocument("");
            }
      

            // write content of PDF to String writer
            CharArrayWriter writer = new CharArrayWriter();
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.writeText(pdfDocument, writer);

            rawContent = writer.toString();
            writer.close();
      
                       
        } catch (CryptographyException exc) {
           throw new CmsIndexException("Decrypting resource " + resource.getRootPath() + " failed.", exc);
        } catch (InvalidPasswordException exc) {
            //they didn't suppply a password and the default of "" was wrong.
           throw new CmsIndexException("Resource " + resource.getRootPath() + " is password protected.", exc);
        } catch (IOException exc)  {          
            throw new CmsIndexException("Reading resource " + resource.getRootPath() + " failed: "+exc.getMessage(), exc);
        } finally {
            try {
                pdfDocument.close();
            } catch (Exception exc) {
                // ignore
            }
        }
        
        return rawContent;
    }
    
    /**
     * Generates a new lucene document instance from contents of the given resource.<p>
     * 
     * @see org.opencms.search.documents.I_CmsDocumentFactory#newInstance(org.opencms.file.CmsObject, org.opencms.search.A_CmsIndexResource, java.lang.String)
     */
    public Document newInstance (CmsObject cms, A_CmsIndexResource resource, String language) throws CmsException {
                   
        Document document = super.newInstance(cms, resource, language);
        document.add(Field.Text(I_CmsDocumentFactory.DOC_CONTENT, getRawContent(cms, resource, language)));
        
        return document;
    }
}
