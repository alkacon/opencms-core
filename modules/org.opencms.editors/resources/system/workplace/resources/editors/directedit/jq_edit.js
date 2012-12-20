/** 
 * Creates the direct edit buttons dynamically and places them at the correct position.
 * Additionally it activates the hover effects and actions.<p> 
 *
 * Version: 1.0
 *
 * Requires: 	jQuery 1.2.+
 * 				jQuery Dimensions 1.2
 *				jQuery FlyDOM 3.0.5
 */


/** Flag if the buttons should be shown or not. **/
var ocms_de_visible = true;

/** Contains all data for each direct edit button. **/  
var ocms_de_data = new Array();

/** For the hover effect. **/
var ocms_de_mbo=3;
var ocms_hover_border_width=1;

/** Template to create the div and the form. **/   
var t_ocms_de_template = function() {
    return [
    	'div', { className: 'ocms_de_bt', id: 'buttons_' + this.id }, [
    		'form', {name: 'form_' + this.id, id: 'form_' + this.id, method: 'post', className: 'ocms_nomargin', target: '_top', action: this.editLink},[
    			'input', { type: 'hidden', name: 'resource', NAME: 'resource', value: this.resource }, [],
    			'input', { type: 'hidden', name: 'directedit', NAME: 'directedit', value: 'true' }, [],
    			'input', { type: 'hidden', name: 'elementlanguage', NAME: 'elementlanguage', value: this.language }, [],
    			'input', { type: 'hidden', name: 'elementname', NAME: 'elementname', value: this.element }, [],
    			'input', { type: 'hidden', name: 'backlink', NAME: 'backlink', value: this.backlink }, [],
    			'input', { type: 'hidden', name: 'newlink', NAME: 'newlink', value: this.newlink }, [],
    			'input', { type: 'hidden', name: 'closelink', NAME: 'closelink', value: this.closelink }, [],
    			'input', { type: 'hidden', name: 'deletelink', NAME: 'deletelink', value: this.deletelink }, [],
    			'input', { type: 'hidden', name: 'redirect', NAME: 'redirect', value: 'true' }, [],
    			'input', { type: 'hidden', name: 'editortitle', NAME: 'editortitle', value: this.editortitle }
    		]
	    ]
    ];
};

/** Template to create each direct edit button. **/
var t_ocms_de_buttonEnabled = function() {
    return [
    	'a', { href: '#'+ this.id, className:  this.typeClass,  title: this.title, rel: this.typeClass}
    ];
};
/** Template to create a disabled direct edit button. **/
var t_ocms_de_buttonDisabled = function() {
    return [
    	'span', { className:  this.typeClass +' '+ this.typeClass+'_disabled',  title: this.title}
    ];
};

/** Template to create the hover effect for the direct edit buttons. **/
var t_ocms_de_hoverEffect = function() {
    return [
    	'div', { className:  'directedit-highlight directedit-highlight-top',  		style: 'width: '+	this.width+'px; left: '	+(this.offset.left-ocms_de_mbo-ocms_hover_border_width)+'px; top: '+			(this.offset.top-ocms_de_mbo-ocms_hover_border_width)+'px;'}, [],
    	'div', { className:  'directedit-highlight directedit-highlight-right',  	style: 'height: '+	this.height+'px; left: '+(this.offset.left+this.width-ocms_de_mbo-ocms_hover_border_width)+'px; top: '+		(this.offset.top-ocms_de_mbo)+'px;'}, [],
    	'div', { className:  'directedit-highlight directedit-highlight-bottom',  	style: 'width: '+	this.width+'px; left: '	+(this.offset.left-ocms_de_mbo-ocms_hover_border_width)+'px; top: '+			(this.offset.top+this.height-ocms_de_mbo)+'px;'}, [],
    	'div', { className:  'directedit-highlight directedit-highlight-left',  	style: 'height: '+	this.height+'px; left: '+(this.offset.left-ocms_de_mbo-ocms_hover_border_width)+'px; top: '+			(this.offset.top-ocms_de_mbo)+'px;'}, [],
    	'div', { className:  'directedit-highlight directedit-highlight-all',  		style: 'height: '+	this.height+'px; left: '+(this.offset.left-ocms_de_mbo-ocms_hover_border_width)+'px; top: '+			(this.offset.top-ocms_de_mbo)+'px; width: '+	this.width+'px;'}
    ];
};

