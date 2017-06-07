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

import org.opencms.file.CmsObject;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsLog;
import org.opencms.security.CmsPermissionViolationException;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Suggestion;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrCore;

/**
 * CmsSolrSpellchecker is used to perform spellchecking in OpenCms by using Solr. The JSON-formatted result of the
 * spellchecking operation contains suggestions for misspelled words and is compatible with the expected structure
 * of the tinyMCE editor.
 */
public final class CmsSolrSpellchecker {

    /** The spellcheck core name. */
    public static final String SPELLCHECKER_INDEX_CORE = "spellcheck";

    /** Logging facility for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSolrSpellchecker.class);

    /** The singleton instance of this class. */
    private static CmsSolrSpellchecker instance;

    /** Constant, defining the default spellchecker language. */
    private static final String LANG_DEFAULT = "en";

    /** Constant, defining the JSON 'id'-field key. */
    private static final String JSON_ID = "id";

    /** Constant, defining the JSON 'lang'-field key. */
    private static final String JSON_LANG = "lang";

    /** Constant, defining the JSON 'error'-field key. */
    private static final String JSON_ERROR = "error";

    /** Constant, defining the JSON 'words'-field key. */
    private static final String JSON_WORDS = "words";

    /** Constant, defining the JSON 'params'-field key. */
    private static final String JSON_PARAMS = "params";

    /** Constant, defining the JSON 'result'-field key. */
    private static final String JSON_RESULT = "result";

    /** Constant, defining the parameter name containing the words. */
    private static final String HTTP_PARAMETER_WORDS = "words";

    /** Constant, defining the parameter name containing the language. */
    private static final String HTTP_PARAMETER_LANG = "lang";

    /** Constant, defining the parameter name used to force rebuild the index. */
    private static final String HTTP_PARAMTER_REBUILD = "rebuild";

    /** Constant, defining the parameter name used to check and rebuild the index. */
    private static final String HTTP_PARAMETER_CHECKREBUILD = "check";

    /** The SolrCore object. */
    private SolrCore m_core;

    /** The Solr CoreContainer object. */
    private CoreContainer m_coreContainer;

    /** The SolrClient object. */
    private SolrClient m_solrClient;

    /**
     * Private constructor due to usage of the Singleton pattern.
     *
     * @param container Solr CoreContainer container object.
     * @param core The Solr Core object.
     */
    private CmsSolrSpellchecker(CoreContainer container, SolrCore core) {

        if ((null == container) || (null == core)) {
            throw new IllegalArgumentException();
        }

        m_core = core;
        m_coreContainer = container;
        m_solrClient = new EmbeddedSolrServer(m_coreContainer, m_core.getName());
    }

    /**
     * Return an instance of this class.
     *
     * @return instance of CmsSolrSpellchecker
     */
    public static CmsSolrSpellchecker getInstance() {

        return instance;
    }

    /**
     * Return an instance of this class.
     *
     * @param container Solr CoreContainer container object in order to create a server object.
     * @param core The Solr Core object in order to create a server object.
     * @return instance of CmsSolrSpellchecker
     */
    public static CmsSolrSpellchecker getInstance(CoreContainer container, SolrCore core) {

        if (null == instance) {
            synchronized (CmsSolrSpellchecker.class) {
                if (null == instance) {
                    instance = new CmsSolrSpellchecker(container, core);
                }
            }
        }

        return instance;
    }

    /**
     * Performs spellchecking using Solr and returns the spellchecking results using JSON.
     *
     * @param res The HttpServletResponse object.
     * @param servletRequest The ServletRequest object.
     * @param cms The CmsObject object.
     *
     * @throws CmsPermissionViolationException in case of the anonymous guest user
     * @throws IOException if writing the response fails
     */
    public void getSpellcheckingResult(
        final HttpServletResponse res,
        final ServletRequest servletRequest,
        final CmsObject cms)
    throws CmsPermissionViolationException, IOException {

        // Perform a permission check
        performPermissionCheck(cms);

        // Set the appropriate response headers
        setResponeHeaders(res);

        // Figure out whether a JSON or HTTP request has been sent
        CmsSpellcheckingRequest cmsSpellcheckingRequest = null;
        try {
            String requestBody = getRequestBody(servletRequest);
            final JSONObject jsonRequest = new JSONObject(requestBody);
            cmsSpellcheckingRequest = parseJsonRequest(jsonRequest);
        } catch (Exception e) {
            LOG.debug(e.getMessage(), e);
            cmsSpellcheckingRequest = parseHttpRequest(servletRequest, cms);
        }

        if ((null != cmsSpellcheckingRequest) && cmsSpellcheckingRequest.isInitialized()) {
            // Perform the actual spellchecking
            final SpellCheckResponse spellCheckResponse = performSpellcheckQuery(cmsSpellcheckingRequest);

            /*
             * The field spellCheckResponse is null when exactly one correctly spelled word is passed to the spellchecker.
             * In this case it's safe to return an empty JSON formatted map, as the passed word is correct. Otherwise,
             * convert the spellchecker response into a new JSON formatted map.
             */
            if (null == spellCheckResponse) {
                cmsSpellcheckingRequest.m_wordSuggestions = new JSONObject();
            } else {
                cmsSpellcheckingRequest.m_wordSuggestions = getConvertedResponseAsJson(spellCheckResponse);
            }
        }

        // Send response back to the client
        sendResponse(res, cmsSpellcheckingRequest);
    }

