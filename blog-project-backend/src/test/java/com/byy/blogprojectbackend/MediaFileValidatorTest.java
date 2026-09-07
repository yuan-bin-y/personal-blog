package com.byy.blogprojectbackend;

import com.byy.blogprojectbackend.media.config.MediaStorageProperties;
import com.byy.blogprojectbackend.media.enums.MediaType;
import com.byy.blogprojectbackend.media.enums.MediaUsageType;
import com.byy.blogprojectbackend.media.exception.MediaPayloadTooLargeException;
import com.byy.blogprojectbackend.media.exception.UnsupportedMediaTypeException;
import com.byy.blogprojectbackend.media.validation.DetectedMedia;
import com.byy.blogprojectbackend.media.validation.MediaFileValidator;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MediaFileValidatorTest {

    private final MediaStorageProperties properties = new MediaStorageProperties();
    private final MediaFileValidator validator = new MediaFileValidator(properties);

    @Test
    void validate_detectsRealImageContentInsteadOfTrustingFilename() throws Exception {
        BufferedImage image = new BufferedImage(2, 3, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        javax.imageio.ImageIO.write(image, "png", output);
        MockMultipartFile file = new MockMultipartFile(
                "file", "fake.txt", "text/plain", output.toByteArray()
        );

        DetectedMedia detected = validator.validate(file, MediaUsageType.AVATAR);

        assertEquals(MediaType.IMAGE, detected.mediaType());
        assertEquals("image/png", detected.contentType());
        assertEquals(2, detected.width());
        assertEquals(3, detected.height());
    }

    @Test
    void validate_rejectsMediaTypeThatDoesNotMatchUsage() {
        MockMultipartFile audio = new MockMultipartFile(
                "file", "music.mp3", "audio/mpeg", new byte[]{'I', 'D', '3', 0}
        );

        assertThrows(
                UnsupportedMediaTypeException.class,
                () -> validator.validate(audio, MediaUsageType.AVATAR)
        );
    }

    @Test
    void validate_enforcesConfiguredSizeLimit() {
        properties.setMaxAudioBytes(3);
        MockMultipartFile audio = new MockMultipartFile(
                "file", "music.mp3", "audio/mpeg", new byte[]{'I', 'D', '3', 0}
        );

        assertThrows(
                MediaPayloadTooLargeException.class,
                () -> validator.validate(audio, MediaUsageType.MUSIC)
        );
    }
}
