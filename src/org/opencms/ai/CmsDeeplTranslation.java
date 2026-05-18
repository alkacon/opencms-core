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

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.contenteditor.I_CmsContentTranslator;
import org.opencms.ai.CmsTranslationUtil.FoundOrCreatedValue;
import org.opencms.configuration.CmsConfigurationException;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.I_CmsXmlContentAugmentation;
import org.opencms.xml.types.CmsXmlHtmlValue;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.logging.Log;

import com.deepl.api.DeepLClient;
import com.deepl.api.DeepLClientOptions;
import com.deepl.api.Language;
import com.deepl.api.TextResult;
import com.deepl.api.TextTranslationOptions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class CmsDeeplTranslation implements I_CmsContentTranslator {

    /** Key used for looking up the API key in the secret store. */
    public static final String SECRET_API_KEY = "contenteditor.translation.deepl.apiKey";

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDeeplTranslation.class);

    /** Configuration parameter for determining a target locale mapping. */
    protected static final Object PARAM_TARGET_LOCALE_MAPPING = "targetLocaleMapping";

    /** Cached available target languages. */
    private volatile List<Language> m_targetLanguages;

    /** Cached available source languages. */
    private volatile List<Language> m_sourceLanguages;

    /** The parameters from the configuration. */
    private CmsParameterConfiguration m_params = new CmsParameterConfiguration();

    /** The parsed target locale mapping. */
    private volatile Map<Locale, Locale> m_targetLocaleMapping;

    /**
     * Tries to find a suitable Language object from a list of DeepL-supported languages that matches a given locale.
     *
     * @param locale the locale
     * @param languages the languages
     * @return the matching language
     */
    public static Language getMatchingLanguage(Locale locale, List<Language> languages) {

        for (Language language : languages) {
            if (Locale.forLanguageTag(language.getCode()).equals(locale)) {
                return language;
            }
        }
        // if not found, fall back to just using the language portion
        for (Language language : languages) {
            Locale l1 = new Locale(Locale.forLanguageTag(language.getCode()).getLanguage());
            Locale l2 = new Locale(locale.getLanguage());
            if (l1.equals(l2)) {
                return language;
            }
        }
        return null;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    @Override
    public void addConfigurationParameter(String paramName, String paramValue) {

        m_params.add(paramName, paramValue);

    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    @Override
    public CmsParameterConfiguration getConfiguration() {

        return m_params;
    }

    /**
     * @see org.opencms.ade.contenteditor.I_CmsContentTranslator#getContentAugmentation()
     */
    @Override
    public I_CmsXmlContentAugmentation getContentAugmentation() {

        return new I_CmsXmlContentAugmentation() {

            @Override
            public void augmentContent(Context context) throws Exception {

                CmsObject cms = context.getCmsObject();
                final Locale wpLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
                CmsXmlContent content = context.getContent();
                Locale sourceLocale = context.getLocale();
                String targetLocaleParam = context.getParameter(CmsGwtConstants.PARAM_TARGET_LOCALE);
                String apiKey = OpenCms.getSecretStore().getSecret(SECRET_API_KEY);
                apiKey = apiKey.trim();
                Locale targetLocale = CmsLocaleManager.getLocale(targetLocaleParam);

                DeepLClientOptions options = new DeepLClientOptions();
                DeepLClient client = new DeepLClient(apiKey, options);

                String html = CmsTranslationUtil.getWaitMessage(wpLocale);
                context.progress(html);

                List<Language> sourceLanguages = getSourceLanguages(client);
                List<Language> targetLanguages = getTargetLanguages(client);
                Language srcLang = getMatchingLanguage(sourceLocale, sourceLanguages);
                Language targetLang = getMatchingLanguage(
                    getTargetLocaleMapping().getOrDefault(targetLocale, targetLocale),
                    targetLanguages);

                List<I_CmsXmlContentValue> values = CmsTranslationUtil.getValuesToTranslate(
                    cms,
                    content,
                    sourceLocale,
                    targetLocale);

                // DeepL can translate multiple strings in one API call. But the total request size is limited,
                // and also all strings in that API call are translated with the same translation options.
                // So we first group the values to be translated by whether they are HTML values or not
                // (This is because when we use HTML mode for non-HTML values, it may replace <, > with entities.).
                // Then we split each of these two groups into smaller batches which can be translated in one API call each.

                Multimap<Boolean, I_CmsXmlContentValue> valuesByType = ArrayListMultimap.create();
                for (I_CmsXmlContentValue value : values) {
                    valuesByType.put(value instanceof CmsXmlHtmlValue, value);
                }

                // Collect *all* translation results in this map
                Map<String, String> valuesToSet = new HashMap<>();

                for (Boolean isHtml : Arrays.asList(Boolean.FALSE, Boolean.TRUE)) {
                    TextTranslationOptions translationOptions = new TextTranslationOptions();
                    if (isHtml) {
                        translationOptions.setTagHandlingVersion("v2");
                        translationOptions.setTagHandling("html");
                    }
                    Collection<I_CmsXmlContentValue> valuesForCurrentMode = valuesByType.get(isHtml);
                    if (valuesForCurrentMode.isEmpty()) {
                        continue;
                    }
                    List<List<I_CmsXmlContentValue>> batches = new ArrayList<>();

                    // Max request size for the API is 128K, but this also includes JSON punctuation and additional parameters, so we leave a wide safety margin
                    final int limit = 15;
                    int currentBatchSize = 0;
                    List<I_CmsXmlContentValue> currentBatch = new ArrayList<>();
                    batches.add(currentBatch);
                    for (I_CmsXmlContentValue val : valuesForCurrentMode) {
                        String strValue = getTranslationValue(cms, val);
                        long byteSize = strValue.getBytes(StandardCharsets.UTF_8).length;
                        if ((byteSize + currentBatchSize) > limit) {
                            currentBatch = new ArrayList<>();
                            batches.add(currentBatch);
                            currentBatchSize = 0;
                        }
                        currentBatch.add(val);
                        currentBatchSize += byteSize;
                    }

                    for (List<I_CmsXmlContentValue> batch : batches) {
                        if (batch.size() == 0) {
                            continue;
                        }

                        List<String> inputs = batch.stream().map(val -> getTranslationValue(cms, val)).collect(
                            Collectors.toList());
                        List<TextResult> outputs = client.translateText(
                            inputs,
                            srcLang,
                            targetLang,
                            translationOptions);
                        for (int i = 0; i < batch.size(); i++) {
                            valuesToSet.put(batch.get(i).getPath(), outputs.get(i).getText());
                        }
                    }
                }
                int numTranslatedFields = 0;

                if (!content.hasLocale(targetLocale)) {
                    content.copyLocale(sourceLocale, targetLocale);
                    for (Map.Entry<String, String> entry : valuesToSet.entrySet()) {
                        I_CmsXmlContentValue targetValue = content.getValue(entry.getKey(), targetLocale);
                        setTranslationValue(cms, targetValue, entry.getValue());
                        numTranslatedFields += 1;
                    }
                } else {
                    for (Map.Entry<String, String> entry : valuesToSet.entrySet()) {
                        I_CmsXmlContentValue origValue = content.getValue(entry.getKey(), sourceLocale);
                        try {
                            FoundOrCreatedValue val = CmsTranslationUtil.findOrCreateValue(
                                cms,
                                content,
                                targetLocale,
                                origValue.getPath());
                            // If the value already existed, we only want to write to it if it's empty.
                            // But if it was just created, it might have a default value, which we need to overwrite.
                            if (val.wasCreated()
                                || CmsStringUtil.isEmptyOrWhitespaceOnly(val.getValue().getStringValue(cms))) {
                                if (val.getValue() instanceof CmsXmlHtmlValue) {
                                    // for HTML values, we need to copy the old value first so the link table is filled
                                    val.getValue().setStringValue(cms, origValue.getStringValue(cms));
                                }
                                setTranslationValue(cms, val.getValue(), entry.getValue());
                                numTranslatedFields += 1;
                            }
                        } catch (Exception e) {
                            LOG.debug(e.getLocalizedMessage(), e);
                        }
                    }
                }

                if (numTranslatedFields > 0) {
                    context.setResult(content);
                    context.setHtmlMessage(buildFeedbackHtml(cms, sourceLocale, targetLocale, numTranslatedFields));
                    context.setNextLocale(targetLocale);
                } else {
                    String nothingTranslated = Messages.get().getBundle(wpLocale).key(
                        Messages.GUI_TRANSLATION_NOTHING_TRANSLATED_0);
                    context.setHtmlMessage("<p>" + nothingTranslated + "</p>");
                }

            }

        };

    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    @Override
    public void initConfiguration() throws CmsConfigurationException {

    }

    @Override
    public void initialize(CmsObject cms) {

    }

    /**
     * @see org.opencms.ade.contenteditor.I_CmsContentTranslator#isEnabled(org.opencms.file.CmsObject, org.opencms.ade.configuration.CmsADEConfigData, org.opencms.file.CmsFile)
     */
    @Override
    public boolean isEnabled(CmsObject cms, CmsADEConfigData config, CmsFile file) {

        return OpenCms.getSecretStore().getSecret(SECRET_API_KEY) != null;

    }

    /**
     * Gets (and lazily creates if necessary) the target locale mapping.
     *
     * <p>DeepL API only lists en-US / en-GB  for English, and pt-BR / pt-PT for Portuguese as target languages.
     * Using just en or pt might work, but it's probably a good idea to be specific,
     * So if just en or pt is specified, we map it to the variant that's more widely used by default.
     *
     * <p>This can be overriden by the configuration parameter targetLocaleMapping, which has the form l1:r1|l2:r2|..., where the li is
     * the locale to map, and the ri is the locale to map to.
     * @return
     */
    protected Map<Locale, Locale> getTargetLocaleMapping() {

        if (m_targetLocaleMapping == null) {
            Map<Locale, Locale> targetLocaleMapping = new HashMap<>();
            targetLocaleMapping.put(Locale.ENGLISH, Locale.US);
            targetLocaleMapping.put(new Locale("pt"), Locale.forLanguageTag("pt-PT"));
            String localeMappingStr = m_params.get(PARAM_TARGET_LOCALE_MAPPING);
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(localeMappingStr)) {
                Map<String, String> mappings = CmsStringUtil.splitAsMap(localeMappingStr.trim(), "|", ":");
                for (Map.Entry<String, String> entry : mappings.entrySet()) {
                    targetLocaleMapping.put(
                        LocaleUtils.toLocale(entry.getKey()),
                        LocaleUtils.toLocale(entry.getValue()));
                }
            }
            m_targetLocaleMapping = targetLocaleMapping;
        }

        return m_targetLocaleMapping;
    }

    /**
     * Builds the HTML for the feedback screen.
     *
     * @param cms the CMS context
     * @param sourceLocale the translation source locale
     * @param targetLocale the translation target locale
     * @param numSuccessfulFieldUpdates the number of translated fields
     * @param conflictFields the list of fields with conflicts
     * @return
     */
    private String buildFeedbackHtml(
        CmsObject cms,
        Locale sourceLocale,
        Locale targetLocale,
        int numSuccessfulFieldUpdates) {

        StringBuilder buffer = new StringBuilder();
        Locale wpLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        buffer.append("<p>");
        buffer.append(
            Messages.get().getBundle(wpLocale).key(
                Messages.GUI_TRANSLATION_FEEDBACK_3,
                numSuccessfulFieldUpdates,
                sourceLocale.getDisplayName(wpLocale),
                targetLocale.getDisplayName(wpLocale)));
        buffer.append("</p>");
        return buffer.toString();
    }

    /**
     * Gets the available source languages from the client, but caches them for later calls.
     *
     * @param client the DeepL client
     * @return the available source languages
     */
    private List<Language> getSourceLanguages(DeepLClient client) {

        if (m_sourceLanguages == null) {
            try {
                m_sourceLanguages = client.getSourceLanguages();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return m_sourceLanguages;
    }

    /**
     * Gets the available target languages from the DeepL client, but caches them for later calls
     *
     * @param client the DeepL client
     * @return the available target languages
     */
    private List<Language> getTargetLanguages(DeepLClient client) {

        if (m_targetLanguages == null) {
            try {
                m_targetLanguages = client.getTargetLanguages();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return m_targetLanguages;

    }

    /**
     * Gets the value to translate, which in the case of HTML values is the value with unexpanded macros, and the normal string value otherwise.
     *
     * @param cms the CMS context
     * @param value an XML content value
     *
     * @return the value to translate
     */
    private String getTranslationValue(CmsObject cms, I_CmsXmlContentValue value) {

        if (value instanceof CmsXmlHtmlValue) {
            return ((CmsXmlHtmlValue)value).getRawContent();
        } else {
            return value.getStringValue(cms);
        }
    }

    /**
     * Sets the translated value, which in the case of HTML values sets just the content node of the value (with unexpanded macros), and sets the value normally otherwise.
     *
     * @param cms the CMS context
     * @param value the value to modify
     * @param newValue
     * @param newValue
     */
    private void setTranslationValue(CmsObject cms, I_CmsXmlContentValue value, String newValue) {

        if (value instanceof CmsXmlHtmlValue) {
            ((CmsXmlHtmlValue)value).setRawContent(newValue);
        } else {
            value.setStringValue(cms, newValue);
        }

    }

}
