var base_url = "/faurecia/svc";
// var base_url="http://www.eniso.info/faurecia/svc";
// var base_url = "http://localhost:8080/faurecia/svc";

var UNDEFINED;
var my_module = {
    ajax_loading: false,
    ajax_loading_starttime: UNDEFINED,
    ajax_loading_max: 2,
    map: UNDEFINED,
    marker: {},
    geocoder: {},
    owner_uuid: 'me',
    my_devices: {},
    device_uuid: '#',
    server_packets: [],
    line_type: 'speed',
    events_type: 'alarms',
    events_time: -1,
    auto_run: 1,
    manual_play: 0,
    manual_play_timeout: 1,
    retrieve_data_timeout: 15,
    gps_min_hdop: 0.0,
    gps_max_hdop: 1000.0,
    max_packet_count: 200,
    max_packet_count_limit: 2000,
    max_packet_older: 0,
    packet_date_from: '',
    packet_date_to: '',
    update_document_for_device_uuid_loop_timer: 0,
    manual_play_loop_timer: 0,
    fetch_raw: false,
    path: {},
    conf: {
        aggregate_alarms: true
    }
}
my_module.dashboard_line = new Morris.Line({
    element: 'line-chart',
    resize: true,
    data: [
        {y: '2011 Q1', item1: 2666}

    ],
    xkey: 'y',
    ykeys: ['speed'],
    labels: ['Speed (km/h)'],
    lineColors: ['#efefef'],
    lineWidth: 2,
    hideHover: 'auto',
    gridTextColor: "#fff",
    gridStrokeWidth: 0.4,
    pointSize: 4,
    pointStrokeColors: ["#efefef"],
    gridLineColor: "#efefef",
    gridTextFamily: "Open Sans",
    gridTextSize: 10
}).on('click',
    function (i, row) {
        on_change_time(i);
    }
);
$(function () {
        // $('#packetSlider').slider();
        // $('#packetSlider').slider({
        //     formatter: function(value) {
        //         return 'Current value: ' + value;
        //     }
        // });
        $appAjaxMonitor = $("#app-ajax-monitor");

        $(document).on({
            ajaxStart: function () {
                my_module.ajax_loading = true;
                my_module.ajax_loading_starttime = Date.now();
            },
            ajaxStop: function () {
                my_module.ajax_loading = false;
                my_module.ajax_loading_starttime = UNDEFINED;
                $appAjaxMonitor.removeClass("loading");
            }
        });

        lazy_request_timer = setInterval(function () {
            if (my_module.ajax_loading && my_module.ajax_loading_starttime
                && ((Date.now() - my_module.ajax_loading_starttime) / 1000) > my_module.ajax_loading_max

            ) {
                if (!$appAjaxMonitor.hasClass("loading")) {
                    $appAjaxMonitor.addClass("loading");
                }
            }
        }, 2000);
        check_signed_in();


        update_visibility();

        google.maps.event.addDomListener(window, 'load', initializeMap);
        my_module.geocoder = new google.maps.Geocoder;
        change_auto_run(1);

        invokeURL("/devices/" + my_module.owner_uuid
            , function (obj) {
                my_module.my_devices = obj;
                my_module.device_uuid = obj[0];
                update_devices_list_menu();
                update_document_for_device_uuid_loop();
                manual_play_loop();
                change_auto_run(1);
            }
            , function (obj) {
                console.log("Error : " + obj)
            }
        );
    }
);


function update_document_for_device_uuid_loop() {
    $("#max_packet_count").val(my_module.max_packet_count);
    $("#packet_date_from").val(my_module.packet_date_from);
    $("#packet_date_to").val(my_module.packet_date_to);
    $("#retrieve_data_timeout").val(my_module.retrieve_data_timeout);
    if (my_module.update_document_for_device_uuid_loop_timer) {
        clearInterval(my_module.update_document_for_device_uuid_loop_timer);
    }
    my_module.update_document_for_device_uuid_loop_timer = setInterval(function () {
        if (my_module.auto_run) {
            update_document_for_device_uuid_once();
        }
    }, my_module.retrieve_data_timeout * 1000);
    update_document_for_device_uuid_once();
    //$("#box1Label" ).text( "Hot Fuzz" );
}

