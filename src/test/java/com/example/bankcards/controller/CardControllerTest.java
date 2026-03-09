package com.example.bankcards.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.bankcards.service.CardService;

import io.restassured.mapper.ObjectMapper;

@WebMvcTest(CardController.class)
class CardControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CardService cardService;

    // // private BankCardDto testCardDto;
    // private final String BASE_URL = "/api/customers/card/";
    // private final String CARD_NUM = "1234567890123456";
    // private final String CARD_NUM2 = "1234567890123457";


    // @BeforeEach
    // void setUp() {
    //     testCardDto = BankCardDto.builder()
    //             .cardNumber("1234567890123456")
    //             .customerId(1L)
    //             .build();
    // }

    // @Test
    // public void testGetAllCards() throws Exception {

    //     Long customerId = 1L;
    //     Pageable pageable = PageRequest.of(0, 10);

    //     CustomerDto customer = new CustomerDto(customerId, "John Weak", "babaYaga@ya.ru", Role.ROLE_ADMIN);

    //     BankCardDto card1 = BankCardDto.builder()
    //         .cardNumber(CARD_NUM)
    //         .customerId(customerId)
    //         .build();

    //     BankCardDto card2 = BankCardDto.builder()
    //         .cardNumber(CARD_NUM2)
    //         .customerId(customerId)
    //         .build();
            
    //     List<BankCardDto> cardList = Arrays.asList(card1, card2);

        // when(cardService.findAll()).thenReturn(Arrays.asList(
        //         new Card(1L, "Visa", "1234"),
        //         new Card(2L, "Mastercard", "5678")));

        // mockMvc.perform(get("/api/cards"))
        //         .andExpect(status().isOk());
    // }
}