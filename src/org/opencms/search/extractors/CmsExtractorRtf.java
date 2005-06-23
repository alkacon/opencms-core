/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/extractors/CmsExtractorRtf.java,v $
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

package org.opencms.search.extractors;

import java.io.StringReader;
import java.util.regex.Pattern;

import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;

/**
 * Extracts the text form a RTF  document.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.6 $ 
 * 
 * @since 6.0.0 
 */
public final class CmsExtractorRtf extends A_CmsTextExtractor {

    /** Static member instance of the extractor. */
    private static final CmsExtractorRtf INSTANCE = new CmsExtractorRtf();

    /** Pattern used to remove {\*\ts...} RTF keywords, which cause NPE in Java 1.4. */
    private static final Pattern TS_REMOVE_PATTERN = Pattern.compile("\\{\\\\\\*\\\\ts[^\\}]*\\}", Pattern.DOTALL);

    /**
     * Hide the public constructor.<p> 
     */
    private CmsExtractorRtf() {

        // noop
    }

    /**
     * Returns an instance of this text extractor.<p> 
     * 
     * @return an instance of this text extractor
     */
    public static I_CmsTextExtractor getExtractor() {

        return INSTANCE;
    }

    /**
     * @see org.opencms.search.extractors.I_CmsTextExtractor#extractText(byte[], java.lang.String)
     */
    public I_CmsExtractionResult extractText(byte[] content, String encoding) throws Exception {

        // RTF always uses ASCII, so we don't need to care about the encoding
        String input = new String(content);

        // workaround to remove RTF keywords that cause a NPE in Java 1.4
        // this is a known bug in Java 1.4 that was fixed in 1.5
        // please see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5042109 for the official bug report
        input = TS_REMOVE_PATTERN.matcher(input).replaceAll("");

        // use build in RTF parser from Swing API
        RTFEditorKit rtfEditor = new RTFEditorKit();
        Document doc = rtfEditor.createDefaultDocument();
        rtfEditor.read(new StringReader(input), doc, 0);

        String result = doc.getText(0, doc.getLength());
        result = removeControlChars(result);

        return new CmsExtractionResult(result);
    }

    // this would be the better implementation if the bug mentioned above did not exist in 1.4 
    //  
    //    /**
    //     * @see org.opencms.search.extractors.I_CmsTextExtractor#extractText(java.io.InputStream, java.lang.String)
    //     */
    //    public I_CmsExtractionResult extractText(InputStream in, String encoding) throws Exception {
    //
    //        // use build in RTF parser from Swing API
    //        RTFEditorKit rtfEditor = new RTFEditorKit();
    //        Document doc = rtfEditor.createDefaultDocument();
    //        rtfEditor.read(in, doc, 0);
    //
    //        String result = doc.getText(0, doc.getLength());
    //        result = removeControlChars(result);
    //
    //        return new CmsExtractionResult(result);
    //    }

}