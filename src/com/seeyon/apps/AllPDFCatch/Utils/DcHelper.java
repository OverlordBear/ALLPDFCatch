
package com.seeyon.apps.AllPDFCatch.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.seeyon.ctp.common.log.CtpLogFactory;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.logging.Log;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithID;
import org.bouncycastle.crypto.signers.SM2Signer;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECPoint;

public class DcHelper {
    private static final int LENGTH_32 = 32;
    private static final int USERID_LEN = 16;
    private static final int CONNECT_TIMEOUT = 15000;
    private static final int READ_TIMEOUT = 60000;
    private static final int STATUS_OK = 200;
    private static Base64.Encoder encoder = Base64.getEncoder();
    private static Base64.Decoder decoder = Base64.getDecoder();
    private final String url;
    private final String uid;
    private final byte[] userId;
    private final String alg;
    private final byte[] privateKey;
    private final byte[] publicKey;
    private final byte[] symKey;
    private static final Log LOGGER = CtpLogFactory.getLog(DcHelper.class);

    public DcHelper(String url, String uid, String privateKey, String publicKey, String symKey) {
        LOGGER.info("进入了DChelper的构造方法");
        this.url = url;
        this.uid = uid;
        this.userId = getUserId(uid);
        this.alg = "SM";
        this.privateKey = decoder.decode(privateKey);
        this.publicKey = decoder.decode(publicKey);
        this.symKey = symKey.getBytes(StandardCharsets.UTF_8);
    }

    public String sendRequest(String data, String funcode) throws IOException, CryptoException, GeneralSecurityException {
        LOGGER.info("进入了sendrequest方法");
        JsonObject requestJson = (JsonObject)(new Gson()).fromJson(data, JsonObject.class);
        String source = recursiveKeySort(requestJson);
        byte[] signature = cmbSM2SignWithSM3(this.userId, this.privateKey, source.getBytes(StandardCharsets.UTF_8));
        requestJson.getAsJsonObject("signature").addProperty("sigdat", new String(encoder.encode(signature), StandardCharsets.UTF_8));
        String request = requestJson.toString();
        byte[] encryptRequest = cmbSM4Crypt(this.symKey, this.userId, request.getBytes(StandardCharsets.UTF_8), 1);
        String encryptedRequest = new String(encoder.encode(encryptRequest), StandardCharsets.UTF_8);
        HashMap<String, String> map = new HashMap();
        map.put("UID", this.uid);
        map.put("ALG", this.alg);
        map.put("DATA", URLEncoder.encode(encryptedRequest, StandardCharsets.UTF_8.displayName()));
        map.put("FUNCODE", funcode);
        String response = httpPost(this.url, map);
        if (response.startsWith("CDCServer:")) {
            if (response.contains("错误次数已达上限")) {
                LOGGER.error("错误次数已达上限666666666");
                throw new IOException("接口调用次数已达当日上限，请明天再试: " + response);
            } else {
                LOGGER.error("访问目标地址 " + this.url + " 失败:" + response);
                throw new IOException("访问目标地址 " + this.url + " 失败:" + response);
            }
        } else {
            response = new String(cmbSM4Crypt(this.symKey, this.userId, decoder.decode(response), 2), StandardCharsets.UTF_8);
            JsonObject responseJson = (JsonObject)(new Gson()).fromJson(response, JsonObject.class);
            JsonObject signatureJson = responseJson.getAsJsonObject("signature");
            String responseSignature = signatureJson.get("sigdat").getAsString();
            signatureJson.addProperty("sigdat", "__signature_sigdat__");
            responseJson.add("signature", signatureJson);
            String responseSorted = recursiveKeySort(responseJson);
            boolean verify = cmbSM2VerifyWithSM3(this.userId, this.publicKey, responseSorted.getBytes(StandardCharsets.UTF_8), decoder.decode(responseSignature));
            if (!verify) {
                throw new IOException("响应报文的签名无效");
            } else {
                LOGGER.info("sendrequest发送完成");
                return response;
            }
        }
    }

