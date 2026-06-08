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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.solr.CmsSolrIndex;
import org.opencms.search.solr.CmsSolrResultList;
import org.opencms.security.I_CmsSecretStore;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

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
public class CmsLlmsGenerator {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLlmsGenerator.class);

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

    /** The configuration bean. */
    private CmsXmlSeoConfiguration m_config;

    /** The configuration file resource. */
    private CmsResource m_configRes;

    /**
     * Constructor, with parameters.<p>
     *
     * @param xmlSitemapGenerator the XMl sitemap generator to use for building the list of URLs
     * @param config the configuration bean
     * @param configRes the configuration file resource
     * @param cms the current users context
     */
    public CmsLlmsGenerator(CmsXmlSeoConfiguration config, CmsResource configRes, CmsObject cms) {

        m_config = config;
        // modification dates always have to be computed
        m_config.setComputeContainerPageModificationDates(true);
        m_configRes = configRes;
        m_cms = cms;
    }

    /**
     * The system prompt for grouping a list of web pages.<p>
     *
     * @param locale the preferred locale of the answer
     * @return the system prompt
     */
    private static final String SYSTEM_PROMPT_GROUP(Locale locale) {

        return """
        # llms.txt Structure Optimizer

        ## ROLE
        You are a technical document structurer. Your sole task is to add meaningful headings to an existing `llms.txt` file without altering any of its entries.

        ## INPUT
        You receive a JSON object with the following structure:

        ```json
        {
          "document": "A document content in markdown",
          "question": "Please generate headlines for the first- and second-level list entries where it makes sense, as well as for different language versions, and keep all existing entries."
        }
        ```

        ## TASK
        Analyze the document and insert headings to group entries logically — by language version and by URL path structure — while leaving all existing content untouched.

        > **Important:** Do not fetch, scrape, or visit any URLs. Work exclusively with the content already provided in the `document` field.

        ## INSTRUCTIONS

        ### 1. Preserve all existing content
        - Keep every entry exactly as-is: title, URL, and description text must not be altered
        - Important: do **not** fetch, scrape, or visit any URLs — all necessary content is already provided in the document
        - Keep the original order of all entries — do not reorder, merge, or remove anything

        ### 2. Language version headings (Level 2)
        - Detect whether the document contains entries in more than one language
        - If yes: insert a **level 2 heading (`##`)** before each language group, written in that language
        - If only one language is present: skip language headings entirely — even if URL paths suggest multiple language versions
        - **Example:** All URLs starting with `/en/` do not constitute multiple language versions — they are all the same language

        ### 3. Section headings by URL path structure (Level 2 or 3)
        - Determine groupings from the first and second path segments of each URL (e.g. `/products/`, `/products/software/`)
        - Insert a heading before each group **only if it adds meaningful context**
        - Use **level 3 (`###`)** when language headings are present, **level 2 (`##`)** when they are not
        - Write the heading in the language of that section
        - Do **not** add a heading if the group contains only one entry or if no meaningful label can be derived

        ### 4. Language fallback
        If the language of a section cannot be determined, use: `%s`

        ## OUTPUT FORMAT
        - Markdown only (headings, bold, lists — no links, no code blocks)
        - No preamble, no explanation, no meta-commentary
        - The structured document — nothing else""".formatted(
            locale.getDisplayLanguage(Locale.ENGLISH));
    }

    /**
     * The system prompt for summarizing of a single page content.<p>
     *
     * @param locale the preferred locale of the answer
     * @return the system prompt
     */
    private static final String SYSTEM_PROMPT_SUMMARY(Locale locale) {

        return """
        # Page Summary Generator for llms.txt

        ## ROLE
        You are a technical content summarizer. Your sole task is to generate concise, information-dense summaries optimized for LLM consumption.

        ## INPUT
        You receive a JSON object with the following structure:

        ```json
        {
          "url": "https://example.com/index.html",
          "locale": "en",
          "title": "The page title",
          "excerpt": "An excerpt of the page content",
          "question": "Please generate a summary for this title and excerpt usable in an llms.txt file."
        }
        ```

        ## TASK
        Generate a summary of **1-2 sentences** for the given page, suitable for inclusion in an `llms.txt` file.

        ## INSTRUCTIONS
        1. Base the summary primarily on the **title** and use the excerpt to extract the most relevant information
        2. The excerpt is **unordered** (SOLR index output) - infer structure and relevance from context, not order
        3. Optimize for **LLM readability**: prefer precise, factual, and dense phrasing over fluent prose
        4. Write in the **same language as the title and excerpt** and use the information from the **locale** - do not mix languages
        5. If the language cannot be determined, default to: `%s`
        6. Do **not** invent or infer information not present in the input

        ## OUTPUT FORMAT
        - Plain markdown text (no links, no code blocks, no headers)
        - No preamble, no explanation, no meta-commentary
        - The summary itself - nothing else""".formatted(
            locale.getDisplayLanguage(Locale.ENGLISH));
    }

    /**
     * Generates a textual representation in markdown for the current set of site documents by querying a chatbot.<p>
     *
     * @return a formatted text containing either AI-generated summaries/groupings
     *         or a fallback list of site documents in markdown format
     */
    public String getLlmsText() throws CmsException {

        String result = "";
        // update the llms file in offline mode
        CmsLlmsFileContainer updateResult = updateLlmsFile();
        if (updateResult.updated()) {
            // directly use the updated result
            result = updateResult.llmsbean().getResult();
        } else {
            // read the llms file and get the result if present
            CmsFile llmsFile = getLlmsVfsFile();
            if (llmsFile != null) {
                CmsXmlContent content = CmsXmlContentFactory.unmarshal(m_cms, llmsFile);
                result = content.getStringValue(m_cms, CmsLlmsFile.NODE_RESULT, CmsLlmsFile.LOCALE);
            }
        }

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_config.getRobotsTxtText())) {
            // add prefix text
            result = m_config.getRobotsTxtText() + "\n\n" + result;
        }
        return result;
    }

    /**
     * Updates the llms.txt summary file if necessary in offline mode or when triggered by scheduled job.<p>
     *
     * @return the update results
     *
     * @throws CmsException if something goes wrong
     */
    public CmsLlmsFileContainer updateLlmsFile() throws CmsException {

        if (!m_cms.getRequestContext().getCurrentProject().isOnlineProject()) {
            // first get XML content file with generated summary
            CmsFile llmsFile = getLlmsVfsFile();

            // create objects from file entries
            CmsLlmsFile llmsBean = CmsLlmsFile.createLlmsFileFromContent(
                CmsXmlContentFactory.unmarshal(m_cms, llmsFile),
                m_cms);

            // check if overrides have to be updated due to a file modification
            boolean updateOverridesOrHideFlag = llmsFile.getDateLastModified() > llmsBean.getDate();

            CmsXmlSitemapGenerator xmlSitemapGenerator = CmsXmlSitemapActionElement.prepareSitemapGenerator(
                m_configRes,
                m_config);
            // compute URLs of current sitemap
            List<CmsXmlSitemapUrlBean> urls = xmlSitemapGenerator.generateSitemapBeans();
            // after that, compare entries from file with entries of current sitemap
            List<CmsLlmsPage> resultPages = new ArrayList<CmsLlmsPage>(urls.size());
            boolean changed = false;
            for (CmsXmlSitemapUrlBean url : urls) {
                if (url.getDateLastModified() == null) {
                    // ensure that the URL has a last modification date set
                    url.setDateLastModified(new Date());
                    LOG.debug("URL " + url.getUrl() + " has no valid last modification date, set it to current date.");
                }
                CmsUUID resId = url.getOriginalResource().getStructureId();
                CmsLlmsPage testPage = llmsBean.getPagesMap().get(resId);
                if (testPage == null) {
                    // new page
                    changed = true;
                    testPage = getSummaryForPage(url, null);
                    LOG.debug("Adding new URL " + url.getUrl() + " to page list.");
                } else if (testPage.isHide()) {
                    if (updateOverridesOrHideFlag) {
                        // in case a hide option has been updated, we need to refresh the result
                        changed = true;
                    }
                    llmsBean.getPagesMap().remove(resId);
                } else if (llmsBean.getDate() < url.getDateLastModified().getTime()) {
                    // updated page after last summary creation
                    changed = true;
                    testPage = getSummaryForPage(url, testPage.getOverrideSummary());
                    llmsBean.getPagesMap().remove(resId);
                    LOG.debug("Updated URL " + url.getUrl() + " of page list.");
                } else {
                    // unchanged page, check override
                    if (updateOverridesOrHideFlag
                        && CmsStringUtil.isNotEmptyOrWhitespaceOnly(testPage.getOverrideSummary())) {
                        // in case an override has been updated, we need to refresh the result
                        changed = true;
                    }
                    llmsBean.getPagesMap().remove(resId);
                    LOG.debug("Skipped unchanged URL " + url.getUrl() + " of page list.");
                }
                // add page to result
                resultPages.add(testPage);
            }

            if (llmsBean.getPagesMap().size() > 0) {
                // at least one page is not in the current sitemap any more
                changed = true;
            }

            if (changed) {
                // store the updated pages in object
                llmsBean.setPages(resultPages);
                // generate final result output for llms.txt
                llmsBean.setResult(getFinalLlmsText(llmsBean));

                // set time stamp for updated content (10 seconds in future to be sure to be newer than last modified date of the file)
                llmsBean.setDate(new Date().getTime() + 10000);

                // generate XML from updated pages and set it to file content
                llmsFile.setContents(llmsBean.getXmlContentFromLlmsFile(m_cms).marshal());

                // check lock state
                CmsLock lock = m_cms.getLock(llmsFile);
                boolean locked = false;
                if (lock.isUnlocked() && lock.isLockableBy(m_cms.getRequestContext().getCurrentUser())) {
                    m_cms.lockResource(llmsFile);
                    locked = true;
                } else if (!lock.isOwnedBy(m_cms.getRequestContext().getCurrentUser()) && !lock.isInherited()) {
                    m_cms.changeLock(llmsFile);
                    locked = true;
                }

                if (m_cms.getLock(llmsFile).isOwnedBy(m_cms.getRequestContext().getCurrentUser())) {
                    // write file
                    m_cms.writeFile(llmsFile);
                    if (locked) {
                        m_cms.unlockResource(llmsFile);
                    }
                    LOG.debug("XML file " + llmsFile.getRootPath() + " successfully updated.");
                    return new CmsLlmsFileContainer(llmsBean, llmsFile, true);
                } else {
                    LOG.debug("XML file " + llmsFile.getRootPath() + " not locked by current user, not updated.");
                }
            }
        }
        return new CmsLlmsFileContainer(null, null, false);
    }

    /** Helper container for storing update results. */
    protected record CmsLlmsFileContainer(CmsLlmsFile llmsbean, CmsFile llmsFile, boolean updated) {}

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
     * Returns the grouped formatted pages list from the objects that is used for the llms.txt file.<p>
     *
     * @param llmsBean the object containing the pages for the list
     * @return the grouped formatted pages list
     */
    private String getFinalLlmsText(CmsLlmsFile llmsBean) {

        String result = "";
        // build the list of pages in markdown
        StringBuilder pagesList = new StringBuilder();
        for (CmsLlmsPage page : llmsBean.getPages()) {

            if (!page.isHide()) {
                pagesList.append("- [").append(page.getTitle()).append("]");
                pagesList.append("(").append(page.getUrl()).append(")");

                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(page.getOverrideSummary())) {
                    pagesList.append(": ").append(page.getOverrideSummary());
                } else if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(page.getSummary())) {
                    pagesList.append(": ").append(page.getSummary());
                }
                pagesList.append("\n");
            }
        }

        try {
            ChatRequest q = ChatRequest.builder().messages(
                SystemMessage.from(SYSTEM_PROMPT_GROUP(Locale.ENGLISH)),
                UserMessage.from(getUserQueryGroup(pagesList.toString()))).maxOutputTokens(8192).build();
            LOG.debug("Sending group query to chatbot.");
            result = getChatModel().chat(q).aiMessage().text();
        } catch (Exception e) {
            LOG.error("Failed to get group answer for document query", e);
        } finally {
            LOG.debug("Answer is: " + result);
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(result)) {
                LOG.debug("No answer, use ungrouped list of pages as fallback.");
                result = pagesList.toString();
            }
        }

        return result;
    }

    /**
     * Returns the VFS file for the generated summaries.<p>
     *
     * If the file does not exist and cannot be created, <code>null</code> is returned.<p>
     *
     * @return the VFS file for the generated summaries, or <code>null</code> if it is not present
     *
     * @throws CmsException if reading or creating the file fails
     */
    private CmsFile getLlmsVfsFile() throws CmsException {

        // first check if XML content with generated summary is present
        String pathLlmsFile = m_cms.getSitePath(m_configRes) + ".xml";
        CmsResource llmsFileRes = null;
        if (!m_cms.existsResource(pathLlmsFile) && !m_cms.getRequestContext().getCurrentProject().isOnlineProject()) {
            // create new XML content file
            CmsXmlContent content = CmsXmlContentFactory.createDocument(
                m_cms,
                CmsLlmsFile.LOCALE,
                (CmsResourceTypeXmlContent)OpenCms.getResourceManager().getResourceType(
                    CmsLlmsFile.VFS_FILE_TYPE_NAME));
            llmsFileRes = m_cms.createResource(
                pathLlmsFile,
                OpenCms.getResourceManager().getResourceType(CmsLlmsFile.VFS_FILE_TYPE_NAME),
                content.marshal(),
                null);
        } else {
            // read existing XML content file
            llmsFileRes = m_cms.readResource(pathLlmsFile);
        }
        return llmsFileRes != null ? m_cms.readFile(llmsFileRes) : null;

    }

    /**
     * Returns the page updated with the generated summary.<p>
     *
     * @param url the url bean to create the page form
     * @param overrideSummary the override summary text
     *
     * @return the page updated with the generated summary
     */
    private CmsLlmsPage getSummaryForPage(CmsXmlSitemapUrlBean url, String overrideSummary) {

        // create new page object and fill values from url bean
        CmsLlmsPage result = new CmsLlmsPage();
        result.setDate(url.getDateLastModified().getTime());
        result.setId(url.getOriginalResource().getStructureId());
        result.setUrl(url.getUrl());
        result.setOverrideSummary(overrideSummary);
        // set empty title and summary values
        result.setTitle("");
        result.setSummary("");

        // get the SOLR index values for this resource
        CmsSolrIndex solrOnline = OpenCms.getSearchManager().getIndexSolr(CmsSolrIndex.DEFAULT_INDEX_NAME_ONLINE);

        // get locale to use for getting index fields
        CmsResource locRes = url.getDetailPageResource() == null
        ? url.getOriginalResource()
        : url.getDetailPageResource();
        Locale contentLocale = CmsLocaleManager.getMainLocale(m_cms, locRes);

        String searchParams = "fq=id:\""
            + url.getOriginalResource().getStructureId()
            + "\"&fl=Title_prop,"
            + "content_"
            + contentLocale;
        try {
            // search the index
            CmsSolrResultList onlineResults = solrOnline.search(m_cms, searchParams);
            if (onlineResults.size() > 0) {
                // found a match, get title and generate summary
                result.setTitle(onlineResults.get(0).getField("Title_prop"));
                String content = onlineResults.get(0).getField("content_" + contentLocale);
                if (CmsStringUtil.isNotEmpty(content)) {
                    String summary = "";
                    try {
                        ChatRequest q = ChatRequest.builder().messages(
                            SystemMessage.from(SYSTEM_PROMPT_SUMMARY(contentLocale)),
                            UserMessage.from(getUserQuerySummary(result, content, contentLocale))).build();
                        LOG.info("Sending excerpt query for URL " + url.getUrl() + " to chatbot.");
                        summary = getChatModel().chat(q).aiMessage().text();
                    } catch (Exception e) {
                        LOG.error(
                            "Failed to get answer for summary query for URL " + url.getUrl() + ": " + e.getMessage());
                    } finally {
                        if (CmsStringUtil.isEmptyOrWhitespaceOnly(summary)) {
                            LOG.debug("No summary, use parts of SOLR content as fallback.");
                            summary = CmsStringUtil.trimToSize(content, 200);
                        }
                    }
                    result.setSummary(summary);
                } else {
                    LOG.debug("No SOLR content found for resource.");
                    result.setSummary("");
                }
            }
        } catch (CmsException e) {
            LOG.error("Failed to search the index for URL " + url.getUrl(), e);
        }

        return result;
    }

    /**
     * Returns the user query for the list of documents as markup in JSON format.<p>
     *
     * @param documents the list of documents as markup
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

    /**
     * Returns the user query for the summary of a URL in JSON format.<p>
     *
     * @param llmsPage the page object
     * @param excerpt the page content to summarize
     * @param contentLocale Locale of the content to summarize
     *
     * @return the user query
     * @throws Exception if creating the JSON fails
     */
    private String getUserQuerySummary(CmsLlmsPage llmsPage, String excerpt, Locale contentLocale) throws Exception {

        ObjectNode userQuery = MAPPER.createObjectNode();
        userQuery.put("url", llmsPage.getUrl());
        userQuery.put("locale", contentLocale.getLanguage());
        userQuery.put("title", llmsPage.getTitle());
        userQuery.put("excerpt", excerpt);
        userQuery.put("question", "Please generate a summary for this title and excerpt usable in an llms.txt file.");
        return userQuery.toString();
    }

}
