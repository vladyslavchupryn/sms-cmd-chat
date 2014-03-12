package ua.pp.chuprin.smssender.shared;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.BusObject;
import org.alljoyn.bus.annotation.BusInterface;
import org.alljoyn.bus.annotation.BusSignal;

@BusInterface(name = SmsReceiver.NAME)
public interface SmsReceiver {

    public static final String NAME = "ua.pp.chuprin.smssender.shared.SmsReceiver";
    public static final ObjectSmsReceiver EMPTY = new ObjectSmsReceiver();

    @BusSignal
    public void receive(String contact, String message);

    public static class ObjectSmsReceiver implements SmsReceiver, BusObject {
        @Override
        public void receive(String contact, String message) {}
    }
}
