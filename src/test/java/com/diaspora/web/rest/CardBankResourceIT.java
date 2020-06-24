package com.diaspora.web.rest;

import com.diaspora.MsSecureCardBankApp;
import com.diaspora.config.SecurityBeanOverrideConfiguration;
import com.diaspora.domain.CardBank;
import com.diaspora.repository.CardBankRepository;
import com.diaspora.web.rest.errors.ExceptionTranslator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
import java.util.List;

import static com.diaspora.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link CardBankResource} REST controller.
 */
@SpringBootTest(classes = {SecurityBeanOverrideConfiguration.class, MsSecureCardBankApp.class})
public class CardBankResourceIT {

    private static final String DEFAULT_CODE_ENCODED = "AAAAAAAAAA";
    private static final String UPDATED_CODE_ENCODED = "BBBBBBBBBB";

    @Autowired
    private CardBankRepository cardBankRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private Validator validator;

    private MockMvc restCardBankMockMvc;

    private CardBank cardBank;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final CardBankResource cardBankResource = new CardBankResource(cardBankRepository);
        this.restCardBankMockMvc = MockMvcBuilders.standaloneSetup(cardBankResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CardBank createEntity(EntityManager em) {
        CardBank cardBank = new CardBank()
            .codeEncoded(DEFAULT_CODE_ENCODED);
        return cardBank;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CardBank createUpdatedEntity(EntityManager em) {
        CardBank cardBank = new CardBank()
            .codeEncoded(UPDATED_CODE_ENCODED);
        return cardBank;
    }

    @BeforeEach
    public void initTest() {
        cardBank = createEntity(em);
    }

    @Test
    @Transactional
    public void createCardBank() throws Exception {
        int databaseSizeBeforeCreate = cardBankRepository.findAll().size();

        // Create the CardBank
        restCardBankMockMvc.perform(post("/api/card-banks")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(cardBank)))
            .andExpect(status().isCreated());

        // Validate the CardBank in the database
        List<CardBank> cardBankList = cardBankRepository.findAll();
        assertThat(cardBankList).hasSize(databaseSizeBeforeCreate + 1);
        CardBank testCardBank = cardBankList.get(cardBankList.size() - 1);
        assertThat(testCardBank.getCodeEncoded()).isEqualTo(DEFAULT_CODE_ENCODED);
    }

    @Test
    @Transactional
    public void createCardBankWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = cardBankRepository.findAll().size();

        // Create the CardBank with an existing ID
        cardBank.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restCardBankMockMvc.perform(post("/api/card-banks")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(cardBank)))
            .andExpect(status().isBadRequest());

        // Validate the CardBank in the database
        List<CardBank> cardBankList = cardBankRepository.findAll();
        assertThat(cardBankList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllCardBanks() throws Exception {
        // Initialize the database
        cardBankRepository.saveAndFlush(cardBank);

        // Get all the cardBankList
        restCardBankMockMvc.perform(get("/api/card-banks?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(cardBank.getId().intValue())))
            .andExpect(jsonPath("$.[*].codeEncoded").value(hasItem(DEFAULT_CODE_ENCODED)));
    }
    
    @Test
    @Transactional
    public void getCardBank() throws Exception {
        // Initialize the database
        cardBankRepository.saveAndFlush(cardBank);

        // Get the cardBank
        restCardBankMockMvc.perform(get("/api/card-banks/{id}", cardBank.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(cardBank.getId().intValue()))
            .andExpect(jsonPath("$.codeEncoded").value(DEFAULT_CODE_ENCODED));
    }

    @Test
    @Transactional
    public void getNonExistingCardBank() throws Exception {
        // Get the cardBank
        restCardBankMockMvc.perform(get("/api/card-banks/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCardBank() throws Exception {
        // Initialize the database
        cardBankRepository.saveAndFlush(cardBank);

        int databaseSizeBeforeUpdate = cardBankRepository.findAll().size();

        // Update the cardBank
        CardBank updatedCardBank = cardBankRepository.findById(cardBank.getId()).get();
        // Disconnect from session so that the updates on updatedCardBank are not directly saved in db
        em.detach(updatedCardBank);
        updatedCardBank
            .codeEncoded(UPDATED_CODE_ENCODED);

        restCardBankMockMvc.perform(put("/api/card-banks")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedCardBank)))
            .andExpect(status().isOk());

        // Validate the CardBank in the database
        List<CardBank> cardBankList = cardBankRepository.findAll();
        assertThat(cardBankList).hasSize(databaseSizeBeforeUpdate);
        CardBank testCardBank = cardBankList.get(cardBankList.size() - 1);
        assertThat(testCardBank.getCodeEncoded()).isEqualTo(UPDATED_CODE_ENCODED);
    }

    @Test
    @Transactional
    public void updateNonExistingCardBank() throws Exception {
        int databaseSizeBeforeUpdate = cardBankRepository.findAll().size();

        // Create the CardBank

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCardBankMockMvc.perform(put("/api/card-banks")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(cardBank)))
            .andExpect(status().isBadRequest());

        // Validate the CardBank in the database
        List<CardBank> cardBankList = cardBankRepository.findAll();
        assertThat(cardBankList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteCardBank() throws Exception {
        // Initialize the database
        cardBankRepository.saveAndFlush(cardBank);

        int databaseSizeBeforeDelete = cardBankRepository.findAll().size();

        // Delete the cardBank
        restCardBankMockMvc.perform(delete("/api/card-banks/{id}", cardBank.getId())
            .accept(TestUtil.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<CardBank> cardBankList = cardBankRepository.findAll();
        assertThat(cardBankList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
