package com.byy.blogprojectbackend.media.validation;

import com.byy.blogprojectbackend.media.config.MediaStorageProperties;
import com.byy.blogprojectbackend.media.enums.MediaType;
import com.byy.blogprojectbackend.media.enums.MediaUsageType;
import com.byy.blogprojectbackend.media.exception.MediaPayloadTooLargeException;
import com.byy.blogprojectbackend.media.exception.UnsupportedMediaTypeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** 使用文件签名而不是扩展名识别媒体，并校验使用场景。 */
@Component
@RequiredArgsConstructor
public class MediaFileValidator {

    private static final Map<MediaUsageType, Set<MediaType>> ALLOWED_TYPES = Map.of(
            MediaUsageType.AVATAR, EnumSet.of(MediaType.IMAGE),
            MediaUsageType.HERO, EnumSet.of(MediaType.IMAGE, MediaType.VIDEO),
            MediaUsageType.POST_COVER, EnumSet.of(MediaType.IMAGE),
            MediaUsageType.POST_CONTENT, EnumSet.of(MediaType.IMAGE, MediaType.VIDEO),
            // MUSIC 同时承载播放器音频和音乐封面；两者会在站点配置中分别保存 URL。
            MediaUsageType.MUSIC, EnumSet.of(MediaType.IMAGE, MediaType.AUDIO),
            MediaUsageType.PAGE_BACKGROUND, EnumSet.of(MediaType.IMAGE, MediaType.VIDEO)
    );

    private final MediaStorageProperties properties;

    public DetectedMedia validate(MultipartFile file, MediaUsageType usageType) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        DetectedMedia detected = detect(file);
        if (!ALLOWED_TYPES.get(usageType).contains(detected.mediaType())) {
            throw new UnsupportedMediaTypeException(
                    usageType + " 不支持 " + detected.mediaType() + " 文件"
            );
        }

        long limit = switch (detected.mediaType()) {
            case IMAGE -> properties.getMaxImageBytes();
            case VIDEO -> properties.getMaxVideoBytes();
            case AUDIO -> properties.getMaxAudioBytes();
        };
        if (file.getSize() > limit) {
            throw new MediaPayloadTooLargeException("上传文件超过允许大小");
        }
        return detected;
    }

    private DetectedMedia detect(MultipartFile file) {
        byte[] header = new byte[16];
        int length;
        try (InputStream input = file.getInputStream()) {
            length = input.read(header);
        } catch (IOException exception) {
            throw new IllegalArgumentException("无法读取上传文件");
        }

        if (length >= 8 && matches(header, 0, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)) {
            return image(file, "image/png", "png");
        }
        if (length >= 3 && matches(header, 0, 0xFF, 0xD8, 0xFF)) {
            return image(file, "image/jpeg", "jpg");
        }
        if (length >= 6 && ascii(header, 0, "GIF87a", "GIF89a")) {
            return image(file, "image/gif", "gif");
        }
        if (length >= 12 && ascii(header, 0, "RIFF") && ascii(header, 8, "WEBP")) {
            return new DetectedMedia(MediaType.IMAGE, "image/webp", "webp", null, null, null);
        }
        if (length >= 8 && ascii(header, 4, "ftyp")) {
            return new DetectedMedia(MediaType.VIDEO, "video/mp4", "mp4", null, null, null);
        }
        if (length >= 4 && matches(header, 0, 0x1A, 0x45, 0xDF, 0xA3)) {
            return new DetectedMedia(MediaType.VIDEO, "video/webm", "webm", null, null, null);
        }
        if ((length >= 3 && ascii(header, 0, "ID3"))
                || (length >= 2 && (header[0] & 0xFF) == 0xFF && (header[1] & 0xE0) == 0xE0)) {
            return new DetectedMedia(MediaType.AUDIO, "audio/mpeg", "mp3", null, null, null);
        }
        if (length >= 12 && ascii(header, 0, "RIFF") && ascii(header, 8, "WAVE")) {
            return new DetectedMedia(MediaType.AUDIO, "audio/wav", "wav", null, null, null);
        }
        if (length >= 4 && ascii(header, 0, "OggS")) {
            return new DetectedMedia(MediaType.AUDIO, "audio/ogg", "ogg", null, null, null);
        }

        throw new UnsupportedMediaTypeException("无法识别或不支持该媒体格式");
    }

    private DetectedMedia image(MultipartFile file, String contentType, String extension) {
        try (InputStream input = file.getInputStream()) {
            BufferedImage image = ImageIO.read(input);
            if (image == null) {
                throw new UnsupportedMediaTypeException("图片内容损坏或无法解析");
            }
            return new DetectedMedia(
                    MediaType.IMAGE,
                    contentType,
                    extension,
                    image.getWidth(),
                    image.getHeight(),
                    null
            );
        } catch (IOException exception) {
            throw new UnsupportedMediaTypeException("图片内容损坏或无法解析");
        }
    }

    private boolean matches(byte[] bytes, int offset, int... expected) {
        for (int i = 0; i < expected.length; i++) {
            if ((bytes[offset + i] & 0xFF) != expected[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean ascii(byte[] bytes, int offset, String... candidates) {
        for (String candidate : candidates) {
            boolean matches = true;
            for (int i = 0; i < candidate.length(); i++) {
                if (bytes[offset + i] != (byte) candidate.charAt(i)) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                return true;
            }
        }
        return false;
    }
}
