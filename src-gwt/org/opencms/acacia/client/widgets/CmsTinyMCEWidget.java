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

package org.opencms.acacia.client.widgets;

import org.opencms.acacia.client.CmsEditorBase;
import org.opencms.acacia.client.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsDomUtil.Style;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.logical.shared.HasResizeHandlers;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;

/**
 * This class is used to start TinyMCE for editing the content of an element.<p>
 *
 * After constructing the instance, the actual editor is opened using the init() method, and destroyed with the close()
 * method. While the editor is opened, the edited contents can be accessed using the methods of the HasValue interface.
 */
public final class CmsTinyMCEWidget extends A_CmsEditWidget implements HasResizeHandlers, I_CmsHasDisplayDirection {

    /** Use as option to disallow any HTML or formatting the content. */
    public static final String NO_HTML_EDIT = "no_html_edit";

    /** The disabled style element id. */
    private static final String DISABLED_STYLE_ID = "editorDisabledStyle";

    /** The minimum editor height. */
    private static final int MIN_EDITOR_HEIGHT = 70;

    /** A flag which indicates whether the editor is currently active. */
    protected boolean m_active;

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

    /** Flag indicating the editor has been initialized. */
    boolean m_initialized;

    /** The element to store the widget content in. */
    private Element m_contentElement;

    /** Indicates the value has been set from external, not from within the widget. */
    private boolean m_externalValueChange;

    /** Indicating if the widget has been attached yet. */
    private boolean m_hasBeenAttached;

    /** Flag indicating if in line editing is used. */
    private boolean m_inline;

    /** The editor options. */
    private Object m_options;

    private String m_typografLocale;

    /**
     * Creates a new instance for the given element. Use this constructor for in line editing.<p>
     *
     * @param element the DOM element
     * @param options the tinyMCE editor options to extend the default settings
     */
    public CmsTinyMCEWidget(Element element, Object options) {

        this(element, options, true);
    }

    /**
     * Creates a new instance with the given options. Use this constructor for form based editing.<p>
     *
     * @param options the tinyMCE editor options to extend the default settings
     */
    public CmsTinyMCEWidget(Object options) {

        this(DOM.createDiv(), options, false);
    }

    /**
     * Constructor.<p>
     *
     * @param element the DOM element
     * @param options the tinyMCE editor options to extend the default settings
     * @param inline flag indicating if in line editing is used
     */
    private CmsTinyMCEWidget(Element element, Object options, boolean inline) {

        super(element);
        m_originalContent = "";
        m_options = options;
        m_active = true;
        m_inline = inline;
        if (m_inline) {
            m_contentElement = element;
        } else {
            // using a child DIV as content element
            m_contentElement = getElement().appendChild(DOM.createDiv());
        }
    }

