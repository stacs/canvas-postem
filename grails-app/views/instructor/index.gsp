<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <script type="text/javascript">
        function setFileName()
        {
            var filename = $('#myFile').val().split('\\').pop();
            $('#fileTitle').val($('#myFile').val().split(/[\/\\]/).pop().split('.')[0]);
            $('#filename').val(filename);
            $('#filename').attr('placeholder', filename);
            $('#filename').focus();
        }
        function checkExistingFile()
        {
            if($('#myFile').val().length > 0){

                var fileTitleText = $('#fileTitle').val();
                <g:each in="${courseFiles}" var="courseFile">
                    var courseFileName = "${courseFile.displayName}";
                    if(fileTitleText == courseFileName){
                        return confirm('Overwrite existing file?');
                    }
                </g:each>

                return true;
            }
            else{

                let html1 = '<div class="alert alert-danger alert-dismissable" role="alert" aria-live="assertive">'
                html1 += '<a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a><strong>'
                html1 += 'No file uploaded. Please upload a valid CSV file';
                html1 += '</strong><br></div>';
                $("#alerts_div").html(html1);

                return false;
            }

        }

        function addAriaClasses(){

             $('.paginate_button').each(function () {
                    $(this).attr("aria-label", "Go to page " + $(this).text());
             });

             $('.page-link').each(function () {
                   let attr = $(this).attr('aria-current');
                   if (typeof attr !== 'undefined' && attr !== false) {
                      $(this).attr("aria-label", "Page " + $(this).text() + ". Current Page. " + $("#feedbackTable_info").html());
                   }

                   let attr2 = $(this).attr('aria-disabled');
                   if (typeof attr2 !== 'undefined' && attr2 !== false) {
                     $(this).attr("tabindex", "-1");
                  }
             })

             $("#feedbackTable_previous").attr("aria-label", "Go to previous page");
             $("#feedbackTable_next").attr("aria-label", "Go to next page");
        }
        $(document).ready(function(){
             var table = $('#feedbackTable').DataTable({
                searching: false,
                initComplete: function(settings, json) {
                  addAriaClasses();
                }
             });

             $('#feedbackTable').on( 'draw.dt', function () {
                addAriaClasses();
             } );

        });
    </script>
</head>

