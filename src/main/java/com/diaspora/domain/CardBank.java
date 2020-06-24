package com.diaspora.domain;


import javax.persistence.*;

import java.io.Serializable;

/**
 * A CardBank.
 */
@Entity
@Table(name = "card_bank")
public class CardBank implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_encoded")
    private String codeEncoded;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodeEncoded() {
        return codeEncoded;
    }

    public CardBank codeEncoded(String codeEncoded) {
        this.codeEncoded = codeEncoded;
        return this;
    }

    public void setCodeEncoded(String codeEncoded) {
        this.codeEncoded = codeEncoded;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CardBank)) {
            return false;
        }
        return id != null && id.equals(((CardBank) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "CardBank{" +
            "id=" + getId() +
            ", codeEncoded='" + getCodeEncoded() + "'" +
            "}";
    }
}
