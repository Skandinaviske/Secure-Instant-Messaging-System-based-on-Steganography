package com.tencent.qcloud.presentation.event;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.tencent.imsdk.TIMCallBack;
import com.tencent.imsdk.TIMElem;
import com.tencent.imsdk.TIMElemType;
import com.tencent.imsdk.TIMFaceElem;
import com.tencent.imsdk.TIMImage;
import com.tencent.imsdk.TIMImageElem;
import com.tencent.imsdk.TIMImageType;
import com.tencent.imsdk.TIMManager;
import com.tencent.imsdk.TIMMessage;
import com.tencent.imsdk.TIMMessageListener;
import com.tencent.imsdk.TIMSoundElem;
import com.tencent.imsdk.TIMTextElem;
import com.tencent.imsdk.TIMUserConfig;
import com.tencent.imsdk.ext.message.TIMMessageLocator;
import com.tencent.imsdk.ext.message.TIMMessageRevokedListener;
import com.tencent.imsdk.ext.message.TIMUserConfigMsgExt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 消息通知事件，上层界面可以订阅此事件
 */
public class MessageEvent extends Observable implements TIMMessageListener, TIMMessageRevokedListener {

    /**
     * 非对称加密密钥算法
     */
    private static final String KEY_ALGORITHM = "DH";
    /**
     * 本地密钥算法，即对称加密密钥算法
     * 可选DES、DESede或者AES
     */
    private static final String SELECT_ALGORITHM = "AES";
    /**
     * 密钥长度
     */
    private static final int KEY_SIZE = 512;
    //公钥
    private static final String PUBLIC_KEY = "DHPublicKey";
    //私钥
    private static final String PRIVATE_KEY = "DHPrivateKey";

    /**
     * 初始化甲方密钥
     * @return Map 甲方密钥Map
     * @throws Exception
     */

    private volatile static MessageEvent instance;

    private MessageEvent(){
        //注册消息监听器
        TIMManager.getInstance().addMessageListener(this);


    }

    public TIMUserConfig init(TIMUserConfig config) {
        TIMUserConfigMsgExt ext = new TIMUserConfigMsgExt(config);
        ext.setMessageRevokedListener(this);
        return ext;
    }

    public static MessageEvent getInstance(){
        if (instance == null) {
            synchronized (MessageEvent.class) {
                if (instance == null) {
                    instance = new MessageEvent();
                }
            }
        }
        return instance;
    }

    public static byte[] readBytes(InputStream inputStream) throws IOException {
        // this dynamically extends to take the bytes you read
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        // this is storage overwritten on each iteration with bytes
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        // we need to know how may bytes were read to write them to the byteBuffer
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray();
    }

    public static String getStringByGet(String url){
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url)
                    .openConnection();
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
                byte[] data;
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    System.out.println("秦崔果皇");
                    InputStream in = conn.getInputStream();
                    byte[] a = readStream(in);
                    String ex = new String(Base64.encode(a, Base64.DEFAULT));
                    return ex;

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public boolean onNewMessages(List<TIMMessage> list) { // 接收消息

           for (TIMMessage item : list) {
               String result = "";
               if (item != null) {
                   List<TIMElem> elems = new ArrayList<>();
                   for (int i = 0; i < item.getElementCount(); ++i) {
                       elems.add(item.getElement(i));
                       if (item.getElement(i).getType() == TIMElemType.Text) {
                           TIMTextElem t = (TIMTextElem) item.getElement(i);
                           result += t.getText();
                       } else if (item.getElement(i).getType() == TIMElemType.Face) {
                           TIMFaceElem faceElem = (TIMFaceElem) item.getElement(i);
                           byte[] data = faceElem.getData();
                           if (data != null) {
                               result += new String(data, Charset.forName("UTF-8"));

                           }
                       } else if (item.getElement(i).getType() == TIMElemType.Image) {
                           TIMImageElem e = (TIMImageElem) item.getElement(i);
                           for (final TIMImage image : e.getImageList()) {

                               //final String uuid = image.getUuid()

                               Thread thread = new Thread() {
                                   @Override
                                   public void run() {
                                       String x=image.getUrl();
                                       String b64 = getStringByGet(x);;
                                       Log.d("bigmom",b64);
                                   }
                               };
                               thread.start();

                           }
                       }
                       else if (item.getElement(i).getType() == TIMElemType.Sound) {
                           TIMSoundElem s = (TIMSoundElem) item.getElement(i);
                           s.getSoundToFile("/storage/emulated/0/Android/data/com.tencent.qcloud.timchat/cache/tempAudios", new TIMCallBack() {
                               @Override
                               public void onError(int i, String s) {}

                               @Override
                               public void onSuccess() {}
                           });
                           final String path="/storage/emulated/0/Android/data/com.tencent.qcloud.timchat/cache/tempAudios";
                           Thread thread = new Thread() {
                               @Override
                               public void run() {
                                   File xing2=new File(path);
                                   InputStream in = null;
                                   try {
                                       in = new FileInputStream(xing2);
                                   } catch (FileNotFoundException e1) {
                                       e1.printStackTrace();
                                   }
                                   byte[] a = readStream(in);
                                   String ex = Base64.encodeToString(a,Base64.NO_WRAP);
                                   Log.d("bigmom",ex);
                               }
                           };
                           thread.start();

                       }
                   }
               }
               Log.d("bigmom",result);
               Log.d("bigmom","蛤蟆神功");
               setChanged();
               notifyObservers(item);
           }

        return false;
    }