function manual_play_loop() {
    $("#manual_play_timeout").val(my_module.manual_play_timeout);
    if (my_module.manual_play_loop_timer) {
        clearInterval(my_module.manual_play_loop_timer);
    }
    my_module.manual_play_loop_timer = setInterval(function () {
        if (my_module.manual_play == 1) {
            inc_time();
        }
    }, my_module.manual_play_timeout * 1000);
    //$("#box1Label" ).text( "Hot Fuzz" );
}

function formattedDate(date) {
    var d = new Date(date || Date.now()),
        month = '' + (d.getMonth() + 1),
        day = '' + d.getDate(),
        year = d.getFullYear(),
        h = "" + d.getHours(),
        m = "" + d.getMinutes(),
        s = "" + d.getSeconds()
        ;

    if (month.length < 2) month = '0' + month;
    if (day.length < 2) day = '0' + day;
    if (h.length < 2) h = '0' + h;
    if (m.length < 2) m = '0' + m;
    if (s.length < 2) s = '0' + s;

    return [year, month, day].join('/') + " " + h + ":" + m + ":" + s;
}


function update_document_for_loaded_packets() {

    if (my_module.server_packets.length > 0) {
        var nbr = my_module.time;
        if (nbr < 0) {
            nbr = 0;
        } else if (nbr >= my_module.server_packets.length) {
            nbr = my_module.server_packets.length - 1;
        }
        var r0 = uniformRow(my_module.server_packets[nbr], my_module.conf.aggregate_alarms);
        $("#box1Val").text("Device");
        $("#box1Label1").text("ID : " + r0.DeviceUUID);
        $("#box1Label2").text("Last update : " + formattedDate(new Date(r0.TimestampValue + " UTC")));
        $("#box1Label3").text("");
        $("#box1Label4").text("");

        $("#box2Val").text("Battery");
        $("#box2Label1").text("Car : " + r0.CarBattery + " (v)");
        $("#box2Label2").text("Inner : " + r0.InnerBattery + " (v)");
        $("#box2Label3").text("");
        $("#box2Label4").text("");

        $("#box3Val").text("Status");
        $("#box3Label1").text("Mileage : " + r0.Mileage);
        +" km"
        $("#box3Label2").text("Lat : " + r0.Latitude + " ; Lon : " + r0.Longitude);
        $("#box3Label3").text("");
        $("#box3Label4").text("");

        $("#box4Val").text("" + (r0.ActiveAlarms.length) + " / " + (r0.Alarms.length) + "");
        $("#box4Label1").text((r0.ActiveAlarms.length) == 0 ? "No Alarm" : "Alarms detected");
        $("#box4Label2").text((r0.ActiveStatuses.length + " Armed ; ") + (r0.ActiveAlarms.length + " Fired"));
        $("#box4Label3").text("");
        $("#box4Label4").text("");

        var latlng = {lat: parseFloat(r0.Latitude), lng: parseFloat(r0.Longitude)}
        initializeMap();
        my_module.marker.setPosition(latlng)
        my_module.geocoder.geocode({'location': latlng}, function (results, status) {
            if (status === google.maps.GeocoderStatus.OK) {
                if (results[1]) {
                    initializeMap();
                    //map.setZoom(11);
                    $("#box3Label2").text("Position : " + results[1].formatted_address)
                    // infowindow.setContent(results[1].formatted_address);
                    // infowindow.open(map, marker);
                }
            } else {
                //window.alert('Geocoder failed due to: ' + status);
            }
        });
        var pathCoordinates = [
            // {lat: 37.772, lng: -122.214},
            // {lat: 21.291, lng: -157.821},
            // {lat: -18.142, lng: 178.431},
            // {lat: -27.467, lng: 153.027}
        ];
        my_module.server_packets.forEach(function (rr) {
            var rru = uniformRow(rr, false);
            var latlng = {lat: parseFloat(rru.Latitude), lng: parseFloat(rru.Longitude)}
            pathCoordinates.push(latlng);
        });
        my_module.path.setPath(pathCoordinates);

        $("#knobSpeed").val(r0.Speed).trigger('change');
        $("#knobRPM").val(r0.RPM).trigger('change');
        $("#knobTemp").val(0).trigger('change');
        $("#knobCoolTemp").val(r0.EngineCoolantTemperature).trigger('change');
        $("#knobBattTemp").val(r0.DeviceTemp).trigger('change');
        $("#knobOil").val(0).trigger('change');
        $("#knobFuel").val(r0.Fuel).trigger('change');

        eventsList_clear();
        eventsList_addMessage_currRow(r0);

        initializeMap();
        my_module.map.setCenter(new google.maps.LatLng(r0.Latitude, r0.Longitude));
    } else {
        $("#box1Val").text("Device");
        $("#box1Label1").text("Device has no events, try change period");
        $("#box1Label2").text("No Data");
        $("#box1Label3").text("");
        $("#box1Label4").text("");

        $("#box2Val").text("Battery");
        $("#box2Label1").text("Car : No Data");
        $("#box2Label2").text("Inner : No Data");
        $("#box2Label3").text("");
        $("#box2Label4").text("");

        $("#box3Val").text("Status");
        $("#box3Label1").text("Mileage : No Data");
        $("#box3Label2").text("Lat : No Data");
        $("#box3Label3").text("");
        $("#box3Label4").text("");

        $("#box4Val").text("0/0");
        $("#box4Label1").text("No Events");
        $("#box4Label2").text("No Alarms");
        $("#box4Label3").text("");
        $("#box4Label4").text("");
        $("#knobSpeed").val(0).trigger('change');
        $("#knobRPM").val(0).trigger('change');
        $("#knobTemp").val(0).trigger('change');
        $("#knobCoolTemp").val(0).trigger('change');
        $("#knobBattTemp").val(0).trigger('change');
        $("#knobOil").val(0).trigger('change');
        $("#knobFuel").val(0).trigger('change');
        var pathCoordinates = [];
        my_module.path.setPath(pathCoordinates);
        eventsList_clear();
    }


    var newData = [];
    my_module.server_packets.forEach(function (rr) {
        var rru = uniformRow(rr, false);
        var val1 = {};
        val1.y = rru.TimestampValue;
        if (my_module.line_type == "speed") {
            val1.speed = rru.Speed;
        } else if (my_module.line_type == "rpm") {
            val1.speed = rru.RPM;
        } else if (my_module.line_type == "fuel") {
            val1.speed = rru.Fuel;
        } else if (my_module.line_type == "engineCoolantTemperature") {
            val1.speed = rru.EngineCoolantTemperature;
        } else if (my_module.line_type == "deviceTemp") {
            val1.speed = rru.DeviceTemp;
        } else if (my_module.line_type == "alarms") {
            val1.speed = rru.ActiveAlarms.length;
        } else {
            val1.speed = rru.Speed;
        }
        newData.push(val1);
    });
    if (my_module.line_type == "speed") {
        $("#lineTitle").text("Speed")
        //labels
        my_module.dashboard_line.options.labels = (['Speed (km/h)']);

    } else if (my_module.line_type == "rpm") {
        $("#lineTitle").text("RPM")
        my_module.dashboard_line.options.labels = (['RPM']);

    } else if (my_module.line_type == "fuel") {
        $("#lineTitle").text("Fuel")
        my_module.dashboard_line.options.labels = (['Fuel']);

    } else if (my_module.line_type == "engineCoolantTemperature") {
        $("#lineTitle").text("Engine Coolant Temperature")
        my_module.dashboard_line.options.labels = (['Engine Coolant Temperature']);

    } else if (my_module.line_type == "deviceTemp") {
        $("#lineTitle").text("Battery Temperature")
        my_module.dashboard_line.options.labels = (['Battery Temperature']);
    } else if (my_module.line_type == "alarms") {
        $("#lineTitle").text("Alarms Count")
        my_module.dashboard_line.options.labels = (['Alarms Count']);
    } else {
        $("#lineTitle").text("Speed")
        my_module.dashboard_line.options.labels = (['Speed (km/h)']);
    }
    my_module.dashboard_line.setData(newData);
}


