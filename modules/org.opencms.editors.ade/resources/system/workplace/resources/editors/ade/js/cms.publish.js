(function(cms) {

   var M = cms.messages;
   var STATE_CHANGED = 'C';
   var STATE_DELETED = 'D';
   var STATE_NEW = 'N';
   
   /**
    * Retrieves the CSS classes that should be 
    * @param {Object} item
    */
   var getCssClassesForItem = function(item) {
      var classes = [];
      if (item.reason == 'published') {
         classes.push('cms-pstate-published');
      }
      if (item.reason == 'locked') {
         classes.push('cms-pstate-locked');
      }
      if (item.state == 'N') {
         classes.push('cms-fstate-new');
      }
      if (item.state == 'C') {
         classes.push('cms-fstate-changed');
      }
      if (item.state == 'D') {
         classes.push('cms-fstate-deleted');
      }
      
      return classes.join(' ');
   }
   
   /**
    * Maps the reason field of a data item to a human-readable description 
    * @param {Object} reason the reason value
    */
   var getReasonString = function(reason) {
      var reasonMap = {
         perm: 'no publish permission',
         pub: 'already published',
         locked: 'locked'
      }
      return '('+reasonMap[reason]+')';
   }
   
   /**
    * Creates the table row for the first stage of the publish dialog
    * @param {Object} item the item for which the row should be generated
    * @param {Object} cssClasses the css classes to add to the row
    */
   var createPublishItem = function(item, cssClasses) {
       var td = function($row, cssClass) {
         return $('<td></td>').appendTo($row).addClass(cssClass ? cssClass : '');
      }
      var $row = $('<tr class="cms-publish-item"></tr>').addClass(cssClasses);
      $('<td><img src="' + item.icon + '"></img></td>').appendTo($row);
      var $status = $('<td></td>');
      
      var problemClass = null;
      var statusIconPath = null;
      var hoverText = "";
      if (item.lockedBy) {
         problemClass = 'cms-publish-problem-locked';
         hoverText = 'locked by ' + item.lockedBy;
         
      } else if (item.publishState == 'p') {
         problemClass = 'cms-publish-problem-published';
         hoverText = 'already published';
      }
      if (problemClass != null) {
         var $link = $('<a>&nbsp;&nbsp;&nbsp;</a>').addClass('cms-publish-problem').addClass(problemClass).attr('title', hoverText);
         $link.appendTo($status);
      }
      //$status.appendTo($element);
      //$('<td></td>').text(getReasonString(item.reason)).appendTo($element);
      $('<td class="cms-publish-title"></td>').text(item.title).appendTo($row);
      $('<td class="cms-publish-path"></td>').text(item.uri).appendTo($row);
      $('<td class="cms-publish-changedate"></td>').text("DUMMY DATE").appendTo($row);
      $('<td></td>').text(getReasonString(item.reason)).appendTo($row);
      $('<td class="cms-publish-state"></td>').text(item.state).appendTo($row);
      //td($element, )
      var $del = null;
      if (item.reason != 'pub') {
          $del = $('<a href="#" class="cms-publish-list-remove"></a>').text('Remove').attr('title', 'Remove from publish list').click(function() {
              var id = $.data(this, 'cms-item');
              cms.data.removeFromPublishList(id, function() {
                  var $table = $row.closest('table');
                  $row.remove();
                  resetRowParity($table);
              });
          });
      } else {
          $del = $('<p class="cms-publish-remove-auto"></p>').text('remove automatically');
      }
      td($row).append($del);

      $row.addClass(getCssClassesForItem(item));
      $.data($row.get(0), 'resource_id', item.id);
      return $row;
   }
   
   
   
   /**
    * Resets the button row of a jQuery UI dialog
    * @param {Object} $dialog the dialog for which the buttons should be set
    * @param {Object} buttons the new buttons that should be appended to the dialog
    */
   var setButtons = function($dialog, buttons) {
      var $buttonPane = $dialog.nextAll('.ui-dialog-buttonpane');
      $buttonPane.empty();
      for (var i = buttons.length - 1; i >= 0; i--) {
         var $button = buttons[i];
         $button.addClass('ui-corner-all ui-state-default').appendTo($buttonPane);
      }
   }
   
   /**
    * Constructs a table with a single row containing the strings passed into the function as headers.
    *  
    * @param {Object} headers the table headers
    */
   var makeTableWithHeaders = function(headers) {
      var $table = $('<table border="0" cellspacing="0" class="cms-publish-items-table"></table>');
      var $row = $('<tr></tr>');
      for (var i = 0; i < headers.length; i++) {
         $('<th></th>').text(headers[i]).appendTo($row);
      }
      $row.appendTo($table);
      return $table;
   }
   
   /** 
    * Constructor for the PublishDialog class.
    * @param {Object} isAdmin a flag indicating whether the current user is the administrator
    */
   var PublishDialog = cms.publish.PublishDialog = function(isAdmin) {
       this.isAdmin = isAdmin;
      // empty
   }
   
   /**
    * Returns an image tag for the data item wrapped in a jQuery object.
    * @param {Object} item the data item for which the icon should be retrieved 
    */
   var getItemIcon = function(item) {
      return $('<img></img>').attr('src', item.icon);
   }
   
   /**
    * Corrects the cms-even/odd classes in a table after deleting a row.
    * @param {Object} $table the table for which the classes should be corrected 
    */
   var resetRowParity = function($table) {
      
      $table.find('tr.cms-even:nth-child(odd)').removeClass('cms-even').addClass('cms-odd');
      $table.find('tr.cms-odd:nth-child(even)').removeClass('cms-odd').addClass('cms-even');
   }
   /**
    * Creates a row for the second stage of the publish dialog
    * @param {Object} item the data item for which the row should be generated
    * @param {Object} cssClasses the css classes to add to the row
    */
   var createPublishItem2 = function(item, cssClasses) {
      var td = function($row, cssClass) {
         return $('<td></td>').appendTo($row).addClass(cssClass ? cssClass : '');
      }
      var $row = $('<tr class="cms-publish-item"></tr>').addClass(cssClasses);
      var $checkbox = $('<input type="checkbox" class="cms-publish-checkbox"></input>').attr('rel', item.id);
      $checkbox.get(0).checked = true;
      td($row).append($checkbox);
      td($row).append(getItemIcon(item));
      td($row, "cms-publish-title").text(item.title);
      var $path = td($row, "cms-publish-path");
      $path.append($('<div></div>').text(item.uri));
      $path.append($('<div class="cms-related"></div>').text(item.infoName + item.infoValue).css('position', 'relative').css('left', '40px'));
      $path.append($('<div class="cms-related"></div>').text(item.infoName + item.infoValue).css('position', 'relative').css('left', '40px'));
      
      
      td($row, "cms-publish-date").text('DUMMY DATE');
      
      var $del = $('<a href="#" class="cms-publish-list-remove"></a>').text('Remove').attr('title', 'Remove from publish list').click(function() {
         var id = $.data(this, 'cms-item');
         cms.data.removeFromPublishList(id, function() {
            var $table = $row.closest('table');
            $row.remove();
            resetRowParity($table);
         });
      });
      td($row).append($del);
      $.data($row.get(0), 'cms-item', item.id);
      $row.addClass(getCssClassesForItem(item));
      return $row;
   }
   /**
    * Creates a table row for the third stage of the publish dialog.
    * 
    * @param {Object} item the item for which the row should be generated 
    * @param {Object} cssClasses the css classes to add to the row
    */
   var createPublishItem3 = function(item, cssClasses) {
      var td = function($row, cssClass) {
         return $('<td></td>').appendTo($row).addClass(cssClass ? cssClass : '');
      }
      var $row = $('<tr class="cms-publish-item"></tr>').addClass(cssClasses);
      td($row).append(getItemIcon(item));
      td($row, "cms-publish-title").text(item.title);
      var $path = td($row, "cms-publish-path");
      $path.append($('<div></div>').text(item.uri));
      $path.append($('<div class="cms-related"></div>').text(item.infoName +' '+ item.infoValue).css('margin-left', '40px'));
      return $row;
   }
   
   /**
    * Creates a p tag containing both a checkbox and a label text and returns both elements in an object
    * 
    * @param {Object} title the text with which the checkbox should be labeled 
    */
   var createCheckboxLine = function(title) {
   
      var $line = $('<p></p>');
      var $checkbox = $('<input type="checkbox"></input>')
      var $label = $('<span></span>').text(title);
      $line.append($checkbox).append($label);
      return {
         line: $line,
         checkbox: $checkbox
      };
   }
   
   
   
   PublishDialog.prototype = {
      /**
       * Activates the dialog
       */
      start: function() {
         var self = this;
         cms.data.getPublishProblemList(function(ok, data) {
            self.goToPublishProblemsState(data.resources);
         });
      },
      
      /**
       * Gets the current instance of the dialog, or creates it if there isn't one
       */
      getDialog: function() {
         if (!this.$dialog) {
            if ($('#cms-publish-dialog').size() == 0) {
               $('<div id="cms-publish-dialog"></div>').appendTo('body');
            }
            this.$dialog = $('#cms-publish-dialog');
            this.$dialog.dialog({
               autoOpen: true,
               modal: true,
               zIndex: 10000,
               width: '70em',
               resizable: true,
               title: 'Publish',
               close: function() {
                  $dlg.dialog('destroy');
               },
               buttons: {
                  dummy: function() {
                                    }
               }
            }); // dialog
         }
         return this.$dialog;
      },
      
      setButtons: function(buttons) {
         var $buttonPane = this.$dialog.nextAll('.ui-dialog-buttonpane');
         $buttonPane.empty();
         for (var i = buttons.length - 1; i >= 0; i--) {
            var $button = buttons[i];
            $button.addClass('ui-corner-all ui-state-default').appendTo($buttonPane);
         }
      },
      
      /**
       * Enter the first state ("show problems") of the publish dialog
       * @param {Object} items
       */
      goToPublishProblemsState: function(items) {
         var self = this;
         var $dlg = this.getDialog();
         $dlg.empty();
         var $tableBox = $('<div class="cms-publish-items"></div>');
         var $table = makeTableWithHeaders(['', 'Title', 'Path', 'Date', 'Reason', 'Status', 'Remove']);
         var even = true;
         for (var i = 0; i < items.length; i++) {
            var item = items[i];
            $table.append(createPublishItem(item, even ? 'cms-even' : 'cms-odd'));
            even = !even;
         }
         $('<p></p>').css('margin-bottom', '40px').text('The following resources can\'t be published and will be removed from your publish list:').appendTo($dlg);
         $table.appendTo($tableBox);
         $tableBox.appendTo($dlg);
         var $okButton = $('<button>Continue</button>').click(function() {
            cms.data.getPublishList(false, false, $.map(items, function(item) {
               return item.id;
            }), function(ok, data) {
               self.goToPublishListState(data.resources)
            });
         });
         
         var $cancelButton = $('<button>Cancel</button>').click(function() {
            $dlg.dialog('destroy');
         });
         
         var buttons = [$okButton, $cancelButton];
         this.setButtons(buttons);
      },
      
      /**
       * Fills the table for state 2 of the publish dialog
       * @param {Object} $table the table to fill
       * @param {Object} items the items with which the table should be filled
       */
      fillTable2: function($table, items) {
         $table.find('tr:not(:first)').remove();
         var even = true;
         for (var i = 0; i < items.length; i++) {
            var item = items[i];
            $table.append(createPublishItem2(item, even ? 'cms-even' : 'cms-odd'));
            even = !even;
         }
      },
      
     /**
      * Enter the second state ("show publish list") of the publish dialog
      * @param {Object} items the items that should be displayed in the table
      */
      goToPublishListState: function(items) {
      
         var self = this;
         var $dlg = this.getDialog();
         $dlg.empty();
         $('<p></p>').text('The following resources will be published:').css('margin-bottom', '40px').appendTo($dlg);
         var $publishButton = $('<button></button>').text('Publish').click(function() {
             
            var $checkboxes = $('.cms-publish-checkbox', $dlg);
            var resourceIds = [];
            $checkboxes.each(function() {
               var checkbox = this;
               if (checkbox.checked) {
                  resourceIds.push($(checkbox).attr('rel'));
               }
            });
            self.resourcesToPublish = resourceIds;
            var publishCallback = function(ok, data) {
               if (data.resources) {
                  self.goToLinkCheckState(data.resources);
                  //alert("broken link dialog not implemented");
                  //$dlg.dialog('destroy');
               } else {
                  $dlg.dialog('destroy');
               }
            };
            cms.data.publishResources(resourceIds, publishCallback);
         });
         
         var $cancelButton = $('<button></button>').text('Cancel').click(function() {
            $dlg.dialog('destroy');
            alert('hello world');
         });
         
         this.setButtons([$publishButton, $cancelButton]);
         
         var $table = makeTableWithHeaders(['Publish', '', 'Title', 'Path', 'Date', 'Remove']);
         self.fillTable2($table, items);
         var $tableBox = $('<div class="cms-publish-items"></div>');
         $tableBox.append($table);
         $dlg.append($tableBox);
         
         
         var publishRelatedElems = createCheckboxLine("Publish related resources");
         var publishSiblingsElems = createCheckboxLine("Publish siblings");
         $dlg.append(publishRelatedElems.line).append(publishSiblingsElems.line);
         var updateTable = function() {
            var publishRelated = publishRelatedElems.checkbox.get(0).checked;
            var publishSiblings = publishSiblingsElems.checkbox.get(0).checked;
            cms.data.getPublishList(publishRelated, publishSiblings, null, function(ok, data) {
               self.fillTable2($table, data.resources);
            });
         }
         publishRelatedElems.checkbox.click(updateTable);
         publishSiblingsElems.checkbox.click(updateTable);
         
         $dlg.append(createCheckboxLine("Publish siblings", function(state) {
            alert("state=" + state)
         }));
      },
      /**
       * Fills the table for state 3 of the publish dialog
       * @param {Object} $table the table to be filled
       * @param {Object} items the items with which the table should be filled
       */
      fillTable3: function($table, items) {
         $table.find('tr:not(:first)').remove();
         var even = true;
         for (var i = 0; i < items.length; i++) {
            var item = items[i];
            $table.append(createPublishItem3(item, even ? 'cms-even' : 'cms-odd'));
            even = !even;
         }
      },

      /**
       * Enter the third state ("link check") of the publish dialog
       * @param {Object} resources the resources representing the links that would be broken
       */
      goToLinkCheckState: function(resources) {
          var self = this;
          var $dlg = this.getDialog();
          $dlg.empty();
          $('<p></p>').text('The following links would be broken:').css('margin-bottom' , '30px').appendTo($dlg);
          var $tableBox = $('<div class="cms-publish-items"></div>');
          var $table = makeTableWithHeaders(['', 'Title', 'Path']);
          self.fillTable3($table, resources);
          $table.appendTo($tableBox);
          $tableBox.appendTo($dlg);
          
          var $backButton = $('<button></button>').text('Back').click(function() {
            cms.data.getPublishList(false, false, null, function(ok, data) {
               self.goToPublishListState(data.resources)
            });
          });
          
          var $cancelButton = $('<button></button>').text('Cancel').click(function() {
              $dlg.dialog('destroy');
          });
          
          var $forceButton = $('<button></button>').text('Publish').click(function() {
              cms.data.publishResources(self.resourcesToPublish, function(ok, data) {
                  $dlg.dialog('destroy');
              });
          });
          
          var buttons = [$backButton, $cancelButton];
          if (self.isAdmin) {
              buttons.splice(0,0,$forceButton);
          }
          self.setButtons(buttons);
      }
   }
})(cms);
