
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.pipe.PipeService;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.PipeAdvertisement;


import java.io.*;


public class AdministracionUniversitaria {

    public static void main(String args[]){
        //Se obtiene una instancia del NetPeerGroup
        PeerGroup netPeerGroup = null;
        try {
            NetworkManager manager = new NetworkManager(NetworkManager.ConfigMode.EDGE, "AdministracionUniversitaria", new File(new File(".cache"), "AdministracionUniversitaria").toURI());
            manager.startNetwork();
            //Obtenemos el NetPeerGroup (grupo por defecto al que pertenece el peer al conectarse a la red)
            netPeerGroup = manager.getNetPeerGroup();
        } catch (PeerGroupException | IOException e) {
            System.err.println("Error al obtener una instancia del NetPeerGroup");
            e.printStackTrace();
            System.exit(1);
        }

        //Instanciamos las clases necesarias para ler la entrada por consola del usuario
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);
        
        String seleccion = "2";
        //Menú interactivo para crear el grupo o salir de la aplicación
        System.out.println("\n***** ADMINISTRACIÓN UNIVERSITARIA *****\n");
        System.out.println("Seleccione una acción:");
        System.out.println("1. Crear grupo universitario");
        System.out.println("2. Cerrar sesión");
        try{
            seleccion = br.readLine();
        } catch (IOException e){
            e.printStackTrace();
        }

        switch(seleccion){
            case "1":
                crearGrupoUniversitario(netPeerGroup);
                break;
            case "2":
                System.out.println("\nCerrando sesión...");
                break;
        }

        if(seleccion.equals("1")){
            //Hacemos uso de Peer Discovery para avisar de que el grupo se ha creado y está disponible
            DiscoveryService discovery = netPeerGroup.getDiscoveryService();
            try{
                while(true){
                    Thread.sleep(30000);
                }
            } catch(Exception e){
                System.err.println("Error publicando el aviso de la existencia del grupo");
                e.printStackTrace();
            }
        }

    }

    private static void crearGrupoUniversitario(PeerGroup grupoPadre){
        try{
            GroupManager manager = new GroupManager(grupoPadre);
            PeerGroup grupoUni = manager.addGroup("Universidad", "AdministracionUniversitaria", "Grupo que representa al conjunto de estudiantes de la universidad", null);

        } catch (Exception e){
            System.err.println("¡¡¡Error creando el grupo universitario!!!");
            e.printStackTrace();
        }
    }

    private static PipeAdvertisement getPipeAdvertisement() {
        PipeAdvertisement advertisement = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());
        advertisement.setPipeID(IDFactory.newPipeID(PeerGroupID.defaultNetPeerGroupID));
        advertisement.setType(PipeService.UnicastType);
        advertisement.setName("Grupo universitario creado!");

        return advertisement;
    }

}