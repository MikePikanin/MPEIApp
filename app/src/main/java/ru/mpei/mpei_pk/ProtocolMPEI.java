package ru.mpei.mpei_pk;

import android.content.Context;
import android.content.SharedPreferences;

import android.util.Base64;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import de.ailis.pherialize.MixedArray;
import de.ailis.pherialize.Pherialize;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProtocolMPEI {
    private Context context;
    private static final int QUICK_AUTH = 1;
    private static final int USUAL_AUTH = 2;
    private static final int TICKET_AUTH = 3;
    private final static String authURL = "https://www.pkmpei.ru/mobile/auth.php";
    private final static String regURL = "https://www.pkmpei.ru/mobile/reg.php";

    private static OkHttpClient client = new OkHttpClient();

    public ProtocolMPEI(Context context)
    {
        this.context = context;
    }

    public boolean quickAuth()
    {
        SharedPreferences sharedPref = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String nickname = sharedPref.getString("nickname", null);
        String privateKeyStr = sharedPref.getString("mobilePrivateKey", null);
        RsaEncryption rsaEnc = new RsaEncryption();
        JSONObject jsonObj = new JSONObject();
        try {
            String sign_data = Integer.toString(QUICK_AUTH) + nickname + FirebaseInstanceId.getInstance().getToken();
            String sign = rsaEnc.sign(sign_data, Base64.decode(privateKeyStr, Base64.DEFAULT));
            sign = Base64.encodeToString(sign.getBytes("ISO-8859-1"), Base64.DEFAULT);

            jsonObj.put("method", QUICK_AUTH);
            jsonObj.put("nic", nickname);
            jsonObj.put("sign", sign);

            String data = jsonObj.toString();

            RequestBody formBody = new FormBody.Builder()
                    .add("param", data)
                    .build();
            Request request = new Request.Builder()
                    .url(authURL)
                    .post(formBody)
                    .build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String result = response.body().string();

                jsonObj = new JSONObject(result);
                String status = jsonObj.getString("status");

                sign = jsonObj.getString("sign");
                sign = new String(Base64.decode(sign, Base64.DEFAULT), "ISO-8859-1");
                sign_data = status + jsonObj.getString("padding");

                if (status.startsWith("Authorized") && rsaEnc.verify(sign_data, sign, Base64.decode(context.getResources().getString(R.string.siteCommonPubKey), Base64.DEFAULT))) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("isAuthorizedUser", true);
                    editor.apply();

                    return true;
                }
            }
        } catch (Exception e) {
            Log.e("ProtocolMPEI", e.getMessage());
        }

        return false;
    }

    public String ticketAuth()
    {
        SharedPreferences sharedPref = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String nickname = sharedPref.getString("nickname", null);
        String privateKeyStr = sharedPref.getString("mobilePrivateKey", null);

        RsaEncryption rsaEnc = new RsaEncryption();
        JSONObject jsonObj = new JSONObject();
        try {
            String sign_data = Integer.toString(TICKET_AUTH) + nickname + FirebaseInstanceId.getInstance().getToken();
            String sign = rsaEnc.sign(sign_data, Base64.decode(privateKeyStr, Base64.DEFAULT));
            sign = Base64.encodeToString(sign.getBytes("ISO-8859-1"), Base64.DEFAULT);

            jsonObj.put("method", TICKET_AUTH);
            jsonObj.put("nic", nickname);
            jsonObj.put("sign", sign);

            String data = jsonObj.toString();

            OkHttpClient client = new OkHttpClient();

            RequestBody formBody = new FormBody.Builder()
                    .add("param", data)
                    .build();
            Request request = new Request.Builder()
                    .url(authURL)
                    .post(formBody)
                    .build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String result = response.body().string();

                jsonObj = new JSONObject(result);
                String status = jsonObj.getString("status");
                String ticket = jsonObj.getString("ticket");

                sign = jsonObj.getString("sign");
                sign = new String(Base64.decode(sign, Base64.DEFAULT), "ISO-8859-1");
                sign_data = status + jsonObj.getString("padding") + ticket;

                if (status.startsWith("Authorized") && rsaEnc.verify(sign_data, sign, Base64.decode(context.getResources().getString(R.string.siteCommonPubKey), Base64.DEFAULT))) {
                    return ticket;
                }
            }
        } catch (Exception e) {
            Log.e("ProtocolMPEI", e.getMessage());
        }
        return null;
    }

    public boolean auth(String nickname, String password)
    {
        SharedPreferences sharedPref = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String savedNickname = sharedPref.getString("nickname", null);
        RsaEncryption rsaEnc = new RsaEncryption();
        JSONObject jsonObj = new JSONObject();
        String publicKeyStr = null;
        String privateKeyStr = null;

        if (savedNickname != null && savedNickname.equals(nickname)) {
            //Если есть сохраненный ник и он совпадает с введеным, то попытка извлечь ключи из памяти.
            publicKeyStr = sharedPref.getString("mobilePublicKey", null);
            privateKeyStr = sharedPref.getString("mobilePrivateKey", null);
        }
        try {
            if (publicKeyStr == null || privateKeyStr == null) {
                //Генерация ключей, если нет сохраненных в памяти.
                Map<String, String> keyMap = rsaEnc.generateKeys();
                publicKeyStr = keyMap.get("publicKey");
                privateKeyStr = keyMap.get("privateKey");
            }
            //Подготовка сертификата из открытого ключа для отправки на сервер.
            String publicKey = RsaEncryption.makePemCertificate(publicKeyStr, RsaEncryption.PUBLIC_KEY);

            jsonObj.put("method", USUAL_AUTH);
            jsonObj.put("nic", nickname);
            jsonObj.put("mobilePubKey", Base64.encodeToString(publicKey.getBytes("ISO-8859-1"), Base64.DEFAULT));
            jsonObj.put("password", password);
            jsonObj.put("token", FirebaseInstanceId.getInstance().getToken());

            String data = jsonObj.toString();
            //Отправка запроса и получение результата.

            RequestBody formBody = new FormBody.Builder()
                    .add("param", data)
                    .build();
            Request request = new Request.Builder()
                    .url(authURL)
                    .post(formBody)
                    .build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String result = response.body().string();

                jsonObj = new JSONObject(result);
                String status = jsonObj.getString("status");
                String userId = jsonObj.getString("userId");
                String sitePubKey = jsonObj.getString("userPublicKey");
                sitePubKey = new String(Base64.decode(sitePubKey, Base64.DEFAULT));
                String sign = jsonObj.getString("sign");
                sign = new String(Base64.decode(sign, Base64.DEFAULT), "ISO-8859-1");
                String sign_data = sitePubKey + userId + nickname + status;

                if (status.startsWith("Authorized") && rsaEnc.verify(sign_data, sign, Base64.decode(context.getResources().getString(R.string.siteCommonPubKey), Base64.DEFAULT))) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("isAuthorizedUser", true);
                    editor.putString("userId", userId);
                    editor.putString("nickname", nickname);
                    editor.putString("sitePublicKey", RsaEncryption.getKeyFromPemCertificate(sitePubKey));
                    editor.putString("mobilePublicKey", publicKeyStr);
                    editor.putString("mobilePrivateKey", privateKeyStr);
                    editor.apply();

                    return true;
                }
            }
        } catch (Exception e) {
            Log.e("ProtocolMPEI", e.getMessage());
        }
        return false;
    }
    public boolean exit() {
        SharedPreferences sharedPref = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String nickname = sharedPref.getString("nickname", null);
        String privateKeyStr = sharedPref.getString("mobilePrivateKey", null);
        RsaEncryption rsaEnc = new RsaEncryption();
        JSONObject jsonObj = new JSONObject();
        try {
            String sign_data = Integer.toString(5) + nickname + FirebaseInstanceId.getInstance().getToken();
            String sign = rsaEnc.sign(sign_data, Base64.decode(privateKeyStr, Base64.DEFAULT));
            sign = Base64.encodeToString(sign.getBytes("ISO-8859-1"), Base64.DEFAULT);

            jsonObj.put("method", 5);
            jsonObj.put("nic", nickname);
            jsonObj.put("sign", sign);

            String data = jsonObj.toString();

            RequestBody formBody = new FormBody.Builder()
                    .add("param", data)
                    .build();
            Request request = new Request.Builder()
                    .url(authURL)
                    .post(formBody)
                    .build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String result = response.body().string();

                jsonObj = new JSONObject(result);
                String status = jsonObj.getString("status");

                sign = jsonObj.getString("sign");
                sign = new String(Base64.decode(sign, Base64.DEFAULT), "ISO-8859-1");
                sign_data = status + jsonObj.getString("padding");

                if (status.startsWith("Authorized") && rsaEnc.verify(sign_data, sign, Base64.decode(context.getResources().getString(R.string.siteCommonPubKey), Base64.DEFAULT))) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.remove("userId");
                    editor.remove("nickname");
                    editor.putBoolean("isAuthorizedUser", false);
                    editor.apply();

                    return true;
                }
            }
        } catch (Exception e) {
            Log.e("ProtocolMPEI", e.getMessage());
        }

        return false;
    }

    public HashMap<String,String> reg(Map<String, String> userInfo) {
        RsaEncryption rsaEnc = new RsaEncryption();
        String publicKeyStr, privateKeyStr, publicKey;
        JSONObject jsonObj = new JSONObject();
        try {
            Map<String, String> keyMap = rsaEnc.generateKeys();
            publicKeyStr = keyMap.get("publicKey");
            privateKeyStr = keyMap.get("privateKey");

            publicKey = RsaEncryption.makePemCertificate(publicKeyStr, RsaEncryption.PUBLIC_KEY);
            publicKey = Base64.encodeToString(publicKey.getBytes("ISO-8859-1"), Base64.DEFAULT);

            jsonObj.put("method", 1);
            jsonObj.put("mobilePubKey", publicKey);
            jsonObj.put("token", FirebaseInstanceId.getInstance().getToken());
            for (Map.Entry<String, String> entry : userInfo.entrySet()) {
                jsonObj.put(entry.getKey(), entry.getValue());
            }

            String data = jsonObj.toString();

            RequestBody formBody = new FormBody.Builder()
                    .add("param", data)
                    .build();
            Request request = new Request.Builder()
                    .url(regURL)
                    .post(formBody)
                    .build();
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                String result = response.body().string();

                jsonObj = new JSONObject(result);
                String status = jsonObj.getString("status");
                String sitePubKey = jsonObj.getString("userPublicKey");
                sitePubKey = new String(Base64.decode(sitePubKey, Base64.DEFAULT), "ISO-8859-1");
                String userId = jsonObj.getString("userId");
                String userNic = jsonObj.getString("userNic");
                String userPwd = jsonObj.getString("userPwd");
                String sign = jsonObj.getString("sign");
                sign = new String(Base64.decode(sign, Base64.DEFAULT), "ISO-8859-1");

                if (status.startsWith("Registered")) {
                    String sign_data = sitePubKey + status + userNic + userPwd + userId;
                    if (rsaEnc.verify(sign_data, sign, Base64.decode(context.getResources().getString(R.string.siteCommonPubKey), Base64.DEFAULT))) {
                        SharedPreferences sharedPref = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean("isAuthorizedUser", true);
                        editor.putString("userId", userId);
                        editor.putString("nickname", userNic);
                        editor.putString("sitePublicKey", RsaEncryption.getKeyFromPemCertificate(sitePubKey));
                        editor.putString("mobilePublicKey", publicKeyStr);
                        editor.putString("mobilePrivateKey", privateKeyStr);
                        editor.apply();

                        HashMap<String,String> info = new HashMap<>();
                        info.put("userNic", userNic);
                        info.put("userPwd", userPwd);
                        return info;
                    }
                }
            }
        } catch (Exception e) {
            Log.e("ProtocolMPEI", e.getMessage());
        }
        return null;
    }
    public String get_reserve_room(String VisitType, String EducationLevel, String OnlyPayForm) {
        try {
            RequestBody formBody = new FormBody.Builder()
                    .add("VisitType", VisitType)
                    .add("EducationLevel", EducationLevel)
                    .add("OnlyPayForm", OnlyPayForm)
                    .add("Intervals", Integer.toString(1))
                    .build();
            Request request = new Request.Builder()
                    .url("https://www.pkmpei.ru/ajax/queue/pk_get_reserve_room.php")
                    .post(formBody)
                    .build();
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                return response.body().string();
            }
            else {
                return null;
            }
        } catch (Exception e) {
            Log.e("ProtocolMPEI", e.getMessage());
            return null;
        }
    }
    public String get_queue_load() {
        try {
            Request request = new Request.Builder()
                    .url("https://www.pkmpei.ru/mobile/get_queue_load.php")
                    .build();
            SharedPreferences sharedPref = context.getSharedPreferences("savedState", Context.MODE_PRIVATE);
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("queueLoad", response.body().string());
                    editor.apply();
                    return response.body().string();
                }
                else {
                    return sharedPref.getString("queueLoad", null);
                }
            } catch (Exception e) {
                return sharedPref.getString("queueLoad", null);
            }
        } catch (Exception e) {
            Log.e("ProtocolMPEI", e.getMessage());
            return null;
        }
    }

    public String get_news(String token) {
        try {
            RequestBody formBody = new FormBody.Builder()
                    .add("token", token)
                    .build();
            Request request = new Request.Builder()
                    .url("https://www.pkmpei.ru/mobile/get_news.php")
                    .post(formBody)
                    .build();
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                String answer = response.body().string();
                if (answer.startsWith("OK")) {
                    String[] ar = answer.split("&", 4);
                    String sign_data = ar[3] + ar[1];
                    String sign = new String(Base64.decode(ar[2], Base64.DEFAULT), "ISO-8859-1");
                    RsaEncryption rsaEnc = new RsaEncryption();
                    if (rsaEnc.verify(sign_data, sign, Base64.decode(context.getResources().getString(R.string.siteCommonPubKey), Base64.DEFAULT))) {
                        return ar[3];
                    }
                }
            }
            return null;

        } catch (Exception e) {
            Log.e("ProtocolMPEI", e.getMessage());
            return null;
        }
    }

    public String get_queue_numbers() {
        try {
            Request request = new Request.Builder()
                    .url("https://www.pkmpei.ru/mobile/get_queue_numbers.php")
                    .build();
            SharedPreferences sharedPref = context.getSharedPreferences("savedState", Context.MODE_PRIVATE);
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("queueNumbers", response.body().string());
                    editor.apply();
                    return response.body().string();
                }
                else {
                    return sharedPref.getString("queueNumbers", null);
                }
            } catch (Exception e) {
                return sharedPref.getString("queueNumbers", null);
            }

        } catch (Exception e) {
            Log.e("ProtocolMPEI", e.getMessage());
            return null;
        }
    }

    public String queue_reserve(String valueId, int type) {
        try {
            String cmd, keyId;
            if (type == 0) {
                cmd = "reserve";
                keyId = "TimeInterval";
            } else {
                cmd = "reject";
                keyId = "IdReserve";
            }

            RsaEncryption rsaEnc = new RsaEncryption();
            SharedPreferences sharedPref = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
            String userNic = sharedPref.getString("nickname", "");
            String userId = sharedPref.getString("userId", "");
            String privateKey = sharedPref.getString("mobilePrivateKey", "");

            String padding = Base64.encodeToString(new SecureRandom().generateSeed(128), Base64.DEFAULT);
            String sign_data = userId + userNic + cmd + valueId + FirebaseInstanceId.getInstance().getToken() + padding;
            String sign = rsaEnc.sign(sign_data, Base64.decode(privateKey, Base64.DEFAULT));
            sign = Base64.encodeToString(sign.getBytes("ISO-8859-1"), Base64.DEFAULT);

            RequestBody formBody = new FormBody.Builder()
                    .add("nic", userNic)
                    .add("cmd", cmd)
                    .add(keyId, valueId)
                    .add("padding", padding)
                    .add("sign", sign)
                    .build();

            Request request = new Request.Builder()
                    .url("https://www.pkmpei.ru/mobile/queue_reserve.php")
                    .post(formBody)
                    .build();
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                String answer = response.body().string();
                String[] ar;
                if (type == 0) {
                    if (answer.startsWith("RESERVED")) {
                        ar = answer.split("&");
                        sign_data = ar[0] + ar[1] + ar[2];
                        sign = new String(Base64.decode(ar[3], Base64.DEFAULT), "ISO-8859-1");
                        if (rsaEnc.verify(sign_data, sign, Base64.decode(context.getResources().getString(R.string.siteCommonPubKey), Base64.DEFAULT))) {
                            return "RESERVED&" + ar[1];
                        }
                    } else if (answer.startsWith("OVERLOAD")) {
                        return "OVERLOAD";
                    } else if (answer.startsWith("ALREADY")) {
                        return "ALREADY";
                    } else if (answer.startsWith("ERROR")) {
                        return "ERROR";
                    }
                } else if (type == 1){
                    if (answer.startsWith("REJECTED")) {
                         ar = answer.split("&");
                        sign_data = ar[0] + ar[1];
                        sign = new String(Base64.decode(ar[2], Base64.DEFAULT), "ISO-8859-1");
                        if (rsaEnc.verify(sign_data, sign, Base64.decode(context.getResources().getString(R.string.siteCommonPubKey), Base64.DEFAULT))) {
                            return "REJECTED";
                        }
                    } else if (answer.startsWith("ALREADY")) {
                        return "ALREADY";
                    }
                }
                return null;
            }
            else {
                return null;
            }
        } catch (Exception e) {
            Log.e("ProtocolMPEI", e.getMessage());
            return null;
        }
    }

    public HashMap<String, String> get_person_info() {
        try {
            RsaEncryption rsaEnc = new RsaEncryption();
            SharedPreferences sharedPref = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
            String userNic = sharedPref.getString("nickname", "");
            String userId = sharedPref.getString("userId", "");
            String privateKey = sharedPref.getString("mobilePrivateKey", "");

            String padding = Base64.encodeToString(new SecureRandom().generateSeed(128), Base64.DEFAULT);
            String sign_data = userId + userNic + FirebaseInstanceId.getInstance().getToken() + padding;
            String sign = rsaEnc.sign(sign_data, Base64.decode(privateKey, Base64.DEFAULT));
            sign = Base64.encodeToString(sign.getBytes("ISO-8859-1"), Base64.DEFAULT);

            RequestBody formBody = new FormBody.Builder()
                    .add("nic", userNic)
                    .add("padding", padding)
                    .add("sign", sign)
                    .build();

            Request request = new Request.Builder()
                    .url("https://www.pkmpei.ru/mobile/get_person_info.php")
                    .post(formBody)
                    .build();
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                String answer = response.body().string();
                if (answer.startsWith("OK")) {
                    String[] ar = answer.split("&");
                    sign = new String(Base64.decode(ar[2], Base64.DEFAULT), "ISO-8859-1");
                    sign_data = ar[3] + ar[1];

                    if (rsaEnc.verify(sign_data, sign, Base64.decode(context.getResources().getString(R.string.siteCommonPubKey), Base64.DEFAULT))) {
                        MixedArray personInfo = Pherialize.unserialize(ar[3]).toArray();
                        byte isForeigner = personInfo.getByte("isForeigner");
                        byte toMoscow = personInfo.getByte("toMoscow");
                        byte dataEntered = personInfo.getByte("DataEntered");

                        HashMap<String, String> map = new HashMap<>();
                        if (dataEntered == 1) {
                            if (toMoscow == 1) {
                                if (isForeigner == 0) {
                                    String visitType = personInfo.getChar("DocAccepted") == 'Y' ? "2" : "1";
                                    String eduLevel = personInfo.getString("TargetEducationLevel");
                                    String onlyPayForm = (personInfo.getByte("OnlyForm") == 1 || personInfo.getByte("OnlyPay") == 1) ? "1" : "0";
                                    String idReserve = personInfo.getString("IdReserve");

                                    map.put("VisitType", visitType);
                                    map.put("EducationLevel", eduLevel);
                                    map.put("OnlyPayForm", onlyPayForm);
                                    map.put("IdReserve", idReserve);

                                    int id_reserve;
                                    try {
                                        id_reserve = Integer.parseInt(idReserve);
                                    } catch (Exception e) {
                                        id_reserve = 0;
                                    }
                                    if (id_reserve > 0) {
                                        String txtStatus = personInfo.getString("ReserveStatus");
                                        map.put("ReserveStatus", txtStatus);
                                        if (txtStatus.equals("OK")) {
                                            String txtInterval = personInfo.getString("ReserveInterval");
                                            String txtEduLevel = personInfo.getString("ReserveEducationLevel");
                                            String txtVisitType = personInfo.getString("ReserveVisitType");
                                            map.put("ReserveInterval", txtInterval);
                                            map.put("ReserveEducationLevel", txtEduLevel);
                                            map.put("ReserveVisitType", txtVisitType);
                                        }
                                    }

                                } else {
                                    map.put("error", "Для иностранцев поставновка в очередь не производится");
                                }
                            } else {
                                map.put("error", "Резервирование возможно для подачи документов в Москве");
                            }
                        } else  {
                            map.put("error", "Не введены данные");
                        }
                        return map;
                    }
                }
                return null;
            }
            else {
                return null;
            }
        } catch (Exception e) {
            Log.e("ProtocolMPEI", e.getMessage());
            return null;
        }
    }
}