    /**
     * Parses and adds dictionaries to the Solr index.
     *
     * @param cms The OpenCms object.
     */
    void parseAndAddDictionaries(CmsObject cms) {

        CmsSpellcheckDictionaryIndexer.parseAndAddZippedDictionaries(m_solrClient, cms);
        CmsSpellcheckDictionaryIndexer.parseAndAddDictionaries(m_solrClient, cms);
    }

    /**
     * Converts the suggestions from the Solrj format to JSON format.
     *
     * @param response The SpellCheckResponse object containing the spellcheck results.
     * @return The spellcheck suggestions as JSON object or null if something goes wrong.
     */
    private JSONObject getConvertedResponseAsJson(SpellCheckResponse response) {

        if (null == response) {
            return null;
        }

        final JSONObject suggestions = new JSONObject();
        final Map<String, Suggestion> solrSuggestions = response.getSuggestionMap();

        // Add suggestions to the response
        for (final String key : solrSuggestions.keySet()) {

            // Indicator to ignore words that are erroneously marked as misspelled.
            boolean ignoreWord = false;

            // Suggestions that are in the form "Xxxx" -> "xxxx" should be ignored.
            if (Character.isUpperCase(key.codePointAt(0))) {
                final String lowercaseKey = key.toLowerCase();
                // If the suggestion map doesn't contain the lowercased word, ignore this entry.
                if (!solrSuggestions.containsKey(lowercaseKey)) {
                    ignoreWord = true;
                }
            }

            if (!ignoreWord) {
                try {
                    // Get suggestions as List
                    final List<String> l = solrSuggestions.get(key).getAlternatives();
                    suggestions.put(key, l);
                } catch (JSONException e) {
                    LOG.debug("Exception while converting Solr spellcheckresponse to JSON. ", e);
                }
            }
        }

        return suggestions;
    }

    /**
     * Returns the result of the performed spellcheck formatted in JSON.
     *
     * @param request The CmsSpellcheckingRequest.
     * @return JSONObject that contains the result of the performed spellcheck.
     */
    private JSONObject getJsonFormattedSpellcheckResult(CmsSpellcheckingRequest request) {

        final JSONObject response = new JSONObject();

        try {
            if (null != request.m_id) {
                response.put(JSON_ID, request.m_id);
            }

            response.put(JSON_RESULT, request.m_wordSuggestions);

        } catch (Exception e) {
            try {
                response.put(JSON_ERROR, true);
                LOG.debug("Error while assembling spellcheck response in JSON format.", e);
            } catch (JSONException ex) {
                LOG.debug("Error while assembling spellcheck response in JSON format.", ex);
            }
        }

        return response;
    }

    /**
     * Returns the body of the request. This method is used to read posted JSON data.
     *
     * @param request The request.
     *
     * @return String representation of the request's body.
     *
     * @throws IOException in case reading the request fails
     */
    private String getRequestBody(ServletRequest request) throws IOException {

        final StringBuilder sb = new StringBuilder();

        String line = request.getReader().readLine();
        while (null != line) {
            sb.append(line);
            line = request.getReader().readLine();
        }

        return sb.toString();
    }

    /**
     * Parse parameters from this request using HTTP.
     *
     * @param req The ServletRequest containing all request parameters.
     * @param cms The OpenCms object.
     * @return CmsSpellcheckingRequest object that contains parsed parameters.
     */
    private CmsSpellcheckingRequest parseHttpRequest(final ServletRequest req, final CmsObject cms) {

        if ((null != cms) && !cms.getRequestContext().getCurrentUser().isGuestUser()) {
            if (null != req.getParameter(HTTP_PARAMETER_CHECKREBUILD)) {
                if (CmsSpellcheckDictionaryIndexer.updatingIndexNecessesary(cms)) {
                    parseAndAddDictionaries(cms);
                }
            }

            if (null != req.getParameter(HTTP_PARAMTER_REBUILD)) {
                parseAndAddDictionaries(cms);
            }
        }

        final String q = req.getParameter(HTTP_PARAMETER_WORDS);

        if (null == q) {
            LOG.debug("Invalid HTTP request: No parameter \"" + HTTP_PARAMETER_WORDS + "\" defined. ");
            return null;
        }

        final StringTokenizer st = new StringTokenizer(q);
        final List<String> wordsToCheck = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            final String word = st.nextToken();
            wordsToCheck.add(word);

            if (Character.isUpperCase(word.codePointAt(0))) {
                wordsToCheck.add(word.toLowerCase());
            }
        }

