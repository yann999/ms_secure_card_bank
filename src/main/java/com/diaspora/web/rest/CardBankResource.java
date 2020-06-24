package com.diaspora.web.rest;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.diaspora.domain.CardBank;
import com.diaspora.repository.CardBankRepository;
import com.diaspora.web.rest.errors.BadRequestAlertException;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.bind.DatatypeConverter;

/**
 * REST controller for managing {@link com.diaspora.domain.CardBank}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class CardBankResource {

	private PrivateKey privateKey;
	private byte[] resultfinal;
	private Long lastId;

    private final Logger log = LoggerFactory.getLogger(CardBankResource.class);

    private static final String ENTITY_NAME = "msSecureCardBankCardBank";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CardBankRepository cardBankRepository;

    public CardBankResource(CardBankRepository cardBankRepository) {
        this.cardBankRepository = cardBankRepository;
    }

    /**
     * {@code POST  /card-banks} : Create a new cardBank.
     *
     * @param cardBank the cardBank to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new cardBank, or with status {@code 400 (Bad Request)} if the cardBank has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
//    @PostMapping("/card-banks")
//    public ResponseEntity<CardBank> createCardBank(@RequestBody CardBank cardBank) throws URISyntaxException {
//        log.debug("REST request to save CardBank : {}", cardBank);
//        if (cardBank.getId() != null) {
//            throw new BadRequestAlertException("A new cardBank cannot already have an ID", ENTITY_NAME, "idexists");
//        }
//        CardBank result = cardBankRepository.save(cardBank);
//        return ResponseEntity.created(new URI("/api/card-banks/" + result.getId()))
//            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
//            .body(result);
//    }

    /**
     * {@code PUT  /card-banks} : Updates an existing cardBank.
     *
     * @param cardBank the cardBank to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated cardBank,
     * or with status {@code 400 (Bad Request)} if the cardBank is not valid,
     * or with status {@code 500 (Internal Server Error)} if the cardBank couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/card-banks")
    public ResponseEntity<CardBank> updateCardBank(@RequestBody CardBank cardBank) throws URISyntaxException {
        log.debug("REST request to update CardBank : {}", cardBank);
        if (cardBank.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        CardBank result = cardBankRepository.save(cardBank);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, cardBank.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /card-banks} : get all the cardBanks.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of cardBanks in body.
     */
    @GetMapping("/card-banks")
    public List<CardBank> getAllCardBanks() {
        log.debug("REST request to get all CardBanks");
        return cardBankRepository.findAll();
    }

    /**
     * {@code GET  /card-banks/:id} : get the "id" cardBank.
     *
     * @param id the id of the cardBank to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the cardBank, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/card-banks/{id}")
    public ResponseEntity<CardBank> getCardBank(@PathVariable Long id) {
        log.debug("REST request to get CardBank : {}", id);
        Optional<CardBank> cardBank = cardBankRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(cardBank);
    }

    /**
     * {@code DELETE  /card-banks/:id} : delete the "id" cardBank.
     *
     * @param id the id of the cardBank to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/card-banks/{id}")
    public ResponseEntity<Void> deleteCardBank(@PathVariable Long id) {
        log.debug("REST request to delete CardBank : {}", id);
        cardBankRepository.deleteById(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build();
    }
	
    
	@PostMapping("/card-banks")
	public ResponseEntity<JSONObject> encodeBank(@RequestParam String messageToEncrypt) throws NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, SignatureException {
		//on recupere une instance de fournisseur d algorithme de cryptage et decryptage
		Cipher cipherRsa = Cipher.getInstance("RSA");
		CardBank cardBank = new CardBank();
		Map<String, Object> token = new HashMap<String, Object>();
		JSONObject json = new JSONObject();
		
		//on genere une clé publique pour le cryptage
		KeyPair pair = KeyPairGenerator.getInstance("RSA", "SunRsaSign").generateKeyPair();
		privateKey = pair.getPrivate();
		
		//on initialise et on crypte la donnée
		cipherRsa.init(Cipher.ENCRYPT_MODE, pair.getPublic());
		resultfinal = cipherRsa.doFinal(messageToEncrypt.getBytes());
		
		//save cardBank datas in db and build token to send to user
		cardBank.setCodeEncoded(DatatypeConverter.printHexBinary(resultfinal).toString());
		json.put("algo", privateKey.getAlgorithm());
		json.put("format", privateKey.getFormat());
		json.put("encoded", privateKey.getEncoded());
		token.put("lastId", cardBankRepository.save(cardBank).getId());
		token.put("username", "Yann");
		token.put("private key", json);
		
		//print result
		log.info("------------------------ affichage console des resultats d'encryptage ------------------------");
		log.info("Message Origin    -> " + messageToEncrypt);
		log.info("Message Encrypted -> " + cardBank.getCodeEncoded());
		
		String token2 = JWT.create().withSubject(messageToEncrypt)
				   .withExpiresAt(new Date())
				   .withIssuer("this.issuerID")
				   .withIssuedAt(new Date())
				   .withNotBefore(new Date())
				   .withClaim("userFingerprint", true)
				   .withHeader(null)
				   .sign(Algorithm.HMAC256(pair.getPublic().getEncoded()));
		
		token.put("token", token2);
		
		return ResponseEntity.ok().header("token", token2).body(json);
	}
	
	@GetMapping("/decode")
	public Map<String, Object> decode() throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		//on recupere une instance de fournisseur d algorithme de cryptage et decryptage
		Cipher rsa2 = Cipher.getInstance("RSA");
		JSONObject backObject = new JSONObject();
		Map<String, Object> backMap = new HashMap<>();
		
		//on initialise et on decrypte la donnée
		rsa2.init(Cipher.DECRYPT_MODE, privateKey);
//		System.out.println("**************************** le dernier id est : " + cardBankRespository.findById(lastId).get().getCodeEncoded());
//		byte[] msg = cardBankRespository.findById(lastId).get().getCodeEncoded().getBytes();
//		String retu = cardBankRespository.findById(lastId).get().getCodeEncoded();
		
		//print result
		log.info("--------------------------- affichage console des resultats de decryptage ---------------------------");
		log.info("Message Encrypted -> " + resultfinal);
		log.info("Message Encrypted -> " + DatatypeConverter.printHexBinary(resultfinal).toString());
		log.info("Message Decrypted -> " + new String(rsa2.doFinal(resultfinal)));
		
		
		backObject.put("Message Encrypted", resultfinal);
		backObject.put("Message Encrypted", DatatypeConverter.printHexBinary(resultfinal).toString());
		backObject.put("Message Decrypted", new String(rsa2.doFinal(resultfinal)));
		backMap.put("Response Status", "Transformation effectué avec succès");
		backMap.put("Response Body", backObject);
		
		return backMap;
	}
}
