/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/extractors/A_CmsTextExtractor.java,v $
 * Date   : $Date: 2005/03/23 19:08:22 $
 * Version: $Revision: 1.1 $
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

package org.opencms.search.extractors;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Base utility class that allows extraction of the indexable "plain" text from a given document format.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @since 5.7.2
 */
public abstract class A_CmsTextExtractor implements I_CmsTextExtractor {

    /**
     * @see org.opencms.search.extractors.I_CmsTextExtractor#extractText(byte[])
     */
    public I_CmsExtractionResult extractText(byte[] content) throws Exception {

        // encoding is null        
        return extractText(content, null);
    }

    /**
     * @see org.opencms.search.extractors.I_CmsTextExtractor#extractText(byte[], java.lang.String)
     */
    public I_CmsExtractionResult extractText(byte[] content, String encoding) throws Exception {

        // call stream based method of extraction
        return extractText(new ByteArrayInputStream(content), encoding);
    }

    /**
     * @see org.opencms.search.extractors.I_CmsTextExtractor#extractText(java.io.InputStream)
     */
    public I_CmsExtractionResult extractText(InputStream in) throws Exception {

        // encoding is null        
        return extractText(in, null);
    }

    /**
     * @see org.opencms.search.extractors.I_CmsTextExtractor#extractText(java.io.InputStream, java.lang.String)
     */
    public I_CmsExtractionResult extractText(InputStream in, String encoding) throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream(512);
        int c = 0;
        while (c >= 0) {
            try {
                c = in.read();
                out.write(c);
            } catch (IOException e) {
                // finished
                c = -1;
            }
        }

        // call byte array based method of extraction
        return extractText(out.toByteArray(), encoding);
    }

    /**
     * Removes "unwanted" control chars from the given content.<p>
     * 
     * @param content the content to remove the unwanted control chars from
     * 
     * @return the content with the unwanted control chars removed
     */
    protected String removeControlChars(String content) {

        char[] chars = content.toCharArray();
        StringBuffer result = new StringBuffer(chars.length);
        boolean wasUnwanted = false;
        for (int i = 0; i < chars.length; i++) {
            char ch = chars[i];

            int type = Character.getType(ch);
            switch (type) {

                // punctuation
                case Character.CURRENCY_SYMBOL:
                case Character.CONNECTOR_PUNCTUATION:
                case Character.FINAL_QUOTE_PUNCTUATION:
                case Character.INITIAL_QUOTE_PUNCTUATION:
                case Character.DASH_PUNCTUATION:
                case Character.START_PUNCTUATION:
                case Character.END_PUNCTUATION:
                case Character.OTHER_PUNCTUATION:
                // letters
                case Character.OTHER_LETTER:
                case Character.MODIFIER_LETTER:
                case Character.UPPERCASE_LETTER:
                case Character.TITLECASE_LETTER:
                case Character.LOWERCASE_LETTER:
                // digits
                case Character.DECIMAL_DIGIT_NUMBER:
                // spaces
                case Character.SPACE_SEPARATOR:
                    result.append(ch);
                    wasUnwanted = false;
                    break;

                // line separators
                case Character.LINE_SEPARATOR:
                    result.append('\n');
                    wasUnwanted = true;
                    break;

                // symbols
                case Character.MATH_SYMBOL:
                case Character.OTHER_SYMBOL:
                // other stuff:
                case Character.CONTROL:
                case Character.COMBINING_SPACING_MARK:
                case Character.ENCLOSING_MARK:
                case Character.FORMAT:
                case Character.LETTER_NUMBER:
                case Character.MODIFIER_SYMBOL:
                case Character.NON_SPACING_MARK:
                case Character.PARAGRAPH_SEPARATOR:
                case Character.PRIVATE_USE:
                case Character.SURROGATE:
                case Character.UNASSIGNED:
                case Character.OTHER_NUMBER:
                default:
                    if (!wasUnwanted) {
                        result.append('\n');
                        wasUnwanted = true;
                    }
            }
        }

        return result.toString();
    }
}