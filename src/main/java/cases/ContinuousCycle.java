/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */
package cases;

import model.Task;

/**
 * A continuous task which have no deadline (runs indefinitely) and no period time. The initial workload is 1.
 *
 * @see Task
 */
public class ContinuousCycle extends Task {

    private static final int INITIAL_WORKLOAD = 1;

    public ContinuousCycle() {
        super(0, 0, INITIAL_WORKLOAD);
    }

}
