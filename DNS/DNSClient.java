import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class DNSClient {

    static DatagramSocket socket;
    static int port = 53;
    
    static int header_length = 12;
    static int qtype_length = 2;
    static int qclass_length = 2;
    static byte[] query;
    static byte[] answer;
    
    public DNSClient(String url) {
        try {
         socket = new DatagramSocket();
         socket.setSoTimeout(2000);
        }
        catch (Exception ex) {
               System.out.printf("Exception : ", ex);
        }
        
        query = new byte[header_length + url.length() + 2 + qtype_length + qclass_length];
    }
    
    public static String ByteToString(byte number, int groupSize) {
        StringBuilder result = new StringBuilder();

        for(int i = 7; i >= 0 ; i--) {
            int mask = 1 << i;
            result.append((number & mask) != 0 ? "1" : "0");

            if (i % groupSize == 0)
                result.append(" ");
        }
        result.replace(result.length() - 1, result.length(), "");

        return result.toString();
    }
    
    public void setHeader() {
        for(int i = 0; i< query.length; ++i)
            query[i] = (byte) 0x00;

        /* Identifier */
        query[0] = (byte)4; // random integer
        query[0] = (byte)( query[0] & 0xFF );
        query[1] = (byte)( query[0] >> 2);
        
        /* Flags and Codes */
        query[2] = (byte)(0 << 7); // Query/Response Flag(1 bit) - 0
        
        byte opCode = (byte) 0x00; // Operation Code(4 bits) - 0000
        query[2] |= (opCode << 3);
        
        query[2] |= (byte)(0 << 2); // Authoritative Answer(1 bit) - 0
        query[2] |= (byte)(0 << 1); // Truncation Flag(1 bit) - 0
        query[2] |= (byte)(0 << 0); // Recursion Desired(1 bit) - 0
        query[3] |= (byte)(0 << 7); // Recursion Available(1 bit) - 0
        
        /* Question Count */
        query[4] = (byte)(0x00);
        query[5] = (byte)(0x01);

    }
    
    public void setQuestion(String url) {
        int question_name_index = 13;
        int question_type_index = header_length + url.length() + 2;
        int question_class_index = question_type_index + 2;
        int i = 0;
        
        /* Question Name */
        for(char c : url.toCharArray())
        {
            i++;
            
            if(c != '.') {
                query[question_name_index] = (byte)((int)c);
                question_name_index++;
            }
            else if(c == '.'){
                query[question_name_index - i] = (byte)((i - 1) & (0xFF));
                question_name_index++;
                i = 0;
            }    
        }
        query[question_name_index - i - 1] = (byte)((i) & (0xFF));
        query[question_name_index] = (byte)(~(0xFF)); // last byte of Question Name 
        
        /* Question Type */
        query[question_type_index] = (byte)((0x00));
        query[question_type_index + 1] = (byte)((0x01));
        
        /* Question Class */
        query[question_class_index] = (byte)((0x00));
        query[question_class_index + 1] = (byte)((0x01));
    }
    
    public void sendMessage() {
    	String serverAddressIp = "81.180.223.1";
        String[] serverAddress =  serverAddressIp.split("\\.");
        DatagramPacket request = null;
        
        byte[] serverAddressBytes = new byte[serverAddress.length];
        for (int i = 0; i < serverAddress.length; i++)
        {
            serverAddressBytes[i] = (byte) Integer.parseInt(serverAddress[i]);
        }
        
        try {
            
            InetAddress host = InetAddress.getByAddress(serverAddressBytes);
            request = new DatagramPacket(query, query.length, host, port);
            socket.send(request);
            
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void getAnswer() {
    	byte[] buffer = new byte[512];
        DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
        
        try {
            socket.receive(reply);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        answer = reply.getData();
    }
    
    public void messageProcessing() {
    	int rCode = (byte) (answer[3] & 0x0F);
    	int answerCount = ( (answer[6] & 0xFF) << 8 ) | (answer[7] & 0xFF);
    	int name_index = 12;
    	if(rCode != 0)
    		System.err.print("Error " + Integer.toBinaryString(rCode) + "\n");
    	
    	if(answerCount >= 1)
    		System.out.print("Answer Count " + answerCount + " >= 1\n");
    	else
    		System.out.print("Answer Count " + answerCount + " < 1\n");
    		
    	
    	/*while(answer[name_index] != 0x00) {
    		
    	}*/
    }
    
    public static void main(String args[]) {
        String url = "www.tuiasi.ro";
        DNSClient c = new DNSClient(url);
        
        c.setHeader();
        c.setQuestion(url);
        
        /*for(int i = 0; i < query.length; ++i)
            System.out.println(ByteToString(query[i], 8));*/
        
        c.sendMessage();
        c.getAnswer();
        
        for(int i = 0; i < answer.length; ++i)
            System.out.println(ByteToString(answer[i], 8));
        
        c.messageProcessing();
    }

}