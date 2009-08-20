(function($) {

  /**
   * @author Remy Sharp
   * @url http://remysharp.com/2007/01/25/jquery-tutorial-text-box-hints/
   */
  $.fn.hint = function (blurClass) {
    if (!blurClass) { 
      blurClass = 'blur';
    }
    return this.each(function () {
      // get jQuery version of 'this'
      var $input = $(this);
      // capture the rest of the variable to allow for reuse
      var title = $input.attr('title'),
          $form = $(this.form),
          $win = $(window);

      function remove() {
        if ($input.val() === title && $input.hasClass(blurClass)) {
          $input.val('').removeClass(blurClass).css('color', '');
        }
      }

      // only apply logic if the element has the attribute
      if (title) { 
        // on blur, set value to title attr if text is blank
        $input.blur(function () {
          if (this.value === '') {
            $input.val(title).addClass(blurClass).css('color', '#999999');
          }
        }).focus(remove).blur(); // now change all inputs to title
      
        // clear the pre-defined text when form is submitted
        $form.submit(remove);
        $win.unload(remove); // handles Firefox's autocomplete
      }
    });
  };
  
  /* @see org.opencms.workplace.list.CmsListMultiSearchAction#barHtml(CmsWorkplace) */
  $(document).ready(function() {
  
  try {
     // hide search bar
     var $sbar = $("#" + LIST_SEARCH_DATA.SEARCH_BAR_INPUT_ID);

     // decorate new input boxes
     var form = document.forms[LIST_SEARCH_DATA.FORM]; 
     var colSel = "input[name^=listColFilter"; // missing end bracket
     var $searchButton = $sbar.siblings("span.link").eq(0);
     var submitFn = $searchButton.attr("onclick");
     $searchButton.attr("onclick", "");
     var com = function() {
       var data = "";
       var cols = LIST_SEARCH_DATA.COLUMNS;
       var n = cols.length;
       while (n--) {
         var $ctl = $(colSel + cols[n] + "]");
         if ($ctl.val() === $ctl.attr("title") && $ctl.hasClass("blur")) {
           $ctl.val('');
         } else {
           data += cols[n];
           data += "#";
           data += $ctl.val();
           if (n > 0) {
             data += "|";
           }
         }
       }
       $sbar.val(data);
       return true;
     };
     $searchButton.click(function() {
       com();
       submitFn();
     });     
     $(colSel + "]").hint().keypress(function (e) {  
       if ((e.which && e.which == 13) || (e.keyCode && e.keyCode == 13)) {  
         com();
         submitFn();
         return false;  
       } else {  
         return true;  
       }  
     });
     
     
  } catch(e) {
    alert(e);
  }
  }); 
  
})(window.jQuery);

