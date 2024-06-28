package com.nt118.proma;

import com.google.auth.oauth2.GoogleCredentials;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class AccessToken {
    private static final String firebaseMessagingScope = "https://www.googleapis.com/auth/firebase.messaging";

    public String getAccessToken() {
        try {
            String json = "{\n" +
                    "  \"type\": \"service_account\",\n" +
                    "  \"project_id\": \"proma-4ca44\",\n" +
                    "  \"private_key_id\": \"64c84515840274b5b1345cc64a19ce784fe3ec6f\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDKOj+BT5GhAM7K\\nOsF1NlDsEQu50P5jhPjT2UMA+58kMjEx1u0nOxQD2TBgHQiyZxZj2G/S3/Fq+GZY\\nU3mVxVwSGKh2LNNLQJwsITDrM5QSjfx7vgq390vCBpXQt0ev3iMULCvgz67VqNxJ\\ntxrWX6NUeX01wkdQ4pBcrBmatVTtQrksiU4qvx1mwK82McWIhRAe14Xjku7OdW9D\\n7FhkvgkD1IGsEhNtrj2p42h9vQJdZ//tCCj807cXllh1uyl/sVlkWfPIAEWTuFYg\\nedsl6VdOLx/7BszbYdqAJByM8yHkaeF3H2DcCvQ5cYkrZ6lKjQX4BL7drJ9AbUFo\\nnwT1kyEZAgMBAAECggEABgC9MKATpL/nONz/9wJcsVtw1/GYvG/c37Ac0nNCHHlW\\ngCr2CjU/kiCQDVQbwAlL5iCCx+yG28WjhzXJzBor8jY81e6H7SD8Pj2M35Svpnyu\\n0yYugyjsB5vn4K2UiyBLiw15R73u6/F+M5/5NbDaTSdbnmmgccKamvvZgEcZXFR1\\n61A8KbPl1DEdIXBpY3+6d9HRXonxJlbvFV804EVcqcZaKVi9FaXamRJ1efhW7+Xu\\nuvHb3M9kbyUJezHAWnV6WpYG1n0We9ni1i7jFTHnCX307XZrYK/W30kF25pfeDig\\nV1pncNbZMWVKi+wSL1cppgBuqaPOTzkBncbNpIsJGwKBgQDrHTAhRWTIEbhx0H9l\\nFlYAnBvOdnh/AdwT3wqPgDdKPyu3LeNIN7cRWYxvCsRXQg40csM8duJ0suBsCJkn\\niNTm91ZHFoRyifb8WTgAgMXqk6LfI9vdDCfuVDK80XlBU8CcDVA4HVzXb72pB3JB\\nbJBqZvVxJBWjx/4gQgkC9mZuBwKBgQDcMS0/j6Qhi7Wbp6fuMOXvhvcRxseWrV0n\\n6l34A2mA7F6FtZMip6I0YyZsu+9NCZ4RSDNuyRvxnpnP01QHXHdKghPim6JmQU4D\\nhkWdBPgsUgURAnmGt+I+k5Q7IgINUPq5+JeWkF14hwYE7eM3l2R1R9bmzz+RNIUJ\\n/VlxESov3wKBgAtckrnY36DnclFVsQJSjP9ki9dzVClXDqqLKbh7irB00wj8F7Ki\\ntp8JA8dN7uUElSklrpeaayEWj8/IrpYBf8BVjX7L8MSUNDJHoXQsxOOsFL/RvMs2\\nzisvOHB1cvWcNX+VCR5dFOJ4TOMBMB1HQMdTiGLaSo1qmbY3WrYg8Bw7AoGAKDof\\nvx356VsBybceeeibCWCJ4MiFDCUOAM3Tbw0IiFImjpz2jX08p/95NKI8/MEGDLg5\\nFUNOhJzqEsrT7ihZbtZG1b6DM9U4cHJmY2JWCJrPbCUcmJ0dizsg+JCk+VPTCoi9\\nAPfM68kXM21WdoCFDNwcCbzRKzECISoU3hA5udcCgYBK6I24UrfS/oYRSSKk29Ua\\nklXCVtlhi0b+oPUVFM74mmQJlvwC3NfwN4KtJDmR159I47FcQRurjKKDVtv92/kV\\n35HTd550d4RgMbNmQAEsj1Hly+o9u1jlpNbAXDMnNfc1PS86f5Q65aKpTFxVT3DS\\nxGULkH7r0wbDuohkIY10ag==\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"firebase-adminsdk-m5v71@proma-4ca44.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"105095253930634284681\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-m5v71%40proma-4ca44.iam.gserviceaccount.com\",\n" +
                    "  \"universe_domain\": \"googleapis.com\"\n" +
                    "}\n";
            InputStream serviceAccount = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
            GoogleCredentials googleCredential = GoogleCredentials.fromStream(serviceAccount)
                    .createScoped(Collections.singletonList(firebaseMessagingScope));
            googleCredential.refresh();
            return googleCredential.getAccessToken().getTokenValue();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
