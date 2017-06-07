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

package org.opencms.jsp.search.config;

/**
 * Search configuration for highlighting options.
 */
public class CmsSearchConfigurationHighlighting implements I_CmsSearchConfigurationHighlighting {

    /** The fields that should be used for highlighting. */
    private final String m_highlightField;
    /** The number of snippets to return. */
    private final Integer m_snippetCount;
    /** The size of a snippet (in letters). */
    private final Integer m_fragSize;
    /** A field that should be displayed if no highlighting snippet is found. */
    private final String m_alternateField;
    /** The maximal length of the snippet shown from the alternative field (in letters). */
    private final Integer m_maxAlternateFieldLength;
    /** The String added in front of the highlighted part. */
    private final String m_pre;
    /** The String added behind the highlighted part. */
    private final String m_post;
    /** The formatter used for highlighting. */
    private final String m_formatter;
    /** The fragmenter used for highlighting. */
    private final String m_fragmenter;
    /** Flag, indicating if fast vector highlighting should be used. */
    private final Boolean m_fastVectorHighlighting;

    /** The constructor setting all configuration values.
     * @param field The fields that should be used for highlighting. (Solr: hl.fl)
     * @param snippetCount The number of snippets to return. (Solr: hl.snippets)
     * @param fragSize The size of a snippet (in letters). (Solr: hl.fragsize)
     * @param alternateField A field that should be displayed if no highlighting snippet is found. (Solr: hl.alternateField)
     * @param maxAlternateFieldLength The maximal length of the snippet shown from the alternative field (in letters). (Solr: hl.maxAlternateFieldLength)
     * @param pre The String added in front of the highlighted part. (Solr: hl.simple.pre)
     * @param post The String added behind the highlighted part. (Solr: hl.simple.post)
     * @param formatter The formatter used for highlighting. (Solr: hl.formatter)
     * @param fragmenter The fragmenter used for highlighting. (Solr: hl.fragmenter)
     * @param useFastVectorHighlighting Flag, indicating if fast vector highlighting should be used. (Solr: hl.useFastVectorHighlighting)
     */
    public CmsSearchConfigurationHighlighting(
        final String field,
        final Integer snippetCount,
        final Integer fragSize,
        final String alternateField,
        final Integer maxAlternateFieldLength,
        final String pre,
        final String post,
        final String formatter,
        final String fragmenter,
        final Boolean useFastVectorHighlighting) {

        m_highlightField = field;
        m_snippetCount = snippetCount;
        m_fragSize = fragSize;
        m_alternateField = alternateField;
        m_maxAlternateFieldLength = maxAlternateFieldLength;
        m_pre = pre;
        m_post = post;
        m_formatter = formatter;
        m_fragmenter = fragmenter;
        m_fastVectorHighlighting = useFastVectorHighlighting;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationHighlighting#getAlternateHighlightField()
     */
    @Override
    public String getAlternateHighlightField() {

        return m_alternateField;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationHighlighting#getFormatter()
     */
    @Override
    public String getFormatter() {

        return m_formatter;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationHighlighting#getFragmenter()
     */
    @Override
    public String getFragmenter() {

        return m_fragmenter;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationHighlighting#getFragSize()
     */
    @Override
    public Integer getFragSize() {

        return m_fragSize;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationHighlighting#getHightlightField()
     */
    @Override
    public String getHightlightField() {

        return m_highlightField;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationHighlighting#getMaxAlternateHighlightFieldLength()
     */
    @Override
    public Integer getMaxAlternateHighlightFieldLength() {

        return m_maxAlternateFieldLength;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationHighlighting#getSimplePost()
     */
    @Override
    public String getSimplePost() {

        return m_post;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationHighlighting#getSimplePre()
     */
    @Override
    public String getSimplePre() {

        return m_pre;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationHighlighting#getSnippetsCount()
     */
    @Override
    public Integer getSnippetsCount() {

        return m_snippetCount;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationHighlighting#getUseFastVectorHighlighting()
     */
    @Override
    public Boolean getUseFastVectorHighlighting() {

        return m_fastVectorHighlighting;
    }

}
