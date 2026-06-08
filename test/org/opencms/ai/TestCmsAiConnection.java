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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ai;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.test.OpenCmsTestRunner;
import org.opencms.util.CmsFileUtil;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.CmsXmlStringValue;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.jsoup.nodes.TextNode;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiChatModel;

/**
 * Unit tests for AI connection.<p>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestCmsAiConnection extends OpenCmsTestRunner {

    private static final String SCHEMA_SYSTEM_ID_1 = "http://www.opencms.org/llm-test1.xsd";
    private static final String TEST_HTML_1 = "<h2>Einfaches HTML</h2>\n<p>Dies ist ein <strong> Typoblindtext</strong>. Manchmal benutzt man Sätze, die alle Buchstaben des Alphabets enthalten. Sehr bekannt ist dieser:&nbsp;<em>The quick brown fox jumps over the lazy old dog.</em></p><p>&nbsp;</p>";

    /**
     * @see org.opencms.test.OpenCmsTestRunner#$openCmsSetUp(org.junit.jupiter.api.TestInfo)
     */
    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo, "simpletest", "/");
    }

    /**
     * Basic test for an AI provider comection.
     *
     * @throws Exception in case something goes wrong
     */
    @Test
    @Order(2)
    public void testAiConnection() {

        CmsAiProviderConfig aiProviderConfig = getProviderConfig();
        ChatModel LLM = OpenAiChatModel.builder().apiKey(aiProviderConfig.getApiKey()).baseUrl(
            aiProviderConfig.getProviderUrl()).modelName(aiProviderConfig.getModelName()).build();

        List<ChatMessage> messages = new ArrayList<ChatMessage>();
        messages.add(
            SystemMessage.from(
                "You are a writer of shakespeare sonnets. "
                    + "Format the generated sonnet as markdowne. "
                    + "Add a '#' heading to the sonnet that reflects the sonnets theme considering the users input."));
        messages.add(UserMessage.from("Write a sonnet about the open source software OpenCms."));

        ChatRequest llm_req = ChatRequest.builder().messages(messages).build();

        String result = LLM.chat(llm_req).aiMessage().text();
        // write to stdout for visual control
        System.out.println(result);

        assertNotNull(result);
    }

    /**
     * Test the (static) HTML extraction methods.
     *
     * @throws Exception in case something goes wrong
     */
    @Test
    @Order(1)
    public void testHTMLExtraction() throws Exception {

        System.out.println(TEST_HTML_1);
        CmsAiTranslator.HtmlParseResult result = CmsAiTranslator.parseHtmlTextNodes(TEST_HTML_1);
        assertNotNull(result.getDocument());
        System.out.println(result.toString());
        // test for changing of a text node qith the Jsoup API
        List<TextNode> textNodes = result.getTextNodes();
        assertTrue(textNodes.size() > 0);
        assertEquals("Einfaches HTML", textNodes.get(0).text());
        textNodes.get(1).text("Dies wurde geändert");
        String docString = result.toString();
        System.out.println(docString);
        assertTrue(docString.contains("Dies wurde geändert"));
        // check for creating a simplified translation string
        String translateString = result.toTranslateString();
        System.out.println(translateString);
        assertTrue(translateString.contains("Dies wurde geändert"));
        assertTrue(translateString.contains("⟦#3#⟧"));
        assertFalse(translateString.contains("⟦#4#⟧"));
        // check the result has the expeced number of text nodes
        List<String> translateStrings = CmsAiTranslator.HtmlParseResult.splitTranslateString(translateString);
        assertEquals(5, translateStrings.size());
        for (String tS : translateStrings) {
            System.out.println(tS);
        }
        // check for changing the Jsoup text nodes based on a simplified translation string
        translateString = "Manuell HTML⟦#0#⟧Geändert⟦#1#⟧Typoblindtext ⟦#2#⟧Sätze⟦#3#⟧The quick brown fox.";
        docString = result.setTranslatedString(translateString);
        System.out.println(docString);
        assertTrue(docString.contains("<h2>Manuell HTML</h2>"));
        assertFalse(docString.contains("Einfaches HTML"));
    }

    /**
     * Tests the AI model response when translating a CmsXmlContent.<p>
     *
     * @throws Exception in case the test fails
     */
    @Test
    @Order(3)
    public void testRawTranslation() throws Exception {

        CmsAiProviderConfig aiProviderConfig = getProviderConfig();

        CmsObject cms = getCmsObject();
        CmsXmlContent xmlContent = getTestContent(cms);

        // this content comes from the file that has been read
        String xpath = "String";
        CmsXmlStringValue stringValue = (CmsXmlStringValue)xmlContent.getValue(xpath, Locale.GERMAN, 0);
        assertEquals("LLmtest 1", stringValue.getStringValue(cms));

        // translate this with the configured LLM
        CmsAiTranslator aiTranslator = new CmsAiTranslator(cms, aiProviderConfig, xmlContent);
        // ChatResponse response = aiTranslator.translateXmlContentRaw(Locale.GERMAN, Locale.ENGLISH);
        String llmResult = aiTranslator.translateXmlContentRaw(Locale.GERMAN, Locale.ENGLISH, null);

        System.out.println(llmResult);
        assertNotNull(llmResult);

        Map<String, String> translated = CmsAiTranslator.parseTranslationResult(llmResult);
        assertNotNull(translated);

        for (Map.Entry<String, String> entry : translated.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }

        // ensure all values have been translated

        List<I_CmsXmlContentValue> xmlValues = CmsTranslationUtil.getValuesToTranslate(
            cms,
            xmlContent,
            Locale.GERMAN,
            null);
        Set<String> valSet = new HashSet<String>();
        for (I_CmsXmlContentValue xmlVal : xmlValues) {
            valSet.add(xmlVal.getPath());
        }
        assertEquals(translated.keySet(), valSet);

    }

    /**
     * Tests the AI model response streaming when translating a CmsXmlContent.<p>
     *
     * @throws Exception in case the test fails
     */
    @Test
    @Order(4)
    public void testRawTranslationWithStream() throws Exception {

        CmsAiProviderConfig aiProviderConfig = getProviderConfig();

        CmsObject cms = getCmsObject();
        CmsXmlContent xmlContent = getTestContent(cms);

        // this content comes from the file that has been read
        String xpath = "String";
        CmsXmlStringValue stringValue = (CmsXmlStringValue)xmlContent.getValue(xpath, Locale.GERMAN, 0);
        assertEquals("LLmtest 1", stringValue.getStringValue(cms));

        StreamingChatResponseHandler handler = new StreamingChatResponseHandler() {

            public void onCompleteResponse(ChatResponse response) {

                System.out.println("DONE!\n\n");
            }

            public void onError(Throwable error) {

                System.out.println("ERROR: " + error.getMessage() + "\n\n");
            }

            public void onPartialResponse(String partialResponse) {

                System.out.println("data: " + partialResponse.replace("\r", "").replace("\n", "\\n"));
            }
        };

        // translate this with the configured LLM
        CmsAiTranslator aiTranslator = new CmsAiTranslator(cms, aiProviderConfig, xmlContent);
        // ChatResponse response = aiTranslator.translateXmlContentRaw(Locale.GERMAN, Locale.ENGLISH);
        String llmResult = aiTranslator.translateXmlContentRaw(Locale.GERMAN, Locale.ENGLISH, handler);

        System.out.println(llmResult);
        assertNotNull(llmResult);

        Map<String, String> translated = CmsAiTranslator.parseTranslationResult(llmResult);
        assertNotNull(translated);

        for (Map.Entry<String, String> entry : translated.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }

        // ensure all values have been translated

        List<I_CmsXmlContentValue> xmlValues = CmsTranslationUtil.getValuesToTranslate(
            cms,
            xmlContent,
            Locale.GERMAN,
            null);
        Set<String> valSet = new HashSet<String>();
        for (I_CmsXmlContentValue xmlVal : xmlValues) {
            valSet.add(xmlVal.getPath());
        }
        assertEquals(translated.keySet(), valSet);

    }

    /**
     * Tests the complete translation of a CmsXmlContent.<p>
     *
     * @throws Exception in case the test fails
     */
    @Test
    @Order(5)
    public void testXmlTranslation() throws Exception {

        CmsAiProviderConfig aiProviderConfig = getProviderConfig();

        CmsObject cms = getCmsObject();
        CmsXmlContent xmlContent = getTestContent(cms);
        assertFalse(xmlContent.hasLocale(Locale.ENGLISH));
        assertFalse(xmlContent.hasLocale(Locale.FRENCH));

        CmsAiTranslator aiTranslator = new CmsAiTranslator(cms, aiProviderConfig, xmlContent);
        xmlContent = aiTranslator.translateXmlContent(Locale.GERMAN, Locale.ENGLISH);
        assertTrue(xmlContent.hasLocale(Locale.ENGLISH));
        System.out.println(xmlContent.toString());

        /*
        xmlContent = aiTranslator.translateXmlContent(Locale.GERMAN, Locale.FRENCH);
        assertTrue(xmlContent.hasLocale(Locale.FRENCH));
        System.out.println(xmlContent.toString());

        xmlContent = aiTranslator.translateXmlContent(Locale.GERMAN, Locale.ITALIAN);
        assertTrue(xmlContent.hasLocale(Locale.ITALIAN));
        System.out.println(xmlContent.toString());
        */
    }

    /**
     * Reads the AI provider configuration.
     *
     * @throws Exception in case something goes wrong
     */
    private CmsAiProviderConfig getProviderConfig() {

        String llmApiKey = System.getProperty("LLM_API_KEY");
        String llmApiProvider = System.getProperty("LLM_PROVIDER");
        String llmModelName = System.getProperty("LLM_MODEL_NAME");

        CmsAiProviderConfig config = null;

        if ((llmApiKey != null) && (llmApiProvider != null) && (llmModelName != null)) {
            config = new CmsAiProviderConfig(llmApiKey, llmApiProvider, llmModelName);
        }

        if (config == null) {
            String message = "\nNo AI provider configuration found!\n"
                + "Tests that require an AI provider are skipped.\n\n"
                + "LLM_PROVIDER: "
                + llmApiProvider
                + "\nLLM_MODEL_NAME: "
                + llmModelName
                + "\nLLM_API_KEY: "
                + (llmApiKey != null ? "(available)" : null)
                + "\n\nSet these values as System properties for running the tests!\n"
                + "In Eclipse you can do this in 'Run Configurations > VM arguments > -DLLM_PROVIDER=xxxxx'\n\n";
            System.out.println(message);
            // abort the test
            Assumptions.assumeTrue(config != null, message);
        }

        return config;
    }

    /**
     * Reads the CmsXmlContent to be used in the test cases from the RFS.<p>
     *
     * @throws Exception in case XML generation fails (this should not happen)
     */
    private CmsXmlContent getTestContent(CmsObject cms) throws Exception {

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);
        String content;
        // unmarshal content definition
        content = CmsFileUtil.readFile("org/opencms/ai/xmlcontent-definition-1.xsd", CmsEncoder.ENCODING_UTF_8);
        CmsXmlContentDefinition definition = CmsXmlContentDefinition.unmarshal(content, SCHEMA_SYSTEM_ID_1, resolver);
        // store content definition in entity resolver
        content = CmsFileUtil.readFile("org/opencms/ai/xmlcontent-1.xml", CmsEncoder.ENCODING_UTF_8);
        CmsXmlEntityResolver.cacheSystemId(
            SCHEMA_SYSTEM_ID_1,
            definition.getSchema().asXML().getBytes(CmsEncoder.ENCODING_UTF_8));
        // now create the XML content
        CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);

        return xmlContent;
    }
}
