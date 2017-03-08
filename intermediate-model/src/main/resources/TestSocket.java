import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.URLConnection;
import java.net.Socket;

public class ABC {

    ServerSocket s = new Socket();
    ServerSocket s_s = new Socket();
    URLConnection sr = new URLConnection();
    URLConnection sr2 = new URLConnection();
    Socket noTime = new Socket();
    Socket jupTime = new Socket();

    public ABC(){
        s.setSoTimeout(1100);
        sr.setReadTimeout(100);
        jupTime.setSoTimeout(1000);
    }

    public static void
    connectWithTimeouts( final URLConnection connection )
            throws IOException
    {
        connection.setConnectTimeout(100);
        connection.connect();  //should trigger a time cnst
    }

    public void m1(){
        s.accept(); //should trigger a time cnst
    }

    public void m2(){
        s_s.accept(); //should NOT trigger a time cnst
    }

    public void m3(){
        s_s.setSoTimeout(1100);
        s_s.accept(); //should trigger a time cnst
    }

    public static void
    connectWithTimeout(final URLConnection connection)
            throws IOException
    {
        connectWithTimeouts(connection);
    }

    public void m4(){
        DatagramSocket d = new DatagramSocket();
        d.setSoTimeout(100);
        d.receive( new DatagramPacket(p) );
    }

    public void m5(){
        DatagramSocket d = new DatagramSocket();
        //d.setSoTimeout(100);
        d.receive( new DatagramPacket(p) );
    }

    public void m6(){
        sr.getInputStream().read();
        jupTime.getInputStream().read();
        noTime.getInputStream().read();

    }

}