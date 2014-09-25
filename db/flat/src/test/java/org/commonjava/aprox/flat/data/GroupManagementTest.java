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
package org.commonjava.aprox.flat.data;


import org.commonjava.aprox.core.data.GroupDataManagerTCK;
import org.commonjava.aprox.core.data.TCKFixtureProvider;
import org.junit.Rule;

public class GroupManagementTest
    extends GroupDataManagerTCK
{

    @Rule
    public DataFileTCKFixtureProvider provider = new DataFileTCKFixtureProvider();

    @Override
    protected TCKFixtureProvider getFixtureProvider()
    {
        return provider;
    }

}