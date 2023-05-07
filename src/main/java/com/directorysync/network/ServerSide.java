package com.directorysync.network;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.directorysync.DirectorySync;

import java.io.*;
import java.net.*;
import java.nio.file.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;

public class ServerSide {

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
                BufferedInputStream bis = new BufferedInputStream(clientSocket.getInputStream());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead = 0;
                while ((bytesRead = bis.read(buffer, 0, buffer.length)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                byte[] fileBytes = baos.toByteArray();
                String fileName = "received_file";
                FileOutputStream fos = new FileOutputStream(fileName);
                fos.write(fileBytes, 0, fileBytes.length);
                System.out.println("File saved as " + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
