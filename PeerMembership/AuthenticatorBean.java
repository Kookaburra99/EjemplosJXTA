/*
 * AuthenticatorBean.java
 */

import net.jxta.credential.AuthenticationCredential;
import net.jxta.membership.Authenticator;
import net.jxta.membership.MembershipService;
import javax.swing.*;
import java.beans.*;
import java.lang.reflect.*;
import java.util.*;


public class AuthenticatorBean extends javax.swing.JDialog {
   Authenticator bean;
   /** Creates new form AuthenticatorBean */
   public AuthenticatorBean(java.awt.Frame parent, boolean modal,Authenticator bean, String title) {
      super(parent, modal);
      this.bean = bean;
      if (bean == null){
         throw new NullPointerException("Authenticator must be provided");
      }
      initComponents();
      try{
         BeanInfo info = Introspector.getBeanInfo(bean.getClass());
         properties = info.getPropertyDescriptors();
         buildDisplay(bean,  properties);
      }catch(java.beans.IntrospectionException ie){
         ie.printStackTrace();
      }
      titleLabel.setText(title);
      //vertabras = vertabraFactory(bean, bean.getClass());
      pack();
      setSize(350,300);
   }
   
   /** This method is called from within the constructor to
    * initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is
    * always regenerated by the Form Editor.
    */
   private void initComponents() {//GEN-BEGIN:initComponents
      titlePanel = new javax.swing.JPanel();
      titleLabel = new javax.swing.JLabel();
      parameterScrollPane = new javax.swing.JScrollPane();
      parameterPanel = new javax.swing.JPanel();
      buttonPanel = new javax.swing.JPanel();
      cancelButton = new javax.swing.JButton();
      submitButton = new javax.swing.JButton();

      addWindowListener(new java.awt.event.WindowAdapter() {
         public void windowClosing(java.awt.event.WindowEvent evt) {
            closeDialog(evt);
         }
      });

      titleLabel.setText("jLabel1");
      titlePanel.add(titleLabel);

      getContentPane().add(titlePanel, java.awt.BorderLayout.NORTH);

      parameterScrollPane.setViewportBorder(new javax.swing.border.TitledBorder("Authentication Requirements"));
      parameterPanel.setLayout(new javax.swing.BoxLayout(parameterPanel, javax.swing.BoxLayout.Y_AXIS));

      parameterPanel.setBorder(new javax.swing.border.EtchedBorder());
      parameterScrollPane.setViewportView(parameterPanel);

      getContentPane().add(parameterScrollPane, java.awt.BorderLayout.CENTER);

      cancelButton.setText("Cancel");
      cancelButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            cancelButtonActionPerformed(evt);
         }
      });

      buttonPanel.add(cancelButton);

      submitButton.setText("Submit");
      submitButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            submitButtonActionPerformed(evt);
         }
      });

      buttonPanel.add(submitButton);

      getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);

      pack();
   }//GEN-END:initComponents
   
   private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
      setVisible(false);
      dispose();
   }//GEN-LAST:event_cancelButtonActionPerformed
   
   private void submitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitButtonActionPerformed
      setVisible(false);
      try{
         authenticate( bean );
      }catch( Exception e){
         e.printStackTrace();
      }
      dispose();
   }//GEN-LAST:event_submitButtonActionPerformed
   
   /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
       setVisible(false);
       dispose();
    }//GEN-LAST:event_closeDialog
    
    Method [] methods;
    protected PropertyEditor editors[];
    protected JComponent views[];
    //protected Object target;
    protected PropertyDescriptor[] properties;
    private void authenticate( Authenticator authenticator ) throws Exception {
       System.out.println("**** Starting authentication ******");
       for( int i = 0; i < properties.length; i++ ) {
          
          Method getter = properties[i].getWriteMethod();
          JComponent view = views[i];
          Object value;
          if (view instanceof JTextField){
             value = ((JTextField)view).getText();
          }else  if (view instanceof JPasswordField){
             value = ((JPasswordField)view).getPassword();
          }else  if (view instanceof JComboBox){
             value = ((JComboBox)view).getSelectedItem();
          }else{
             continue;
             //throw new RuntimeException("GUI Type not implemented:"+view.getClass());
          }
          Class[] paramType = getter.getParameterTypes();
          
          Object input = null;
          if (paramType[0].isPrimitive()){
             if (paramType[0].getName().equals("boolean")){
                input = new Boolean(value.toString());
             } else if (paramType[0].getName().equals("int")){
                input = new Integer(value.toString());
             } else if (paramType[0].getName().equals("float")){
                input = new Float(value.toString());
             } else if (paramType[0].getName().equals("double")){
                input = new Double(value.toString());
             } else if (paramType[0].getName().equals("short")){
                input = new Short(value.toString());
             } else if (paramType[0].getName().equals("byte")){
                input = new Byte(value.toString());
             } else if (paramType[0].getName().equals("long")){
                input = new Long(value.toString());
             }
          }else if (paramType[0].isAssignableFrom( Boolean.class)){
             input = new Boolean(value.toString());
          }else if (paramType[0].isAssignableFrom(String.class)){
             input = value.toString();
          }else if (paramType[0].isAssignableFrom( Short.class)){
             input = new Short(value.toString());
          }else if (paramType[0].isAssignableFrom(  Byte.class)){
             input = new Byte(value.toString());
          }else if (paramType[0].isAssignableFrom(  Character.class)){
             char[] val = value.toString().toCharArray();
             if (val.length >= 1){
                input = new Character(val[0]);
             }else{
                input = new Character('?');
             }
          }else if (paramType[0].isAssignableFrom( Integer.class)){
             input = new Integer(value.toString());
          }else if (paramType[0].isAssignableFrom( Long.class)){
             input = new Long(value.toString());
          }else if (paramType[0].isAssignableFrom( Float.class)){
             input = new Float(value.toString());
          }else if (paramType[0].isAssignableFrom( Double.class)){
             input = new Double(value.toString());
          }else{ // hope types match
             System.out.println("*** Failed to find the class. Type:"+paramType[0].getName()+"***");
             input = value;
          }
          Object [] params = { input };
          try{
             getter.invoke( authenticator, params );
          }catch(Exception e){
             System.out.println("Failure invoking target Method:"+getter.getName()+" Type:"+paramType[0].getName()+" value:'"+input+"'");
             e.printStackTrace();
          }
       }
       System.out.println("**** Authentication Complete******");
       
    }
    
    
    protected void buildDisplay(Object target, PropertyDescriptor[] properties){
       
       editors = new PropertyEditor[properties.length];
       views = new JComponent[properties.length];
       for (int i = 0; i < properties.length; i++) {
          
          // Don't display hidden or expert properties.
          if (properties[i].isHidden() || properties[i].isExpert()) {
             continue;
          }
          
          String name = properties[i].getDisplayName();
          Class type = properties[i].getPropertyType();
          Method getter = properties[i].getReadMethod();
          Method setter = properties[i].getWriteMethod();
          
          // Only attempt to fill in writable properties.
          if ( setter == null) {
             continue;
          }
          
          JComponent view = null;
          // only get values if a getter is available.
          try {
             
             Object args[] = { };
             Object value = null;
             if (getter != null){
                value = getter.invoke(target, args);
             }
             
             PropertyEditor editor = null;
             Class pec = properties[i].getPropertyEditorClass();
             if (pec != null) {
                try {
                   editor = (PropertyEditor)pec.newInstance();
                } catch (Exception ex) {
                   // Drop through.
                }
             }
             if (editor == null) {
                editor = PropertyEditorManager.findEditor(type);
             }
             editors[i] = editor;
             
             // If we can't edit this component, skip it.
             if (editor == null) {
                // If it's a user-defined property we give a warning.
                String getterClass = properties[i].getReadMethod().getDeclaringClass().getName();
                if (getterClass.indexOf("java.") != 0) {
                   System.err.println("Warning: Can't find public property editor for property \""
                   + name + "\".  Skipping.");
                }
                continue;
             }
             
             // Don't try to set null values:
             if (value != null) {
                editor.setValue(value);
             }
             // :o( editor.addPropertyChangeListener(adaptor);
             
             // Now figure out how to display it...
             if (name.equals("password")){
                if (editor.getAsText() != null && !editor.getAsText().equals("null")) {
                   view = new JPasswordField(editor.getAsText(),20);
                } else {
                   view  = new JPasswordField("",20);
                }
                
             } else
                
             /* :o( if (editor.isPaintable() && editor.supportsCustomEditor()) {
                view = new JPanel(frame, editor);
             } else*/ if (editor.getTags() != null) {
                view = new JComboBox(editor.getTags());
             } else if (editor.getAsText() != null && !editor.getAsText().equals("null")) {
                view = new JTextField(editor.getAsText(),20);
             } else {
                view = new JTextField("",20);
             }
          } catch (InvocationTargetException ex) {
             System.err.println("Skipping property " + name + " ; exception on target: " + ex.getTargetException());
             ex.getTargetException().printStackTrace();
             continue;
          } catch (Exception ex) {
             System.err.println("Skipping property " + name + " ; exception: " + ex);
             ex.printStackTrace();
             continue;
          }
          JPanel propertyEntryPanel = new JPanel();
          parameterPanel.add(propertyEntryPanel);
          JLabel label = new JLabel(name, JLabel.RIGHT);
          propertyEntryPanel.add(label);
          views[i] = view;
          propertyEntryPanel.add(views[i]);
       }
    }
    
   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JPanel buttonPanel;
   private javax.swing.JButton submitButton;
   private javax.swing.JLabel titleLabel;
   private javax.swing.JButton cancelButton;
   private javax.swing.JPanel parameterPanel;
   private javax.swing.JScrollPane parameterScrollPane;
   private javax.swing.JPanel titlePanel;
   // End of variables declaration//GEN-END:variables
    
    
    
}// end of AuthenticatorBean

