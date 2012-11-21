/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.search.galleries;

import org.opencms.search.CmsSearchIndex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WordlistLoader;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;

/**
 * Special analyzer for multiple languages, used in the OpenCms gallery search index.<p>
 * 
 * The gallery search is done in one single index that may contain multiple languages.<p>
 * 
 * According to the Lucene JavaDocs (3.0 version), the Lucene {@link org.apache.lucene.analysis.standard.StandardAnalyzer} is already using
 * "a good tokenizer for most European-language documents". The only caveat is that a 
 * list of English only stop words is used.<p>
 * 
 * This extended analyzer used a compound list of stop words compiled from the following languages:<ul>
 * <li>English
 * <li>German
 * <li>Spanish
 * <li>Italian
 * <li>French
 * <li>Portugese
 * <li>Danish
 * <li>Dutch
 * <li>Catalan
 * <li>Czech
 * </ul>
 * 
 * @since 8.0.0 
 */
public class CmsGallerySearchAnalyzer extends StopwordAnalyzerBase {

    /** Default maximum allowed token length. */
    public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;

    /**
     * Constructor with version parameter.<p>
     * 
     * @param version the Lucene standard analyzer version to match
      * @throws IOException 
     */
    public CmsGallerySearchAnalyzer(Version version)
    throws IOException {

        // initialize superclass
        super(version, WordlistLoader.getWordSet(
            new BufferedReader(new InputStreamReader(
                CmsGallerySearchAnalyzer.class.getResourceAsStream("stopwords_multilanguage.txt"))),
            "#",
            CmsSearchIndex.LUCENE_VERSION));
    }

    /**
     * @see org.apache.lucene.analysis.ReusableAnalyzerBase#createComponents(java.lang.String, java.io.Reader)
     * 
     * This is take from the Lucene StandardAnalyzer, which is final since 3.1
     */
    @Override
    protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {

        final StandardTokenizer src = new StandardTokenizer(matchVersion, reader);
        src.setMaxTokenLength(DEFAULT_MAX_TOKEN_LENGTH);
        TokenStream tok = new StandardFilter(matchVersion, src);
        tok = new LowerCaseFilter(matchVersion, tok);
        tok = new StopFilter(matchVersion, tok, stopwords);
        return new TokenStreamComponents(src, tok) {

            @Override
            protected boolean reset(final Reader r) throws IOException {

                src.setMaxTokenLength(DEFAULT_MAX_TOKEN_LENGTH);
                return super.reset(r);
            }
        };
    }
}
