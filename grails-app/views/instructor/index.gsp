<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <script type="text/javascript">
        function setFileName()
        {
            var theFile = document.getElementById('myFile');
            var fileTitleText = document.getElementById('fileTitle');
            fileTitleText.value = theFile.value.split(/[\/\\]/).pop().split('.')[0];

            var filename = $('#myFile').val().split('\\').pop();
            $('#filename').val(filename);
            $('#filename').attr('placeholder', filename);
            $('#filename').focus();
        }
        function checkExistingFile()
        {
            var fileTitleText = document.getElementById('fileTitle');
            <g:each in="${courseFiles}" var="courseFile">
                var courseFileName = "${courseFile.displayName}";
                if(fileTitleText.value == courseFileName){
                    return confirm('Overwrite existing file?');
                }
            </g:each>
            return true;
        }
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
            <div class="form-group">
                <g:if test="${status == 'error'}">
                    <g:if test="${badUsers && badUsers.size() > 0}">
                        <div class="alert alert-danger alert-dismissable" role="alert">
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
                        <div class="alert alert-danger alert-dismissable" role="alert">
                            <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
                            <strong><g:message code="${description}"/></strong><br>
                        </div>
                    </g:else>
                </g:if>
                <g:elseif test="${status == 'success'}">
                    <div class="alert alert-success alert-dismissable" role="alert">
                        <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
                        <strong><g:message code="${description}"/></strong><br>
                    </div>
                </g:elseif>
                <h2>Add/Update Feedback</h2>
                <a href="${createLink(controller: 'instructor', action: 'downloadFile', params: [courseId: params.courseId, userId: params.userId])}">Download your course template</a><br>
                <g:uploadForm controller="instructor" action="upload" onsubmit="return checkExistingFile()">
                    <g:hiddenField name="courseId" value="${params.courseId}"/>
                    <g:hiddenField name="userId" value="${params.userId}"/>
            </div>
            <div class="form-group">
                <label for="myFile">Feedback File (CSV)</label><br/>
                <button type="button" class="btn btn-secondary" onclick="document.getElementById('myFile').click(); return false;" aria-describedby="fileHelp" >Choose File</button>
                <input type="file" class="form-control-file" name="myFile" id="myFile" onchange="setFileName()" style="display: none;"/>
                <label for="filename" class="hide">
                    Uploaded File
                </label>
                <input type="text" id="filename" autocomplete="off" readonly placeholder="No File Uploaded"><br/>
                <small id="fileHelp" class="form-text text-muted">File with extension *.csv based on course template. File Size Limit = 10 MB</small>
            </div>
            <div class="form-group">
                <label for="fileTitle">Title</label><br>
                <g:textField id="fileTitle" name="fileTitle"/>
            </div>
            <div class="form-group">
                <label class="form-check-label">
                    <g:checkBox name="releaseCheckbox" value="${false}" />
                    Release feedback to participants?
                </label>
            </div>
            <div class="form-group">
                <button type="submit" class="btn btn-primary">Upload</button>
            </div>
        </g:uploadForm>
    <table class="table table-striped table-bordered table-responsive" id="feedbackTable" data-toggle="table" data-pagination="true" data-pagination-v-align="bottom" data-smart-display="true" data-page-size="10" data-page-list="[5, 10, 20, 50, 100, All]">
        <thead>
        <tr>
            <th scope="col" data-sortable="true">Title</th>
            <th scope="col" data-sortable="true">Modified By</th>
            <th scope="col" data-sortable="true">Last Modified</th>
            <th scope="col" data-sortable="true">Released</th>
            <th scope="col" data-sortable="true">Actions</th>
        </tr></thead>
        <tbody>
        <g:each in="${courseFiles}" var="courseFile">
            <tr>
                <td scope="row">${courseFile.displayName}</td>
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
                <td>
                    <div class="dropup">
                      <button class="btn btn-primary dropdown-toggle" type="button" id="dropdownMenu1" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                        Action
                        <span class="caret"></span>
                      </button>
                      <ul class="dropdown-menu" aria-labelledby="dropdownMenu1">
                        <li><a href="${createLink(controller: 'instructor', action: 'viewFile', params: [fileId: courseFile.fileId, fileURL: courseFile.url, displayName: courseFile.displayName, released: !courseFile.hidden, courseId: params.courseId, userId: params.userId])}">View</a></li>
                        <li><a href="${createLink(controller: 'instructor', action: 'studentView', params: [fileId: courseFile.fileId, fileURL: courseFile.url, displayName: courseFile.displayName, released: !courseFile.hidden, courseId: params.courseId, user: params.user, userId: params.userId])}">Student View</a></li>
                        <li><a href="${createLink(controller: 'instructor', action: 'editFile', params: [fileId: courseFile.fileId, editType: 'rename', displayName: courseFile.displayName, courseId: params.courseId, userId: params.userId])}">Edit Title</a></li>
                        <li><a href="${createLink(controller: 'instructor', action: 'editFile', params: [fileId: courseFile.fileId, editType: 'add', displayName: courseFile.displayName, courseId: params.courseId, userId: params.userId])}">Upload New Version</a></li>
                        <li><a href="${createLink(controller: 'instructor', action: 'downloadCSV', params: [fileId: courseFile.fileId, fileURL: courseFile.url, displayName: courseFile.displayName, courseId: params.courseId, userId: params.userId])}">Download</a></li>
                        <g:if test="${courseFile.hidden == true}">
                            <li><a href="${createLink(controller: 'instructor', action: 'unrelease', params: [fileId: courseFile.fileId, courseId: params.courseId, userId: params.userId])}">Unrelease</a></li>
                        </g:if>
                        <g:else>
                            <li><a href="${createLink(controller: 'instructor', action: 'release', params: [fileId: courseFile.fileId, courseId: params.courseId, userId: params.userId])}">Release</a></li>
                        </g:else>
                        <li role="separator" class="divider"></li>
                        <li><a href="#fileModal_${courseFile.fileId}" data-toggle="modal" data-target="#fileModal_${courseFile.fileId}">Delete</a></li>
                      </ul>
                    </div>

                    <div id="fileModal_${courseFile.fileId}" class="modal fade" tabindex="-1" role="dialog">
                        <div class="modal-dialog" role="document">
                            <div class="modal-content">
                                <div class="modal-header">
                                    <h5 class="modal-title">Confirm Delete ${courseFile.displayName}</h5>
                                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                                        <span aria-hidden="true">&times;</span>
                                    </button>
                                </div>
                                <div class="modal-body">
                                    <p class="mb-0" role="text">Are you sure you want to Delete ${courseFile.displayName}?</p>
                                </div>
                                <div class="modal-footer">
                                    <button type="button" class="btn btn-secondary" data-dismiss="modal" id="confirmDialog_confirm_${courseFile.fileId}_${params.courseId}_${params.userId}">Yes</button>
                                    <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>
</body>
</html>