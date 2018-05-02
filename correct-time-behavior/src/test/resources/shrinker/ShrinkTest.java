/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package org.apache.kafka.connect.runtime.distributed;

import org.apache.kafka.clients.consumer.internals.AbstractCoordinator;

import java.io.Closeable;

/**
 * This class manages the coordination process with the Kafka group coordinator on the broker for managing assignments
 * to workers.
 */
public final class WorkerCoordinator extends AbstractCoordinator implements Closeable {

    public void poll(long timeout) {
        // poll for io until the timeout expires
        long now = System.currentTimeMillis();
        long deadline = now + timeout;

        while (now <= deadline) {
            if (coordinatorUnknown()) {
                ensureCoordinatorReady();
            }

            if (needRejoin()) {
                ensureActiveGroup();
                now = System.currentTimeMillis();
            }

            now = System.currentTimeMillis();

            if (coordinatorUnknown()) {
                ensureCoordinatorReady();
            } else {
                ensureCoordinatorReady();
            }

            if (coordinatorUnknown()) {
                ensureCoordinatorReady();
            } else {
                now = System.currentTimeMillis();
            }

        }
        long time = System.currentTimeMillis();
        while (true){
            System.out.println("bla bla bla");
        }

        int i = 10;
        while(i < 10){
            System.out.println("lol");
        }
    }




}
