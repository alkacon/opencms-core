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

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

/**
 * Provides a wrapper for an AI model with provider configuration.
 *
 * @since 21.0.0
 */
public class CmsAiModel {

    /** The AI provider configuration. */
    private CmsAiProviderConfig m_config;

    /** The AI model. */
    private ChatModel m_model;

    /** The streaming AI model. */
    private StreamingChatModel m_streamingModel;

    /** The basic prompt for the LLM. */
    private SystemMessage m_llmPrompt;

    /** The (optional) LLM response format. */
    private ResponseFormat m_llmResponseFormat;

    /**
     * Generate a new AI model wrapper.
     *
     * @param config the AI provider configuration
     */
    public CmsAiModel(CmsAiProviderConfig config) {

        m_config = config;
    }

    /**
     * Returns a chat model initialized with the provider configuration.<p>
     *
     * @return  a chat model initialized with the provider configuration
     */
    public ChatModel getChatModel() {

        if (m_model == null) {
            m_model = OpenAiChatModel.builder().apiKey(m_config.getApiKey()).baseUrl(
                m_config.getProviderUrl()).modelName(m_config.getModelName()).build();
        }
        return m_model;
    }

    /**
     * Returns a streaming chat model initialized with the provider configuration.<p>
     *
     * @return a streaming chat model initialized with the provider configuration
     */
    public StreamingChatModel getStreamingChatModel() {

        if (m_streamingModel == null) {
            m_streamingModel = OpenAiStreamingChatModel.builder().apiKey(m_config.getApiKey()).baseUrl(
                m_config.getProviderUrl()).modelName(m_config.getModelName()).build();
        }
        return m_streamingModel;
    }
}
