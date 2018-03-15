var base_url="/faurecia/svc";
// var base_url="http://www.eniso.info/faurecia/svc";
// var base_url = "http://localhost:8080/faurecia/svc";

var UNDEFINED;
$(function () {

}
);



function invokeURL(url_part, success_event, error_event) {
    console.log('invokeURL ' + url_part);
    //    /sensors/log

    var request = $.ajax({
        url: (base_url + url_part),
        method: "GET"
        //, data: { } // id : menuId 
        //,  dataType: "html"
        , dataType: "json"
    });

    request.done(function (msg) {
        if (msg.s && msg.s == 's') {
            success_event(msg.r);
        } else if (msg.s && msg.s == 'f') {
            error_event(msg.r);
        } else {
            error_event(msg)
        }
        //$( "#log" ).html( msg );
    });

    request.fail(function (jqXHR, textStatus) {
        error_event(textStatus);
    });
    /*
     var resultText = "{\"s\":\"s\",\"r\":{\"myName\":\"MyValue\"}}";
     var obj=JSON.parse(resultText);
     if(obj.s='s') {
     successEvent(obj.r);
     }else{
     errorEvent(obj.r);
     }*/
}

function update_devices_list_menu(){
    $("my_devices").not(':first').remove();
    my_module.my_devices.forEach(function (rr) {
       var ss="<ul class=\"treeview-menu\"><li class=\"active\"><a onclick=\"my_module.device_uuid='"+rr+"'\"><i class=\"fa fa-circle-o\"></i> "+rr+"</a></li></ul>";
        var $2 = $(ss);
        $2.appendTo(my_devices);
        $2.css( 'cursor', 'pointer' );
    });
}

function sign_in(){
    var user=$("#login").val();
    var challenge=$("#password").val();
    invokeURL("/login?user="+user+"&challenge="+challenge
        , function (obj) {
            window.location="tracking.html";
        }
        , function (obj) {
            console.log("Error : " + obj)
        }
    );
    return false;
}

function check_signed_in(){
    invokeURL("/check-signed"
        , function (obj) {
            //ok;
        }
        , function (obj) {
            console.log("Error : " + obj)
            window.location="index.html"
        }
    );
    return false;
}
