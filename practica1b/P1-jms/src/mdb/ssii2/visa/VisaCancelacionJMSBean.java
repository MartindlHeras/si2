/**
 * Pr&aacute;ctricas de Sistemas Inform&aacute;ticos II
 * VisaCancelacionJMSBean.java
 */

package ssii2.visa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.ejb.ActivationConfigProperty;
import javax.jms.MessageListener;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.JMSException;
import javax.annotation.Resource;
import java.util.logging.Logger;

/**
 * @author jaime
 */
@MessageDriven(mappedName = "jms/VisaPagosQueue")
public class VisaCancelacionJMSBean extends DBTester implements MessageListener {
  static final Logger logger = Logger.getLogger("VisaCancelacionJMSBean");
  @Resource
  private MessageDrivenContext mdc;

  private static final String UPDATE_CANCELA_QRY = "update pago " +
                                                "set codRespuesta=999 " +
                                                "where pago.idAutorizacion=?";
  private static final String UPDATE_RECTIFICA_QRY = "update tarjeta " +
                                                "set saldo=? " +
                                                "where tarjeta.numeroTarjeta=?";


  public VisaCancelacionJMSBean() {
  }

  // TODO : Método onMessage de ejemplo
  // Modificarlo para ejecutar el UPDATE definido más arriba,
  // asignando el idAutorizacion a lo recibido por el mensaje
  // Para ello conecte a la BD, prepareStatement() y ejecute correctamente
  // la actualización
  public void onMessage(Message inMessage) {
      PreparedStatement pstmt = null;
      TextMessage msg = null;
      Connection con = null;
      ResultSet rs = null;
      String numTarjeta = "";
      Double importe = 0;
      Double saldo = 0;

      try {

          con = getConnection();
          if (inMessage instanceof TextMessage) {
              msg = (TextMessage) inMessage;
              logger.info("MESSAGE BEAN: Message received: " + msg.getText());

             String query = UPDATE_CANCELA_QRY;
             String idAutorizacion = msg.getText();

             pstmt = con.prepareStatement(query);
             pstmt.setInt(1, Integer.parseInt(idAutorizacion));
             pstmt.executeUpdate();

             query = "select numeroTarjeta, importe " +
                    "from Pago " +
                    "where pago.idAutorizacion=?";

             pstmt = con.prepareStatement(query);
             pstmt.setInt(1, Integer.parseInt(idAutorizacion));
             rs = pstmt.executeQuery();

             if (rs.next()) {
                 importe = rs.getDouble("importe");
                 tarjeta = rs.getString("numeroTarjeta");
             }
             else {
                 throw new EJBException("Cancelacion no realizada: id no autorizado");
             }

             query = "select saldo " +
                "from tarjeta " +
                "where numeroTarjeta=?";

             pstmt = con.prepareStatement(query);
             pstmt.setString(1, tarjeta);
             rs = pstmt.executeQuery();

             if (rs.next()) {
                 saldo = rs.getDouble("saldo");
             }
             else {
                 throw new EJBException("Cancelacion anulada: no se encuentra el saldo de la tarjeta");
             }

             saldo = saldo + importe;

             query = UPDATE_RECTIFICA_QRY;
             pstmt.con.prepareStatement(query);
             pstmt.setDouble(1, saldo);
             pstmt.setString(2, tarjeta);
             pstmt.executeUpdate();
             

          } else {
              logger.warning(
                      "Message of wrong type: "
                      + inMessage.getClass().getName());
          }
      } catch (JMSException e) {
          e.printStackTrace();
          mdc.setRollbackOnly();
      } catch (Throwable te) {
          te.printStackTrace();
      }
  }


}
