//package net.vpc.app.bdvbbroker.web;
//
//import com.google.gson.Gson;
//import net.vpc.app.bdvbbroker.*;
//import net.vpc.app.bdvbbroker.web.conf.BrokerConf;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.ws.rs.GET;
//import javax.ws.rs.Path;
//import javax.ws.rs.Produces;
//import javax.ws.rs.core.Context;
//import javax.ws.rs.core.MediaType;
//import java.util.LinkedList;
//
///**
// * Created by vpc on 9/15/16.
// */
//@Component
//@Path("/sensors")
//public class SensorsWS {
//
//    @Context
//    private HttpServletRequest request;
//
//    @GET
//    @Transactional
//    @Path("/log")
//    @Produces(MediaType.APPLICATION_JSON)
//    public String log() {
//        Gson g=new Gson();
//        return g.toJson(BrokerConf.getPackets(false));
//    }
//
//}
