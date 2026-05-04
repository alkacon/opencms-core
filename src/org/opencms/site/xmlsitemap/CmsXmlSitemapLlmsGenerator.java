/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (https://www.alkacon.com)
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

package org.opencms.site.xmlsitemap;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.I_CmsSecretStore;
import org.opencms.util.CmsStringUtil;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.openai.OpenAiChatModel;

/**
 * Generates the output for llms.txt files using the configured AI chat model.<p>
 */
public class CmsXmlSitemapLlmsGenerator {

    /** A document to use for the llms.txt file. */
    public static class SiteDocument {

        /** The description of the document. */
        private String m_description;
        /** The locale of the document. */
        private Locale m_locale;
        /** The URL of the document. */
        private String m_url;
        /** The title of the document. */
        private String m_title;

        /**
         * @param url URL of the document
         * @param title title of the document
         * @param description description of the document
         * @param locale the locale of the document
        
         */
        public SiteDocument(String url, String title, String description, Locale locale) {

            m_url = url;
            m_title = title;
            m_description = description;
            m_locale = locale;
        }

        /**
         * JSON representation of the document.
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {

            try {
                return MAPPER.writeValueAsString(this);
            } catch (Exception e) {
                return super.toString();
            }
        }

        /**
         * @return the document description
         */
        @JsonIgnore
        String getDescription() {

            return m_description;
        }

        /**
         * @return the locale of the document
         */
        @JsonProperty(value = "locale", defaultValue = "")
        Locale getLocale() {

            return m_locale;
        }

        /**
         * @return the document title
         */
        @JsonProperty(value = "title", defaultValue = "")
        String getTitle() {

            return m_title;
        }

        /**
         * @return the URL of the document
         */
        @JsonProperty(value = "url", defaultValue = "")
        String getUrl() {

            return m_url;
        }
    }

    /** The maximum size of pages to send to the AI per request. */
    private static final int CHUNK_SIZE = 50;

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsXmlSitemapLlmsGenerator.class);

    /** The object mapper to use for JSON generation. */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** The query timeout for each AI request in minutes. */
    private static final int QUERY_TIMEOUT = 5;

    /** Secret store key. */
    private static final String SECRET_APIKEY = "seo.llms.apikey";

    /** Secret store base URL. */
    private static final String SECRET_BASEURL = "seo.llms.baseurl";

    /** Secret store model. */
    private static final String SECRET_MODEL = "seo.llms.model";

    /** The configured chat model to use. */
    private ChatModel m_chatModel;

    /** The CmsObject instance to use for resource access. */
    private CmsObject m_cms;

    /** The list of page URLs to include in the llms.txt file. */
    private List<CmsXmlSitemapUrlBean> m_urls;

    /**
     * Default constructor.
     */
    public CmsXmlSitemapLlmsGenerator(List<CmsXmlSitemapUrlBean> urls, CmsObject cms) {

        m_urls = urls;
        m_cms = cms;
    }

    /**
     * Splits the given input list into consecutive sublists ("chunks") of the specified maximum size.<p>
     *
     * Each chunk is a view backed by the original list (via {@link List#subList(int, int)}),
     * meaning changes to the returned sublists will reflect in the original list and vice versa.<p>
     *
     * @param input the source list to be partitioned into chunks; must not be {@code null}
     * @param size  the maximum size of each chunk; must be greater than 0
     * @return a list of sublists, where each sublist has at most {@code size} elements
     */
    private static <T> List<List<T>> chunkSiteDocumentList(List<T> input, int size) {

        if (size < 1) {
            throw new IllegalArgumentException("List target size must be > 0");
        }

        List<List<T>> result = new ArrayList<>();
        for (int i = 0; i < input.size(); i += size) {
            int end = Math.min(i + size, input.size());
            result.add(input.subList(i, end));
        }
        return result;
    }

    /**
     * The system prompt for generating a list of documents.<p>
     *
     * @param locale the preferred locale of the answer
     * @return the system prompt
     */
    private static final String SYSTEM_PROMPT_DOCUMENTS(Locale locale) {

        return """
        # Generation of an llms.txt file for a website

        ## PROMPT
        You receive queries in this JSON format:

        ```
        {
          "documents": [
            {
              "url": "https://example.com/index.html",
              "title": "Example page",
              "locale": "en"
            }
          ],
          "question": "Please generate a markdown list with summaries for the above given documents."
        }
        ```

        ## INSTRUCTIONS:
        1. Iterate the JSON list of documents
        2. For each list entry, generate an unordered list entry in markdown using the title and url information
        3. Essential: keep the order of the entries, arrange them exactly as they were esp. separate different language versions
        4. Read the url, crawl the page and extract meta info like description and keywords. Also use the paragraph belonging to the most important 'h1' heading for this.
        5. Use the found meta info and paragraph content to summarize the page in one or two longer sentence(s)
        6. The summary has to be optimized for usage with LLMs so that the most important information of the page is included
        7. Add this summary behind the link, use a colon character to separate it from the link
        8. IMPORTANT: Use the individual locale information for the language of the complete list entry, esp. the title and the summary, do not mix languages in one entry
        9. Just generate the pure unordered list without adding anything else like headings or paragraphs

        If you are unsure about the language, use %s.

        ## FORMATTING RULES:
        - Use markdown for emphasis/lists (no links or code blocks)

        ## OUTPUT: Markdown only, no preamble or explanation.""".formatted(
            locale.getDisplayLanguage(Locale.ENGLISH));
    }

    /**
     * The system prompt for grouping a list of documents.<p>
     *
     * @param locale the preferred locale of the answer
     * @return the system prompt
     */
    private static final String SYSTEM_PROMPT_GROUP(Locale locale) {

        return """
        # Optimization of an llms.txt file for a website

        ## PROMPT
        You receive queries in this JSON format:

        ```
        {
          "document": "A document content in markdown",
          "question": "Please generate headlines for the first- and second-level list entries where it makes sense, as well as for different language versions, and keep all existing entries."
        }
        ```

        ## INSTRUCTIONS:
        1. Analyze the content of the "document" element, it is a list of URLs with a summary for each URLin markdown syntax
        2. Essential: keep the order of the entries, arrange them exactly as they were especially separate different language versions
        3. Important: keep every entry of the list, leave the title, URL and description text exactly as it is
        4. If there is more than one language version, for each language version list, add a short title heading (starting at level 2) for the following language version, using the language of the sub section
        5. For each first level  and second level transition (can be determined by the path structure), find a matching heading (starting at level 3 or level 2 if there are no different language versions) if it makes sense, summarizing the sub section in short, using the language of the sub section

        If you are unsure about the language, use %s.

        ## FORMATTING RULES:
        - Use markdown for emphasis/lists/headings (no links or code blocks)
        - Start headings at level 2 (##)

        ## OUTPUT: Markdown only, no preamble or explanation.""".formatted(
            locale.getDisplayLanguage(Locale.ENGLISH));
    }

    /**
     * Generates a textual representation in markdown for the current set of site documents by querying a chatbot.<p>
     *
     * @return a formatted text containing either AI-generated summaries/groupings
     *         or a fallback list of site documents in markdown format
     */
    public String getLlmsTextForUrls() {

        String result = "";
        ChatRequest q = null;
        // prepare the query
        LOG.info("Preparing queries for chatbot.");
        long start = System.currentTimeMillis();
        List<List<SiteDocument>> docChunks = chunkSiteDocumentList(getSiteDocuments(), CHUNK_SIZE);
        int loopCount = 1;
        StringBuilder documentList = new StringBuilder();
        // loop the document parts and query the AI
        for (List<SiteDocument> docChunk : docChunks) {
            String answer = "";
            try {
                q = ChatRequest.builder().messages(
                    SystemMessage.from(SYSTEM_PROMPT_DOCUMENTS(Locale.ENGLISH)),
                    UserMessage.from(getUserQueryDocuments(docChunk))).build();
                LOG.info("Sending document query " + loopCount + "/" + docChunks.size() + " to chatbot.");
                answer = getChatModel().chat(q).aiMessage().text();
            } catch (Exception e) {
                LOG.error("Failed to get answer for document query", e);
            } finally {
                LOG.debug("Answer is: " + answer);
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(answer)) {
                    LOG.info("No answer, fall back to manual list of pages.");
                    StringBuilder manualAnswer = new StringBuilder();
                    for (SiteDocument doc : docChunk) {
                        manualAnswer.append("- [").append(doc.getTitle()).append("]");
                        manualAnswer.append("(").append(doc.getUrl()).append(")");
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(doc.getDescription())) {
                            manualAnswer.append(":").append(doc.getDescription());
                        }
                        manualAnswer.append("\n");
                    }
                    answer = manualAnswer.toString();
                }
                documentList.append(answer).append('\n');
            }
            loopCount++;
        }

        // send all listed documents to AI to group them and add headlines
        try {
            q = ChatRequest.builder().messages(
                SystemMessage.from(SYSTEM_PROMPT_GROUP(Locale.ENGLISH)),
                UserMessage.from(getUserQueryGroup(documentList.toString()))).build();
            LOG.info("Sending group query to chatbot.");
            result = getChatModel().chat(q).aiMessage().text();
        } catch (Exception e) {
            LOG.error("Failed to get answer for document query", e);
        } finally {
            LOG.debug("Answer is: " + result);
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(result)) {
                LOG.info("No answer, just use ungrouped list of pages.");
                result = documentList.toString();;
            }
            long end = System.currentTimeMillis();
            float seconds = ((end - start) / 1000);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Chatbot query took " + seconds + "s for \nQuery: \n" + q + "\nAnswer: " + result);
            } else if (LOG.isInfoEnabled()) {
                LOG.info("Chatbot query  took " + seconds + "s.");
            }
        }

        return result;
    }

    /**
     * returns the initialized chat model to use for generating the llms.txt file.<p>
     *
     * @return the initialized chat model
     */
    private ChatModel getChatModel() {

        if (m_chatModel == null) {
            I_CmsSecretStore secrets = OpenCms.getSecretStore();
            String apiKey = secrets.getSecret(SECRET_APIKEY);
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(apiKey)) {
                throw new IllegalStateException("Missing API key");
            }
            String baseUrl = secrets.getSecret(SECRET_BASEURL);
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(baseUrl)) {
                throw new IllegalStateException("Missing base URL");
            }
            String model = secrets.getSecret(SECRET_MODEL);
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(model)) {
                throw new IllegalStateException("Missing model");
            }
            m_chatModel = OpenAiChatModel.builder().apiKey(apiKey).baseUrl(baseUrl).modelName(model).timeout(
                Duration.ofMinutes(QUERY_TIMEOUT)).build();

        }
        return m_chatModel;
    }

    /**
     * Returns the site documents with all necessary information to create the list of pages.<p>
     *
     * @return the site documents
     */
    private List<SiteDocument> getSiteDocuments() {

        List<SiteDocument> siteDocuments = new ArrayList<SiteDocument>(m_urls.size());

        for (CmsXmlSitemapUrlBean url : m_urls) {
            CmsResource res = url.getOriginalResource();
            try {
                String title = m_cms.readPropertyObject(res, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue("");
                String description = m_cms.readPropertyObject(
                    res,
                    CmsPropertyDefinition.PROPERTY_DESCRIPTION,
                    false).getValue("");
                siteDocuments.add(new SiteDocument(url.getUrl(), title, description, url.getLocale()));
            } catch (CmsException e) {
                // failed to read document properties, ignore this document
            }
        }

        return siteDocuments;
    }

    /**
     * Returns the user query including the given list of site documents in JSON format.<p>
     *
     * @param siteDocuments the documents to include in the query
     * @return the user query
     * @throws Exception if creating the JSON fails
     */
    private String getUserQueryDocuments(List<SiteDocument> siteDocuments) throws Exception {

        ObjectNode userQuery = MAPPER.createObjectNode();
        userQuery.set("documents", MAPPER.valueToTree(siteDocuments));
        userQuery.put("question", "Please generate a markdown list with summaries for the above given documents.");
        return userQuery.toString();
    }

    /**
     * Returns the user query for the formatted list of documents in JSON format.<p>
     *
     * @return the user query
     * @throws Exception if creating the JSON fails
     */
    private String getUserQueryGroup(String documents) throws Exception {

        ObjectNode userQuery = MAPPER.createObjectNode();
        userQuery.put("document", documents);
        userQuery.put(
            "question",
            "Please generate headlines for the first- and second-level list entries where it makes sense, as well as for different language versions, and keep all existing entries.");
        return userQuery.toString();
    }

}
