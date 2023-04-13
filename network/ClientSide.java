package network;



import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


public class ClientSide{
    @SuppressWarnings("resource")
	public static void main(String[] args) throws UnknownHostException, IOException {
		Socket sock = new Socket("localhost", 3456);
		String FileName = "srcDir/truc";
		File MyFile = new File(FileName);
		int FileSize = (int) MyFile.length();
		OutputStream os =sock.getOutputStream();
		PrintWriter pr = new PrintWriter(sock.getOutputStream(), true);
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(MyFile));
		Scanner in = new Scanner(sock.getInputStream());
		
		pr.println(FileName);
		pr.println(FileSize);
		byte[] filebyte = new byte[FileSize];
		bis.read(filebyte, 0, filebyte.length);
		os.write(filebyte, 0, filebyte.length);
		System.out.println(in.nextLine());
		os.flush();
		sock.close();
	}

    /*

    public static void main(String[] args) {
        
        System.out.println("Client started");
        try {
            Socket soc = new Socket("localhost",49100);
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter text:");
            String str = userInput.readLine();
            PrintWriter out = new PrintWriter(soc.getOutputStream(), true);
            out.println(str);
            BufferedReader in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
            System.out.println(in.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
    } */
}