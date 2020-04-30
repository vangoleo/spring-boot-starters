package com.vangoleo.starter.mysql;

import com.github.pagehelper.PageInterceptor;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.util.StringUtils;
import tk.mybatis.spring.mapper.MapperScannerConfigurer;

import javax.sql.DataSource;
import java.util.Properties;

@EnableConfigurationProperties({com.vangoleo.starter.mysql.MysqlProperties.class})
public class MysqlAutoConfiguration implements EnvironmentAware {

    Environment environment;

    private final static String MAPPER_PACKAGE_KEY = "spring.datasource.scanPackage";
    private final static String COMMON_MAPPER_PACKAGE_KEY = "mapper.mappers";

    @Primary
    @Bean("dataSource")
    public DataSource dataSource(@Autowired com.vangoleo.starter.mysql.MysqlProperties mysqlProperties) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(mysqlProperties.getUrl());
        config.setUsername(mysqlProperties.getUsername());
        config.setPassword(mysqlProperties.getPassword());
        config.setDriverClassName("com.mysql.jdbc.Driver");
        // 参考 https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration
        //是否自定义配置，为true时prepStmtCacheSize与prepStmtCacheSqlLimit才能生效
        config.addDataSourceProperty("cachePrepStmts", "true");
        //连接池大小默认25，官方推荐250-500
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        //单条语句最大长度默认256，官方推荐2048
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        //新版本MySQL支持服务器端准备，开启能够得到显著性能提升
        config.addDataSourceProperty("useServerPrepStmts", "true");
        // 最小空闲的连接数
        config.setMinimumIdle(1);
        //最大的连接数目。超过这个数目，新的数据库访问线程会被阻塞
        config.setMaximumPoolSize(20);
        config.setPoolName(mysqlProperties.getThreadPoolName());
        config.setIdleTimeout(mysqlProperties.getIdleTimeout());
        config.setConnectionInitSql(mysqlProperties.getConnectionInitSql());
        return new HikariDataSource(config);
    }

    @Primary
    @Bean("sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("dataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        //默认路径，需要跟业务方沟通，固定该格式
        factoryBean.setMapperLocations(resolver.getResources("classpath:mapper/*.xml"));
        factoryBean.setPlugins(new Interceptor[]{createPageHelper()});
        return factoryBean.getObject();
    }

    /**
     * 首先被加载的Bean, 并且加载该bean之前，properties还未进行注入
     * @return
     */
    @Bean
    public MapperScannerConfigurer mapperScannerConfigurer() {
        MapperScannerConfigurer configurer = new MapperScannerConfigurer();
        configurer.setSqlSessionFactoryBeanName("sqlSessionFactory");
        //获取要扫描的mapper接口对应的包路径
        configurer.setBasePackage(environment.getProperty(MAPPER_PACKAGE_KEY));
        Properties properties = new Properties();
        properties.setProperty("not-empty", "false");
        properties.setProperty("identity", "MYSQL");
        String commonMappers = environment.getProperty(COMMON_MAPPER_PACKAGE_KEY);
        if (!StringUtils.isEmpty(commonMappers)) {
            properties.setProperty("mappers", commonMappers);
        }
        configurer.setProperties(properties);
        return configurer;
    }

    @Primary
    @Bean(value = "transactionManager")
    public DataSourceTransactionManager createTransactionManager(@Qualifier("dataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public PageInterceptor createPageHelper() {
        PageInterceptor pageInterceptor = new PageInterceptor();
        Properties properties = new Properties();
        properties.setProperty("helperDialect", "mysql");
        properties.setProperty("reasonable", "false");
        properties.setProperty("supportMethodsArguments", "true");
        properties.setProperty("params", "count=countSql");
        pageInterceptor.setProperties(properties);
        return pageInterceptor;
    }


    @Override
    public void setEnvironment(Environment environment) {
        //enviroment中，可以获取到properties中所有的配置
        this.environment = environment;
    }
}
