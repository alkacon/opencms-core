<%@ page import="org.opencms.workplace.galleries.*" %><%

A_CmsAjaxGallery wp = new CmsAjaxImageGallery(pageContext, request, response);

%>
var LANG = {

	"DETAIL_SIZE"			: "<%= wp.key(Messages.GUI_GALLERY_ITEMDETAIL_SIZE_0) %>",
	"DETAIL_EDIT_HELP"		: "<%= wp.key(Messages.GUI_GALLERY_ITEMDETAIL_EDIT_HELP_0) %>",
	"DETAIL_EDIT_URL_HELP"		: "<%= wp.key(Messages.GUI_GALLERY_ITEMDETAIL_EDIT_URL_HELP_0) %>",
	"DETAIL_DM"			: "<%= wp.key(Messages.GUI_GALLERY_ITEMDETAIL_DATE_MODIFIED_0) %>",
	"IMGDETAIL_STATE_NEW"		: "<%= wp.key(Messages.GUI_IMAGEGALLERY_IMGDETAIL_STATE_NEW_0) %>",
	"IMGDETAIL_STATE_CHANGED"	: "<%= wp.key(Messages.GUI_IMAGEGALLERY_IMGDETAIL_STATE_CHANGED_0) %>",

	"IMGPREVIEW_SIZE_LOCK"		: "<%= wp.key(Messages.GUI_IMAGEGALLERY_IMGPREVIEW_SIZE_LOCK_0) %>",
	"IMGPREVIEW_SIZE_UNLOCK"	: "<%= wp.key(Messages.GUI_IMAGEGALLERY_IMGPREVIEW_SIZE_UNLOCK_0) %>",

	"IMGITEM_STATE_NEW"		: "<%= wp.key(Messages.GUI_IMAGEGALLERY_IMGITEM_STATE_NEW_0) %>",
	"IMGITEM_STATE_CHANGED"		: "<%= wp.key(Messages.GUI_IMAGEGALLERY_IMGITEM_STATE_CHANGED_0) %>",

	"IMGITEM_LOCKSTATE_LOCKED"	: "<%= wp.key(Messages.GUI_IMAGEGALLERY_IMGITEM_LOCKSTATE_LOCKED_0) %>",
	
	"DETAIL_STATE_NEW"		: "<%= wp.key(Messages.GUI_GALLERY_ITEMDETAIL_STATE_NEW_0) %>",
	"DETAIL_STATE_CHANGED"		: "<%= wp.key(Messages.GUI_GALLERY_ITEMDETAIL_STATE_CHANGED_0) %>",

	"ITEM_STATE_NEW"		: "<%= wp.key(Messages.GUI_GALLERY_ITEM_STATE_NEW_0) %>",
	"ITEM_STATE_CHANGED"		: "<%= wp.key(Messages.GUI_GALLERY_ITEM_STATE_CHANGED_0) %>",

	"ITEM_LOCKSTATE_LOCKED"		: "<%= wp.key(Messages.GUI_GALLERY_ITEM_LOCKSTATE_LOCKED_0) %>",
	
	"ITEM_INPUT_LINKURL"		: "<%= wp.key(Messages.GUI_INPUT_LINKTARGET_0) %>",

	"PAGINATION_NEXT"		: "<%= wp.key(Messages.GUI_GALLERY_PAGINATION_NEXT_0) %>",
	"PAGINATION_PREVIOUS"		: "<%= wp.key(Messages.GUI_GALLERY_PAGINATION_PREVIOUS_0) %>",


	"FORMAT_ORIGINAL"		: "<%= wp.key(Messages.GUI_IMAGEGALLERY_FORMAT_ORIGINAL_0) %>",
	"FORMAT_USER"			: "<%= wp.key(Messages.GUI_IMAGEGALLERY_FORMAT_USER_0) %>",
	"FORMAT_FREECROP"		: "<%= wp.key(Messages.GUI_IMAGEGALLERY_FORMAT_FREECROP_0) %>",
	"FORMAT_SMALL"			: "<%= wp.key(Messages.GUI_IMAGEGALLERY_FORMAT_SMALL_0) %>",
	"FORMAT_LARGE"			: "<%= wp.key(Messages.GUI_IMAGEGALLERY_FORMAT_LARGE_0) %>",

	"BUTTON_OK"			: "<%= wp.key(Messages.GUI_GALLERY_BUTTON_OK_0) %>",
	"BUTTON_CANCEL"			: "<%= wp.key(Messages.GUI_GALLERY_BUTTON_CANCEL_0) %>",
	"NO_SEARCH_RESULTS"		: "<%= wp.key(Messages.GUI_GALLERY_SEARCH_NORESULTS_0) %>",
	"SEARCH_RESULTS"		: "<%= wp.key(Messages.GUI_GALLERY_SEARCH_RESULTS_0) %>",
	"SEARCH_RESULT"			: "<%= wp.key(Messages.GUI_GALLERY_SEARCH_RESULT_0) %>"
};