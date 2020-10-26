import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Server {

	private static final int sPort = 8000;   //The server will be listening on this port number
	private static int clientNum = 1;	

	public static void main(String[] args) throws Exception {
		System.out.println("The server is running."); 
        	ServerSocket listener = new ServerSocket(sPort);
		//int clientNum = 1;
        	try {
            		while(true) {
                		new Handler(listener.accept(),clientNum).start();
				System.out.println("Client "  + clientNum + " is connected!");
				clientNum++;
            			}
        	} finally {
            		listener.close();
        	} 
 
    	}

	/**
     	* A handler thread class.  Handlers are spawned from the listening
     	* loop and are responsible for dealing with a single client's requests.
     	*/
    	private static class Handler extends Thread {
        	private String message;    //message received from the client
		private String MESSAGE;    //uppercase message send to the client
		private String filename;   //cmd line for filename
		private Socket connection;
        	private ObjectInputStream in;	//stream read from the socket
        	private ObjectOutputStream out;    //stream write to the socket
		private int no;		//The index number of the client

        	public Handler(Socket connection, int no) {
            		this.connection = connection;
	    		this.no = no;
        	}

        public void run() {
 		try{
			//initialize Input and Output streams
			out = new ObjectOutputStream(connection.getOutputStream());
			out.flush();
			in = new ObjectInputStream(connection.getInputStream());
			try{
				while(true)
				{
					System.out.println("Wait for cmd from client...");
					message = (String)in.readObject();

					if(message.equals("get")){
						System.out.println("Receive GET cmd ... from client" + no);
						filename = (String)in.readObject();
						SendFile(filename);
						continue;
						
					} else if (message.equals("put")){
						System.out.println("Receive PUT cmd ... from client" + no);
						filename = (String)in.readObject();
						if(filename.equals("File not found")){
							System.out.println("Uploading error occurs...");
							continue;
						}
						ReceiveFile(filename);
						continue;
					} else if (message.equals("dir")){
						System.out.println("Receive DIR cmd ... from client" + no);
						ListFile();
						continue;
					}
					
					/****
					//receive the message sent from the client
					message = (String)in.readObject();
					//show the message to the user
					System.out.println("Receive message: " + message + " from client " + no);
					//Capitalize all letters in the message
					MESSAGE = message.toUpperCase();
					//send MESSAGE back to the client
					sendMessage(MESSAGE);
					****/
				}
			}
			catch(ClassNotFoundException classnot){
					System.err.println("Data received in unknown format");
			}
			catch(Exception ex){
			}
		}
		catch(IOException ioException){
			System.out.println("Disconnect with Client " + no);
		}
		finally{
			//Close connections
			try{
				clientNum--;
				in.close();
				out.close();
				connection.close();
			}
			catch(IOException ioException){
				System.out.println("Disconnect with Client " + no);
			}
		}
	}

	void SendFile(String filename) throws Exception{
		File file = new File(filename);
		if(file.exists()){
			sendMessage("Start downloading from server...");
			FileInputStream fin = new FileInputStream(file);
			int ch = 0;
			while(ch != -1){
				ch = fin.read(); //Reads a byte of data from this input stream
				sendMessage(Integer.toString(ch));
			}
			fin.close();
			sendMessage("Downloading file succeed...");
		} else {
			sendMessage("File not found...");
			return;
		}
	}

	void ReceiveFile(String filename) throws Exception{
		File file = new File(filename);
		sendMessage("Start uploading to server...");
		FileOutputStream fout = new FileOutputStream(file);
		int ch = 0;
		while(ch != -1){
			ch = Integer.parseInt((String)in.readObject());
			if(ch != -1)
				fout.write(ch);		
		}
		fout.close();
		sendMessage("Upload file succeed...");
		
	}

	void ListFile() throws Exception{
		String[] dir_list;

		File file = new File("./");
		sendMessage("Listing files of server directory...");
		dir_list = file.list();

		for(String filename: dir_list)
			sendMessage(filename);
		sendMessage("End of listing...");
		
	}

	//send a message to the output stream
	public void sendMessage(String msg)
	{
		try{
			out.writeObject(msg);
			out.flush();
			//System.out.println("Send message: " + msg + " to Client " + no);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}

    }

}
