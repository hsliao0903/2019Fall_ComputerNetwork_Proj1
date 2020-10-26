import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Client {
	Socket requestSocket;           //socket connect to the server
	ObjectOutputStream out;         //stream write to the socket
 	ObjectInputStream in;          //stream read from the socket
	String message;                //message send to the server
	String MESSAGE;                //capitalized message read from the server
	String filename;
	String username = "", passwd = "";
	//String ftp_cmd;
	public void Client() {}

	void run()
	{
		try{
			//get Input from standard input
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

			while(true){
				try{
					System.out.println("Please input ftpclient cmd");
					
					message = bufferedReader.readLine();
					String[] ftp_cmd = message.split(" ");
					if(ftp_cmd.length != 3 || !ftp_cmd[0].equals("ftpclient")||!validIP(ftp_cmd[1])){
						System.out.println("Wrong cmd format...");
						continue;
					}
					
					//create a socket to connect to the server
					requestSocket = new Socket(ftp_cmd[1], Integer.parseInt(ftp_cmd[2]));
					System.out.println("Connected to" + ftp_cmd[0] + "in port" + ftp_cmd[2]);
					break;
					//System.out.println("@@:" + ftp_cmd[0] + "IP:" + ftp_cmd[1] + " port: " + Integer.parseInt(ftp_cmd[2]));
	
				}
				catch(NumberFormatException e){
					System.err.println("error format for port...");
				}
				catch(UnknownHostException unknownHost){
					System.err.println("You are trying to connect to an unknown host!");
				}
				catch(SocketException e){
					System.err.println("You are trying to connect to an unknown host or invalid port!");
				}
			}


			//get Input from standard input
			//BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			while(!(username.equals("Admin"))||!(passwd.equals("1234"))){
				//Client Login Check
				System.out.println("Please enter your username and passwd:");
				System.out.print("Username: ");
				username = bufferedReader.readLine();
				System.out.print("Passwd: ");
				passwd = bufferedReader.readLine();
			}

			//create a socket to connect to the server
			//requestSocket = new Socket("localhost", 8000);
			//System.out.println("Connected to localhost in port 8000");
			//initialize inputStream and outputStream
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(requestSocket.getInputStream());
			
			//get Input from standard input
			//BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			while(true)
			{
				System.out.print("Please input one of the three cmds: ");
				System.out.println("1.get 2.put 3.dir");
				message = bufferedReader.readLine();
				if(message.equals("get")){
					sendMessage("get");
					System.out.print("Input a file name...");
					filename = bufferedReader.readLine();
					ReceiveFile(filename);
				} else if(message.equals("put")){
					sendMessage("put");
					System.out.print("Input a file name...");
					filename = bufferedReader.readLine();
					SendFile(filename);
				} else if(message.equals("dir")){
					sendMessage("dir");
					ListFile();
				} else {
					System.out.print("Unknown cmd...");
				}
				

				/****
				System.out.println("Hello, please input a sentence: ");
				//read a sentence from the standard input
				message = bufferedReader.readLine();
				//Send the sentence to the server
				sendMessage(message);
				//Receive the upperCase sentence from the server
				MESSAGE = (String)in.readObject();
				//show the message to the user
				System.out.println("Receive message: " + MESSAGE);
				****/
			}
		}
		catch (ConnectException e) {
    			System.err.println("Connection refused. You need to initiate a server first.");
		} 
		catch ( ClassNotFoundException e ) {
            		System.err.println("Class not found");
        	} 
		catch(UnknownHostException unknownHost){
			System.err.println("You are trying to connect to an unknown host!");
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
		catch(Exception ex){
		}
		finally{
			//Close connections
			try{
				in.close();
				out.close();
				requestSocket.close();
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
	}

	void SendFile(String filename) throws Exception{
		File file = new File(filename);
        	if(!file.exists()){
            		System.out.println("File not found...");
            		sendMessage("File not found");
            		return;
        	}
		
		sendMessage(filename);
		message = (String)in.readObject();
		System.out.println(message);
		FileInputStream fin = new FileInputStream(file);
		int ch = 0;
		while(ch != -1){
			ch = fin.read();
			sendMessage(Integer.toString(ch));
		}
		fin.close();
		message = (String)in.readObject();
		System.out.println(message);
	}

	void ReceiveFile(String filename) throws Exception{
		sendMessage(filename);
		message = (String)in.readObject();
		if(message.equals("File not found...")){
			System.out.println("File not found...");
			return;
		} else if(message.equals("Start downloading from server...")){
			System.out.println("Downloading file ...");
			File file = new File(filename);
			FileOutputStream fout = new FileOutputStream(file);
			int ch = 0;
			while(ch != -1){
				ch = Integer.parseInt((String)in.readObject());
				if(ch != 1)
					fout.write(ch);
			}
			fout.close();
			message = (String)in.readObject();
			System.out.println(message);
		}
	}

	void ListFile() throws Exception{
		message = (String)in.readObject();
		System.out.println(message);
		while( (message = (String)in.readObject()) != null){
			System.out.println(message);
			if(message.equals("End of listing..."))
				break;
		}	
	}

	public static boolean validIP (String ip) {
    		try {

			if(ip.equals("localhost"))
				return true;
        		if ( ip == null || ip.isEmpty() ) {
            			return false;
        		}

        		String[] parts = ip.split( "\\." );
        		if ( parts.length != 4 ) {
            			return false;
        		}

        		for ( String s : parts ) {
            			int i = Integer.parseInt( s );
            			if ( (i < 0) || (i > 255) ) {
                			return false;
            			}
        		}
        		if ( ip.endsWith(".") ) {
            			return false;
        		}

        			return true;
    		} catch (NumberFormatException nfe) {
        			return false;
    		}
	}

	//send a message to the output stream
	void sendMessage(String msg)
	{
		try{
			//stream write the message
			out.writeObject(msg);
			out.flush();
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	//main method
	public static void main(String args[])
	{
		Client client = new Client();
		client.run();
	}

}
