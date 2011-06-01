function pageselectCallback(page_id, jq) {
	loadListPage(page_id + 1);
	return false;
}
   
function initPagination() {
	// Create pagination element
	$("#pagination").pagination(itemCount, {
		num_edge_entries: 1,
		num_display_entries: 10,
		prev_text: fmtPaginationPrev,
		next_text: fmtPaginationNext,
		items_per_page: itemsPerPage,
		callback: pageselectCallback
	   	});

}

function loadListPage(page) {
	lastPage = currentPage;
	currentPage = page;
	if (lastPage != currentPage) {
		if ($('#list_center_page_' + page).length == 0 ) {
			$.post(listCenterPath, { pageUri: pageUri, __locale: itemLocale, pageIndex: currentPage, listConfig: listConfig, itemsPerPage: itemsPerPage, imgPos: imgPos, imgWidth: imgWidth}, function(data){ loadListPage2(data); });
		} else {
			switchPage();
		}
	}
}

function loadListPage2(data) {
	var singlePage = '<div id="list_center_page_' + currentPage + '" style="display: none;"></div>';
	$('#list_center_pages').append(singlePage);
	var divNode = document.getElementById('list_center_page_' + currentPage);
	divNode.innerHTML = data;
	switchPage();
}

function afterFadeOut() {
	$('#list_center_page_' + lastPage).hide();
	$('#list_center_page_' + currentPage).fadeIn('fast');
}

function switchPage() {
	$('#list_center_page_' + lastPage).fadeOut('fast', afterFadeOut);
}