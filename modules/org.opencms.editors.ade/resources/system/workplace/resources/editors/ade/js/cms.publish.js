(function(cms) {

   var M = cms.messages;
   var STATE_CHANGED = 'C';
   var STATE_DELETED = 'D';
   var STATE_NEW = 'N';
   WAIT_GIF_URL = cms.data.SKIN_URI + 'commons/wait.gif';
   var classPublishItem = 'cms-publish-item';
   var classPublishCheckbox = 'cms-publish-checkbox';
   var classRemoveButton = 'cms-publish-remove-button';
   var classToRemove = 'cms-publish-toremove';
   var classKeep = 'cms-publish-keep';
   var classPublishDialog = 'cms-publish-dialog';
   var buttonHeight = 24;
   
   /**
    * Returns the icon for a resource
    * @param {Object} item the resource item
    */
   var _getItemIcon = function(item) {
      var resourceIcon = item.icon;
      return $('<img  class="cms-publish-icon"></img>').attr('src', resourceIcon);
   }
   
   var problemIconClasses = {
      locked: 'cms-icon-locked',
      published: 'cms-icon-published',
      permissions: 'cms-icon-permission',
      brokenlink: 'cms-icon-brokenlink',
      related: 'cms-icon-relation'
   }
   
   var problemClasses = {
      locked: 'cms-publish-problem-locked',
      published: 'cms-publish-problem-published',
      permissions: 'cms-publish-problem-permission'
   }
   /**
    * Returns a DOM object that signalizes a problem status for publishing (wrapped in a jQuery object)
    * @param {Object} problem the problem keyword
    * @param {Object} text the title text
    */
   var _getProblemIndicator = function(problem, text) {
      var $indicator = $('<span class="cms-publish-icon"></span>');
      if (text) {
         $indicator.attr('title', text);
      }
      if (problemIconClasses[problem]) {
         $indicator.addClass(problemIconClasses[problem]);
      }
      return $indicator;
   }
   
   
   /**
    * Retrieves the CSS classes that should be
    * @param {Object} item
    */
   var _getCssClassesForPublishItem = function(resource) {
      var classes = [];
      if (resource.state == 'C') {
         classes.push('cms-fstate-changed');
      }
      if (resource.state == 'D') {
         classes.push('cms-fstate-deleted');
      }
      if (resource.state == 'N') {
         classes.push('cms-fstate-new');
      }
      return classes.join(' ');
   }
   
   
   /**
    * Creates an entry for the publish list based on a resource
    * @param {Object} resource the resource for which the entry should be created
    */
   var _formatPublishItem = function(resource) {
    
      var $item = $('<div></div>').addClass(classPublishItem + ' cms-item ui-corner-all ui-widget-content');//.css(itemStyle);
      var $row1 = $('<div></div>').appendTo($item);
      var $row2 = $('<div></div>').appendTo($item);
      $('<div></div>').addClass('cms-publish-item-clear').css('clear', 'both').appendTo($item);
      _getItemIcon(resource).appendTo($row1);
      var title = '(no title)';
      if (resource.title && resource.title.length > 0) {
         title = resource.title;
      }
      $('<div class="cms-publish-title"></div>').text(title).appendTo($row1);
      _getProblemIndicator(resource.infotype, resource.info).appendTo($row2);
      $('<div class="cms-publish-path"></div>').text(resource.uri).appendTo($row2);
      if (problemClasses[resource.infotype]) {
         $item.addClass(problemClasses[resource.infotype]);
      }
      $item.attr('rel', resource.id);
      return $item;
      
   }
   /**
    * Checks all publish checkboxes inside a DOM element
    * @param {Object} $parent
    */
   var _checkAllCheckboxes = function($parent) {
      $('.' + classPublishCheckbox, $parent).each(function() {
         this.checked = !this.disabled;
      });
   }
   
   /**
    * Unchecks all publish checkboxes inside a DOM element
    * @param {Object} $parent
    */
   var _uncheckAllCheckboxes = function($parent) {
      $('.' + classPublishCheckbox, $parent).each(function() {
         this.checked = false;
      })
   }
   
   /**
    * Collects the rel attributes (which represent resource ids) from the DOM elements in a jQuery object
    * @param {Object} $items the jQuery object
    */
   var _collectIds = function($items) {
      var result = [];
      $items.each(function() {
         result.push($(this).attr('rel'));
      });
      return result;
   }
   
   var PublishDialog = cms.publish.PublishDialog = function(project) {
      this.project = project ? project : '';
   }
   
   PublishDialog.prototype = {
      setData: function(data) {
            },
      goToLinkCheckState: function(data) {
            },
      
      /**
       * Starts loading the publish list.
       *
       * The publish list dialog will be opened asynchronously when the publish list is loaded.
       */
      start: function() {
         $('button[name=publish]').addClass('cms-deactivated');
         var self = this;
         self.goToWaitState();
         cms.data.getPublishOptions(function(ok, data) {
            self.checkedRelated = !!data.related;
            self.checkedSiblings = !!data.siblings;
            self.project = data.project ? data.project : '';
            if (ok) {
               cms.data.getPublishList(self.checkedRelated, self.checkedSiblings, self.project, function(ok, data) {
               
                  if (ok) {
                     self.goToMainState(data.groups);
                  }
               });
            }
         });
      },
      
      /**
       * Returns the dialog, and creates it if it doesn't already exist
       */
      getDialog: function() {
         var self = this;
         if (!this.$dialog) {
            if ($('#' + classPublishDialog).size() == 0) {
               $('<div id="' + classPublishDialog + '"></div>').appendTo('body');
            }
            var $dlg = $('#' + classPublishDialog);
            this.$dialog = $dlg;
            $dlg.dialog({
               autoOpen: true,
               title: 'Publish',
               modal: true,
               zIndex: 9999,
               width: 750,
               close: function() {
                  self.destroy();
               },
               position: 'top',
               resizable: true
            });
         }
         return this.$dialog;
      },
      
      destroy: function() {
         this.$dialog.dialog('destroy');
         $('button[name=publish]').removeClass('cms-deactivated');
      },
      
      /**
       * Changes the publish list parameters and loads the new publish list for the changed parameters.
       * @param {Object} related flag indicating whether related resources should be included
       * @param {Object} siblings flag indicating whether siblings should be included
       * @param {Object} project the project UUID for which the publish list should be retrieved, or the empty string
       * for the list of the current user's changed resources
       */
      updateData: function(related, siblings, project) {
         var self = this;
         self.goToWaitState();
         cms.data.getPublishList(related, siblings, project, function(ok, data) {
            if (ok) {
               self.goToMainState(data.groups);
            }
         })
         self.checkedRelated = related;
         self.checkedSiblings = siblings;
         self.project = project;
      },
      
      
      /**
       * Enters the main (publish list) state of the dialog.
       * @param {Object} data the data to use for the publish dialog
       */
      goToMainState: function(data) {
      
         var self = this;
         var $dlg = this.getDialog();
         
         $dlg.empty();
         if (data.length == 0) {
            var $projectSelector = self.createProjectSelector().css('float', 'right');
            $('<div></div>').text('There are no resources to publish.').append($projectSelector).appendTo($dlg);
            $('<button></button>').addClass('ui-state-default ui-corner-all').css('clear', 'both').css('margin-top', '75px').height(buttonHeight).css('float', 'right').width(75).text('OK').click(function() {
               self.destroy();
            }).appendTo($dlg);
            
            return;
         }
         
         this.$topPanel = $('<div></div>').appendTo($dlg);
         this.$topPanel.css('margin-bottom', '30px');
         $('<span></span>').text('Select: ').appendTo(self.$topPanel);
         var $selectAll = $('<button>All</button>').addClass('ui-state-default ui-corner-all').width(80).height(buttonHeight).appendTo(self.$topPanel).click(function() {
            _checkAllCheckboxes(self.$mainPanel);
         });
         var $selectNone = $('<button>None</button>').addClass('ui-state-default ui-corner-all').width(80).height(buttonHeight).appendTo(self.$topPanel).click(function() {
            _uncheckAllCheckboxes(self.$mainPanel);
         })
         var $projectSelector = self.createProjectSelector().css('float', 'right').appendTo(self.$topPanel);
         $('<span></span>').text('Publish list: ').css('float', 'right').appendTo(self.$topPanel);
         this.$mainPanel = $('<div></div>').addClass('cms-publish-main').css({
            'overflow': 'auto',
            'max-height': '500px',
            'margin-bottom': '100px',
            'position': 'relative'
         }).appendTo($dlg);
         
         this.$checkboxes = $('<div></div>').appendTo($dlg);
         var $relatedCheckbox = $('<input type="checkbox"></input>');
         $relatedCheckbox.get(0).checked = !!(self.checkedRelated);
         var $siblingCheckbox = $('<input type="checkbox"></input>');
         $siblingCheckbox.get(0).checked = !!(self.checkedSiblings);
         var _updateState = function() {
            self.saveState();
            self.updateData($relatedCheckbox.get(0).checked, $siblingCheckbox.get(0).checked);
         };
         $relatedCheckbox.click(_updateState);
         $siblingCheckbox.click(_updateState);
         $('<div></div>').text('Publish related resources').prepend($relatedCheckbox).appendTo(self.$checkboxes);
         $('<div></div>').text('Publish siblings').prepend($siblingCheckbox).appendTo(self.$checkboxes);
         
         this.$bottomPanel = $('<div></div>').appendTo($dlg).css({
            'margin-top': '20px',
            'margin-left': '20px'
         });
         var $cancel = $('<button></button>').text('Cancel').addClass('ui-corner-all ui-state-default').width(80).height(buttonHeight).css('float', 'right').appendTo(self.$bottomPanel).click(function() {
            self.destroy();
         });
         var $publish = $('<button></button>').text('Publish').addClass('ui-corner-all ui-state-default').width(80).height(buttonHeight).css('float', 'right').appendTo(self.$bottomPanel).click(function() {
            self.startPublish();
         });
         
         
         for (var i = 0; i < data.length; i++) {
            this.addGroup(data[i]);
         }
         self.restoreState();
      },
      
      
      /**
       * Adds a group of resources to publish to the publish list in the dialog.
       * @param {Object} group an object with a name property (string) and a resources property (array of resources)
       */
      addGroup: function(group) {
         var $main = this.$mainPanel;
         $('<p></p>').addClass('cms-publish-group-header').text(group.name).appendTo($main);
         
         for (var i = 0; i < group.resources.length; i++) {
            this.addResource(group.resources[i], false, $main);
         }
      },
      
      /**
       * Adds a resource to the publish list in the dialog.
       * @param {Object} resource the resource to be added
       * @param {Boolean} isRelated a flag that indicates whether the resource is merely a related resource of publish list resources
       * @param {Object} $parent the parent DOM object to which the resource entry should be appended
       */
      addResource: function(resource, isRelated, $parent) {
         var self = this;
         if (!$parent) {
            $parent = this.$mainPanel;
         }
         var $row = $('<div></div>').addClass('cms-publish-row').appendTo($parent);
         $row.addClass(classKeep);
         $row.attr('rel', resource.id);
         $row.addClass(_getCssClassesForPublishItem(resource));
         if (problemClasses[resource.infotype]) {
            $row.addClass(problemClasses[resource.infotype]);
         }
         
         var checkboxStyle = {
            'display': 'block',
            'clear': 'both',
            'margin-left': '8px',
            'margin-top': '8px',
            'float': 'left'
         };
         
         var $checkbox = $('<input class="' + classPublishCheckbox + '" type="checkbox"></input>').css(checkboxStyle);
         if (resource.info) {
            $('<span></span>').css({float: 'left', width: '30px'}).appendTo($row);
         } else {
            $('<span></span>').css({float: 'left', width: '30px'}).append($checkbox).appendTo($row);
         }
         $checkbox.attr('rel', resource.id);
         var $publishItem = _formatPublishItem(resource);
         if (isRelated) {
            $publishItem.css('margin-left', '60px');
         }
         var itemVerticalOffset = 1;
         $publishItem.appendTo($row);
         var $removeButton = $('<button></button>').addClass(classRemoveButton).addClass('ui-corner-all ui-state-default').text('Remove').attr('rel', resource.id);
//         if ($.browser.msie) {
//            $removeButton.css('margin-top', '-30px');
//         }
         
         var removeButtonState = 0;
         $removeButton.click(function() {
            if (removeButtonState == 0) {
               $row.addClass(classToRemove);
               $row.removeClass(classKeep);
               $checkbox.get(0).disabled = true;
               $checkbox.get(0).checked = false;
               
               $removeButton.text('Unremove');
            } else {
               $row.removeClass(classToRemove);
               $row.addClass(classKeep);
               if (!resource.info) {
                  $checkbox.get(0).disabled = false;
               }
               $removeButton.text('Remove');
            }
            removeButtonState = 1 - removeButtonState;
         });
    //     $row.height($publishItem.height() + 2 * itemVerticalOffset);
         
         $removeButton.height(25);
         if (resource.removable) {
       //     $removeButton.css('float', 'right');
            $('.cms-publish-item-clear', $publishItem).before($removeButton);
            
         }
         $publishItem.hover(function() {
            $removeButton.show();
         }, function() {
            $removeButton.hide();
         })
         if (resource.related) {
            var related = resource.related;
            for (var i = 0; i < resource.related.length; i++) {
               var subResource = related[i];
               self.addResource(subResource, true);
            }
         }
      },
      
      /**
       * Starts publishing the checked items from the publish list.
       * @param {Object} force
       */
      startPublish: function(force) {
         var self = this;
         self.goToWaitState();
         cms.data.publishResources(self.getResourcesToPublish(), self.getResourcesToRemove(), force, function(ok, data) {
            if (!ok) {
               return;
            }
            if (data.resources) {
               self.goToLinkCheckState(data);
            } else {
               self.destroy();
            }
         });
      },
      
      /**
       * Creates the project selector for the publish dialog.
       */
      createProjectSelector: function() {
         var self = this;
         var projects = cms.data.projects;
         var userListId = '';
         var userListLabel = 'My changes';
         var values=[];
         values.push({value: userListId, title: userListLabel});
         for (var i = 0; i < projects.length; i++) {
             values.push({value: projects[i].id, title: projects[i].name});
         }
         var $select=$.fn.selectBox('generate',{'values': values, select: function($this, replacer, value) {
            self.updateData(self.checkedRelated, self.checkedSiblings, value);
         }});
         $select.selectBox('setValue',self.project);
         
   /*      var $select = $('<select></select>').attr();
         $('<option></option>').attr('value', userListId).text(userListLabel).appendTo($select);
         for (var i = 0; i < projects.length; i++) {
            var projectName = projects[i].name;
            var projectId = projects[i].id;
            $('<option></option>').attr('value', projectId).text(projectName).appendTo($select);
         }
         $('option[value=' + self.project + ']', $select).attr('selected', 'selected');
         
         $select.change(function() {
            var value = $(this).val();
            self.updateData(self.checkedRelated, self.checkedSiblings, value);
         });
         
         */
         return $select;
      },
      
      /**
       * Sets the publish checkbox of the publish list item with a given rel attribute to unchecked.
       * @param {Object} key the rel attribute value of the checkbox
       */
      uncheckByRel: function(key) {
         var self = this;
         $('.' + classPublishCheckbox + '[rel=' + key + ']', self.$mainPanel).each(function() {
            if (this.checked) {
               $(this).trigger('click');
            }
         });
      },
      
      /**
       * Sets the status of a publish item with a given rel attribute to 'remove'.
       * @param {Object} key the rel attribute value of the publish list item
       */
      removeByRel: function(key) {
         var self = this;
         $('.' + classRemoveButton + '[rel=' + key + ']').trigger('click');
      },
      
      
      /**
       * Saves the state of the publish checkboxes and the 'remove' statuses from the publish dialog.
       */
      saveState: function() {
         var self = this;
         var unchecked = _collectIds($('.' + classPublishCheckbox + ':not(:checked)', self.$mainPanel));
         var toRemove = _collectIds($('.' + classPublishItem + '.' + classToRemove, self.$mainPanel));
         self.selectState = {
            unchecked: unchecked,
            toRemove: toRemove
         };
      },
      
      
      /**
       * Sets the state of the publish checkboxes and 'remove' statuses of the dialog to the state they were in
       * when the saveState method was called.
       */
      restoreState: function() {
         var self = this;
         _checkAllCheckboxes(self.$mainPanel);
         
         if (self.selectState) {
            var unchecked = self.selectState.unchecked;
            var toRemove = self.selectState.toRemove;
            for (var i = 0; i < unchecked.length; i++) {
               self.uncheckByRel(unchecked[i]);
            }
            for (var j = 0; j < toRemove.length; j++) {
               self.removeByRel(toRemove[j]);
            }
         }
      },
      
      /**
       * Gets the ids for resources that should be published from the publish dialog.
       */
      getResourcesToPublish: function() {
         var $main = this.$mainPanel;
         var $checkedCheckboxes = $('.' + classPublishCheckbox + ':checked', this.$mainPanel);
         var resourcesToPublish = [];
         $checkedCheckboxes.each(function() {
            resourcesToPublish.push($(this).attr('rel'));
         });
         return resourcesToPublish;
      },
      
      /**
       * Gets the resource ids for resources that should be removed from the publish list from the publish dialog.
       */
      getResourcesToRemove: function() {
         var $main = this.$mainPanel;
         var $toRemove = $('.' + classToRemove + ' ,  .' + problemClasses.published, $main);
         var resourcesToRemove = [];
         $toRemove.each(function() {
            resourcesToRemove.push($(this).attr('rel'));
         });
         return resourcesToRemove;
      },
      
      /**
       * Changes the publish dialog to the wait state.
       */
      goToWaitState: function() {
         var $dialog = this.getDialog();
         var self = this;
         $dialog.empty();
         $('<img></img>').attr('src', WAIT_GIF_URL).appendTo($dialog);
      },
      
      /**
       * Enters the link check state of
       * @param {Object} data
       */
      goToLinkCheckState: function(data) {
         var resources = data.resources;
         var self = this;
         var $dlg = this.getDialog();
         $dlg.empty();
         $('<div></div>').text('The following links will be broken:').appendTo($dlg);
         var $linkCheckPanel = self.$linkCheckPanel = $('<div></div>').appendTo($dlg).css('margin-top', '40px').css('padding-bottom', '110px');
         
         var $linkCheckButtons = $('<div></div>').css('clear', 'both').appendTo($dlg);
         var $backButton = $('<button></button>').text('Back').addClass('ui-state-default ui-corner-all').css('float', 'right').width(80).appendTo($linkCheckButtons).height(buttonHeight);
         var $cancelButton = $('<button></button>').text('Cancel').addClass('ui-state-default ui-corner-all').css('float', 'right').width(80).appendTo($linkCheckButtons).height(buttonHeight);
         var $forceButton = $('<button></button>').text('Publish').addClass('ui-state-default ui-corner-all').css('float', 'right').width(80).height(buttonHeight);
         if (data.canPublish) {
            $forceButton.appendTo($linkCheckButtons);
         }
         $backButton.click(function() {
            self.updateData(self.checkedRelated, self.checkedSiblings, self.project);
         });
         $cancelButton.click(function() {
            self.destroy();
         });
         $forceButton.click(function() {
            self.startPublish(true);
         });
         var _appendItem = function(resource, isRelated) {
            var $row = $('<div></div>').appendTo($linkCheckPanel);
            var $item = _formatPublishItem(resource).css('clear', 'both').appendTo($row);
            $item.css('margin-top', '2px').css('margin-bottom', '2px');
            if (isRelated) {
               $item.css('margin-left', '80px');
            }
            $linkCheckPanel.append($row);
         }
         
         for (var i = 0; i < resources.length; i++) {
            var res = resources[i];
            _appendItem(res, false);
            for (var j = 0; j < res.related.length; j++) {
               var related = res.related[j];
               _appendItem(related, true);
            }
         }
      }
   }
})(cms);