$(document).ready(function(){
	
	/** Creates the structure with the form and divs. **/
	$("div[id^='ocms_']").each(function() {
		$(this).tplAppend(ocms_de_data[this.id], t_ocms_de_template);
		var btWidth=0;
		var t_buttons=t_ocms_de_buttonDisabled;
		if(!ocms_de_data[this.id].disabled){
			t_buttons=t_ocms_de_buttonEnabled;
		}
		// Creates the buttons
		if(ocms_de_data[this.id].hasEdit){
			btWidth= btWidth + 20;
			$('#buttons_'+ this.id).tplAppend({typeClass: 'ocms_de_edit', title: ocms_de_data[this.id].button_edit, id: this.id},t_buttons);
			$('#buttons_'+ this.id).width(btWidth);
		}
		if(ocms_de_data[this.id].hasDelete){
			btWidth= btWidth+ 20;
			$('#buttons_'+ this.id).tplAppend({typeClass: 'ocms_de_delete', title: ocms_de_data[this.id].button_delete, id: this.id},t_buttons);
			$('#buttons_'+ this.id).width(btWidth);
		}
		if(ocms_de_data[this.id].hasNew){
			$('#buttons_'+ this.id).tplAppend({typeClass: 'ocms_de_new', title: ocms_de_data[this.id].button_new, id: this.id},t_buttons);
			btWidth= btWidth+ 20;
			$('#buttons_'+ this.id).width(btWidth);
		}
		
	});
	
	/** For the first load set the buttons to the correct position. **/
	$("div[id^='ocms_']").each(func_ocms_de_reposition);
    
    /** By the resize of the window set the buttons to the correct position. **/
    $(window).resize(function(){
        $("div[id^='ocms_']").each(func_ocms_de_reposition);
    });
    
    /** If "[STRG] or [SHIFT] + [SPACE]" is pressed, then show or not the direct edit buttons. **/
    $(document).keyup(function(event){
        if ((event.keyCode == 32) && (event.ctrlKey || event.shiftKey)) {
            if(ocms_de_visible) {
               ocms_de_visible=false;
               $("div[id^='buttons_ocms_']").css('visibility', "hidden");
            } else {
                ocms_de_visible=true;
                $("div[id^='buttons_ocms_']").css('visibility', "visible");
            }
        }
    });
    
    /** This sets the effects to the direct edit buttons. **/
     $(".ocms_de_bt").hover(func_ocms_de_activate,func_ocms_de_deactivate);
     
     /** After click of the direct edit button the form is submitted. **/
     $('a.ocms_de_edit, a.ocms_de_delete, a.ocms_de_new').click(func_ocms_de_submit).mouseover(function(){
     		var atr=$(this).attr('rel');
     		if(!$(this).hasClass(atr+'_hover')) {
       			$(this).addClass(atr+'_hover');
       		}
     	}).mouseout(function(){
     		var atr=$(this).attr('rel');
     		if($(this).hasClass(atr+'_hover')) {
       			$(this).removeClass(atr+'_hover');
       		}
     	});
});


/** Function to activate the hover effects. **/
var func_ocms_de_activate = function() {
	var id = this.id;
	var type = id.substr(id.indexOf('ocms_'));
	
	// remove margin-bottom of the last element
	var last = $('#' + type + ' > :not(:last)');
	var marginBottom = parseFloat(jQuery.css(last[0], "margin-bottom", true)) || 0;
	if(!$('#'+ type).hasClass('ocms_de_selected')) {
		$('#'+ type).addClass('ocms_de_selected');
		var pointTo={ offset: $('#'+ type).calcOcmsDeOffset(), width:  ($('#'+ type).width() + 2*ocms_de_mbo), height: ($('#'+ type).height() - marginBottom + 2*ocms_de_mbo)};
		$('#'+ type).parent().tplAppend(pointTo,t_ocms_de_hoverEffect);
		$(".directedit-highlight-all").opacity(.1);
	}
};

/** Function to deactivate the hover effects. **/
var func_ocms_de_deactivate = function() {
	var id = this.id;
	var type = id.substr(id.indexOf('ocms_'));
	if($('#'+ type).hasClass('ocms_de_selected')) {
		$('#'+ type).removeClass('ocms_de_selected');
		$('div.directedit-highlight').remove();
		$('.directedit-highlight-all').remove();
	}
};

/** Function to set the position of the buttons to the correct position. **/
var func_ocms_de_reposition = function() {
	var offset = $(this).calcOcmsDeOffset();
    offset.left = offset.left + $(this).width() - $('#buttons_'+this.id).width();
    $('#buttons_'+this.id).css(offset);
    if(ocms_de_visible) {
       $('#buttons_'+this.id).css('visibility', "visible");
     } else {
       $('#buttons_'+this.id).css('visibility', "hidden");
     }
};

/** Function submit the form after clicking the direct edit button. **/
var func_ocms_de_submit = function() {
	var href = this.href;
	var id = href.substr(href.indexOf('#')+1);

	if($(this).hasClass('ocms_de_edit')){
		$("#form_"+id +"> input[name='editortitle']").val('');
		$("#form_"+id +"> input[name='newlink']").val('');
		$('#form_'+id).submit();
		return;
	} else if($(this).hasClass('ocms_de_delete')){
		$('#form_'+id).attr('action', $("#form_"+id +"> input[name='deletelink']").val());
		$('#form_'+id).submit();
		return;
	} else if($(this).hasClass('ocms_de_new')){
		$('#form_'+id).submit();
		return;
	}
	alert("Unknown form action [" + id + "/ ]");
};

/** Function to calculate the offset or position of this element. **/
$.fn.calcOcmsDeOffset = function() {		
	if($.browser.msie && !jQuery.boxModel){
		return $(this).offset();
	}
	return $(this).position();
}

/**
 * Opacity function for jQuery
 *
 * @name   .opacity
 * @cat    Plugins/Effects
 * @author Woody Gilk/woody.gilk@gmail.com
 *
 * @example $(this).opacity(.2);
 */
$.fn.opacity = function(amount) {
        if (amount > 1) amount = 1;
        if (amount < 0) amount = 0;
        if ($.browser.msie) {
                amount = (parseFloat(amount) * 100);
                this.css('filter', 'alpha(opacity='+amount+')');
        } else {
                this.css('opacity', amount);
                this.css('-moz-opacity', amount);
        }
        return this;
}
