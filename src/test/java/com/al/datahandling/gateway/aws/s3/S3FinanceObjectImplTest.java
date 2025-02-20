package com.al.datahandling.gateway.aws.s3;

import com.al.datahandling.finance.domain.FinanceData;
import com.al.datahandling.finance.gateway.FinanceObjectApi;
import com.al.datahandling.finance.gateway.FinanceObjectException;
import com.al.datahandling.finance.gateway.aws.s3.S3FinanceObjectImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith( MockitoExtension.class )
public class S3FinanceObjectImplTest {

    private static final String S3_BUCKET = "dummy-bucket";

    private static final String S3_OBJECT_KEY = "dummy-file.zip";

    private static final String ZIP_FILE_LOCATION = String.format("s3://%s/%s", S3_BUCKET, S3_OBJECT_KEY);

    private static final Region S3_DUMMY_REGION = Region.of("dummy-region");

    private static final GetObjectRequest GET_OBJECT_REQUEST = GetObjectRequest.builder()
            .bucket(S3_BUCKET)
            .key(S3_OBJECT_KEY)
            .build();

    @Mock
    S3Client s3Client;

    @InjectMocks
    private FinanceObjectApi financeObjectApi = new S3FinanceObjectImpl();

    @BeforeEach
    public void setUp() {
        when(s3Client.utilities()).thenReturn(S3Utilities.builder().region(S3_DUMMY_REGION).build());
    }

    @Test
    public void shouldGetFileSuccessfully() throws IOException {
        // mock S3.getObject
        try (final ByteArrayOutputStream baos = generateCSVsZipFile()) {
            ResponseBytes<ByteArrayOutputStream> res = ResponseBytes.fromByteArray(baos, baos.toByteArray());
            when(s3Client.getObject(eq(GET_OBJECT_REQUEST), any(ResponseTransformer.class)))
                    .thenReturn(res);
        }

        List<FinanceData> finances = financeObjectApi.get(ZIP_FILE_LOCATION);

        assertThat(finances.size(), equalTo(2));
        assertThat(finances, containsInAnyOrder(
                new FinanceData("123", "maikon", null),
                new FinanceData("234", "cezario", null)
        ));
    }

    @Test
    public void shouldThrowExceptionIfFailureHappensWhenManipulatingFile() throws IOException {
        // mock S3.getObject and simulate an error when dealing with the downloaded file
        final IOException dummyIoException = new IOException("Dummy error");

        ResponseBytes<ByteArrayOutputStream> res = mock(ResponseBytes.class);
        when(s3Client.getObject(eq(GET_OBJECT_REQUEST), any(ResponseTransformer.class)))
                .thenReturn(res);
        InputStream is = mock(InputStream.class);
        when(res.asInputStream()).thenReturn(is);
        when(is.read(any(), anyInt(), anyInt())).thenThrow(dummyIoException);

        RuntimeException thrown = assertThrows(FinanceObjectException.class, () -> financeObjectApi.get(ZIP_FILE_LOCATION));
        assertThat(thrown.getMessage(), equalTo(String.format("Error to manipulate file from AWS S3 bucket %s", ZIP_FILE_LOCATION)));
        assertThat(thrown.getCause(), instanceOf(IOException.class));
        assertThat(thrown.getCause(), equalTo(dummyIoException));
    }

    @Test
    public void shouldThrowExceptionIfFailureHappensWhenGettingFile() throws IOException {
        // mock S3.getObject and simulate an error when dealing with the downloaded file
        final SdkException dummyAwsException = SdkException.create("Dummy message", new Exception("Dummy error"));
        when(s3Client.getObject(eq(GET_OBJECT_REQUEST), any(ResponseTransformer.class)))
                .thenThrow(dummyAwsException);

        RuntimeException thrown = assertThrows(FinanceObjectException.class, () -> financeObjectApi.get(ZIP_FILE_LOCATION));
        assertThat(thrown.getMessage(), equalTo(String.format("Error to get object from AWS S3 bucket %s", ZIP_FILE_LOCATION)));
        assertThat(thrown.getCause(), instanceOf(SdkException.class));
        assertThat(thrown.getCause(), equalTo(dummyAwsException));
    }

    private ByteArrayOutputStream generateCSVsZipFile() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry1 = new ZipEntry("file1.csv");
            zos.putNextEntry(entry1);
            zos.write("maikon;123".getBytes());

            ZipEntry entry2 = new ZipEntry("file2.csv");
            zos.putNextEntry(entry2);
            zos.write("cezario;234".getBytes());
            zos.closeEntry();
        }
        return baos;
    }

}
