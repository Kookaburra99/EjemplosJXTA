/**
 * Ejemplo de implementación del Peer Membership Protocol de JXTA
 * Basado en las indicaciones del libro "JXTA: Java P2P Programming", Daniel Brookshier et al., Sams Publishing, 2002
 */

import net.jxta.credential.AuthenticationCredential;
import net.jxta.credential.Credential;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredTextDocument;
import net.jxta.exception.PeerGroupException;
import net.jxta.exception.ProtocolNotSupportedException;
import net.jxta.impl.id.UUID.UUIDFactory;
import net.jxta.impl.protocol.PeerGroupAdv;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.peergroup.PeerGroupFactory;
import net.jxta.impl.peergroup.StdPeerGroupParamAdv;
import net.jxta.membership.Authenticator;
import net.jxta.membership.MembershipService;
import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.ModuleImplAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;

import java.util.Enumeration;
import java.util.Map;

/**
 * Clase genérica para crear nuevos grupos con el Membership Service de JXTA
 * Proporciona una API básica para las funcionalidades de unirse o dejar un grupo
 */
public class GroupManager {
    //El grupo padre del grupo actual que se quiere crear
    protected PeerGroup parent;
    protected DiscoveryService disco;
    protected PeerGroup activeGroup;

    //El tipo mime por defecto para todos los documentos usados en este ejemplo
    public static final String DOCUMENT_MIME_TYPE="text";
    //El tipo base por defecto para todos los documentos usados en este ejemplo
    public static final String DOCUMENT_BASE_TYPE="xml";

    public static final String DOCUMENT_ROOT_ELEMENT="Comp";
    private static final String KEY="Efmt"; 
    private static final String VALUE="JDK1.8";
    private static final String BINDING_KEY="Bind";
    private static final String BINDING_VALUE="V2.0 Ref Impl";

    public static StructuredTextDocument stdCompatStatement = mkCS();
    public String stdUri = "https://github.com/Kookaburra99/EjemplosJXTA";
    public String stdProvider = "kookaburra99";

    /**
     * Constructor de la clase GroupManager
     * @param grupoPadre El grupo padre del grupo que se quiere crear
     */
    public GroupManager(PeerGroup grupoPadre) {
        this.parent = grupoPadre;
        this.disco = grupoPadre.getDiscoveryService();
        this.activeGroup = grupoPadre;
        if(parent == null)
            throw new NullPointerException("Grupo padre nulo!");
    }

    /**
     * Método para crear y publicar un nuevo grupo de peers especificando su información
     */
    public PeerGroup addGroup(String groupName, String groupMembershipClassName, String groupDescription, ModuleImplAdvertisement advertisement) throws Exception{
        //Se comprueba si ya existe otro grupo con el mismo nombre
        PeerGroupID oldPeerGroupID = alreadyExists(groupName);
        if(oldPeerGroupID != null)
            System.out.println("Ya existe un grupo con el mismo nombre con id: " + oldPeerGroupID);
        
        //Si el parámetro "advertisement" es nulo, se crea un nuevo anuncio
        if(advertisement == null) {
            try{
                advertisement = parent.getAllPurposePeerGroupImplAdvertisement();
                StructuredDocument paramDoc = advertisement.getParam();
                StdPeerGroupParamAdv paramAdv = new StdPeerGroupParamAdv(paramDoc);
                //Lista de todos los servicios estándar disponibles
                Map services = paramAdv.getServices();
                //Se crea un ModuleImplAdvertisement para el Membership Service
                ModuleImplAdvertisement moduleAdv = mkImplAdvBuiltin(PeerGroup.refMembershipSpecID,groupMembershipClassName, groupDescription);
                //Se añade este servicio aa la lista de servicios
                services.put(PeerGroup.membershipClassID, moduleAdv);
                paramAdv.setServices(services);
                advertisement.setParam((net.jxta.document.TextElement)paramAdv.getDocument(new net.jxta.document.MimeMediaType(DOCUMENT_MIME_TYPE, DOCUMENT_BASE_TYPE)));
            } catch(PeerGroupException peerGroupException){
                peerGroupException.printStackTrace();
                System.exit(-1);
                System.err.println("Error creando el anuncio");
            } catch(Exception genericException){
                genericException.printStackTrace();
                System.err.println("Error creando el anuncio");
                System.exit(-1);
            }
        }

        PeerGroup peerGroup = null;
        try{
            //Se crea un nuevo ID para el PeerGroup
            PeerGroupID peerGroupID = null;
            if(oldPeerGroupID != null){
                peerGroupID = oldPeerGroupID;
                System.out.println("***** Recuperando al grupo ya existente *****");
            }
            else{
                peerGroupID = net.jxta.id.IDFactory.newPeerGroupID();
                System.out.println("***** Creando el nuevo grupo *****");
            }
            //Se crea e inicializa el PeerGroup
            peerGroup = parent.newGroup(peerGroupID, advertisement, groupName, groupDescription);
            peerGroup.init(this.parent, peerGroupID, advertisement);
        } catch (PeerGroupException peerGroupException){
            peerGroupException.printStackTrace();
            System.err.println("No se ha podido crear el grupo ");
        }
        
        publish(peerGroup, advertisement);
        return peerGroup;
    }
    
    /**
     * Método que crea el ModuleImplAdvertisement en la función addGroup
     */
    private ModuleImplAdvertisement mkImplAdvBuiltin(net.jxta.platform.ModuleSpecID specID, String code, String descr) {                      
        String moduleImplAdvType = ModuleImplAdvertisement.getAdvertisementType();
        ModuleImplAdvertisement implAdvertisement = (ModuleImplAdvertisement) AdvertisementFactory.newAdvertisement(moduleImplAdvType);
        implAdvertisement.setModuleSpecID(specID);
        implAdvertisement.setCompat(stdCompatStatement);
        implAdvertisement.setCode(code);
        implAdvertisement.setUri(stdUri);
        implAdvertisement.setProvider(stdProvider);
        implAdvertisement.setDescription(descr);
        return implAdvertisement;
   }