function update_document_for_device_uuid_once() {
    invokeURL("/packets/" + my_module.owner_uuid + "/" + my_module.device_uuid
        + "?count=" + my_module.max_packet_count + "&from=" + my_module.packet_date_from
        + "&to=" + my_module.packet_date_to
        + (my_module.fetch_raw ? "&raw=true" : "")
        , function (obj) {
            my_module.server_packets = [];
            var max = obj.length;
            if (max > my_module.max_packet_count) {
                max = my_module.max_packet_count;
            }
            if (my_module.max_packet_older == 1) {
                for (i = 0; i < max; i++) {
                    var ii = i;
                    var obj2 = obj[ii];
                    var hdop = parseFloat(obj2.value.gps.hdop);
                    if (hdop >= my_module.gps_min_hdop && hdop <= my_module.gps_max_hdop) {
                        my_module.server_packets.push(obj2)
                    }
                }
            } else {
                for (i = 0; i < max; i++) {
                    var ii = obj.length - max + i;
                    var obj2 = obj[ii];
                    var hdop = parseFloat(obj2.value.gps.hdop);
                    if (hdop >= my_module.gps_min_hdop && hdop <= my_module.gps_max_hdop) {
                        my_module.server_packets.push(obj2)
                    }
                }
            }
            my_module.time = my_module.server_packets.length - 1;
            update_visibility();
            update_document_for_loaded_packets();
        }
        , function (obj) {
            console.log("Error : " + obj)
        }
    );
}

