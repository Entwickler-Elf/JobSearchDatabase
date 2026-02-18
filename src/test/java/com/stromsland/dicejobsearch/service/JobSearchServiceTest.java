package com.stromsland.dicejobsearch.service;

import com.stromsland.dicejobsearch.model.DiceJobEntity;
import com.stromsland.dicejobsearch.model.JobListing;
import com.stromsland.dicejobsearch.repository.DiceJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobSearchServiceTest {

    @Mock
    private DiceJobRepository diceJobRepository;
    @Mock
    private JobMapper jobMapper;
    @Mock
    private ChatClient.Builder chatClientBuilder;
    @Mock
    private ChatClient chatClient;
    @Mock
    private ToolCallbackProvider mcpTools;
    @Mock
    private RestTemplate restTemplate;

    private JobSearchService jobSearchService;

    @BeforeEach
    void setUp() {
        // Configure the fluent builder to return itself for every call
        lenient().when(chatClientBuilder.defaultSystem(anyString())).thenReturn(chatClientBuilder);
        lenient().when(chatClientBuilder.defaultToolCallbacks(any(org.springframework.ai.tool.ToolCallback[].class))).thenReturn(chatClientBuilder);
        lenient().when(chatClientBuilder.defaultTools(any())).thenReturn(chatClientBuilder);
        lenient().when(chatClientBuilder.build()).thenReturn(chatClient);

        // Ensure mcpTools doesn't return null if the constructor calls it
        lenient().when(mcpTools.getToolCallbacks()).thenReturn(new org.springframework.ai.tool.ToolCallback[0]);

        jobSearchService = new JobSearchService(chatClientBuilder, mcpTools, diceJobRepository, jobMapper);

        ReflectionTestUtils.setField(jobSearchService, "restTemplate", restTemplate);
    }

    @Test
    void getAllListings_ShouldReturnSortedAndMappedListings() {
        // Arrange
        DiceJobEntity entity = new DiceJobEntity();
        JobListing listing = new JobListing("1", "Title", "Sum", "Co", "Loc", "URL", "C-URL", "Sal", "Type", "Work", "Date", true, true, false,"Dice-1");

        when(diceJobRepository.findAllByOrderByPostedDateDesc()).thenReturn(List.of(entity));
        when(jobMapper.toListing(entity)).thenReturn(listing);

        // Act
        List<JobListing> results = jobSearchService.getAllListings();

        // Assert
        assertEquals(1, results.size());
        assertEquals("Title", results.getFirst().title());
        verify(diceJobRepository).findAllByOrderByPostedDateDesc();
        verify(jobMapper).toListing(entity);
    }

    @Test
    void fetchDiceId_ShouldParseIdFromHtml() {
        // Arrange
        String url = "http://dice.com/job123";
        String html = "<html><body>Some text Dice Id: <!-- --> 987654 more text</body></html>";
        when(restTemplate.getForObject(url, String.class)).thenReturn(html);

        // Act
        String result = jobSearchService.fetchDiceId(url);

        // Assert
        assertEquals("987654", result);
    }

    @Test
    void fetchDiceId_ShouldReturnErrorWhenIdNotFound() {
        // Arrange
        String url = "http://dice.com/job123";
        String html = "<html><body>No ID here</body></html>";
        when(restTemplate.getForObject(url, String.class)).thenReturn(html);

        // Act
        String result = jobSearchService.fetchDiceId(url);

        // Assert
        assertEquals("Dice Id not found on page", result);
    }

    @Test
    void fetchDiceId_ShouldReturnErrorOnException() {
        // Arrange
        String url = "http://dice.com/job123";
        when(restTemplate.getForObject(url, String.class)).thenThrow(new RuntimeException("Network error"));

        // Act
        String result = jobSearchService.fetchDiceId(url);

        // Assert
        assertTrue(result.contains("Error retrieving Dice Id"));
    }
    }