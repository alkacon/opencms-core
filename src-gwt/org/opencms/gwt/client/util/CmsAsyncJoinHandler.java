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

package org.opencms.gwt.client.util;

import java.util.HashSet;
import java.util.Set;

/**
 * Helper class used to perform an action when multiple asynchronous tasks have finished.<p>
 *
 * To use this, first, for every action, add a token which uniquely identifies the action. Remove the corresponding
 * token when the action is finished. When all tokens are removed, the final action will be executed.<p>
 */
public class CmsAsyncJoinHandler {

    /** The action to perform when all tokens have been removed. */
    private Runnable m_joinAction;

    /** The set of tokens. */
    protected Set<Object> m_tokens = new HashSet<Object>();

    /**
     * Creates a new instance.<p>
     *
     * @param joinAction the final action to execute
     */
    public CmsAsyncJoinHandler(Runnable joinAction) {

        m_joinAction = joinAction;
    }

    /**
     * Adds tokens.<p>
     *
     * @param tokens the tokens to add
     */
    public void addTokens(Object... tokens) {

        for (Object token : tokens) {
            m_tokens.add(token);
        }
    }

    /**
     * Removes a token.<p>
     *
     * When all tokens have been removed, the final action is executed.<p>
     *
     * @param token the token to remove
     */
    public void removeToken(Object token) {

        m_tokens.remove(token);
        if (m_tokens.isEmpty()) {
            if (m_joinAction != null) {
                m_joinAction.run();
            }
        }
    }

}
