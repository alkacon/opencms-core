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

package org.opencms.acacia.client.entity;

import org.opencms.acacia.shared.I_Entity;
import org.opencms.acacia.shared.I_EntityAttribute;
import org.opencms.acacia.shared.I_Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * The VIE wrapper singleton.<p>
 */
public final class Vie extends JavaScriptObject implements I_Vie {

    /** The singleton Vie instance. */
    private static Vie m_instance;

    /** Flag indicating that id's should always be wrapped in '<>' brackets. */
    private static boolean OVERRIDE_BRACKET_WRAPPED_NAMES = true;

    /** The global VIE instance variable name. */
    private static final String VIE_INSTANCE = "comAlkaconVieClientVIE";

    /**
     * Protected constructor, needed for sub classes of GWT-JavaScriptObjects.<p>
     */
    protected Vie() {

        // noop
    }

    /**
     * Adds pointy brackets to the given value if not already present.<p>
     * 
     * @param value the value
     * 
     * @return the value wrapped in pointy brackets
     */
    public static String addPointyBrackets(String value) {

        if (OVERRIDE_BRACKET_WRAPPED_NAMES
            && ((value.indexOf("<") != 0) || (value.lastIndexOf(">") != (value.length() - 1)))) {
            value = "<" + value + ">";
        }
        return value;
    }

    /**
     * Returns the vie instance.<p>
     * 
     * @return the vie instance
     */
    public static Vie getInstance() {

        if (m_instance == null) {
            m_instance = createInstance();
        }
        return m_instance;
    }

    /**
     * Removes pointy brackets from beginning and end of the given string.<p>
     * 
     * @param value the value to manipulate
     * 
     * @return the bracket less string
     */
    public static String removePointyBrackets(String value) {

        if (OVERRIDE_BRACKET_WRAPPED_NAMES
            && (value.indexOf("<") == 0)
            && (value.lastIndexOf(">") == (value.length() - 1))) {
            value = value.substring(1, value.length() - 1);
        }
        return value;
    }

    /**
     * Sets the use bracket wrapped id's flag.<p>
     * 
     * @param overrideBrackets <code>true</code> to use bracket wrapped id's
     */
    public static void setOverrideBracketWrappedNames(boolean overrideBrackets) {

        OVERRIDE_BRACKET_WRAPPED_NAMES = overrideBrackets;
    }

    /**
     * Creates a new JS VIE object instance.<p>
     * 
     * @return the JS VIE object
     */
    private static native Vie createInstance() /*-{
                                               if ($wnd[@org.opencms.acacia.client.entity.Vie::VIE_INSTANCE]&&$wnd[@org.opencms.acacia.client.entity.Vie::VIE_INSTANCE].types){
                                               // there is already a VIE instance within the window context, use that one
                                               return $wnd[@org.opencms.acacia.client.entity.Vie::VIE_INSTANCE];
                                               }else{
                                               $wnd[@org.opencms.acacia.client.entity.Vie::VIE_INSTANCE]=new $wnd.VIE();
                                               return $wnd[@org.opencms.acacia.client.entity.Vie::VIE_INSTANCE];
                                               }
                                               }-*/;

    /**
     * Binds a given callback to the entities of vie.<p>
     * 
     * @param functionName the name of the function
     * @param callback the function that should be executed
     */
    public native void bindFunctionToEntities(String functionName, I_EntityCallback callback)/*-{

                                                                                             this.entities
                                                                                             .bind(
                                                                                             functionName,
                                                                                             function(entity) {
                                                                                             callback.@org.opencms.acacia.client.entity.I_EntityCallback::execute(Lorg/opencms/acacia/client/entity/Entity;)(entity);
                                                                                             });
                                                                                             }-*/;

    /**
     * @see org.opencms.acacia.client.entity.I_Vie#changeEntityContentValues(org.opencms.acacia.client.entity.Entity, org.opencms.acacia.shared.I_Entity)
     */
    public void changeEntityContentValues(Entity original, I_Entity newContent) {

        clearEntityAttributes(original);
        for (I_EntityAttribute attribute : newContent.getAttributes()) {
            if (attribute.isSimpleValue()) {
                for (String value : attribute.getSimpleValues()) {
                    original.addAttributeValue(attribute.getAttributeName(), value);
                }
            } else {
                for (I_Entity value : attribute.getComplexValues()) {
                    original.addAttributeValue(attribute.getAttributeName(), Vie.getInstance().registerEntity(value));
                }
            }
        }
    }

