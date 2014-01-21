/*******************************************************************************
 * Copyright (C) 2014 John Casey.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.commonjava.aprox.bind.vertx.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.commonjava.aprox.core.util.UriFormatter;
import org.commonjava.aprox.rest.AproxWorkflowException;
import org.commonjava.aprox.rest.util.ApplicationHeader;
import org.commonjava.aprox.rest.util.ApplicationStatus;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;

public final class ResponseUtils
{

    private ResponseUtils()
    {
    }

    public static void setStatus( final ApplicationStatus status, final HttpServerRequest request )
    {
        request.response()
               .setStatusCode( status.code() )
               .setStatusMessage( status.message() );
    }

    public static void setStatus( final ApplicationStatus status, final HttpServerResponse response )
    {
        response.setStatusCode( status.code() )
                .setStatusMessage( status.message() );
    }

    public static void formatRedirect( final HttpServerRequest request, final String url )
    {
        setStatus( ApplicationStatus.FOUND, request );
        request.response()
               .putHeader( ApplicationHeader.uri.key(), url );
    }

    public static void formatCreatedResponse( final HttpServerRequest request, final UriFormatter uriFormatter, final String basePath,
                                              final String... params )
    {
        final String location = uriFormatter.formatAbsolutePathTo( basePath, params );

        request.response()
               .setStatusCode( ApplicationStatus.CREATED.code() )
               .setStatusMessage( ApplicationStatus.CREATED.message() )
               .putHeader( ApplicationHeader.location.key(), location );
    }

    public static void formatOkResponseWithEntity( final HttpServerRequest request, final String json )
    {
        request.response()
               .setStatusCode( ApplicationStatus.OK.code() )
               .setStatusMessage( ApplicationStatus.OK.message() )
               .write( json );
    }

    public static void formatBadRequestResponse( final HttpServerRequest request, final String error )
    {
        request.response()
               .setStatusCode( ApplicationStatus.BAD_REQUEST.code() )
               .setStatusMessage( ApplicationStatus.BAD_REQUEST.message() )
               .write( "{\"error\": \"" + error + "\"}" );
    }

    public static void formatResponse( final AproxWorkflowException error, final HttpServerResponse response )
    {
        formatResponse( error, response, true );
    }

    public static void formatResponse( final AproxWorkflowException error, final HttpServerResponse response, final boolean includeExplanation )
    {
        if ( error.getStatus() > 0 )
        {
            response.setStatusCode( error.getStatus() );
        }
        else
        {
            response.setStatusCode( ApplicationStatus.SERVER_ERROR.code() )
                    .setStatusMessage( ApplicationStatus.SERVER_ERROR.message() );
        }

        if ( includeExplanation )
        {
            response.write( formatEntity( error ).toString() );
        }
    }

    public static void formatResponse( final Throwable e, final HttpServerResponse response )
    {
        formatResponse( e, response, true );
    }

    public static void formatResponse( final Throwable error, final HttpServerResponse response, final boolean includeExplanation )
    {
        response.setStatusCode( ApplicationStatus.SERVER_ERROR.code() )
                .setStatusMessage( ApplicationStatus.SERVER_ERROR.message() );

        if ( includeExplanation )
        {
            response.write( error.getMessage() );
        }
    }

    public static CharSequence formatEntity( final Throwable error )
    {
        final StringWriter sw = new StringWriter();
        sw.append( error.getMessage() );

        final Throwable cause = error.getCause();
        if ( cause != null )
        {
            sw.append( "\n\n" );
            cause.printStackTrace( new PrintWriter( sw ) );
        }

        return sw.toString();
    }

}