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

package org.opencms.ai;

import org.opencms.file.CmsObject;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import dev.langchain4j.model.chat.request.json.JsonArraySchema;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonRawSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialResponse;
import dev.langchain4j.model.chat.response.PartialResponseContext;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;

/**
 * Translates OpenCms XML content using an external AI provider.<p>
 *
 * @since 21.0.0
 */
public class CmsAiTranslator {

    /**
     * Holder for parsed HTML text nodes and helper operations.<p>
     */
    public static class HtmlParseResult {

        private final Document m_doc;
        private final List<TextNode> m_textNodes;
        private String m_translateString;
        private final boolean m_isFullDocument;

        /**
         * Creates a new parse result.<p>
         *
         * @param doc the parsed document
         * @param textNodes the collected text nodes
         * @param isFullDocument true if input is a full HTML document
         */
        public HtmlParseResult(Document doc, List<TextNode> textNodes, boolean isFullDocument) {

            m_doc = doc;
            m_textNodes = textNodes;
            m_isFullDocument = isFullDocument;
        }

        /**
         * Splits a translation string into segments by marker tokens.<p>
         *
         * @param input the translation string
         *
         * @return the list of segments
         */
        public static List<String> splitTranslateString(String input) {

            if (input == null) {
                return new ArrayList<String>();
            }

            String[] parts = input.split("⟦#\\d+#⟧", -1);
            return new ArrayList<String>(Arrays.asList(parts));
        }

        /**
         * Returns the parsed document.<p>
         *
         * @return the parsed document
         */
        public Document getDocument() {

            return m_doc;
        }

        /**
         * Returns the collected text nodes.<p>
         *
         * @return the collected text nodes
         */
        public List<TextNode> getTextNodes() {

            return m_textNodes;
        }

        /**
         * Returns true if the input represented a full document.<p>
         *
         * @return true if the input is a full document
         */
        public boolean isFullDocument() {

            return m_isFullDocument;
        }

        /**
         * Applies a translated string to the text nodes and returns the HTML output.<p>
         *
         * @param translation the translated string
         *
         * @return the resulting HTML
         *
         * @throws CmsAiException if the translation does not match the text node count
         */
        public String setTranslatedString(String translation) throws CmsAiException {

            String result = null;
            if (translation != null) {
                List<String> translatioStrings = splitTranslateString(translation);
                if (translatioStrings.size() != m_textNodes.size()) {
                    throw new CmsAiException("Translation does not contain the same number of text nodes.");
                }
                for (int i = 0; i < m_textNodes.size(); i++) {
                    TextNode textNode = m_textNodes.get(i);
                    textNode.text(translatioStrings.get(i));
                }
                result = toString();
            }
            return result;
        }

        /**
         * Returns a string representation of the HTML of the parsed document.<p>
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {

            if (m_doc == null) {
                return null;
            }

            return m_isFullDocument ? m_doc.outerHtml() : m_doc.body().children().outerHtml();
        }

        /**
         * Returns a concatenated string with markers between text nodes.<p>
         *
         * @return the translation input string
         */
        public String toTranslateString() {

            if (m_translateString == null) {
                if (m_textNodes != null) {
                    StringBuilder builder = new StringBuilder();
                    int markerId = 0;
                    for (int i = 0; i < m_textNodes.size(); i++) {
                        TextNode textNode = m_textNodes.get(i);
                        builder.append(textNode.text());
                        if (i < (m_textNodes.size() - 1)) {
                            builder.append("⟦#");
                            builder.append(markerId);
                            builder.append("#⟧");
                        }
                        markerId++;
                    }
                    m_translateString = builder.toString();
                }
            }
            return m_translateString;
        }
    }

    private class FoundOrCreatedValue {

        private I_CmsXmlContentValue m_value;
        private boolean m_created;

        public FoundOrCreatedValue(I_CmsXmlContentValue value, boolean created) {

            m_value = value;
            m_created = created;
        }

        public I_CmsXmlContentValue getValue() {

            return m_value;
        }

        public boolean wasCreated() {

            return m_created;
        }
    }

