package com.snapattack.dvlog4ja;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Path("vulnerable")
public class RestAPI {
    
    private static final Logger logger = LogManager.getLogger();
    
    @GET
    @Path("/user")
    public Response user(@HeaderParam("user-agent") String userAgent){
        logger.info("User Agent: " + userAgent);
        return Response
                .ok("User Agent: " + userAgent + "\n")
                .build();
    }
    
    @GET
    @Path("/get")
    public Response getParam(@QueryParam("x") String x){
        logger.info("GET: " + x);
        return Response
                .ok("GET '?x=': " + x + "\n")
                .build();
    }
    
    @POST
    @Path("/post")
    public Response postParam(String post){
        logger.info("POST: " + post);
        return Response
                .ok("POST: " + post + "\n")
                .build();
    }
    
    
}
