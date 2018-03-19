import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class EncryptedServer {
	@SuppressWarnings("resource")
	public static void main(String sa[]) throws IOException, InterruptedException {
		String receivedMessage = "";
		String sendMessage = "";
		ServerSocket socket = null;
		int port = 0;
		
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
				System.out.println("[Server] Buffers established.");
				
				receivedMessage = input.readLine();
				System.out.println("[Server] Message received from client: " + receivedMessage);
				output.writeBytes("Acknowledged: " + receivedMessage + "\n");
				receivedMessage = "";
				
				System.out.println("[Server] Waiting...");
				TimeUnit.SECONDS.sleep(5);
			}
			
		} else {
			System.out.println("[Server] Arguments must only include port number");
			return;
		}
	}
}
