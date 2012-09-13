/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ade.contenteditor.client;

import com.alkacon.acacia.client.EditorBase;
import com.alkacon.acacia.shared.ValidationResult;
import com.alkacon.vie.client.Vie;
import com.alkacon.vie.shared.I_Entity;

import org.opencms.ade.contenteditor.client.css.I_CmsLayoutBundle;
import org.opencms.ade.contenteditor.shared.CmsContentDefinition;
import org.opencms.ade.contenteditor.shared.rpc.I_CmsContentServiceAsync;
import org.opencms.ade.contenteditor.widgetregistry.client.WidgetRegistry;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;
import org.opencms.gwt.shared.rpc.I_CmsCoreServiceAsync;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Command;

/**
 * The content editor base.<p>
 */
public class CmsEditorBase extends EditorBase {

    /** The core RPC service instance. */
    private I_CmsCoreServiceAsync m_coreSvc;

    /** The content service. */
    private I_CmsContentServiceAsync m_service;

    /**
     * Constructor.<p>
     * 
     * @param service the content service 
     */
    public CmsEditorBase(I_CmsContentServiceAsync service) {

        super(service);
        m_service = service;
        getWidgetService().setWidgetFactories(WidgetRegistry.getInstance().getWidgetFactories());
    }

    /**
     * Returns if the given element or it's descendants are inline editable.<p>
     * 
     * @param element the element
     * 
     * @return <code>true</code> if the element has editable descendants
     */
    public static boolean hasEditable(Element element) {

        List<Element> children = Vie.getInstance().find("[property^=\"opencms://\"]", element);
        return (children != null) && !children.isEmpty();
    }