    /**
     * Returns the disabled text color.<p>
     *
     * @return the disabled text color
     */
    private static String getDisabledTextColor() {

        return I_CmsLayoutBundle.INSTANCE.constants().css().textColorDisabled();
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasResizeHandlers#addResizeHandler(com.google.gwt.event.logical.shared.ResizeHandler)
     */
    public HandlerRegistration addResizeHandler(ResizeHandler handler) {

        return addHandler(handler, ResizeEvent.getType());
    }

    /**
     * @see org.opencms.acacia.client.widgets.A_CmsEditWidget#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsHasDisplayDirection#getDisplayingDirection()
     */
    public Direction getDisplayingDirection() {

        return Direction.above;
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
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public String getValue() {

        if (m_editor != null) {
            return getContent().trim();
        }
        return m_originalContent.trim();
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#isActive()
     */
    public boolean isActive() {

        return m_active;
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#setActive(boolean)
     */
    public void setActive(boolean active) {

        if (m_active == active) {
            return;
        }
        m_active = active;
        if (m_editor != null) {
            if (m_active) {
                getElement().removeClassName(I_CmsLayoutBundle.INSTANCE.form().inActive());
                removeEditorDisabledStyle();
                fireValueChange(true);
            } else {
                getElement().addClassName(I_CmsLayoutBundle.INSTANCE.form().inActive());
                setEditorDisabledStyle();
            }
        }
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#setName(java.lang.String)
     */
    public void setName(String name) {

        // no input field so nothing to do

    }

    /**
     * Sets the Typograf locale for inline editing mode.
     */
    public void setTypografLocale(String typografLocale) {

        m_typografLocale = typografLocale;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    public void setValue(String value) {

        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    public void setValue(String value, boolean fireEvents) {

        if (value != null) {
            value = value.trim();
        }
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
     * Returns if the widget is used in inline mode.<p>
     *
     * @return <code>true</code> if the widget is used in inline mode
     */
    protected boolean isInline() {

        return m_inline;
    }

    /**
     * @see com.google.gwt.user.client.ui.FocusWidget#onAttach()
     */
    @Override
    protected void onAttach() {

        super.onAttach();
        if (!m_hasBeenAttached) {
            m_hasBeenAttached = true;
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                public void execute() {

                    if (isAttached()) {
                        m_editorHeight = calculateEditorHeight();
                        m_id = ensureId(getMainElement());
                        m_width = calculateWidth();
                        checkLibraries();
                        if (isInline()) {
                            if (CmsDomUtil.getCurrentStyleInt(getElement(), Style.zIndex) < 1) {
                                getElement().getStyle().setZIndex(1);
                            }
                            addDomHandler(new ClickHandler() {

                                public void onClick(ClickEvent event) {

                                    // prevent event propagation while editing inline, to avoid following links in ancestor nodes
                                    event.stopPropagation();
                                    event.preventDefault();
                                }
                            }, ClickEvent.getType());
                        }
                        initNative(CmsCoreProvider.get().getWpLanguage());
                        if (!m_active) {
                            getElement().addClassName(I_CmsLayoutBundle.INSTANCE.form().inActive());
                        }
                    } else {
                        resetAtachedFlag();
                    }
                }
            });
        }
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
     * Propagates the a focus event.<p>
     */
    protected void propagateFocusEvent() {

        if (m_initialized) {
            NativeEvent nativeEvent = Document.get().createFocusEvent();
            DomEvent.fireNativeEvent(nativeEvent, this, getElement());
        }
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
     * Removes the editor instance.<p>
     */
    protected native void removeEditor() /*-{
		var editor = this.@org.opencms.acacia.client.widgets.CmsTinyMCEWidget::m_editor;
		editor.remove();
    }-*/;

    /**
     * Sets the main content of the element which is inline editable.<p>
     *
     * @param html the new content html
     */
    protected native void setMainElementContent(String html) /*-{
		var instance = this;
		var elementId = instance.@org.opencms.acacia.client.widgets.CmsTinyMCEWidget::m_id;
		var mainElement = $wnd.document.getElementById(elementId);
		mainElement.innerHTML = html;
    }-*/;

    /**
     * Checks if the main element contains the current text selection.<p>
     *
     * @return <code>true</code> if the main element contains the current text selection
     */
    protected boolean shouldReceiveFocus() {

        return m_inline && CmsEditorBase.shouldFocusOnInlineEdit(getElement());
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

        int result;
        if (m_inline && CmsDomUtil.getCurrentStyle(getElement(), Style.display).equals("inline")) {
            com.google.gwt.dom.client.Element parentBlock = getElement().getParentElement();
            while (CmsDomUtil.getCurrentStyle(parentBlock, Style.display).equals("inline")) {
                parentBlock = parentBlock.getParentElement();
            }
            result = parentBlock.getOffsetWidth();
        } else {
            result = getElement().getOffsetWidth();
        }
        return result - 2;
    }

    /**
     * Initializes the TinyMCE instance.
     *
     * @param locale the UI locale
     */
    native void initNative(String locale) /*-{

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


        var languageMap = { "it": "it_IT", "cs": "cs_CZ", "ru": "ru_RU", "zh": "zh_CN"};
        var translatedLanguage = languageMap[locale];
        if (translatedLanguage) {
            locale = translatedLanguage;
        }



		var self = this;
		var needsRefocus = self.@org.opencms.acacia.client.widgets.CmsTinyMCEWidget::shouldReceiveFocus()();
		var elementId = self.@org.opencms.acacia.client.widgets.CmsTinyMCEWidget::m_id;
		var mainElement = $wnd.document.getElementById(elementId);
		var editorHeight = self.@org.opencms.acacia.client.widgets.CmsTinyMCEWidget::m_editorHeight;

		var fireChange = function() {
			self.@org.opencms.acacia.client.widgets.CmsTinyMCEWidget::fireChangeFromNative()();
		};
		var options = this.@org.opencms.acacia.client.widgets.CmsTinyMCEWidget::m_options;
		if (options != null && options.editorHeight) {
			editorHeight = options.editorHeight;
			delete options.editorHeight;
		}
		// default options:
		var defaults;
		if (@org.opencms.acacia.client.widgets.CmsTinyMCEWidget::NO_HTML_EDIT == options) {
			// disallow any formatting

			defaults = {
				selector : mainElement.tagName + "#" + elementId,
				entity_encoding : "raw",
				mode : "exact",
				theme : "silver",
				plugins : "paste emoticons charmap",
				paste_as_text : true,
				toolbar : "undo redo | emoticons charmap",
				menubar : false,
				forced_root_block : false
			};
			var typografLocale = this.@org.opencms.acacia.client.widgets.CmsTinyMCEWidget::m_typografLocale;
			if (typografLocale) {
                defaults.plugins = "paste emoticons typograf charmap";
                defaults.toolbar = "undo redo | typograf | emoticons charmap";
                defaults.typograf = {
                    disableRule: ["*"],
                    enableRule2: ["common/punctuation/quote"],
                    locale: [typografLocale, "en-US"]
                };
            }
			options = null;
		} else {
			defaults = {
				selector : mainElement.tagName + "#" + elementId,
				relative_urls : false,
				remove_script_host : false,
				entity_encoding : "raw",
				skin_variant : 'ocms',
				mode : "exact",
				theme : "silver",
				plugins : "autolink lists pagebreak table save codemirror hr image link emoticons spellchecker insertdatetime preview media searchreplace print paste directionality noneditable visualchars nonbreaking template wordcount advlist",
				paste_as_text : true,
				menubar : false,
			};
		}
		if (this.@org.opencms.acacia.client.widgets.CmsTinyMCEWidget::m_inline) {
			self.@org.opencms.acacia.client.widgets.CmsTinyMCEWidget::m_currentContent = mainElement.innerHTML;
			defaults.inline = true;
			defaults.width = this.@org.opencms.acacia.client.widgets.CmsTinyMCEWidget::m_width;
		} else {
			self.@org.opencms.acacia.client.widgets.CmsTinyMCEWidget::m_currentContent = self.@org.opencms.acacia.client.widgets.CmsTinyMCEWidget::m_originalContent;
			defaults.min_height = 100;
			defaults.max_height = editorHeight;
			defaults.width = '100%';
			defaults.resize = 'both';
		}
		// extend the defaults with any given options
		if (options != null) {
			if (options.style_formats) {
				// tinymce performs a type test for arrays wich fails in case the array was not created in the same window context
				var formats = new $wnd.Array();
				for (var i = 0; i < options.style_formats.length; i++) {
					formats[i] = options.style_formats[i];
				}
				options.style_formats = formats;
			}
			defaults = merge(defaults, options);
		}
		if (this.@org.opencms.acacia.client.widgets.CmsTinyMCEWidget::m_inline) {
			delete defaults.content_css;
		} else {
			// enable autoresize
			defaults.plugins = "autoresize " + defaults.plugins;
		}
		if (needsRefocus) {
			defaults.auto_focus = elementId;
		}

		// add the setup function
		defaults.setup = function(ed) {
			self.@org.opencms.acacia.client.widgets.CmsTinyMCEWidget::m_editor = ed;
			ed.on('SetContent', fireChange);
			ed.on('change', fireChange);
			ed.on('KeyDown', fireChange);
			ed
					.on(
							'LoadContent',
							function() {
								if (!self.@org.opencms.acacia.client.widgets.CmsTinyMCEWidget::m_inline) {
									// firing resize event on resize of the editor iframe
									ed.dom
											.bind(
													ed.getWin(),
													'resize',
													function() {
														self.@org.opencms.acacia.client.widgets.CmsTinyMCEWidget::fireResizeEvent()();
													});
									var content = self.@org.opencms.acacia.client.widgets.CmsTinyMCEWidget::m_originalContent;
									if (content != null) {
										ed.setContent(content);
									}
								}
							});
			ed
					.on(
							'init',
							function() {
								self.@org.opencms.acacia.client.widgets.CmsTinyMCEWidget::scheduleInitializationDone()();
							});

			if (!self.@org.opencms.acacia.client.widgets.CmsTinyMCEWidget::m_inline) {

				ed
						.on(
								'Click',
								function(event) {
									self.@org.opencms.acacia.client.widgets.CmsTinyMCEWidget::propagateFocusEvent()();
								});
				ed
						.on(
								'activate',
								function(event) {
									self.@org.opencms.acacia.client.widgets.CmsTinyMCEWidget::propagateFocusEvent()();
								});
				ed
						.on(
								'focus',
								function(event) {
									self.@org.opencms.acacia.client.widgets.CmsTinyMCEWidget::propagateFocusEvent()();
								});
			}
		};

		// initialize tinyMCE
		defaults.language = locale;
		if (defaults.typograf && $wnd.Typograf && !$wnd.Typograf.hasLocale(defaults.typograf.locale[0])) {
		    delete defaults.typograf;
		    defaults.toolbar1 = defaults.toolbar1.replace("typograf","");
		}

		$wnd.tinymce.init(defaults);
    }-*/;

    /**
     * Removes the disabled editor styling.<p>
     */
    native void removeEditorDisabledStyle()/*-{
		var ed = this.@org.opencms.acacia.client.widgets.CmsTinyMCEWidget::m_editor;
		var styleEl = ed
				.getDoc()
				.getElementById(
						@org.opencms.acacia.client.widgets.CmsTinyMCEWidget::DISABLED_STYLE_ID);
		if (styleEl != null) {
			ed.getDoc().head.removeChild(styleEl);
		}
    }-*/;

    /**
     * Resets the attached flag.<p>
     */
    void resetAtachedFlag() {

        m_hasBeenAttached = false;
    }

    /**
     * Scheduling to set the initialized flag.<p>
     */
    void scheduleInitializationDone() {

        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            public void execute() {

                m_initialized = true;
                if (m_active) {
                    removeEditorDisabledStyle();
                } else {
                    setEditorDisabledStyle();
                }
            }
        });
    }

    /**
     * Sets the editor disabled styling.<p>
     */
    native void setEditorDisabledStyle()/*-{
		var ed = this.@org.opencms.acacia.client.widgets.CmsTinyMCEWidget::m_editor;
		if (ed
				.getDoc()
				.getElementById(
						@org.opencms.acacia.client.widgets.CmsTinyMCEWidget::DISABLED_STYLE_ID) == null) {
			var styleEl = ed.getDoc().createElement("style");
			styleEl
					.setAttribute("id",
							@org.opencms.acacia.client.widgets.CmsTinyMCEWidget::DISABLED_STYLE_ID);
			var styleText = ed
					.getDoc()
					.createTextNode(
							"body, body *{ color: "
									+ @org.opencms.acacia.client.widgets.CmsTinyMCEWidget::getDisabledTextColor()()
									+ " !important;}");
			styleEl.appendChild(styleText);
			ed.getDoc().head.appendChild(styleEl);
		}
    }-*/;

    /**
     * Removes the editor.<p>
     */
    private native void detachEditor() /*-{

		var ed = this.@org.opencms.acacia.client.widgets.CmsTinyMCEWidget::m_editor;
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
        if (m_initialized && !m_externalValueChange && m_active) {
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                public void execute() {

                    try {
                        fireValueChange(false);
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
		var editor = this.@org.opencms.acacia.client.widgets.CmsTinyMCEWidget::m_editor;
		return editor.getContent();
    }-*/;

    /**
     * Sets the content of the TinyMCE editor.<p>
     *
     * @param newContent the new content
     */
    private native void setContent(String newContent) /*-{
		var editor = this.@org.opencms.acacia.client.widgets.CmsTinyMCEWidget::m_editor;
		editor.setContent(newContent);
    }-*/;

}
