package com.silliconthink.blog.service;

import com.silliconthink.blog.dto.MediaUploadVO;
import com.silliconthink.common.ErrorCode;
import com.silliconthink.config.AppProperties;
import com.silliconthink.blog.storage.BlogObjectKeys;
import com.silliconthink.blog.storage.BlogObjectStore;
import com.silliconthink.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaUploadService {

    private final AppProperties appProperties;
    private final BlogObjectStore blogObjectStore;

    public MediaUploadVO uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ErrorCode.INVALID_IMAGE);
        }
        if (!blogObjectStore.isRootWritable()) {
            throw new BizException(ErrorCode.MEDIA_STORAGE_UNAVAILABLE);
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR);
        }

        long maxBytes = appProperties.getUpload().getMaxBytes();
        if (bytes.length > maxBytes) {
            throw new BizException(ErrorCode.IMAGE_TOO_LARGE);
        }

        ImageKind kind = detectImage(bytes);
        if (kind == null) {
            throw new BizException(ErrorCode.INVALID_IMAGE);
        }

        String filename = UUID.randomUUID().toString().replace("-", "") + "." + kind.ext();
        String key = BlogObjectKeys.media(filename);
        blogObjectStore.put(key, bytes);

        // Public URL maps /uploads/** -> media/** under storage root
        String publicPath = normalizePublicPrefix(appProperties.getUpload().getPublicPathPrefix());
        String relativeUnderMedia = key.substring("media/".length());
        return new MediaUploadVO(publicPath + "/" + relativeUnderMedia);
    }

    private static String normalizePublicPrefix(String publicPath) {
        String path = publicPath == null ? "/uploads" : publicPath;
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    static ImageKind detectImage(byte[] data) {
        if (data == null || data.length < 12) {
            return null;
        }
        if (data[0] == (byte) 0xFF && data[1] == (byte) 0xD8 && data[2] == (byte) 0xFF) {
            return ImageKind.JPEG;
        }
        if (data[0] == (byte) 0x89 && data[1] == 0x50 && data[2] == 0x4E && data[3] == 0x47
                && data[4] == 0x0D && data[5] == 0x0A && data[6] == 0x1A && data[7] == 0x0A) {
            return ImageKind.PNG;
        }
        if (data[0] == 'G' && data[1] == 'I' && data[2] == 'F'
                && data[3] == '8' && (data[4] == '7' || data[4] == '9') && data[5] == 'a') {
            return ImageKind.GIF;
        }
        if (data[0] == 'R' && data[1] == 'I' && data[2] == 'F' && data[3] == 'F'
                && data[8] == 'W' && data[9] == 'E' && data[10] == 'B' && data[11] == 'P') {
            return ImageKind.WEBP;
        }
        return null;
    }

    enum ImageKind {
        JPEG("jpg"),
        PNG("png"),
        GIF("gif"),
        WEBP("webp");

        private final String ext;

        ImageKind(String ext) {
            this.ext = ext;
        }

        String ext() {
            return ext;
        }
    }
}
