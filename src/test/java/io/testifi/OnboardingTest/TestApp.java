package io.testifi.OnboardingTest;

import io.testifi.cast.testng.DefaultTestRunner;

public class TestApp {

    public static void main(String[] args) throws Exception {
        System.setProperty("xray:testEnvironment", "localRun");
        System.setProperty("xray:executionName", "remote");
        DefaultTestRunner.runTestNg("regression");
    }

}