   /**
    * Método que comprueba si ya existe otro grupo con el mismo nombre
    */
    public PeerGroupID alreadyExists(String name){
        /**
         * Nótese que en esta función se emplea el protocolo Peer Discover
         * Una implementación explicada de este puede verse en el ejemplo PeerDiscovery
         */
        DiscoveryService discovery = parent.getDiscoveryService();
        Enumeration enumeration = null;
        try{
            enumeration = discovery.getLocalAdvertisements(discovery.GROUP, "Name", name);
        } catch(java.io.IOException ioException) {
            System.err.println("Error obteniendo los anuncios locales");
            return null;
        }
    
        //Si el grupo ya existe la numeración no será nula y contendrá más elementos
        if(enumeration != null && enumeration.hasMoreElements())
            return ((PeerGroupAdv)enumeration.nextElement()).getPeerGroupID();
        else
            return null; 
    }

    /**
     * Método que intenta publicar el nuevo PeerGroup creado
     */
    private void publish(PeerGroup child,Advertisement pgAdv) {
        System.out.println("***** Publicando el grupo de peers *****");
      
        DiscoveryService discovery;
        try {
            discovery = parent.getDiscoveryService();
            discovery.publish(pgAdv);
        } catch (java.io.IOException ioException) {
            System.err.println("No se ha podido publicar el grupo");
        }

        System.out.println("+++++ Se ha publicado el grupo correctamente +++++");
    }

    /**
     * Método que crea una credencial para unirse al grupo
     */
    public Credential joinGroup(PeerGroup newGroup, StructuredDocument credentials, String authenticationMethod) throws ProtocolNotSupportedException{
        MembershipService membership = (MembershipService) newGroup.getMembershipService();
        //Se envían las credenciales al Membership Service y devuelve un autenticador
        Authenticator authenticator = applyToMembershipService(newGroup, credentials, authenticationMethod);
        authenticate(authenticator);
        //Se intenta unir al MembershioService
        Credential authenticatedCredential = joinMembershipService(authenticator, newGroup);

        //Actualizamos el grupo activo al nuevo grupo (anteriormente era el grupo padre)
        activeGroup = newGroup;           
        System.out.println("+++++ Unido al grupo correctamente +++++");
        return authenticatedCredential;   
    }

    /**
     * Método para enviar las credenciales al Membership Service
     */
    private Authenticator applyToMembershipService(PeerGroup peerGroup, StructuredDocument credentials, String authenticationMethod) throws ProtocolNotSupportedException{
        Authenticator authenticator = null;
        try {
            AuthenticationCredential authenticationCredential = new AuthenticationCredential(peerGroup, authenticationMethod, credentials);
            MembershipService membership = (MembershipService) peerGroup.getMembershipService();
            authenticator = (Authenticator) membership.apply(authenticationCredential);
        } catch(PeerGroupException peerGroupException) {
            System.err.println("Error en el proceso de aplicación");
        } catch (ProtocolNotSupportedException protocolNotSupportedException){
                protocolNotSupportedException.printStackTrace();
                throw protocolNotSupportedException;  
        }
        return authenticator;                                        
    }

    private void authenticate(Authenticator authenticator){
        AuthenticatorBean viewBean = new AuthenticatorBean(null, true, authenticator, "Introduzca la información necesaria del grupo");
        viewBean.setVisible(true);
    }

    /**
     * Método que permite unirse a un Membership Service
     */
    private Credential joinMembershipService(Authenticator authenticator, PeerGroup peerGroup){                             
        Credential authenticatedCredential = null;
        if(!authenticator.isReadyForJoin()) {
            System.err.println("El autenticador no está listo para unirse");
        }
         
        try{
            MembershipService membership = (MembershipService) peerGroup.getMembershipService();
            authenticatedCredential = membership.join(authenticator);
        } catch(PeerGroupException peerGroupException) {
            System.err.println("Error en el proceso de unirse");
        }
        return authenticatedCredential;                                                     
    }

    /**
     * Método para salir del grupo actual
     */
    public void leaveGroup(){
        MembershipService memberShipService = activeGroup.getMembershipService();
        try{
            memberShipService.resign();    
        } catch(PeerGroupException peerGroupException) {
            System.err.println("Error saliendo del grupo");
        }
        activeGroup = parent;
        purgeGroups();
    }

    /**
     * Limpia todos los grupos de la caché para evitar problemas
     */
    public void purgeGroups(){
        DiscoveryService discovery = parent.getDiscoveryService();
        try{
            discovery.flushAdvertisements(null,DiscoveryService.GROUP);
        } catch (java.io.IOException ioException) {
            System.err.println("Error limpiando los grupos de la caché");
        }
    }

    private static StructuredTextDocument mkCS() {                                
        StructuredTextDocument doc = (StructuredTextDocument) net.jxta.document.StructuredDocumentFactory.newStructuredDocument(MimeMediaType.XMLUTF8, DOCUMENT_ROOT_ELEMENT);
        net.jxta.document.Element e = doc.createElement(KEY, VALUE);
        doc.appendChild(e);
        e = doc.createElement(BINDING_KEY, BINDING_VALUE);
        doc.appendChild(e);
        return doc;
    }

    /**
     * Método que devuelve el grupo activo
     */
    public PeerGroup getActiveGroup(){
        return activeGroup;
    }
}