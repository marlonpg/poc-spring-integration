# Spring Integration

## 1. What is Spring Integration?

Spring Integration is a framework that extends the Spring programming model to support Enterprise Integration Patterns (EIP). It provides a lightweight messaging framework within Spring-based applications, enabling integration with external systems through declarative adapters.

Spring Integration implements common integration patterns such as:
- Message routing
- Transformation
- Filtering
- Aggregation
- Splitting
- Service activation

## 2. Why Do I Need This?

### Features

- **Message-driven architecture**: Asynchronous processing with channels and endpoints
- **Enterprise Integration Patterns**: Pre-built implementations of common integration patterns
- **Adapter support**: Connect to various external systems (JMS, HTTP, FTP, Email, etc.)
- **Transformation**: Convert messages between different formats
- **Routing**: Direct messages to appropriate handlers based on content or headers
- **Error handling**: Comprehensive error handling and retry mechanisms
- **Testing support**: Built-in testing utilities for integration flows

### Benefits

- **Loose coupling**: Components communicate through messages, reducing dependencies
- **Scalability**: Asynchronous processing improves application performance
- **Maintainability**: Clear separation of concerns and standardized patterns
- **Reusability**: Common integration logic can be shared across applications
- **Testability**: Easy to unit test individual components in isolation
- **Spring ecosystem**: Seamless integration with other Spring projects

### Architecture Deep Dive

Spring Integration is built around several core concepts:

#### Message
The unit of data transfer containing:
- **Payload**: The actual data being transferred
- **Headers**: Metadata about the message (timestamp, correlation ID, etc.)

#### Channel
The conduit through which messages flow:
- **Point-to-Point**: Direct channel between producer and consumer
- **Publish-Subscribe**: Broadcast messages to multiple subscribers
- **Queue**: Buffered channel for asynchronous processing

#### Endpoint
Components that connect channels to external systems or business logic:
- **Inbound**: Receive data from external systems
- **Outbound**: Send data to external systems
- **Service Activator**: Invoke business logic methods

#### Gateway
Provides a simple interface to the messaging system, hiding complexity from business code.

#### Flow Architecture
```
[External System] → [Inbound Adapter] → [Channel] → [Transformer] → [Channel] → [Service Activator] → [Channel] → [Outbound Adapter] → [External System]
```

## 3. Code Examples

### Basic Setup

Add dependency to `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.integration</groupId>
    <artifactId>spring-integration-core</artifactId>
    <version>6.2.0</version>
</dependency>
```

### Simple Message Flow

```java
@Configuration
@EnableIntegration
public class IntegrationConfig {

    @Bean
    public MessageChannel inputChannel() {
        return MessageChannels.direct().get();
    }

    @Bean
    public MessageChannel outputChannel() {
        return MessageChannels.direct().get();
    }

    @Bean
    @ServiceActivator(inputChannel = "inputChannel", outputChannel = "outputChannel")
    public MessageHandler messageProcessor() {
        return message -> {
            String payload = (String) message.getPayload();
            System.out.println("Processing: " + payload.toUpperCase());
        };
    }
}
```

### File Processing Example

```java
@Configuration
public class FileIntegrationConfig {

    @Bean
    @InboundChannelAdapter(value = "fileInputChannel", poller = @Poller(fixedDelay = "1000"))
    public MessageSource<File> fileReadingMessageSource() {
        FileReadingMessageSource source = new FileReadingMessageSource();
        source.setDirectory(new File("input"));
        source.setFilter(new SimplePatternFileListFilter("*.txt"));
        return source;
    }

    @Bean
    @Transformer(inputChannel = "fileInputChannel", outputChannel = "processedChannel")
    public FileToStringTransformer fileToStringTransformer() {
        return new FileToStringTransformer();
    }

    @Bean
    @ServiceActivator(inputChannel = "processedChannel")
    public MessageHandler fileProcessor() {
        return message -> {
            String content = (String) message.getPayload();
            System.out.println("File content: " + content);
        };
    }
}
```

### HTTP Integration Example

```java
@RestController
public class HttpGatewayController {

    @Autowired
    private HttpGateway httpGateway;

    @PostMapping("/process")
    public String processMessage(@RequestBody String message) {
        return httpGateway.process(message);
    }
}

@MessagingGateway
interface HttpGateway {
    @Gateway(requestChannel = "httpInputChannel")
    String process(String message);
}

@Configuration
public class HttpIntegrationConfig {

    @Bean
    @Transformer(inputChannel = "httpInputChannel", outputChannel = "httpOutputChannel")
    public GenericTransformer<String, String> upperCaseTransformer() {
        return String::toUpperCase;
    }

    @Bean
    @ServiceActivator(inputChannel = "httpOutputChannel")
    public GenericHandler<String> responseHandler() {
        return (payload, headers) -> "Processed: " + payload;
    }
}
```

### JMS Integration Example

```java
@Configuration
public class JmsIntegrationConfig {

    @Bean
    @ServiceActivator(inputChannel = "jmsInputChannel")
    public JmsOutboundChannelAdapter jmsOutbound(ConnectionFactory connectionFactory) {
        JmsOutboundChannelAdapter adapter = new JmsOutboundChannelAdapter(connectionFactory);
        adapter.setDestinationName("output.queue");
        return adapter;
    }

    @Bean
    public JmsInboundChannelAdapter jmsInbound(ConnectionFactory connectionFactory) {
        JmsInboundChannelAdapter adapter = new JmsInboundChannelAdapter(connectionFactory);
        adapter.setDestination("input.queue");
        adapter.setOutputChannel(jmsInputChannel());
        return adapter;
    }

    @Bean
    public MessageChannel jmsInputChannel() {
        return MessageChannels.direct().get();
    }
}
```

### DSL Configuration Example

```java
@Configuration
public class IntegrationFlowConfig {

    @Bean
    public IntegrationFlow fileProcessingFlow() {
        return IntegrationFlows
            .from(Files.inboundAdapter(new File("input"))
                .patternFilter("*.csv")
                .autoCreateDirectory(true),
                e -> e.poller(Pollers.fixedDelay(5000)))
            .transform(Files.toStringTransformer())
            .split(s -> s.delimiters("\n"))
            .filter("payload.length() > 0")
            .transform(String.class, String::toUpperCase)
            .aggregate()
            .handle(Files.outboundAdapter(new File("output"))
                .fileNameGenerator(m -> "processed_" + System.currentTimeMillis() + ".txt"))
            .get();
    }
}
```

### Testing Integration Flows

```java
@SpringBootTest
@DirtiesContext
class IntegrationFlowTest {

    @Autowired
    private MessageChannel inputChannel;

    @Autowired
    private PollableChannel outputChannel;

    @Test
    void testMessageFlow() {
        Message<String> message = MessageBuilder.withPayload("test message").build();
        inputChannel.send(message);
        
        Message<?> result = outputChannel.receive(1000);
        assertThat(result).isNotNull();
        assertThat(result.getPayload()).isEqualTo("TEST MESSAGE");
    }
}
```

## Getting Started

1. Add Spring Integration dependency to your project
2. Enable integration with `@EnableIntegration`
3. Define channels, endpoints, and message flows
4. Configure adapters for external system integration
5. Test your integration flows

Spring Integration provides a powerful, flexible framework for building robust integration solutions within the Spring ecosystem.