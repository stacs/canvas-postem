<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <script type="text/javascript">
                function changeList(fileURL) {
                       var choice = document.getElementById('participantselect').value;
                       if(choice == 'null'){
                          $("#headContent").html("");
                          $("#bodyContent").html("");
                       }
                       else{
                            $.ajax({
                                    method: "POST",
                                    dataType: 'json',
                                    url: "/lts-postem/instructor/studentFileInfo",
                                    data: {
                                         "fileURL" : fileURL,
                                         "user" : choice
                                     },
                                    success: function(response){
                                          let html = "";
                                          let i = 0;
                                          let headers = response["headers"];
                                          let result = response[choice];
                                          html = "";
                                          for (i = 0; i < headers.length; i++) {
                                            html += "<th scope='col' tabIndex='1'>" + headers[i] + "</th>";

                                          }
                                          $("#headContent").html(html);
                                          html = "";
                                          for (i = 0; i < result.length; i++) {
                                                if(i == 0){
                                                    html += "<th scope='row' tabIndex='1'>" + result[i] + "</th>";
                                                }
                                                else{
                                                    html += "<td tabIndex='1'>" + result[i] + "</td>";
                                                }

                                          }
                                          $("#bodyContent").html(html);
                                    }
                            });
                       }
                }
    </script>
</head>

<body>
    <div class="panel panel-default">
        <div class="panel-heading"><strong>View Student in ${displayName}</strong></div>
    </div>
    <div class="form-group">
        <label for="participantselect">Select a Student</label>
        <g:select id="participantselect" name="participantchoice"
          from="${logins}" optionKey="key" optionValue="value"
          noSelection="${['null': 'Please Choose...']}"
          onchange="JavaScript:changeList('${fileURL}')" />

    </div>
    <div class="table_wrapper" tabIndex="1">
    <table class="table table-bordered" id="studentTableInstructorView" data-toggle="table"  data-smart-display="true" tabIndex="1">
    <thead style='background-color: #f5f5f5;'><tr id="headContent">

    </tr></thead><tbody><tr id="bodyContent">


    </tr></tbody></table>
    </span></div>
    <br><br>
    <a href="${createLink(action: 'index', params: [courseId: courseId, userId: userId  ])}" class="btn btn-custom-dark" style="text-decoration:none" role="button">Back</a>
</body>
</html>