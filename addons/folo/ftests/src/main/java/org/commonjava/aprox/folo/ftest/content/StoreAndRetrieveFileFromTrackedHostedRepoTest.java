/**
 * Copyright (C) 2011 Red Hat, Inc. (jdcasey@commonjava.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.aprox.folo.ftest.content;

import static org.commonjava.aprox.model.core.StoreType.hosted;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.commonjava.aprox.folo.client.AproxFoloContentClientModule;
import org.junit.Test;

public class StoreAndRetrieveFileFromTrackedHostedRepoTest
    extends AbstractFoloContentManagementTest
{

    @Test
    public void storeAndRetrieveFile()
        throws Exception
    {
        final String trackingId = newName();
        
        final byte[] bytes = ( "This is a test: " + System.nanoTime() ).getBytes();
        final InputStream stream = new ByteArrayInputStream( bytes );

        final String path = "/path/to/foo.class";
        client.module( AproxFoloContentClientModule.class )
              .store( trackingId, hosted, STORE, path, stream );

        final InputStream result = client.module( AproxFoloContentClientModule.class )
                                         .get( trackingId, hosted, STORE, path );
        final byte[] resultBytes = IOUtils.toByteArray( result );
        result.close();

        assertThat( Arrays.equals( bytes, resultBytes ), equalTo( true ) );
    }
}
