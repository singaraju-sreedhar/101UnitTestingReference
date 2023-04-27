package io.marketplace.services.contact.config;

import io.marketplace.commons.logging.Logger;
import io.marketplace.commons.logging.LoggerFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

@Configuration
public class ExtraConfigLoader {
    private static Logger log = LoggerFactory.getLogger(ExtraConfigLoader.class);
    private static final String KRB5_DEBUG = "java.security.krb5.debug";
    private static final String JAAS_TEMPLATE = "com.sun.security.auth.module.Krb5LoginModule " + "required renewTicket=true  " + "doNotPrompt=true debug=true "
            + "serviceName=\"%s\"  " + "principal=\"%s\" " + "useKeyTab=true " + "storeKey=true  client=true " + "keyTab=\"%s\";";

    @Value("${kafka.keytab.file:}")
    private String kafkaKeyTabFile;

    @Value("${kafka.service.name:kafka}")
    private String kafkaServiceName;

    @Value("${kafka.principal:SVC_ABACUS_KAFKA_PRE@SANUK.SANTANDERUK.CORP}")
    private String kafkaPrinciple;

    @Value("${spring.cloud.config.uri}")
    public String cloudConfigServiceUrl;

    @Value("${spring.profiles.active:Pre}")
    public String cloudConfigServiceProfile;

    @Value("${spring.cloud.config.label}")
    public String cloudConfigServiceLabel;


    @Value("${settings.files:}")
    public String settingFiles;

    @Value(value = "#{${jvm.opts:{:}}}")
    private Map<String, String> jvmOpts;

    public String getJaasConfig() {
        if (!StringUtils.isEmpty(kafkaKeyTabFile)) {

            return String.format(JAAS_TEMPLATE,
                    kafkaServiceName,
                    kafkaPrinciple,
                    kafkaKeyTabFile);
        }
        return null;
    }

    @PostConstruct
    public void init() {
        String copyLocation = System.getProperty("java.io.tmpdir");
        new File(copyLocation).mkdirs();
        if (!StringUtils.isEmpty(settingFiles)) {
            Arrays.stream(settingFiles.split(","))
                  .map(String::trim)
                  .forEach(this::downloadFile);
        }
        jvmOpts.forEach(System::setProperty);
    }

    private void downloadFile(final String fileName) {
        String copyLocation = System.getProperty("java.io.tmpdir");
        final RestTemplate restTemplate = new RestTemplate();
        String baseUrl = String.format("%s/*/%s/%s/",
                cloudConfigServiceUrl,
                cloudConfigServiceProfile,
                cloudConfigServiceLabel);
        try {
            final ResponseEntity<byte[]> response = restTemplate
                    .getForEntity(baseUrl + fileName, byte[].class);
            FileUtils.writeByteArrayToFile(new File(copyLocation, FilenameUtils.getName(fileName)), response.getBody());
            if(fileName.endsWith(".base64")) {
              byte[] decodedData =  Base64.getMimeEncoder().encode(response.getBody());
                String binaryFileName = fileName
                        .replace(".base64", ".bin");
                FileUtils.writeByteArrayToFile(new File(copyLocation, FilenameUtils.getName(binaryFileName)), decodedData);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Download setting file error", e);
        }
    }

    public void printDebug() {
        log.info("{} = {}", KRB5_DEBUG, System.getProperty(KRB5_DEBUG, "(notset)"));
    }
}
