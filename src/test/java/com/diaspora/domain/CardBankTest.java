package com.diaspora.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import com.diaspora.web.rest.TestUtil;

public class CardBankTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CardBank.class);
        CardBank cardBank1 = new CardBank();
        cardBank1.setId(1L);
        CardBank cardBank2 = new CardBank();
        cardBank2.setId(cardBank1.getId());
        assertThat(cardBank1).isEqualTo(cardBank2);
        cardBank2.setId(2L);
        assertThat(cardBank1).isNotEqualTo(cardBank2);
        cardBank1.setId(null);
        assertThat(cardBank1).isNotEqualTo(cardBank2);
    }
}
