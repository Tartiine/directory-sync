/** 
* Class name : DirectorySync
* Description : Server side of the network, processing the events
* @author Isaac Dalberto, Sofia Saadi
*/

package com.directorysync.network;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class ServerSide {
/**
 * @brief Main function that starts the server
 * @param args Command line arguments
 * @throws IOException if an I/O error occurs
 */
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
                processMessage(clientSocket, message);
            }

            // Close the socket
            clientSocket.close();
        }
        // servsock.close();
    }
    /**
     * @brief Process the incoming message from the client
     * @param clientSocket The socket for the client connection
     * @param message The message received from the client
     */
    public static void processMessage(Socket clientSocket, String message) {
        String[] parts = message.split(" ");
        String kind = parts[0];
        String file = parts[1];

        System.out.println("Event kind: " + kind);
        System.out.println("File: " + file);

        if (kind.equals("ENTRY_DELETE")) {
            try {
                Files.delete(Paths.get(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (kind.equals("ENTRY_CREATE")) {
            try {
                Files.createFile(Paths.get(file));
                System.out.println("HERE");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (kind.equals("ENTRY_CREATE_DIR")) {
            try {
                Files.createDirectory(Paths.get(file));
                System.out.format("Directory '%s' created.%n", file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (kind.equals("ENTRY_MODIFY")) {
            // Handle modify event
            try {
                Files.delete(Paths.get(file));
                // Receive the target path from the client
                BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String targetPath = br.readLine();
                System.out.println("Target path received: " + targetPath);

                // Receive the file bytes from the client and create the file
                byte[] buffer = new byte[1024];
                int bytesRead;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                BufferedInputStream bis = new BufferedInputStream(clientSocket.getInputStream());
                while ((bytesRead = bis.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                byte[] fileBytes = baos.toByteArray();
                Path filePath = Paths.get(targetPath);
                Files.write(filePath, fileBytes);
                System.out.println("File created at: " + filePath);

                // Send confirmation message to the client
                PrintWriter pw = new PrintWriter(clientSocket.getOutputStream(), true);
                pw.println("File created at: " + filePath);

                // Close the streams and socket
                bis.close();
                br.close();
                clientSocket.close();
                System.out.println("Connection closed.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}