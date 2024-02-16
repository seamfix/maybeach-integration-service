package com.seamfix.nimc.maybeach.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class Utility {
    private Utility(){}
    public static void logError(String label, String... message){
        if(log.isErrorEnabled()){
            log.error(label, message);
        }
    }
}