    /**
     * @see org.opencms.acacia.client.entity.I_Vie#clearEntities()
     */
    public native void clearEntities() /*-{

                                       this.entities.reset();
                                       }-*/;

    /**
     * Creates a new entity registering it within VIE.<p>
     * 
     * @param entityId the entity id
     * @param entityTypeName the entity type
     * 
     * @return the new entity
     */
    public native I_Entity createEntity(String entityId, String entityTypeName) /*-{
                                                                                var entityType = this.types
                                                                                .get(@org.opencms.acacia.client.entity.Vie::addPointyBrackets(Ljava/lang/String;)(entityTypeName));
                                                                                var entityInstance;
                                                                                if (entityType != null) {
                                                                                // if the type is available, use it to create the new instance
                                                                                var attributes = {};
                                                                                if (entityId == null) {
                                                                                // create a new generic id
                                                                                entityId=entityTypeName+"/"+(new Date()).getTime();
                                                                                var newIdStump=entityId;
                                                                                var i=0;
                                                                                // make sure the id has not een registered yet
                                                                                while (this.entities.get(@org.opencms.acacia.client.entity.Vie::addPointyBrackets(Ljava/lang/String;)(entityId))!=null){
                                                                                	entityId=newIdStump+"#"+i;
                                                                                	i++;
                                                                                }
                                                                                }
                                                                                attributes['@subject'] = @org.opencms.acacia.client.entity.Vie::addPointyBrackets(Ljava/lang/String;)(entityId);
                                                                                entityInstance = entityType.instance(attributes);
                                                                                } else {
                                                                                throw Error('Type has not been registered yet.');
                                                                                }
                                                                                return this.entities.addOrUpdate(entityInstance);
                                                                                }-*/;

    /**
      * @see org.opencms.acacia.client.entity.I_Vie#createType(java.lang.String)
      */
    public native I_Type createType(String id) /*-{
                                               id = @org.opencms.acacia.client.entity.Vie::addPointyBrackets(Ljava/lang/String;)(id);
                                               var type = new this.Type(id);

                                               // all type inherit from owl:Thing
                                               type.inherit("owl:Thing");
                                               this.types.add(type);
                                               return type;
                                               }-*/;

    /**
     * Returns all descending elements and self that match the given selector.<p>
     * 
     * @param selector the selector
     * @param context the context element, if <code>null</code> the body element is used as context
     * 
     * @return the matching elements
     */
    public List<Element> find(String selector, Element context) {

        JsArray<Element> results = JavaScriptObject.createArray().cast();
        if (context == null) {
            context = RootPanel.getBodyElement();
        }
        results = find(selector, context, results);
        List<Element> elements = new ArrayList<Element>();
        for (int i = 0; i < results.length(); i++) {
            elements.add(results.get(i));
        }
        return elements;
    }

    /**
     * @see org.opencms.acacia.client.entity.I_Vie#getAttributeElements(com.google.gwt.dom.client.Element)
     */
    public List<Element> getAttributeElements(Element context) {

        return select("[property]", context);
    }

    /**
     * @see org.opencms.acacia.client.entity.I_Vie#getAttributeElements(org.opencms.acacia.shared.I_Entity, java.lang.String, com.google.gwt.dom.client.Element)
     */
    public List<Element> getAttributeElements(I_Entity entity, String attributeName, Element context) {

        return getAttributeElements(entity.getId(), attributeName, context);
    }

    /**
     * @see org.opencms.acacia.client.entity.I_Vie#getAttributeElements(java.lang.String, java.lang.String, com.google.gwt.dom.client.Element)
     */
    public List<Element> getAttributeElements(String entityId, String attributeName, Element context) {

        JsArray<Element> aboutElements = JavaScriptObject.createArray().cast();
        if (context == null) {
            context = RootPanel.getBodyElement();
        }
        aboutElements = find("[about='" + entityId + "']", context, aboutElements);
        JsArray<Element> results = JavaScriptObject.createArray().cast();
        for (int i = 0; i < aboutElements.length(); i++) {
            find("[property~='" + attributeName + "']", aboutElements.get(i), results);
        }
        List<Element> elements = new ArrayList<Element>();
        for (int i = 0; i < results.length(); i++) {
            // prevent duplicate entries
            if (!elements.contains(results.get(i))) {
                elements.add(results.get(i));
            }
        }
        return elements;
    }

