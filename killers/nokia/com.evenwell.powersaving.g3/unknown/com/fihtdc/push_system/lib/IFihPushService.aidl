package com.fihtdc.push_system.lib;


interface IFihPushService {
    /**
     * Start listen push from XMPP server.
     */
    void startPushService();
    
    /**
     * This application do not want to listen push from server anymore.
     */
    void stopPush();
    
    /**
     * Is connected with XMPP server
     */
    boolean isPushConnected();
    
    /**
     * Stop this Service and never run anymore. It used to stop service if there are more than 1 service run at same time.
     */
    void shutdown();
    
    /**
     * Disconnect with XMPP server. It will still auto connect when reboot / network change.. etc
     * The XMPP server will change user status to offline when disconnect.
     */
    void disconnect();
}