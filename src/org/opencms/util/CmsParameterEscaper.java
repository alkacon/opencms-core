/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.util;

import org.opencms.cache.CmsVfsMemoryObjectCache;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;

import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.CleanResults;
import org.owasp.validator.html.Policy;
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;

/**
 * This class is responsible for automatically escaping parameters in Flex requests. It keeps track
 * of which parameters to escape (or not escape), and which parameters need to be processed by AntiSamy.<p>
 */
public class CmsParameterEscaper {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsParameterEscaper.class);

    /** The names of parameters which shouldn't be escaped. */
    private Set<String> m_exceptions = new HashSet<String>();

    /** The names of parameters which need to be HTML-cleaned. */
    private Set<String> m_cleanHtml = new HashSet<String>();

    /** The file name of the default policy. */
    public static final String DEFAULT_POLICY = "antisamy-opencms.xml";

    /** The AntiSamy instance for cleaning HTML. */
    private AntiSamy m_antiSamy;

    /** The default policy, which is used when no policy path is given. */
    protected static Policy defaultPolicy;

    static {
        try {
            // Don't hardcode the resource path, use the package of this class as the location
            String packageName = CmsParameterEscaper.class.getPackage().getName();
            String resourceName = packageName.replace(".", "/") + "/" + DEFAULT_POLICY;
            InputStream stream = CmsParameterEscaper.class.getClassLoader().getResourceAsStream(resourceName);
            @SuppressWarnings("deprecation")
            Policy policy = Policy.getInstance(stream);
            defaultPolicy = policy;
        } catch (PolicyException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Helper method for reading an AntiSamy policy file from the VFS.<p>
     *
     * @param cms the current CMS context
     * @param sitePath the site path of the policy file
     *
     * @return the policy object for the given path
     */
    public static Policy readPolicy(CmsObject cms, String sitePath) {

        try {
            CmsFile policyFile = cms.readFile(sitePath);
            ByteArrayInputStream input = new ByteArrayInputStream(policyFile.getContents());

            // we use the deprecated method here because it is the only way to load
            // a policy directly from the VFS.
            @SuppressWarnings("deprecation")
            Policy policy = Policy.getInstance(input);
            return policy;
        } catch (CmsException e) {
            LOG.error("Could not read Antisamy policy file");
            LOG.error(e.getLocalizedMessage(), e);
            return null;
        } catch (PolicyException e) {
            LOG.error("Invalid Antisamy policy read from " + sitePath);
            LOG.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * Creates a new AntiSamy instance for a given policy path.<p>
     *
     * @param cms the current CMS context
     * @param policyPath the policy site path
     *
     * @return the new AntiSamy instance
     */
    public AntiSamy createAntiSamy(CmsObject cms, String policyPath) {

        String rootPath = cms.addSiteRoot(policyPath);
        Policy policy = null;
        if (policyPath != null) {
            Object cacheValue = CmsVfsMemoryObjectCache.getVfsMemoryObjectCache().getCachedObject(cms, rootPath);
            if (cacheValue == null) {
                policy = readPolicy(cms, policyPath);
                if (policy != null) {
                    CmsVfsMemoryObjectCache.getVfsMemoryObjectCache().putCachedObject(cms, rootPath, policy);
                }
            } else {
                policy = (Policy)cacheValue;
            }
        }
        if (policy == null) {
            policy = defaultPolicy;
        }
        if (policy != null) {
            return new AntiSamy(policy);
        }
        return null;
    }

    /**
     * Enables the AntiSamy HTML cleaning for some parameters.<p>
     *
     * @param cms the current CMS context
     * @param policyPath the policy site path in the VFS
     * @param params the parameters for which HTML cleaning should be  enabled
     */
    public void enableAntiSamy(CmsObject cms, String policyPath, Set<String> params) {

        m_antiSamy = createAntiSamy(cms, policyPath);
        m_cleanHtml = params;
    }

    /**
     * Escapes a single parameter value.<p>
     *
     * @param name the name of the parameter
     * @param html the value of the parameter
     *
     * @return the escaped parameter value
     */
    public String escape(String name, String html) {

        if (html == null) {
            return null;
        }
        if (m_exceptions.contains(name)) {
            return html;
        }
        LOG.info("Escaping parameter '" + name + "' with value '" + html + "'");
        if (m_cleanHtml.contains(name)) {
            return filterAntiSamy(html);
        }
        return CmsEncoder.escapeXml(html);
    }

    /**
     * Escapes an array of parameter values.<p>
     *
     * @param name the parameter name
     * @param values the parameter values
     *
     * @return the escaped parameter values
     */
    public String[] escape(String name, String[] values) {

        if (values == null) {
            return null;
        }
        if (m_exceptions.contains(name)) {
            return values;
        }
        boolean cleanHtml = m_cleanHtml.contains(name);
        String[] result = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            if (cleanHtml) {
                result[i] = filterAntiSamy(values[i]);
            } else {
                result[i] = CmsEncoder.escapeXml(values[i]);
            }
        }
        return result;
    }

    /**
     * Filters HTML input using the internal AntiSamy instance.<p>
     *
     * @param html the HTML to filter
     *
     * @return the filtered HTML
     */
    public String filterAntiSamy(String html) {

        if (m_antiSamy == null) {
            LOG.warn("Antisamy policy invalid, using escapeXml as a fallback");
            return CmsEncoder.escapeXml(html);
        }
        try {
            CleanResults results = m_antiSamy.scan(html);
            if (results.getNumberOfErrors() > 0) {
                LOG.info("Antisamy error messages:");
                for (Object message : results.getErrorMessages()) {
                    LOG.info(message);
                }
            }
            return results.getCleanHTML();
        } catch (PolicyException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return CmsEncoder.escapeXml(html);
        } catch (ScanException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return CmsEncoder.escapeXml(html);
        }
    }

    /**
     * Sets the set of names of parameters which shouldn't be escaped.<p>
     *
     * @param exceptions a set of parameter names
     */
    public void setExceptions(Collection<String> exceptions) {

        m_exceptions = new HashSet<String>(exceptions);
    }

}