    /**
     * Returns the element subject.<p>
     * 
     * @param element the DOM element
     * 
     * @return the elements subject
     */
    public native String getElementPredicate(Element element) /*-{

                                                              return this.services.rdfa.getElementPredicate(element);
                                                              }-*/;

    /**
     * Returns the element subject.<p>
     * 
     * @param element the DOM element
     * 
     * @return the elements subject
     */
    public native String getElementSubject(Element element) /*-{

                                                            return this.services.rdfa.getElementSubject(element);
                                                            }-*/;

    /**
     * Returns the entities of vie.<p>
     * 
     * @return the entities
     */
    public native I_EntityCollection getEntities() /*-{

                                                   return this.entities;
                                                   }-*/;

    /**
     * Returns the entity with the given id.<p>
     * 
     * @param entityId the entity id
     * 
     * @return the entity
     */
    public native I_Entity getEntity(String entityId) /*-{

                                                      return this.entities
                                                      .get(@org.opencms.acacia.client.entity.Vie::addPointyBrackets(Ljava/lang/String;)(entityId));
                                                      }-*/;

    /**
     * @see org.opencms.acacia.client.entity.I_Vie#getType(java.lang.String)
     */
    public native I_Type getType(String id) /*-{

                                            return this.types
                                            .get(@org.opencms.acacia.client.entity.Vie::addPointyBrackets(Ljava/lang/String;)(id));
                                            }-*/;

    /**
     * @see org.opencms.acacia.client.entity.I_Vie#load(java.lang.String, java.lang.String, org.opencms.acacia.client.entity.I_EntityArrayCallback)
     */
    public void load(String service, String selector, I_EntityArrayCallback callback) {

        loadInternal(service, selector, callback);
    }

    /**
    * @see org.opencms.acacia.client.entity.I_Vie#registerEntity(org.opencms.acacia.shared.I_Entity)
    */
    public I_Entity registerEntity(I_Entity entity) {

        return registerEntity(entity, false);
    }

    /**
     * @see org.opencms.acacia.client.entity.I_Vie#registerEntity(org.opencms.acacia.shared.I_Entity, boolean)
     */
    public I_Entity registerEntity(I_Entity entity, boolean discardIds) {

        String id = discardIds ? null : entity.getId();
        I_Entity result = createEntity(id, entity.getTypeName());
        for (I_EntityAttribute attribute : entity.getAttributes()) {
            if (attribute.isSimpleValue()) {
                for (String value : attribute.getSimpleValues()) {
                    result.addAttributeValue(attribute.getAttributeName(), value);
                }
            } else {
                for (I_Entity value : attribute.getComplexValues()) {
                    result.addAttributeValue(attribute.getAttributeName(), registerEntity(value, discardIds));
                }
            }
        }
        return result;
    }

    /**
     * @see org.opencms.acacia.client.entity.I_Vie#registerTypes(org.opencms.acacia.shared.I_Type, java.util.Map)
     */
    public void registerTypes(I_Type type, Map<String, I_Type> types) {

        if (getType(type.getId()) != null) {
            // type already registered
            return;
        }
        I_Type regType = createType(type.getId());
        if (type.isSimpleType()) {
            return;
        }
        regType.setChoiceMaxOccurrence(type.getChoiceMaxOccurrence());
        for (String attributeName : type.getAttributeNames()) {
            String attributeType = type.getAttributeTypeName(attributeName);
            registerTypes(types.get(attributeType), types);
            regType.addAttribute(
                attributeName,
                attributeType,
                type.getAttributeMinOccurrence(attributeName),
                type.getAttributeMaxOccurrence(attributeName));
        }
    }

