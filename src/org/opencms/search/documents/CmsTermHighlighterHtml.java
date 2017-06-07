/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the m_terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

import org.opencms.i18n.CmsEncoder;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.CmsSearchParameters;
import org.opencms.search.fields.CmsLuceneFieldConfiguration;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryTermScorer;

/**
 * Default highlighter implementation used for generation of search excerpts.<p>
 *
 * @since 6.0.0
 */
public class CmsTermHighlighterHtml implements I_CmsTermHighlighter {

    /** Separator for the search excerpt fragments. */
    private static final String EXCERPT_FRAGMENT_SEPARATOR = " ... ";

    /** Fragments required in excerpt. */
    private static final int EXCERPT_REQUIRED_FRAGMENTS = 5;

    /**
     * @see org.opencms.search.documents.I_CmsTermHighlighter#getExcerpt(org.apache.lucene.document.Document, org.opencms.search.CmsSearchIndex, org.opencms.search.CmsSearchParameters, org.apache.lucene.search.Query, org.apache.lucene.analysis.Analyzer)
     */
    public String getExcerpt(
        Document doc,
        CmsSearchIndex index,
        CmsSearchParameters params,
        Query query,
        Analyzer analyzer) throws IOException, InvalidTokenOffsetsException {

        if ((doc == null) || (index == null) || (params == null) || (analyzer == null) || (query == null)) {
            return null;
        }
        if (!(index.getFieldConfiguration() instanceof CmsLuceneFieldConfiguration)) {
            // also return null if the field configuration is not a lucene field configuration
            return null;
        }
        Highlighter highlighter = null;
        CmsLuceneFieldConfiguration conf = (CmsLuceneFieldConfiguration)index.getFieldConfiguration();
        Iterator<String> excerptFieldNames = conf.getExcerptFieldNames().iterator();
        StringBuffer excerptBuffer = new StringBuffer();
        while (excerptFieldNames.hasNext()) {
            String fieldName = excerptFieldNames.next();
            boolean createExcerpt = !params.isExcerptOnlySearchedFields() || params.getFields().contains(fieldName);
            if (createExcerpt && (doc.getField(fieldName) != null)) {
                // only generate field excerpt if the field is available in the document
                String text = doc.getField(fieldName).stringValue();
                // make sure all XML in the text is escaped, otherwise excerpt HTML output may be garbled
                text = CmsEncoder.escapeXml(text);

                TokenStream stream = analyzer.tokenStream(fieldName, new StringReader(text));

                if (params.isExcerptOnlySearchedFields()) {
                    // highlight the search query only in the matching fields
                    highlighter = new Highlighter(new QueryTermScorer(query, fieldName));
                } else {
                    // highlight search query in all fields
                    if (highlighter == null) {
                        highlighter = new Highlighter(new QueryTermScorer(query));
                    }
                }
                String fragment = highlighter.getBestFragments(
                    stream,
                    text,
                    EXCERPT_REQUIRED_FRAGMENTS,
                    EXCERPT_FRAGMENT_SEPARATOR);

                // kill all unwanted chars in the excerpt
                fragment = fragment.replace('\t', ' ');
                fragment = fragment.replace('\n', ' ');
                fragment = fragment.replace('\r', ' ');
                fragment = fragment.replace('\f', ' ');

                if (excerptBuffer.length() > 0) {
                    // this is not the first fragment
                    excerptBuffer.append(EXCERPT_FRAGMENT_SEPARATOR);
                }
                excerptBuffer.append(fragment);
            }
        }

        String result = null;
        if (excerptBuffer.length() > 0) {
            result = excerptBuffer.toString();
        }

        int maxLength = OpenCms.getSearchManager().getMaxExcerptLength();
        if ((result != null) && (result.length() > maxLength)) {
            result = result.substring(0, maxLength);
        }

        return result;
    }
}