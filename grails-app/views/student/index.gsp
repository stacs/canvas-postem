<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <script type="text/javascript">
        $(document).ready(function(){
             var table = $('#studentTable').DataTable({
                     paging: false,
                     searching: false,
                     fixedColumns:   {
                         left: 1
                     }
             });
        });
    </script>
</head>

<body>
<div class="table_wrapper">
<table class="table table-bordered" id="studentTable" data-toggle="table" >
        <thead style="background-color: #f5f5f5;">
    <tr>
        <th>Title</th>
        <th>Last Modified</th>
        <th>Actions</th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${courseFiles}" var="courseFile">
        <tr>
            <td>${courseFile.displayName}</td>
            <td>${courseFile.updatedAt}</td>
            <td>
                <a href="${createLink(action: 'viewFile', params: [fileURL: courseFile.url, fileId: courseFile.fileId, courseId: params.courseId, user: params.user ])}" style="text-decoration:none" role="button" class="btn btn-custom">View</a>
            </td>
        </tr>
    </g:each>
    </tbody>
</table>
</div>
</body>
</html>