    /**
     * @see org.opencms.acacia.client.entity.I_Vie#removeEntity(java.lang.String)
     */
    public native void removeEntity(String entityId) /*-{
                                                     var self = this;
                                                     var removeEntity = function(entity) {
                                                     if (entity != null && entity.isEntity) {
                                                     var attributes = entity.attributes;
                                                     if (attributes != null) {
                                                     for (key in attributes) {
                                                     var attribute = entity.get(key);
                                                     if (attribute != null && attribute.isCollection) {
                                                     for ( var i = 0; i < attribute.size(); i++) {
                                                     var child = attribute.at(i);
                                                     removeEntity(child);
                                                     }
                                                     }
                                                     }
                                                     }
                                                     self.entities.remove(entity);
                                                     }
                                                     }
                                                     var entity = self.entities
                                                     .get(@org.opencms.acacia.client.entity.Vie::addPointyBrackets(Ljava/lang/String;)(entityId));
                                                     removeEntity(entity);
                                                     }-*/;

    /**
     * Returns a list of elements matching the given CSS selector.<p>
     * 
     * @param selector the selector
     * @param context the context element, if <code>null</code> the body element is used as context
     * 
     * @return the element list
     */
    public List<Element> select(String selector, Element context) {

        JsArray<Element> results = JavaScriptObject.createArray().cast();
        if (context == null) {
            context = RootPanel.getBodyElement();
        }
        results = select(selector, context, results);
        List<Element> elements = new ArrayList<Element>();
        for (int i = 0; i < results.length(); i++) {
            elements.add(results.get(i));
        }
        return elements;
    }

    /**
     * Sets VIE to use the RDFA service.<p>
     */
    public native void useRdfaService() /*-{
                                        this.use(new this.RdfaService());
                                        }-*/;

    /**
     * Removes all attributes from the given entity.<p>
     * 
     * @param entity the entity
     */
    private void clearEntityAttributes(Entity entity) {

        for (I_EntityAttribute attribute : entity.getAttributes()) {
            if (attribute.isComplexValue()) {
                for (I_Entity child : attribute.getComplexValues()) {
                    clearEntityAttributes((Entity)child);
                    removeEntity((Entity)child);
                }
            }
            entity.removeAttributeSilent(attribute.getAttributeName());
        }
    }

    /**
     * Returns all descending elements and self that match the given selector.<p>
     * 
     * @param selector the selector
     * @param context the context element, if <code>null</code> the body element is used as context
     * @param results the array to add the matching elements to
     * 
     * @return the element array
     */
    private native JsArray<Element> find(String selector, Element context, JsArray<Element> results) /*-{
                                                                                                     if (context != null) {
                                                                                                     this.jQuery(context).filter(selector).each(function() {
                                                                                                     results.push(this);
                                                                                                     });
                                                                                                     }
                                                                                                     this.jQuery(context).find(selector).each(function() {
                                                                                                     results.push(this);
                                                                                                     });
                                                                                                     return results;
                                                                                                     }-*/;

    /**
     * Executes the load function on the VIE instance.<p>
     * 
     * @param service the name of the service to use
     * @param selector the jQuery selector to specify the HTML-Elements inside the DOM to search for entities
     * @param callback the callback that is executed on success
     * 
     * @see org.opencms.acacia.client.entity.I_Vie#load(java.lang.String, java.lang.String, org.opencms.acacia.client.entity.I_EntityArrayCallback)
     */
    private native void loadInternal(String service, String selector, I_EntityArrayCallback callback) /*-{

                                                                                                      var vie = @org.opencms.acacia.client.entity.Vie::m_instance;
                                                                                                      var call = function(entities) {
                                                                                                      callback.@org.opencms.acacia.client.entity.I_EntityArrayCallback::execute(Lcom/google/gwt/core/client/JsArray;)(entities);
                                                                                                      }
                                                                                                      vie.load({
                                                                                                      element : $wnd.jQuery(selector)
                                                                                                      }).using(service).execute().success(call);
                                                                                                      }-*/;

    /**
     * Removes the entity.<p>
     * 
     * @param entity the entity
     */
    private native void removeEntity(Entity entity) /*-{
                                                    this.entities.remove(entity);
                                                    }-*/;

    /**
     * Returns an array of elements matching the given CSS selector.<p>
     * 
     * @param selector the selector
     * @param context the context element, if <code>null</code> the body element is used as context
     * @param results the array to add the matching elements to
     * 
     * @return the element array
     */
    private native JsArray<Element> select(String selector, Element context, JsArray<Element> results) /*-{

                                                                                                       this.jQuery(selector, context).each(function() {
                                                                                                       results.push(this);
                                                                                                       });
                                                                                                       return results;
                                                                                                       }-*/;
}
