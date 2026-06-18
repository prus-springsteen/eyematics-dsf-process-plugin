package org.eyematics.process.tools.generator.util;

public class FHIRServer {
    private String dic;
    private String localPort;
    private String demonstratorPort;

    public FHIRServer(String dic, String localPort, String demonstratorPort) {
        this.dic = dic;
        this.localPort = localPort;
        this.demonstratorPort = demonstratorPort;
    }

    public String getDic() {
        return this.dic;
    }

    public String getLocalPort() {
        return this.localPort;
    }

    public String getDemonstratorPort() {
        return this.demonstratorPort;
    }

    public void setDic(String dic) {
        this.dic = dic;
    }

    public void setLocalPort(String localPort) {
        this.localPort = localPort;
    }

    public void setDemonstratorPort(String demonstratorPort) {
        this.demonstratorPort = demonstratorPort;
    }

    @Override
    public String toString() {
        return "FHIRServer{" +
                "dic='" + dic + '\'' +
                ", localPort='" + localPort + '\'' +
                ", demonstratorPort='" + demonstratorPort + '\'' +
                '}';
    }
}
