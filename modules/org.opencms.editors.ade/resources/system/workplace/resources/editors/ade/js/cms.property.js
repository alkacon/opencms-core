(function(cms) {
   var M = cms.messages;
   
   
   var dump = function(obj) {
      $('<pre></pre>').text($.dump(obj)).appendTo('body');
   }
   
   var makeDialogDiv = cms.property.makeDialogDiv = function(divId) {
      var $d = $('#' + divId);
      if ($d.size() == 0) {
         $d = $('<div></div>').attr('id', divId).css('display', 'none').appendTo('body');
      }
      $d.empty();
      return $d;
   }
   
   /**
    * Builds a row of the property editor table and stores the row's widget in another object.
    *
    * @param {Object} name the name of the property
    * @param {Object} entry the property entry
    * @param {Object} widgets the object in which this row's widget should be stored
    */
   var buildRow = function(name, entry, widgets) {
      var $row = $('<tr></tr>');
      var widgetClass = widgetTypes[entry.widget];
      var niceName = entry.niceName ? entry.niceName : name;
      var isDefault = !(entry.value);
      $('<td></td>').text(niceName).appendTo($row);
      var widget = new widgetClass(entry.widgetConf, entry.defaultValue);
      var widgetWrapper = new WidgetWrapper(widget, name, entry, $row);
      var $widget = widget.$widget;
      $('<td></td>').append($widget).appendTo($row);
      var $checkbox = $('<input type="checkbox"></input>').attr('checked', isDefault).click(function() {
         widgetWrapper.useDefault(!!$checkbox.attr('checked'));
      });
      $('<td></td>').append($checkbox).appendTo($row);
      widgets[name] = widgetWrapper;
      return $row;
   };
   
   var buildPropertyRow = function(name, entry, widgets) {
      var widgetClass = widgetTypes[entry.widget];
      var niceName = entry.niceName ? entry.niceName : name;
      var isDefault = !(entry.value);
      var $row = $('<div class="cms-editable-field' + (isDefault ? ' cms-default-value' : '') + '" rel="' + name + '" style="margin:3px 0px;"><span class="cms-item-title cms-width-90">' + niceName + '</span></div>')
      var widgetClass = widgetTypes[entry.widget];
      var niceName = entry.niceName ? entry.niceName : name;
      var isDefault = !(entry.value);
      var widget = new widgetClass(entry.widgetConf, entry.defaultValue);
      widget.$widget.appendTo($row);
      var widgetWrapper = new WidgetWrapper(widget, name, entry, $row);
      
      widgets[name] = widgetWrapper;
      return $row;
   }
   
   
   /**
    * Constructor for the basic text input widget.
    * @param configuration not needed for this widget
    */
   var StringWidget = cms.property.StringWidget = function(configuration, defaultValue) {
      var self = this;
      var $widget = self.$widget = $('<input type="text" class="cms-item-edit ui-corner-all" value="" />');
      $widget.focus(function() {
         self.onFocus();
      });
      $widget.blur(function() {
         self.onBlur();
      });
      self.defaultValue = defaultValue;
   }
   
   StringWidget.prototype = {
   
      /**
       * Helper function to enable or disable the widget
       * @param {Boolean}
       */
      setEnabled: function(/**Boolean*/enabled) {
         if (enabled) {
            this.$widget.closest('.cms-editable-field').removeClass('cms-default-value');
         } else {
            this.$widget.closest('.cms-editable-field').addClass('cms-default-value');
            this.setValue(this.defaultValue);
         }
         //this.$widget.attr('disabled', !enabled);
      },
      
      /**
       * Helper function that returns the current value of the text field.
       */
      getValue: function() {
         if (this.$widget.closest('.cms-editable-field').hasClass('cms-default-value')) {
            return null;
         }
         return this.$widget.val();
      },
      
      setValue: function(newValue) {
         this.$widget.val(newValue);
      },
      
      onFocus: function() {
         if (this.$widget.closest('.cms-editable-field').hasClass('cms-default-value')) {
            this.$widget.val('');
            this.setEnabled(true);
         }
      },
      
      onBlur: function() {
         if ($.trim(this.$widget.val()) == '') {
            this.setEnabled(false);
         }
      }
   };
   
   /**
    * Basic select box widget
    * @param {String} configuration a '|'-separated list of selection values, for example 'choice1|choice2|choice3'
    */
   var SelectorWidget = cms.property.SelectorWidget = function(configuration) {
      var options = parseSelectOptions(configuration);
      this.$widget = _buildSelectList(options);
   }
   
   SelectorWidget.prototype = {
      setEnabled: function(enabled) {
         this.$widget.attr('disabled', !enabled);
      },
      
      getValue: function() {
         return this.$widget.val();
      },
      
      setValue: function(value) {
         var selectElem = this.$widget.get(0);
         for (var i = 0; i < selectElem.length; i++) {
            if (selectElem.options[i].value == value) {
               selectElem.selectedIndex = i;
               return;
            }
         }
      }
   };
   
   
   /**
    * Basic widget consisting of a single checkbox.
    *
    * @param {Object} configuration not needed for this class
    */
   var CheckboxWidget = cms.property.CheckboxWidget = function(configuration, defaultValue) {
      var self = this;
      this.$widget = $('<div class="cms-checkbox-widget" style="position: relative; display: inline-block; width: 200px; vertical-align: top;"></div>');
      this.$widgetOverlay = $('<div class="cms-widget-overlay" style="display: none; position: absolute; top: 0px; left: 0px; width: 200px; background: #FFFFFF; border: solid 1px #999999; padding: 2px;"></div>').appendTo(this.$widget);
      if (configuration) {
         var config = JSON.parse(configuration);
         if (config && config.values && config.values.length) {
            for (var i = 0; i < config.values.length; i++) {
               this.$widget.append('<div class="cms-checkbox-widget-row"><input type="checkbox" value="' + config.values[i] + '" style="vertical-align: middle;" /><span class="cms-checkbox-label">' + config.values[i] + '</span></div>');
            }
            this.$defaultSwitch = $('<div class="cms-widget-defaultswitch"><span>Use default</span>&nbsp;<input type="checkbox" value="true" name="defaultswitch" /></div>').appendTo(this.$widgetOverlay);
            $('input:checkbox', this.$defaultSwitch).change(function() {
               self.setEnabled(!this.checked);
            });
            this.$widget.hover(function() {
               self.hoverIn();
            }, function() {
               self.hoverOut();
            })
         }
      }
      
      this.defaultValue = defaultValue;
   }
   
   CheckboxWidget.prototype = {
   
      getValue: function() {
         if (this.$widget.closest('.cms-editable-field').hasClass('cms-default-value')) {
            return null;
         }
         var result = '';
         var selected=$('input:checked', this.$widget)
         selected.each(function() {
            result += $(this).val() + '|';
         });
         result = result.substring(0, result.length - 1);
         return result;
      },
      
      setValue: function(newValue) {
         $('input:checkbox[name!="defaultswitch"]', this.$widget).removeAttr('checked');
         if (newValue == null) {
            return;
         }
         var values = newValue.split('|');
         for (var i = 0; i < values.length; i++) {
            $('input[value="' + values[i] + '"]', this.$widget).attr('checked', 'checked');
         }
         
      },
      
      hoverIn: function() {
         $('div.cms-checkbox-widget-row', this.$widget).prependTo(this.$widgetOverlay);
         this.$widgetOverlay.show();
      },
      
      hoverOut: function() {
         $('div.cms-checkbox-widget-row', this.$widgetOverlay).prependTo(this.$widget);
         this.$widgetOverlay.hide();
      },
      
      /**
       * Helper function to enable or disable the widget
       * @param {Boolean}
       */
      setEnabled: function(/**Boolean*/enabled) {
         if (enabled) {
            $('input:checkbox[name="defaultswitch"]', this.$widget).removeAttr('checked');
            this.$widget.closest('.cms-editable-field').removeClass('cms-default-value');
         } else {
            $('input:checkbox[name!="defaultswitch"]', this.$widget).attr('checked','checked');
            this.$widget.closest('.cms-editable-field').addClass('cms-default-value');
            this.setValue(this.defaultValue);
         }
      }
   };
   
   var bindFunction = function(fn, arg) {
      return function() {
         fn(arg);
      }
   }
   
   var _buildColorTable = function(colorArray, colorCallback) {
      var $table = $('<table></table>');
      for (var i = 0; i < colorArray.length; i++) {
         var row = colorArray[i];
         var $tblRow = $('<tr></tr>');
         for (var j = 0; j < colorArray[i].length; j++) {
            var color = colorArray[i][j];
            var $td = $('<td></td>').css('background-color', color).css('width', '20px').css('height', '20px');
            $td.click(bindFunction(colorCallback, color));
            $tblRow.append($td);
         }
         $table.append($tblRow);
      }
      return $table;
   }
   
   var ColorWidget = function(configuration) {
      var colorTbl = []
      var rows = configuration.split('|');
      for (var i = 0; i < rows.length; i++) {
         colorTbl.push(rows[i].split(';'));
      }
      var $outer = $('<div></div>');
      var $inner = $('<div style="width:100%">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div>').appendTo($outer);
      this.enabled = true;
      var self = this;
      $inner.click(function() {
         if (!self.enabled) {
            return;
         }
         var $dlg = makeDialogDiv('cms-dlg-colorpicker');
         $dlg.empty();
         var options = {
            autoOpen: true,
            title: M.COLOR_SELECT_TITLE,
            modal: true,
            stack: true,
            zIndex: 10999,
            close: function() {
               $dlg.dialog('destroy');
            }
         };
         $dlg.append(_buildColorTable(colorTbl, function(color) {
            $dlg.dialog('destroy');
            $inner.css('background-color', color).attr('rel', color);
         }));
         $dlg.dialog(options);
      });
      this.$widget = $outer;
      this.$inner = $inner;
   }
   
   ColorWidget.prototype = {
      setValue: function(newValue) {
         this.$inner.css('background-color', newValue).attr('rel', newValue);
      },
      
      getValue: function() {
         return this.$inner.attr('rel');
      },
      
      setEnabled: function(enabled) {
         this.enabled = enabled;
         this.$widget.css('border-color', enabled ? '#000000' : '#dddddd');
      }
   }
   
   
   var RadioWidget = function(configuration) {
      this.name = "cms-radio-" + Math.floor(Math.random() * 10000000000000001);
      var options = parseSelectOptions(configuration);
      var $widget = this.$widget = $('<div></div>');
      var radioButtons = this.radioButtons = [];
      for (var optionValue in options) {
         var optionText = options[optionValue];
         var $radio = $('<input type="radio" name="' + this.optionValue + '"></input>').attr('value', optionValue);
         $radio.appendTo($widget);
         $('<span></span>').text(optionText).appendTo($widget);
         
         $widget.append('<br>');
         radioButtons.push($radio);
      }
   }
   
   RadioWidget.prototype = {
      setValue: function(newValue) {
         for (var i = 0; i < this.radioButtons.length; i++) {
            var radioElem = this.radioButtons[i].get(0);
            radioElem.checked = (radioElem.value == newValue);
         }
      },
      
      getValue: function() {
         for (var i = 0; i < this.radioButtons.length; i++) {
            var radioElem = this.radioButtons[i].get(0);
            if (radioElem.checked) {
               return radioElem.value;
            }
         }
      },
      
      setEnabled: function(enabled) {
         for (var i = 0; i < this.radioButtons.length; i++) {
            this.radioButtons[i].attr('disabled', !enabled);
         }
      }
   }
   
   var DateWidget = function(configuration) {
      var $widget = this.$widget = $('<input></input>').datepicker();
   }
   
   DateWidget.prototype = {
      setValue: function(newValue) {
         this.$widget.val(newValue);
      },
      
      getValue: function() {
         return this.$widget.val();
      },
      
      setEnabled: function(enabled) {
         this.$widget.attr('disabled', !enabled);
      }
   }
   
   
   var validateString = function(validation, s) {
      if (validation.substring(0, 1) == '!') {
         return !s.match('^' + validation.substring(1) + '$');
      } else {
         return s.match('^' + validation + '$');
      }
   }
   
   
   /**
    * Wrapper class for widgets which keeps track of the default state and default value.<p>
    *
    * The wrapper can be in two states: the 'default' and 'nondefault' state.
    * When the state is changed to 'default', the underlying widget is deactivated and its value set to the default value.
    * When the state is changed to 'nondefault', the widget is activated.
    * When the save method is called, the property value will only be saved if the current state is 'nondefault', and the value
    * will be taken directly from the widget.
    *
    * @param {Object} widget the underlying widget
    * @param {Object} name the name of the edited property
    * @param {Object} entry the property entry
    * @param {Object} $row the table row for the property
    */
   var WidgetWrapper = cms.property.WidgetWrapper = function(widget, name, entry, $row) {
      this.$row = $row;
      this.widget = widget;
      var isDefault = !entry.value;
      this.value = isDefault ? entry.defaultValue : entry.value;
      this.name = name;
      this.defaultValue = entry.defaultValue;
      this.isDefault = isDefault;
      this.widget.setEnabled(!isDefault);
      this.widget.setValue(this.value);
      this.validation = entry.ruleRegex;
      this.validationError = entry.error;
   }
   
   WidgetWrapper.prototype = {
      useDefault: function(defaultState) {
         this.isDefault = defaultState;
         this.widget.setEnabled(!defaultState);
         if (defaultState) {
            this.widget.setValue(this.defaultValue);
         }
      },
      
      save: function(properties) {
         var val = this.widget.getValue();
         if (val != null) {
            properties[this.name] = val;
         } else {
            delete properties[this.name];
         }
         //         if (this.isDefault) {
         //            delete properties[this.name];
         //         } else {
         //            properties[this.name] = this.widget.getValue();
         //         }
      },
      
      validate: function() {
         var validationOK = !this.validation || this.isDefault || validateString(this.validation, this.widget.getValue());
         $('.cms-validation-error', this.$row).remove();
         if (!validationOK) {
            $('<div class="cms-validation-error">&#x25B2; ' + this.validationError + '</div>').appendTo(this.$row);
         }
         /*  this.$row.next('.cms-validation-error').remove();
          if (!validationOK) {
          var $validationRow = $('<tr class="cms-validation-error"></tr>').css('color', '#ff0000');
          $validationRow.append('<td>&#x25B2;</td>');
          var $validationError = $('<td colspan="2"></td>').text(this.validationError);
          $validationRow.append($validationError);
          this.$row.after($validationRow);
          }  */
         return validationOK;
      }
   };
   
   
   
   /**
    * The available widget types.
    */
   var widgetTypes = {
      'string': StringWidget,
      'selector': SelectorWidget,
      'checkbox': CheckboxWidget,
      'color': ColorWidget,
      'radio': RadioWidget
   }
   
   /**
    * Builds a table containing, for each property, its name, a widget for changing it, and a checkbox to reset
    * the property to the default state.
    *
    * @param {Object} properties the properties which should be in the table
    * @param {Object} defaults the object containing the default values and widget types
    * @param {Object} widgets the object in which the widget objects should be stored
    */
   var buildPropertyTable = cms.property.buildPropertyTable = function(properties, widgets) {
      var $table = $('<table cellspacing="0" cellpadding="3" align="left"></table>');
      $table.append('<tr><th><b>' + M.PROPERTIES_HEADING_NAME + '</b></th><th><b>' + M.PROPERTIES_HEADING_EDIT + '</b></th><th><b>' + M.PROPERTIES_HEADING_DEFAULT + '</b></th></tr>');
      var $fields = $('<div></div>')
      for (var propName in properties) {
         //         var defaultEntry = properties[propName];
         //         var widgetClass = widgetTypes[_getWidgetType(defaultEntry)];
         //         var defaultValue = _getDefaultValue(defaultEntry);
         //         var configuration = _getWidgetConfiguration(defaultEntry);
         //         var value = null;
         //         var isDefault = true;
         //         if (defaultEntry.hasOwnProperty('value')) {
         //            value = defaultEntry.value;
         //            isDefault = false;
         //         }
         var $row = buildPropertyRow(propName, properties[propName], widgets);
         $row.appendTo($fields);
      }
      return $fields;
      
   }
   
   /**
    * Helper function for saving the values of a set of widgets to a properties object.
    *
    * @param {Object} props the object in which the widget values should be stored.
    * @param {Object} widgets a map from property names to widgets
    */
   var saveWidgetValues = cms.property.saveWidgetValues = function(props, widgets) {
      for (widgetName in widgets) {
         widgets[widgetName].save(props);
      }
   }
   
   var setDialogButtonEnabled = cms.property.setDialogButtonEnabled = function($button, enabled) {
      if (enabled) {
         $button.attr('disabled', false).css('color', '#000000');
      } else {
         $button.attr('disabled', true).css('color', '#aaaaaa');
      }
   }
   
   /**
    * Checks whether an object has no properties.
    * @param {Object} obj
    */
   var _isEmpty = function(obj) {
      for (var key in obj) {
         return false;
      }
      return true;
   }
   
   /**
    * Displays the property editor.
    *
    * @param {Object} properties the current non-default properties
    * @param {Object} defaults the property configuration containing defaults and widget types
    */
   var showPropertyEditor = cms.property.showPropertyEditor = function(properties, callback) {
      var widgets = {}
      var newProps = {};
      if (_isEmpty(properties)) {
         cms.util.dialogAlert(M.NO_PROPERTIES, M.NO_PROPERTIES_TITLE);
         return;
      }
      var $table = buildPropertyTable(properties, widgets);
      var $dlg = makeDialogDiv('cms-property-dialog');
      $dlg.empty().append($table);
      
      var _destroy = function() {
         $dlg.dialog('destroy');
      }
      
      var buttons = {};
      buttons[M.EDIT_PROPERTIES_CANCEL] = function() {
         _destroy();
      };
      var options = {
         title: M.EDIT_PROPERTIES_TITLE,
         modal: true,
         autoOpen: true,
         width: 440,
         zIndex: 9999,
         close: _destroy,
         buttons: buttons
      }
      
      $dlg.dialog(options);
      
      
      var $ok = $('<button></button>').addClass('ui-corner-all').addClass('ui-state-default').text(M.EDIT_PROPERTIES_OK);
      
      var validateAll = function() {
         var result = true;
         setDialogButtonEnabled($ok, true);
         for (var key in widgets) {
            if (!widgets[key].validate()) {
               setDialogButtonEnabled($ok, false);
               result = false;
            }
         }
         return result;
      }
      
      $dlg.click(function() {
         validateAll();
         return true;
      });
      $dlg.nextAll().click(validateAll);
      $dlg.keydown(function(e) {
         // user pressed Tab key
         if (e.keyCode == 9) {
            validateAll();
         }
      });
      
      $ok.click(function() {
         if (validateAll()) {
            _destroy();
            saveWidgetValues(newProps, widgets);
            callback(newProps);
         }
      });
      $dlg.nextAll('.ui-dialog-buttonpane').append($ok);
   }
   
   /**
    * Returns the default value for a property default entry
    * @param {Object} defaultEntry
    */
   var _getDefaultValue = function(defaultEntry) {
      return defaultEntry.defaultValue;
   }
   
   /**
    * Returns the widget type for a property default entry
    * @param {Object} defaultEntry
    */
   var _getWidgetType = function(defaultEntry) {
      return defaultEntry.widget;
   }
   
   var _getWidgetConfiguration = function(defaultEntry) {
      return defaultEntry.widgetConf;
   }
   
   var _getKey = function(obj) {
      for (var key in obj) {
         return key;
      }
   }
   
   /**
    * Function for editing the element properties of a given element.<p>
    *
    * After editing is finished, this function will reload the element which was
    * edited and update the display.
    *
    * @param {Object} $element the element for which the properties should be edited
    */
   var editProperties = cms.property.editProperties = function($element) {
   
      var $container = $element.parent();
      var containerType = cms.data.containers[$container.attr('id')].type;
      var id = $element.attr('rel');
      cms.data.getProperties(id, function(ok, data) {
         var properties = data.properties;
         if (!ok) {
            return;
         }
         showPropertyEditor(properties, function(newProperties) {
            cms.data.getElementWithProperties(id, newProperties, function(ok, data) {
               if (!ok) {
                  return;
               }
               cms.toolbar.setPageChanged(true);
               var newElement = data.elements[_getKey(data.elements)];
               $element.replaceWith(newElement.getContent(containerType));
               cms.move.updateContainer($container.attr('id'));
               $('#toolbar_content button.ui-state-active').trigger('click').trigger('click');
            });
         });
      });
   }
   
   
   /**
    * Returns a list of ids of elements from cms.data.elements which have an id that starts with a given string.<p>
    *
    * This is useful if we want to get all elements with a given resource id, regardless of the properties
    * encoded in the id.
    *
    * @param {Object} prefix the prefix that the returned element ids should have
    */
   var getElementsWithSamePrefix = cms.property.getElementsWithSamePrefix = function(id) {
      var hashPos = id.indexOf('#');
      if (hashPos != -1) {
         id = id.substring(0, hashPos);
      }
      var result = [];
      for (var elementName in cms.data.elements) {
         if (elementName.match('^' + id)) {
            result.push(elementName);
         }
      }
      return result;
   }
   /**
    * This function is used to reload all elements with the same resource id after the corresponding resource is
    * edited.<p>
    *
    * To update the display, this function also empties and re-fills all containers after the elements have been
    * reloaded.
    * @param {Object} id
    */
   var updateEditedElement = cms.property.updateEditedElement = function(id) {
      var hashPos = id.indexOf('#');
      if (hashPos != -1) {
         id = id.substring(0, hashPos);
      }
      var elementsToReload = getElementsWithPrefix(unpackedId.id);
      cms.data.loadElements(elementsToReload, function(ok, data) {
         cms.data.fillContainers();
      });
   }
   
   
   var parseSelectOptions = function(configuration) {
      var result = {};
      var items = configuration.split('|');
      for (var i = 0; i < items.length; i++) {
         var text = '';
         var value = '';
         var currentItem = items[i];
         var colonPos = currentItem.indexOf(':');
         if (colonPos == -1) {
            text = value = currentItem;
         } else {
            value = currentItem.substring(0, colonPos);
            text = currentItem.substring(colonPos + 1);
         }
         result[value] = text;
      }
      return result;
   }
   
   
   /**
    * Builds a HTML select list from an array of options
    * @param {Object} options
    */
   var _buildSelectList = function(options) {
      var $select = $('<select></select>');
      for (var value in options) {
         $('<option></option>').attr('value', value).text(options[value]).appendTo($select);
      };
      return $select;
   }
   
})(cms);
