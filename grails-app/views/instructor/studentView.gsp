<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <script type="text/javascript">
                function changeList(fileURL) {
                       var choice = document.getElementById('participantselect').value;
                       if(choice == 'null'){
                           $("#studentInfo").html("No Student is selected");
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
                                         for (i = 0; i < result.length; i++) {
                                             if(i == 0){
                                                 html += "<strong>Student ID : </strong>" + result[0] + "</br>";
                                                 html += "<strong>Student Name : </strong>" + result[1] + "," + result[2] + "</br>";
                                                 i +=2;
                                             }
                                             else {
                                                 html += "<strong>" + headers[i] + ": </strong>" + result[i] + "</br>";
                                             }
                                         }
                                         $("#studentInfo").html(html);
                                    }
                            });
                       }
                }
    </script>
</head>

<body>
    <div class="panel panel-default">
        <div class="panel-heading"><label>View Student in ${displayName}</label></div>
    </div>
    <div class="form-group">
        <label for="participantselect">Select a Student</label>
        <g:select id="participantselect" name="participantchoice"
          from="${logins}" optionKey="key" optionValue="value"
          noSelection="${['null': 'Please Choose...']}"
          onchange="JavaScript:changeList('${fileURL}')" />

    </div>
    <div id="output" style="padding-left:10px" aria-label="student information" tabindex="0" aria-describedby="studentInfo"><span id="studentInfo">
    </span></div>
    <br><br>
    <a href="${createLink(action: 'index', params: [courseId: courseId, userId: userId  ])}" class="btn btn-custom" style="text-decoration:none" role="button">Back</a>
</body>
</html>