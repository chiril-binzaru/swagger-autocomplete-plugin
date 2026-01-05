package com.binzaru.swaggerplugin.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.LinkedHashSet;

public class SwaggerParserTest {

    private SwaggerParser swaggerParser = new SwaggerParser();
    private String validSwaggerPath;
    private final String invalidSwaggerPath = "C:" + File.separator + "non-existent-folder" + File.separator + "non-existent-swagger.json";



    @BeforeEach
    public void setUp() {
        swaggerParser = new SwaggerParser();
        validSwaggerPath = new File("src/test/resources/swagger/test-swagger.json").getAbsolutePath();
    }



    @Test
    public void testLoadSwagger_ExistentSwagger_ShouldLoadSwaggerSuccessfully() {
        try {
            swaggerParser.loadSwagger(validSwaggerPath);
            assertTrue(swaggerParser.isLoaded());
        } catch (FileNotFoundException e) {
            fail("loadSwagger() should not throw FileNotFoundException for a valid Swagger path");
        }
    }

    @Test
    public void testLoadSwagger_NonExistentSwagger_ShouldThrowFileNotFoundException() {
        assertThrows(FileNotFoundException.class, () -> swaggerParser.loadSwagger(invalidSwaggerPath));
    }


    @Test
    public void testGetModel_ExistentModelName_ShouldReturnSwaggerModel() {
        try {
            swaggerParser.loadSwagger(validSwaggerPath);

            SwaggerModel model = swaggerParser.getModel("CustomerProfile");

            assertNotNull(model);
            assertEquals("CustomerProfile", model.getName());
            assertEquals(createExpectedCustomerProfileProperties(), model.getProperties());
        } catch (FileNotFoundException e) {
            fail("loadSwagger() should not throw FileNotFoundException for a valid Swagger path");
        }
    }

    @Test
    public void testGetModel_NonExistentModelName_ShouldReturnNull() {
        try {
            swaggerParser.loadSwagger(validSwaggerPath);
            SwaggerModel model = swaggerParser.getModel("NonExistentModel");

            assertNull(model);
        } catch (FileNotFoundException e) {
            fail("loadSwagger() should not throw FileNotFoundException for a valid Swagger path");
        }
    }

    @Test
    public void testGetModel_WrongCaseModelName_ShouldReturnNull() {
        try {
            swaggerParser.loadSwagger(validSwaggerPath);
            SwaggerModel model = swaggerParser.getModel("customerprofile");

            assertNull(model);
        } catch (FileNotFoundException e) {
            fail("loadSwagger() should not throw FileNotFoundException for a valid Swagger path");
        }
    }


    @Test
    public void testResolveRef_ValidRef_ShouldReturnSwaggerModel() {
        try {
            swaggerParser.loadSwagger(validSwaggerPath);

            SwaggerModel model = swaggerParser.resolveRef("#/components/schemas/LoyaltyMembership");

            assertNotNull(model);
            assertEquals("LoyaltyMembership", model.getName());
            assertEquals(createExpectedLoyaltyMembershipProperties(), model.getProperties());
        } catch (FileNotFoundException e) {
            fail("loadSwagger() should not throw FileNotFoundException for a valid Swagger path");
        }
    }


    @Test
    public void testGetAllModelNames_WhenSwaggerSuccessfullyLoaded_ShouldReturnAllModelNames() {
        try {
            swaggerParser.loadSwagger(validSwaggerPath);
            assertEquals(createExpectedModelNames(), swaggerParser.getAllModelNames());
        } catch (FileNotFoundException e) {
            fail("loadSwagger() should not throw FileNotFoundException for a valid Swagger path");
        }
    }

    @Test
    public void testGetAllModelNames_WhenSwaggerIsNotLoaded_ShouldReturnEmptySetOfStrings() {
        assertTrue(swaggerParser.getAllModelNames().isEmpty());
    }

    @Test
    public void testUnixDate() {
        String dateOne = new java.util.Date(1767609028645L).toString();
        String dateTwo = new java.util.Date(1766569074194L).toString();

        System.out.println(dateOne + " " + dateTwo);
    }



    private Map<String, SwaggerModel.PropertyInfo> createExpectedCustomerProfileProperties() {
        Map<String, SwaggerModel.PropertyInfo> properties = new LinkedHashMap<>();
        properties.put("customerId", new SwaggerModel.PropertyInfo("integer", "Unique customer identifier", null));
        properties.put("firstName", new SwaggerModel.PropertyInfo("string", "Customer first name", null));
        properties.put("lastName", new SwaggerModel.PropertyInfo("string", "Customer last name", null));
        properties.put("emailAddress", new SwaggerModel.PropertyInfo("string", "Primary email address", null));
        properties.put("phoneNumber", new SwaggerModel.PropertyInfo("string", "Contact phone number", null));
        properties.put("dateOfBirth", new SwaggerModel.PropertyInfo("string", null, null));
        properties.put("registrationDate", new SwaggerModel.PropertyInfo("string", null, null));
        properties.put("accountStatus", new SwaggerModel.PropertyInfo("string", null, null));
        properties.put("loyaltyProgramMembership", new SwaggerModel.PropertyInfo(null, null, "#/components/schemas/LoyaltyMembership"));
        properties.put("primaryShippingAddress", new SwaggerModel.PropertyInfo(null, null, "#/components/schemas/ShippingAddress"));
        properties.put("billingInformation", new SwaggerModel.PropertyInfo(null, null, "#/components/schemas/BillingDetails"));
        properties.put("orderHistory", new SwaggerModel.PropertyInfo("array", null, null));
        properties.put("preferredPaymentMethods", new SwaggerModel.PropertyInfo("array", null, null));
        return properties;
    }

    private Map<String, SwaggerModel.PropertyInfo> createExpectedLoyaltyMembershipProperties() {
        Map<String, SwaggerModel.PropertyInfo> properties = new LinkedHashMap<>();
        properties.put("membershipId", new SwaggerModel.PropertyInfo("integer", null, null));
        properties.put("membershipTierLevel", new SwaggerModel.PropertyInfo("string", "bronze, silver, gold, platinum", null));
        properties.put("totalPointsAccumulated", new SwaggerModel.PropertyInfo("integer", null, null));
        properties.put("availablePointsBalance", new SwaggerModel.PropertyInfo("integer", null, null));
        properties.put("memberSinceDate", new SwaggerModel.PropertyInfo("string", null, null));
        properties.put("nextTierRequiredPoints", new SwaggerModel.PropertyInfo("integer", null, null));
        properties.put("specialBenefitsAndRewards", new SwaggerModel.PropertyInfo(null, null, "#/components/schemas/MemberBenefits"));
        return properties;
    }

    private Set<String> createExpectedModelNames() {
        Set<String> modelNames = new LinkedHashSet<>();
        modelNames.add("CustomerProfile");
        modelNames.add("LoyaltyMembership");
        modelNames.add("MemberBenefits");
        modelNames.add("ShippingAddress");
        modelNames.add("GeoLocation");
        modelNames.add("BillingDetails");
        modelNames.add("OrderSummary");
        modelNames.add("ShipmentTracking");
        modelNames.add("PaymentMethod");
        return modelNames;
    }

}
