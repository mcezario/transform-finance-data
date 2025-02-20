package com.al.datahandling.finance.gateway.aws.s3;

import com.al.datahandling.finance.domain.FinanceData;
import com.al.datahandling.finance.gateway.FinanceObjectApi;
import com.al.datahandling.finance.gateway.FinanceObjectException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Uri;
import software.amazon.awssdk.services.s3.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

@Service( "FinanceFetcherApiGateway" )
public class S3FinanceObjectImpl implements FinanceObjectApi {

    private static final String CSV_FILE_SEPARATOR = ",";

    private final Logger logger = LoggerFactory.getLogger(S3FinanceObjectImpl.class);

    @Autowired
    public S3Client s3Client;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<FinanceData> get(String bucket) {
        List<FinanceData> financeData = new ArrayList<>();

        try {
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setDelimiter(CSV_FILE_SEPARATOR)
                    .build();

            for (String objectKey : listObjectKeys(bucket)) {
                ResponseBytes<GetObjectResponse> object = getObject(bucket, objectKey);
                try (final ZipInputStream in = new ZipInputStream(object.asInputStream());
                     final BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                    while (in.getNextEntry() != null) {
                        final List<CSVRecord> records = csvFormat.parse(reader).getRecords();
                        final Map<Integer, String> dateHeader = extractDateHeader(records, 2);
                        for (int i = 0; i < records.size(); i++) {
                            financeData.add(parseCsvRow(records.get(i), dateHeader));
                        }
                    }
                } catch (IOException e) {
                    final String errorMessage = String.format("Error to manipulate file from AWS S3 bucket %s/%s", bucket, objectKey);
                    logger.error(errorMessage, e);
                    throw new FinanceObjectException(errorMessage, e);
                }
            }
        } catch (SdkException e) {
            final String errorMessage = String.format("Error to get object from AWS S3 bucket %s", bucket);
            logger.error(errorMessage, e);
            throw new FinanceObjectException(errorMessage, e);
        }
        return financeData;
    }

    private FinanceData parseCsvRow(CSVRecord record, Map<Integer, String> dateHeader) {
        Map<String, String> datesAndValues = new HashMap<>();
        for (int dc = 2; dc < record.size(); dc++) {
            datesAndValues.put(dateHeader.get(dc), record.get(dc));
        }
        return new FinanceData(
                record.get(0), // id
                record.get(1), // scale
                datesAndValues // Key/Pair of date and value
        );
    }

    private Map<Integer, String> extractDateHeader(List<CSVRecord> records, int columnIndexStart) {
        Map<Integer, String> dateHeader = new HashMap<>();
        for (int h = columnIndexStart; h < records.get(0).size(); h++) {
            dateHeader.put(h, records.get(0).get(h));
        }
        return dateHeader;
    }

    private List<String> listObjectKeys(String bucket) {
        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .build();
        ListObjectsV2Response listObjectsV2Response = s3Client.listObjectsV2(listObjectsV2Request);
        return listObjectsV2Response.contents().stream().map(S3Object::key).collect(Collectors.toList());
    }

    private ResponseBytes<GetObjectResponse> getObject(String bucket, String key) {
        S3Uri s3Uri = s3Client.utilities().parseUri(URI.create(String.format("s3://%s/%s", bucket, key)));
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(s3Uri.bucket().get())
                .key(s3Uri.key().get())
                .build();
        return s3Client.getObject(objectRequest, ResponseTransformer.toBytes());
    }

}
