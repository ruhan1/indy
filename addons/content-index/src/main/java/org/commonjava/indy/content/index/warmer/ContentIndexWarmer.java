package org.commonjava.indy.content.index.warmer;

import org.commonjava.cdi.util.weft.ExecutorConfig;
import org.commonjava.cdi.util.weft.WeftManaged;
import org.commonjava.indy.IndyWorkflowException;
import org.commonjava.indy.content.DownloadManager;
import org.commonjava.indy.content.index.ContentIndexManager;
import org.commonjava.indy.data.IndyDataException;
import org.commonjava.indy.data.StoreDataManager;
import org.commonjava.indy.model.core.ArtifactStore;
import org.commonjava.indy.model.core.Group;
import org.commonjava.indy.model.core.StoreKey;
import org.commonjava.indy.model.core.StoreType;
import org.commonjava.maven.galley.model.Transfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

@ApplicationScoped
public class ContentIndexWarmer
{
    @Inject
    private ContentIndexManager indexManager;

    @Inject
    private StoreDataManager storeDataManager;

    @Inject
    private DownloadManager downloadManager;

    @WeftManaged
    @ExecutorConfig( named = "content-index-warmer", priority = 6, threads = 12 )
    @Inject
    private ExecutorService executor;

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    public void warmCaches()
    {
        executor.submit( ()->{
            Map<StoreKey, List<Transfer>> transferMap = new ConcurrentHashMap<>();

            try
            {
                List<ArtifactStore> concreteStores = storeDataManager.query().storeTypes( StoreType.hosted, StoreType.remote ).getAll();

                CountDownLatch latch = new CountDownLatch( concreteStores.size() );

                concreteStores.forEach( store -> executor.submit( () -> {
                    try
                    {
                        List<Transfer> transfers = downloadManager.listRecursively( store.getKey(), DownloadManager.ROOT_PATH );
                        transferMap.put( store.getKey(), transfers );
                        transfers.forEach( t->indexManager.indexTransferIn( t, store.getKey() ) );
                    }
                    catch ( IndyWorkflowException e )
                    {
                        logger.warn( "Failed to retrieve root directory of storage for: " + store.getKey(),
                                     e );
                    }
                    finally
                    {
                        latch.countDown();
                    }
                } ) );

                try
                {
                    latch.await();
                }
                catch ( InterruptedException e )
                {
                    logger.info("Manager thread interrupted while waiting for concrete store indexing to complete.");
                    return;
                }

                storeDataManager.query().storeType( Group.class ).stream().forEach( g-> executor.submit(() -> {
                    StoreKey gkey = g.getKey();

                    try
                    {
                        List<ArtifactStore> stores =
                                storeDataManager.query().getOrderedConcreteStoresInGroup( g.getName() );

                        stores.forEach( s -> {
                             List<Transfer> txfrs = transferMap.get( s.getKey() );
                             txfrs.forEach( t -> {
                                 if ( indexManager.getIndexedStorePath( gkey, t.getPath() ) == null )
                                 {
                                     indexManager.indexTransferIn( t, gkey );
                                 }
                             } );
                         } );
                    }
                    catch ( IndyDataException e )
                    {
                        logger.warn( "Failed to get ordered concrete stores for group: " + g.getName(), e );
                    }
                } ) );
            }
            catch ( IndyDataException e )
            {
                logger.warn( "Content index warm-up failed: %s", e, e.getMessage() );
            }

            logger.info( "Content index cache has been re-established." );

        } );
    }
}
