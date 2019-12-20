import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


//import sun.misc.BASE64Decoder;
//import sun.misc.BASE64Encoder;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
/**
 * <p>Title:</p>
 * <p>Description: 明文信息加解密类 </p>
 *
 * @author Abdul.Wu
 * @version 1.00
 * <pre>
 * 修改记录:
 * 修改后版本			修改人				修改内容
 * 201510908.1		Abdul.Wu			创建
 * 20151023.1		Abdul.Wu			指定随机加密算法，兼容Linux解密报错问题
 * </pre>
 */
public class EncryptServices {
	
	private final static String ENCRYPT_KEY = "Y29tLnNpbm9zZXJ2aWNlcy5vcmc=";
	
	private final static Log log = LogFactory.getLog(EncryptServices.class);
	
	public String encrypt(String value){
		if( StringUtils.isEmpty(value) ){
			return null;
		}
		String result = aesEncrypt(value, ENCRYPT_KEY);
		return result;
	}
	
	public String decrypt(String value){
		if(  StringUtils.isEmpty(value) ){
			return null;
		}
		String result = aesDecrypt(value, ENCRYPT_KEY);
		return result;
	}
	/**
	 * 
	 * Description : AES加密+Base64转码
	 * @param value
	 * @param encryptKey
	 * @return
	 * @author Abdul
	 * CreateTime 20150907
	 */
	private String aesEncrypt(String value,String encryptKey){
		try {
			String key = getEncryptKey(encryptKey);
			if( null == key ){
				return null;
			}
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			//20151023.1 Abdul.Wu 指定随机算法，解决Linux上解密问题
			SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG" );
            secureRandom.setSeed(key.getBytes());  
			kgen.init(128, secureRandom );
			Cipher cipher = Cipher.getInstance("AES");
	        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec( kgen.generateKey().getEncoded(), "AES"));
	        byte[] bytes = value.getBytes("utf-8");
	        byte[] resultBytes = cipher.doFinal(bytes);
	        String result = base64Encode(resultBytes);
	        return result;
		} catch (NoSuchAlgorithmException e) {
			log.error("encrypt error", e);
		} catch (NoSuchPaddingException e) {
			log.error("encrypt error", e);
		} catch (InvalidKeyException e) {
			log.error("encrypt error", e);
		} catch (UnsupportedEncodingException e) {
			log.error("encrypt error", e);
		} catch (IllegalBlockSizeException e) {
			log.error("encrypt error", e);
		} catch (BadPaddingException e) {
			log.error("encrypt error", e);
		}
		return null;
	}
	/**
	 * 
	 * Description : base64转码 + AES解密
	 * @param value
	 * @param encryptKey
	 * @return
	 * @author Abdul
	 * CreateTime 20150907
	 */
	private String aesDecrypt(String value,String encryptKey){
		try {
			String key = getEncryptKey(encryptKey);
			if( null == key ){
				return null;
			}
			byte[] bytes = base64Decode(value);
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			//20151023.1 Abdul.Wu 指定随机算法，解决Linux上解密问题
			SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG" );
            secureRandom.setSeed(key.getBytes());
			kgen.init(128, secureRandom );
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec( kgen.generateKey().getEncoded(), "AES"));
			byte[] result = cipher.doFinal(bytes);
			return new String(result,"utf-8");
		} catch (NoSuchAlgorithmException e) {
			log.error("encrypt error", e);
		} catch (NoSuchPaddingException e) {
			log.error("encrypt error", e);
		} catch (InvalidKeyException e) {
			log.error("encrypt error", e);
		} catch (IOException e) {
			log.error("encrypt error", e);
		} catch (IllegalBlockSizeException e) {
			log.error("encrypt error", e);
		} catch (BadPaddingException e) {
			log.error("encrypt error", e);
		}
		return null;
	}
	
	/**
	 * 
	 * Description : base64转码
	 * @param bytes
	 * @return
	 * @author Abdul
	 * CreateTime 20150907
	 */ 
    private String base64Encode(byte[] bytes){
//    	BASE64Encoder encoder = new BASE64Encoder();
//    	String encodeStr = encoder.encode(bytes);
 
    	Encoder encoder = Base64.getEncoder();
        String encodeStr = encoder.encodeToString(bytes);
        return encodeStr;  
    }

	/**
	 * 
	 * Description : base64反转码
	 * 
	 * @param base64Value
	 * @return
	 * @throws Exception
	 * @author Abdul CreateTime 20150907
	 * @throws IOException
	 */
	private byte[] base64Decode(String base64Value) throws IOException {
//    	BASE64Decoder encoder = new BASE64Decoder();
//    	byte[] decodeByte = encoder.decodeBuffer(base64Value);
		Decoder decoder = Base64.getDecoder();
		byte[] decodeByte = decoder.decode(base64Value);
		return decodeByte;
	}
    /**
     * 
     * Description : 转码
     * @param key
     * @return
     * @author Abdul
     * CreateTime 20150907
     */
    private String getEncryptKey(String key){
    	try {
			byte[] resultByte = base64Decode(key);
			String result = new String(resultByte,"utf-8");
			return result;
		} catch (IOException e) {
			log.error("encrypt key error", e);
		}
		return null;
    }
    
    public static void main(String[] args) {
//    	Scanner sc = new Scanner(System.in);
//    	EncryptServices services = new EncryptServices();
//		encryptValue(sc, services);

		EncryptServices services = new EncryptServices();
		String str = "118L9ntyuPoll/X6O8T2vw==";
		String value = services.aesDecrypt(str,ENCRYPT_KEY);
		System.out.println(value);
	}
    
    private static void encryptValue(Scanner sc, EncryptServices services){
    	System.out.println("Please input encrypt value: ");
    	String noEncryptStr = sc.nextLine();
		//加密
		String result = services.encrypt(noEncryptStr);
		System.out.println(noEncryptStr + " encrypt result: \n" + result);
		System.out.println("Whether to continue: Y/N?");
		String isContinueFlag = sc.nextLine();
		if( "N".equalsIgnoreCase(isContinueFlag) ){
			System.exit(0);
		}else{
			encryptValue(sc, services);
		}
    }
    
}
