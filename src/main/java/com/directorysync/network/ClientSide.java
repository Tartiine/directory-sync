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

	/**
	 * @brief Sends a message to the server.
	 * @param message The message to send.
	 * @throws IOException if an I/O error occurs.
	 */
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

	/**
	 * @brief Sends the details of a file event to the server.
	 * @param kind The type of file event.
	 * @param file The path of the file that triggered the event or the target path.
	 * @throws IOException if an I/O error occurs.
	 */
	public static void events(String kind, Path file) throws IOException {
		String message = kind + " " + file.toString(); 
		sendMessage(message);
		if (kind.equals("ENTRY_MODIFY")){
			sendFile(file);
		}
	}

	/**
	 * @brief Sends a file to the server.
	 * @param filePath The path of the file to send.
	 * @throws IOException if an I/O error occurs.
	 */
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

	/**
	 * @brief Checks if a directory exists on the server.
	 * @param file The path of the directory to check.
	 * @throws IOException if an I/O error occurs.
	 */
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


