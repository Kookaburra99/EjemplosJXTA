/**
 * Ejemplo de implementación del Peer Discovery Protocol de JXTA
 * Basado en el código de https://www.tamps.cinvestav.mx/~vjsosa/clases/redes/JXTA_SE_ProgGuide_v2.5.pdf
 */

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.pipe.PipeService;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.PipeAdvertisement;

import java.io.File;
import java.util.Enumeration;

/**
 * Peer que se conecta a la red enviando mensajes de aviso para ser descubierto
 */
public class PeerDescubierto implements DiscoveryListener {
    private transient NetworkManager manager;
    private transient DiscoveryService discovery;

    /**
     * Constructor del PeerDescubierto
     * Inicia el peer en la red, conectándose al grupo NetPeerGroup (por defecto) y obtiene el servicio para descubrir a otros peers del grupo
     */
    public PeerDescubierto() {
        try{
            //Definimos el peer como un "edge peer" (un peer básico), le asignamos un nombre dentro de la red, e iniciamos la red
            manager = new NetworkManager(NetworkManager.ConfigMode.EDGE, "PeerDescubierto", new File(new File(".cache"), "PeerDescubierto").toURI());
            manager.startNetwork();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        //Obtenemos el NetPeerGroup (grupo por defecto al que pertenece el peer al conectarse a la red)
        PeerGroup netPeerGroup = manager.getNetPeerGroup();
        //Obtenemos el Discovery Service (servicio con el que poder descubrir a otros peers del grupo)
        discovery = netPeerGroup.getDiscoveryService();
    }

     /**
     * Método main
     */
    public static void main(String args[]) {
        //Instanciamos el peer y lo inicializamos
        PeerDescubierto peerDescubierto = new PeerDescubierto();
        peerDescubierto.start();
    }


    /**
     * Bucle infinito que crea anuncios y los publica en la red durante 2 minutos cada 3 minutos
     */
    public void start() {
        long tiempoVida = 60 * 2 * 1000L; //2 minutos (120000 milisegundos)
        long expiracion = 60 * 2 * 1000L; //2 minutos (120000 milisegundos)
        long tiempoEspera = 60 * 3 * 1000L; //3 minutos (180000 milisegundos)

        try{
            while (true) {
                PipeAdvertisement pipeAdv = getPipeAdvertisement();
                //Publica el aviso durante el tiempo de vida estipulado en el Discovery Service
                System.out.println("Publicando el siguiente anuncio con un tiempo de vida de " + tiempoVida + " milisegundos");
                System.out.println(pipeAdv.toString());
                discovery.publish(pipeAdv, tiempoVida, expiracion);
                try{
                    System.out.println("Esperando " + tiempoEspera + " milisegundos hasta enviar el siguiente anuncio");
                    Thread.sleep(tiempoEspera);
                } catch (Exception e) {
                    //No necesitamos tratar esta excepción
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Método llamado cuando se recibe una respuesta de descubrimiento
     */
    public void discoveryEvent(DiscoveryEvent ev) {
        //Obtenemos el mensaje de la respuesta
        DiscoveryResponseMsg res = ev.getResponse();
        System.out.println("[ Se obtuvo una Discovery Response (respuesta de descubrimiento) {" + res.getResponseCount() + " elementos} del peer: " + ev.getSource() + " ]");
        
        //Mostramos el contenido de los avisos
        Advertisement adv;
        Enumeration en = res.getAdvertisements();
        if (en != null) {
            while (en.hasMoreElements()) {
                adv = (Advertisement) en.nextElement();
                System.out.println(adv);
            }
        }
    }

    /**
     * Crea el aviso a enviar
     */
    public static PipeAdvertisement getPipeAdvertisement() {
        PipeAdvertisement advertisement = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());
        advertisement.setPipeID(IDFactory.newPipeID(PeerGroupID.defaultNetPeerGroupID));
        advertisement.setType(PipeService.UnicastType);
        advertisement.setName("Discovery tutorial");

        return advertisement;
    }
}