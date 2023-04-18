package com.directorysync.network;



import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ServerSide{

    @SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		ServerSocket servsock = new ServerSocket(3456);
		Socket sock = servsock.accept();
		Scanner in = new Scanner(sock.getInputStream());
		InputStream is = sock.getInputStream();
		PrintWriter pr = new PrintWriter(sock.getOutputStream(), true);
		String FileName = in.nextLine();
		int FileSize = in.nextInt();
		FileOutputStream fos = new FileOutputStream(FileName);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		byte[] filebyte = new byte[FileSize];
		
		int file = is.read(filebyte, 0, filebyte.length);
		bos.write(filebyte, 0, file);
		
		System.out.println("Incoming File: " + FileName);
		System.out.println("Size: " + FileSize + "Byte");
		if(FileSize == file)System.out.println("File is verified");
		else System.out.println("File is corrupted. File Received " + file + " Byte");
		pr.println("File Recieved Successfully.");
		bos.close();
		sock.close();
	}
    /* 
    public static void main(String[] args) {
        try {
            System.out.println("Waiting for clients...");
            ServerSocket ss = new ServerSocket(49100);
            Socket soc = ss.accept();
            System.out.println("Connection established");
            BufferedReader in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
            String str = in.readLine();
            PrintWriter out = new PrintWriter(soc.getOutputStream(), true);
            out.println("Server sends:"+str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    */
}




