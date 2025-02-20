package com.al.datahandling.finance.config;

import com.al.datahandling.finance.domain.FinanceData;
import com.al.datahandling.finance.gateway.FinanceObjectException;
import com.al.datahandling.finance.gateway.aws.s3.S3FinanceObjectImpl;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Uri;
import software.amazon.awssdk.services.s3.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Test {

    public static void main(String[] args) {
        S3Client s3Client = S3Client.builder()
                .region(Region.of("us-west-2"))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
        S3FinanceObjectImpl s3FinanceObject = new S3FinanceObjectImpl();
        s3FinanceObject.s3Client = s3Client;
        List<FinanceData> financeData = s3FinanceObject.get("as-findata-tech-challenge");
        System.out.println(financeData);

    }
    public static void main2(String[] args) {
        S3Client s3Client = S3Client.builder()
                .region(Region.of("us-west-2"))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
        S3Uri s3Uri = s3Client.utilities().parseUri(URI.create("s3://findata-tech-challenge"));

        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket("as-findata-tech-challenge")
                .build();
        ListObjectsV2Response listObjectsV2Response = s3Client.listObjectsV2(listObjectsV2Request);
        List<S3Object> contents = listObjectsV2Response.contents();

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setDelimiter(",")
                .build();

//        Each row represents a specific ID’s value over time.
//            Each date cell in the header row represents a date range, where the value indicates the start date of a 3-month period.
//        Each row's "scale" value indicates the factor that was applied to get the scaled value that is stored in each subsequent cell in the row;
//        for example, a scale of 10 and a stored value of 10 indicates an actual value of 100.

        for (S3Object content : listObjectsV2Response.contents()) {
            System.out.println(content.key());

            String location = String.format("s3://as-findata-tech-challenge/%s", content.key());
            System.out.println(location);
            ResponseBytes<GetObjectResponse> object = getObject(s3Client, location);
            try (final ZipInputStream in = new ZipInputStream(object.asInputStream());
                 final BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                ZipEntry zipEntry;
                while ((zipEntry = in.getNextEntry()) != null) {
                    System.out.println(zipEntry.getName());
                    System.out.println("-----");
                    List<CSVRecord> records = csvFormat.parse(reader).getRecords();
                    Map<Integer, String> dateHeader = new HashMap<>();
                    for (int i = 0; i < records.size(); i++) {
                        if (i == 0) { // configure dynamic header values and assign their values to the current position index represented in the csv file
                            for (int h = 2; h < records.get(i).size(); h++) {
                                dateHeader.put(h, records.get(i).get(h));
                            }
                            continue;
                        }
                        Map<String, String> datesAndValues = new HashMap<>();
                        for (int dc = 2; dc < records.get(i).size(); dc++) {
                            datesAndValues.put(dateHeader.get(dc), records.get(i).get(dc));
                        }
                        FinanceData financeData = new FinanceData(
                                records.get(i).get(0), // id
                                records.get(i).get(1), // scale
                                datesAndValues // Key/Pair of date and value
                        );
                        System.out.println(financeData);
                    }
                    System.out.println("-----");

                }
            } catch (IOException e) {
                final String errorMessage = String.format("Error to manipulate file from AWS S3 bucket %s", location);
                throw new FinanceObjectException(errorMessage, e);
            }
        }

//
//        GetObjectRequest objectRequest = GetObjectRequest.builder()
//                .bucket(s3Uri.bucket().get())
//                .key(s3Uri.key().get())
//                .build();

    }

    private static ResponseBytes<GetObjectResponse> getObject(S3Client s3Client, String location) {
        S3Uri s3Uri = s3Client.utilities().parseUri(URI.create(location));
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(s3Uri.bucket().get())
                .key(s3Uri.key().get())
                .build();
        return s3Client.getObject(objectRequest, ResponseTransformer.toBytes());
    }
}