    /** The XML content types that are considered translatable. */
    public static final List<String> translatedTypes = new ArrayList<String>(
        List.of(
            org.opencms.xml.types.CmsXmlStringValue.TYPE_NAME,
            org.opencms.xml.types.CmsXmlPlainTextStringValue.TYPE_NAME,
            org.opencms.xml.types.CmsXmlHtmlValue.TYPE_NAME));

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsAiTranslator.class);

    /** The AI provider configuration. */
    private CmsAiProviderConfig m_providerConfig;

    /** The current users OpenCms context.  */
    private CmsObject m_cms;

    /** The CmsXmlContent to translate. */
    private CmsXmlContent m_xmlContent;

    /**
     * Creates a new translator for the given provider configuration.<p>
     *
     * @param config the provider configuration
     */
    public CmsAiTranslator(CmsObject cms, CmsAiProviderConfig config, CmsXmlContent xmlContent) {

        m_cms = cms;
        m_providerConfig = config;
        m_xmlContent = xmlContent;
    }

    /**
     * Returns true if the given text contains HTML markup.<p>
     *
     * @param text the text to check
     *
     * @return true if markup is detected
     */
    public static boolean hasHtmlMarkup(String text) {

        if (text == null) {
            return false;
        }

        int lt = text.indexOf('<');
        while (lt >= 0) {
            int gt = text.indexOf('>', lt + 1);
            if (gt > (lt + 1)) {
                char c = text.charAt(lt + 1);
                if ((c == '/') || Character.isLetter(c)) {
                    return true;
                }
            }
            lt = text.indexOf('<', lt + 1);
        }

        return false;
    }

    /**
     * Parses HTML text and collects its text nodes.<p>
     *
     * @param text the input text
     *
     * @return the parse result
     */
    public static HtmlParseResult parseHtmlTextNodes(String text) {

        String input = text == null ? "" : text;
        String trimmed = input.trim().toLowerCase(Locale.ROOT);
        boolean isFullDocument = trimmed.startsWith("<!doctype")
            || trimmed.contains("<html")
            || trimmed.contains("<body");
        Document doc = isFullDocument ? Jsoup.parse(input) : Jsoup.parseBodyFragment(input);
        List<TextNode> textNodes = collectTextNodes(doc);
        return new HtmlParseResult(doc, textNodes, isFullDocument);
    }

    /**
     * Parses the translation response JSON into a map of xpath to text.<p>
     *
     * @param jsonText the JSON text to parse
     *
     * @return the translated values mapped by id
     *
     * @throws JSONException if the JSON is invalid
     */
    public static Map<String, String> parseTranslationResult(String jsonText) throws JSONException {

        Map<String, String> result = null;
        JSONArray segments = null;

        if (jsonText != null) {
            int jsonStart = jsonText.indexOf('{');
            int jsonEnd = jsonText.lastIndexOf('}');
            if ((jsonStart >= 0) && (jsonEnd >= jsonStart)) {
                jsonText = jsonText.substring(jsonStart, jsonEnd + 1);
            }
            JSONObject json = new JSONObject(jsonText);
            segments = json.getJSONArray("segments");
        }

        if (segments != null) {
            result = new HashMap<String, String>();
            for (int i = 0; i < segments.length(); i++) {
                JSONObject segment = segments.getJSONObject(i);
                String id = segment.getString("id");
                String text = segment.getString("text");
                result.put(id, text);
            }
        }

        return result;
    }

    /**
     * Collects all TextNodes from a Jsoup HTML document.<p>
     *
     * @param root the root node where to start collection from
     *
     * @return the collected TextNodes
     */
    private static List<TextNode> collectTextNodes(Node root) {

        List<TextNode> result = new ArrayList<>();
        if (root == null) {
            return result;
        }

        Deque<Node> stack = new ArrayDeque<>();
        stack.push(root);

        while (!stack.isEmpty()) {
            Node node = stack.pop();

            if (node instanceof TextNode) {
                TextNode textNode = (TextNode)node;
                if (!textNode.text().trim().isEmpty()) {
                    result.add(textNode);
                }
            }

            List<Node> children = node.childNodes();
            for (int i = children.size() - 1; i >= 0; i--) {
                stack.push(children.get(i));
            }
        }

        return result;
    }

    /**
     * Collects the translatable XML values for the given locale.<p>
     *
     * @param xmlContent the XML content
     * @param locale the locale to read
     *
     * @return the translatable values
     */
    public List<I_CmsXmlContentValue> getXmlValues(Locale locale) {

        List<I_CmsXmlContentValue> result = new ArrayList<I_CmsXmlContentValue>();

        if (m_xmlContent != null) {
            result = m_xmlContent.getValuesInDocumentOrder(locale).stream().filter(
                val -> translatedTypes.contains(val.getTypeName())).collect(Collectors.toList());
        }

        return result;
    }

    public CmsXmlContent translateXmlContent(Locale srcLocale, Locale targetLocale)
    throws JSONException, CmsXmlException, CmsAiException {

        return translateXmlContent(srcLocale, targetLocale, null);
    }

    /**
     * Translates XML content and applies the results to the target locale.<p>
     *
     * @param srcLocale the source locale
     * @param targetLocale the target locale
     *
     * @return the updated XML content
     *
     * @throws JSONException if the JSON request or response is invalid
     * @throws CmsXmlException in case of problems accessing the XML content
     * @throws CmsAiException if the AI translation result does not match the required structure
     */
    public CmsXmlContent translateXmlContent(
        Locale srcLocale,
        Locale targetLocale,
        StreamingChatResponseHandler handler)
    throws JSONException, CmsXmlException, CmsAiException {

        String jsonText = translateXmlContentRaw(srcLocale, targetLocale, handler);
        if (jsonText == null) {
            return null;
        }

        Map<String, String> translationResult = parseTranslationResult(jsonText);
        if (!m_xmlContent.hasLocale(targetLocale)) {
            if (translationResult.size() > 0) {
                m_xmlContent.copyLocale(srcLocale, targetLocale);

                for (Map.Entry<String, String> entry : translationResult.entrySet()) {
                    String xpath = entry.getKey();
                    String text = entry.getValue();
                    I_CmsXmlContentValue sval = m_xmlContent.getValue(xpath, srcLocale);
                    String source = sval.getStringValue(m_cms);
                    if (hasHtmlMarkup(source)) {
                        HtmlParseResult parsed = parseHtmlTextNodes(source);
                        text = parsed.setTranslatedString(text);
                    }
                    I_CmsXmlContentValue tval = m_xmlContent.getValue(xpath, targetLocale);
                    tval.setStringValue(m_cms, text);
                }
            }
        } else {
            for (Map.Entry<String, String> entry : translationResult.entrySet()) {
                try {
                    String xpath = entry.getKey();
                    String text = entry.getValue();
                    I_CmsXmlContentValue sval = m_xmlContent.getValue(xpath, srcLocale);
                    String source = sval.getStringValue(m_cms);
                    if (hasHtmlMarkup(source)) {
                        HtmlParseResult parsed = parseHtmlTextNodes(source);
                        text = parsed.setTranslatedString(text);
                    }
                    FoundOrCreatedValue val = findOrCreateValue(m_cms, m_xmlContent, targetLocale, entry.getKey());
                    if (val != null) {
                        // If the value already existed, we only want to write to it if it's empty.
                        // But if it was just created, it might have a default value, which we need to overwrite.
                        if (val.wasCreated()) {
                            val.getValue().setStringValue(m_cms, text);
                        } else {
                            if (CmsStringUtil.isEmptyOrWhitespaceOnly(val.getValue().getStringValue(m_cms))) {
                                val.getValue().setStringValue(m_cms, text);
                            }
                        }
                    }
                } catch (Exception e) {
                    LOG.debug(e.getLocalizedMessage(), e);

                }
            }
        }

        return m_xmlContent;
    }

    /**
     * Translates XML content with the AI provider and returns the raw response.<p>
     *
     * @param srcLocale the source locale
     * @param targetLocale the target locale
     *
     * @return the raw AI response
     *
     * @throws JSONException in the unlikely case of problems generating a JSON object for the translation request
     */
    protected String translateXmlContentRaw(Locale srcLocale, Locale targetLocale, StreamingChatResponseHandler handler)
    throws JSONException {

        String result = null;

        List<I_CmsXmlContentValue> xmlValues = getXmlValues(srcLocale);
        AtomicBoolean cancelled = new AtomicBoolean(Boolean.FALSE);
        if (xmlValues.size() > 0) {

            JSONObject root = new JSONObject();
            JSONArray segments = new JSONArray();

            root.put("source_language", srcLocale.toString());
            root.put("target_language", targetLocale.toString());

            String textToTranslate = "";
            for (I_CmsXmlContentValue val : xmlValues) {
                String strVal = val.getStringValue(m_cms);
                strVal = CmsAiTranslator.parseHtmlTextNodes(strVal).toTranslateString();
                segments.put(new JSONObject().put("id", val.getPath()).put("text", strVal));
            }

            root.put("segments", segments);
            textToTranslate = root.toString(2);

            final String llmPrompt = String.join(
                "\n",
                "You are a professional translation engine.",
                "You will receive a JSON object with the following structure:",
                "- source_language: the source language code",
                "- target_language: the target language code",
                "- segments: an array of objects",
                "  - id: a unique identifier",
                "  - text: the text to translate",
                "Your task:",
                "- Translate ONLY the value of segments[].text from source_language to target_language.",
                "- Preserve meaning, tone, and grammar.",
                "- Do NOT translate ids.",
                "- Do NOT change, remove, reorder, or add any segments.",
                "- Do NOT add explanations, comments, or extra fields.",
                "- Keep placeholders, markers, or tokens (e.g. ⟦#1#⟧, {{...}}, ${...}, [TAG1], etc.) intact.");

            ResponseFormat.Builder schema = ResponseFormat.builder().type(ResponseFormatType.JSON);
            JsonObjectSchema jsonSchema = JsonObjectSchema.builder().build();
            JsonRawSchema raw = JsonRawSchema.from(llmPrompt);

            ResponseFormat llmResponseFormat = ResponseFormat.builder().type(ResponseFormatType.JSON).jsonSchema(
                JsonSchema.builder().name("TranslationResult").rootElement(
                    JsonObjectSchema.builder().addProperty(
                        "segments",
                        JsonArraySchema.builder().items(
                            JsonObjectSchema.builder().addStringProperty(
                                "id",
                                "The id for the text, must remain unchanged and kept in order").addStringProperty(
                                    "text",
                                    "The text to translate with optional placeholders like ⟦#1#⟧").required(
                                        "id",
                                        "text").build()).build()).required("segments").build()).build()).build();

            List<ChatMessage> messages = new ArrayList<ChatMessage>();
            messages.add(SystemMessage.from(llmPrompt));
            messages.add(UserMessage.from(textToTranslate));

            ChatRequest llmRequest = ChatRequest.builder().messages(messages).responseFormat(llmResponseFormat).build();

            if (handler == null) {
                ChatModel chatModel = new CmsAiModel(m_providerConfig).getChatModel();
                ChatResponse response = chatModel.chat(llmRequest);
                result = response.aiMessage().text();
            } else {

                StreamingChatModel chatModel = new CmsAiModel(m_providerConfig).getStreamingChatModel();

                final java.util.concurrent.CountDownLatch COUNTDOWN = new java.util.concurrent.CountDownLatch(1);
                AtomicReference<String> resultRef = new AtomicReference<String>();
                StringBuilder partialBuffer = new StringBuilder();

                chatModel.chat(llmRequest, new StreamingChatResponseHandler() {

                    public void onCompleteResponse(ChatResponse response) {

                        String text = response.aiMessage().text();
                        resultRef.set(text != null ? text : partialBuffer.toString());
                        handler.onCompleteResponse(response);
                        COUNTDOWN.countDown();
                    }

                    public void onError(Throwable error) {

                        handler.onError(error);
                        COUNTDOWN.countDown();
                    }

                    public void onPartialResponse(PartialResponse responsePart, PartialResponseContext context) {

                        String responsePartText = responsePart.text();
                        if (responsePartText != null) {
                            partialBuffer.append(responsePartText);
                        }
                        handler.onPartialResponse(responsePart, context);
                        if (context.streamingHandle().isCancelled()) {
                            cancelled.set(true);
                        }
                    }
                });

                // Block this request thread until streaming finishes
                try {
                    // safety timeout: 10 minutes
                    COUNTDOWN.await(10, java.util.concurrent.TimeUnit.MINUTES);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                result = resultRef.get();
                if ((result == null) && !cancelled.get() && (partialBuffer.length() > 0)) {
                    result = partialBuffer.toString();
                }
            }
        }
        return result;
    }

    /**
     * Helper method to either find an existing value for a given xpath or to try and create it, along with its parent values if necessary.
     *
     * <p>The result object contains both the value and an indication whether it was created or found.
     *
     * <p>Note that this method will fail with an exception when the value can neither found nor created. This can happen if its creation
     * would conflict with pre-existing values in the content, e.g. choice values with different choices than that indicated by the given path.
     *
     * @param cms the CMS context
     * @param content the XML content
     * @param locale the locale in which to find/create the value
     * @param path the xpath of the value to find/create
     * @return the result, containing both the value and a flag which indicates whether a pre-existing value was found or whether it had to be created
     *
     * @throws Exception if something goes wrong
     */
    private FoundOrCreatedValue findOrCreateValue(CmsObject cms, CmsXmlContent content, Locale locale, String path)
    throws Exception {

        I_CmsXmlContentValue value = content.getValue(path, locale);
        if (value != null) {
            return new FoundOrCreatedValue(value, false);
        }
        I_CmsXmlContentValue parent = null;
        boolean isMultipleChoice = false;
        if (CmsXmlUtils.isDeepXpath(path)) {

            String parentPath = CmsXmlUtils.removeLastXpathElement(path);
            parent = findOrCreateValue(cms, content, locale, parentPath).getValue();
            isMultipleChoice = ((CmsXmlNestedContentDefinition)parent).getChoiceMaxOccurs() > 1;

            // Maybe it got created by creating a parent value?
            value = content.getValue(path, locale);
            if (value != null) {
                return new FoundOrCreatedValue(value, true);
            }
        }

        int index = CmsXmlUtils.getXpathIndexInt(path); // 1-based
        int numValues = 0;
        if (isMultipleChoice) {
            while ((numValues = content.getValues(path, locale).size()) < index) {
                content.addValue(cms, path, locale, content.getSubValues(parent.getPath(), locale).size());
            }
        } else {
            while ((numValues = content.getValues(path, locale).size()) < index) {
                content.addValue(cms, path, locale, numValues);
            }
        }
        value = content.getValue(path, locale);
        if (value != null) {
            return new FoundOrCreatedValue(value, true);
        } else {
            throw new RuntimeException("failed to find/create value " + path + " for unknown reasons.");
        }
    }

}
