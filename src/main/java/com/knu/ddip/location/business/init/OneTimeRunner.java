package com.knu.ddip.location.business.init;

public interface OneTimeRunner {
    void runOnce(String lockName, Runnable task);
}

