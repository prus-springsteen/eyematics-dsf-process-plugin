package org.eyematics.process.tools.generator.util;

public class FHIRServer {
    private String dic;
    private String localPort;
    private String commonPort;

    public FHIRServer(String dic, String localPort, String commonPort) {
        this.dic = dic;
        this.localPort = localPort;
        this.commonPort = commonPort;
    }

    public String getDic() {
        return this.dic;
    }

    public String getLocalPort() {
        return this.localPort;
    }

    public String getCommonPort() {
        return this.commonPort;
    }

    public void setDic(String dic) {
        this.dic = dic;
    }

    public void setLocalPort(String localPort) {
        this.localPort = localPort;
    }

    public void setCommonPort(String commonPort) {
        this.commonPort = commonPort;
    }

    @Override
    public String toString() {
        return "FHIRServer{" +
                "dic='" + dic + '\'' +
                ", localPort='" + localPort + '\'' +
                ", commonPort='" + commonPort + '\'' +
                '}';
    }
}
