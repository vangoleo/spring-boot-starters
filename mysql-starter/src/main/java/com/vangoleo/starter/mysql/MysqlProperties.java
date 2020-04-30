package com.vangoleo.starter.mysql;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.datasource")
public class MysqlProperties {

    private String url;
    private String username;
    private String password;

    private String threadPoolName = "inhope-hikari";

    // 解决mysql5.5升级到mysql5.7时编码问题
    private String connectionInitSql = "set names utf8mb4;";

    //连接在池中闲置超过这个时间，则删除。0表示空闲连接不删除，默认值10分钟
    private Integer idleTimeout = 60000;


    public String getUrl() {
        return this.url;
    }

    public String getUsername() {
       return this.username;
    }

    public String getPassword() {
        return this.password;
    }


    public void setUrl(String url) {
        this.url = url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getThreadPoolName() {
        return threadPoolName;
    }

    public void setThreadPoolName(String threadPoolName) {
        this.threadPoolName = threadPoolName;
    }

    public String getConnectionInitSql() {
        return connectionInitSql;
    }

    public void setConnectionInitSql(String connectionInitSql) {
        this.connectionInitSql = connectionInitSql;
    }

    public Integer getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(Integer idleTimeout) {
        this.idleTimeout = idleTimeout;
    }
}
