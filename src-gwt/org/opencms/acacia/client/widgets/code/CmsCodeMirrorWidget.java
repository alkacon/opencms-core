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

package org.opencms.acacia.client.widgets.code;

import org.opencms.acacia.client.css.I_CmsLayoutBundle;
import org.opencms.acacia.client.widgets.I_CmsEditWidget;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.FontOpenCms;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsJsUtil;
import org.opencms.gwt.shared.I_CmsCodeMirrorClientConfiguration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.HasResizeHandlers;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;

/**
 * Client-side content editor widget for editing source code using the CodeMirror library.
 * */
public class CmsCodeMirrorWidget extends ComplexPanel implements I_CmsEditWidget, HasResizeHandlers {

    /** Map from modes to labels. */
    private static LinkedHashMap<String, String> m_modes;

    /** Map from simplified mode names to actual mode names (e.g. from 'javascript' to 'text/javascript'). */
    private static Map<String, String> m_simpleModeTranslations;

    /** Helper for loading the necessary scripts. */
    private static final CmsCodeMirrorScriptLoader scriptLoader = new CmsCodeMirrorScriptLoader();

    static {
        m_modes = new LinkedHashMap<>();
        m_simpleModeTranslations = new HashMap<>();
        addMode("text/css", "css", "CSS");
        addMode("text/html", "html", "HTML");
        addMode("text/plain", "text", "Text");
        addMode("text/x-java", "java", "Java");
        addMode("text/javascript", "javascript", "Javascript");
        addMode("application/x-jsp", "jsp", "JSP");
        addMode("application/xml", "xml", "XML");
    }

    /** The token to control activation. */
    private boolean m_active = true;

    /** The configuration from the server. */
    private I_CmsCodeMirrorClientConfiguration m_config;

    /** The native CodeMirror instance. */
    private JavaScriptObject m_editor;

    /** Boolean flag indicating whether syntax highlighting is currently turned on.*/
    private boolean m_highlighting = true;

    /** The currently selected mode (may be different from the actual mode in the CodeMirror instance). */
    private String m_mode = "text/plain";

    /** The mode selector. */
    private CmsSelectBox m_modeSelect;

    /** The initial content (we need to save this because CodeMirror is loaded asynchronously). */
    private String m_originalContent;

    /** The previous value. */
    private String m_previousValue;

    /** The 'redo' button. */
    private CmsCodeMirrorToolbarButton m_redo;

    /** Toggle button for automatically closing brackets / tags. */
    private CmsCodeMirrorToggleButton m_toggleAutoClose;

    /** The 'show tabs' toggle button. */
    private CmsCodeMirrorToggleButton m_toggleShowTabs;

    /** The 'undo' button. */
    private CmsCodeMirrorToolbarButton m_undo;

    private String m_id;

    /**
     * Creates a new display widget.<p>
     *
     * @param config the widget configuration string
     */
    public CmsCodeMirrorWidget(String config) {

        super();
        m_id = "" + Math.random();
        setElement(Document.get().createDivElement());
        m_config = AutoBeanCodex.decode(
            CmsCoreProvider.AUTO_BEAN_FACTORY,
            I_CmsCodeMirrorClientConfiguration.class,
            config).as();
        addStyleName("oc-codemirror-editorwidget");
        Integer height = m_config.getHeight();
        if (height != null) {
            nativeSetProperty(getElement(), "--codemirror-height", height + "px");
        }
        add(createToolbar(), getElement().<Element> cast());
    }

    /**
     * Helper method used to make a mode available for selection.
     *
     * @param name the normal MIME type name of the mode
     * @param simpleName simple mode name for use in the server-side configuration, can be null
     * @param title the title to be used in the mode selector
     */
    private static void addMode(String name, String simpleName, String title) {

        m_modes.put(name, title);
        if (simpleName != null) {
            m_simpleModeTranslations.put(simpleName, name);
        }
    }

