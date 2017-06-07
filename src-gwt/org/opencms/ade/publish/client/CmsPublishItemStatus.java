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

package org.opencms.ade.publish.client;

import org.opencms.util.CmsUUID;

/**
 * This class encapsulates the possible states of a publish item.<p>
 *
 * An item can be enabled or disabled (because of an error). If it is enabled,
 * it can change between the states "normal", "publish", and "remove", but if it is
 * disabled, it can only change between "normal" and "remove".<p>
 *
 * The state will be changed depending on various signals which are passed as
 * parameters to the handleSignal() method.<p>
 *
 * If the item is enabled, the only possible state transitions are as follows:
 *
 * publish to normal
 * publish to remove
 * normal to publish
 * normal to remove
 * remove to normal
 *
 * @since 8.0.0
 */
public class CmsPublishItemStatus {

    /**
     * The enum for the publish item state.<p>
     */
    public enum State {
        /** Normal state. */
        normal, /** State for items which should be published. */
        publish,

        /** State for items which should be removed. */
        remove;
    }

    /**
     * The enum for the type of signals which can change the item state.<p>
     */
    enum Signal {
        /** User selected publish. */
        publish, /** User selected remove. */
        remove, /** User deselected publish. */
        unpublish, /** User deselected remove. */
        unremove;
    }

    /** The status update handler which should be notified of changes to the state. */
    I_CmsPublishItemStatusUpdateHandler m_handler;

    /** Flag which indicates if this item is disabled. */
    private boolean m_disabled;

    /** The id of the item. */
    private CmsUUID m_id;

    /** The current state of the item. */
    private State m_state;

    /**
     * Creates a new publish item status bean.<p>
     *
     * @param id the publish item id
     * @param state the publish item state
     * @param disabled true if this item is disabled
     * @param handler the handler which should be notified of state changes
     */
    public CmsPublishItemStatus(
        CmsUUID id,
        State state,
        boolean disabled,
        I_CmsPublishItemStatusUpdateHandler handler) {

        m_id = id;
        m_state = state;
        m_disabled = disabled;
        m_handler = handler;
        assert m_disabled ? m_state != State.publish : true;
    }

    /**
     * Gets the current state of the publish item.<p>
     *
     * @return the current state
     */
    public State getState() {

        return m_state;
    }

    /**
     * Handles a signal which may change the current state.<p>
     *
     * @param signal the signal
     */
    public void handleSignal(Signal signal) {

        switch (signal) {
            case publish:
                signalPublish();
                break;
            case unpublish:
                signalUnpublish();
                break;
            case remove:
                signalRemove();
                break;
            case unremove:
                signalUnremove();
                break;
            default:
                break;
        }
        m_handler.update(m_id, this);
    }

    /**
     * Checks whether this publish item is disabled.<p>
     *
     * @return true if the publish item is disabled
     */
    public boolean isDisabled() {

        return m_disabled;
    }

    /**
     * Executes a publish signal.<p>
     */
    protected void signalPublish() {

        if (!m_disabled) {
            if (m_state != State.remove) {
                m_state = State.publish;
            }
        }
    }

    /**
     * Executes a remove signal.<p>
     */
    protected void signalRemove() {

        m_state = State.remove;
    }

    /**
     * Executes an unpublish signal.<p>
     */
    protected void signalUnpublish() {

        if (m_state == State.publish) {
            m_state = State.normal;
        }
    }

    /**
     * Executes an unremove signal.<p>
     */
    protected void signalUnremove() {

        if (m_disabled) {
            m_state = State.normal;
        } else {
            m_state = State.publish;
        }
    }

}
