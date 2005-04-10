/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/extractors/CmsExtractorMsWord.java,v $
 * Date   : $Date: 2005/04/10 11:00:14 $
 * Version: $Revision: 1.3 $
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

import java.io.InputStream;
import java.util.Map;

import org.apache.poi.poifs.eventfilesystem.POIFSReader;

import org.textmining.text.extraction.WordExtractor;

/**
 * Extracts the text form an MS Word document.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @since 5.7.2
 */
public final class CmsExtractorMsWord extends A_CmsTextExtractorMsOfficeBase {

    /** Static member instance of the extractor. */
    private static final CmsExtractorMsWord INSTANCE = new CmsExtractorMsWord();

    /**
     * Hide the public constructor.<p> 
     */
    private CmsExtractorMsWord() {

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
     * @see org.opencms.search.extractors.I_CmsTextExtractor#extractText(java.io.InputStream, java.lang.String)
     */
    public I_CmsExtractionResult extractText(InputStream in, String encoding) throws Exception {

        // first extract the text using the text actraction libary
        WordExtractor wordExtractor = new WordExtractor();
        String result = wordExtractor.extractText(getStreamCopy(in));
        result = removeControlChars(result);

        // now extract the meta information using POI 
        POIFSReader reader = new POIFSReader();
        reader.registerListener(this);
        reader.read(getStreamCopy(in));
        Map metaInfo = extractMetaInformation();

        // return the final result
        return new CmsExtractionResult(result, metaInfo);
    }

}