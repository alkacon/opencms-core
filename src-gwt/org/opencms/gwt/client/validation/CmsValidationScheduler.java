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

package org.opencms.gwt.client.validation;

import java.util.LinkedList;

/**
 * This is a helper class for scheduling form validations.<p>
 *
 * Since validations can be asynchronous, it would be possible for a validation to start while another one
 * is still waiting for a response from the server if it were ran directly. This might result in an inconsistent state
 * of the form fields being validated. To prevent this, validation controllers use this class to schedule validations,
 * and call this class again after they're finished to execute the next validation.<p>
 *
 * The result of this is that a validation will only start after all unfinished validations which have been scheduled
 * before it have finished running.<p>
 *
 * @since 8.0.0
 */
public class CmsValidationScheduler {

    /** The singleton instance of this class. */
    private static CmsValidationScheduler instance = new CmsValidationScheduler();

    /** The queue of validations which still need to be run. */
    private LinkedList<CmsValidationController> m_actions = new LinkedList<CmsValidationController>();

    /** A flag which indicates whether there is no validation currently running. */
    private boolean m_idle = true;

    /**
     * Hidden default constructor.<p>
     */
    protected CmsValidationScheduler() {

        // do nothing
    }

    /**
     * Returns the singleton instance of the validation scheduler.<p>
     *
     * @return the validation scheduler
     */
    public static CmsValidationScheduler get() {

        return instance;
    }

    /**
     * This method should be called by the validation controller when it has finished running.<p>
     *
     * It will execute the next validation if there is one.<p>
     */
    public void executeNext() {

        if (!m_idle) {
            if (m_actions.isEmpty()) {
                m_idle = true;
            } else {
                CmsValidationController action = m_actions.removeFirst();
                action.internalStartValidation();
            }
        } else {
            assert false; // this shouldn't happen
        }
    }

    /**
     * This schedules a new validation to be run after all currently scheduled or running validations have finished.<p>
     *
     * If there are no validations running, the validation will be started immediately.<p>
     *
     * @param action the validation to be scheduled
     */
    public void schedule(CmsValidationController action) {

        if (m_idle) {
            m_idle = false;
            action.internalStartValidation();
        } else {
            m_actions.add(action);
        }
    }
}
