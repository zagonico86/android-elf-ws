package com.zagonico.vies;

import android.util.ArrayMap;

import com.zagonico.elfws.ElfWsClient;
import com.zagonico.elfws.ElfWsResponse;

import java.util.Map;

public class VatChecker {
    public static VatInfo elfCheckVat(String countryCode, String vatNumber) {
        VatInfo info = new VatInfo();

        try {
            ElfWsClient client = new ElfWsClient();

            String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                    "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"" +
                    " xmlns:ns1=\"urn:ec.europa.eu:taxud:vies:services:checkVat:types\">" +
                    "<SOAP-ENV:Body>" +
                    "<ns1:checkVat>" +
                    "<ns1:countryCode>YYYY</ns1:countryCode>" +
                    "<ns1:vatNumber>XXXX</ns1:vatNumber>" +
                    "</ns1:checkVat>" +
                    "</SOAP-ENV:Body>" +
                    "</SOAP-ENV:Envelope>";

            xml = xml.replace("YYYY", countryCode);
            xml = xml.replace("XXXX", vatNumber);

            client.addXml(xml);

            Map<String, String> map = new ArrayMap<>();
            map.put("SOAPAction", "\"\"");
            map.put("Connection", "Keep-Alive");
            map.put("User-Agent", "Elf-SOAP/1.0");
            map.put("Content-Length", "" + (xml.length() + 2));
            client.setAddictionalHeaders(map);

            ElfWsResponse response = client.httpRequest("https://ec.europa.eu/taxation_customs/vies/services/checkVatService");

            if (response == null || response.isError()) {
                return null;
            }

            String xmlResp = response.getContent();
            if (xmlResp!=null && xmlResp.indexOf("<checkVatResponse") > 0) {
                String[] tags = new String[] { "countryCode", "vatNumber", "requestDate", "valid", "name", "address" };

                for (String tag : tags) {
                    int start = xmlResp.indexOf("<"+tag+">") + tag.length()+2;
                    int end = xmlResp.indexOf("</"+tag+">");
                    String value = xmlResp.substring(start, end);

                    switch (tag) {
                        case "countryCode":
                            info.countryCode = value;
                            break;
                        case "vatNumber":
                            info.vatNumber = value;
                            break;
                        case "requestDate":
                            info.date = value;
                            break;
                        case "valid":
                            info.valid = "true".equals(value);
                            break;
                        case "name":
                            info.name = value;
                            break;
                        case "address":
                            info.address = value;
                            break;
                    }
                }

            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return info;
    }

}