    public static String encodeToBase64(Bitmap image, Bitmap.CompressFormat compressFormat, int quality)
    {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

    /**
     * 主动通知新消息
     */
    public void onNewMessage(TIMMessage message) throws Exception {
        String result = "";
        if (message != null) {
            List<TIMElem> elems = new ArrayList<>();
            for (int i = 0; i < message.getElementCount(); ++i) {
                elems.add(message.getElement(i));
                if (message.getElement(i).getType() == TIMElemType.Text) {
                    TIMTextElem t = (TIMTextElem)message.getElement(i);
                    result += t.getText();
                    final String[] finalResult = {result};
                    final int finalI = i;
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                Log.d("bigmom",ex(finalResult[finalI]));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    thread.start();

                    /*final String finalResult = result;
                    Thread thread = new Thread() {
                        @Override
                        public void run() {

                            Log.d("bigmom","what's up");
                            try {
                                initKey();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Log.d("bigmom","what happened");
                            byte[] encode1 = new byte[0];
                            try {
                                encode1 = encrypt(finalResult.getBytes(), key1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Log.d("bigmom","加密:\n" + Base64.encodeToString(encode1,Base64.NO_WRAP));
                            byte[] decode1 = new byte[0];
                            try {
                                decode1 = decrypt(encode1, key2);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            String output1 = new String(decode1);
                            Log.d("bigmom3","解密:\n" + output1);
                        }
                    };
                    thread.start();*/

                    /*Log.d("bigmom","what's up");
                    initKey();
                    Log.d("bigmom","what happened");
                    byte[] encode1 = encrypt(result.getBytes(), key1);
                    Log.d("bigmom","加密:\n" + Base64.encodeToString(encode1,Base64.NO_WRAP));
                    byte[] decode1 = decrypt(encode1, key2);
                    String output1 = new String(decode1);
                    Log.d("bigmom3","解密:\n" + output1);*/

                }
                else if (message.getElement(i).getType() == TIMElemType.Face) {
                    TIMFaceElem faceElem = (TIMFaceElem) message.getElement(i);
                    byte[] data = faceElem.getData();
                    if (data != null){
                        result += new String(data, Charset.forName("UTF-8"));
                    }
                }
                else if (message.getElement(i).getType() == TIMElemType.Image) {
                    final TIMImageElem e = (TIMImageElem) message.getElement(i);
                    final String path=e.getPath();
                        //final String uuid = image.getUuid()

                    Thread thread = new Thread() {
                            @Override
                            public void run() {

                                File xing=new File(path);
                                InputStream in = null;
                                try {
                                    in = new FileInputStream(xing);
                                } catch (FileNotFoundException e1) {
                                    e1.printStackTrace();
                                }
                                byte[] a = readStream(in);
                                String ex = Base64.encodeToString(a,Base64.NO_WRAP);
                                Log.d("bigmom",ex);
                            }
                        };
                        thread.start();


                }
                else if (message.getElement(i).getType() == TIMElemType.Sound) {
                    final TIMSoundElem s = (TIMSoundElem) message.getElement(i);
                    final String path=s.getPath();
                    Log.d("bigmom",path);

                        //final String uuid = image.getUuid()

                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            File xing2=new File(path);
                            InputStream in = null;
                            try {
                                in = new FileInputStream(xing2);
                            } catch (FileNotFoundException e1) {
                                e1.printStackTrace();
                            }
                            byte[] a = readStream(in);
                            String ex = Base64.encodeToString(a,Base64.NO_WRAP);
                            Log.d("bigmom",ex);
                            }
                        };
                        thread.start();

                }
            }
        }

