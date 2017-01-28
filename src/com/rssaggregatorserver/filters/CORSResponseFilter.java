package com.rssaggregatorserver.filters;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class CORSResponseFilter
implements ContainerResponseFilter {

	/*public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {

		System.out.println("Ajout du header CORS");
		MultivaluedMap<String, Object> headers = responseContext.getHeaders();

		headers.add("Access-Control-Allow-Origin", "null");
		headers.add("Access-Control-Allow-Header", "Origin, X-Requested-With, Content-Type, Accept, Authorization"); //allows CORS requests only coming from podcastpedia.org
		headers.add("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE");
		headers.add("Access-Control-Allow-Credentials", "true");
	}*/
	
	 @Override
	    public void filter(ContainerRequestContext request,
	            ContainerResponseContext response) throws IOException {
	        response.getHeaders().add("Access-Control-Allow-Origin", "*");
	        response.getHeaders().add("Access-Control-Allow-Headers",
	                "origin, content-type, accept, authorization");
	        response.getHeaders().add("Access-Control-Allow-Credentials", "true");
	        response.getHeaders().add("Access-Control-Allow-Methods",
	                "GET, POST, PUT, DELETE, OPTIONS, HEAD");
	    }

}
