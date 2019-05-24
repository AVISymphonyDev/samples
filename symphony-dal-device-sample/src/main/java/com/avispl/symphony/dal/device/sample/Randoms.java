/*
 * Copyright (c) 2019 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.device.sample;

import java.util.Random;

/**
 * Generates various type of random values
 * @author Symphony Dev Team<br> Created on May 2, 2019
 */
public class Randoms {
    private static Random random = new Random();
 
    public static boolean randomBoolean() {
        return random.nextBoolean();
    }
    
    public static int randomInt(int n) {
        return random.nextInt(n) + 1;
    }
    
    public static int randomInt() {
        return random.nextInt();
    }
    
    public static int randomInt(int min, int max) {
        int n = max - min + 1;
        int i = random.nextInt(max) % n;
        return min + i;
    }
    
    public static long randomLong() {
        return random.nextLong();
    }
    
    public static String randomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; ++i) {
            // use digits and letters from ASCII chart
            do {
                int n = random.nextInt(75) + 48;
                if ((n > 47 && n < 58) || (n > 64 && n < 91) || (n > 96 && n < 123)) {
                    sb.append((char) n);
                    break;
                }
            } while (true);
        }
        
        return sb.toString();
    }
    
    public static String randomIPAddress() {
        return random.nextInt(256) + "." + random.nextInt(256) + "." + random.nextInt(256) + "." + random.nextInt(256);
    }
    
    public static String randomMacAddress() {
        return Integer.toHexString(random.nextInt()) + ":" + Integer.toHexString(random.nextInt()) + ":" + Integer.toHexString(random.nextInt()) + ":"
                + Integer.toHexString(random.nextInt()) + ":" + Integer.toHexString(random.nextInt()) + ":" + Integer.toHexString(random.nextInt());
    }
    
    private Randoms() {
        super();
    }
    
    public static String randomString() {
        return randomString(random.nextInt(50) + 1);
    }
    
}

