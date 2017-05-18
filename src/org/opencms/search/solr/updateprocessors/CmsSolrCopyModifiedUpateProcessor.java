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

package org.opencms.search.solr.updateprocessors;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;

/**
 * Implementation of an {@link UpdateRequestProcessor} that
 * <ul>
 *  <li>reads a field's (source) value,</li>
 *  <li>applies a regex replacement,</li>
 *  <li>adds a field (target) with the modified value.</li>
 * </ul>
 *
 *  @see CmsSolrCopyModifiedUpateProcessorFactory
 */
public class CmsSolrCopyModifiedUpateProcessor extends UpdateRequestProcessor {

    /** The source field where the original value is read from. */
    private final String m_source;
    /** The target field where the modified value is written to. */
    private final String m_target;
    /** The regular expression that the original value is matched to. */
    private final Pattern m_regex;
    /** The replacement for the matches of the regex on the original value. */
    private final String m_replacement;

    /**
     * Default constructor.
     * @param source the name of the source field (where the original value is read from).
     * @param target the name of the target field (where the modified value is written to).
     * @param regex the regex applied to the original value.
     * @param replacement the replacement for the matched parts in the original value.
     * @param nextProcessor the {@link UpdateRequestProcessor} to process next.
     */
    public CmsSolrCopyModifiedUpateProcessor(
        @Nonnull String source,
        @Nonnull String target,
        @Nonnull Pattern regex,
        @Nonnull String replacement,
        UpdateRequestProcessor nextProcessor) {
        super(nextProcessor);
        m_source = source;
        m_target = target;
        m_regex = regex;
        m_replacement = replacement;
    }

    /**
     * @see org.apache.solr.update.processor.UpdateRequestProcessor#processAdd(org.apache.solr.update.AddUpdateCommand)
     */
    @Override
    public void processAdd(AddUpdateCommand cmd) throws IOException {

        SolrInputDocument doc = cmd.getSolrInputDocument();

        String v = (String)doc.getFieldValue(m_source);
        if (v != null) {
            String v1 = m_regex.matcher(v).replaceAll(m_replacement);
            doc.addField(m_target, v1);
        }

        // pass it up the chain
        super.processAdd(cmd);
    }
}
