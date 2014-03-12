package ua.pp.chuprin.smssender.desktop;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.annotation.BusSignalHandler;
import ua.pp.chuprin.smssender.shared.SmsReceiver;

public class SmsReceiverImpl implements SmsReceiver {

    private final String contact;

    public SmsReceiverImpl(String contact) {
        this.contact = contact;
    }

    @BusSignalHandler(iface = SmsReceiver.NAME, signal = "receive")
    public void receive(String contact, String message) {
        if(this.contact.equals(contact)) {
            System.out.println(">>> " + message);
        }
    }
}
