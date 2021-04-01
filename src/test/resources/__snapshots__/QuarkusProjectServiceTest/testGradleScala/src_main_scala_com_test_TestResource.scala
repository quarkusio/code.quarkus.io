package com.test

import javax.ws.rs.{GET, Path, Produces}
import javax.ws.rs.core.MediaType

@Path("/hello-resteasy")
class TestResource {

    @GET
    @Produces(Array[String](MediaType.TEXT_PLAIN))
    def hello() = "Hello RESTEasy"
}