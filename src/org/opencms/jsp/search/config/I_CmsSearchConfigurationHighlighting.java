/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.jsp.search.config;

import java.util.Map;

/** The interface each highlighting configuration must implement. */
public interface I_CmsSearchConfigurationHighlighting {

    /** Returns the index field whose content should be used if no highlighting snippet is available.
     * @return The index field whose content should be used if no highlighting snippet is available. (Solr: hl.alternateField)
     */
    @Deprecated
    String getAlternateHighlightField();

    /** Returns the formatter that should be used for highlighting.
     * @return The formatter that should be used for highlighting. (Solr: hl.formatter)
     */
    @Deprecated
    String getFormatter();

    /** Returns the fragmenter that should be used for highlighting.
     * @return The fragmenter that should be used for highlighting. (Solr: hl.fragmenter)
     */
    @Deprecated
    String getFragmenter();

    /** Returns the maximal size a highlighted snippet should have.
     * @return The maximal size a highlighted snippet should have. (Solr: hl.fragsize)
     */
    @Deprecated
    Integer getFragSize();

    /** Returns the index field that should be used for highlighting.
     * @return The index field that should be used for highlighting.
     */
    @Deprecated
    String getHightlightField();

    /** Returns the maximal length of the snippet that should be shown from the alternative field, if no highlighting snippet was found.
     * @return The maximal length of the snippet that should be shown from the alternative field, if no highlighting snippet was found. (Solr: hl.maxAlternateFieldLength)
     */
    @Deprecated
    Integer getMaxAlternateHighlightFieldLength();

    /**
     * Returns the value set for the configuration parameter.
     * @param paramName the parameter name without the preceeding 'hl.'.
     * @return the value that is set, or <code>null</code> if the value is not set explicitly.
     */
    Map<String, String> getParams();

    /** Returns the String that should be inserted directly after the term that should be highlighted.
     * @return The String that should be inserted directly after the term that should be highlighted. (Solr: hl.simple.post)
     */
    @Deprecated
    String getSimplePost();

    /** Returns the String that should be inserted directly in front of the term that should be highlighted.
     * @return The String that should be inserted directly in front the term that should be highlighted. (Solr: hl.simple.pre)
     */
    @Deprecated
    String getSimplePre();

    /** Returns the number of highlighted snippets that should be returned.
     * @return The number of highlighted snippets that should be returned. (Solr: hl.snippets)
     */
    @Deprecated
    Integer getSnippetsCount();

    /** Returns the flag, that indicates if fast vector highlighting should be used.
     * @return The flag, that indicates if fast vector highlighting should be used. (Solr: hl.useFastVectorHighlighting)
     */
    @Deprecated
    Boolean getUseFastVectorHighlighting();

}
