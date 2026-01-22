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

package org.opencms.ade.contenteditor.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Contains status information about a content augmentation that has just been run.
 */
public class CmsContentAugmentationDetails implements IsSerializable {

    /** Indicates that the job was aborted. */
    public static final String PROGRESS_ABORTED = "ABORTED";

    /** Indicates that the job has finished. */
    public static final String PROGRESS_DONE = "DONE";

    /** An error that happened on the server side. */
    private Throwable m_exception;

    /** The message to display, as HTML (null for no message) . */
    private String m_htmlMessage;

    /**
     * The list of locales from the augmented content.
     */
    private List<String> m_locales;

    /** The locale to switch to (null to not switch locale). */
    private String m_nextLocale;

    /** The progress message. */
    private String m_progress;

    /** The result caption. */
    private String m_resultCaption;

    /**
     * Creates a new instance.
     */
    public CmsContentAugmentationDetails() {

    }

    /**
     * Gets the exception (if any) from the server side.
     *
     * @return the exception from the server side
     */
    public Throwable getException() {

        return m_exception;
    }

    /**
     * Gets the message to display to the user, in HTML format.
     *
     * @return the HTML message
     */
    public String getHtmlMessage() {

        return m_htmlMessage;
    }

    /**
     * Gets the locales that are part of the augmented content (as strings).
     *
     * @return the list of locales
     */
    public List<String> getLocales() {

        return m_locales;
    }

    /**
     * Gets tje next locale to switch to (might be null if we should remain in the current locale).
     *
     * @return the locale to switch to
     */
    public String getNextLocale() {

        return m_nextLocale;
    }

    /**
     * Gets the progress message.
     *
     * @return the progress message
     */
    public String getProgress() {

        return m_progress;
    }

    /**
     * Gets the caption to display on the result dialog.
     *
     * @return the caption to display on the results dialog
     */
    public String getResultCaption() {

        return m_resultCaption;
    }

    /**
     * Checks if the augmentation job was aborted.
     *
     *
     * @return true if the augmentation job was aborted.
     */
    public boolean isAborted() {

        return PROGRESS_ABORTED.equals(m_progress);
    }

    /**
     * Checks if the content augmentation has sucessfully completed.
     *
     * @return true if the content augmentation has successfully completed
     */
    public boolean isDone() {

        return PROGRESS_DONE.equals(m_progress);
    }

    /**
     * Sets the exception to display.
     *
     * @param exception the exception to display
     */
    public void setException(Throwable exception) {

        m_exception = exception;
    }

    /**
     * Sets the message to display, in HTML format
     *
     * @param htmlMessage the message to display
     */
    public void setHtmlMessage(String htmlMessage) {

        m_htmlMessage = htmlMessage;
    }

    /**
     * Sets the locales.
     *
     * @param locales the locales
     */
    public void setLocales(List<String> locales) {

        m_locales = locales;
    }

    /**
     * Sets the next locale to switch to (can be null if current locale should be maintained).
     *
     * @param nextLocale the next locale to switch to
     */
    public void setNextLocale(String nextLocale) {

        m_nextLocale = nextLocale;
    }

    /**
     * Sets the progress message.
     *
     * @param progress the progress message
     */
    public void setProgress(String progress) {

        m_progress = progress;
    }

    /**
     * Sets the caption to display on the results dialog.
     *
     * @param resultCaption the caption to display on the results dialog
     */
    public void setResultCaption(String resultCaption) {

        m_resultCaption = resultCaption;
    }

}
