var toggleTree = function() {
	var button = $(this);
	if (button.hasClass('ui-state-active')) {
		// disabling tree mode
		$("#tree-list").jTree.destroy();
		$("#tree-list").css('display', 'none');
		button.removeClass('ui-state-active');
	} else {
		$('button.ui-state-active').trigger('click');
		// enabling tree-mode
		$('<div></div>').appendTo(document.body).addClass('ui-widget-overlay')
				.css( {
					width :$(document).width(),
					height :$(document).height(),
					zIndex :11000,
					opacity :0.8
				});
		$('<a class="cms-handle cms-move"></a>').appendTo(
				'#tree-list .cms-head');
		$('#tree-list').css('display', 'block');
		$("#tree-list")
				.jTree(
						{
							showHelper :true,
							hOpacity :0.5,
							hBg :"transparent",
							hColor :"#222",
							pBorder :"none",
							pBg :"#EEE url(images/placeholder-bg.gif) no-repeat scroll 0px 0px",
							pColor :"#222",
							pHeight :"37px",
							snapBack :1200,
							childOff :40,
							handle :"a.cms-move",
							onDrop :treeDrop,
							beforeArea :".cms-navtext",
							intoArea :".cms-file-icon",
							afterArea :".cms-title"

						});

		$("#jTreeHelper").addClass('cms-item-list');
		button.addClass('ui-state-active');
	}
};

var collapse = function() {
	$(this).closest('li').toggleClass('cms-collapsed');
};

var treeDrop = function(event) {
	var elem = event.target ? event.target : event.srcElement;
	$('ul:empty', elem).remove();
	$('li a.cms-collapse-icon').each( function() {

		if (1 != $(this).closest('li').children('ul').length) {
			this.parentNode.removeChild(this);
		}
	});

	$('a.cms-collapse-icon', elem).unbind('click', collapse);
	$('li.last', elem).removeClass('last');
	$('ul', elem).andSelf().each( function() {
		$(this).children('li').filter(':last').addClass('last');
	});

	$('li:has(ul)', elem).children(
			'div.ui-widget-content:not(:has(a.cms-collapse-icon))').append(
			'<a class="cms-collapse-icon"></a>');

	$('a.cms-collapse-icon', elem).click(collapse);

	// alert('done');

	// $('li:not(:has(ul))', elem).find('a.cms-collapse-icon').each(function(){
	// this.parentNode.removeChild(this);
	// });

};

var treeDrop_ = function() {
	var elem = 'ul.cms-item-list';
	$('li.last', elem).removeClass('last');
	$('ul', elem).each( function() {
		$(this).children('li').filter(':last').addClass('last')
	});
	var collapseIcon = $('<a class="cms-collapse-icon"></a>').click(
			function() {
				$(this).closest('li').toggleClass('cms-collapsed');
			});
	$('li:has(ul:has(li))', elem).children(
			'div.ui-widget-content:not(:has(a.cms-collapse-icon))').append(
			collapseIcon);

};
