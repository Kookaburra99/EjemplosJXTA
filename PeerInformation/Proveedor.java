
import net.jxta.platform.NetworkManager;
import java.io.File;

public class Proveedor {
    public static void main(String args[]) {
        try{
            //Conectamos el peer a la red
            NetworkManager manager = new NetworkManager(NetworkManager.ConfigMode.EDGE, "Proveedor", new File(new File(".cache"), "Proveedor").toURI());
            manager.startNetwork();

            Thread.sleep(30000);
        } catch(Exception e){
            e.printStackTrace();
        }
       
    }
}