/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/galleries/CmsGallerySearchAnalyzer.java,v $
 * Date   : $Date: 2010/01/14 15:30:14 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.lucene.analysis.WordlistLoader;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;

/**
 * Special analyzer for multiple languages, used in the OpenCms ADE gallery search.<p>
 * 
 * The gallery search is done in one single index that may contain multiple languages.<p>
 * 
 * According to the Lucene JavaDocs (3.0 version), the Lucene {@link StandardAnalyzer} is already using
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
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0 
 */
public class CmsGallerySearchAnalyzer extends StandardAnalyzer {

    /**
     * Constructor with version parameter.<p>
     * 
     * @param matchVersion the Lucene standard analyzer version to match
     * @throws URISyntaxException 
     * @throws IOException 
     */
    public CmsGallerySearchAnalyzer(Version matchVersion)
    throws URISyntaxException, IOException {

        // initialize superclass
        super(matchVersion, WordlistLoader.getWordSet(new File(
            CmsGallerySearchAnalyzer.class.getClassLoader().getResource(
                "org/opencms/search/galleries/stopwords_multilanguage.txt").toURI()), "#"));
    }
}
