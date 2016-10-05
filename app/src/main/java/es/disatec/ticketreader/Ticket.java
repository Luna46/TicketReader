package es.disatec.ticketreader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Pepe on 04/10/2016.
 */
public class Ticket {

    private Date fecha;
    private Integer idticket;
    private String uid;
    private String grupo;
    private String comercio;
    private String ticket;


    public Ticket() {
    }

    public Ticket(JSONObject obj) {
        this.fromJSON(obj);
    }

    public Ticket(Integer idticket, String uid) {
        this.idticket = idticket;
        this.uid = uid;
    }

    public Ticket(Integer idticket) {
        this.idticket = idticket;
    }

    public Integer getIdticket() {
        return idticket;
    }

    public void setIdticket(Integer idticket) {
        this.idticket = idticket;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    public String getComercio() {
        return comercio;
    }

    public void setComercio(String comercio) {
        this.comercio = comercio;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idticket != null ? idticket.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Ticket)) {
            return false;
        }
        Ticket other = (Ticket) object;
        if ((this.idticket == null && other.idticket != null) || (this.idticket != null && !this.idticket.equals(other.idticket))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "disatec.ticket.Ticket[ idticket=" + idticket + " ]";
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public JSONObject toJSON()
    {
        try {
            JSONObject obj = new JSONObject();
            obj.put("id", this.getIdticket());
            obj.put("grupo", this.getGrupo());
            obj.put("comercio", this.getComercio());
            obj.put("fecha", this.getFecha());
            obj.put("ticket", this.getTicket());

            return obj;
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }

        return null;
    }


    public void fromJSON(JSONObject obj)
    {
        try
        {
            this.setIdticket(obj.getInt("id"));
            this.setGrupo(obj.getString("grupo"));
            this.setComercio(obj.getString("comercio"));
            // La fecha la tratamos luego, TODO
            //this.setFecha(obj.getString("fecha"));
            this.setTicket(obj.getString("ticket"));

        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }

    }

}