function uniformRow(r0, aggregate_alarms) {
    var o = {};
    o.DeviceUUID = '';
    o.CarBattery = -1;
    o.DeviceTemp = -1;
    o.InnerBattery = -1;
    o.Latitude = -1;
    o.Longitude = -1;
    o.TimestampValue = 'never';
    o.Mileage = 0;
    o.Speed = 0;
    o.RPM = 0;
    o.Fuel = 0;

    try {
        o.DeviceUUID = ( r0['deviceUUID'] );
    } catch (e) {
    }
    try {
        o.CarBattery = r0.value['vehicle']['battery'];// r0['raw']['ADC']['Car Battery'];
        if (!o.CarBattery) {
            o.CarBattery = -1
        }
        ;
    } catch (e) {
    }

    try {
        o.DeviceTemp = r0.value['device']['device-temperature'];// r0['raw']['ADC']['Device Temperature'];
        if (!o.DeviceTemp) {
            o.DeviceTemp = -1
        }
        ;
    } catch (e) {
    }

    try {
        o.InnerBattery = r0.value['device']['inner-battery'];//r0['raw']['ADC']['Inner Battery'];
        if (!o.InnerBattery) {
            o.InnerBattery = -1
        }
        ;
    } catch (e) {
    }

    try {
        o.Latitude = r0.value['gps']['latitude'];//r0['raw']['GPS']['latitude'];
        if (!o.Latitude) {
            o.Latitude = -1
        }
        ;
    } catch (e) {
    }

    try {
        o.Longitude = r0.value['gps']['longitude'];//r0['raw']['GPS']['longitude'];
        if (!o.Longitude) {
            o.Longitude = -1
        }
        ;
    } catch (e) {
    }

    try {
        o.Hdop = r0.value['gps']['hdop'];//r0['raw']['GPS']['longitude'];
        if (!o.Longitude) {
            o.Longitude = -1
        }
        ;
    } catch (e) {
    }

    try {
        o.Speed = parseInt(r0.value['gps']['speed']);//r0['raw']['GPS']['speed']
        if (!o.Speed) {
            o.Speed = 0
        }
        ;
    } catch (e) {
    }

    try {
        o.EngineSeconds = parseInt(r0.value['vehicle']['engine-seconds']);//r0['raw']['EGT']
        if (!o.EngineSeconds) {
            o.EngineSeconds = 0
        }
        ;
    } catch (e) {
    }

    try {
        o.RPM = r0.value['vehicle']['rpm'];
        o.EngineCoolantTemperature = r0.value['vehicle']['engine-coolant-temperature'];
        o.ObdSpeed = r0.value['vehicle']['speed'];
        if (!o.Speed) {
            o.Speed = o.ObdSpeed;
        }
        if (!o.RPM) {
            o.RPM = 0
        }
    } catch (e) {
    }

    try {
        o.TimestampValue = r0.value['timestamp'];
        if (!o.TimestampValue) {
            o.TimestampValue = 'never'
        }
        ;
    } catch (e) {
    }

    try {
        o.Mileage = r0.value['vehicle']['distance-traveled'];
        if (!o.Mileage) {
            o.Mileage = r0.value['vehicle']['mileage'];
        }
        if (!o.Mileage) {
            o.Mileage = 0
        }
        ;
    } catch (e) {
    }

    try {
        o.Fuel = r0.value['vehicle']['fuel-level']; //r0['raw']['FUL'];
        if (!o.Fuel) {
            o.Fuel = 0
        }
        ;
    } catch (e) {
    }

    o.ActiveAlarms = [];
    o.Alarms = [];
    o.Statuses = [];
    o.ActiveStatuses = [];

    try {
        var visited = {};
        if (aggregate_alarms) {
            my_module.server_packets.forEach(function (rr) {
                add_alarm(o, rr, visited);
            });
        } else {
            add_alarm(o, r0, visited);
        }

        var extra_alarm_moving = {};
        extra_alarm_moving.name = "Moving";
        extra_alarm_moving.value = 0;
        o.Alarms.push(extra_alarm_moving);
        if (o.Speed != 0) {
            extra_alarm_moving.value = 1;
            o.ActiveAlarms.push(extra_alarm_moving);
            // o.Statuses.push(extra_alarm_moving);
        }


    } catch (e) {
        console.log("Error : " + e);
    }

    return o;
}

