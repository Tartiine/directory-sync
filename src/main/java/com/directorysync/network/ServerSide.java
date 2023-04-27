package com.directorysync.network;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ServerSide{




	public static void main(String[] args) throws IOException {

			ServerSocket servsock;

			servsock = new ServerSocket(3456);
			System.out.println("Server started on port 3456.");


			while (true) {
				System.out.println("Waiting for incoming connections...");
				Socket clientSocket = servsock.accept();
				System.out.println("Accepted connection from " + clientSocket.getRemoteSocketAddress());
	
				// Read incoming messages from the client
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
					String message = reader.readLine();
					processMessage(message);
				}

	
	
				// Close the socket
				clientSocket.close();
			}
			//servsock.close();
	}


	public static void processMessage(String message) {
		String[] parts = message.split(" ");
		String kind = parts[0];
		String file = parts[1];
		
		System.out.println("Event kind: " + kind);
		System.out.println("File: " + file);

		if (kind.equals("ENTRY_DELETE")){
			try {
				Files.delete(Paths.get(file));
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (kind.equals("ENTRY_CREATE")){
			try {
				Files.createFile(Paths.get(file));
				System.out.println("HERE");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (kind.equals("ENTRY_MODIFY")){
			// Handle modify event
		}
	}
	

}





		/* 
		Scanner in = new Scanner(sock.getInputStream());
		InputStream is = sock.getInputStream();
		PrintWriter pr = new PrintWriter(sock.getOutputStream(), true);
		String FileName = in.nextLine();
		int FileSize = in.nextInt();
		FileOutputStream fos = new FileOutputStream("trgDir/truc");
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		byte[] filebyte = new byte[FileSize];
		
		int file = is.read(filebyte, 0, filebyte.length);
		bos.write(filebyte, 0, file);
		
		System.out.println("Incoming File: " + FileName);
		System.out.println("Size: " + FileSize + "Byte");
		if(FileSize == file)System.out.println("File is verified");
		else System.out.println("File is corrupted. File Received " + file + " Byte");
		pr.println("File Received Successfully.");
		

		bos.close();
		*/