        Log.d("bigmom2",result);
        Log.d("bigmom","火云邪神");
        System.out.println(result);
        setChanged();
        notifyObservers(message);

    }

    /**
     * 清理消息监听
     */
    public void clear(){
        instance = null;
    }

    @Override
    public void onMessageRevoked(TIMMessageLocator timMessageLocator) {
        setChanged();
        notifyObservers(timMessageLocator);
    }


    public static byte[] readStream(InputStream inStream) {
        try {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            outStream.close();
            inStream.close();
            return outStream.toByteArray();

        }catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /*public static Map<String, Object> initKeys() throws Exception{
        //实例化密钥对生成器
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        //初始化密钥对生成器
        keyPairGenerator.initialize(KEY_SIZE);
        //生成密钥对
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        //甲方公钥
        DHPublicKey publicKey = (DHPublicKey)keyPair.getPublic();
        //甲方私钥
        DHPrivateKey privateKey = (DHPrivateKey)keyPair.getPrivate();
        //将密钥对存储在Map中
        Map<String, Object> keyMap = new HashMap<String, Object>(2);
        keyMap.put(PUBLIC_KEY, publicKey);
        keyMap.put(PRIVATE_KEY, privateKey);
        return keyMap;

    }
    public static Map<String, Object> initKey(byte[] key) throws Exception{
        //解析甲方公钥
        //转换公钥材料
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(key);
        //实例化密钥工厂
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        //产生公钥
        PublicKey pubKey = keyFactory.generatePublic(x509KeySpec);
        //由甲方公钥构建乙方密钥
        DHParameterSpec dhParameterSpec = ((DHPublicKey)pubKey).getParams();
        //实例化密钥对生成器
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        //初始化密钥对生成器
        keyPairGenerator.initialize(KEY_SIZE);
        //产生密钥对
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        //乙方公钥
        DHPublicKey publicKey = (DHPublicKey) keyPair.getPublic();
        //乙方私约
        DHPrivateKey privateKey = (DHPrivateKey) keyPair.getPrivate();
        //将密钥对存储在Map中
        Map<String, Object> keyMap = new HashMap<String, Object>(2);
        keyMap.put(PUBLIC_KEY, publicKey);
        keyMap.put(PRIVATE_KEY, privateKey);
        return keyMap;
    }

    public static byte[] encrypt(byte[] data, byte[] key) throws Exception{
        //生成本地密钥
        SecretKey secretKey = new SecretKeySpec(key, SELECT_ALGORITHM);
        //数据加密
        Cipher cipher = Cipher.getInstance(secretKey.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }
    public static byte[] decrypt(byte[] data, byte[] key) throws Exception{
        //生成本地密钥
        SecretKey secretKey = new SecretKeySpec(key, SELECT_ALGORITHM);
        //数据揭秘
        Cipher cipher = Cipher.getInstance(secretKey.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }
    public static byte[] getSecretKey(byte[] publicKey, byte[] privateKey) throws Exception{
        //实例化密钥工厂
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        //初始化公钥
        //密钥材料转换
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKey);
        //产生公钥
        PublicKey pubKey = keyFactory.generatePublic(x509KeySpec);
        Log.d("bigmom4",pubKey.toString());

        //初始化私钥
        //密钥材料转换
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(privateKey);
        //产生私钥
        PrivateKey priKey = keyFactory.generatePrivate(pkcs8KeySpec);
        //实例化
        KeyAgreement keyAgree = KeyAgreement.getInstance(keyFactory.getAlgorithm());
        //初始化
        keyAgree.init(priKey);
        //keyAgree.init(pubKey);
        keyAgree.doPhase(pubKey, false);
        //生成本地密钥
        SecretKey secretKey = keyAgree.generateSecret(SELECT_ALGORITHM);
        return secretKey.getEncoded();
    }
    /**
     * 取得私钥
     * @param keyMap 密钥Map
     * @return byte[] 私钥
     * @throws Exception
     */
    /*public static byte[] getPrivateKey(Map<String, Object> keyMap) throws Exception{
        Key key = (Key) keyMap.get(PRIVATE_KEY);
        return key.getEncoded();
    }

    /**
     * 取得公钥
     * @param keyMap 密钥Map
     * @return byte[] 公钥
     * @throws Exception
     */
   /* public static byte[] getPublicKey(Map<String, Object> keyMap) throws Exception{
        Key key = (Key) keyMap.get(PUBLIC_KEY);
        return key.getEncoded();
    }

    private static byte[] publicKey1;
    //甲方私钥
    private static byte[] privateKey1;
    //甲方本地密钥
    private static byte[] key1;
    //乙方公钥
    private static byte[] publicKey2;
    //乙方私钥
    private static byte[] privateKey2;
    //乙方本地密钥
    private static byte[] key2;


    public static final void initKey() throws Exception{
        //生成甲方密钥对
        Map<String, Object> keyMap1 = initKeys();
        publicKey1 = getPublicKey(keyMap1);
        privateKey1 = getPrivateKey(keyMap1);
        Log.d("bigmom3","甲方公钥:\n" + Base64.encodeToString(publicKey1,Base64.NO_WRAP));
        Log.d("bigmom3","甲方私钥:\n" + Base64.encodeToString(privateKey1,Base64.NO_WRAP));
        //由甲方公钥产生本地密钥对
        Map<String, Object> keyMap2 = initKey(publicKey1);
        publicKey2 = getPublicKey(keyMap2);
        privateKey2 = getPrivateKey(keyMap2);
        Log.d("bigmom3","乙方公钥:\n" + Base64.encodeToString(publicKey2,Base64.NO_WRAP));
        Log.d("bigmom3","乙方私钥:\n" + Base64.encodeToString(privateKey2,Base64.NO_WRAP));
        key1 = getSecretKey(publicKey2, privateKey1);
        //Log.d("bigmom3","甲方本地密钥:\n" + Base64.encodeToString(key1,Base64.NO_WRAP));
        key2 = getSecretKey(publicKey1, privateKey2);
        //Log.d("bigmom3","乙方本地密钥:\n" + Base64.encodeToString(key2,Base64.NO_WRAP));
        Log.d("bigmom3","测试一下");
    } */
    public String ex(String lzf) throws Exception
    {
        HQDH dh = HQDH.getInstance();
        HQBase64 base64 = HQBase64.getInstance();

        String ce = "";
        byte[] data = lzf.getBytes();
        HQKeyPair keyPairA = dh.initPartyAKey();
        Log.d("bigmom甲方私钥：", base64.encodeToString(keyPairA.getPrivateKey()));
        Log.d("bigmom甲方公钥：" , base64.encodeToString(keyPairA.getPublicKey()));
        HQKeyPair keyPairB = dh.initPartyBKey(keyPairA.getPublicKey());
        Log.d("bigmom乙方私钥：" , base64.encodeToString(keyPairB.getPrivateKey()));
        Log.d("bigmom乙方公钥：" , base64.encodeToString(keyPairB.getPublicKey()));

        HQDH.HQDHSymmetricalAlgorithm[] algorithms = HQDH.HQDHSymmetricalAlgorithm.values();
        for (HQDH.HQDHSymmetricalAlgorithm algorithm : algorithms)
        {
            System.err.println("=========================================");
            System.err.println(algorithm);
            byte[] result = dh.encrypt(data, keyPairB.getPublicKey(), keyPairA.getPrivateKey(), algorithm);
            Log.d("bigmom加密：", base64.encodeToString(result));
            Log.d("bigmom解密：",new String(dh.decrypt(result, keyPairA.getPublicKey(), keyPairB.getPrivateKey(), algorithm)));
            ce=new String(dh.decrypt(result, keyPairA.getPublicKey(), keyPairB.getPrivateKey(), algorithm));
        }
        return ce;
    }

}
