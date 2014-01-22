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

package org.opencms.search.solr.spellchecking;

import org.opencms.json.JSONObject;

/**
 * 
 */
public class CmsSpellcheckingRequest {

    /**
     * @param q string array containing the words to check. 
     * @param dictionary the language code of the dictionary to use.
     */
    CmsSpellcheckingRequest(String[] q, String dictionary) {

        this(q, dictionary, null);
    }

    /**
     * @param q string array containing the words to check. 
     * @param dictionary the language code of the dictionary to use. 
     * @param id the id of the spellcheck query. 
     */
    CmsSpellcheckingRequest(String[] q, String dictionary, String id) {

        this.m_wordsToCheck = q;
        this.m_dictionaryToUse = dictionary;
        this.m_id = id;
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
     * Returns the id.<p>
     *
     * @return the id
     */
    public String getId() {

        return m_id;
    }

    /**
     * Sets the id.<p>
     *
     * @param id the id to set
     */
    public void setId(String id) {

        m_id = id;
    }

    /**
     * Returns the dictionaryToUse.<p>
     *
     * @return the dictionaryToUse
     */
    public String getDictionaryToUse() {

        return m_dictionaryToUse;
    }

    /**
     * Sets the dictionaryToUse.<p>
     *
     * @param dictionaryToUse the dictionaryToUse to set
     */
    public void setDictionaryToUse(String dictionaryToUse) {

        m_dictionaryToUse = dictionaryToUse;
    }

    /**
     * Returns the wordsToCheck.<p>
     *
     * @return the wordsToCheck
     */
    public String[] getWordsToCheck() {

        return m_wordsToCheck;
    }

    /**
     * Sets the wordsToCheck.<p>
     *
     * @param wordsToCheck the wordsToCheck to set
     */
    public void setWordsToCheck(String[] wordsToCheck) {

        m_wordsToCheck = wordsToCheck;
    }

    /**
     * Returns the wordSuggestions.<p>
     *
     * @return the wordSuggestions
     */
    public JSONObject getWordSuggestions() {

        return m_wordSuggestions;
    }

    /**
     * Sets the wordSuggestions.<p>
     *
     * @param wordSuggestions the wordSuggestions to set
     */
    public void setWordSuggestions(JSONObject wordSuggestions) {

        m_wordSuggestions = wordSuggestions;
    }

    /** ID. */
    private String m_id;

    /** The dictionary to use.  */
    private String m_dictionaryToUse;

    /** The string array that contains the words that have to checked. */
    private String[] m_wordsToCheck;

    /** JSON object containing the computed suggestions for the checked words. */
    private JSONObject m_wordSuggestions;
}