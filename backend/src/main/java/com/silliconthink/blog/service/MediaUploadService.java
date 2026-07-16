package com.silliconthink.blog.service;

import com.silliconthink.blog.dto.MediaUploadVO;
import com.silliconthink.common.ErrorCode;
import com.silliconthink.config.AppProperties;
import com.silliconthink.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaUploadService {

    private final AppProperties appProperties;

    public MediaUploadVO uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ErrorCode.INVALID_IMAGE);
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

        // 以文件魔数为准，不信任客户端 Content-Type / 扩展名
        ImageKind kind = detectImage(bytes);
        if (kind == null) {
            throw new BizException(ErrorCode.INVALID_IMAGE);
        }

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + kind.ext();
        Path root = Paths.get(appProperties.getUpload().getDir()).toAbsolutePath().normalize();
        Path targetDir = root.resolve(datePath).normalize();
        if (!targetDir.startsWith(root)) {
            throw new BizException(ErrorCode.INTERNAL_ERROR);
        }
        try {
            Files.createDirectories(targetDir);
            Files.write(targetDir.resolve(filename), bytes);
        } catch (IOException e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR);
        }

        String publicPath = normalizePublicPrefix(appProperties.getUpload().getPublicPathPrefix());
        return new MediaUploadVO(publicPath + "/" + datePath + "/" + filename);
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

    /**
     * 根据文件头识别常见图片格式；无法识别则拒绝。
     */
    static ImageKind detectImage(byte[] data) {
        if (data == null || data.length < 12) {
            return null;
        }
        // JPEG: FF D8 FF
        if (data[0] == (byte) 0xFF && data[1] == (byte) 0xD8 && data[2] == (byte) 0xFF) {
            return ImageKind.JPEG;
        }
        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if (data[0] == (byte) 0x89 && data[1] == 0x50 && data[2] == 0x4E && data[3] == 0x47
                && data[4] == 0x0D && data[5] == 0x0A && data[6] == 0x1A && data[7] == 0x0A) {
            return ImageKind.PNG;
        }
        // GIF: GIF87a / GIF89a
        if (data[0] == 'G' && data[1] == 'I' && data[2] == 'F'
                && data[3] == '8' && (data[4] == '7' || data[4] == '9') && data[5] == 'a') {
            return ImageKind.GIF;
        }
        // WEBP: RIFF....WEBP
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
