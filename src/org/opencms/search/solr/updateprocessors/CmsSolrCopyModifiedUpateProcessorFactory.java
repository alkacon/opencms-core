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

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;

/**
 * An updated processor that applies a configured regex to any
 * CharSequence values found in the source field, replaces
 * any matches with the configured replacement string, and writes
 * the resulting string to the target field.
 *
 * <p>
 * For example, with the configuration listed below, the sequence in field <code>path</code>
 * will be matched against the regex <code>(.*)_([a-z]{2}(?:_[A-Z]{2})?)((?:\.[^\.]*)?)$</code>,
 * where matched parts will be replaced by <code>$1$3</code>, i.e., the first and third group of the match.
 * The resulting sequence will be written to <code>path_remove_locale</code>.
 * </p>
 *
 * <pre class="prettyprint">
 * &lt;processor class="org.opencms.search.solr.updateprocessors.CmsSolrCopyModifiedUpateProcessorFactory"&gt;
 *   &lt;str name="source"&gt;path&lt;/str&gt;
 *   &lt;str name="target"&gt;path_remove_locale&lt;/str&gt;
 *   &lt;str name="regex"&gt;(.*)_([a-z]{2}(?:_[A-Z]{2})?)((?:\.[^\.]*)?)$&lt;/str&gt;
 *   &lt;str name="replacement"&gt;$1$3&lt;/str&gt;
 * &lt;/processor&gt;</pre>
 *
 * <p>
 * If, e.g., a document with value "document_de.txt" in field <code>source</code> is processed, the field
 * <code>path_remove_locale</code> with value "document.txt will be added.
 * </p>
 *
 * <p>
 * To add the update processor to your installation, define an update processor chain as in the following example.
 * </p>
 *
 * <pre class="prettyprint">
 * &lt;updateRequestProcessorChain name="mychain" default="true"&gt;
 *   &lt;processor class="org.opencms.search.solr.updateprocessors.CmsSolrCopyModifiedUpateProcessorFactory"&gt;
 *     &lt;str name="source"&gt;path&lt;/str&gt;
 *     &lt;str name="target"&gt;path_remove_locale&lt;/str&gt;
 *     &lt;str name="regex"&gt;(.*)_([a-z]{2}(?:_[A-Z]{2})?)((?:\.[^\.]*)?)$&lt;/str&gt;
 *     &lt;str name="replacement"&gt;$1$3&lt;/str&gt;
 *   &lt;/processor&gt;
 *   &lt;processor class="solr.LogUpdateProcessorFactory" /&gt;
 *   &lt;processor class="solr.RunUpdateProcessorFactory" /&gt;
 * &lt;/updateRequestProcessorChain&gt;</pre>
 *
 * @see org.apache.solr.update.processor.UpdateRequestProcessorChain
 *
 * @see java.util.regex.Pattern
 */
public class CmsSolrCopyModifiedUpateProcessorFactory extends UpdateRequestProcessorFactory {

    /** Name of the parameter, the regex is provided. */
    private static final String PARAM_REGEX = "regex";
    /** Name of the parameter, the replacement string is provided. */
    private static final String PARAM_REPLACEMENT = "replacement";
    /** Name of the parameter, the source field is provided. */
    private static final String PARAM_SOURCE = "source";
    /** Name of the parameter, the target field is provided. */
    private static final String PARAM_TARGET = "target";

    /** The pattern to match the source against. */
    private Pattern m_regex;
    /** The replacement string for matches. */
    private String m_replacement;
    /** The field, the value that is matched against is read from. */
    private String m_source;
    /** The field, the modified value is written to. */
    private String m_target;

    /**
     * @see org.apache.solr.update.processor.UpdateRequestProcessorFactory#getInstance(org.apache.solr.request.SolrQueryRequest, org.apache.solr.response.SolrQueryResponse, org.apache.solr.update.processor.UpdateRequestProcessor)
     */
    @Override
    public UpdateRequestProcessor getInstance(
        SolrQueryRequest req,
        SolrQueryResponse rsp,
        UpdateRequestProcessor next) {

        return new CmsSolrCopyModifiedUpateProcessor(m_source, m_target, m_regex, m_replacement, next);
    }

    /**
     * Read the parameters on initialization.
     *
     * @see org.apache.solr.update.processor.UpdateRequestProcessorFactory#init(org.apache.solr.common.util.NamedList)
     */
    @Override
    public void init(NamedList args) {

        Object regex = args.remove(PARAM_REGEX);
        if (null == regex) {
            throw new SolrException(ErrorCode.SERVER_ERROR, "Missing required init parameter: " + PARAM_REGEX);
        }
        try {
            m_regex = Pattern.compile(regex.toString());
        } catch (PatternSyntaxException e) {
            throw new SolrException(ErrorCode.SERVER_ERROR, "Invalid regex: " + regex, e);
        }

        Object replacement = args.remove(PARAM_REPLACEMENT);
        if (null == replacement) {
            throw new SolrException(ErrorCode.SERVER_ERROR, "Missing required init parameter: " + PARAM_REPLACEMENT);
        }
        m_replacement = replacement.toString();

        Object source = args.remove(PARAM_SOURCE);
        if (null == source) {
            throw new SolrException(ErrorCode.SERVER_ERROR, "Missing required init parameter: " + PARAM_SOURCE);
        }
        m_source = source.toString();

        Object target = args.remove(PARAM_TARGET);
        if (null == target) {
            throw new SolrException(ErrorCode.SERVER_ERROR, "Missing required init parameter: " + PARAM_TARGET);
        }
        m_target = target.toString();

    }

}
