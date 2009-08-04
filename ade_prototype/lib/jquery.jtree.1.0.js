//$.fn.navTree = function(options){
//	var self=this;
//	var opts=$.extend({}, $.fn.navTree.defaults, options);
//	
//};
//$.fn.navTree.defaults={};

/* Copyright (c) 2008 Kean Loong Tan http://www.gimiti.com/kltan
 * Licensed under the MIT (http://www.opensource.org/licenses/mit-license.php)
 * Copyright notice and license must remain intact for legal use
 * jTree 1.0
 * Version: 1.0 (May 5, 2008)
 * Requires: jQuery 1.2+
 */
(function($) {

	$.fn.jTree = function(options) {
		$("body").append('<ul id="jTreeHelper"></ul>');
		var opts = $.extend({}, $.fn.jTree.defaults, options);
		var cur = 0, curOff = 0, off =0, h =0, w=0, hover = 0;
		var str='<li class="jTreePlacement" style="background:'+opts.pBg+';border:'+opts.pBorder+';color:'+opts.pColor+';height:'+opts.pHeight+'"></li>';
		var mover=$('<li class="jTreePlacement" style="background:'+opts.pBg+';border:'+opts.pBorder+';color:'+opts.pColor+';height:'+opts.pHeight+'"></li>');
		var placeholder=$('<li class="jTreePlaceholder" style="background:'+opts.pBg+';border:'+opts.pBorder+';color:'+opts.pColor+';height:'+opts.pHeight+'"></li>');
		var container = this;
		var changed=false;
		//events are written here
		if ($.isFunction(opts.onDrop)) $(container).bind('treeDrop', opts.onDrop);
		
		$(this).find(opts.handle).mousedown(function(e){
			if ($("#jTreeHelper").is(":not(:animated)") && e.button !=2) {
				$("body").css("cursor","move");
				// append jTreePlacement to body and hides
				//$("body").append(str);
				mover=$('<li class="jTreePlacement" style="background:'+opts.pBg+';border:'+opts.pBorder+';color:'+opts.pColor+';height:'+opts.pHeight+'"></li>');
				mover.appendTo('body').hide();
				//$(".jTreePlacement").hide();
				placeholder=$('<li class="jTreePlaceholder" style="background:'+opts.pBg+';border:'+opts.pBorder+';color:'+opts.pColor+';height:'+opts.pHeight+'"></li>');
				
				
				if (opts.handle!="li"){
					cur=$(this).closest("li")[0];
				}else{
					cur = this;
				}
				
				//get the current li and append to helper
				$(cur).clone().appendTo("#jTreeHelper");
				// get initial state, cur and offset
				
				curOff = $(cur).offset();
				$(cur).hide();
				$(cur).before(placeholder);
				placeholder.css('background-color', 'green');
				// show initial helper
				$("#jTreeHelper").css ({
					position: "absolute",
					top: e.pageY + opts.offsetHelper.top,
					left: e.pageX + opts.offsetHelper.left,
					background: opts.hBg,
					opacity: opts.hOpacity
				}).hide();
				
				if(opts.showHelper)
					$("#jTreeHelper").show();
				
				$("#jTreeHelper *").css ({
					color: opts.hColor,
					background: opts.hBg
				});
				// start binding events to use
				// prevent text selection
				$(document).bind("selectstart", doNothing);
				
				// doubleclick is destructive, better disable
				//$(container).find(opts.handle).bind("dblclick", doNothing);
				
				// in single li calculate the offset, width height of hovered block
				//$(container).find("li").bind("mouseover", getInitial);
				
				$(container).find('li '+ opts.beforeArea).mouseover(enterBefore).css("cursor","url('green-up.cur')");
				$(container).find('li '+ opts.afterArea).mouseover(enterAfter).css('cursor','url(images/16x16/nav_down_green.ico)');
				$(container).find('li '+ opts.intoArea).mouseover(enterInto).css('cursor','url(images/16x16/nav_redo_green.ico)');
				// in single li put placement in correct places, also move the helper around
				$(container).bind("mousemove", sibOrChild);
				
				// in container put placement in correct places, also move the helper around
				$(container).find("li").bind("mousemove", enterPlacement);
				
				// handle mouse movement outside our container
				$(document).bind("mousemove", helperPosition);
			}
			//prevent bubbling of mousedown
			return false;
		});
		
		// in single li or in container, snap into placement if present then destroy placement
		// and helper then show snapped in object/li
		// also destroys events
		$(this).find("li").andSelf().mouseup(function(e){
			if(cur!=0){
				// if placementBox is detected
				$("body").css("cursor","default");
				if ($(".jTreePlacement").is(":visible")) {
					$(cur).insertBefore(".jTreePlacement").show();
				}
				//$(cur).show();
				cur.style.display='';
				$(".jTreePlacement, .jTreePlaceholder").remove();	
				// remove helper and placement box and clean all empty ul
				$(container).find("ul:empty").remove();
				
				$("#jTreeHelper").empty().hide();
					
				
				// remove bindings
				destroyBindings();
				if ($.isFunction(opts.onDrop)) $(container).trigger('treeDrop');
			}
			return false;
		});
		
//		$(document).mouseup(function(e){
//			$("body").css("cursor","default");
//			if ($("#jTreeHelper").is(":not(:empty)")) {
//				$("#jTreeHelper").animate({
//					top: curOff.top,
//					left: curOff.left
//						}, opts.snapBack, function(){
//							$("#jTreeHelper").empty().hide();
//							$(".jTreePlacement").remove();
//							$(cur).show();
//						}
//				);
//				
//				destroyBindings();
//			}
//			return false;
//		});
		//functions are written here
		var doNothing = function(){
			return false;
		};
		
		var destroyBindings = function(){
			$(document).unbind("selectstart", doNothing);
			$(container).find(opts.handle).unbind("dblclick", doNothing);
			//$(container).find("li").unbind("mouseover", getInitial);
			$(container).find('li '+ opts.beforeArea).unbind("mouseover", enterBefore)[0].style.cursor='';
			$(container).find('li '+ opts.afterArea).unbind("mouseover", enterAfter)[0].style.cursor='';
			$(container).find('li '+ opts.intoArea).unbind("mouseover", enterInto)[0].style.cursor='';
			$(container).find("li").unbind("mousemove", enterPlacement);
			$(document).unbind("mousemove", helperPosition);
			$(container).unbind("mousemove", sibOrChild);
			return false;
		};
		
		var helperPosition = function(e) {
			$("#jTreeHelper").css ({
				top: e.pageY + opts.offsetHelper.top,
				left: e.pageX + opts.offsetHelper.left
			});
			
//			$(".jTreePlacement").remove();
			
			return false;
		};
		
		var getInitial = function(e){
			off = $(this).offset();
			h = $(this).outerHeight();
			w = $(this).outerWidth();
			hover = this;
			return false;
		};
		
		var sibOrChild = function(e){
			$("#jTreeHelper").css ({
				top: e.pageY + opts.offsetHelper.top,
				left: e.pageX + opts.offsetHelper.left
			});
			return false;
		};
		
		var putPlacement = function(e){
			$(cur).hide();
			$("#jTreeHelper").css ({
				top: e.pageY + opts.offsetHelper.top,
				left: e.pageX + opts.offsetHelper.left
			});
			
			
	
			//inserting before
			if ( e.pageY >= off.top && e.pageY < (off.top + h/2 - 1) ) {
				if (!$(this).prev().hasClass("jTreePlacement")) {
					$(".jTreePlacement").remove();
					$(this).before(str);
				}
			}
			//inserting after
			else if (e.pageY >(off.top + h/2) &&  e.pageY <= (off.top + h) ) {
				// as a sibling
				if (e.pageX > off.left && e.pageX < off.left + opts.childOff) {
					if (!$(this).next().hasClass("jTreePlacement")) {
						$(".jTreePlacement").remove();
						$(this).after(str);
					}
				}
				// as a child
				else if (e.pageX > off.left + opts.childOff) {
					$(".jTreePlacement").remove();
					if ($(this).find("ul").length == 0)
						$(this).append('<ul>'+str+'</ul>');
					else
						$(this).find("ul").prepend(str);
				}
			}
			
			if($(".jTreePlacement").length>1)
				$(".jTreePlacement:first-child").remove();
			return false;
		}
		
		var lockIn = function(e) {
			// if placement box is present, insert before placement box
			if ($(".jTreePlacement").length==1) {
				$(cur).insertBefore(".jTreePlacement");
			}
			$(cur).show();
			
			// remove helper and placement box
			$("#jTreeHelper").empty().hide();
			
			$(".jTreePlacement").remove();
			return false;
		}
		
		var enterBefore = function(e){
			hover=$(this).closest('li')[0];
			enter=0;
			changed=true;
		}
		
		var enterInto = function(e){
			hover=$(this).closest('li')[0];
			enter=1;
			changed=true;
		}
		
		var enterAfter = function(e){
			hover=$(this).closest('li')[0];
			enter=2;
			changed=true;
		}
		
		var enterPlacement = function(e){
			$(cur).hide();
			$("#jTreeHelper").css ({
				top: e.pageY + opts.offsetHelper.top,
				left: e.pageX + opts.offsetHelper.left
			});
			if (changed){
				changed=false;
				//inserting before
				if ( enter==0 ) {
					if (!$(this).prev().hasClass("jTreePlacement") ) {
//						$(".jTreePlacement").remove();
//						$(this).before(str);
						
						if ($(this).prev()[0]==cur){
							placeholder.hide();
						}else{
							placeholder.show();
						}
						mover.insertBefore(this).show();
						
					}
				}
				//inserting after
				else if (enter==2) {
						if (!$(this).next().hasClass("jTreePlacement")) {
//							$(".jTreePlacement").remove();
//							$(this).after(str);
							if ($(this).next().hasClass("jTreePlaceholder")){
								placeholder.hide();
							}else{
								placeholder.show();
							}
							mover.insertAfter(this).show();
						}
				}
				// as a child
				else if (enter==1) {
						//$(".jTreePlacement").remove();
						if ($(this).children("ul").length == 0){
//							$(this).append('<ul>'+str+'</ul>');
							mover.appendTo($('<ul></ul>').appendTo(this)).show();
							placeholder.show();
						}else{
//							$(this).children("ul").prepend(str);
							if (!$(this).children("ul").children("li:first-child").hasClass("jTreePlacement")){
								if ( $(this).children("ul").children("li:first-child").hasClass("jTreePlaceholder")){
									placeholder.hide();
								}else{
									placeholder.show();
								}
								
								mover.prependTo($(this).children("ul")).show();
							}
							
						}
				}
				
//				if($(".jTreePlacement").length>1)
//					$(".jTreePlacement:first-child").remove();
//				return false;
			}
		}
		
		var destroy=function(){
			var elem=$(this);
			elem.find(opts.handle).unbind('mousedown');
			elem.find("li").andSelf().unbind('mouseup');
		}

	}; // end jTree


	$.fn.jTree.defaults = {
		showHelper: true,
		hOpacity: 0.5,
		hBg: "#FCC",
		hColor: "#222",
		pBorder: "1px dashed #CCC",
		pBg: "#EEE",
		pColor: "#222",
		pHeight: "20px",
		childOff: 20,
		snapBack: 1000,
		handle: "li",
		offsetHelper: {top: -0, left: -310}

	};
		  
})(jQuery);


