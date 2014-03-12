package ua.pp.chuprin.smssender.android;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.telephony.SmsManager;
import org.alljoyn.bus.BusObject;
import ua.pp.chuprin.smssender.shared.SmsReceiver;
import ua.pp.chuprin.smssender.shared.SmsSender;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SmsSenderImpl implements SmsSender, BusObject {

    private final ContentResolver contentResolver;

    public SmsSenderImpl(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    @Override
    public boolean send(String phone, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phone, null, message, null, null);

        ContentValues sentSms = new ContentValues();
        sentSms.put("address", phone);
        sentSms.put("body", message);
        contentResolver.insert(Uri.parse("content://sms/sent"), sentSms);

        return true;
    }
}