function add_alarm(o, rr, visited) {
    if (rr['value']) {
        if (rr['value']['alarms']) {
            var pp = rr['value']['alarms'];
            for (var p in pp) {
                if (pp.hasOwnProperty(p)) {
                    var n = p;
                    var v = pp[p];
                    var oo = {};
                    oo.name = n;
                    oo.value = parseInt(v);
                    if (!visited["a:" + oo.name]) {
                        visited["a:" + oo.name] = oo;
                        o.Alarms.push(oo);
                    } else {
                        visited["a:" + oo.name].value += oo.value;
                    }
                    if (oo.value == 1) {
                        if (!visited["a!:" + oo.name]) {
                            visited["a!:" + oo.name] = oo;
                            o.ActiveAlarms.push(oo);
                        } else {
                            visited["a!:" + oo.name].value += oo.value;
                        }
                    }
                }
            }
        }
        if (rr['value']['settings'] && rr['value']['settings']['alarms']) {
            pp = rr['value']['settings']['alarms'];
            for (var p in pp) {
                if (pp.hasOwnProperty(p)) {
                    var n = p;
                    var v = pp[p];
                    var oo = {};
                    oo.name = n;
                    oo.value = parseInt(v);
                    if (!visited["a:" + oo.name]) {
                        visited["a:" + oo.name] = true;
                        o.Statuses.push(oo);
                    }
                    if (oo.value == 1) {
                        if (!visited["a!:" + oo.name]) {
                            visited["a!:" + oo.name] = true;
                            o.ActiveStatuses.push(oo.name);
                        }
                    }
                }
            }
        }
    }
}

