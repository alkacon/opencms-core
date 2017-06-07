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

package org.opencms.search.solr.spellchecking;

import org.opencms.json.JSONObject;

/**
 * Helper class that represents a spellchecking request.
 */
class CmsSpellcheckingRequest {

    /** The Id of the request sent by tinyMce. */
    String m_id;

    /** The dictionary to use.  */
    String m_dictionaryToUse;

    /** The string array that contains the words that have to checked. */
    String[] m_wordsToCheck;

    /** JSON object containing the computed suggestions for the checked words. */
    JSONObject m_wordSuggestions;

    /**
     * Constructor.
     */
    CmsSpellcheckingRequest() {

    }

    /**
     * Constructor.
     *
     * @param q the string that contains the words that have to checked.
     * @param dictionary the dictionary to use.
     */
    CmsSpellcheckingRequest(String[] q, String dictionary) {

        this(q, dictionary, null);
    }

    /**
     * Constructor.
     *
     * @param q the string that contains the words that have to checked.
     * @param dictionary the dictionary to use.
     * @param id the Id of the request sent by tinyMce.
     */
    CmsSpellcheckingRequest(String[] q, String dictionary, String id) {

        m_wordsToCheck = q;
        m_dictionaryToUse = dictionary;
        m_id = id;
    }

    /**
     * Returns whether this class has been correctly initialized.
     *
     * @return true if this class has been correctly initialized, otherwise false.
     */
    public boolean isInitialized() {

        return (null != m_dictionaryToUse) && (null != m_wordsToCheck);
    }

    /**
     * Sets the words to check.
     * @param q Array containing all words to check.
     */
    public void setWordsToCheck(String[] q) {

        m_wordsToCheck = q;
    }
}
