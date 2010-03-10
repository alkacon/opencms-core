/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/input/demo/client/Attic/CmsInputDemo.java,v $
 * Date   : $Date: 2010/03/10 12:51:59 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt.client.ui.input.demo.client;

import org.opencms.gwt.client.ui.css.I_CmsInputCss;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsMultiCheckBox;
import org.opencms.gwt.client.ui.input.CmsRadioButtonGroup;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.client.ui.input.CmsTextArea;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.client.ui.input.I_CmsFormWidget;
import org.opencms.gwt.client.util.CmsPair;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * Demonstration class for the input package.<p>
 * 
 *  @author Georg Westenberger
 *  
 *  @version $Revision: 1.3 $
 *  
 *  @since 8.0.0
 */
public class CmsInputDemo extends Composite {

    /** The CSS bundle for this widget. */
    static final I_CmsInputCss CSS = I_CmsInputLayoutBundle.INSTANCE.inputCss();
    /**
     * Constructs a new instance.<p>
     */
    public CmsInputDemo() {

        super();

        FlowPanel panel = new FlowPanel();
        initWidget(panel);

        //I_CmsLayoutBundle.INSTANCE.inputCss().ensureInjected();
        Style style = panel.getElement().getStyle();
        style.setLeft(140, Unit.PX);
        style.setTop(140, Unit.PX);
        style.setWidth(250, Unit.PX);
        style.setPosition(Position.ABSOLUTE);

        List<CmsPair<String, String>> items = new ArrayList<CmsPair<String, String>>();
        items.add(new CmsPair<String, String>("foo", "First item - foo"));
        items.add(new CmsPair<String, String>("bar", "Second item - bar"));
        items.add(new CmsPair<String, String>("baz", "Third item - baz"));
        final CmsSelectBox selectbox = new CmsSelectBox(CmsSelectBox.Mode.TEXT, items);
        panel.add(selectbox);
        Button button = new Button("show value");
        panel.add(button);
        button.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent e) {

                Window.alert("value = " + selectbox.getFormValue());

            }
        });

        final CmsMultiCheckBox multiBox = new CmsMultiCheckBox(items);
        panel.add(multiBox);
        Button button2 = new Button("show value");
        panel.add(button2);
        button2.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent e) {

                String selected = "";
                for (String item : multiBox.getSelected()) {
                    selected += "|" + item;

                }
                Window.alert("value = " + selected);
            }
        });

        final CmsRadioButtonGroup radioGroup = new CmsRadioButtonGroup(items);
        Button button3 = new Button("show value");
        panel.add(button3);
        button3.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent e) {

                Window.alert("value = " + radioGroup.getFormValue());

            }
        });

        panel.add(radioGroup);
        panel.add(button3);
        final CmsTextBox text = new CmsTextBox();
        panel.add(text);
        Button button4 = new Button("show value");
        panel.add(button4);
        button4.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent e) {

                Window.alert("value = " + text.getText());
            }
        });

        final CmsTextArea text2 = new CmsTextArea();
        panel.add(text2);
        Button button5 = new Button("show value");
        panel.add(button5);
        button5.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent e) {

                Window.alert("value = " + text2.getFormValue());
            }
        });

        Button button6 = new Button("show errors");
        panel.add(button6);
        button6.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent e) {

                for (I_CmsFormWidget widget : new I_CmsFormWidget[] {selectbox, text, text2, multiBox, radioGroup}) {
                    widget.setErrorMessage("error");
                }
            }
        });

    }

    static {
        CSS.ensureInjected();
    }
}
