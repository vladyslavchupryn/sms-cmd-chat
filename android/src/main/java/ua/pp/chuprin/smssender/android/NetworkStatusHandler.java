package ua.pp.chuprin.smssender.android;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import org.alljoyn.bus.BusAttachment;
import org.alljoyn.bus.BusListener;
import org.alljoyn.bus.Mutable;
import org.alljoyn.bus.SessionOpts;
import org.alljoyn.bus.SessionPortListener;
import org.alljoyn.bus.SignalEmitter;
import org.alljoyn.bus.Status;
import org.alljoyn.bus.alljoyn.DaemonInit;
import ua.pp.chuprin.smssender.shared.Config;
import ua.pp.chuprin.smssender.shared.SmsReceiver;
import ua.pp.chuprin.smssender.shared.SmsSender;
import ua.pp.chuprin.smssender.shared.TransportException;

import java.util.Random;

import static ua.pp.chuprin.smssender.shared.AllJoynHelper.check;

public class NetworkStatusHandler extends Handler {

    public static final int CONNECT = 1;
    public static final int DISCONNECT = 2;

    private final Context application;
    private final Handler uiHandler;

    private BusAttachment senderBus;
    private final SmsSenderImpl sender;

    public NetworkStatusHandler(Looper looper, Context application, Handler uiHandler) {
        super(looper);
        this.application = application;
        this.uiHandler = uiHandler;
        sender = new SmsSenderImpl(application.getContentResolver());
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case CONNECT:
                connect();
                break;
            case DISCONNECT:
                disconnect();
                getLooper().quit();
                break;
        }
    }

    private void disconnect() {
        if(senderBus != null && senderBus.isConnected()) {
            senderBus.unregisterBusObject(sender);
            senderBus.unregisterBusObject(SmsReceiver.EMPTY);
            senderBus.unbindSessionPort(Config.SERVICE_PORT);
            senderBus.cancelAdvertiseName(Config.SERVICE_BUS_NAME, SessionOpts.TRANSPORT_ANY);
            senderBus.disconnect();
        }
        senderBus = null;
    }

    private void connect() {
        DaemonInit.PrepareDaemon(application);

        senderBus = new BusAttachment("SmsCmdChat-android", BusAttachment.RemoteMessage.Receive);
        senderBus.registerBusListener(new BusListener());

        try {
            check(senderBus.registerBusObject(sender, SmsSender.PATH), "BusAttachment.registerBusObject(sender)");
            check(senderBus.registerBusObject(SmsReceiver.EMPTY, "/any/random/path"), "BusAttachment.registerBusObject(receiver)");
            check(senderBus.connect(), "BusAttachment.connect()");

            Mutable.ShortValue contactPort = new Mutable.ShortValue(Config.SERVICE_PORT);
            check(senderBus.bindSessionPort(contactPort, Config.DEFAULT_SESSION_OPTIONS, new JoinSessionListener()),
                    "BusAttachment.bindSessionPort(%d, %s)", contactPort.value, Config.DEFAULT_SESSION_OPTIONS.toString());

            String bus;
            String pin;
            do {
                pin = generatePin();
                bus = Config.SERVICE_BUS_NAME + pin;
            } while (senderBus.requestName(bus, BusAttachment.ALLJOYN_REQUESTNAME_FLAG_DO_NOT_QUEUE) != Status.OK);

            check(senderBus.advertiseName(bus, SessionOpts.TRANSPORT_ANY), "BusAttachment.advertiseName(%s)", bus);

            updatePinInView(pin);
        } catch (TransportException e) {
            disconnect();
        }
    }

    private void updatePinInView(String pin) {
        Message updatePin = uiHandler.obtainMessage(MainActivity.UiActionsHandler.UPDATE_PIN, pin);
        uiHandler.sendMessage(updatePin);
    }

    private String generatePin() {
        Random random = new Random();
        String value = String.valueOf(random.nextInt(9999));
        while(value.length() != 4) {
            value = "0" + value;
        }

        return value;
    }

    private class JoinSessionListener extends SessionPortListener {
        @Override
        public boolean acceptSessionJoiner(short sessionPort, String joiner, SessionOpts sessionOpts) {
            return true;
        }

        @Override
        public void sessionJoined(short sessionPort, int id, String joiner) {
            SignalEmitter emitter = new SignalEmitter(SmsReceiver.EMPTY, joiner, id, SignalEmitter.GlobalBroadcast.Off);
            SmsReceiver receiver = emitter.getInterface(SmsReceiver.class);
            SmsListener.addReceiver(receiver);
        }
    }
}
