/*
 * FCKeditor - The text editor for internet
 * Copyright (C) 2003-2005 Frederico Caldeira Knabben
 * 
 * Licensed under the terms of the GNU Lesser General Public License:
 * 		http://www.opensource.org/licenses/lgpl-license.php
 * 
 * For further information visit:
 * 		http://www.fckeditor.net/
 * 
 * "Support Open Source software. What about a donation today?"
 * 
 * File Name: vi.js
 * 	Vietnamese language file.
 * 
 * File Authors:
 * 		Phan Binh Giang (bbbgiang@yahoo.com)
 */

var FCKLang =
{
// Language direction : "ltr" (left to right) or "rtl" (right to left).
Dir					: "ltr",

ToolbarCollapse		: "Collapse Toolbar",
ToolbarExpand		: "Expand Toolbar",

// Toolbar Items and Context Menu
Save				: "Ghi",
NewPage				: "Trang Mới",
Preview				: "Xem trước",
Cut					: "Cắt",
Copy				: "Sao",
Paste				: "Dán",
PasteText			: "Dán Ký tự đơn thuần",
PasteWord			: "Dán với định dạng Word",
Print				: "In",
SelectAll			: "Chọn Tất cả",
RemoveFormat		: "Xoá Định dạng",
InsertLinkLbl		: "Liên kết",
InsertLink			: "Chèn/Sửa Liên kết",
RemoveLink			: "Xoá Liên kết",
Anchor				: "Chèn/Sửa Neo",
InsertImageLbl		: "Hình ảnh",
InsertImage			: "Chèn/Sửa Hình ảnh",
InsertFlashLbl		: "Flash",
InsertFlash			: "Chèn/Sửa Flash",
InsertTableLbl		: "Bảng",
InsertTable			: "Chèn/Sửa bảng",
InsertLineLbl		: "Đường phân cách",
InsertLine			: "Chèn đường phân cách ngang",
InsertSpecialCharLbl: "Ký tự đặt biệt",
InsertSpecialChar	: "Chèn Ký tự đặc biệt",
InsertSmileyLbl		: "Hình cảm xúc",
InsertSmiley		: "Chèn Hình cảm xúc",
About				: "Giới thiệu về FCKeditor",
Bold				: "Đậm",
Italic				: "Nghiêng",
Underline			: "Gạch chân",
StrikeThrough		: "Gạch ngang",
Subscript			: "Chỉ số dưới",
Superscript			: "Chỉ số trên",
LeftJustify			: "Canh bên Trái",
CenterJustify		: "Canh Giữa",
RightJustify		: "Canh bên Phải",
BlockJustify		: "Canh Hai bên",
DecreaseIndent		: "Dịch sang Trái",
IncreaseIndent		: "Dịch sang Phải",
Undo				: "Phục hồi Lùi",
Redo				: "Phục hồi Tiến",
NumberedListLbl		: "Số thứ tự",
NumberedList		: "Chèn/Xoá Số thứ tự",
BulletedListLbl		: "Danh sách Bulleted",
BulletedList		: "Chèn/Xoá Danh sách Bulleted",
ShowTableBorders	: "Hiện thị Đường viền bảng",
ShowDetails			: "Hiện thị Chi tiết",
Style				: "Mẫu",
FontFormat			: "Định dạng",
Font				: "Font",
FontSize			: "Cỡ Chữ",
TextColor			: "Màu Chữ",
BGColor				: "Màu Nền",
Source				: "Mã nguồn",
Find				: "Tìm",
Replace				: "Thay thế",
SpellCheck			: "Kiểm tra Chính tả",
UniversalKeyboard	: "Bàn phím quốc tế",

Form			: "Form",
Checkbox		: "Nốt Kiểm",
RadioButton		: "Nốt Đài",
TextField		: "Text Field",
Textarea		: "Textarea",
HiddenField		: "Hidden Field",
Button			: "Button",
SelectionField	: "Selection Field",
ImageButton		: "Image Button",

// Context Menu
EditLink			: "Sửa Liên kết",
InsertRow			: "Chèn Dòng",
DeleteRows			: "Xoá Dòng",
InsertColumn		: "Chèn Cột",
DeleteColumns		: "Xoá Cột",
InsertCell			: "Chèn Ô",
DeleteCells			: "Xoá Ô",
MergeCells			: "Trộn Ô",
SplitCell			: "Chia Ô",
CellProperties		: "Thuộc tính Ô",
TableProperties		: "Thuộc tính Bảng",
ImageProperties		: "Thuộc tính Hình ảnh",
FlashProperties		: "Thuộc tính Flash",

AnchorProp			: "Thuộc tính Neo",
ButtonProp			: "Thuộc tính Button",
CheckboxProp		: "Thuộc tính Nốt kiểm",
HiddenFieldProp		: "Thuộc tính Hidden Field",
RadioButtonProp		: "Thuộc tính Nốt đài",
ImageButtonProp		: "Thuộc tính Image Button",
TextFieldProp		: "Thuộc tính Text Field",
SelectionFieldProp	: "Thuộc tính Selection Field",
TextareaProp		: "Thuộc tính Textarea",
FormProp			: "Thuộc tính Form",

FontFormats			: "Normal;Formatted;Address;Heading 1;Heading 2;Heading 3;Heading 4;Heading 5;Heading 6;Paragraph (DIV)",

// Alerts and Messages
ProcessingXHTML		: "Đang xử lý XHTML. Xin hãy đợi...",
Done				: "Đã hoàn thành",
PasteWordConfirm	: "Văn bản bạn muốn dán có kèm định dạng của Word. Bạn có muốn loại bỏ định dạng Word trước khi dán?",
NotCompatiblePaste	: "Lệnh này chỉ được hỗ trợ từ trình duyệt Internet Explorer phiên bản 5.5 hoặc mới hơn. Bạn có muốn dán nguyên mẫu?",
UnknownToolbarItem	: "Không rõ Nốt \"%1\"",
UnknownCommand		: "Không rõ lệnh \"%1\"",
NotImplemented		: "Lệnh không được thi hành",
UnknownToolbarSet	: "Thanh công cụ \"%1\" không tồn tại",
NoActiveX			: "You browser's security settings could limit some features of the editor. You must enable the option \"Run ActiveX controls and plug-ins\". You may experience errors and notice missing features.",	//MISSING

// Dialogs
DlgBtnOK			: "Đồng ý",
DlgBtnCancel		: "Bỏ qua",
DlgBtnClose			: "Đóng",
DlgBtnBrowseServer	: "Duyệt trên máy chủ",
DlgAdvancedTag		: "Mở rộng",
DlgOpOther			: "&lt;Khác&gt;",
DlgInfoTab			: "Thông tin",
DlgAlertUrl			: "Hãy đưa vào một URL",

// General Dialogs Labels
DlgGenNotSet		: "&lt;không thiết lập&gt;",
DlgGenId			: "Định danh",
DlgGenLangDir		: "Đường dẫn Ngôn ngữ",
DlgGenLangDirLtr	: "Trái sang Phải (LTR)",
DlgGenLangDirRtl	: "Phải sang Trái (RTL)",
DlgGenLangCode		: "Mã Ngôn ngữ",
DlgGenAccessKey		: "Phím Truy cập",
DlgGenName			: "Tên",
DlgGenTabIndex		: "Tab Index",
DlgGenLongDescr		: "Mô tả URL",
DlgGenClass			: "Stylesheet Classes",
DlgGenTitle			: "Advisory Title",
DlgGenContType		: "Advisory Content Type",
DlgGenLinkCharset	: "Linked Resource Charset",
DlgGenStyle			: "Mẫu",

// Image Dialog
DlgImgTitle			: "Thuộc tính Hình ảnh",
DlgImgInfoTab		: "Thông tin Hình ảnh",
DlgImgBtnUpload		: "Gửi lên Máy chủ",
DlgImgURL			: "URL",
DlgImgUpload		: "Tải lên",
DlgImgAlt			: "Chú thích Hình ảnh",
DlgImgWidth			: "Rộng",
DlgImgHeight		: "Cao",
DlgImgLockRatio		: "Giữ tỷ lệ",
DlgBtnResetSize		: "Kích thước gốc",
DlgImgBorder		: "Đường viền",
DlgImgHSpace		: "HSpace",
DlgImgVSpace		: "VSpace",
DlgImgAlign			: "Vị trí",
DlgImgAlignLeft		: "Trái",
DlgImgAlignAbsBottom: "Abs Bottom",
DlgImgAlignAbsMiddle: "Abs Middle",
DlgImgAlignBaseline	: "Baseline",
DlgImgAlignBottom	: "Dưới",
DlgImgAlignMiddle	: "Giữa",
DlgImgAlignRight	: "Phải",
DlgImgAlignTextTop	: "Text Top",
DlgImgAlignTop		: "Trên",
DlgImgPreview		: "Xem trước",
DlgImgAlertUrl		: "Hãy đưa vào URL của hình ảnh",
DlgImgLinkTab		: "Liên kết",

// Flash Dialog
DlgFlashTitle		: "Thuộc tính Flash",
DlgFlashChkPlay		: "Tự động Chạy",
DlgFlashChkLoop		: "Lặp",
DlgFlashChkMenu		: "Kích hoạt Menu Flash ",
DlgFlashScale		: "Scale",
DlgFlashScaleAll	: "Hiển thị tất cả",
DlgFlashScaleNoBorder	: "Không đường viền",
DlgFlashScaleFit	: "Exact Fit",

// Link Dialog
DlgLnkWindowTitle	: "Liên kết",
DlgLnkInfoTab		: "Thông tin Liên kết",
DlgLnkTargetTab		: "Hướng tới",

DlgLnkType			: "Kiểu Liên kết",
DlgLnkTypeURL		: "URL",
DlgLnkTypeAnchor	: "Neo trong trang này",
DlgLnkTypeEMail		: "E-Mail",
DlgLnkProto			: "Giao thức",
DlgLnkProtoOther	: "&lt;khác&gt;",
DlgLnkURL			: "URL",
DlgLnkAnchorSel		: "Chọn một Neo",
DlgLnkAnchorByName	: "Theo Tên Neo",
DlgLnkAnchorById	: "Theo Định danh Element",
DlgLnkNoAnchors		: "&lt;Không có Neo nào trong tài liệu&gt;",
DlgLnkEMail			: "E-Mail",
DlgLnkEMailSubject	: "Tựa đề Thông điệp",
DlgLnkEMailBody		: "Nội dung Thông điệp",
DlgLnkUpload		: "Tải lên",
DlgLnkBtnUpload		: "Gửi lên Máy chủ",

DlgLnkTarget		: "Hướng tới",
DlgLnkTargetFrame	: "&lt;frame&gt;",
DlgLnkTargetPopup	: "&lt;cửa sổ popup&gt;",
DlgLnkTargetBlank	: "Cửa sổ mới (_blank)",
DlgLnkTargetParent	: "Cửa sổ cha (_parent)",
DlgLnkTargetSelf	: "Cùng cửa sổ (_self)",
DlgLnkTargetTop		: "Cửa sổ trên cùng(_top)",
DlgLnkTargetFrameName	: "Tên Frame hướng tới",
DlgLnkPopWinName	: "Tên Cửa sổ Popup",
DlgLnkPopWinFeat	: "Cửa sổ Popup Đặc trưng",
DlgLnkPopResize		: "Kích thước thay đổi",
DlgLnkPopLocation	: "Location Bar",	//MISSING
DlgLnkPopMenu		: "Thanh Menu",
DlgLnkPopScroll		: "Thanh cuộn",
DlgLnkPopStatus		: "Thanh trạng thái",
DlgLnkPopToolbar	: "Thanh công cụ",
DlgLnkPopFullScrn	: "Toàn màn hình (IE)",
DlgLnkPopDependent	: "Dependent (Netscape)",
DlgLnkPopWidth		: "Rộng",
DlgLnkPopHeight		: "Cao",
DlgLnkPopLeft		: "Vị trí Trái",
DlgLnkPopTop		: "Vị trí Trên",

DlnLnkMsgNoUrl		: "Hãy đưa vào Liên kết URL",
DlnLnkMsgNoEMail	: "Hãy đưa vào địa chỉ e-mail",
DlnLnkMsgNoAnchor	: "Hãy chọn một Neo",

// Color Dialog
DlgColorTitle		: "Chọn màu",
DlgColorBtnClear	: "Xoá",
DlgColorHighlight	: "Điểm sáng",
DlgColorSelected	: "Đã chọn",

// Smiley Dialog
DlgSmileyTitle		: "Chèn một hình cảm xúc",

// Special Character Dialog
DlgSpecialCharTitle	: "Chọn ký tự đặc biệt",

// Table Dialog
DlgTableTitle		: "Thuộc tính bảng",
DlgTableRows		: "Dòng",
DlgTableColumns		: "Cột",
DlgTableBorder		: "Cỡ Đường viền",
DlgTableAlign		: "Alignment",
DlgTableAlignNotSet	: "<Không thiết lập>",
DlgTableAlignLeft	: "Trái",
DlgTableAlignCenter	: "Giữa",
DlgTableAlignRight	: "Phải",
DlgTableWidth		: "Rộng",
DlgTableWidthPx		: "điểm",
DlgTableWidthPc		: "%",
DlgTableHeight		: "Cao",
DlgTableCellSpace	: "Khoảng cách Ô",
DlgTableCellPad		: "Đệm Ô",
DlgTableCaption		: "Đầu đề",

// Table Cell Dialog
DlgCellTitle		: "Thuộc tính Ô",
DlgCellWidth		: "Rộng",
DlgCellWidthPx		: "điểm",
DlgCellWidthPc		: "%",
DlgCellHeight		: "Cao",
DlgCellWordWrap		: "Dàn từ",
DlgCellWordWrapNotSet	: "&lt;Không thiết lập&gt;",
DlgCellWordWrapYes	: "Đồng ý",
DlgCellWordWrapNo	: "Không",
DlgCellHorAlign		: "Sắp xếp Ngang",
DlgCellHorAlignNotSet	: "&lt;Không thiết lập&gt;",
DlgCellHorAlignLeft	: "Trái",
DlgCellHorAlignCenter	: "Giữa",
DlgCellHorAlignRight: "Phải",
DlgCellVerAlign		: "Sắp xếp Dọc",
DlgCellVerAlignNotSet	: "&lt;Không thiết lập&gt;",
DlgCellVerAlignTop	: "Trên",
DlgCellVerAlignMiddle	: "Giữa",
DlgCellVerAlignBottom	: "Dưới",
DlgCellVerAlignBaseline	: "Baseline",
DlgCellRowSpan		: "Rows Span",
DlgCellCollSpan		: "Columns Span",
DlgCellBackColor	: "Màu nền",
DlgCellBorderColor	: "Màu viền",
DlgCellBtnSelect	: "Chọn...",

// Find Dialog
DlgFindTitle		: "Tìm",
DlgFindFindBtn		: "Tìm",
DlgFindNotFoundMsg	: "Chuỗi cần tìm không thấy.",

// Replace Dialog
DlgReplaceTitle			: "Thay thế",
DlgReplaceFindLbl		: "Tìm gì:",
DlgReplaceReplaceLbl	: "Thay bằng:",
DlgReplaceCaseChk		: "Đúng chứ HOA/thường",
DlgReplaceReplaceBtn	: "Thay thế",
DlgReplaceReplAllBtn	: "Thay thế Tất cả",
DlgReplaceWordChk		: "Đúng từ",

// Paste Operations / Dialog
PasteErrorPaste	: "An ninh trình duyệt của bạn được thiết lập không cho phép trình soạn thảo tự động thực thi lệnh dán. Hãy sử dụng bàn phím cho lệnh này (Ctrl+V).",
PasteErrorCut	: "An ninh trình duyệt của bạn được thiết lập không cho phép trình soạn thảo tự động thực thi lệnh cắt. Hãy sử dụng bàn phím cho lệnh này (Ctrl+X).",
PasteErrorCopy	: "An ninh trình duyệt của bạn được thiết lập không cho phép trình soạn thảo tự động thực thi lệnh sao chép. Hãy sử dụng bàn phím cho lệnh này (Ctrl+C).",

PasteAsText		: "Dán ký tự đơn thuần",
PasteFromWord	: "Dán với định dạng Word",

DlgPasteMsg2	: "Hãy dán vào trong khung bên dưới, sử dụng tổ hợp phím (<STRONG>Ctrl+V</STRONG>) và nhấn vào nốt <STRONG>Đồng ý</STRONG>.",
DlgPasteIgnoreFont		: "Chấp nhận các định dạng Font",
DlgPasteRemoveStyles	: "Xoá tất cả các định dạng Styles",
DlgPasteCleanBox		: "Xoá sạch",


// Color Picker
ColorAutomatic	: "Tự động",
ColorMoreColors	: "Màu khác...",

// Document Properties
DocProps		: "Thuộc tính tài liệu",

// Anchor Dialog
DlgAnchorTitle		: "Thuộc tính Neo",
DlgAnchorName		: "Tên Neo",
DlgAnchorErrorName	: "Hãy đưa vào tên Neo",

// Speller Pages Dialog
DlgSpellNotInDic		: "Không trong từ điển",
DlgSpellChangeTo		: "Change to",
DlgSpellBtnIgnore		: "Bỏ qua",
DlgSpellBtnIgnoreAll	: "Bỏ qua Tất cả",
DlgSpellBtnReplace		: "Thay thế",
DlgSpellBtnReplaceAll	: "Thay thế Tất cả",
DlgSpellBtnUndo			: "Phục hồi lại",
DlgSpellNoSuggestions	: "- Không đề xuất -",
DlgSpellProgress		: "Đang tiến hành kiểm tra chính tả...",
DlgSpellNoMispell		: "Hoàn tất kiểm tra chính tả: Không có lỗi chính tả",
DlgSpellNoChanges		: "Hoàn tất kiểm tra chính tả: Không từ nào được thay đổi",
DlgSpellOneChange		: "Hoàn tất kiểm tra chính tả: Một từ đã được thay đổi",
DlgSpellManyChanges		: "Hoàn tất kiểm tra chính tả: %1 từ đã được thay đổi",

IeSpellDownload			: "Chức năng kiểm tra chính tả chưa được cài đặt. Bạn có tải về ngay bây giờ?",

// Button Dialog
DlgButtonText	: "Text (Value)",
DlgButtonType	: "Kiểu",

// Checkbox and Radio Button Dialogs
DlgCheckboxName		: "Tên",
DlgCheckboxValue	: "Giá trị",
DlgCheckboxSelected	: "Đã chọn",

// Form Dialog
DlgFormName		: "Tên",
DlgFormAction	: "Action",
DlgFormMethod	: "Phương thức",

// Select Field Dialog
DlgSelectName		: "Tên",
DlgSelectValue		: "Giá trị",
DlgSelectSize		: "Kích cỡ",
DlgSelectLines		: "dòng",
DlgSelectChkMulti	: "Chấp nhận chọn nhiều",
DlgSelectOpAvail	: "Available Options",
DlgSelectOpText		: "Text",
DlgSelectOpValue	: "Giá trị",
DlgSelectBtnAdd		: "Thêm",
DlgSelectBtnModify	: "Thay đổi",
DlgSelectBtnUp		: "Lên",
DlgSelectBtnDown	: "Xuống",
DlgSelectBtnSetValue : "Giá trị được chọn",
DlgSelectBtnDelete	: "Xoá",

// Textarea Dialog
DlgTextareaName	: "Tên",
DlgTextareaCols	: "Cột",
DlgTextareaRows	: "Dòng",

// Text Field Dialog
DlgTextName			: "Tên",
DlgTextValue		: "Giá trị",
DlgTextCharWidth	: "Rộng",
DlgTextMaxChars		: "Số Ký tự tối đa",
DlgTextType			: "Kiểu",
DlgTextTypeText		: "Ký tự",
DlgTextTypePass		: "Mật khẩu",

// Hidden Field Dialog
DlgHiddenName	: "Tên",
DlgHiddenValue	: "Giá trị",

// Bulleted List Dialog
BulletedListProp	: "Thuộc tính Danh sách Bulleted",
NumberedListProp	: "Thuộc tính Danh sách Số",
DlgLstType			: "Kiểu",
DlgLstTypeCircle	: "Tròn",
DlgLstTypeDisc		: "Disc",	//MISSING
DlgLstTypeSquare	: "Vuông",
DlgLstTypeNumbers	: "Số (1, 2, 3)",
DlgLstTypeLCase		: "Chữ cái thường (a, b, c)",
DlgLstTypeUCase		: "Chữ cái hoa (A, B, C)",
DlgLstTypeSRoman	: "Số LaMa thường (i, ii, iii)",
DlgLstTypeLRoman	: "Số LaMa hoa (I, II, III)",

// Document Properties Dialog
DlgDocGeneralTab	: "Toàn thể",
DlgDocBackTab		: "Nền",
DlgDocColorsTab		: "Màu sắc và Biên",
DlgDocMetaTab		: "Meta Data",

DlgDocPageTitle		: "Tiêu đề Trang",
DlgDocLangDir		: "Đường dẫn Ngôn Ngữ",
DlgDocLangDirLTR	: "Trái sang Phải (LTR)",
DlgDocLangDirRTL	: "Phải sang Trái (RTL)",
DlgDocLangCode		: "Mã Ngôn ngữ",
DlgDocCharSet		: "Character Set Encoding",
DlgDocCharSetOther	: "Other Character Set Encoding",

DlgDocDocType		: "Kiểu Đề mục Tài liệu",
DlgDocDocTypeOther	: "Kiểu Đề mục Tài liệu khác",
DlgDocIncXHTML		: "Bao gồm cả định nghĩa XHTML",
DlgDocBgColor		: "Màu nền",
DlgDocBgImage		: "Background Image URL",
DlgDocBgNoScroll	: "Không cuộn nền",
DlgDocCText			: "Text",
DlgDocCLink			: "Liên kết",
DlgDocCVisited		: "Liên kết Đã viếng thăm",
DlgDocCActive		: "Liên kết Hoạt động",
DlgDocMargins		: "Biên của Trang",
DlgDocMaTop			: "Trên",
DlgDocMaLeft		: "Trái",
DlgDocMaRight		: "Phải",
DlgDocMaBottom		: "Dưới",
DlgDocMeIndex		: "Document Indexing Keywords (comma separated)",
DlgDocMeDescr		: "Mô tả tài liệu",
DlgDocMeAuthor		: "Tác giả",
DlgDocMeCopy		: "Bản quyền",
DlgDocPreview		: "Xem trước",

// Templates Dialog
Templates			: "Mẫu dựng sẵn",
DlgTemplatesTitle	: "Nội dung Mẫu dựng sẵn",
DlgTemplatesSelMsg	: "Please select the template to open in the editor<br>(the actual contents will be lost):",
DlgTemplatesLoading	: "Đang nạp Danh sách Mẫu dựng sẵn. Xin hãy chờ...",
DlgTemplatesNoTpl	: "(Không có Mẫu dựng sẵn nào được định nghĩa)",

// About Dialog
DlgAboutAboutTab	: "Giới thiệu",
DlgAboutBrowserInfoTab	: "Thông tin trình duyệt",
DlgAboutVersion		: "phiên bản",
DlgAboutLicense		: "Licensed under the terms of the GNU Lesser General Public License",
DlgAboutInfo		: "Thông tin thêm hãy đến"
}