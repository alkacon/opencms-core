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

package org.opencms.acacia.client;

import org.opencms.acacia.shared.CmsContentDefinition;
import org.opencms.acacia.shared.CmsEntity;
import org.opencms.acacia.shared.CmsValidationResult;
import org.opencms.acacia.shared.rpc.I_CmsContentServiceAsync;
import org.opencms.gwt.client.ui.CmsTabbedPanel;

import java.util.Collections;
import java.util.Map.Entry;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Validation handler.<p>
 */
public final class CmsValidationHandler
implements ValueChangeHandler<CmsEntity>, HasValueChangeHandlers<CmsValidationContext> {

    /**
     * The validation timer.<p>
     */
    protected class ValidationTimer extends Timer {

        /** The entity to validate. */
        private CmsEntity m_entity;

        /**
         * Constructor.<p>
         *
         * @param entity the entity to validate
         */
        protected ValidationTimer(CmsEntity entity) {

            m_entity = entity;
        }

        /**
         * @see com.google.gwt.user.client.Timer#run()
         */
        @Override
        public void run() {

            validate(m_entity);
            m_validationTimer = null;
        }
    }

    /** Flag indicating the a validation call is running. */
    boolean m_validating;

    /** The current validation timer instance. */
    Timer m_validationTimer;

    /** The content service use for validation. */
    private I_CmsContentServiceAsync m_contentService;

    /** The event bus. */
    private SimpleEventBus m_eventBus;

    /** The forms tabbed panel. */
    private CmsTabbedPanel<?> m_formTabPanel;

    /** The handler registration. */
    private HandlerRegistration m_handlerRegistration;

    /** Indicates validation is paused. */
    private boolean m_paused;

    /** The root attribute handler. */
    private CmsRootHandler m_rootHandler;

    /** The validation context. */
    private CmsValidationContext m_validationContext;

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<CmsValidationContext> handler) {

        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Destroys the current handler instance.<p>
     */
    public void clear() {

        if (m_handlerRegistration != null) {
            m_handlerRegistration.removeHandler();
            m_handlerRegistration = null;
        }
        m_validationContext = null;
        m_paused = false;
        m_validating = false;
        if (m_validationTimer != null) {
            m_validationTimer.cancel();
            m_validationTimer = null;
        }
    }

    /**
     * Displays the given error messages within the form.<p>
     *
     * @param entityId the entity id
     * @param validationResult the validationResult
     */
    public void displayValidation(String entityId, CmsValidationResult validationResult) {

        if (m_formTabPanel != null) {
            CmsAttributeHandler.clearErrorStyles(m_formTabPanel);
        }
        if (validationResult.hasWarnings(entityId)) {
            for (Entry<String[], String> warning : validationResult.getWarnings(entityId).entrySet()) {
                String[] pathElements = warning.getKey();
                // check if there are no errors for this attribute
                if (!validationResult.hasErrors(entityId)
                    || !validationResult.getErrors(entityId).containsKey(pathElements)) {
                    CmsAttributeHandler handler = m_rootHandler.getHandlerByPath(pathElements);
                    if (handler != null) {
                        String attributeName = pathElements[pathElements.length - 1];
                        handler.setWarningMessage(
                            CmsContentDefinition.extractIndex(attributeName),
                            warning.getValue(),
                            m_formTabPanel);
                    }
                }
            }
        }
        if (validationResult.hasErrors(entityId)) {
            for (Entry<String[], String> error : validationResult.getErrors(entityId).entrySet()) {
                String[] pathElements = error.getKey();
                CmsAttributeHandler handler = m_rootHandler.getHandlerByPath(pathElements);
                if (handler != null) {
                    String attributeName = pathElements[pathElements.length - 1];
                    handler.setErrorMessage(
                        CmsContentDefinition.extractIndex(attributeName),
                        error.getValue(),
                        m_formTabPanel);
                }
            }
            m_validationContext.addInvalidEntity(entityId);
        } else {
            m_validationContext.addValidEntity(entityId);
        }
        ValueChangeEvent.fire(this, m_validationContext);
        m_validating = false;
    }

    /**
     * @see com.google.gwt.event.shared.HasHandlers#fireEvent(com.google.gwt.event.shared.GwtEvent)
     */
    public void fireEvent(GwtEvent<?> event) {

        ensureHandlers().fireEventFromSource(event, this);
    }

    /**
     * Returns the validation context.<p>
     *
     * @return the validation context
     */
    public CmsValidationContext getValidationContext() {

        return m_validationContext;
    }

    /**
     * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)
     */
    public void onValueChange(final ValueChangeEvent<CmsEntity> event) {

        if (!m_paused) {
            if (m_validationTimer != null) {
                m_validationTimer.cancel();
            }
            m_validationTimer = new ValidationTimer(event.getValue());
            m_validationTimer.schedule(300);
        }
    }

    /**
     * Registers the validation handler for the given entity.<p>
     *
     * @param entity the entity
     */
    public void registerEntity(CmsEntity entity) {

        if (m_validationContext == null) {
            m_validationContext = new CmsValidationContext();
        }
        if (m_handlerRegistration != null) {
            m_handlerRegistration.removeHandler();
        }
        m_paused = false;
        m_handlerRegistration = entity.addValueChangeHandler(this);
    }

    /**
     * Sets the content service used for validation.<p>
     *
     * @param contentService the content service
     */
    public void setContentService(I_CmsContentServiceAsync contentService) {

        m_contentService = contentService;
    }

    /**
     * Sets the form tabbed panel.<p>
     *
     * @param tabPanel the tabbed panel
     */
    public void setFormTabPanel(CmsTabbedPanel<?> tabPanel) {

        m_formTabPanel = tabPanel;
    }

    /**
     * Sets the validation to pause.<p>
     *
     * @param paused <code>true</code> to pause the validation
     * @param entity the entity will be revalidated when setting paused to <code>false</code>
     */
    public void setPaused(boolean paused, CmsEntity entity) {

        if (paused != m_paused) {
            m_paused = paused;
            if (m_paused) {
                if (m_validationTimer != null) {
                    m_validationTimer.cancel();
                    m_validationTimer = null;
                }
            } else {
                m_validationTimer = new ValidationTimer(entity);
                m_validationTimer.schedule(300);
            }

        }
    }

    /**
     * Sets the root attribute handler.<p>
     *
     * @param rootHandler the root attribute handler
     */
    public void setRootHandler(CmsRootHandler rootHandler) {

        m_rootHandler = rootHandler;
    }

    /**
     * Adds this handler to the widget.
     *
     * @param <H> the type of handler to add
     * @param type the event type
     * @param handler the handler
     * @return {@link HandlerRegistration} used to remove the handler
     */
    protected <H extends EventHandler> HandlerRegistration addHandler(final H handler, GwtEvent.Type<H> type) {

        return ensureHandlers().addHandlerToSource(type, this, handler);
    }

    /**
     * Validates the given entity.<p>
     *
     * @param entity the entity
     */
    protected void validate(final CmsEntity entity) {

        if (!m_validating) {
            m_validating = true;
            m_contentService.validateEntities(
                Collections.singletonList(entity),
                new AsyncCallback<CmsValidationResult>() {

                    public void onFailure(Throwable caught) {

                        // can be ignored
                    }

                    public void onSuccess(CmsValidationResult result) {

                        displayValidation(entity.getId(), result);
                    }
                });
        }
    }

    /**
     * Lazy initializing the handler manager.<p>
     *
     * @return the handler manager
     */
    private SimpleEventBus ensureHandlers() {

        if (m_eventBus == null) {
            m_eventBus = new SimpleEventBus();
        }
        return m_eventBus;
    }
}
