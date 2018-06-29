package ru.mpei.mpei_pk;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Base64;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;

//Шифрование RSA. Используется размер ключа 2048 бит.
//Реализованы функции: шифрования, дешифровки, подписи, проверки подписи, генерации ключей.
//Ключи передаются в байтах, так как хранятся в формате Base64, и с помощью функций
//getPublicKeyFromBytes и getPrivateKeyFromBytes воссоздаются из байтов.
//Кодировка ISO-8859-1 используется для корректного преобразования массива байтов в строку и обратно

public class RsaEncryption {
    private final String algorithm = "RSA/ECB/PKCS1Padding";
    public static final byte PUBLIC_KEY = 0;
    public static final byte PRIVATE_KEY = 1;
    //Шифрование, результат - строка в формате ISO-8859-1.
    public String encrypt(String plainText, byte[] publicKeyBytes) throws Exception{
        //Создание сущности открытого ключа
        PublicKey publicKey = getPublicKeyFromBytes(publicKeyBytes);

        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] cipherBytes = cipher.doFinal(plainText.getBytes("UTF-8"));

        return new String(cipherBytes, "ISO-8859-1");

    }
    //Дешифровка, результат - строка в формате UTF-8.
    public String decrypt(String cipherText, byte[] privateKeyBytes) throws Exception{
        //Создание сущности закрытого ключа
        PrivateKey privateKey = getPrivateKeyFromBytes(privateKeyBytes);

        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        //byte[] plainBytes = cipher.doFinal(Base64.decode(cipherText, Base64.DEFAULT));
        byte[] plainBytes = cipher.doFinal(cipherText.getBytes("ISO-8859-1"));
        return new String(plainBytes, "UTF-8");
    }
    //Подпись, результат - строка в формате ISO-8859-1.
    public String sign(String plainText, byte[] privateKeyBytes) throws Exception{
        //Создание сущности закрытого ключа
        PrivateKey privateKey = getPrivateKeyFromBytes(privateKeyBytes);

        Signature privateSignature = Signature.getInstance("SHA1withRSA");
        privateSignature.initSign(privateKey);
        privateSignature.update(plainText.getBytes("UTF-8"));
        return new String(privateSignature.sign(), "ISO-8859-1");
    }
    //Проверка подписи
    public boolean verify(String plainText, String signature, byte[] publicKeyStrBytes) throws Exception{
        //Создание сущности открытого ключа
        PublicKey publicKey = getPublicKeyFromBytes(publicKeyStrBytes);

        Signature publicSignature = Signature.getInstance("SHA1withRSA");
        publicSignature.initVerify(publicKey);
        publicSignature.update(plainText.getBytes("UTF-8"));
        byte[] signatureBytes = signature.getBytes("ISO-8859-1");
        return publicSignature.verify(signatureBytes);
    }
    private PublicKey getPublicKeyFromBytes(byte[] publicKeyBytes) throws Exception{
        X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(X509publicKey);
    }
    private PrivateKey getPrivateKeyFromBytes(byte[] privateKeyBytes) throws Exception{
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);
    }
    //Генерация ключей, используется ассоциативный массив для возвращения результата для удобства.
    //Ключи возвращаются в формате Base64.
    public Map <String, String> generateKeys()throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair kp = keyGen.generateKeyPair();

        String privateKey = Base64.encodeToString(kp.getPrivate().getEncoded(), Base64.DEFAULT).trim();
        String publicKey = Base64.encodeToString(kp.getPublic().getEncoded(), Base64.DEFAULT).trim();

        Map<String, String> map = new HashMap<String, String>();
        map.put("privateKey", privateKey);
        map.put("publicKey", publicKey);
        return map;
    }
    //Формирование PEM сертификата из ключа в формате Base64.
    @NonNull
    public static String makePemCertificate(String key, byte type){
        String str = key;
        str = str.replaceAll("\n", "");
        str = str.replaceAll("(.{64})", "$1|");
        String[] ss = str.split("\\|");
        str = TextUtils.join("\r\n", ss);

        switch (type){
            case PUBLIC_KEY:
                return "-----BEGIN PUBLIC KEY-----\r\n" + str + "\r\n-----END PUBLIC KEY-----\r\n";
            case PRIVATE_KEY:
                return "-----BEGIN PRIVATE KEY-----\r\n" + str + "\r\n-----END PRIVATE KEY-----\r\n";
        }
        return key;
    }
    //Формирование ключа в формате Base64 из PEM сертификата.
    @NonNull
    public static String getKeyFromPemCertificate(String cert) {
        String str = cert.replaceAll("-----.*?-----", "");
        str = str.replaceAll("\r\n", "");
        str = str.replaceAll("(.{76})", "$1|");
        String[] ss = str.split("\\|");
        str = TextUtils.join("\n", ss);
        return str.trim() + '\n';
    }
}