package com.example;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Hello world!
 *
 */
public class HTTPServerMain 
{
    public static void main( String[] args ) throws IOException, URISyntaxException
    {
        HTTPServer server = new HTTPServer();
        server.start();
    }
}
