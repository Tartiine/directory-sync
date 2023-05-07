package com.directorysync.network;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import com.directorysync.DirectorySync;


public class ClientSide{

	static String address = "localhost"; //"192.168.1.69";
	public static Path dir = Paths.get("./trgDir");

    public static void main(String[] args) throws IOException {

		dirExist(dir);
		while (true) {	
		}

    }

	public static void sendMessage(String message) throws IOException {
		try (Socket socket = new Socket(address, 3456)) {
			System.out.println("Sending message: " + message);
	
			OutputStream outputStream = socket.getOutputStream();
			outputStream.write(message.getBytes());
			outputStream.flush();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void events(String kind, Path file) throws IOException {
		String message = kind + " " + file.toString(); 
		sendMessage(message);
		if (kind.equals("ENTRY_MODIFY")){
			sendFile(file);
		}
	}

	private static void sendFile(Path filePath) {
		try (Socket socket = new Socket(address, 3456)) {
			OutputStream outputStream = socket.getOutputStream();
		
			// Send the target path
			PrintWriter pw = new PrintWriter(outputStream, true);
			pw.println(DirectorySync.targetPath);
		
			// Send the file bytes to the server
			File file = filePath.toFile();
			byte[] fileBytes = new byte[(int) file.length()];
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			bis.read(fileBytes, 0, fileBytes.length);
			outputStream.write(fileBytes, 0, fileBytes.length);
			outputStream.flush();
		
			// Receive the confirmation message from the server
			Scanner scanner = new Scanner(socket.getInputStream());
			String confirmation = scanner.nextLine();
			System.out.println(confirmation);
		
			// Close the streams and socket
			bis.close();
			outputStream.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void dirExist(Path file) throws IOException {
		try (Socket socket = new Socket(address, 3456)) {
			String message = "checking" + " " + file.toString();
			OutputStream outputStream = socket.getOutputStream();
			outputStream.write(message.getBytes());
			outputStream.flush();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}


