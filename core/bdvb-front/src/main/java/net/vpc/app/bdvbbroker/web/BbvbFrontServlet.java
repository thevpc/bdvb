/**
 * ====================================================================
 *            Big Data Vista Baby
 *
 * is a new Open Source IoT Broker and HAL Server for Iot Devices.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.bdvbbroker.web;

import com.google.gson.Gson;
import net.vpc.app.bdvbbroker.client.BdvbPacket;
import net.vpc.app.bdvbbroker.client.BdvbPacketTransform;
import net.vpc.app.bdvbbroker.client.ClientUtils;
import net.vpc.app.bdvbbroker.client.DefaultBdvbClient;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by vpc on 11/23/16.
 */
@WebServlet(urlPatterns = "/svc/*")
public class BbvbFrontServlet extends HttpServlet {
    public static final BdvbPacketTransform SIMPLIFY_TRANSFORM = new BdvbPacketTransform() {
        @Override
        public BdvbPacket transform(BdvbPacket packet) {
            BdvbPacket simplify = packet.simplify();
            simplify.setRaw(null);//ignore raw data
            return simplify;
        }
    };
    public static final BdvbPacketTransform RAW_TRANSFORM = new BdvbPacketTransform() {
        @Override
        public BdvbPacket transform(BdvbPacket packet) {
            return packet;
        }
    };
    private DefaultBdvbClient client;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        client = new DefaultBdvbClient(null);
    }

    @Override
    public void destroy() {
        super.destroy();
        if (client != null) {
            client.stop();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if(pathInfo==null){
            //never
            pathInfo="";
        }
        pathInfo=pathInfo.trim();
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "OPTIONS, GET, POST");//, PUT, DELETE"
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        try {
            if (!pathInfo.isEmpty()) {
                if (pathInfo.startsWith("/packets/")) {
                    String[] owneruuidAnddeviceuuid = pathInfo.substring("/packets/".length()).split("/");
                    String owneruuid = owneruuidAnddeviceuuid.length < 1 ? "#" : owneruuidAnddeviceuuid[0];
                    String deviceuuid = owneruuidAnddeviceuuid.length < 2 ? "#" : owneruuidAnddeviceuuid[1];
                    List<BdvbPacket> result = client.findByDeviceUUID(owneruuid, deviceuuid
                            , ClientUtils.parseDateOrNull(req.getParameter("from"))
                            , ClientUtils.parseDateOrNull(req.getParameter("to"))
                            , ClientUtils.parseInt(req.getParameter("count"),100)
                            , Boolean.parseBoolean(req.getParameter("raw"))?RAW_TRANSFORM:SIMPLIFY_TRANSFORM
                    );
                    sendSuccess(result, resp);
                    return;
                }else if (pathInfo.startsWith("/devices/")) {
                    String owneruuid = pathInfo.substring("/devices/".length());
                    Set<String> result = client.findDistinctDeviceUUID(owneruuid
                            , ClientUtils.parseDateOrNull(req.getParameter("from"))
                            , ClientUtils.parseDateOrNull(req.getParameter("to"))
                            , ClientUtils.parseInt(req.getParameter("count"),100)
                    );
                    sendSuccess(result, resp);
                    return;
                }else if (pathInfo.equals("/login")) {
                    String user = req.getParameter("user");
                    String challenge = req.getParameter("challenge");
                    String owner=null;
                    //will change this to db access soon
                    if("admin".equals(user)
                            && "admin".equals(challenge)
                            ) {
                        owner = "me";
                    }

                    if(owner!=null){
                        req.getSession(true).setAttribute("connected", user);
                        sendSuccess(owner, resp);
                        return;
                    }else{
                        sendFail("Unauthorized", 403, resp);
                        return;
                    }
                }else if (pathInfo.equals("/check-signed")) {
                    if(req.getSession(true).getAttribute("connected")!=null){
                        sendSuccess(String.valueOf(req.getSession(true).getAttribute("connected")), resp);
                        return;
                    }else{
                        sendFail("Unauthorized", 403, resp);
                        return;
                    }
                }else if (pathInfo.equals("/logout")) {
                    try {
                        req.getSession(true).invalidate();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    sendSuccess("singed-out", resp);
                    return;
                }
            }
            sendFail("Unsupported Operation", 404, resp);
        }catch(Exception e){
            sendFail("Error", 500, resp);
        }
    }

    public void sendSuccess(Object value, HttpServletResponse resp) throws IOException {
        PrintWriter writer = resp.getWriter();
        resp.setStatus(200);
        resp.setContentType("text/json");
        Gson g = new Gson();
        Map<String, Object> v = new HashMap<>();
        v.put("s", "s");
        v.put("r", value);
        writer.println(g.toJson(v));
    }

    public void sendFail(Object value, int code, HttpServletResponse resp) throws IOException {
        PrintWriter writer = resp.getWriter();
        resp.setStatus(code);
        resp.setContentType("text/json");
        Gson g = new Gson();
        Map<String, Object> v = new HashMap<>();
        v.put("s", "f");
        v.put("r", value);
        writer.println(g.toJson(v));
    }
}
