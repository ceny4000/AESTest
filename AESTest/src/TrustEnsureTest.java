import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.Key;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;

public class TrustEnsureTest {
	//AES加解密使用的key
	private final static String KEY = "88889999";
	//POST對應的URL
	private final static String TARGET_URL = "http://xxx.xx";
	//檔案位置
	private final static String FILE_PATH = "C:\\Users\\Windows\\Downloads\\xxx.xml"; 
	//對應使用參數名稱
	private final static String TARGET_PARAM = "SENDDATA";

	/**
	 * 主程式進入點
	 * @param args
	 */
	public static void main(String args[]) {
		String content = "";

		//讀檔
		FileReader fr;
		try {
			fr = new FileReader(FILE_PATH);

			BufferedReader br = new BufferedReader(fr);

			String str;
			while ((str = br.readLine()) != null) {
				content += str;
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//Key先使用MD5加密，轉為32碼
		String md5Key = md5(KEY);
		
		String request = null;
		try {
			//送出加密
			request = new String(TrustEnsureTest.encrypt(content, md5Key));
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		try {
			TrustEnsureTest tet = new TrustEnsureTest();
			String result = tet.sendPost(TARGET_URL, request);
			
		    System.out.println("Request : " + request);
		    System.out.println("Result : " + result);
		    
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * POST Function
	 * @param targetURL
	 * @param docXml
	 * @return
	 * @throws Exception
	 */
	protected String sendPost(String targetURL, String docXml) throws Exception {
		HttpClient client = new HttpClient();
		client.getParams().setParameter("http.useragent", "Test Client");

		BufferedReader br = null;

		PostMethod method = new PostMethod(targetURL);

		method.addParameter(TARGET_PARAM, docXml);

		String readLine = null;

		try{
			int returnCode = client.executeMethod(method);

			if(returnCode == HttpStatus.SC_NOT_IMPLEMENTED) {
				// still consume the response body
				method.getResponseBodyAsString();
			} else {
				br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream(), "UTF8"));
				String rl = null;
				while ((rl = br.readLine()) != null) {
					if (readLine == null) {
						readLine = "";
					}
					readLine += rl;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			method.releaseConnection();
			if(br != null) try { br.close(); } catch (Exception fe) {}
		}
		return readLine;
	}

	/**
	 * MD5 加密
	 * @param str
	 * @return
	 */
	public static String md5(String str) {
		String md5=null;
		try {
			MessageDigest md=MessageDigest.getInstance("MD5");
			byte[] barr=md.digest(str.getBytes());  //將 byte 陣列加密
			StringBuffer sb=new StringBuffer();  //將 byte 陣列轉成 16 進制
			for (int i=0; i < barr.length; i++) {sb.append(byte2Hex(barr[i]));}
			String hex=sb.toString();
			md5=hex.toUpperCase(); //一律轉成大寫
		}
		catch(Exception e) {e.printStackTrace();}
		return md5;
	}
	
	public static String byte2Hex(byte b) {
		String[] h={"0","1","2","3","4","5","6","7","8","9","a","b","c","d","e","f"};
		int i=b;
		if (i < 0) {i += 256;}
		return h[i/16] + h[i%16];
	}
	
	/**
	 * AES 加密 Function
	 * @param content
	 * @param keyStr
	 * @return
	 * @throws Exception
	 */
	public static String encrypt(String content, String keyStr) throws Exception { 
		Key key = new SecretKeySpec(keyStr.getBytes(), "AES");
		
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		final byte[] encryptData = cipher.doFinal(content.getBytes());
		return new String(Base64.encodeBase64String(encryptData));
	}  
}
