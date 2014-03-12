package ua.pp.chuprin.smssender.shared;

import org.alljoyn.bus.annotation.AccessPermission;
import org.alljoyn.bus.annotation.BusInterface;
import org.alljoyn.bus.annotation.BusMethod;

@BusInterface
public interface SmsSender {
    public static final String PATH = "/smssender";

//    @AccessPermission("android.permission.SEND_SMS")
    @BusMethod
    public boolean send(String phone, String message);
}
