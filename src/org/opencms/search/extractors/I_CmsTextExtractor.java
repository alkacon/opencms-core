/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/extractors/I_CmsTextExtractor.java,v $
 * Date   : $Date: 2005/06/23 11:11:28 $
 * Version: $Revision: 1.5 $
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

package org.opencms.search.extractors;

import java.io.InputStream;

/**
 * Allows extraction of the indexable "plain" text plus (optional) meta information from a given binary 
 * input document format.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.5 $ 
 * 
 * @since 6.0.0 
 */
public interface I_CmsTextExtractor {

    /**
     * Extracts the text and meta information from the given binary document.<p> 
     * 
     * The encoding of the input stream is either not required (the document type may have 
     * one common default encoding) or the extractor is able to divine the encoding 
     * from the provided binary array automatically.<p>
     * 
     * Delivers is the same result as calling <code>{@link #extractText(byte[], String)}</code>
     * when <code>String == null</code>.<p>
     * 
     * @param content the binary content of the document to extract the text from
     * @return the extracted text
     * 
     * @throws Exception if the text extration fails
     */
    I_CmsExtractionResult extractText(byte[] content) throws Exception;

    /**
     * Extracts the text and meta information from the given binary document, using the specified content encoding.<p> 
     * 
     * The encoding is a hint for the text extractor, if the value given is <code>null</code> then 
     * the text extractor should try to figure out the encoding itself.<p>
     * 
     * @param content the binary content of the document to extract the text from
     * @param encoding the encoding to use
     * 
     * @return the extracted text
     * 
     * @throws Exception if the text extration fails
     */
    I_CmsExtractionResult extractText(byte[] content, String encoding) throws Exception;

    /**
     * Extracts the text and meta information from the document on the input stream.<p> 
     * 
     * The encoding of the input stream is either not required (the document type may have 
     * one common default encoding) or the extractor is able to divine the encoding 
     * from the provided input stream automatically.<p>
     * 
     * Delivers is the same result as calling <code>{@link #extractText(InputStream, String)}</code>
     * when <code>String == null</code>.<p>
     * 
     * @param in the input stream for the document to extract the text from
     * @return the extracted text and meta information 
     * 
     * @throws Exception if the text extration fails
     */
    I_CmsExtractionResult extractText(InputStream in) throws Exception;

    /**
     * Extracts the text and meta information from the document on the input stream, using the specified content encoding.<p> 
     * 
     * The encoding is a hint for the text extractor, if the value given is <code>null</code> then 
     * the text extractor should try to figure out the encoding itself.<p>
     * 
     * @param in the input stream for the document to extract the text from
     * @param encoding the encoding to use
     * 
     * @return the extracted text and meta information 
     * 
     * @throws Exception if the text extration fails
     */
    I_CmsExtractionResult extractText(InputStream in, String encoding) throws Exception;
}