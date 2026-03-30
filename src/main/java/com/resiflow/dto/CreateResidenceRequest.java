package com.resiflow.dto;

import java.math.BigDecimal;

public class CreateResidenceRequest {

    private String name;
    private String address;
    private String code;
    private Boolean enabled;
    private BigDecimal montantMensuel;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(final String address) {
        this.address = address;
    }

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(final Boolean enabled) {
        this.enabled = enabled;
    }

    public BigDecimal getMontantMensuel() {
        return montantMensuel;
    }

    public void setMontantMensuel(final BigDecimal montantMensuel) {
        this.montantMensuel = montantMensuel;
    }
}
