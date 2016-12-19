$(document).ready(function(){
    $('#search').on('click', function(){
      var search = {
        'className': $('#className').val(),
        'methodName':$('#methodName').val()
      };
      console.log(search);
      $.ajax({
            type: 'POST',
            data: search,
            url: '/search',
            dataType: 'JSON'
        }).done(function( response ) {
            var n = response.length;
            var txt = "Results: " + n;
            $("#txtResult").html(txt);
            $("#resultList").html("");
            for(var i in response){
              var elm = response[i];
              var $c = $("<pre><code></code></pre>");
              $c.html(JSON.stringify(elm, null, 2));
              $("#resultList").append($c);
            }
        });
    });
});