<body>
    <div class="panel panel-default">
        <div class="panel-body">
            <div>
                <h2>Description</h2>
                <div class="text-default">The Posted Feedback tool is a way for instructors to provide detailed text feedback to students by uploading a single CSV file.  Your CSV file can contain multiple columns of feedback and you can upload as many different CSV files as you wish.</div>
                <h2>Instructions</h2>
                <div class="text-default">
                <ol style="margin-left:-25px">
                <li>To begin, download your course template CSV file below.  Your template will contain all of the required columns and is pre-populated with your student roster.  Add your data/feedback/grades in the columns to the right of the required columns.  Make sure that you put the feedback for each student in the appropriate row and column.</li>
                <li>When you are ready, save and upload your completed template file below.</li>
                </ol>
                </div>
            </div>
        </div>
    </div>
            <div class="form-group" id="alerts_div">
                <g:if test="${status == 'error'}">
                    <g:if test="${badUsers && badUsers.size() > 0}">
                        <div class="alert alert-danger alert-dismissable" role="alert" aria-live="assertive">
                            <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
                            <strong>Error - Invalid Users: Please correct the below rows and try again.</strong><br>
                            <ul style="padding-left:20px">
                                <g:each in="${badUsers}" var="badUser">
                                    <li>${badUser}</li>
                                </g:each>
                            </ul>
                        </div>
                    </g:if>
                    <g:else>
                        <div class="alert alert-danger alert-dismissable" role="alert" aria-live="assertive">
                            <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
                            <strong><g:message code="${description}"/></strong><br>
                        </div>
                    </g:else>
                </g:if>
                <g:elseif test="${status == 'success'}">
                    <div class="alert alert-success alert-dismissable" role="alert" aria-live="assertive">
                        <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
                        <strong><g:message code="${description}"/></strong><br>
                    </div>
                </g:elseif>
                <h2>Add/Update Feedback</h2>
                <a href="${createLink(controller: 'instructor', action: 'downloadFile', params: [courseId: params.courseId, userId: params.userId])}">Download your course template</a><br>
            </div>
        <g:uploadForm controller="instructor" action="upload" onsubmit="return checkExistingFile()">
            <g:hiddenField name="courseId" value="${params.courseId}"/>
            <g:hiddenField name="userId" value="${params.userId}"/>
            <g:hiddenField name="user" value="${params.user}"/>
            <div class="form-group">
                <label for="myFile" id="choose">Feedback File (CSV)</label><br/>
                <button id="uploadButton" type="button" class="btn btn-custom" onclick="document.getElementById('myFile').click(); return false;" aria-labelledby="choose uploadButton">Choose File</button>
                <input type="file" class="form-control-file" name="myFile" id="myFile" onchange="setFileName()" style="display: none;"/>
                <label for="filename" class="hide">
                    Uploaded File
                </label>
                <input type="text" id="filename" autocomplete="off" readonly placeholder="No File Uploaded" aria-labelledby="fileHelp filename"><br/>
                <small id="fileHelp" class="form-text text-muted">File with extension *.csv based on course template. File Size Limit = 10 MB.</small>
            </div>
            <div class="form-group">
                <label for="fileTitle">Title</label><br>
                <g:textField id="fileTitle" name="fileTitle"/>
            </div>
            <div class="form-group">
                <label>
                    <g:checkBox name="releaseCheckbox" value="${false}" />
                    Release feedback to participants?
                </label>
            </div>
            <div class="form-group">
                <button type="submit" class="btn btn-custom-dark">Upload</button>
            </div>
        </g:uploadForm>
        <div class="table_wrapper">
        <table class="table table-bordered" id="feedbackTable" data-toggle="table" data-pagination="true" data-pagination-v-align="bottom" data-smart-display="true" data-page-size="10" data-sort-name="title" data-sort-order="asc">
        <thead style="background-color: #f5f5f5;">
        <tr>
            <th scope="col" data-sortable="true" data-field="title">Title</th>
            <th scope="col" data-sortable="true" data-field="modified by">Modified By</th>
            <th scope="col" data-sortable="true" data-field="last modified">Last Modified</th>
            <th scope="col" data-sortable="true" data-field="released">Released</th>
            <th scope="col" data-sortable="true" data-field="actions">Actions</th>
        </tr></thead>
        <tbody>
        <g:each status="i" in="${courseFiles}" var="courseFile">
            <tr>
                <th scope="row">${courseFile.displayName}</th>
                <td>${courseFile.modifiedBy}</td>
                <td>${courseFile.updatedAt}</td>
                <td>
                    <g:if test="${courseFile.hidden == true}">
                    Yes
                    </g:if>
                    <g:else>
                    No
                    </g:else>
                </td>
                <td style="width:200px">
                    <div class="dropdown">
                      <button class="btn btn-custom dropdown-toggle" type="button" id="dropdownMenu${i}" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                        Actions
                        <span class="caret"></span>
                      </button>
                      <ul class="dropdown-menu" aria-labelledby="dropdownMenu${i}">
                        <li><a href="${createLink(controller: 'instructor', action: 'viewFile', params: [fileId: courseFile.fileId, fileURL: courseFile.url, displayName: courseFile.displayName, released: !courseFile.hidden, courseId: params.courseId, userId: params.userId])}">View</a></li>
                        <li><a href="${createLink(controller: 'instructor', action: 'studentView', params: [fileId: courseFile.fileId, fileURL: courseFile.url, displayName: courseFile.displayName, released: !courseFile.hidden, courseId: params.courseId, user: params.user, userId: params.userId])}">Student View</a></li>
                        <li><a href="${createLink(controller: 'instructor', action: 'editFile', params: [fileId: courseFile.fileId, editType: 'rename', displayName: courseFile.displayName, courseId: params.courseId, userId: params.userId])}">Edit Title</a></li>
                        <li><a href="${createLink(controller: 'instructor', action: 'editFile', params: [fileId: courseFile.fileId, editType: 'add', displayName: courseFile.displayName, courseId: params.courseId, userId: params.userId])}">Upload New Version</a></li>
                        <li><a href="${createLink(controller: 'instructor', action: 'downloadCSV', params: [fileId: courseFile.fileId, fileURL: courseFile.url, displayName: courseFile.displayName, courseId: params.courseId, userId: params.userId])}">Download</a></li>
                        <g:if test="${courseFile.hidden == true}">
                            <li><a href="${createLink(controller: 'instructor', action: 'unrelease', params: [fileId: courseFile.fileId, displayName: courseFile.displayName, courseId: params.courseId, userId: params.userId])}">Unrelease</a></li>
                        </g:if>
                        <g:else>
                            <li><a href="${createLink(controller: 'instructor', action: 'release', params: [fileId: courseFile.fileId, displayName: courseFile.displayName, courseId: params.courseId, userId: params.userId])}">Release</a></li>
                        </g:else>
                        <li role="separator" class="divider"></li>
                        <li><a href="#fileModal_${courseFile.fileId}" data-toggle="modal" data-target="#fileModal_${courseFile.fileId}">Delete</a></li>
                      </ul>
                    </div>
                    <div style='display:none' id="fileModalMessage_${courseFile.fileId}"> Are you sure you want to Delete ${courseFile.displayName}? Yes </div>
                    <div id="fileModal_${courseFile.fileId}" class="modal fade" tabindex="-1" role="dialog">
                        <div class="modal-dialog modal-lg" role="document">
                            <div class="modal-content">
                                <div class="modal-header">
                                    <h5 class="modal-title">Confirm Delete File</h5>
                                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                                        <span aria-hidden="true">&times;</span>
                                    </button>
                                </div>
                                <div class="modal-body" role="text">
                                  Are you sure you want to Delete ${courseFile.displayName}?
                                </div>
                                <div class="modal-footer">
                                    <a href="${createLink(action: 'delete', params: [fileId: courseFile.fileId, courseId: params.courseId, userId: params.userId ])}" style="text-decoration:none" role="button" class="btn btn-custom-dark" aria-labelledby="fileModalMessage_${courseFile.fileId} ">Yes</a>
                                    <button type="button" class="btn btn-custom-dark" data-dismiss="modal">Close</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>
    </div>
</body>
</html>