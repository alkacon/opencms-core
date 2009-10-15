(function(cms) {


   var dump = function(obj) {
      $('<pre></pre>').text($.dump(obj)).appendTo('body');
   }
   
   var makeDialogDiv = function(divId) {
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
    * @param {Object} widgetClass the widget constructor function to use
    * @param {Object} name the name of the property
    * @param {Object} currentValue the current value of the property
    * @param {Object} defaultValue the default value of the property
    * @param {Object} isDefault true if the property is set to default
    * @param {String} configuration the widget configuration
    * @param {Object} widgets the object in which this row's widget should be stored
    */
   var buildRow = function(widgetClass, name, currentValue, defaultValue, isDefault, configuration, widgets) {
      var $row = $('<tr></tr>');
      $('<td></td>').text(name).appendTo($row);
      var widget = new widgetClass(configuration);
      var widgetWrapper = new WidgetWrapper(widget, name, currentValue, defaultValue, isDefault);
      var $widget = widget.$widget;
      $('<td></td>').append($widget).appendTo($row);
      var $checkbox = $('<input type="checkbox"></input>').attr('checked', isDefault).click(function() {
         widgetWrapper.useDefault(!!$checkbox.attr('checked'));
      });
      $('<td></td>').append($checkbox).appendTo($row);
      widgets[name] = widgetWrapper;
      return $row;
   };
   
   /**
    * Constructor for the basic text input widget.
    * @param configuration not needed for this widget
    */
   var StringWidget = cms.property.StringWidget = function(configuration) {
      var $widget = this.$widget = $('<input type="text"></input>');
   }
   
   StringWidget.prototype = {
   
      /**
       * Helper function to enable or disable the widget
       * @param {Boolean}
       */
      setEnabled: function(/**Boolean*/enabled) {
         this.$widget.attr('disabled', !enabled);
      },
      
      /**
       * Helper function that returns the current value of the text field.
       */
      getValue: function() {
         return this.$widget.val();
      },
      
      setValue: function(newValue) {
         this.$widget.val(newValue);
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
   var CheckboxWidget = cms.property.CheckboxWidget = function(configuration) {
      this.$widget = $('<input type="checkbox"></input>');
   }
   
   CheckboxWidget.prototype = {
   
      getValue: function() {
         return "" + this.$widget.get(0).checked;
      },
      
      setValue: function(newValue) {
         var realValue = false;
         if (newValue == "true" || newValue == "checked" || newValue == true) {
            realValue = true;
         }
         
         this.$widget.get(0).checked = realValue;
      },
      
      setEnabled: function(enabled) {
         this.$widget.attr('disabled', !enabled);
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
            title: 'Select color',
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
   
   /*transfer*/
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
    * @param {Object} value the initial value of the property (will be ignored if isDefault == true)
    * @param {Object} defaultValue the default value of the property
    * @param {Object} isDefault true if the wrapper should be in the 'default' state initially
    */
   var WidgetWrapper = cms.property.WidgetWrapper = function(widget, name, value, defaultValue, isDefault) {
      this.widget = widget;
      this.value = isDefault ? defaultValue : value;
      this.name = name;
      this.defaultValue = defaultValue;
      this.isDefault = isDefault;
      this.widget.setEnabled(!isDefault);
      this.widget.setValue(this.value);
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
         if (this.isDefault) {
            delete properties[this.name];
         } else {
            properties[this.name] = this.widget.getValue();
         }
      }
   };
   
   
   
   /**
    * The available widget types.
    */
   var widgetTypes = {
      'string': StringWidget,
      'sel': SelectorWidget,
      'chk': CheckboxWidget,
      'col': ColorWidget
   
   }
   
   /**
    * Builds a table containing, for each property, its name, a widget for changing it, and a checkbox to reset
    * the property to the default state.
    *
    * @param {Object} properties the properties which should be in the table
    * @param {Object} defaults the object containing the default values and widget types
    * @param {Object} widgets the object in which the widget objects should be stored
    */
   var buildPropertyTable = function(properties, widgets) {
      var $table = $('<table border="1" cellspacing="0" cellpadding="3"></table>');
      $table.append('<tr><th><b>Property</b></th><th><b>Edit</b></th><th><b>Default</b></th></tr>');
      for (var propName in properties) {
         var defaultEntry = properties[propName];
         var widgetClass = widgetTypes[_getWidgetType(defaultEntry)];
         var defaultValue = _getDefaultValue(defaultEntry);
         var configuration = _getWidgetConfiguration(defaultEntry);
         var value = null;
         var isDefault = true;
         if (defaultEntry.hasOwnProperty('value')) {
            value = defaultEntry.value;
            isDefault = false;
         }
         var $row = buildRow(widgetClass, propName, value, defaultValue, isDefault, configuration, widgets);
         $row.appendTo($table);
      }
      return $table;
   }
   
   /**
    * Helper function for saving the values of a set of widgets to a properties object.
    *
    * @param {Object} props the object in which the widget values should be stored.
    * @param {Object} widgets a map from property names to widgets
    */
   var _saveWidgetValues = function(props, widgets) {
      for (widgetName in widgets) {
         widgets[widgetName].save(props);
      }
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
      var $table = buildPropertyTable(properties, widgets);
      var $dlg = makeDialogDiv('cms-property-dialog');
      $dlg.empty().append($table);
      
      var _destroy = function() {
         $dlg.dialog('destroy');
      }
      
      var buttons = {};
      buttons['Ok'] = function() {
         _saveWidgetValues(newProps, widgets);
         callback(newProps);
         _destroy();
      };
      buttons['Cancel'] = function() {
         _destroy();
      };
      var options = {
         title: 'Edit Properties',
         modal: true,
         autoOpen: true,
         width: 440,
         zIndex: 9999,
         close: _destroy,
         buttons: buttons
      }
      
      $dlg.dialog(options);
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
   
   //   var properties = {
   //      'foo': '42'
   //   };
   //   var colors = '#440000;#880000;#cc0000|#004400;#008800;#00cc00|#000044;#000088;#0000cc';
   //   var defaults = {
   //      foo: ['23', 'str', null],
   //      bar: ['999', 'str', null],
   //      baz: ['a', 'sel', 'a|b|c|d'],
   //      qux: ['true', 'chk', ''],
   //      co: ['#ff0000', 'col', colors]
   //   };
   //   
   //   
   //   $(document).ready(function() {
   //      showPropertyEditor(properties, defaults, function(newProperties) {
   //         dump(newProperties);
   //      });
   //      
   //   });
   
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
         delete properties['state'];
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