        final String[] w = wordsToCheck.toArray(new String[wordsToCheck.size()]);
        final String dict = req.getParameter(HTTP_PARAMETER_LANG) == null
        ? LANG_DEFAULT
        : req.getParameter(HTTP_PARAMETER_LANG);

        return new CmsSpellcheckingRequest(w, dict);
    }

    /**
     * Parse JSON parameters from this request.
     *
     * @param jsonRequest The request in the JSON format.
     * @return CmsSpellcheckingRequest object that contains parsed parameters or null, if JSON input is not well
     * defined.
     */
    private CmsSpellcheckingRequest parseJsonRequest(JSONObject jsonRequest) {

        final String id = jsonRequest.optString(JSON_ID);

        final JSONObject params = jsonRequest.optJSONObject(JSON_PARAMS);

        if (null == params) {
            LOG.debug("Invalid JSON request: No field \"params\" defined. ");
            return null;
        }
        final JSONArray words = params.optJSONArray(JSON_WORDS);
        final String lang = params.optString(JSON_LANG, LANG_DEFAULT);
        if (null == words) {
            LOG.debug("Invalid JSON request: No field \"words\" defined. ");
            return null;
        }

        // Convert JSON array to array of type String
        final List<String> wordsToCheck = new LinkedList<String>();
        for (int i = 0; i < words.length(); i++) {
            final String word = words.opt(i).toString();
            wordsToCheck.add(word);

            if (Character.isUpperCase(word.codePointAt(0))) {
                wordsToCheck.add(word.toLowerCase());
            }
        }

        return new CmsSpellcheckingRequest(wordsToCheck.toArray(new String[wordsToCheck.size()]), lang, id);
    }

    /**
     * Perform a security check against OpenCms.
     *
     * @param cms The OpenCms object.
     *
     * @throws CmsPermissionViolationException in case of the anonymous guest user
     */
    private void performPermissionCheck(CmsObject cms) throws CmsPermissionViolationException {

        if (cms.getRequestContext().getCurrentUser().isGuestUser()) {
            throw new CmsPermissionViolationException(null);
        }
    }

    /**
     * Performs the actual spell check query using Solr.
     *
     * @param request the spell check request
     *
     * @return Results of the Solr spell check of type SpellCheckResponse or null if something goes wrong.
     */
    private SpellCheckResponse performSpellcheckQuery(CmsSpellcheckingRequest request) {

        if ((null == request) || !request.isInitialized()) {
            return null;
        }

        final String[] wordsToCheck = request.m_wordsToCheck;

        final ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("spellcheck", "true");
        params.set("spellcheck.dictionary", request.m_dictionaryToUse);
        params.set("spellcheck.extendedResults", "true");

        // Build one string from array of words and use it as query.
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < wordsToCheck.length; i++) {
            builder.append(wordsToCheck[i] + " ");
        }

        params.set("spellcheck.q", builder.toString());

        final SolrQuery query = new SolrQuery();
        query.setRequestHandler("/spell");
        query.add(params);

        try {
            QueryResponse qres = m_solrClient.query(query);
            return qres.getSpellCheckResponse();
        } catch (Exception e) {
            LOG.debug("Exception while performing spellcheck query...", e);
        }

        return null;
    }

    /**
     * Sends the JSON-formatted spellchecking results to the client.
     *
     * @param res The HttpServletResponse object.
     * @param request The spellchecking request object.
     *
     * @throws IOException in case writing the response fails
     */
    private void sendResponse(final HttpServletResponse res, final CmsSpellcheckingRequest request) throws IOException {

        final PrintWriter pw = res.getWriter();
        final JSONObject response = getJsonFormattedSpellcheckResult(request);
        pw.println(response.toString());
        pw.close();
    }

    /**
     * Sets the appropriate headers to response of this request.
     *
     * @param response The HttpServletResponse response object.
     */
    private void setResponeHeaders(HttpServletResponse response) {

        response.setHeader("Cache-Control", "no-store, no-cache");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", System.currentTimeMillis());
        response.setContentType("text/plain; charset=utf-8");
        response.setCharacterEncoding("utf-8");
    }
}
