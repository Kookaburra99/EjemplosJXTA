/**
 * Ejemplo de implementación del Peer Discovery Protocol de JXTA
 * Basado en el código de https://www.tamps.cinvestav.mx/~vjsosa/clases/redes/JXTA_SE_ProgGuide_v2.5.pdf
 */

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.DiscoveryResponseMsg;

import java.io.File;
import java.util.Enumeration;

/**
 * Peer que envía Discovery Messages periodicamente, para descubrir nuevos peers en el grupo
 */

public class PeerDescubridor implements DiscoveryListener {
    private transient NetworkManager manager;
    private transient DiscoveryService discovery;

    /**
     * Constructor del PeerDescubridor
     * Inicia el peer en la red, conectándose al grupo NetPeerGroup (por defecto) y obtiene el servicio para descubrir a otros peers del grupo
     */
    public PeerDescubridor() {
        try {
            //Definimos el peer como un "edge peer" (un peer básico), le asignamos un nombre dentro de la red, e iniciamos la red
            manager = new NetworkManager(NetworkManager.ConfigMode.EDGE, "PeerDescubridor", new File(new File(".cache"), "PeerDescubridor").toURI());
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
    public static void main(String args[]){
        //Instanciamos el peer y lo inicializamos
        PeerDescubridor peerDescubridor = new PeerDescubridor();
        peerDescubridor.start();
    }


    /**
     * Bucle infinito que espera anuncios de otros peers para descubrirlos cada minuto
     */
    public void start(){
        long tiempoEspera = 60 * 1000L; //60 segundos (60000 milisegundos)
        try{
            //Añade al propio peer como escuchador del Discovery Service para recibir eventos del mismo (anuncios de otros peers)
            discovery.addDiscoveryListener(this);
            discovery.getRemoteAdvertisements(
                //Esperamos avisos de todos los peers del grupo (null), no de uno en concreto
                null,
                //Esperamos avisos de peers o de grupos
                DiscoveryService.ADV,
                //No especificamos ningún atributo en concreto por el que filtrar a los emisores de los avisos
                null,
                //Al no especificar atributo, no le esperamos ningún valor concreto al mismo
                null,
                //Esperamos como máximo un anuncio de cada vez
                1,
                //No especificamos ningún listener en concreto, usaremos el global (por defecto)
                null);

            while (true) {
                //Esperamos el tiempo de espera establecido
                try{
                    System.out.println("Esperando por " + tiempoEspera + " milisegundos...");
                    Thread.sleep(tiempoEspera);
                } catch (Exception e){
                    //No necesitamos tratar esta excepción
                }
                System.out.println("¡Enviando un Discovery Message (mensaje para descubrir nuevos peers)!");
                discovery.getRemoteAdvertisements(
                    //Esperamos avisos de todos los peers del grupo (null), no de uno en concreto
                    null,
                    //Esperamos avisos de peers o de grupos
                    DiscoveryService.ADV,
                    //Esta vez filtramos por el nombre del aviso
                    "Name",
                    //Solo nos interesan los avisos de nombre "Discovery tutorial", que son los que enviará el otro peer de este ejemplo
                    "Discovery tutorial",
                    //Esperamos como máximo un anuncio de cada vez
                    1,
                    //No especificamos ningún listener en concreto, usaremos el global (por defecto)
                    null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Método llamado cuando nuestro mensaje de descubrimiento recibe una respuesta (un aviso de otro peer)
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
     * Método que para la plataforma JXTA en el que corre el peer
     */
    public void stop(){
        manager.stopNetwork();
    }

}