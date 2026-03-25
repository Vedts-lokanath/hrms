package com.vts.hrms.dto;


import lombok.Data;

import java.io.Serializable;

@Data
public class AddressDTO implements Serializable {

    private Long addressId;
    private Long empId;
    private String currentAddress;
    private String currentAddressState;
    private String currentAddressCity;
    private String currentAddressPinCode;
    private String permanentAddress;
    private String permanentAddressState;
    private String permanentAddressCity;
    private String permanentAddressPinCode;
    private String homeTownAddress;
    private String homeTownAddressState;
    private String homeTownAddressCity;
    private String homeTownAddressPinCode;

    public AddressDTO(Long addressId, Long empId, String currentAddress, String currentAddressState, String currentAddressCity, String currentAddressPinCode, String permanentAddress, String permanentAddressState, String permanentAddressCity, String permanentAddressPinCode, String homeTownAddress, String homeTownAddressState, String homeTownAddressCity, String homeTownAddressPinCode) {
        this.addressId = addressId;
        this.empId = empId;
        this.currentAddress = currentAddress;
        this.currentAddressState = currentAddressState;
        this.currentAddressCity = currentAddressCity;
        this.currentAddressPinCode = currentAddressPinCode;
        this.permanentAddress = permanentAddress;
        this.permanentAddressState = permanentAddressState;
        this.permanentAddressCity = permanentAddressCity;
        this.permanentAddressPinCode = permanentAddressPinCode;
        this.homeTownAddress = homeTownAddress;
        this.homeTownAddressState = homeTownAddressState;
        this.homeTownAddressCity = homeTownAddressCity;
        this.homeTownAddressPinCode = homeTownAddressPinCode;
    }
}
