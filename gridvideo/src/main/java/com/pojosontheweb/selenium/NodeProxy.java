package com.pojosontheweb.selenium;

import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.internal.Registry;
import org.openqa.grid.internal.TestSession;

public class NodeProxy extends org.openqa.grid.selenium.proxy.DefaultRemoteProxy {

    public NodeProxy(RegistrationRequest request, Registry registry) {
        super(request, registry);
    }

    @Override
    public void beforeSession(TestSession session) {
        super.beforeSession(session);
        System.out.println("Before session : " + session);
    }

    @Override
    public void afterSession(TestSession session) {
        super.afterSession(session);
        System.out.println("After session : " + session);
    }
}
