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

package org.opencms.db;

import org.opencms.main.CmsLog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.logging.Log;

/**
 * Helper class used for matching rewrite aliases to incoming request URIs.<p>
 */
public class CmsRewriteAliasMatcher {

    /**
     * The result of a match operation.<p>
     */
    public static class RewriteResult {

        /** The rewrite alias which matched the given path. */
        private CmsRewriteAlias m_alias;

        /** The path resulting from the rewrite. */
        private String m_newPath;

        /**
         * Creates a new instance.<p>
         *
         * @param newPath the path resulting from the rewrite
         * @param alias the alias that matched the path
         */
        public RewriteResult(String newPath, CmsRewriteAlias alias) {

            m_newPath = newPath;
            m_alias = alias;

        }

        /**
         * Gets the alias which matched the given path.<p>
         *
         * @return the matching alias
         */
        public CmsRewriteAlias getAlias() {

            return m_alias;
        }

        /**
         * Gets the path resulting from the rewrite.<p>
         *
         * @return the new path
         */
        public String getNewPath() {

            return m_newPath;
        }

    }

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsRewriteAliasMatcher.class);

    /** The list of rewrite aliases to use for matching. */
    private List<CmsRewriteAlias> m_aliases;

    /**
     * Creates a new matcher instance for the given list of rewrite aliases.<p>
     *
     * @param aliases the list of rewrite aliases to be used for matching
     */
    public CmsRewriteAliasMatcher(Collection<CmsRewriteAlias> aliases) {

        m_aliases = new ArrayList<CmsRewriteAlias>(aliases);
    }

    /**
     * Tries to rewrite a given path, and either returns the rewrite result or null if no
     * rewrite alias matched the path.<p>
     *
     * @param path the path to match
     * @return the rewrite result or null if no rewrite alias matched
     */
    public RewriteResult match(String path) {

        for (CmsRewriteAlias alias : m_aliases) {
            try {
                Pattern pattern = Pattern.compile(alias.getPatternString());
                Matcher matcher = pattern.matcher(path);
                if (matcher.matches()) {
                    String newPath = matcher.replaceFirst(alias.getReplacementString());
                    return new RewriteResult(newPath, alias);
                }
            } catch (PatternSyntaxException e) {
                LOG.warn(e.getLocalizedMessage(), e);
            } catch (IndexOutOfBoundsException e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
        }
        return null;
    }
}
