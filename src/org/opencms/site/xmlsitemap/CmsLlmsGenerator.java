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

    /** Helper container for storing update results. */
    protected record CmsLlmsFileContainer(CmsLlmsFile llmsbean, CmsFile llmsFile, boolean updated) {}

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
        5. Only create a language title heading if there are different language versions present
        6. For each first level  and second level transition (can be determined by the path structure), find a matching heading (starting at level 3 or level 2 if there are no different language versions) if it makes sense, summarizing the sub section in short, using the language of the sub section

        If you are unsure about the language, use %s.

        ## FORMATTING RULES:
        - Use markdown for emphasis/lists/headings (no links or code blocks)
        - Start headings at level 2 (##)

        ## OUTPUT: Markdown only, no preamble or explanation.""".formatted(
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
        # Summary of a single page for a llms.txt file for a website

        ## PROMPT
        You receive a query containing an excerpt of a web page, generating by a SOLR index.

        ```
        {
          "url": "https://example.com/index.html",
          "title": "The page title",
          "excerpt": "An excerpt of the page content",
          "question": "Please generate a summary for this title and excerpt usable in an llms.txt file."
        }
        ```

        ## INSTRUCTIONS:
        1. Use the found title and excerpt text to summarize the page in one or two longer sentence(s)
        2. The excerpt content has no specific order as it is built for a SOLR index, so you have to decide which information is important, please also consider the given page title for this
        2. The summary has to be optimized for usage with LLMs so that the most important information of the page is included
        3. IMPORTANT: Use the same language as the title and excerpt language for the language of the the summary, do not mix languages

        If you are unsure about the language, use %s.

        ## FORMATTING RULES:
        - Use markdown for formatting the summary (no links or code blocks)

        ## OUTPUT: Markdown only, no preamble or explanation.""".formatted(
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
            result = m_config.getRobotsTxtText() + "\n\n" + result;
        }
        return result;
    }

    /**
     * Updates the llms summary file if necessary in offline mode.<p>
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
            boolean updateOverrides = llmsFile.getDateLastModified() > llmsBean.getDate();

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
                } else if (llmsBean.getDate() < url.getDateLastModified().getTime()) {
                    // updated page after last summary creation
                    changed = true;
                    testPage = getSummaryForPage(url, testPage.getOverrideSummary());
                    llmsBean.getPagesMap().remove(resId);
                    LOG.debug("Updated URL " + url.getUrl() + " of page list.");
                } else {
                    // unchanged page, check override
                    if (updateOverrides && CmsStringUtil.isNotEmptyOrWhitespaceOnly(testPage.getOverrideSummary())) {
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

            pagesList.append("- [").append(page.getTitle()).append("]");
            pagesList.append("(").append(page.getUrl()).append(")");

            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(page.getOverrideSummary())) {
                pagesList.append(": ").append(page.getOverrideSummary());
            } else if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(page.getSummary())) {
                pagesList.append(": ").append(page.getSummary());
            }
            pagesList.append("\n");
        }

        try {
            ChatRequest q = ChatRequest.builder().messages(
                SystemMessage.from(SYSTEM_PROMPT_GROUP(Locale.ENGLISH)),
                UserMessage.from(getUserQueryGroup(pagesList.toString()))).build();
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
                            SystemMessage.from(SYSTEM_PROMPT_SUMMARY(Locale.ENGLISH)),
                            UserMessage.from(getUserQuerySummary(result, content))).build();
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
     *
     * @return the user query
     * @throws Exception if creating the JSON fails
     */
    private String getUserQuerySummary(CmsLlmsPage llmsPage, String excerpt) throws Exception {

        ObjectNode userQuery = MAPPER.createObjectNode();
        userQuery.put("url", llmsPage.getUrl());
        userQuery.put("title", llmsPage.getTitle());
        userQuery.put("excerpt", excerpt);
        userQuery.put("question", "Please generate a summary for this title and excerpt usable in an llms.txt file.");
        return userQuery.toString();
    }

}
