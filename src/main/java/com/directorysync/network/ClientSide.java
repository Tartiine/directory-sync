package com.directorysync.network;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent.Kind;


public class ClientSide{

	static String address = "192.168.1.69";
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

	public static void events(Kind<?> kind, Path file) throws IOException {
		String message = kind.toString() + " " + file.toString(); 
		sendMessage(message);
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


