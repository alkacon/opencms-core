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
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.content.CmsSynchronizationSpec;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.I_CmsXmlContentHandler;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;

public class CmsTranslationUtil {

    public static class FoundOrCreatedValue {

        private boolean m_created;
        private I_CmsXmlContentValue m_value;

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

    private static final Log LOG = CmsLog.getLog(CmsTranslationUtil.class);

    /** Metadata tag to disable translation for a content field. */
    public static final String AGENT_TAG_NOTRANSLATE = "translate=false";

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
    public static FoundOrCreatedValue findOrCreateValue(
        CmsObject cms,
        CmsXmlContent content,
        Locale locale,
        String path)
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

    /**
     * Collects the translatable XML values for the given locale.<p>
     *
     * @param xmlContent the XML content
     * @param locale the locale to read
     *
     * @return the translatable values
     */
    public static List<I_CmsXmlContentValue> getValuesToTranslate(
        CmsObject cms,
        CmsXmlContent m_xmlContent,
        Locale locale,
        Locale targetLocale) {

        List<I_CmsXmlContentValue> result = new ArrayList<I_CmsXmlContentValue>();

        if (m_xmlContent != null) {
            List<I_CmsXmlContentValue> valuesInOrder = m_xmlContent.getValuesInDocumentOrder(locale).stream().filter(
                val -> translatedTypes.contains(val.getTypeName())).filter(
                    val -> !CmsStringUtil.isEmptyOrWhitespaceOnly(val.getStringValue(cms))).filter(
                        val -> !isTranslationDisabledInSchema(val)).collect(Collectors.toList());

            // Now find those values which we could actually write to if we translated them
            if ((targetLocale == null) || !m_xmlContent.hasLocale(targetLocale)) {
                // if the content doesn't already have the locale, translation involves copying the source to the target locale first,
                // which means we can write all the values from the source locale
                result = valuesInOrder;
            } else {
                // if the content does have the target locale, we can just try creating the values corresponding to the xpaths from the
                // source locale. It doesn't matter that the values we are setting are not the actual translations, we are just checking
                // if they can be created / written to.
                CmsXmlContent copy = null;
                try {
                    copy = CmsXmlContentFactory.unmarshal(cms, m_xmlContent.getFile());
                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage(), e);
                    return Collections.emptyList();
                }
                for (I_CmsXmlContentValue value : valuesInOrder) {
                    try {
                        FoundOrCreatedValue valueInTargetLocaleInCopy = findOrCreateValue(
                            cms,
                            copy,
                            targetLocale,
                            value.getPath());
                        if (valueInTargetLocaleInCopy.wasCreated()
                            || CmsStringUtil.isEmptyOrWhitespaceOnly(
                                valueInTargetLocaleInCopy.getValue().getStringValue(cms))) {
                            result.add(value);
                        } else {
                            LOG.debug(
                                "Excluding "
                                    + value.getPath()
                                    + " from translation because it would cause a conflict in the target locale."
                                    + targetLocale);
                        }
                    } catch (Exception e) {
                        LOG.info(e.getLocalizedMessage(), e);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Gets the message to display to the user, while content is being translated (in HTML format).
     *
     * @param locale the workplace locale
     * @return the message
     */
    public static String getWaitMessage(final Locale locale) {

        String msg = Messages.get().getBundle(locale).key(Messages.GUI_TRANSLATION_PROGRESS_0);
        String s = "<div class=\"oc-translation-message\">\n"
            + "<div class=\"oc-translation-message-dots\">\n"
            + "<span></span>\n"
            + "<span></span>\n"
            + "<span></span>\n"
            + "</div>\n"
            + "<div>"
            + msg
            + "</div>\n"
            + "</div>\n"
            + "";
        return s;
    }

    /**
     * Checks if translation is disabled by schema settings for a specific content value.
     *
     * @param val the content value
     * @return true if translation is disabled
     */
    public static boolean isTranslationDisabledInSchema(I_CmsXmlContentValue val) {

        // We just use AtomicBoolean here because it's a convenient way of having a boolean mutable from a closure, not because of concurrency
        AtomicBoolean result = new AtomicBoolean(false);
        String path = CmsXmlUtils.removeAllXpathIndices(val.getPath());
        I_CmsXmlDocument content = val.getDocument();
        I_CmsXmlContentHandler rootHandler = content.getContentDefinition().getContentHandler();
        CmsSynchronizationSpec syncSpec = rootHandler.getSynchronizations(true);
        if (syncSpec.getSynchronizationPaths().contains(path)) {
            result.set(true);
            LOG.debug("Excluding " + val.getPath() + " from translation because it's synchronized");
        }
        if (!result.get()) {
            content.getContentDefinition().findSchemaTypesForPath(path, (schemaType, remainingPath) -> {
                remainingPath = CmsXmlUtils.concatXpath(schemaType.getName(), remainingPath);
                I_CmsXmlContentHandler handler = schemaType.getContentDefinition().getContentHandler();
                if (handler.getAgentTags(remainingPath).contains(AGENT_TAG_NOTRANSLATE)) {
                    if (result.compareAndSet(false, true)) {
                        LOG.debug(
                            "Excluding "
                                + val.getPath()
                                + " from translation because it's marked with translate=false");
                    }
                }
            });
        }
        return result.get();
    }

}
