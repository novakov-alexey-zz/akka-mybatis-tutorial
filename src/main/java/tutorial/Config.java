package tutorial;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.inject.Named;
import javax.sql.DataSource;

import static tutorial.spring.SpringExtension.SpringExtProvider;

@ComponentScan
@Configuration
public class Config {
  @Autowired
  private ApplicationContext applicationContext;
  @Value("classpath*:/tutorial/dal/**/*.xml")
  private Resource[] mappers;

  @Bean
  public DataSource dataSource() {
    PooledDataSource dataSource = new PooledDataSource();
    dataSource.setDriver("com.mysql.jdbc.Driver");
    dataSource.setUrl("jdbc:mysql://192.168.99.100:3306/somedb?autoReconnect=true&useSSL=false");
    dataSource.setUsername("root");
    dataSource.setPassword("root");
    dataSource.setPoolMaximumActiveConnections(5);
    dataSource.setPoolMaximumIdleConnections(3);
    return dataSource;
  }

  @Bean
  public SqlSessionFactory sqlSessionFactory() throws Exception {
    return new SqlSessionFactoryBean() {
      {
        setDataSource(dataSource());
        setMapperLocations(mappers);
        setTypeHandlersPackage("tutorial.dal");
      }
    }.getObject();
  }

  @Bean
  public SqlSessionTemplate sqlSessionTemplate() throws Exception {
    return new SqlSessionTemplate(sqlSessionFactory());
  }

  @Bean
  public ActorSystem actorSystem() {
    ActorSystem system = ActorSystem.create("AkkaJavaSpring");
    // initialize the application context in the Akka Spring Extension
    SpringExtProvider.get(system).initialize(applicationContext);
    return system;
  }

  @Bean
  @Named("OrderProcessorActor")
  public ActorRef orderProcessor() {
    return actorSystem().actorOf(SpringExtProvider.get(actorSystem()).props("OrderProcessorActor"), "orderProcessor");
  }

  @Bean
  @Named("OrderIdGenerator")
  public ActorRef orderIdGenerator() {
    return actorSystem().actorOf(SpringExtProvider.get(actorSystem()).props("OrderIdGenerator"), "orderIdGenerator");
  }
}
