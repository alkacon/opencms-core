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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * The entity wrapper.<p>
 */
public final class Entity extends JavaScriptObject implements HasValueChangeHandlers<Entity>, I_Entity {

    /** Place holder value for empty strings. */
    // HACK: this place holder is only used because the current native VIE implementation does
    // not support empty strings as values
    private static final String EMPTY_STRING = "########empty-string########";

    /**
     * Constructor, for internal use only.<p>
     */
    protected Entity() {

    }

    /**
     * Helper method for firing a 'value changed' event.<p>
     *
     * @param entity the entity that changed
     */
    private static void fireValueChangedEvent(Entity entity) {

        ValueChangeEvent.fire(entity, entity);
    }

    /**
     * @see org.opencms.acacia.shared.I_Entity#addAttributeValue(java.lang.String, org.opencms.acacia.shared.I_Entity)
     */
    public void addAttributeValue(String attributeName, I_Entity value) {

        internalAddAttributeValue(attributeName, value);
        registerChange((Entity)value);
    }

    /**
     * @see org.opencms.acacia.shared.I_Entity#addAttributeValue(java.lang.String, java.lang.String)
     */
    public native void addAttributeValue(String attributeName, String value) /*-{
                                                                             if (value == "") {
                                                                             value = @org.opencms.acacia.client.entity.Entity::EMPTY_STRING;
                                                                             }
                                                                             this
                                                                             .setOrAdd(
                                                                             @org.opencms.acacia.client.entity.Vie::addPointyBrackets(Ljava/lang/String;)(attributeName),
                                                                             value);
                                                                             }-*/;

