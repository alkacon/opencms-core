/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.search;

import org.opencms.search.fields.CmsSearchField;

import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.SmallFloat;

/**
 * Reduces the importance of the <code>{@link #computeNorm(FieldInvertState)}</code> factor
 * for the <code>{@link org.opencms.search.fields.CmsLuceneField#FIELD_CONTENT}</code> field, while
 * keeping the Lucene default for all other fields.<p>
 *
 * This implementation was added since apparently the default length norm is heavily biased
 * for small documents. In the default, even if a term is found in 2 documents the same number of
 * times, the smaller document (containing less terms) will have a score easily 3x as high as
 * the longer document. Using this implementation the importance of the term number is reduced.<p>
 *
 * Inspired by Chuck Williams WikipediaSimilarity.<p>
 *
 * @since 6.0.0
 */
public class CmsSearchSimilarity extends Similarity {

    /** Logarithm base 10 used as factor in the score calculations. */
    private static final double LOG10 = Math.log(10.0);

    /** Similarity implementation the CmsSearchSimilarity is based on. */
    private final BM25Similarity m_bm25Sim = new BM25Similarity();

    /**
     * Creates a new instance of the OpenCms search similarity.<p>
     */
    public CmsSearchSimilarity() {

    }

    /**
     * Special implementation for "compute norm" to reduce the significance of this factor
     * for the <code>{@link org.opencms.search.fields.CmsLuceneField#FIELD_CONTENT}</code> field, while
     * keeping the Lucene default for all other fields.<p>
     */
    @Override
    public final long computeNorm(FieldInvertState state) {

        final int numTerms = m_bm25Sim.getDiscountOverlaps()
        ? state.getLength() - state.getNumOverlap()
        : state.getLength();

        if (state.getIndexCreatedVersionMajor() >= 7) {
            return SmallFloat.intToByte4(numTerms);
        } else {
            return SmallFloat.floatToByte315(lengthNorm(state, numTerms));
        }
    }

    /**
     * Returns true iff overlap tokens are discounted from the document's length.
     *
     * @return true iff overlap tokens are discounted from the document's length.
     *
     * @see #setDiscountOverlaps(boolean)
     */
    public boolean getDiscountOverlaps() {

        return m_bm25Sim.getDiscountOverlaps();
    }

    /**
     * @see org.apache.lucene.search.similarities.Similarity#scorer(float, org.apache.lucene.search.CollectionStatistics, org.apache.lucene.search.TermStatistics[])
     */
    @Override
    public SimScorer scorer(float boost, CollectionStatistics collectionStats, TermStatistics... termStats) {

        return m_bm25Sim.scorer(boost, collectionStats, termStats);
    }

    /**
     * Special implementation for "compute norm" to reduce the significance of this factor
     * for the <code>{@link org.opencms.search.fields.CmsLuceneField#FIELD_CONTENT}</code> field, while
     * keeping the Lucene default for all other fields.<p>
     *
     * @param state  field invert state
     * @param numTerms number of terms
     *
     * @return the norm as specifically created for OpenCms.
     *
     */
    private float lengthNorm(FieldInvertState state, int numTerms) {

        if (state.getName().equals(CmsSearchField.FIELD_CONTENT)) {
            numTerms = state.getLength() - state.getNumOverlap();
            // special length norm for content
            return (float)(3.0 / (Math.log(1000 + numTerms) / LOG10));
        }
        // all other fields use the default Lucene implementation
        return (float)(1 / Math.sqrt(numTerms));
    }
}