/*
 * File   : $Source $
 * Date   : $Date $
 * Version: $Revision $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.main;

import org.opencms.i18n.CmsMessageContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * A multi exception is a container for several exception messages that may be caused by an internal operation.<p>
 * 
 * This is provided so that the user can see a full picuture of all the issues that have been caused in an operation,
 * rather then only one (usually the first) issue.
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 2.0.0 
 */
public class CmsMultiException extends CmsException {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 1197300254684159700L;

    /** The list of internal exceptions. */
    protected List m_exceptions;

    /** Indicates if the message has been set as individual message. */
    protected boolean m_individualMessage;

    /**
     * Creates a new multi exception, a container for several exception messages.<p>
     */
    public CmsMultiException() {

        this(Messages.get().container(Messages.ERR_MULTI_EXCEPTION_1, new Integer(0)));
    }

    /**
     * Creates a new multi exception using the given base message.<p>
     * 
     * @param message the basic message to use
     */
    public CmsMultiException(CmsMessageContainer message) {

        super(message);
        m_exceptions = new ArrayList();
        setMessage(message);
    }

    /**
     * Creates a new multi exception for the given list of <code>{@link CmsException}</code> instances.<p>
     * 
     * @param exceptions a list of <code>{@link CmsException}</code> instances
     */
    public CmsMultiException(List exceptions) {

        this();
        setExceptions(exceptions);
    }

    /**
     * Adds an Exception to the list of Exceptions kept in this multi Exception.<p>
     * 
     * @param exception the Exception to add
     */
    public void addException(CmsException exception) {

        m_exceptions.add(exception);
        updateMessage();
    }

    /**
     * Adds all Exceptions in the given List to the list of Exceptions kept in this multi Exception.<p>
     * 
     * @param exceptions the Exceptions to add
     */
    public void addExceptions(List exceptions) {

        m_exceptions.addAll(exceptions);
        updateMessage();
    }

    /** 
     * @see org.opencms.main.CmsException#createException(org.opencms.i18n.CmsMessageContainer, java.lang.Throwable)
     */
    public CmsException createException(CmsMessageContainer container, Throwable cause) {

        if (cause instanceof CmsMultiException) {
            CmsMultiException multiException = (CmsMultiException)cause;
            return new CmsMultiException(multiException.getExceptions());
        }
        // not a multi exception, use standard handling
        return super.createException(container, cause);
    }

    /**
     * Returns the (unmodifiable) List of exceptions that are tored in this multi exception.<p>
     * 
     * @return the (unmodifiable) List of exceptions that are tored in this multi exception
     */
    public List getExceptions() {

        return Collections.unmodifiableList(m_exceptions);
    }

    /**
     * Returns a localized message composed of all contained exceptions.<p> 
     * 
     * @see java.lang.Throwable#getLocalizedMessage()
     */
    public String getLocalizedMessage() {

        if (m_exceptions.isEmpty()) {
            return null;
        }
        StringBuffer result = new StringBuffer(128);
        Iterator it = m_exceptions.iterator();
        while (it.hasNext()) {
            CmsException ex = (CmsException)it.next();
            result.append(ex.getLocalizedMessage());
            if (it.hasNext()) {
                result.append('\n');
            }
        }
        return result.toString();
    }

    /**
     * Returns a localized message for the given locale composed of all contained exceptions.<p>
     *  
     * @see org.opencms.main.I_CmsThrowable#getLocalizedMessage(java.util.Locale)
     */
    public String getLocalizedMessage(Locale locale) {

        if (m_exceptions.isEmpty()) {
            return null;
        }
        StringBuffer result = new StringBuffer(128);
        Iterator it = m_exceptions.iterator();
        while (it.hasNext()) {
            CmsException ex = (CmsException)it.next();
            result.append(ex.getLocalizedMessage(locale));
            if (it.hasNext()) {
                result.append('\n');
            }
        }
        return result.toString();
    }

    /**
     * Returns <code>true</code> if this multi exceptions contains at last one individual Exception.<p>
     * 
     * @return <code>true</code> if this multi exceptions contains at last one individual Exception
     */
    public boolean hasExceptions() {

        return !m_exceptions.isEmpty();
    }

    /**
     * Returns <code>true</code> if this multi message has an individual base message set.<p>
     * 
     * @return <code>true</code> if this multi message has an individual base message set
     * 
     * @see #setMessage(CmsMessageContainer)
     */
    public boolean hasIndividualMessage() {

        return m_individualMessage;
    }

    /**
     * Sets an individual message for the multi exception base message.<p>
     * 
     * If no individual message has been set, a default message using the key
     * <code>{@link Messages#ERR_MULTI_EXCEPTION_1}</code> will be used.<p>
     * 
     * If <code>null</code> is given as parameter, any individual message that 
     * has been set is reset to the default message.<p>
     * 
     * @param message the message to set
     */
    public void setMessage(CmsMessageContainer message) {

        if ((message != null) && (message.getKey() != Messages.ERR_MULTI_EXCEPTION_1)) {
            m_individualMessage = true;
            m_message = message;
        } else {
            // if message is null, reset and use default message again
            m_individualMessage = false;
            updateMessage();
        }
    }

    /**
     * Updates the internal list of stored exceptions.<p>
     * 
     * @param exceptions the exceptions to use (will replace the current exception list)
     */
    protected void setExceptions(List exceptions) {

        m_exceptions = new ArrayList(exceptions);
        updateMessage();
    }

    /**
     * Updates the intenal message for the Exception.<p>
     */
    protected void updateMessage() {

        if (!hasIndividualMessage()) {
            m_message = Messages.get().container(Messages.ERR_MULTI_EXCEPTION_1, new Integer(m_exceptions.size()));
        }
    }
}