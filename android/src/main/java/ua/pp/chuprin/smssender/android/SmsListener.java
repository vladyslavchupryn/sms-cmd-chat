package ua.pp.chuprin.smssender.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import ua.pp.chuprin.smssender.shared.SmsReceiver;

import java.util.ArrayList;
import java.util.List;

public class SmsListener extends BroadcastReceiver {

    private static List<SmsReceiver> receivers = new ArrayList<SmsReceiver>();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                SmsMessage[] smsMessages= new SmsMessage[pdus.length];
                for (int i = 0; i < smsMessages.length; i++) {
                    smsMessages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);

                    String from = smsMessages[i].getOriginatingAddress();
                    String body = smsMessages[i].getMessageBody();

                    for (SmsReceiver receiver : receivers) {
                        receiver.receive(from, body);
                    }

                }
            }
        }
    }

    public static void addReceiver(SmsReceiver connected) {
        receivers.add(connected);
    }
}
