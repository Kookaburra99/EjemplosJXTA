/**
 * Ejemplo de implementación del Peer Membership Protocol de JXTA
 * Basado en las indicaciones del libro "JXTA: Java P2P Programming", Daniel Brookshier et al., Sams Publishing, 2002
 */

import net.jxta.document.Element;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredDocumentFactory;


public abstract class PreApplicationForm {
    
    public static final String DEPARTMENT_COMPUTER_SCIENCE = "CS";
    public static final String DEPARTMENT_MANAGEMENT = "MT";
    public static final String DEPARTMENT_ARCHITECTURE = "AT";

    public static final StructuredDocument createPreAppForm(String department){       
        MimeMediaType type = new MimeMediaType("text","xml");
        StructuredDocument doc = StructuredDocumentFactory.newStructuredDocument( type,"PreAppForm" );
        Element e = doc.createElement("Department", department); 
        doc.appendChild(e);
        return doc;
        
   }
}
