package ua.pp.chuprin.smssender.desktop;

import org.alljoyn.bus.BusAttachment;
import org.alljoyn.bus.BusListener;
import org.alljoyn.bus.Mutable;
import org.alljoyn.bus.ProxyBusObject;
import org.alljoyn.bus.SessionListener;
import org.alljoyn.bus.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.pp.chuprin.smssender.shared.Config;
import ua.pp.chuprin.smssender.shared.SmsReceiver;
import ua.pp.chuprin.smssender.shared.SmsSender;
import ua.pp.chuprin.smssender.shared.TransportException;

import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static ua.pp.chuprin.smssender.shared.AllJoynHelper.check;

public class Connection implements Runnable {

    protected static final Logger LOG = LoggerFactory.getLogger(Connection.class);
    private final String contact;

    private BusAttachment bus;
    private Integer sessionId;

    private SmsSender smsSender;
    private String busPrefixName;

    CountDownLatch joinSessionLatch = new CountDownLatch(1);

    public Connection(String pin, String contact) {
        busPrefixName = Config.SERVICE_BUS_NAME + pin;
        this.contact = contact;
    }

    public void run() {
        bus = new BusAttachment("SmsCmdChat-desktop", BusAttachment.RemoteMessage.Receive);
        bus.registerBusListener(new ConnectBusListener());

        try {
            check(bus.registerBusObject(SmsReceiver.EMPTY, "/not/influence"), "BusAttachment.registerBusObject()");
            check(bus.connect(), "BusAttachment.connect()");

            Status findStatus = bus.findAdvertisedName(busPrefixName);
            if(findStatus == Status.BUS_NOT_CONNECTED) {
                System.out.println("Your pin incorrect");
            } else if(findStatus == Status.OK) {

                try {
                    if(joinSessionLatch.await(15, TimeUnit.SECONDS)) {
                        assert (sessionId != null);
                        check(bus.registerSignalHandlers(new SmsReceiverImpl(contact)),
                                "BusAttachment.registerSignalHandlers successful");
                        runMainLoop();
                    } else {
                        System.out.println("Your pin incorrect or problems with network");
                    }
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                }
            } else {
                LOG.error("Error advertising name: %s", findStatus.name());
            }
        } catch (TransportException e) {
            return;
        } finally {
            disconnect();
        }
    }

    private void runMainLoop() {
        System.out.println("connected");
        Scanner scanner = new Scanner(System.in);
        String message;
        try {
            while((message = scanner.nextLine()) != null) {
                if(message.equals("\\q")) {
                    scanner.close();
                    return;
                } else {
                    smsSender.send(contact, message);
                }
            }
        } catch (NoSuchElementException e) {
            scanner.close();
        }
    }

    private void disconnect() {
        if (sessionId != null) {
            bus.leaveSession(sessionId);
        }
        if(bus != null && bus.isConnected()) {
            bus.unregisterBusObject(SmsReceiver.EMPTY);
            bus.disconnect();
        }
        bus = null;
    }

    private class ConnectBusListener extends BusListener {

        @Override
        public void foundAdvertisedName(String name, short transport, String namePrefix) {
            Mutable.IntegerValue sessionId = new Mutable.IntegerValue();
            bus.enableConcurrentCallbacks();
            try {
                check(bus.joinSession(name, Config.SERVICE_PORT, sessionId, Config.DEFAULT_SESSION_OPTIONS, new SessionListener() {
                    @Override
                    public void sessionLost(int sessionId, int reason) {
                        Connection.this.sessionId = null;
                        LOG.debug("MyBusListener.sessionLost(sessionId = %d, reason = %d)", sessionId, reason);
                    }
                }), "BusAttachment.joinSession()");

                ProxyBusObject proxyBusObject = bus.getProxyBusObject(namePrefix, SmsSender.PATH,
                        sessionId.value, new Class[]{SmsSender.class });
                smsSender = proxyBusObject.getInterface(SmsSender.class);
                Connection.this.sessionId = sessionId.value;
                joinSessionLatch.countDown();
            } catch (TransportException e) {
                throw new IllegalStateException(e);
            }
        }
    }

}
