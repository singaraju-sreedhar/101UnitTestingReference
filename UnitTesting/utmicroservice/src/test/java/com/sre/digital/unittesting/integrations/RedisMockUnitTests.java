package com.sre.digital.unittesting.integrations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.sre.digital.unittesting.model.Item;
import com.sre.digital.unittesting.service.BusinessService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runners.Suite;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(SpringExtension.class)
//@SpringBootTest(classes = { RedisConfig.class, BusinessService.class, Itemrepository.class })
@SpringBootTest
@AutoConfigureMockMvc
public class RedisMockUnitTests {

  /*  private static RedisServer redisServer;
    private static RedissonClient redissonClient;
*/
    /*
    @BeforeAll
    public static void setUp() throws Exception {
        // Find a random port for Redis server
        ServerSocket socket = new ServerSocket(0);
        int redisPort = socket.getLocalPort();
        socket.close();

        // Start Redis server on a random port
        redisServer = new RedisServer(redisPort);
        redisServer.start();

        // Create Redisson client
        Config config = new Config();
        config.useSingleServer().setAddress("redis://localhost:" + redisPort);
        redissonClient = Redisson.create(config);
    }*/



/*
    @AfterAll
    public static void tearDown() throws Exception {
        // Shutdown Redisson client and Redis server
        redissonClient.shutdown();
        redisServer.stop();
    }
*/

    @Autowired
    private BusinessService businessService;


    @MockBean
    private RedisTemplate<String, Item> redisTemplate;

    @MockBean
    ValueOperations<String,Item> valueOperations;

    @MockBean
    RedissonClient redissonClient;
    @MockBean
    RedisKeyValueAdapter redisKeyValueAdapter;

    @Test
    public void testGetFilteredRealItemWithRedisCacheMiss() {
        // Given
        int itemId = 1;
        Item expectedItem = new Item(itemId, "apple", (float) 78.19, 1000);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(Integer.toString(itemId))).thenReturn(null);
        doNothing().when(valueOperations).set(anyString(),any(Item.class));

        //when
        Item actualItem = businessService.getFilteredRealItem(itemId);

        // Assert
        verify(valueOperations,times(1)).set(anyString(),any(Item.class));
        assertThat(actualItem).isNotNull();
    }

    @Test
    public void testGetFilteredRealItemWithRedisCacheHit() {
        // Given
        int itemId = 2;
        Item expectedItem = new Item(itemId, "apple", (float) 19, 1000);


        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(Integer.toString(itemId))).thenReturn(expectedItem);
        doNothing().when(valueOperations).set(anyString(),any(Item.class));

        //when
        Item actualItem = businessService.getFilteredRealItem(itemId);

        // Then
        assertThat(actualItem).isNotNull();
        verify(valueOperations,times(0)).set(anyString(),any(Item.class));
        assertThat(actualItem).isEqualTo(expectedItem);
    }



}
