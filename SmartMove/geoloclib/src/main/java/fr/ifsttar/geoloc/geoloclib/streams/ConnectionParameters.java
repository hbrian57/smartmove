package fr.ifsttar.geoloc.geoloclib.streams;

public class ConnectionParameters
{
    private String host;
    private int port;
    private String user;
    private String password;

    //----------------------------------------------------------------------------------------------

    /**
     * Constructor.
     */
    public ConnectionParameters()
    {

    }

    //----------------------------------------------------------------------------------------------


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}