    private static String recursiveKeySort(JsonObject json) {
        StringBuilder appender = new StringBuilder();
        appender.append("{");
        Iterator<String> keys = (new TreeSet(json.keySet())).iterator();

        for(boolean isFirstEle = true; keys.hasNext(); isFirstEle = false) {
            if (!isFirstEle) {
                appender.append(",");
            }

            String key = (String)keys.next();
            Object val = json.get(key);
            if (val instanceof JsonObject) {
                appender.append("\"").append(key).append("\":");
                appender.append(recursiveKeySort((JsonObject)val));
            } else if (!(val instanceof JsonArray)) {
                String value = val.toString();
                appender.append("\"").append(key).append("\":").append(value);
            } else {
                JsonArray jarray = (JsonArray)val;
                appender.append("\"").append(key).append("\":[");
                boolean isFirstArrEle = true;

                for(int i = 0; i < jarray.size(); ++i) {
                    if (!isFirstArrEle) {
                        appender.append(",");
                    }

                    Object obj = jarray.get(i);
                    if (obj instanceof JsonObject) {
                        appender.append(recursiveKeySort((JsonObject)obj));
                    } else {
                        appender.append(obj.toString());
                    }

                    isFirstArrEle = false;
                }

                appender.append("]");
            }
        }

        appender.append("}");
        return appender.toString();
    }

    private static byte[] getUserId(String uid) {
        return (uid + "0000000000000000").substring(0, 16).getBytes();
    }

    private static String httpPost(String httpUrl, Map<String, String> param) throws IOException, GeneralSecurityException {
        HttpURLConnection connection = null;

        String result;
        try {
            URL url = new URL(httpUrl);
            SSLContext sslcontext = SSLContext.getInstance("SSL");
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore)null);
            X509TrustManager defaultTm = null;

            for(TrustManager tm : tmf.getTrustManagers()) {
                if (tm instanceof X509TrustManager) {
                    defaultTm = (X509TrustManager)tm;
                    break;
                }
            }

            sslcontext.init((KeyManager[])null, new TrustManager[]{defaultTm}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(60000);
            connection.setInstanceFollowRedirects(true);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            OutputStream os = connection.getOutputStream();
            Throwable var32 = null;

            try {
                os.write(createLinkString(param).getBytes());
                if (connection.getResponseCode() != 200) {
                    InputStream is = connection.getErrorStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                    StringBuilder sbf = new StringBuilder();

                    String temp;
                    while((temp = br.readLine()) != null) {
                        sbf.append(temp);
                        sbf.append("\r\n");
                    }

                    result = sbf.toString();
                    br.close();
                    is.close();
                } else {
                    InputStream is = connection.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                    StringBuilder sbf = new StringBuilder();

                    String temp;
                    while((temp = br.readLine()) != null) {
                        sbf.append(temp);
                    }

                    result = sbf.toString();
                    br.close();
                    is.close();
                }
            } catch (Throwable var28) {
                var32 = var28;
                throw var28;
            } finally {
                if (os != null) {
                    if (var32 != null) {
                        try {
                            os.close();
                        } catch (Throwable var27) {
                            var32.addSuppressed(var27);
                        }
                    } else {
                        os.close();
                    }
                }

            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }

        }