    /**
     * @see com.google.gwt.event.dom.client.HasFocusHandlers#addFocusHandler(com.google.gwt.event.dom.client.FocusHandler)
     */
    public HandlerRegistration addFocusHandler(FocusHandler handler) {

        return addDomHandler(handler, FocusEvent.getType());
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
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Represents a value change event.<p>
     *
     */
    public void fireChangeEvent() {

        updateUndoRedo();
        fireValueChange(false);
    }

    /**
     * Represents a resize event.<p>
     * @param event from text area panel
     */
    public void fireResizeeEvent(ResizeEvent event) {

        ResizeEvent.fire(this, event.getWidth(), event.getHeight());
    }

    /**
     * Gets the effective mode to use, which depends both on the selected mode and on whether the user has enabled syntax highlighting.
     *
     * @return the effective mode
     */
    public String getEffectiveMode() {

        return m_highlighting ? m_mode : "text/plain";
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    public String getValue() {

        return getContent();
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#isActive()
     */
    public boolean isActive() {

        return m_active;
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#onAttachWidget()
     */
    public void onAttachWidget() {

        onAttach();
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#owns(com.google.gwt.dom.client.Element)
     */
    public boolean owns(Element element) {

        return getElement().isOrHasChild(element);

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
                fireValueChange(true);
            } else {
                getElement().addClassName(I_CmsLayoutBundle.INSTANCE.form().inActive());
            }
        }
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#setName(java.lang.String)
     */
    public void setName(String name) {

        // not used
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
            nativeSetContent(value);
        }
        if (fireEvents) {
            fireValueChange(true);
        }

    }

    /**
     * Fires the value change event, if the value has changed.<p>
     *
     * @param force <code>true</code> to force firing the event, not regarding an actually changed value
     */
    protected void fireValueChange(boolean force) {

        String currentValue = getValue();
        if (force || !currentValue.equals(m_previousValue)) {
            m_previousValue = currentValue;
            ValueChangeEvent.fire(this, currentValue);
        }
    }

    /**
     * Returns the previous value.<p>
     *
     * @return the previous value
     */
    protected String getPreviousValue() {

        return m_previousValue;
    }

    /**
     * @see com.google.gwt.user.client.ui.FocusWidget#onAttach()
     */
    @Override
    protected void onAttach() {

        super.onAttach();
        scriptLoader.load(() -> {
            int height = -1;
            if (m_config.getHeight() != null) {
                height = m_config.getHeight().intValue();
            }
            initCodeMirror(
                CmsCodeMirrorWidget.this,
                getElement(),
                CmsJsUtil.parseJSON(m_config.getPhrasesJSON()),
                height);
            initializeUserControlledSettings();
            if (m_originalContent != null) {
                nativeSetContent(m_originalContent);
                clearHistory();
                updateUndoRedo();
            }
        });
    }

    /**
     * Sets the previous value.<p>
     *
     * @param previousValue the previous value to set
     */
    protected void setPreviousValue(String previousValue) {

        m_previousValue = previousValue;
    }

    /**
     * Clears the undo / redo history.
     */
    private native void clearHistory() /*-{
        var editor = this.@org.opencms.acacia.client.widgets.code.CmsCodeMirrorWidget::m_editor;
        editor.clearHistory();
    }-*/;

    /**
     * Creates a simple toggle button for enabling / disabling a native CodeMirror option.
     *
     * @param icon the icon
     * @param optionName the option name
     * @param initialValue the initial vale of the option
     *
     * @return the newly created toggle button
     */
    private CmsCodeMirrorToggleButton createOptionButton(FontOpenCms icon, String optionName, boolean initialValue) {

        CmsCodeMirrorToggleButton result = new CmsCodeMirrorToggleButton(icon);
        result.setValue(initialValue, false);
        result.addValueChangeHandler(event -> nativeSetOption(optionName, event.getValue().booleanValue()));
        return result;

    }

    /**
     * Creates the editor tool bar.
     * @return the editor tool bar
     */
    private FlowPanel createToolbar() {

        FlowPanel result = new FlowPanel();
        result.addStyleName("oc-codewidget-toolbar");

        {
            CmsCodeMirrorToolbarButton undo = new CmsCodeMirrorToolbarButton(FontOpenCms.UNDO);
            undo.setTitle(Messages.get().key(Messages.GUI_CODEMIRROR_BUTTON_UNDO_0));
            undo.addClickHandler(event -> executeCommand("undo"));
            m_undo = undo;
            result.add(undo);
        }

        {
            CmsCodeMirrorToolbarButton redo = new CmsCodeMirrorToolbarButton(FontOpenCms.REDO);
            redo.setTitle(Messages.get().key(Messages.GUI_CODEMIRROR_BUTTON_REDO_0));
            redo.addClickHandler(event -> executeCommand("redo"));
            m_redo = redo;
            result.add(redo);
        }

        {
            CmsCodeMirrorToolbarButton search = new CmsCodeMirrorToolbarButton(FontOpenCms.SEARCH);
            search.setTitle(Messages.get().key(Messages.GUI_CODEMIRROR_BUTTON_FIND_0));
            search.addClickHandler(event -> {
                search();
            });
            result.add(search);
        }

        {
            CmsCodeMirrorToolbarButton searchReplace = new CmsCodeMirrorToolbarButton(FontOpenCms.SEARCH_REPLACE);
            searchReplace.setTitle(Messages.get().key(Messages.GUI_CODEMIRROR_BUTTON_REPLACE_0));
            searchReplace.addClickHandler(event -> searchReplace());
            result.add(searchReplace);
        }

        {
            CmsCodeMirrorToolbarButton lineButton = new CmsCodeMirrorToolbarButton("L");
            lineButton.setTitle(Messages.get().key(Messages.GUI_CODEMIRROR_BUTTON_JUMP_TO_LINE_0));
            lineButton.addClickHandler(event -> {
                executeCommand("jumpToLine");
            });
            result.add(lineButton);
        }
        {
            CmsCodeMirrorToggleButton highlight = new CmsCodeMirrorToggleButton(FontOpenCms.HIGHLIGHT);
            highlight.setValue(true, false);
            highlight.setTitle(Messages.get().key(Messages.GUI_CODEMIRROR_TOGGLE_SYNTAX_HIGHLIGHTING_0));
            highlight.addValueChangeHandler(event -> setSyntaxHighlightingEnabled(event.getValue().booleanValue()));
            result.add(highlight);
        }

        {
            CmsCodeMirrorToggleButton wrap = createOptionButton(FontOpenCms.WRAP_LINES, "lineWrapping", false);
            wrap.setTitle(Messages.get().key(Messages.GUI_CODEMIRROR_TOGGLE_LINE_WRAP_0));
            result.add(wrap);
        }

        {
            CmsCodeMirrorToggleButton showTabs = new CmsCodeMirrorToggleButton(FontOpenCms.INVISIBLE_CHARS);
            showTabs.setTitle(Messages.get().key(Messages.GUI_CODEMIRROR_TOGGLE_TABS_0));
            showTabs.addValueChangeHandler(event -> setShowTabs(event.getValue().booleanValue()));
            m_toggleShowTabs = showTabs;
            result.add(showTabs);
        }

        {
            CmsCodeMirrorToggleButton brackets = new CmsCodeMirrorToggleButton(FontOpenCms.BRACKETS);
            brackets.setTitle(Messages.get().key(Messages.GUI_CODEMIRROR_TOGGLE_AUTO_CLOSE_BRACKETS_0));
            brackets.addValueChangeHandler(event -> setAutoCloseBrackets(event.getValue().booleanValue()));
            m_toggleAutoClose = brackets;
            result.add(brackets);
        }

        m_modeSelect = new CmsSelectBox(m_modes);
        m_modeSelect.addStyleName("oc-codewidget-mode-select");

        Map<String, String> items = new LinkedHashMap<>();
        for (String fontSize : Arrays.asList("8px", "10px", "12px", "14px", "16px", "18px", "20px")) {
            items.put(fontSize, fontSize);
        }
        CmsSelectBox select = new CmsSelectBox(items);
        select.addStyleName("oc-codewidget-fontsize-select");
        select.setFormValue("14px");
        result.add(select);
        result.add(m_modeSelect);

        select.addValueChangeHandler(event -> {
            String fontSize = event.getValue();
            setFontSize(fontSize);
        });

        m_modeSelect.addValueChangeHandler(event -> {
            String mode = event.getValue();
            setMode(mode);

        });
        return result;

    }

    /**
     * Executes a named CodeMirror command.
     *
     * @param command the name of the command to execute
     */
    private native void executeCommand(String command) /*-{
        var editor = this.@org.opencms.acacia.client.widgets.code.CmsCodeMirrorWidget::m_editor;
        $wnd.CodeMirror.commands[command](editor);
    }-*/;

    /**
     * Gets the editor content.
     *
     * @return the editor content
     */
    private native String getContent() /*-{
        var editor = this.@org.opencms.acacia.client.widgets.code.CmsCodeMirrorWidget::m_editor;
        return editor.getValue();
    }-*/;

    /**
     * Gets the history size.
     *
     * @return the history size
     */
    private native JavaScriptObject getHistorySize() /*-{
        var editor = this.@org.opencms.acacia.client.widgets.code.CmsCodeMirrorWidget::m_editor;
        return editor.historySize();
    }-*/;

    /**
     * Checks if a CodeMirror dialog is currently being displayed.
     *
     * @return true if a CodeMirror dialog is being displayed
     */
    private boolean hasDialog() {

        return CmsDomUtil.getElementsByClass("CodeMirror-dialog", getElement()).size() > 0;
    }

    /**
     * Initializes the CodeMirror instance.
     *
     * @param elem the parent element
     * @param phrases the localization phrases
     * @param height the initial editor height, or -1 if no height should be set
     */
    private native void initCodeMirror(CmsCodeMirrorWidget instance, Element elem, JavaScriptObject phrases, int height) /*-{
        var config = {
            theme: "eclipse",
            mode: "text/plain",
            lineNumbers: true,
            styleActiveLine: true,
            fixedGutter: true,
            indentUnit: 4,
            indentWithTabs: true,
            smartIndent: false
        };

        config.phrases = phrases;
        var result = $wnd.CodeMirror(elem, config);
        instance.getEditor = function() {
            return result;
        }
        this.@org.opencms.acacia.client.widgets.code.CmsCodeMirrorWidget::m_editor = result;
        var that = this;
        result.on("changes", function(editor, changes) {
            that.@org.opencms.acacia.client.widgets.code.CmsCodeMirrorWidget::fireChangeEvent()();
        });
        if (height > 0) {
            result.setSize(null, height);
        }
        var resizeHandler = new ResizeObserver(function(entries) {
            result.refresh();
        });
        resizeHandler.observe(elem);
    }-*/;

    /**
     * Initializes the settings controlled by buttons / select boxes after CodeMirror is loaded.
     */
    private void initializeUserControlledSettings() {

        String startMode = m_config.getStartMode();
        if (startMode != null) {
            if (!m_modes.containsKey(startMode)) {
                startMode = m_simpleModeTranslations.get(startMode.toLowerCase());
            }
            if (startMode != null) {
                m_modeSelect.setFormValue(startMode, true);
            }
        }
        m_toggleAutoClose.setValue(true, /*fireEvents=*/true);
        m_toggleShowTabs.setValue(false, /*fireEvents=*/true);
    }

    /**
     * Sets the content in the native CodeMirror instance.
     *
     * @param content the new editor content
     */
    private native void nativeSetContent(String content) /*-{
        var editor = this.@org.opencms.acacia.client.widgets.code.CmsCodeMirrorWidget::m_editor;
        editor.setValue(content);
    }-*/;

    /**
     * Sets the mode on the native CodeMirror instance.
     *
     * @param mode the mode to set
     */
    private native void nativeSetMode(String mode) /*-{
        var editor = this.@org.opencms.acacia.client.widgets.code.CmsCodeMirrorWidget::m_editor;
        editor.setOption("mode", mode);
    }-*/;

    /**
     * Sets a CodeMirror option of type 'boolean'.
     *
     * @param name the name of the option
     * @param booleanValue the option value
     */
    private native void nativeSetOption(String name, boolean booleanValue) /*-{
        var editor = this.@org.opencms.acacia.client.widgets.code.CmsCodeMirrorWidget::m_editor;
        if (editor) { // editor may not be fully loaded yet
            editor.setOption(name, booleanValue);
        }
    }-*/;

    /**
     * Sets a custom CSS property on the given element.
     *
     * @param element the element on which to set the property
     * @param name the name of the CSS property
     * @param value the value of the CSS property
     */
    private native void nativeSetProperty(com.google.gwt.dom.client.Element element, String name, String value) /*-{
        element.style.setProperty(name, value);
    }-*/;

    /**
     * Executes the 'redo' command.
     */
    private void redo() {

        executeCommand("redo");
    }

    /**
     * Refreshes the editor layout.
     */
    private native void refresh() /*-{
        var editor = this.@org.opencms.acacia.client.widgets.code.CmsCodeMirrorWidget::m_editor;
        editor.refresh();
    }-*/;

    /**
     * Starts a search operation.
     */
    private void search() {

        if (!hasDialog()) {
            // executing a dialog command while a dialog is active leads to weird/confusing UI state
            executeCommand("find");
        }
    }

    /**
     * Starts a search/replace operation.
     */
    private void searchReplace() {

        if (!hasDialog()) {
            // executing a dialog command while a dialog is active leads to weird/confusing UI state
            executeCommand("replace");
        }
    }

    /**
     * Enables/disables automatic closing of brackets.
     *
     * @param value true if automatic bracket closing should be enabled
     */
    private void setAutoCloseBrackets(boolean value) {

        nativeSetOption("autoCloseBrackets", value);
        nativeSetOption("autoCloseTags", value);
        nativeSetOption("matchBrackets", value);
    }

    /**
     * Updates the editor font size.
     *
     * @param value the font size, e.g. '12px'
     */
    private void setFontSize(String value) {

        nativeSetProperty(getElement().cast(), "--codemirror-font-size", value);
        refresh();
    }

    /**
     * Sets the mode selected by the user (this may not result in the mode actually being changed in the editor if syntax highlighting is turned off).
     *
     * @param mode the new mode
     */
    private void setMode(String mode) {

        m_mode = mode;
        if (m_editor != null) {
            nativeSetMode(getEffectiveMode());
        }

    }

    /**
     * Enables/disables tab visibility.
     *
     * @param value true if tabs should be shown
     */
    private void setShowTabs(boolean value) {

        nativeSetProperty(getElement(), "--codemirror-tab-state", value ? "visible" : "hidden");
        nativeSetOption("showTrailingSpace", value);

    }

    /**
     * Enables / disables syntax highlighting.
     *
     * @param booleanValue true if syntax highlighting should be enabled
     */
    private void setSyntaxHighlightingEnabled(boolean booleanValue) {

        m_highlighting = booleanValue;
        nativeSetMode(getEffectiveMode());
    }

    /**
     * Updates undo/redo button states based on history size.
     */
    private void updateUndoRedo() {

        JavaScriptObject jso = getHistorySize();
        JSONObject historySize = new JSONObject(jso);
        int undo = (int)(historySize.get("undo").isNumber().doubleValue());
        int redo = (int)(historySize.get("redo").isNumber().doubleValue());
        m_undo.setEnabled(undo > 0);
        m_redo.setEnabled(redo > 0);
    }

}
