<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
     <script type="text/javascript">
            function setFileNameNewVersion()
            {
                var theFileNewVersion = document.getElementById('myFileNewVersion');
                var filenameNewVersion = $('#myFileNewVersion').val().split('\\').pop();
                $('#filenameNewVersion').val(filenameNewVersion);
                $('#filenameNewVersion').attr('placeholder', filenameNewVersion);
                $('#filenameNewVersion').focus();
            }
            function checkEmptyFile()
            {
                if($('#myFile').val().length > 0){

                    return true;
                }
                else{

                    let html1 = '<div class="alert alert-danger alert-dismissable" role="alert" aria-live="assertive">'
                    html1 += '<a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a><strong>'
                    html1 += 'No file uploaded. Please upload a valid CSV file';
                    html1 += '</strong><br></div>';
                    $("#alerts_div2").html(html1);

                    return false;
                }

            }
     </script>
</head>

<body>
    <div class="form-group" id="alerts_div2">
        <g:if test="${editType == 'rename'}">
            <g:form action="renameFile">
                <g:hiddenField name="courseId" value="${params.courseId}"/>
                <g:hiddenField name="userId" value="${params.userId}"/>
                <br>
                <label style="display: inline-block; float: left; clear: left; width: 300px; text-align: right">Current Title: <g:field type="text" name="currentName" disabled="true" value="${displayName}"/></label><br>
                <label style="display: inline-block; float: left; clear: left; width: 300px; text-align: right">New Title:     <g:textField name="fileName"/></label><br><br>
                <g:hiddenField name="fileId" value="${fileId}" />
                <button type="submit" class="btn btn-custom-dark" style="margin-left: 150px">Update</button>
            </g:form>
        </g:if>
        <g:elseif test="${editType == 'add'}">
            <div class="alert alert-warning alert-dismissable" aria-live="assertive">
                <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
                <strong>Warning: </strong>This file will overwrite and replace the existing file. Previous versions will not be saved.<br>
            </div>
            <g:if test="${status == 'error'}">
                <g:if test="${badUsers && badUsers.size() > 0}">
                    <div class="alert alert-danger alert-dismissable" aria-live="assertive">
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
                    <div class="alert alert-danger alert-dismissable" aria-live="assertive">
                        <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
                        <strong><g:message code="${description}"/></strong><br>
                    </div>
                </g:else>
            </g:if>
            <label>Add/Update Feedback</label><br>
            <a href="${createLink(controller: 'instructor', action: 'downloadFile', params: [courseId: params.courseId, userId: params.userId])}">Download your course template</a><br>
            <g:uploadForm controller="instructor" action="uploadNewVersion" onsubmit="return checkEmptyFile()">
                <g:hiddenField name="courseId" value="${params.courseId}"/>
                <g:hiddenField name="userId" value="${params.userId}"/>
                <g:hiddenField name="user" value="${params.user}"/>
                </div>
                <div class="form-group">
                    <label for="myFileNewVersion" id="choose2">Feedback File (CSV)</label><br/>
                    <button id="uploadButton2" type="button" class="btn btn-custom" onclick="document.getElementById('myFileNewVersion').click(); return false;" aria-labelledby="choose2 uploadButton2" >Choose File</button>
                    <input type="file" class="form-control-file" name="myFileNewVersion" id="myFileNewVersion" onchange="setFileNameNewVersion()" style="display: none;"/>
                    <label for="filenameNewVersion" class="hide">
                        Uploaded File
                    </label>
                    <input type="text" id="filenameNewVersion" autocomplete="off" readonly placeholder="No File Uploaded" aria-labelledby="fileHelpNewVersion filenameNewVersion"><br/>
                    <small id="fileHelpNewVersion" class="form-text text-muted">File with extension *.csv based on course template. File Size Limit = 10 MB.</small>
                </div>
                <g:hiddenField name="fileTitle" value="${displayName}" />
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
        </g:elseif>
        <br>
        <a href="${createLink(action: 'index',params: [courseId: params.courseId, userId: params.userId])}" class="btn btn-custom-dark" style="text-decoration:none" role="button">Back</a>
    </div>
</body>
</html>