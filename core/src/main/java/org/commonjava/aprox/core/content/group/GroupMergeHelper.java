/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.commonjava.aprox.core.content.group;

import static org.apache.commons.codec.digest.DigestUtils.md5Hex;
import static org.apache.commons.codec.digest.DigestUtils.shaHex;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.commonjava.aprox.util.LocationUtils.getKey;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Set;

import javax.inject.Inject;

import org.commonjava.aprox.content.FileManager;
import org.commonjava.aprox.content.group.GroupPathHandler;
import org.commonjava.aprox.model.Group;
import org.commonjava.aprox.model.StoreKey;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupMergeHelper
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    private FileManager fileManager;

    public final void deleteChecksumsAndMergeInfo( final Group group, final String path )
        throws IOException
    {
        final Transfer targetSha = fileManager.getStorageReference( group, path + GroupPathHandler.SHA_SUFFIX );
        final Transfer targetMd5 = fileManager.getStorageReference( group, path + GroupPathHandler.MD5_SUFFIX );
        final Transfer targetInfo = fileManager.getStorageReference( group, path + GroupPathHandler.MERGEINFO_SUFFIX );

        if ( targetSha != null )
        {
            targetSha.delete();
        }

        if ( targetMd5 != null )
        {
            targetMd5.delete();
        }

        if ( targetInfo != null )
        {
            targetInfo.delete();
        }
    }

    public final void writeChecksumsAndMergeInfo( final byte[] data, final Set<Transfer> sources, final Group group, final String path )
    {
        final Transfer targetSha = fileManager.getStorageReference( group, path + GroupPathHandler.SHA_SUFFIX );
        final Transfer targetMd5 = fileManager.getStorageReference( group, path + GroupPathHandler.MD5_SUFFIX );
        final Transfer targetInfo = fileManager.getStorageReference( group, path + GroupPathHandler.MERGEINFO_SUFFIX );

        final String sha = shaHex( data );
        final String md5 = md5Hex( data );

        Writer fw = null;
        try
        {
            fw = new OutputStreamWriter( targetSha.openOutputStream( TransferOperation.GENERATE, true ) );
            fw.write( sha );
        }
        catch ( final IOException e )
        {
            logger.error( String.format( "Failed to write SHA1 checksum for merged metadata information to: %s.\nError: %s", targetSha, e.getMessage() ), e );
        }
        finally
        {
            closeQuietly( fw );
        }

        try
        {
            fw = new OutputStreamWriter( targetMd5.openOutputStream( TransferOperation.GENERATE, true ) );
            fw.write( md5 );
        }
        catch ( final IOException e )
        {
            logger.error( String.format( "Failed to write MD5 checksum for merged metadata information to: %s.\nError: %s", targetMd5, e.getMessage() ), e );
        }
        finally
        {
            closeQuietly( fw );
        }

        try
        {
            fw = new OutputStreamWriter( targetInfo.openOutputStream( TransferOperation.GENERATE ) );
            for ( final Transfer source : sources )
            {
                final StoreKey key = getKey( source );
                fw.write( key.toString() );
                fw.write( "\n" );
            }
        }
        catch ( final IOException e )
        {
            logger.error( String.format( "Failed to write merged metadata information to: %s.\nError: %s", targetInfo, e.getMessage() ), e );
        }
        finally
        {
            closeQuietly( fw );
        }
    }

}