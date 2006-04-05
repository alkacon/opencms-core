/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/CmsLoginMessage.java,v $
 * Date   : $Date: 2005/06/23 11:11:24 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.util.CmsStringUtil;

import java.util.GregorianCalendar;

/**
 * A message to display when a user logs in to the system.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.4 $
 * 
 * @since 6.0.0
 */
public class CmsLoginMessage {

    /** The default end time (if none has been set). This is December 31, 2039. */
    public static final long DEFAULT_TIME_END = new GregorianCalendar(2039, 11, 31).getTimeInMillis();

    /** The default start time (if none has been set). This is January 1, 2000. */
    public static final long DEFAULT_TIME_START = new GregorianCalendar(2000, 0, 1).getTimeInMillis();

    /** Indicates if the message is enabled or not. */
    private boolean m_enabled;

    /** Indicates if the configuration of this message is finalized (frozen). */
    private boolean m_frozen;

    /** Controls if logins are forbidden while this message is active. */
    private boolean m_loginForbidden;

    /** The message to display on a login. */
    private String m_message;

    /** The time when to finish displaying this message. */
    private long m_timeEnd;

    /** The time when to start displaying this message. */
    private long m_timeStart;

    /**
     * Creates a new login message with all default values.<p> 
     */
    public CmsLoginMessage() {

        m_timeStart = DEFAULT_TIME_START;
        m_timeEnd = DEFAULT_TIME_END;
    }

    /**
     * Creates a new login message with the given parameters.<p>
     * 
     * @param timeStart the time when to start displaying this message
     * @param timeEnd the time when to finish displaying this message
     * @param message the message to display
     * @param loginForbidden controls if logins are forbidden while this message is active
     */
    public CmsLoginMessage(long timeStart, long timeEnd, String message, boolean loginForbidden) {

        setTimeStart(timeStart);
        setTimeEnd(timeEnd);
        m_enabled = true;
        setMessage(message);
        m_loginForbidden = loginForbidden;
    }

    /**
     * Creates a new login message with the given parameters.<p>
     * 
     * @param message the message to display
     * @param loginForbidden controls if logins are forbidden while this message is active
     */
    public CmsLoginMessage(String message, boolean loginForbidden) {

        this(DEFAULT_TIME_START, DEFAULT_TIME_END, message, loginForbidden);
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {

        CmsLoginMessage result = new CmsLoginMessage();
        result.m_timeStart = m_timeStart;
        result.m_timeEnd = m_timeEnd;
        result.m_loginForbidden = m_loginForbidden;
        result.m_message = m_message;
        result.m_enabled = m_enabled;

        return result;
    }

    /**
     * Returns the message.<p>
     *
     * @return the message
     */
    public String getMessage() {

        return m_message;
    }

    /**
     * Returns the time the message ends.<p>
     *
     * @return the time the message ends
     */
    public long getTimeEnd() {

        return m_timeEnd;
    }

    /**
     * Returns the time the message starts.<p>
     *
     * @return the time the message starts
     */
    public long getTimeStart() {

        return m_timeStart;
    }

    /**
     * Returns <code>true</code> if this message is currently active.<p>
     * 
     * A message is active if it is enabled and 
     * the current time is after the message start time and before the message end time.<p>
     * 
     * @return <code>true</code> if this message is currently active
     */
    public boolean isActive() {

        if (!m_enabled) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        return ((currentTime > m_timeStart) && (currentTime < m_timeEnd));
    }

    /**
     * Returns <code>true</code> if this login message is the enabled.<p>
     *
     * @return <code>true</code> if this login message is the enabled
     */
    public boolean isEnabled() {

        return m_enabled;
    }

    /**
     * Returns <code>true</code> if logins are currently forbidden according to the settings
     * of this message.<p>
     * 
     * This checks the time settings using <code>{@link #isActive()}</code> and
     * <code>{@link #isEnabled()}</code> as well as the 
     * <code>{@link #isLoginForbidden()}</code> flag.<p>
     * 
     * @return <code>true</code> if logins are currently forbidden according to the settings of this message
     */
    public boolean isLoginCurrentlyForbidden() {

        return m_loginForbidden && isActive();
    }

    /**
     * Returns <code>true</code> if logins are forbidden while this message is active.<p>
     *
     * @return <code>true</code> if logins are forbidden while this message is active
     */
    public boolean isLoginForbidden() {

        return m_loginForbidden;
    }

    /**
     * Sets the enabled status of this message.<p>
     *
     * @param enabled the enabled status to set
     */
    public void setEnabled(boolean enabled) {

        checkFrozen();
        m_enabled = enabled;
    }

    /**
     * Sets the flag that controls if logins are forbidden while this message is active.<p>
     *
     * @param loginForbidden the flag to set
     */
    public void setLoginForbidden(boolean loginForbidden) {

        checkFrozen();
        m_loginForbidden = loginForbidden;
    }

    /**
     * Sets the message to display.<p>
     *
     * @param message the message to set
     */
    public void setMessage(String message) {

        checkFrozen();
        if (isEnabled() && CmsStringUtil.isEmptyOrWhitespaceOnly(message)) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_LOGIN_MESSAGE_BAD_MESSAGE_0));
        }
        m_message = message;
    }

    /**
     * Sets the time when to finish displaying this message.<p>
     *
     * @param timeEnd the time to set
     */
    public void setTimeEnd(long timeEnd) {

        checkFrozen();
        if (timeEnd < 0) {
            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.ERR_LOGIN_MESSAGE_BAD_TIME_1,
                new Long(timeEnd)));
        }
        if (timeEnd == 0) {
            timeEnd = DEFAULT_TIME_END;
        }
        if ((m_timeStart > 0) && (timeEnd <= m_timeStart)) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_LOGIN_MESSAGE_BAD_END_TIME_0));
        }
        m_timeEnd = timeEnd;
    }

    /**
     * Sets the time when to start displaying this message.<p>
     *
     * @param timeStart the time to set
     */
    public void setTimeStart(long timeStart) {

        checkFrozen();
        if (timeStart < 0) {
            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.ERR_LOGIN_MESSAGE_BAD_TIME_1,
                new Long(timeStart)));
        }
        if (timeStart == 0) {
            timeStart = DEFAULT_TIME_START;
        }
        m_timeStart = timeStart;
    }

    /**
     * Checks if this message is frozen.<p>
     * 
     * @throws CmsRuntimeException in case the message is already frozen
     */
    protected void checkFrozen() throws CmsRuntimeException {

        if (m_frozen) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_LOGIN_MESSAGE_FROZEN_0));
        }
    }

    /**
     * Freezes the configuration of this login message object to prevent later changes.<p>
     */
    protected void setFrozen() {

        m_frozen = true;
    }
}