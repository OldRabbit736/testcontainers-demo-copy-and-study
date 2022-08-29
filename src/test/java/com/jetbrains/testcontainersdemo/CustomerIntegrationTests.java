package com.jetbrains.testcontainersdemo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
public class CustomerIntegrationTests {

    @Autowired
    private CustomerDao customerDao;

    @Container
    private static final MySQLContainer<?> container = (MySQLContainer<?>) new MySQLContainer("mysql:8.0.26")
            /*
            withReuse는 컨테이너가
             */
            .withReuse(true);

            /*
            이 부분은 따로 지정해 주지 않으면 testcontainer가 자동으로 정해준다.
            그리고 그렇게 하는 편이 낫다. 아래 @DynamicPropertySource에서 설명됨.
             */
            //.withDatabaseName("somedatabase")
            //.withUsername("root")
            //.withPassword("letsgomarco");

    /*
    @Container
    private static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:latest");
     */

    @DynamicPropertySource
    public static void overrideProps(DynamicPropertyRegistry registry) {
        /*
        db name, port mapping, uesrname, password는 사실 테스트 수행하는 입장에서는 알 필요가 없다.
        그냥 연결되서 테스트 수행하는 부분이 중요하며, 어떤 port, username, password로 연결되었는지는 중요하지 않다.
        따라서 이런 부분은 내부적으로 testcontainer가 정하도록 하고, 컨테이너가 로드된 후,
        spring application이 로드될 시점에 해당 연결 정보를 동적으로 얻으면 되는 것이다. 바로 아래와 같이.
        따라서 위에 container를 초기화 할 때 .withDatabaseName, withUsername 등등은 정할 필요가 보통은 없다.

        https://www.testcontainers.org/features/networking/
        정해줘야 할 필요가 있다면 GenericContainer를 사용할 때 withExposedPorts 같은 항목이다.
        이 withExposedPorts는 testcontainer에게 서비스가 컨테이너 내부에서 어떤 포트로 서비스되는지 알려주는 역할을 한다.
        그래야 해당 포트를 이용해 port mapping을 할 수 있으며 또한 해당 서비스가 부트가 되었는지 해당 포트를 향해 polling 할 수 있기 때문이다.
        이렇듯, GenericContainer 일 경우 해당 서비스가 어떤 포트로 서비스되는지 testcontainers가 알 수 있는 방법이 없으므로
        사용자가 지정해 줘야 한다.
         */
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
    }

    @Test
    void when_using_a_clean_db_this_should_be_empty() throws IOException, InterruptedException {
//        container.withClasspathResourceMapping(
//                "application.properties",
//                "/temp/application.properties",
//                BindMode.READ_ONLY
//        );
        //container.withFileSystemBind("/my/directory", "/tmp", BindMode.READ_ONLY);
        //container.execInContainer("ls", "-la");
        //String logs = container.getLogs(OutputFrame.OutputType.STDOUT);
        //container.withLogConsumer(new Slf4jLogConsumer());
        //Integer mappedPort = container.getMappedPort(3306);

        List<Customer> customers = customerDao.findAll();
        assertThat(customers).hasSize(2);
    }
}
