/**
 * This code was Written by Gil Tene of Azul Systems, and released to the
 * public domain, as explained at http://creativecommons.org/publicdomain/zero/1.0/

 For users of this code who wish to consume it under the "BSD" license
 rather than under the public domain or CC0 contribution text mentioned
 above, the code found under this directory is *also* provided under the
 following license (commonly referred to as the BSD 2-Clause License). This
 license does not detract from the above stated release of the code into
 the public domain, and simply represents an additional license granted by
 the Author.

 -----------------------------------------------------------------------------
 ** Beginning of "BSD 2-Clause License" text. **

  Copyright (c) 2012, 2013, 2014 Gil Tene
  All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice,
     this list of conditions and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright notice,
     this list of conditions and the following disclaimer in the documentation
     and/or other materials provided with the distribution.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
  THE POSSIBILITY OF SUCH DAMAGE.
 */

package benchmark;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.util.concurrent.TimeUnit;

import org.HdrHistogram.Histogram;

/**
 * Records 'hiccups' in the JVM by examining the response time for a sleep and object allocation. This is useful for
 * measuring delays induced by elements such as garbage collection, jitter etc. The idea and large parts of the code is
 * taken from jHiccup by Gil Tene.
 * 
 * @author jepeders
 */
public class HiccupRecorder extends Thread {

    private static final long RESOLUTION_IN_MS = 1;

    public volatile boolean doRun;
    private final boolean allocateObjects;
    public volatile SleepTimeObject lastSleepTimeObj; // public volatile to make sure
                                                      // allocs are not optimized away...
    protected final MetricRecorder recorder;

    public HiccupRecorder(final boolean allocateObjects) {
        this.setDaemon(true);
        this.setName("HiccupRecorder");
        this.allocateObjects = allocateObjects;
        doRun = true;

        this.recorder = new MetricRecorder();
    }

    public Histogram terminate() {
        doRun = false;
        return recorder.getHistogram();
    }

    @Override
    public void run() {
        final long resolutionNsec = TimeUnit.MILLISECONDS.toNanos(RESOLUTION_IN_MS);
        try {
            while (doRun) {
                final long timeBeforeMeasurement = System.nanoTime();

                NANOSECONDS.sleep(resolutionNsec);
                if (allocateObjects) {
                    // Allocate an object to make sure potential allocation
                    // stalls are measured.
                    lastSleepTimeObj = new SleepTimeObject();
                }

                long hiccupTimeNsec = System.nanoTime() - timeBeforeMeasurement;
                recorder.record(hiccupTimeNsec, resolutionNsec);
            }
        } catch (InterruptedException e) {
            System.out.println("# HiccupRecorder interrupted/terminating...");
        }
    }

    /**
     * A class created to measure allocation delays. The class is custom made to avoid hitting any preallocated caches.
     * 
     * @author jepeders
     */
    private class SleepTimeObject {
        /** No content */
    }

}