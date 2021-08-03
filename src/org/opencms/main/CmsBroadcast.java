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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.main;

import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsEncoder;
import org.opencms.ui.components.CmsRichTextArea;

/**
 * A single broadcast message, send from one OpenCms user to another.<p>
 *
 * To addess a broadcast to another user, it must be placed in the
 * broadcast queue of that user using for example
 * {@link org.opencms.main.CmsSessionManager#sendBroadcast(CmsUser, String, CmsUser)}.<p>
 *
 * @since 6.0.0
 */
public class CmsBroadcast implements Cloneable {

    /** The content mode describes how the text used for a broadcast message should be interpreted. */
    public enum ContentMode {
        /** Broadcast message is plaintext and needs to be converted to HTML. */
        plain,
        /** Broadcast message is already HTML, but may need to be cleaned up. */
        html;
    }

    /** Time interval between displays of repeated messages. */
    public static long DISPLAY_AGAIN_TIME = 60 * 1000;

    /** The broadcast content. */
    private String m_message;

    /** The sender of the broadcast. */
    private CmsUser m_sender;

    /** Time the broadcast was send. */
    private long m_sendTime;

    /** The content mode. */
    private ContentMode m_contentMode;

    /**The last display time. */
    private long m_lastDisplay;

    /** True if message should be repeated. */
    private boolean m_repeat;

    /**
     * Creates a new broadcast, with the current system time set as send time.<p>
     *
     *
     *
     * @param sender the sender of the broadcast
     * @param message the message to send
     * @deprecated use the constructors with the content mode instead
     */
    @Deprecated
    public CmsBroadcast(CmsUser sender, String message) {

        this(sender, message, System.currentTimeMillis(), 0L, false);
    }

    /**
     * Creates a new broadcast, with the current system time set as send time.<p>
     *
     * @param sender the sender of the broadcast
     * @param message the message to send
     * @param repeat true if message shoul be repeated
     * @deprecated use the constructors with the content mode instead
     */
    @Deprecated
    public CmsBroadcast(CmsUser sender, String message, boolean repeat) {

        this(sender, message, System.currentTimeMillis(), 0L, repeat);
    }

    /**
     * Creates a new broadcast, with the current system time set as send time.<p>
     *
     * @param sender the sender of the broadcast
     * @param message the message to send
     * @param repeat true if message should be repeated
     * @param mode the content mode
     */
    public CmsBroadcast(CmsUser sender, String message, boolean repeat, ContentMode mode) {

        this(sender, message, System.currentTimeMillis(), 0L, repeat, mode);
    }

    /**
     * Creates a new broadcast, with the current system time set as send time.<p>
     *
     * @param sender the sender of the broadcast
     * @param message the message to send
     * @param mode the content mode
     */
    public CmsBroadcast(CmsUser sender, String message, ContentMode mode) {

        this(sender, message, System.currentTimeMillis(), 0L, false, mode);
    }

    /**
     * Creates a new broadcast, with the current system time set as send time.<p>
     *
     * @param sender the sender of the broadcast
     * @param message the message to send
     * @param sendTime time when broadcast initaly was send
     * @param lastDisplay  last display time
     * @param repeat true if message should be repeated
     *
     * @deprecated use the constructors with the content mode instead
     */
    @Deprecated
    public CmsBroadcast(CmsUser sender, String message, long sendTime, long lastDisplay, boolean repeat) {

        this(sender, message, sendTime, lastDisplay, repeat, ContentMode.plain);

    }

    /**
     * Creates a new broadcast, with the current system time set as send time.<p>
     *
     * @param sender the sender of the broadcast
     * @param message the message to send
     * @param sendTime time when broadcast initaly was send
     * @param lastDisplay  last display time
     * @param repeat true if the message should be repeated
     * @param mode the content mode
     */
    @Deprecated
    public CmsBroadcast(
        CmsUser sender,
        String message,
        long sendTime,
        long lastDisplay,
        boolean repeat,
        ContentMode mode) {

        m_sender = sender;
        m_message = message;
        m_sendTime = sendTime;
        m_lastDisplay = lastDisplay;
        m_repeat = repeat;
        m_contentMode = mode;
    }

    /**
     * Gets the content mode.
     *
     * @return the content mode
     */
    public ContentMode getContentMode() {

        return m_contentMode;
    }

    /**
     * Gets the last display time.
     *
     * @return the last display time
     */
    public long getLastDisplay() {

        return m_lastDisplay;
    }

    /**
     * Returns the processed broadcast message content as HTML.
     *
     * @return the broadcast message content HTML
     */
    public String getMessage() {

        String result;
        switch (m_contentMode) {
            case html:
                result = m_message;
                break;
            case plain:
            default:
                result = htmlForPlain(m_message);
        }
        result = CmsRichTextArea.cleanHtml(result, true);
        return result;
    }

    /**
     * Returns the original message text passed in the constructor.
     *
     * @return the original message text
     */
    public String getRawMessage() {

        return m_message;
    }

    /**
     * Returns the time this broadcast was send.<p>
     *
     * @return the time this broadcast was send
     */
    public long getSendTime() {

        return m_sendTime;
    }

    /**
     * Returns the user that was the sender of this broadcast.<p>
     *
     * It could be <code>null</code> to signalize a system message.<p>
     *
     * @return the user that was the sender of this broadcast
     */
    public CmsUser getUser() {

        return m_sender;
    }

    /**
     * Returns true if this is a repeating message.
     *
     * @return true if this is a repeating message
     */
    public boolean isRepeat() {

        return m_repeat;
    }

    /**
     * Produces a copy of this object, but with a changed lastDisplay attribute.
     *
     * @param lastDisplay the new value for the lastDisplay attribute
     * @return the copy with the modified time
     */
    public CmsBroadcast withLastDisplay(long lastDisplay) {

        CmsBroadcast result;
        try {
            result = (CmsBroadcast)(this.clone());
            result.m_lastDisplay = lastDisplay;
            return result;
        } catch (CloneNotSupportedException e) {
            return null;
        }

    }

    /**
     * Escapes special HTML characters and converts newlines to br tags.
     *
     * @param message the message to convert
     * @return the HTML for the message
     */
    private String htmlForPlain(String message) {

        String result = CmsEncoder.escapeXml(message);
        result = result.replace("\n", "<br>");
        return result;

    }
}