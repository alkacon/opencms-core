$.extend($.ui.sortable.prototype, {
   _uiHash: function(inst) {
      var self = inst || this;
      return {
         helper: self.helper,
         placeholder: self.placeholder || $([]),
         position: self.position,
         absolutePosition: self.positionAbs, //deprecated
         offset: self.positionAbs,
         item: self.currentItem,
         sender: inst ? inst.element : null,
         self: self
      };
   },
   _mouseStop: function(event, noPropagation) {
   
      if (!event) 
         return;
      
      //If we are using droppables, inform the manager about the drop
      if ($.ui.ddmanager && !this.options.dropBehaviour) 
         $.ui.ddmanager.drop(this, event);
      
      if (this.options.revert) {
         var self = this;
         var cur;
         if (self.placeholder.css('display') == 'none') {
            cur = self.cmsStartOffset;
         } else {
            cur = self.placeholder.offset();
         }
         
         self.reverting = true;
         
         $(this.helper).animate({
            left: cur.left - this.offset.parent.left - self.margins.left + (this.offsetParent[0] == document.body ? 0 : this.offsetParent[0].scrollLeft),
            top: cur.top - this.offset.parent.top - self.margins.top + (this.offsetParent[0] == document.body ? 0 : this.offsetParent[0].scrollTop)
         }, parseInt(this.options.revert, 10) || 500, function() {
            self._clear(event);
         });
      } else {
         this._clear(event, noPropagation);
      }
      
      return false;
      
   }
})
