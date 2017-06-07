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

package org.opencms.workplace.tools.workplace.logging;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.workplace.list.CmsListDirectAction;

import java.util.Locale;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Action handler for single actions from logging view.<p>
 *
 * */
public class CmsChangeLogLevelAction extends CmsListDirectAction {

    /** The actual log level of this row. */
    protected Level m_logLevel;

    /** Constructor so generate an instance.<p>
     *
     * @param id The id of the Log-channel
     * @param level The level of the Log-channel
     */
    public CmsChangeLogLevelAction(String id, Level level) {

        super(id);
        m_logLevel = level;
    }

    /** Constructor so generate an instance.<p>
     *
     * @param id Id of the Log-channel
     * @param level Level of the Log-channel
     * @param helpText Helptext of the Log-Channel
     */
    public CmsChangeLogLevelAction(String id, Level level, CmsMessageContainer helpText) {

        super(id);
        m_logLevel = level;
        setName(null);
        setHelpText(helpText);
    }

    /** Constructor so generate an instance.<p>
     *
     * @param id Id of the Log-channel
     * @param level Level of the Log-channel
     * @param name Name of the Log-channel
     * @param helpText Helptext of the Log-Channel
     */
    public CmsChangeLogLevelAction(String id, Level level, CmsMessageContainer name, CmsMessageContainer helpText) {

        super(id);
        m_logLevel = level;
        setName(name);
        setHelpText(helpText);
    }

    /**
     * Help method to resolve the name to use.<p>
     *
     * @param locale the used locale
     *
     * @return the name
     */
    @Override
    protected String resolveName(Locale locale) {

        return getName().key(locale);
    }

    /**
     * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isEnabled()
     */
    @Override
    public boolean isEnabled() {

        boolean isVisible = false;
        Logger logger = null;
        if (getItem() != null) {
            String loggerName = getItem().getId();
            if (loggerName != null) {
                logger = LogManager.getLogger(loggerName);
            }
        }
        if (logger != null) {
            isVisible = !logger.getEffectiveLevel().equals(m_logLevel);
        }
        return isVisible;
    }
}
