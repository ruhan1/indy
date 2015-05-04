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
package org.commonjava.aprox.action;

/**
 * Converts storage/data formats from older versions of AProx into up-to-date forms.
 * These are run right after the system boots, but before AProx enters its startup sequence (see {@link StartupAction}).
 */
public interface MigrationAction
    extends AproxLifecycleAction
{

    /**
     * Execute the migration, and return whether anything was changed as a result.
     */
    boolean migrate()
        throws AproxLifecycleException;

    /**
     * Used to sort the actions, with highest priority executing first.
     * Priority should generally be between 1-100.
     */
    int getMigrationPriority();

}
