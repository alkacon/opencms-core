/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/documents/I_CmsTermHighlighter.java,v $
 * Date   : $Date: 2011/03/23 14:52:04 $
 * Version: $Revision: 1.12 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.search.documents;

import org.opencms.search.CmsSearchIndex;
import org.opencms.search.CmsSearchParameters;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;

/**
 * Highlights arbitrary terms, used for generation of search excerpts.<p>
 *
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.12 $ 
 * 
 * @since 6.0.0 
 */
public interface I_CmsTermHighlighter {

    /**
     * Returns an excerpt of the given document related based on the given index and query.<p>
     * 
     * @param doc the content Lucene document to generate the excerpt for
     * @param index the index that has been searched
     * @param params the current search parameters
     * @param query the search query
     * @param analyzer the analyzer used 
     * 
     * @return an excerpt of the content
     * 
     * @throws IOException if something goes wrong
     */
    String getExcerpt(Document doc, CmsSearchIndex index, CmsSearchParameters params, Query query, Analyzer analyzer)
    throws IOException;
}