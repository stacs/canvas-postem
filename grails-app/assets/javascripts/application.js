// This is a manifest file that'll be compiled into application.js.
//
// Any JavaScript file within this directory can be referenced here using a relative path.
//
// You're free to add application-wide JavaScript to this file, but it's generally better
// to create separate JavaScript files as needed.
//
//= require jquery-2.2.0.min
//= require bootstrap
//= require bootstrap-table
//= require_tree .
//= require_self

if (typeof jQuery !== 'undefined') {
    (function($) {
        $(document).ajaxStart(function() {
            $('#spinner').fadeIn();
        }).ajaxStop(function() {
            $('#spinner').fadeOut();
        });
    })(jQuery);
}

$("[id^='confirmDialog_confirm_']").click(function(){
    var modal = $(this);
    var params = modal.attr("id").split('_');
    var fileId = params[2];
    var courseId = params[3];
    var userId = params[4];

    $.post("/lts-postem/instructor/delete", {"fileId" : fileId, "courseId" : courseId,"userId" : userId}, function(result){});
    $('#fileModal_'+ fileId).modal('hide');
    window.open('index?courseId=' + courseId, '_self');

});


