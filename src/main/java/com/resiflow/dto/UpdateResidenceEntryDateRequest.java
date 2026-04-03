package com.resiflow.dto;

import java.time.LocalDate;

public class UpdateResidenceEntryDateRequest {

    private LocalDate dateEntreeResidence;

    public LocalDate getDateEntreeResidence() {
        return dateEntreeResidence;
    }

    public void setDateEntreeResidence(final LocalDate dateEntreeResidence) {
        this.dateEntreeResidence = dateEntreeResidence;
    }
}
