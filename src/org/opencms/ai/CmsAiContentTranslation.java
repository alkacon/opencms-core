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
import org.opencms.configuration.CmsConfigurationException;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsLog;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.I_CmsXmlContentAugmentation;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;

import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialResponse;
import dev.langchain4j.model.chat.response.PartialResponseContext;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;

/**
 * LLM implementation of the I_CmsContentTranslator interface.
 *
 * <p>Reads the LLM credentials from a JSON file whose file system path is passed via the 'config' configuration parameter.
 */
public class CmsAiContentTranslation implements I_CmsContentTranslator {

    public class Augmentation implements I_CmsXmlContentAugmentation {

        @Override
        public void augmentContent(Context context) throws Exception {

            CmsObject cms = context.getCmsObject();
            CmsXmlContent content = context.getContent();
            Locale sourceLocale = context.getLocale();
            String targetLocaleParam = context.getParameter(CmsGwtConstants.PARAM_TARGET_LOCALE);
            Locale targetLocale = CmsLocaleManager.getLocale(targetLocaleParam);

            CmsAiTranslator translator = new CmsAiTranslator(cms, m_providerConfig, content);
            AtomicInteger charsReceived = new AtomicInteger();
            AtomicReference<Throwable> errorRef = new AtomicReference<Throwable>();

            CmsXmlContent result = translator.translateXmlContent(
                sourceLocale,
                targetLocale,
                new StreamingChatResponseHandler() {

                    @Override
                    public void onCompleteResponse(ChatResponse completeResponse) {

                    }

                    @Override
                    public void onError(Throwable error) {

                        errorRef.set(error);
                    }

                    @Override
                    public void onPartialResponse(PartialResponse partialResponse, PartialResponseContext resContext) {

                        charsReceived.addAndGet(partialResponse.text().length());
                        if (context.isAborted()) {
                            resContext.streamingHandle().cancel();
                        } else {
                            context.progress("Translation progress: " + charsReceived.get());
                        }
                    }
                });
            if (errorRef.get() != null) {
                throw (Exception)errorRef.get();
            }
            if (result != null) {
                context.setResult(result);
                context.setNextLocale(targetLocale);
            }
        }

    }

    /** Parameter from which the config file path is read. */
    public static final String PARAM_CONFIG_FILE = "config";

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsAiContentTranslation.class);

    /** The configuration parameters. */
    private CmsParameterConfiguration m_config = new CmsParameterConfiguration();

    /** The parsed provider configuration. */
    private CmsAiProviderConfig m_providerConfig;

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    @Override
    public void addConfigurationParameter(String paramName, String paramValue) {

        m_config.add(paramName, paramValue);

    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    @Override
    public CmsParameterConfiguration getConfiguration() {

        return m_config;
    }

    /**
     * @see org.opencms.ade.contenteditor.I_CmsContentTranslator#getContentAugmentation()
     */
    @Override
    public I_CmsXmlContentAugmentation getContentAugmentation() {

        return new Augmentation();
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    @Override
    public void initConfiguration() throws CmsConfigurationException {

        String configPath = m_config.getString(PARAM_CONFIG_FILE, null);
        try {
            m_providerConfig = CmsAiProviderConfig.loadFromJsonFile(configPath);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * @see org.opencms.ade.contenteditor.I_CmsContentTranslator#isEnabled(org.opencms.file.CmsObject, org.opencms.ade.configuration.CmsADEConfigData, org.opencms.file.CmsFile)
     */
    @Override
    public boolean isEnabled(CmsObject cms, CmsADEConfigData config, CmsFile file) {

        return m_providerConfig != null;
    }

}
