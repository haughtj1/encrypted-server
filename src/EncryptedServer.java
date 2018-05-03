import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

public class EncryptedServer {
	private static BigInteger e;
	private static BigInteger d;
	private static BigInteger n;
	
	@SuppressWarnings("resource")
	public static void main(String sa[]) throws IOException, InterruptedException {
		String receivedMessage = "";
		String sendMessage = "";
		ServerSocket socket = null;
		int port = 0;

		generateKeys(2048);
		
		if(sa.length == 1) {
			port = Integer.parseInt(sa[0]);
			
			System.out.println("[Server] Attempting to establish server socket on port " + port);
			try {
				socket = new ServerSocket(port);
			} catch (BindException  e) {
				System.out.println("[Server] INVALID PORT: Unable to bind socket to port " + port);
				return;
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			
			System.out.println("[Server] Listening for request.");
			while(true) {
				Socket conSocket = socket.accept();
				System.out.println("[Server] Socket accepted.");
				
				//Establish input and output
				BufferedReader input = new BufferedReader(new InputStreamReader(conSocket.getInputStream()));
				DataOutputStream output = new DataOutputStream(conSocket.getOutputStream());
				
				receivedMessage = input.readLine();
				
				//Check for key request
				if(receivedMessage.equals("KEYREQ")) {
					System.out.println("[Server] Key request received.");
					output.writeBytes(getE().toString() + ":" + getN().toString() +"\n");;
				}				
				
				//now the will need to decrypt the messages
				receivedMessage = input.readLine();
				BigInteger dec = new BigInteger(receivedMessage);
				String message = new String(dec.toByteArray());
				System.out.println("[Server] encrypted: " + message);
				dec = dec.modPow(d, n);
				message = new String(dec.toByteArray());
				System.out.println("[Server] decrypted: " + message);
				receivedMessage = "";
				
				for(int i = 0; i < 3; i++) {
					message = unpadString(message);
					System.out.println(message);
				}	
				
				System.out.println("[Server] Message exchange complete.");				
				System.out.println("[Server] Waiting for next request...");
			}
			
		} else {
			System.out.println("[Server] Arguements must only include port number");
			return;
		}
	}
	
	private static void generateKeys(int size) {
		int keySize = size;

		// Generate some random primes
		BigInteger prime1 = new BigInteger(keySize / 2, 100, new SecureRandom());
		BigInteger prime2 = new BigInteger(keySize / 2, 100, new SecureRandom());

		//Calculate the modulus for the public and private keys (n = pq)
		setN(prime1.multiply(prime2));
		
		//Calculate the totient (tot = (p - 1)(q - 1))
		BigInteger totient = prime1.subtract(BigInteger.ONE).multiply(prime2.subtract(BigInteger.ONE));

		//Create the public exponent (used to encrypt)
		BigInteger tempE;
		do tempE = new BigInteger(totient.bitLength(), new SecureRandom());
		while (tempE.compareTo(BigInteger.ONE) <= 0 || tempE.compareTo(totient) >= 0 || !tempE.gcd(totient).equals(BigInteger.ONE));
		setE(tempE);
		
		//Create the private exponent (used to decrypt)
		setD(getE().modInverse(totient));
	}
	
	public static String unpadString(String text) {
		StringBuilder sb = new StringBuilder(text);
		
		int pad = Character.getNumericValue(sb.charAt(0));
		sb.deleteCharAt(0);
		
		StringBuilder res = new StringBuilder();
		int padPos = pad + 1;
		int prev = 1;
		while(padPos < sb.length()) {
			res.append(sb.substring(prev, padPos));
			prev = padPos + 1;
			padPos += pad + 1;
		}
		res.append(sb.substring(prev));
		
		return res.toString();
	}

	private static BigInteger getE() {
		return e;
	}

	private static void setE(BigInteger publicKey) {
		e = publicKey;
	}

	private static BigInteger getD() {
		return d;
	}

	private static void setD(BigInteger privateKey) {
		d = privateKey;
	}

	private static BigInteger getN() {
		return n;
	}

	private static void setN(BigInteger modulus) {
		n = modulus;
	}
	
}