    /**
     * Replaces the id's within about attributes of the given element and all it's children.<p>
     * 
     * @param element the element
     * @param oldId the old id
     * @param newId the new id
     */
    public static void replaceResourceIds(Element element, String oldId, String newId) {

        String about = element.getAttribute("about");
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(about)) {
            about = about.replace(oldId, newId);
            element.setAttribute("about", about);
        }
        Element child = element.getFirstChildElement();
        while (child != null) {
            replaceResourceIds(child, oldId, newId);
            child = child.getNextSiblingElement();
        }
    }

    /**
     * Sets all annotated child elements editable.<p>
     * 
     * @param element the element
     * @param editable <code>true</code> to enable editing
     * 
     * @return <code>true</code> if the element had editable elements 
     */
    public static boolean setEditable(Element element, boolean editable) {

        I_CmsLayoutBundle.INSTANCE.editorCss().ensureInjected();
        List<Element> children = Vie.getInstance().select("[property^=\"opencms://\"]", element);
        if (children.size() > 0) {
            for (Element child : children) {
                if (editable) {
                    child.setAttribute("contentEditable", "true");
                    child.addClassName(I_CmsLayoutBundle.INSTANCE.editorCss().inlineEditable());
                } else {
                    child.removeAttribute("contentEditable");
                    child.removeClassName(I_CmsLayoutBundle.INSTANCE.editorCss().inlineEditable());
                }
            }
            return true;
        }
        return false;
    }

    /**
     * @see com.alkacon.acacia.client.EditorBase#getService()
     */
    @Override
    public I_CmsContentServiceAsync getService() {

        return m_service;
    }

    /**
     * Loads the content definition for the given entity and executes the callback on success.<p>
     * 
     * @param entityId the entity id
     * @param callback the callback
     */
    public void loadDefinition(final String entityId, final I_CmsSimpleCallback<CmsContentDefinition> callback) {

        CmsRpcAction<CmsContentDefinition> action = new CmsRpcAction<CmsContentDefinition>() {

            @Override
            public void execute() {

                start(0, true);
                getService().loadDefinition(entityId, this);
            }

            @Override
            protected void onResponse(final CmsContentDefinition result) {

                registerContentDefinition(result);
                WidgetRegistry.getInstance().registerExternalWidgets(
                    result.getExternalWidgetConfigurations(),
                    new Command() {

                        public void execute() {

                            stop(false);
                            callback.execute(result);
                        }
                    });
            }
        };
        action.execute();
    }

    /**
     * Loads the content definition for the given entity and executes the callback on success.<p>
     * 
     * @param entityId the entity id
     * @param newLink the new link
     * @param modelFileId  the model file id
     * @param callback the callback
     */
    public void loadDefinition(
        final String entityId,
        final String newLink,
        final CmsUUID modelFileId,
        final I_CmsSimpleCallback<CmsContentDefinition> callback) {

        CmsRpcAction<CmsContentDefinition> action = new CmsRpcAction<CmsContentDefinition>() {

            @Override
            public void execute() {

                start(0, true);
                getService().loadDefinition(entityId, newLink, modelFileId, this);
            }

            @Override
            protected void onResponse(final CmsContentDefinition result) {

                if (result.isModelInfo()) {
                    callback.execute(result);
                } else {
                    registerContentDefinition(result);
                    WidgetRegistry.getInstance().registerExternalWidgets(
                        result.getExternalWidgetConfigurations(),
                        new Command() {

                            public void execute() {

                                stop(false);
                                callback.execute(result);
                            }
                        });
                }
            }
        };
        action.execute();
    }

    /**
     * Loads the content definition for the given entity and executes the callback on success.<p>
     * 
     * @param entityId the entity id
     * @param callback the callback
     */
    public void loadNewDefinition(final String entityId, final I_CmsSimpleCallback<CmsContentDefinition> callback) {

        CmsRpcAction<CmsContentDefinition> action = new CmsRpcAction<CmsContentDefinition>() {

            @Override
            public void execute() {

                getService().loadNewDefinition(entityId, this);
            }

            @Override
            protected void onResponse(final CmsContentDefinition result) {

                registerContentDefinition(result);
                WidgetRegistry.getInstance().registerExternalWidgets(
                    result.getExternalWidgetConfigurations(),
                    new Command() {

                        public void execute() {

                            stop(false);
                            callback.execute(result);
                        }
                    });
            }
        };
        action.execute();
    }

    /**
     * Registers a deep copy of the source entity with the given target entity id.<p>
     * 
     * @param sourceEntityId the source entity id
     * @param targetEntityId the target entity id
     */
    public void registerClonedEntity(String sourceEntityId, String targetEntityId) {

        Vie.getInstance().getEntity(sourceEntityId).createDeepCopy(targetEntityId);
    }

    /**
     * Saves the given entities.<p>
     * 
     * @param entities the entities to save
     * @param deletedEntites the deleted entity id's
     * @param clearOnSuccess <code>true</code> to clear the VIE instance on success
     * @param callback the call back command
     */
    public void saveAndDeleteEntities(
        final List<com.alkacon.acacia.shared.Entity> entities,
        final List<String> deletedEntites,
        final boolean clearOnSuccess,
        final Command callback) {

        CmsRpcAction<ValidationResult> asyncCallback = new CmsRpcAction<ValidationResult>() {

            @Override
            public void execute() {

                start(200, true);
                getService().saveAndDeleteEntities(entities, deletedEntites, this);
            }

            @Override
            protected void onResponse(ValidationResult result) {

                stop(false);
                callback.execute();
                if (clearOnSuccess) {
                    destroyFrom(true);
                }
            }
        };
        asyncCallback.execute();
    }

    /**
     * Saves the given entities.<p>
     * 
     * @param entities the entities to save
     * @param deletedEntites the deleted entity id's
     * @param clearOnSuccess <code>true</code> to clear the VIE instance on success
     * @param callback the call back command
     */
    public void saveAndDeleteEntities(
        final Set<String> entities,
        final Set<String> deletedEntites,
        final boolean clearOnSuccess,
        final Command callback) {

        List<com.alkacon.acacia.shared.Entity> changedEntites = new ArrayList<com.alkacon.acacia.shared.Entity>();
        for (String entityId : entities) {
            I_Entity entity = m_vie.getEntity(entityId);
            if (entity != null) {
                changedEntites.add(com.alkacon.acacia.shared.Entity.serializeEntity(entity));
            }
        }
        saveAndDeleteEntities(changedEntites, new ArrayList<String>(deletedEntites), clearOnSuccess, callback);
    }

    /**
     * Saves the given entity.<p>
     * 
     * @param entity the entity
     * @param clearOnSuccess <code>true</code> to clear all entities from VIE on success
     * @param callback the callback executed on success
     */
    @Override
    public void saveEntity(final I_Entity entity, final boolean clearOnSuccess, final Command callback) {

        CmsRpcAction<ValidationResult> action = new CmsRpcAction<ValidationResult>() {

            @Override
            public void execute() {

                start(0, true);
                getService().saveEntity(com.alkacon.acacia.shared.Entity.serializeEntity(entity), this);

            }

            @Override
            protected void onResponse(ValidationResult result) {

                callback.execute();
                if (clearOnSuccess) {
                    destroyFrom(true);
                }
                stop(true);
            }
        };
        action.execute();
    }

    /**
     * Sets the show editor help flag to the user session.<p>
     * 
     * @param show the show editor help flag
     */
    public void setShowEditorHelp(final boolean show) {

        CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                getCoreService().setShowEditorHelp(show, this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(Void result) {

                //nothing to do
            }
        };
        action.execute();
    }

    /**
     * Removes the given entity from the entity VIE store.<p>
     * 
     * @param entityId the entity id
     */
    public void unregistereEntity(String entityId) {

        Vie.getInstance().removeEntity(entityId);
    }

    /**
     * Returns the core RPC service.<p>
     * 
     * @return the core service
     */
    protected I_CmsCoreServiceAsync getCoreService() {

        if (m_coreSvc == null) {
            m_coreSvc = CmsCoreProvider.getService();
        }
        return m_coreSvc;
    }
}
