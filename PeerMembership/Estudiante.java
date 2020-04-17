/**
 * Ejemplo de implementación del Peer Membership Protocol de JXTA
 * Basado en las indicaciones del libro "JXTA: Java P2P Programming", Daniel Brookshier et al., Sams Publishing, 2002
 */

import net.jxta.peergroup.*;
import net.jxta.exception.*;
import net.jxta.id.IDFactory;
import net.jxta.protocol.*;
import net.jxta.discovery.*;
import net.jxta.document.*;
import net.jxta.credential.*;
import net.jxta.membership.*;
import net.jxta.platform.NetworkManager;

import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.net.*;
import java.io.IOException;

/**
 * Clase de ejemplo de uso del GroupManger
 * Simula un estudiante queriendo acceder al grupo que representa la universidad
 */
 
public class Estudiante implements DiscoveryListener {
    private PeerGroup netPeerGroup = null;
    private GroupManager groupManager = null;
    private final static MimeMediaType XMLMIMETYPE = new MimeMediaType("text/xml");

    
    /*
     * Método main que arranca la aplicación
     */
    public static void main(String args[]){
        Estudiante sample = new Estudiante();
        sample.runJXTA();
    }

    /*
     * Método que arranca la plataforma JXTA
     */
    private void runJXTA(){
        //Se obtiene una instancia del NetPeerGroup
        try {
            NetworkManager manager = new NetworkManager(NetworkManager.ConfigMode.EDGE, "Estudiante", new File(new File(".cache"), "Estudiante").toURI());
            manager.startNetwork();
            //Obtenemos el NetPeerGroup (grupo por defecto al que pertenece el peer al conectarse a la red)
            netPeerGroup = manager.getNetPeerGroup();

            groupManager = new GroupManager(netPeerGroup);
        } catch (Exception e) {
            System.err.println("Error al obtener una instancia del NetPeerGroup");
            System.exit(1);
        }

        //Hacemos uso de Peer Discovery para recibir los mensajes que avisan de la publicación del nuevo grupo
        try{
            System.out.println("Enviando un mensaje para descubrir grupos");
            DiscoveryService discovery = netPeerGroup.getDiscoveryService();
            discovery.addDiscoveryListener(this);
            discovery.getRemoteAdvertisements(null, DiscoveryService.GROUP, "Name", "Universidad", 1, null);
        } catch (Exception e){
            System.err.println("Error esperando un mensaje de anuncio");
            e.printStackTrace();
        }
       
        /*
        //Se crea un nuevo módulo de anuncios ModuleImplAdvertisement
        ModuleClassID mID = IDFactory.newModuleClassID(); 
        ModuleSpecID moduleSpecID = PeerGroup.refMembershipSpecID;
        ModuleImplAdvertisement adv = (ModuleImplAdvertisement) AdvertisementFactory.newAdvertisement(ModuleImplAdvertisement.getAdvertisementType());
        adv.setModuleSpecID(moduleSpecID);
        adv.setCompat(GroupManager.stdCompatStatement);
        adv.setCode("Estudiante");
        adv.setDescription("Módulo del estudiante");
        adv.setUri("https://github.com/Kookaburra99/EjemplosJXTA");
        try{
            netPeerGroup.loadModule(mID,adv);
            init(netPeerGroup, null, null);
        } catch (ProtocolNotSupportedException protocolException) {
            System.err.println("No se pudo cargar el módulo");
            protocolException.printStackTrace();
            System.exit(1);
        } catch (PeerGroupException peerGroupException) {
            System.err.println("No se pudo cargar el módulo");
            peerGroupException.printStackTrace();
            System.exit(1);
        }
        */
    }           
    
    /**
     * Método llamado cuando nuestro mensaje de descubrimiento recibe una respuesta (un aviso de otro peer)
     */
    public void discoveryEvent(DiscoveryEvent ev) {
        //Variable en la que almacenaremos el aviso de la universidad, si lo recibimos
        PeerGroupAdvertisement avisoUni = null;
        //Variable en la que almacenamos la referencia al grupo de la universidad, si lo recibimos
        PeerGroup grupoUni = null;
        //Obtenemos el mensaje de la respuesta
        DiscoveryResponseMsg res = ev.getResponse();
        System.out.println("[ Se obtuvo una Discovery Response (respuesta de descubrimiento) {" + res.getResponseCount() + " elementos} del peer: " + ev.getSource() + " ]");
        
        //Mostramos el contenido de los avisos
        PeerGroupAdvertisement adv = null;
        Enumeration en = res.getAdvertisements();
        if (en != null && en.hasMoreElements()) {
            adv = (PeerGroupAdvertisement) en.nextElement();
            System.out.println(adv);
            //Si el aviso es el de creación del grupo de la universidad, lo almacenamos
            if(adv.getName().equals("Universidad")){
                avisoUni = adv;
            }
        }

        if(avisoUni != null){
            try{
                grupoUni = netPeerGroup.newGroup(avisoUni);
                groupManager.joinGroup(grupoUni, null, null);
            } catch (Exception e){
                System.err.println("Error uniéndose al grupo");
                e.printStackTrace();
            }
            
        }
    }

    /*
     * Método llamado cuando se inincializa el módulo
     * Aquí es donde se llama al GroupManager
     */
    /*
    public void init(PeerGroup parentGroup, ID assignedID, Advertisement implAdv) throws PeerGroupException{                 
        GroupManager manager = new GroupManager(parentGroup);
        PeerGroup p = null;
        try{
            //Se crea el grupo correspondiente a la universidad
            p = manager.addGroup("Universidad", "AdministracionUniversitaria", "Representa al conjunto de estudiantes de la universidad", null);
             
            System.out.println("~~~~~ Grupo de la Universidad publicado correctamente ~~~~~");
        } catch (Exception e) {
            System.err.println("Error creando el grupo");
            e.printStackTrace();
        }
        try{
            //Se envía un formulario para unirse al grupo al departamento de mantenimiento
            StructuredDocument preAppForm = PreApplicationForm.createPreAppForm(PreApplicationForm.DEPARTMENT_MANAGEMENT);
            manager.joinGroup(p, preAppForm, "StringAuthentication");
        } catch(Exception e) {
           System.err.println("Error uniéndose al grupo en el departamento de mantenimiento");
        }

        //Se envía un formulario para unirse al grupo al departamento de ciencias de la computación
        try{
            StructuredDocument preAppForm = PreApplicationForm.createPreAppForm(PreApplicationForm.DEPARTMENT_COMPUTER_SCIENCE);
            manager.joinGroup(p, preAppForm, "StringAuthentication");
        } catch(Exception e) {
            System.err.println("Error uniéndose al grupo en el departamento de ciencias de la computación");
        }
    }
    */

    /*
     * No se usa en este ejemplo
     */
    public int startApp(String[] args){
        return 0;
    }
    public void stopApp(){};
    
}        
    
