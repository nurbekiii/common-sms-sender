package soul.smpp.event;

import soul.smpp.Connection;
import soul.smpp.message.SMPPPacket;

class NotificationDetails {
    private Connection connection;
    private SMPPEvent event;
    private SMPPPacket packet;

    public NotificationDetails() {
    }

    public Connection getConnection() {
        return connection;
    }


    public void setConnection(Connection conn) {
        this.connection = conn;
    }


    public SMPPEvent getEvent() {
        return event;
    }


    public void setEvent(SMPPEvent event) {
        this.event = event;
    }


    public SMPPPacket getPacket() {
        return packet;
    }


    public void setPacket(SMPPPacket pak) {
        this.packet = pak;
    }


    public void setDetails(Connection c, SMPPEvent e, SMPPPacket p) {
        connection = c;
        event = e;
        packet = p;
    }

    public boolean hasEvent() {
        return event != null;
    }
}