        return result;
    }

    private static String createLinkString(Map<String, String> params) {
        ArrayList<String> keys = new ArrayList(params.keySet());
        Collections.sort(keys);
        StringBuilder prestr = new StringBuilder();

        for(int i = 0; i < keys.size(); ++i) {
            String key = (String)keys.get(i);
            String value = (String)params.get(key);
            if (i == keys.size() - 1) {
                prestr.append(key).append("=").append(value);
            } else {
                prestr.append(key).append("=").append(value).append("&");
            }
        }

        return prestr.toString();
    }

    private static byte[] cmbSM2SignWithSM3(byte[] id, byte[] privkey, byte[] msg) throws IOException, CryptoException {
        if (privkey != null && msg != null) {
            ECPrivateKeyParameters privateKey = encodePrivateKey(privkey);
            SM2Signer signer = new SM2Signer();
            ParametersWithID parameters = new ParametersWithID(privateKey, id);
            signer.init(true, parameters);
            signer.update(msg, 0, msg.length);
            return decodeDERSignature(signer.generateSignature());
        } else {
            throw new CryptoException("CMBSM2SignWithSM3 input error");
        }
    }

    private static ECPrivateKeyParameters encodePrivateKey(byte[] value) {
        BigInteger d = new BigInteger(1, value);
        ECParameterSpec spec = ECNamedCurveTable.getParameterSpec("sm2p256v1");
        ECDomainParameters ecParameters = new ECDomainParameters(spec.getCurve(), spec.getG(), spec.getN(), spec.getH(), spec.getSeed());
        return new ECPrivateKeyParameters(d, ecParameters);
    }

    private static byte[] decodeDERSignature(byte[] signature) throws IOException {
        ASN1InputStream stream = new ASN1InputStream(new ByteArrayInputStream(signature));
        ASN1Sequence primitive = (ASN1Sequence)stream.readObject();
        Enumeration<ASN1Integer> enumeration = primitive.getObjects();
        BigInteger intR = ((ASN1Integer)enumeration.nextElement()).getValue();
        BigInteger intS = ((ASN1Integer)enumeration.nextElement()).getValue();
        byte[] bytes = new byte[64];
        byte[] r = format(intR.toByteArray());
        byte[] s = format(intS.toByteArray());
        System.arraycopy(r, 0, bytes, 0, 32);
        System.arraycopy(s, 0, bytes, 32, 32);
        return bytes;
    }

    private static byte[] format(byte[] value) {
        if (value.length == 32) {
            return value;
        } else {
            byte[] bytes = new byte[32];
            if (value.length > 32) {
                System.arraycopy(value, value.length - 32, bytes, 0, 32);
            } else {
                System.arraycopy(value, 0, bytes, 32 - value.length, value.length);
            }

            return bytes;
        }
    }

    private static byte[] cmbSM4Crypt(byte[] key, byte[] iv, byte[] input, int mode) throws GeneralSecurityException {
        SecretKeySpec spec = new SecretKeySpec(key, "SM4");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance("SM4/CBC/PKCS7Padding", "BC");
        cipher.init(mode, spec, ivParameterSpec);
        return cipher.doFinal(input);
    }

    private static boolean cmbSM2VerifyWithSM3(byte[] id, byte[] pubkey, byte[] msg, byte[] signature) throws IOException {
        if (pubkey != null && msg != null && signature != null) {
            ECPublicKeyParameters publicKey = encodePublicKey(pubkey);
            SM2Signer signer = new SM2Signer();
            ParametersWithID parameters = new ParametersWithID(publicKey, id);
            signer.init(false, parameters);
            signer.update(msg, 0, msg.length);
            return signer.verifySignature(encodeDERSignature(signature));
        } else {
            throw new IllegalArgumentException("CMBSM2VerifyWithSM3 input error");
        }
    }

    private static ECPublicKeyParameters encodePublicKey(byte[] value) {
        byte[] x = new byte[32];
        byte[] y = new byte[32];
        System.arraycopy(value, 1, x, 0, 32);
        System.arraycopy(value, 33, y, 0, 32);
        BigInteger intX = new BigInteger(1, x);
        BigInteger intY = new BigInteger(1, y);
        ECParameterSpec spec = ECNamedCurveTable.getParameterSpec("sm2p256v1");
        ECPoint intQ = spec.getCurve().createPoint(intX, intY);
        ECDomainParameters ecParameters = new ECDomainParameters(spec.getCurve(), spec.getG(), spec.getN(), spec.getH(), spec.getSeed());
        return new ECPublicKeyParameters(intQ, ecParameters);
    }

    private static byte[] encodeDERSignature(byte[] signature) throws IOException {
        byte[] r = new byte[32];
        byte[] s = new byte[32];
        System.arraycopy(signature, 0, r, 0, 32);
        System.arraycopy(signature, 32, s, 0, 32);
        ASN1EncodableVector vector = new ASN1EncodableVector();
        vector.add(new ASN1Integer(new BigInteger(1, r)));
        vector.add(new ASN1Integer(new BigInteger(1, s)));
        return (new DERSequence(vector)).getEncoded();
    }
}
