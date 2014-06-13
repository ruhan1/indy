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
package org.commonjava.aprox.core.rest.group;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.commonjava.aprox.AproxWorkflowException;
import org.commonjava.aprox.filer.FileManager;
import org.commonjava.aprox.model.ArtifactStore;
import org.commonjava.aprox.model.Group;
import org.commonjava.aprox.rest.group.GroupPathHandler;
import org.commonjava.aprox.util.ApplicationStatus;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;

@ApplicationScoped
public class ArchetypeCatalogHandler
    implements GroupPathHandler
{

    @Inject
    private GroupMergeHelper helper;

    @Inject
    private FileManager fileManager;

    @Inject
    private ArchetypeCatalogMerger merger;

    @Override
    public boolean canHandle( final String path )
    {
        return path.endsWith( ArchetypeCatalogMerger.CATALOG_NAME );
    }

    @Override
    public Transfer retrieve( final Group group, final List<? extends ArtifactStore> stores, final String path )
        throws AproxWorkflowException
    {
        final Transfer target = fileManager.getStorageReference( group, path );

        if ( !target.exists() )
        {
            final Set<Transfer> sources = fileManager.retrieveAll( stores, path );
            final byte[] merged = merger.merge( sources, group, path );
            if ( merged != null )
            {
                OutputStream fos = null;
                try
                {
                    fos = target.openOutputStream( TransferOperation.GENERATE, true );
                    fos.write( merged );
                }
                catch ( final IOException e )
                {
                    throw new AproxWorkflowException( "Failed to write merged archetype catalog to: {}.\nError: {}", e, target, e.getMessage() );
                }
                finally
                {
                    closeQuietly( fos );
                }

                helper.writeChecksumsAndMergeInfo( merged, sources, group, path );
            }
        }

        if ( target.exists() )
        {
            return target;
        }

        return null;
    }

    @Override
    public Transfer store( final Group group, final List<? extends ArtifactStore> stores, final String path, final InputStream stream )
        throws AproxWorkflowException
    {
        if ( path.endsWith( ArchetypeCatalogMerger.CATALOG_NAME ) )
        {
            // delete so it'll be recomputed.
            final Transfer target = fileManager.getStorageReference( group, path );
            try
            {
                target.delete();
            }
            catch ( final IOException e )
            {
                throw new AproxWorkflowException( "Failed to delete generated file (to allow re-generation on demand: {}. Error: {}", e,
                                                  target.getFullPath(), e.getMessage() );
            }
        }

        return fileManager.store( stores, path, stream );
    }

    @Override
    public boolean delete( final Group group, final List<? extends ArtifactStore> stores, final String path )
        throws AproxWorkflowException
    {
        final Transfer target = fileManager.getStorageReference( group, path );

        if ( target == null )
        {
            return false;
        }

        try
        {
            target.delete();

            helper.deleteChecksumsAndMergeInfo( group, path );
        }
        catch ( final IOException e )
        {
            throw new AproxWorkflowException( ApplicationStatus.SERVER_ERROR, "Failed to delete one or more group files for: {}:{}. Reason: {}", e,
                                              group.getKey(), path, e.getMessage() );
        }

        return true;
    }

}