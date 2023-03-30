<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
</head>

<body>
<table class="table table-striped table-bordered" id="studentViewTable">
    <tr>
        <th>Title</th>
        <th>Last Modified</th>
        <th></th>
    </tr>
    <g:each in="${courseFiles}" var="courseFile">
        <tr>
            <td>${courseFile.displayName}</td>
            <td>${courseFile.updatedAt}</td>
            <td>
              <a href="${createLink(action: 'viewFile', params: [fileURL: courseFile.url, fileId: courseFile.fileId, courseId: params.courseId, user: params.user ])}">View</a>
            </td>
        </tr>
    </g:each>
</table>
</body>
</html>