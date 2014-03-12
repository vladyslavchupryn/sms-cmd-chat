package ua.pp.chuprin.smssender.shared;

import org.alljoyn.bus.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class AllJoynHelper {
    protected static final Logger LOG = LoggerFactory.getLogger(AllJoynHelper.class);

    public static void check(Status status, Object ... s) throws TransportException {
        String message;
        if(s.length == 0) {
            message = status.name();
        } else if(s.length == 1) {
            message = status.name() + " : " + s[0];
        } else {
            message = status.name() + " : " + String.format((String) s[0], Arrays.copyOfRange(s, 1, s.length));
        }
        if (status == Status.OK) {
            LOG.debug(message);
        } else {
            LOG.error(message);
            throw new TransportException();
        }
    }
}