    /**
     * Adds this handler to the widget.
     *
     * @param <H> the type of handler to add
     * @param type the event type
     * @param handler the handler
     * 
     * @return {@link HandlerRegistration} used to remove the handler
     */
    public <H extends EventHandler> HandlerRegistration addHandler(final H handler, GwtEvent.Type<H> type) {

        return ensureHandlers().addHandler(type, handler);
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Entity> handler) {

        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see org.opencms.acacia.shared.I_Entity#createDeepCopy(java.lang.String)
     */
    public I_Entity createDeepCopy(String entityId) {

        I_Entity result = Vie.getInstance().createEntity(entityId, getTypeName());
        for (I_EntityAttribute attribute : getAttributes()) {
            if (attribute.isSimpleValue()) {
                List<String> values = attribute.getSimpleValues();
                for (String value : values) {
                    result.addAttributeValue(attribute.getAttributeName(), value);
                }
            } else {
                List<I_Entity> values = attribute.getComplexValues();
                for (I_Entity value : values) {
                    I_Entity valueCopy = ((Entity)value).createDeepCopy(null);
                    result.addAttributeValue(attribute.getAttributeName(), valueCopy);
                }
            }
        }
        return result;
    }

    /**
     * @see com.google.gwt.event.shared.HasHandlers#fireEvent(com.google.gwt.event.shared.GwtEvent)
     */
    public void fireEvent(GwtEvent<?> event) {

        if (getHandlerManager() != null) {
            getHandlerManager().fireEvent(event);
        }

    }

    /**
     * @see org.opencms.acacia.shared.I_Entity#getAttribute(java.lang.String)
     */
    public I_EntityAttribute getAttribute(String attributeName) {

        String internaltAttributeName = Vie.addPointyBrackets(attributeName);
        if (!hasAttribute(internaltAttributeName)) {
            return null;
        }
        if (isSimpleAttribute(internaltAttributeName)) {
            return new EntityAttribute(attributeName, getSimpleValues(internaltAttributeName));
        }
        return new EntityAttribute(attributeName, getComplexValues(internaltAttributeName));
    }

    /**
     * @see org.opencms.acacia.shared.I_Entity#getAttributes()
     */
    public List<I_EntityAttribute> getAttributes() {

        List<I_EntityAttribute> result = new ArrayList<I_EntityAttribute>();
        JsArrayString attributeNames = getAttributeNames();
        for (int i = 0; i < attributeNames.length(); i++) {
            I_EntityAttribute attribute = getAttribute(attributeNames.get(i));
            if (attribute != null) {
                result.add(attribute);
            }
        }
        return result;
    }

    /**
     * @see org.opencms.acacia.shared.I_Entity#getId()
     */
    public native String getId() /*-{
                                 try {
                                 var subject = this.getSubject();
                                 } catch (error) {
                                 console.log(error);
                                 }
                                 subject = @org.opencms.acacia.client.entity.Vie::removePointyBrackets(Ljava/lang/String;)(subject);
                                 return subject;
                                 }-*/;

    /**
     * @see org.opencms.acacia.shared.I_Entity#getTypeName()
     */
    public native String getTypeName() /*-{

                                       var type = this.get('@type');
                                       var result = (typeof type === 'string') ? type : type.id;
                                       return @org.opencms.acacia.client.entity.Vie::removePointyBrackets(Ljava/lang/String;)(result);
                                       }-*/;

    /**
     * @see org.opencms.acacia.shared.I_Entity#hasAttribute(java.lang.String)
     */
    public native boolean hasAttribute(String attributeName) /*-{

                                                             return this
                                                             .has(@org.opencms.acacia.client.entity.Vie::addPointyBrackets(Ljava/lang/String;)(attributeName));
                                                             }-*/;

    /**
     * Returns if the entity has the given type.<p>
     *
     * @param type the type
     *
     * @return <code>true</code> if the entity has the given type
     */
    public native boolean hasType(String type) /*-{

                                               return this
                                               .hasType(@org.opencms.acacia.client.entity.Vie::addPointyBrackets(Ljava/lang/String;)(type));
                                               }-*/;

    /**
     * @see org.opencms.acacia.shared.I_Entity#insertAttributeValue(java.lang.String, org.opencms.acacia.shared.I_Entity, int)
     */
    public void insertAttributeValue(String attributeName, I_Entity value, int index) {

        I_EntityAttribute attribute = getAttribute(attributeName);
        if (index < 0) {
            throw new IndexOutOfBoundsException("Index should be > 0");
        }
        if ((attribute == null) && (index > 0)) {
            throw new IndexOutOfBoundsException("Index of " + index + " to big.");
        }
        if (attribute == null) {
            setAttributeValue(attributeName, value);
        } else {
            List<I_Entity> values = attribute.getComplexValues();
            if (index > values.size()) {
                throw new IndexOutOfBoundsException("Index of " + index + " to big.");
            }
            if (index == values.size()) {
                addAttributeValue(attributeName, value);
            } else {
                removeAttributeSilent(attributeName);
                for (int i = 0; i < values.size(); i++) {
                    if (i == index) {
                        addAttributeValue(attributeName, value);
                    }
                    addAttributeValue(attributeName, values.get(i));

                }
            }
        }
    }

    /**
     * @see org.opencms.acacia.shared.I_Entity#insertAttributeValue(java.lang.String, java.lang.String, int)
     */
    public void insertAttributeValue(String attributeName, String value, int index) {

        I_EntityAttribute attribute = getAttribute(attributeName);
        if (index < 0) {
            throw new IndexOutOfBoundsException("Index should be > 0");
        }
        if ((attribute == null) && (index > 0)) {
            throw new IndexOutOfBoundsException("Index of " + index + " to big.");
        }
        if (attribute == null) {
            setAttributeValue(attributeName, value);
        } else {
            List<String> values = attribute.getSimpleValues();
            if (index > values.size()) {
                throw new IndexOutOfBoundsException("Index of " + index + " to big.");
            }
            if (index == values.size()) {
                addAttributeValue(attributeName, value);
            } else {
                removeAttributeSilent(attributeName);
                for (int i = 0; i < values.size(); i++) {
                    if (i == index) {
                        addAttributeValue(attributeName, value);
                    }
                    addAttributeValue(attributeName, values.get(i));

                }
            }
        }
    }

    /**
     * Removes the given attribute.<p>
     *
     * @param attributeName the attribute name
     */
    public native void removeAttribute(String attributeName) /*-{

                                                             this
                                                             .unset(@org.opencms.acacia.client.entity.Vie::addPointyBrackets(Ljava/lang/String;)(attributeName));
                                                             }-*/;

    /**
     * @see org.opencms.acacia.shared.I_Entity#removeAttributeSilent(java.lang.String)
     */
    public native void removeAttributeSilent(String attributeName) /*-{

                                                                   this
                                                                   .unset(
                                                                   @org.opencms.acacia.client.entity.Vie::addPointyBrackets(Ljava/lang/String;)(attributeName),
                                                                   {
                                                                   silent : true
                                                                   });
                                                                   }-*/;

    /**
     * @see org.opencms.acacia.shared.I_Entity#removeAttributeValue(java.lang.String, int)
     */
    public void removeAttributeValue(String attributeName, int index) {

        if (!hasAttribute(attributeName)) {
            return;
        }
        I_EntityAttribute attribute = getAttribute(attributeName);
        if (attribute.isSingleValue() && (index == 0)) {
            removeAttribute(attributeName);
        } else {
            removeAttributeSilent(attributeName);
            if (attribute.isSimpleValue()) {
                for (int i = 0; i < attribute.getSimpleValues().size(); i++) {
                    if (i != index) {
                        addAttributeValue(attributeName, attribute.getSimpleValues().get(i));
                    }
                }

            } else {
                for (int i = 0; i < attribute.getComplexValues().size(); i++) {
                    if (i != index) {
                        addAttributeValue(attributeName, attribute.getComplexValues().get(i));
                    }
                }
            }
        }
    }

    /**
     * @see org.opencms.acacia.shared.I_Entity#setAttributeValue(java.lang.String, org.opencms.acacia.shared.I_Entity)
     */
    public void setAttributeValue(String attributeName, I_Entity value) {

        internalSetAttributeValue(attributeName, value);
        registerChange((Entity)value);
    }

    /**
     * @see org.opencms.acacia.shared.I_Entity#setAttributeValue(java.lang.String, org.opencms.acacia.shared.I_Entity, int)
     */
    public void setAttributeValue(String attributeName, I_Entity value, int index) {

        if (!(value instanceof Entity)) {
            throw new UnsupportedOperationException("May only set native entities as values.");
        }
        if ((index == 0) && !hasAttribute(attributeName)) {
            setAttributeValue(attributeName, value);
        }
        I_EntityAttribute attribute = getAttribute(attributeName);
        if ((index == 0) && attribute.isSingleValue()) {
            setAttributeValue(attributeName, value);
        } else {
            List<I_Entity> values = attribute.getComplexValues();
            if (index >= values.size()) {
                throw new IndexOutOfBoundsException("Index of " + index + " to big.");
            }
            removeAttributeSilent(attributeName);
            for (int i = 0; i < values.size(); i++) {
                if (i == index) {
                    addAttributeValue(attributeName, value);
                } else {
                    addAttributeValue(attributeName, values.get(i));
                }
            }
        }

    }

    /**
     * @see org.opencms.acacia.shared.I_Entity#setAttributeValue(java.lang.String, java.lang.String)
     */
    public native void setAttributeValue(String attributeName, String value) /*-{
                                                                             if (value == "") {
                                                                             value = @org.opencms.acacia.client.entity.Entity::EMPTY_STRING;
                                                                             }
                                                                             attributeName = @org.opencms.acacia.client.entity.Vie::addPointyBrackets(Ljava/lang/String;)(attributeName);
                                                                             this.unset(attributeName, {
                                                                             silent : true
                                                                             });
                                                                             this.setOrAdd(attributeName, value);
                                                                             }-*/;

    /**
     * @see org.opencms.acacia.shared.I_Entity#setAttributeValue(java.lang.String, java.lang.String, int)
     */
    public void setAttributeValue(String attributeName, String value, int index) {

        I_EntityAttribute attribute = getAttribute(attributeName);
        if ((attribute == null) || ((index == 0) && attribute.isSingleValue())) {
            setAttributeValue(attributeName, value);
        } else {
            List<String> values = attribute.getSimpleValues();
            if (index >= values.size()) {
                throw new IndexOutOfBoundsException("Index of " + index + " to big.");
            }
            removeAttributeSilent(attributeName);
            for (int i = 0; i < values.size(); i++) {
                if (i == index) {
                    addAttributeValue(attributeName, value);
                } else {
                    addAttributeValue(attributeName, values.get(i));
                }
            }
        }
    }

    /**
     * @see org.opencms.acacia.shared.I_Entity#toJSON()
     */
    public native String toJSON() /*-{

                                  return JSON.stringify(this);
                                  }-*/;

    /**
     * Sets the entity changed, triggering the value changed event.<p>
     */
    protected native void setChanged() /*-{
                                       this.change();
                                       }-*/;

    /**
     * Binds the {@link #org.opencms.acacia.client.entity.Entity.fireValueChangedEvent(Entity)} method 
     * to the native change function and sets the handler manager for this instance.<p>
     *
     * @param handlerManager the handler manager to use
     */
    private native void bindChange(HandlerManager handlerManager)/*-{

                                                                 this.handlerManager = handlerManager;
                                                                 var self = this;
                                                                 this
                                                                 .bind(
                                                                 "change",
                                                                 function() {
                                                                 @org.opencms.acacia.client.entity.Entity::fireValueChangedEvent(Lorg/opencms/acacia/client/entity/Entity;)(self);
                                                                 });
                                                                 }-*/;

    /**
     * Ensures the existence of the handler manager.
     *
     * @return the handler manager
     * */
    private HandlerManager ensureHandlers() {

        if (getHandlerManager() == null) {
            bindChange(new HandlerManager(this));
        }
        return getHandlerManager();
    }

    /**
     * Returns the names of the available attributes.<p>
     * 
     * @return the attribute names
     */
    private native JsArrayString getAttributeNames() /*-{
                                                     var names = new Array();
                                                     var attributes = this.attributes;
                                                     for ( var key in attributes) {
                                                     names
                                                     .push(@org.opencms.acacia.client.entity.Vie::removePointyBrackets(Ljava/lang/String;)(key));
                                                     }
                                                     return names;
                                                     }-*/;

    /**
     * Returns the values of the given attribute as an array of entities.<p>
     * Check if the given attribute is of complex type first!!<p>
     * 
     * @param attributeName the name of the attribute 
     * 
     * @return the attribute values
     */
    private native I_EntityCollection getComplexValues(String attributeName) /*-{

                                                                             var attr = this.get(attributeName);
                                                                             return attr;
                                                                             }-*/;

    /**
     * Returns the handler manager.<p>
     *
     * @return the handler manager
     */
    private native HandlerManager getHandlerManager()/*-{

                                                     return this.handlerManager;
                                                     }-*/;

    /**
     * Returns the values of the given attribute as an array of entities.<p>
     * Check if the given attribute is of complex type first!!<p>
     * 
     * @param attributeName the name of the attribute 
     * 
     * @return the attribute values
     */
    private native JsArrayString getSimpleValues(String attributeName) /*-{

                                                                       var attr = this.get(attributeName);
                                                                       if (typeof attr === 'string') {
                                                                       attr = [ attr ];
                                                                       }
                                                                       if (attr != null) {
                                                                       for ( var i = 0; i < attr.length; i++) {
                                                                       if (attr[i] == @org.opencms.acacia.client.entity.Entity::EMPTY_STRING) {
                                                                       attr[i] = "";
                                                                       }
                                                                       }
                                                                       }
                                                                       return attr;
                                                                       }-*/;

    /**
     * Internal method to add a complex attribute value.<p>
     * 
     * @param attributeName the attribute name
     * @param value the value
     */
    private native void internalAddAttributeValue(String attributeName, I_Entity value) /*-{
                                                                                        this
                                                                                        .setOrAdd(
                                                                                        @org.opencms.acacia.client.entity.Vie::addPointyBrackets(Ljava/lang/String;)(attributeName),
                                                                                        value);
                                                                                        }-*/;

    /**
     * Internal method to set a complex attribute value.<p>
     * 
     * @param attributeName the attribute name
     * @param value the value
     */
    private native void internalSetAttributeValue(String attributeName, I_Entity value) /*-{
                                                                                        attributeName = @org.opencms.acacia.client.entity.Vie::addPointyBrackets(Ljava/lang/String;)(attributeName);
                                                                                        this.unset(attributeName, {
                                                                                        silent : true
                                                                                        });
                                                                                        this.set(attributeName, value);
                                                                                        }-*/;

    /**
     * Returns if the given attribute is of the simple type.<p>
     * 
     * @param attributeName the name of the attribute
     * 
     * @return <code>true</code> is this is a simple type attribute
     */
    private native boolean isSimpleAttribute(String attributeName) /*-{

                                                                   var attr = this.get(attributeName);
                                                                   if (typeof attr === 'string') {
                                                                   return true;
                                                                   }
                                                                   if (attr.isEntity) {
                                                                   return false;
                                                                   }
                                                                   if (Object.prototype.toString.call(attr) === '[object Array]') {
                                                                   if (typeof attr[0] === 'string') {
                                                                   return true;
                                                                   }
                                                                   }
                                                                   return false;
                                                                   }-*/;

    /**
     * Registers the change event of the new child to trigger change on this entity.<p>
     * 
     * @param child the child to register
     */
    private native void registerChange(Entity child) /*-{
                                                     var self = this;
                                                     child.bind("change", function() {
                                                     self.change();
                                                     });
                                                     }-*/;
}
