<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
</head>

<body>
<div class="table_wrapper" tabIndex="1">
<table class="table table-bordered" id="studentViewFileTable" data-toggle="table"  data-smart-display="true" tabIndex="1">
    <thead style="background-color: #f5f5f5;">
    <tr>
        <g:each in="${headers}" var="header">
            <th scope="col" tabIndex="1">${header}</th>
        </g:each>
    </tr>
    </thead>
    <tbody>
    <g:each in="${contents}" var="content">
        <tr>
             <g:each status="i"  in="${content}" var="contentRow">
                <g:if test="${i == 0}">
                    <th scope="row" tabIndex="1">${contentRow}</th>
                </g:if>
                <g:else>
                    <td tabIndex="1">${contentRow}</td>
                </g:else>
             </g:each>
        </tr>
    </g:each>
    </tbody>
</table>
</div>
<a href="${createLink(action: 'index', params: [courseId: courseId, user: user ])}" class="btn btn-custom-dark" style="text-decoration:none" role="button">Back</a>

</body>
</html>