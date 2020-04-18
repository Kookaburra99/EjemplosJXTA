
import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.impl.protocol.PeerInfoResponseMsg;
import net.jxta.peer.PeerID;
import net.jxta.peer.PeerInfoService;
import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PeerInfoResponseMessage;
import net.jxta.meter.PeerMonitorInfo;
import net.jxta.meter.PeerMonitorInfoEvent;
import net.jxta.meter.PeerMonitorInfoListener;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;


public class Solicitador {
    private PeerGroup netPeerGroup = null;
    private NetworkManager manager = null;
    private DiscoveryService discovery = null;
    private PeerInfoService infoService = null; 

    public Solicitador(){
        try{
            //Conectamos el peer a la red
            manager = new NetworkManager(NetworkManager.ConfigMode.EDGE, "Solicitador", new File(new File(".cache"), "Solicitador").toURI());
            manager.startNetwork();

            //Obtenemos la instancia del netPeerGroup (grupo por defecto)
            netPeerGroup = manager.getNetPeerGroup();
            //Obtenemos el servicio Discovery del grupo
            discovery = netPeerGroup.getDiscoveryService();
            //Obtenemos el servicio de información del grupo
            infoService = netPeerGroup.getPeerInfoService();

        } catch (Exception e){
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static void main(String args[]){
        Solicitador solicitador = new Solicitador();
        solicitador.start();
    }


    public void start(){
        //Buscamos nodos en la red a los que le solicitaremos la información
        findPeers();
    }


    /**
     * Método que encuentra un nodo en la red
     */
    public void findPeers(){
        //Listener para manejar la información recibida de un anuncio de otro nodo
        DiscoveryListener peerDiscoveryListener = new DiscoveryListener() {
            public void discoveryEvent(DiscoveryEvent e){
                PeerAdvertisement peerAdv = null;
                DiscoveryResponseMsg respuesta = e.getResponse();
                Enumeration en = respuesta.getResponses();
                String temp = null;
                while(en.hasMoreElements()){
                    try{
                        temp = (String) en.nextElement();
                        peerAdv =  (PeerAdvertisement) AdvertisementFactory.newAdvertisement(new MimeMediaType("text/xml"), new ByteArrayInputStream(temp.getBytes()));
                        System.out.println("***** Peer encontrado con ID: " + peerAdv.getPeerID());
                        //Solicitamos información al nodo que acabamos de encontrar
                        getPeerInfo(peerAdv.getPeerID());
                    } catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            }
        };
        //Enviamos una solicitud de descubrimiento de nodos, facilitando el listener para que se ejecute al recibir una
        discovery.getRemoteAdvertisements(null, DiscoveryService.PEER, null, null, 5, peerDiscoveryListener);
    }

    /**
     * Método para solicitar y obtener la información de un nodo a partir de su ID
     */
    private void getPeerInfo(PeerID peerID){
        //Listener para recibir y manejar los mensajes de información de otros nodos
        PeerMonitorInfoListener peerInfoListener = new PeerMonitorInfoListener() {
            public void peerMonitorInfoReceived(PeerMonitorInfoEvent e){
                PeerMonitorInfo peerInfo = e.getPeerMonitorInfo();
                System.out.println("***** MOSTRANDO INFORMACIÓN DEL OTRO PEER *****");
                System.out.println("Tiempo activo en milisegundos: " + peerInfo.getRunningTime());
            }

            public void peerMonitorInfoNotReceived(PeerMonitorInfoEvent e){
                System.out.println("Se agotó el tiempo y no se recibió la información");
            }
        };

        //Enviado la solicitud de obtener información del nodo de id = peerID durante 10 segundos (10000 milisegundos)
        try{
            infoService.getPeerMonitorInfo(peerID, peerInfoListener, 10000); //Da NullPointerException porque la API es inconsistente en una de las clases que usa
        } catch (Exception e){
            e.printStackTrace();
        }  
    }

}