function invokeURL(url_part, success_event, error_event) {
    console.log('invokeURL ' + url_part);
    //    /sensors/log

    $("#trackingContent").fadeOut();
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


$(".knob").knob({
    /*change : function (value) {
     //console.log("change : " + value);
     },
     release : function (value) {
     console.log("release : " + value);
     },
     cancel : function () {
     console.log("cancel : " + this.value);
     },*/
    // 'format' : function (value) {
    //     return value + '%';
    // },
    "readOnly": true,
    draw: function () {

        // "tron" case
        if (this.$.data('skin') == 'tron') {

            var a = this.angle(this.cv)  // Angle
                , sa = this.startAngle          // Previous start angle
                , sat = this.startAngle         // Start angle
                , ea                            // Previous end angle
                , eat = sat + a                 // End angle
                , r = true;

            this.g.lineWidth = this.lineWidth;

            this.o.cursor
            && (sat = eat - 0.3)
            && (eat = eat + 0.3);

            if (this.o.displayPrevious) {
                ea = this.startAngle + this.angle(this.value);
                this.o.cursor
                && (sa = ea - 0.3)
                && (ea = ea + 0.3);
                this.g.beginPath();
                this.g.strokeStyle = this.previousColor;
                this.g.arc(this.xy, this.xy, this.radius - this.lineWidth, sa, ea, false);
                this.g.stroke();
            }

            this.g.beginPath();
            this.g.strokeStyle = r ? this.o.fgColor : this.fgColor;
            this.g.arc(this.xy, this.xy, this.radius - this.lineWidth, sat, eat, false);
            this.g.stroke();

            this.g.lineWidth = 2;
            this.g.beginPath();
            this.g.strokeStyle = this.o.fgColor;
            this.g.arc(this.xy, this.xy, this.radius - this.lineWidth + 1 + this.lineWidth * 2 / 3, 0, 2 * Math.PI, false);
            this.g.stroke();

            return false;
        }
    }
});

$('#knobTime').trigger('configure', {"readOnly": false});

function eventsList_clear() {
    $(eventsList).empty();
}

function eventsList_addMessage_currRow(r0) {
    var eventArray = [];
    var eventIdLabel = "";//r0.TimestampValue;
    if (my_module.events_type == 'alarms') {
        r0.Alarms.forEach(function (ii) {
            if (ii.value == 0) {
                eventArray.push(createEvent(eventIdLabel, {
                    type: 'primary',
                    icon: 'fa-clock-o',
                    name: 'alarm'
                }, ii.name, [{type: 'default', icon: 'fa-clock-o', name: 'not fired'}]));
            } else {
                eventArray.push(createEvent(eventIdLabel, {
                    type: 'primary',
                    icon: 'fa-clock-o',
                    name: 'alarm'
                }, ii.name, [{
                    type: 'danger',
                    icon: 'fa-clock-o',
                    name: 'fired' + (ii.value > 1 ? (' ' + ii.value + ' times') : '')
                }]));
            }
        });
    }
    if (my_module.events_type == 'statuses') {
        r0.Statuses.forEach(function (ii) {
            if (ii.value == 0) {
                eventArray.push(createEvent(eventIdLabel, {
                    type: 'info',
                    icon: 'fa-gear',
                    name: 'rule'
                }, ii.name, [{type: 'default', icon: 'fa-clock-o', name: 'not armed'}]));
            } else {
                eventArray.push(createEvent(eventIdLabel, {
                    type: 'info',
                    icon: 'fa-gear',
                    name: 'rule'
                }, ii.name, [{type: 'warning', icon: 'fa-clock-o', name: 'armed'}]));
            }
        });
    }
    function compareEval(a) {
        var i = 0;
        var ii = 0;
        for (i = 0; i < a.labels.length; i++) {
            var tt = a.labels[i].type;
            if (tt == "default") {
                ii += 1;
            } else if (tt == "info") {
                ii += 50;
            } else if (tt == "warning") {
                ii += 100;
            } else {
                ii += 10;
            }
        }
        return ii;
    }

    function compare(a, b) {
        return compareEval(b) - compareEval(a);
    }

    eventArray.sort(compare);
    eventArray.forEach(function (ii) {
        eventsList_addMessage(ii.eventIdLabel, ii.eventTypeLabel, ii.messageText, ii.labels);
    })
}

function createEvent(eventIdLabel, eventTypeLabel, messageText, labels) {
    var ee = {};
    ee.eventIdLabel = eventIdLabel;
    ee.messageText = messageText;
    ee.eventTypeLabel = eventTypeLabel;
    ee.labels = labels;
    return ee;
}

function eventsList_addMessage_example() {
    eventsList_addMessage("Design a nice theme", "danger", "fa-clock-o", "2 mins")
}

function eventsList_addMessage(eventIdLabel, eventTypeLabel, messageText, labels) {
    var ss = "<li>";
    if (eventIdLabel && eventIdLabel.length > 0) {
        ss += "<span class=\"handle\"> " + eventIdLabel + " </span>|";
    }
    // ss+="<span class=\"handle\"> <i class=\"fa fa-ellipsis-v\"></i> <i class=\"fa fa-ellipsis-v\"></i> </span>";
    // ss+="<input type=\"checkbox\" value=\"\" checked=\"checked\">";
    ss += "<small class=\"label label-" + eventTypeLabel.type + "\"><i class=\"fa " + eventTypeLabel.icon + "\"></i> " + eventTypeLabel.name + "</small>";
    ss += "<span class=\"text\">" + messageText + "</span>";
    labels.forEach(function (lab) {
        ss += "<small class=\"label label-" + lab.type + "\"><i class=\"fa " + lab.icon + "\"></i> " + lab.name + "</small>";
    });
    ss += "</li>";
    $(ss).appendTo(eventsList);
    /*
     <li>
     <span class="handle">
     <i class="fa fa-ellipsis-v"></i>
     <i class="fa fa-ellipsis-v"></i>
     </span>
     <input type="checkbox" value="" checked="checked">
     <span class="text">Design a nice theme</span>
     <small class="label label-danger"><i class="fa fa-clock-o"></i> 2 mins</small>
     <div class="tools">
     <i class="fa fa-edit"></i>
     <i class="fa fa-trash-o"></i>
     </div>
     </li>


     */
}

function line_onSelect(typeSel) {
    my_module.line_type = typeSel;
    update_document_for_loaded_packets();
}

function events_onSelect(typeSel) {
    my_module.events_type = typeSel;
    if (typeSel == "alarms") {
        $("#eventsTitle").text("Alarms");
    } else if (typeSel == "statuses") {
        $("#eventsTitle").text("Rules");
    } else {
        $("#eventsTitle").text("?");
    }
    update_document_for_loaded_packets();
}


function initializeMap() {
    if (!my_module.map) {
        var latLng = new google.maps.LatLng(-34.397, 150.644);
        var mapOptions = {
            zoom: 8,
            center: latLng
        };
        my_module.map = new google.maps.Map(document.getElementById('map-canvas'),
            mapOptions);
        my_module.marker = new google.maps.Marker({
            position: latLng,
            map: my_module.map,
            animation: google.maps.Animation.DROP,
            title: 'My Device'
        });


        var pathCoordinates = [
            // {lat: 37.772, lng: -122.214},
            // {lat: 21.291, lng: -157.821},
            // {lat: -18.142, lng: 178.431},
            // {lat: -27.467, lng: 153.027}
        ];
        my_module.path = new google.maps.Polyline({
            path: pathCoordinates,
            geodesic: true,
            strokeColor: '#FF0000',
            strokeOpacity: 1.0,
            strokeWeight: 5
        });
        my_module.path.setMap(my_module.map);
    }
}

function get_map() {
    if (!my_module.map) {
        initializeMap();
    }
    return my_module.map;
}

function inc_time() {
    var time = my_module.time;
    if (!time) {
        time = 0;
    }
    time++;
    if (time >= my_module.server_packets.length) {
        time = my_module.server_packets.length - 1;
    }
    on_change_time(time);
}

function on_change_time(new_time) {
    if (!new_time) {
        new_time = 0;
    }
    if (new_time < 0) {
        new_time = 0;
    }
    if (my_module.server_packets) {
        if (new_time >= my_module.server_packets.length) {
            new_time = my_module.server_packets.length - 1;
        }
    }
    my_module.time = new_time;
    if ($('#knobTime').val() && $('#knobTime').val() != new_time) {
        $('#knobTime').val(new_time);
    }
    update_document_for_loaded_packets();
}

$('#knobTime').trigger('configure', {
    "readOnly": false,
    'release': function (v) {
        on_change_time(v);
    }
});

function playback_onNext() {
    on_change_time(my_module.time + 1);
}

function playback_onPrevious() {
    on_change_time(my_module.time - 1);
}

function playback_onPlay() {
    if (my_module.server_packets) {
        if (my_module.time == my_module.server_packets.length - 1) {
            my_module.time = 0;
        }
    }
    change_auto_run(0);
    my_module.manual_play = 1;
}

function playback_onStop() {
    my_module.manual_play = 0;
    my_module.time = 0;
    change_auto_run(1);
}

function playback_onPause() {
    my_module.manual_play = 0;
}

function change_auto_run(ar) {
    if (ar) {
        if (ar == 0 || ar == false) {
            my_module.auto_run = 0;
        }
        if (ar == 1 || ar == true) {
            my_module.auto_run = 1;
            manual_play = 0;
        }
    } else {
        my_module.auto_run = 0;
    }
    if (my_module.auto_run == 0) {
        if ($("#btn_auto_run_active").hasClass("active")) {
            $("#btn_auto_run_active").removeClass("active")
        }
        if (!($("#btn_auto_run_inactive").hasClass("active"))) {
            $("#btn_auto_run_inactive").addClass("active")
        }
        $('#btn_play').css('color', "#dd4b39");
        // $('#knobTime').trigger('configure', {"fgColor": "#cccccc"});
    } else {
        if (!($("#btn_auto_run_active").hasClass("active"))) {
            $("#btn_auto_run_active").addClass("active")
        }
        if ($("#btn_auto_run_inactive").hasClass("active")) {
            $("#btn_auto_run_inactive").removeClass("active")
        }
        // $('#knobTime').trigger('configure', {"fgColor": "#00c0ef"});
        $('#btn_play').css('color', "black");
    }
}


function onclick_configure_playback_apply() {
    var v = $("#manual_play_timeout").val();
    try {
        var v = parseInt(v);
        if (v > 0 && v <= 60) {
            my_module.manual_play_timeout = v;
            manual_play_loop();
        }
        $("#manual_play_timeout").val(my_module.manual_play_timeout);
    } catch (e) {
        console.log("Error : " + e);
        $("#manual_play_timeout").val(my_module.manual_play_timeout);
    }
}

function onclick_configure_playback_cancel() {
    $("#manual_play_timeout").val(my_module.manual_play_timeout);
}

function onclick_configure_retrieve_cancel() {
    $("#max_packet_count").val(my_module.max_packet_count);
    $("#packet_date_from").val(my_module.packet_date_from);
    $("#packet_date_to").val(my_module.packet_date_to);
    $("#retrieve_data_timeout").val(my_module.retrieve_data_timeout);
}

function onclick_configure_retrieve_apply() {
    var v = $("#max_packet_count").val();
    try {
        var v = parseInt(v);
        if (v > 0 && v <= my_module.max_packet_count_limit) {
            my_module.max_packet_count = v;
        } else if (v > my_module.max_packet_count_limit) {
            my_module.max_packet_count = my_module.max_packet_count_limit;
        }
        $("#max_packet_count").val(my_module.max_packet_count);
    } catch (e) {
        console.log("Error : " + e);
        $("#max_packet_count").val(my_module.max_packet_count);
    }

    v = $("#packet_date_from").val();
    try {
        my_module.packet_date_from = v;
        $("#packet_date_from").val(my_module.packet_date_from);
    } catch (e) {
        console.log("Error : " + e);
        $("#packet_date_from").val(my_module.packet_date_from);
    }
    v = $("#packet_date_to").val();
    try {
        my_module.packet_date_to = v;
        $("#packet_date_to").val(my_module.packet_date_to);
    } catch (e) {
        console.log("Error : " + e);
        $("#packet_date_to").val(my_module.packet_date_to);
    }
    v = $("#retrieve_data_timeout").val();
    try {
        var v = parseInt(v);
        if (v > 0 && v <= 500) {
            my_module.retrieve_data_timeout = v;
        }
        $("#retrieve_data_timeout").val(my_module.retrieve_data_timeout);
    } catch (e) {
        console.log("Error : " + e);
        $("#retrieve_data_timeout").val(my_module.retrieve_data_timeout);
    }
    update_document_for_device_uuid_loop();
}

function onclick_step_retrieve_cancel() {
    $("#max_packet_count").val(my_module.max_packet_count);
    $("#packet_date_from").val(my_module.packet_date_from);
    $("#packet_date_to").val(my_module.packet_date_to);
}

function update_visibility() {
    var display = my_module.server_packets.length > 0 ? "block" : "none";
    $("#div_battery").css("display", display)
    $("#div_status").css("display", display)
    $("#div_alarms_count").css("display", display)
    $("#div_alarms").css("display", display)
    $("#div_dashboard").css("display", display)
    $("#div_playback").css("display", display)
    $("#div_map").css("display", "block")
    $("#div_curves").css("display", display)
    $("#div_fuel").css("display", "none")
}

$("#packet_date_from").inputmask("dd/mm/yyyy", {"placeholder": "dd/mm/yyyy"});
$("[data-mask]").inputmask();

function update_devices_list_menu() {
    $("my_devices").not(':first').remove();
    my_module.my_devices.forEach(function (rr) {
        var ss = "<ul class=\"treeview-menu\"><li class=\"active\"><a onclick=\"my_module.device_uuid='" + rr + "'\"><i class=\"fa fa-circle-o\"></i> " + rr + "</a></li></ul>";
        var $2 = $(ss);
        $2.appendTo(my_devices);
        $2.css('cursor', 'pointer');
    });
}

function sign_out() {
    invokeURL("/logout"
        , function (obj) {
            window.location = "index.html";
        }
        , function (obj) {
            console.log("Error : " + obj)
            window.location = "index.html";
        }
    );
    return false;
}


function check_signed_in() {
    invokeURL("/check-signed"
        , function (obj) {
            //ok;
        }
        , function (obj) {
            console.log("Error : " + obj)
            window.location = "index.html"
        }
    );
    return false;
}

