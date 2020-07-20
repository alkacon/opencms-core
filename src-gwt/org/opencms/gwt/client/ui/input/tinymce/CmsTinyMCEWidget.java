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

package org.opencms.gwt.client.ui.input.tinymce;

import org.opencms.gwt.client.I_CmsHasInit;
import org.opencms.gwt.client.ui.I_CmsAutoHider;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.client.ui.input.I_CmsFormWidget;
import org.opencms.gwt.client.ui.input.form.CmsWidgetFactoryRegistry;
import org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory;
import org.opencms.util.CmsStringUtil;

import java.util.Map;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.logical.shared.HasResizeHandlers;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * This class is used to start TinyMCE for editing the content of an element.<p>
 *
 * After constructing the instance, the actual editor is opened using the init() method, and destroyed with the close()
 * method. While the editor is opened, the edited contents can be accessed using the methods of the HasValue interface.
 */
public final class CmsTinyMCEWidget extends FlowPanel
implements I_CmsFormWidget, HasResizeHandlers, I_CmsHasInit, HasValueChangeHandlers<String> {

    /** Use as option to disallow any HTML or formatting the content. */
    public static final String NO_HTML_EDIT = "no_html_edit";

    /** The widget type id.*/
    public static final String WIDGET_TYPE = "wysiwyg";

    /** Counts currently attached widget instances, for use in registering / deregistering preview event listeners. */
    static int attachCount;

    /** The minimum editor height. */
    private static final int MIN_EDITOR_HEIGHT = 70;

    /** The preview handler registration. */
    private static HandlerRegistration previewRegistration;

    /** The current content. */
    protected String m_currentContent;

    /** The TinyMCE editor instance. */
    protected JavaScriptObject m_editor;

    /** The DOM ID of the editable element. */
    protected String m_id;

    /** The original HTML content of the editable element. */
    protected String m_originalContent;

    /** The maximal width of the widget. */
    protected int m_width;

    /** The editor height to set. */
    int m_editorHeight;

    /** The element to store the widget content in. */
    private Element m_contentElement;

    /** Flag controlling whether the widget is enabled. */
    private boolean m_enabled = true;

    /** Indicates the value has been set from external, not from within the widget. */
    private boolean m_externalValueChange;

    /** Indicating if the widget has been attached yet. */
    private boolean m_hasBeenAttached;

    /** Flag indicating the editor has been initialized. */
    private boolean m_initialized;

    /** The editor options. */
    private Object m_options;

    /** The previous value. */
    private String m_previousValue;

    /**
     * Creates a new instance with the given TinyMCE options. Use this constructor for form based editing.<p>
     *
     * @param options the tinyMCE editor options to extend the default settings
     */
    public CmsTinyMCEWidget(Object options) {

        // super(element);
        m_originalContent = "";
        m_options = options;
        // using a child DIV as content element
        m_contentElement = getElement().appendChild(DOM.createDiv());
    }

    /**
     * Creates a new instance based on configuration data from the server.<p>
     *
     * @param config the configuration data
     */
    public CmsTinyMCEWidget(String config) {

        this(CmsTinyMCEHelper.generateOptionsForTiny(config));
    }

    /**
     * Initializes this class.<p>
     */
    public static void initClass() {

        // registers a factory for creating new instances of this widget
        CmsWidgetFactoryRegistry.instance().registerFactory(WIDGET_TYPE, new I_CmsFormWidgetFactory() {

            /**
             * @see org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory#createWidget(java.util.Map, com.google.common.base.Optional)
             */
            public I_CmsFormWidget createWidget(Map<String, String> widgetParams, Optional<String> defaultValue) {

                String cfg = widgetParams.get("v");
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(cfg)) {
                    // something went wrong, fall back to text box
                    return new CmsTextBox();
                } else {
                    return new CmsTinyMCEWidget(decode(cfg));
                }
            }

            private String decode(String string) {

                StringBuffer result = new StringBuffer();
                for (String num : string.split(",")) {
                    result.append((char)(Integer.parseInt(num)));
                }
                return result.toString();
            }
        });
    }

    /**
     * Decrements the attach count, removes preview event handler when we go from 1 to 0.<p>
     */
    private static void decrementAttached() {

        attachCount -= 1;
        if ((attachCount < 1) && (previewRegistration != null)) {
            previewRegistration.removeHandler();
            previewRegistration = null;
        }

    }

    /**
     * Increments the attach count, installs preview event handler when we go from 0 to 1.<p>
     */
    private static void incrementAttached() {

        attachCount += 1;
        if (attachCount == 1) {
            // Prevent events on TinyMCE popups from being cancelled by the PopupPanel containing the property dialog

            previewRegistration = Event.addNativePreviewHandler(new NativePreviewHandler() {

                public void onPreviewNativeEvent(NativePreviewEvent pEvent) {

                    Event event = Event.as(pEvent.getNativeEvent());
                    EventTarget target = event.getEventTarget();
                    if (Element.is(target)) {
                        Element elem = Element.as(target);
                        while (elem != null) {
                            if (elem.getClassName().contains("mce-floatpanel")) {
                                pEvent.consume();
                                return;
                            }
                            elem = elem.getParentElement();
                        }

                    }

                }
            });

        }
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasResizeHandlers#addResizeHandler(com.google.gwt.event.logical.shared.ResizeHandler)
     */
    public HandlerRegistration addResizeHandler(ResizeHandler handler) {

        return addHandler(handler, ResizeEvent.getType());
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getApparentValue()
     */
    public String getApparentValue() {

        return getFormValueAsString();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFieldType()
     */
    public FieldType getFieldType() {

        return FieldType.STRING;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValue()
     */
    public Object getFormValue() {

        return getFormValueAsString();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValueAsString()
     */
    public String getFormValueAsString() {

        return getValue();

    }

    /**
     * Gets the main editable element.<p>
     *
     * @return the editable element
     */
    public Element getMainElement() {

        return m_contentElement;
    }

    /**
     * Gets the value.<p>
     *
     * @return the value
     */
    public String getValue() {

        if (m_editor != null) {
            return getContent().trim();
        }
        return m_originalContent.trim();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#isEnabled()
     */
    public boolean isEnabled() {

        return m_enabled;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#reset()
     */
    public void reset() {

        setFormValueAsString("");
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setAutoHideParent(org.opencms.gwt.client.ui.I_CmsAutoHider)
     */
    public void setAutoHideParent(I_CmsAutoHider autoHideParent) {

        // not supported
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setEnabled(boolean)
     *
     * Partial support: This only works before the actual TinyMCE instance is loaded.
     */
    public void setEnabled(boolean enabled) {

        m_enabled = enabled;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setErrorMessage(java.lang.String)
     */
    public void setErrorMessage(String errorMessage) {

        // not supported
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setFormValueAsString(java.lang.String)
     */
    public void setFormValueAsString(String value) {

        setValue(value, false);
    }

    /**
     * Sets the value.<p>
     *
     * @param value the value
     * @param fireEvents true if value change event should be fired
     */
    public void setValue(String value, boolean fireEvents) {

        if (value != null) {
            value = value.trim();
        }
        if (!Objects.equal(value, getValue())) {
            setPreviousValue(value);
            if (m_editor == null) {
                // editor has not been initialized yet
                m_originalContent = value;
            } else {
                m_externalValueChange = true;
                setContent(value);
            }
            if (fireEvents) {
                fireValueChange(true);
            }
        }

    }

    /**
     * Checks whether the necessary Javascript libraries are available by accessing them.
     */
    protected native void checkLibraries() /*-{
		// fail early if tinymce is not available
		var w = $wnd;
		var init = w.tinyMCE.init;
    }-*/;

    /**
     * Gives an element an id if it doesn't already have an id, and then returns the element's id.<p>
     *
     * @param element the element for which we want to add the id
     *
     * @return the id
     */
    protected String ensureId(Element element) {

        String id = element.getId();
        if ((id == null) || "".equals(id)) {
            id = Document.get().createUniqueId();
            element.setId(id);
        }
        return id;
    }

    /**
     * Fires a change event.<p>
     *
     * @param force true if the event should be fired even if the value does not differ from the previous one
     */
    protected void fireValueChange(boolean force) {

        String currentValue = getValue();
        if (force || !currentValue.equals(m_previousValue)) {
            m_previousValue = currentValue;
            ValueChangeEvent.fire(this, currentValue);
        }

    }

    /**
     * Returns the editor parent element.<p>
     *
     * @return the editor parent element
     */
    protected Element getEditorParentElement() {

        String parentId = m_id + "_parent";
        Element result = getElementById(parentId);
        return result;
    }

    /**
     * Gets an element by its id.<p>
     *
     * @param id the id
     * @return the element with the given id
     */
    protected native Element getElementById(String id) /*-{
		return $doc.getElementById(id);
    }-*/;

    /**
     * Gets the toolbar element.<p>
     *
     * @return the toolbar element
     */
    protected Element getToolbarElement() {

        String toolbarId = m_id + "_external";
        Element result = getElementById(toolbarId);
        return result;
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#onDetach()
     */
    @Override
    protected void onDetach() {

        try {
            detachEditor();
        } catch (Throwable t) {
            // may happen in rare cases, can be ignored
        }
        super.onDetach();
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#onLoad()
     */
    @Override
    protected void onLoad() {

        incrementAttached();

        if (!m_hasBeenAttached) {
            m_hasBeenAttached = true;
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                @SuppressWarnings("synthetic-access")
                public void execute() {

                    if (isAttached()) {
                        m_editorHeight = calculateEditorHeight();
                        m_id = ensureId(getMainElement());
                        m_width = calculateWidth();
                        checkLibraries();
                        initNative(!m_enabled);
                    } else {
                        resetAtachedFlag();
                    }
                }
            });
        }
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#onUnload()
     */
    @Override
    protected void onUnload() {

        decrementAttached();
    }

    /**
     * Propagates the a focus event.<p>
     */
    protected void propagateFocusEvent() {

        NativeEvent nativeEvent = Document.get().createFocusEvent();
        DomEvent.fireNativeEvent(nativeEvent, this, getElement());
    }

    /**
     * Propagates a native mouse event.<p>
     *
     * @param eventType the mouse event type
     * @param eventSource the event source
     */
    protected native void propagateMouseEvent(String eventType, Element eventSource) /*-{
		var doc = $wnd.document;
		var event;
		if (doc.createEvent) {
			event = doc.createEvent("MouseEvents");
			event.initEvent(eventType, true, true);
			eventSource.dispatchEvent(event);
		} else {
			eventSource.fireEvent("on" + eventType);
		}
    }-*/;

    /**
     * Sets focus to the editor. Use only when in line editing.<p>
     */
    protected native void refocusInlineEditor() /*-{
		var elem = $wnd.document
				.getElementById(this.@org.opencms.gwt.client.ui.input.tinymce.CmsTinyMCEWidget::m_id);
		elem.blur();
		elem.focus();
    }-*/;

    /**
     * Removes the editor instance.<p>
     */
    protected native void removeEditor() /*-{
		var editor = this.@org.opencms.gwt.client.ui.input.tinymce.CmsTinyMCEWidget::m_editor;
		editor.remove();
    }-*/;

    /**
     * Schedules to reset the focus to the main element.<p>
     */
    protected void scheduleRefocus() {

        // this needs to be delayed a bit, otherwise the toolbar is not rendered properly
        Timer focusTimer = new Timer() {

            @Override
            public void run() {

                refocusInlineEditor();
            }
        };
        focusTimer.schedule(150);
    }

    /**
     * Sets the main content of the element which is inline editable.<p>
     *
     * @param html the new content html
     */
    protected native void setMainElementContent(String html) /*-{
		var instance = this;
		var elementId = instance.@org.opencms.gwt.client.ui.input.tinymce.CmsTinyMCEWidget::m_id;
		var mainElement = $wnd.document.getElementById(elementId);
		mainElement.innerHTML = html;
    }-*/;

    /**
     * Sets the previous value.<p>
     *
     * @param previousValue the previous value to set
     */
    protected void setPreviousValue(String previousValue) {

        m_previousValue = previousValue;
    }

    /**
     * Calculates the needed editor height.<p>
     *
     * @return the calculated editor height
     */
    int calculateEditorHeight() {

        int result = getElement().getOffsetHeight() + 30;
        return result > MIN_EDITOR_HEIGHT ? result : MIN_EDITOR_HEIGHT;
    }

    /**
     * Calculates the widget width.<p>
     *
     * @return the widget width
     */
    int calculateWidth() {

        return getElement().getOffsetWidth() - 2;
    }

    /**
     * Initializes the TinyMCE instance.
     *
     * @param readonly if true, initialize TinyMCE in readonly mode
     */
    native void initNative(boolean readonly) /*-{

		function merge() {
			var result = {}, length = arguments.length;
			for (i = 0; i < length; i++) {
				for (key in arguments[i]) {
					if (arguments[i].hasOwnProperty(key)) {
						result[key] = arguments[i][key];
					}
				}
			}
			return result;
		}

		var self = this;
		var needsRefocus = false;
		var elementId = self.@org.opencms.gwt.client.ui.input.tinymce.CmsTinyMCEWidget::m_id;
		var mainElement = $wnd.document.getElementById(elementId);
		var editorHeight = self.@org.opencms.gwt.client.ui.input.tinymce.CmsTinyMCEWidget::m_editorHeight
				+ "px";

		var fireChange = function() {
			self.@org.opencms.gwt.client.ui.input.tinymce.CmsTinyMCEWidget::fireChangeFromNative()();
		};
		var options = this.@org.opencms.gwt.client.ui.input.tinymce.CmsTinyMCEWidget::m_options;
		if (options != null && options.editorHeight) {
			editorHeight = options.editorHeight;
			delete options.editorHeight;
		}
		// default options:
		var defaults = {
			elements : elementId,
			relative_urls : false,
			remove_script_host : false,
			entity_encoding : "raw",
			skin_variant : 'ocms',
			mode : "exact",
			theme : "silver",
			plugins : "autolink lists pagebreak table save hr image link emoticons spellchecker insertdatetime preview media searchreplace print paste directionality noneditable visualchars nonbreaking template wordcount advlist",
			paste_as_text : true,
			menubar : false,
		};

		self.@org.opencms.gwt.client.ui.input.tinymce.CmsTinyMCEWidget::m_currentContent = self.@org.opencms.gwt.client.ui.input.tinymce.CmsTinyMCEWidget::m_originalContent;
		defaults.min_height = 100;
		defaults.max_height = editorHeight;
		defaults.width = '100%';
		defaults.resize = 'both';

		// extend the defaults with any given options
		if (options != null) {
			defaults = merge(defaults, options);
		}
		defaults.plugins = "autoresize " + defaults.plugins;
		// add the setup function
		defaults.setup = function(ed) {
			self.@org.opencms.gwt.client.ui.input.tinymce.CmsTinyMCEWidget::m_editor = ed;
			ed.on('change', fireChange);
			ed.on('KeyDown', fireChange);
			ed
					.on(
							'LoadContent',
							function() {

								// firing resize event on resize of the editor iframe
								ed.dom
										.bind(
												ed.getWin(),
												'resize',
												function() {
													self.@org.opencms.gwt.client.ui.input.tinymce.CmsTinyMCEWidget::fireResizeEvent()();
												});
								var content = self.@org.opencms.gwt.client.ui.input.tinymce.CmsTinyMCEWidget::m_originalContent;
								if (content != null) {
									ed.setContent(content);
								}
								self.@org.opencms.gwt.client.ui.input.tinymce.CmsTinyMCEWidget::m_initialized = true;
							});

		};
		// initialize tinyMCE
		if (readonly) {
			defaults.readonly = 1;
		}
		$wnd.tinymce.init(defaults);
    }-*/;

    /**
     * Resets the attached flag.<p>
     */
    void resetAtachedFlag() {

        m_hasBeenAttached = false;
    }

    /**
     * Removes the editor.<p>
     */
    private native void detachEditor() /*-{

		var ed = this.@org.opencms.gwt.client.ui.input.tinymce.CmsTinyMCEWidget::m_editor;
		if (ed != null) {
			ed.remove();
		}
		// in IE somehow the whole document will be selected, empty the selection to resolve that
		if ($wnd.document.selection != null) {
			$wnd.document.selection.empty();
		}
    }-*/;

    /**
     * Used to fire the value changed event from native code.<p>
     */
    private void fireChangeFromNative() {

        // skip firing the change event, if the external flag is set
        //        String message = "fireChangeFromNative\n";
        //        message += "init: " + m_initialized + "\n";
        //        message += "external: " + m_externalValueChange + "\n";
        //        CmsDebugLog.consoleLog(message);

        if (m_initialized && !m_externalValueChange) {
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                public void execute() {

                    try {
                        CmsTinyMCEWidget.this.fireValueChange(false);
                    } catch (Throwable t) {
                        // this may happen when returning from full screen mode, nothing to be done
                    }
                }
            });
        }
        // reset the external flag
        m_externalValueChange = false;
    }

    /**
     * Fires the resize event.<p>
     */
    private void fireResizeEvent() {

        ResizeEvent.fire(this, getOffsetWidth(), getOffsetHeight());
    }

    /**
     * Returns the editor content.<p>
     *
     * @return the editor content
     */
    private native String getContent() /*-{
		var editor = this.@org.opencms.gwt.client.ui.input.tinymce.CmsTinyMCEWidget::m_editor;
		return editor.getContent();
    }-*/;

    /**
     * Sets the content of the TinyMCE editor.<p>
     *
     * @param newContent the new content
     */
    private native void setContent(String newContent) /*-{
		var editor = this.@org.opencms.gwt.client.ui.input.tinymce.CmsTinyMCEWidget::m_editor;
		editor.setContent(newContent);
    }-*/;

    /**
     * Sets the editor status to enabled/disabled.<p>
     *
     * Warning: This only works before the TinyMCE editor has actually been initialized
     * @param editor the editor instance
     * @param enabled true if editor should be enabled
     */
    private native void setEnabled(JavaScriptObject editor, boolean enabled) /*-{
		editor.getBody().setAttribute('contenteditable', enabled);
    }-*/;

}
