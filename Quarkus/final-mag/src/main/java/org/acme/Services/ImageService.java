package org.acme.Services;

import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@ApplicationScoped
public class ImageService {

    private static final String BASE_UPLOAD_DIR = "Uploads/";
    private static final String GAMES_UPLOAD_DIR = BASE_UPLOAD_DIR + "games/";
    private static final String ARTICLES_UPLOAD_DIR = BASE_UPLOAD_DIR + "articles/";
    private static final String AVATARS_UPLOAD_DIR = BASE_UPLOAD_DIR + "avatars/";
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
    private static final String DEFAULT_COVER_URL = "default-cover.jpg";

    // Target dimensions for different image types
    private static final int GAME_WIDTH = 1920;
    private static final int GAME_HEIGHT = 1080; // 16:9 ratio
    private static final int ARTICLE_WIDTH = 1920;
    private static final int ARTICLE_HEIGHT = 1080; // 16:9 ratio
    private static final int AVATAR_SIZE = 512; // 1:1 ratio (square)

    public ImageService() {
        createDirectoryIfNotExists(BASE_UPLOAD_DIR, "Base Uploads");
        createDirectoryIfNotExists(GAMES_UPLOAD_DIR, "Games");
        createDirectoryIfNotExists(ARTICLES_UPLOAD_DIR, "Articles");
        createDirectoryIfNotExists(AVATARS_UPLOAD_DIR, "Avatars");
    }

    private void createDirectoryIfNotExists(String dirPath, String dirName) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public String uploadImage(FileUpload file, String existingUrl, String imageType) throws IOException {
        if (file == null || file.fileName() == null || file.fileName().isEmpty()) {
            return null;
        }

        String filename = file.fileName().toLowerCase();
        if (!isValidImageFile(filename)) {
            throw new IllegalArgumentException("Invalid image file type. Allowed: jpg, jpeg, png, gif, webp.");
        }

        String uploadDir = getUploadDirectory(imageType);
        String urlPrefix = getUrlPrefix(imageType);

        // Generate unique filename (always save as .jpg for consistency)
        String uniqueFilename = UUID.randomUUID() + ".jpg";
        Path targetPath = Paths.get(uploadDir, uniqueFilename);

        try {
            // Read and process the image
            BufferedImage originalImage = readImage(file.uploadedFile());
            BufferedImage processedImage = processImageByType(originalImage, imageType);

            // Save the processed image as JPEG
            ImageIO.write(processedImage, "jpg", targetPath.toFile());

        } catch (IOException e) {
            throw new IOException("Image processing failed: " + e.getMessage(), e);
        }

        // Delete old file if it exists
        deleteOldImage(existingUrl, uploadDir);

        String newUrl = urlPrefix + uniqueFilename;
        return newUrl;
    }

    private BufferedImage readImage(Path filePath) throws IOException {
        BufferedImage image;

        // Try direct file reading first
        try {
            image = ImageIO.read(filePath.toFile());
        } catch (Exception e) {
            // Fallback to InputStream reading
            try (var inputStream = Files.newInputStream(filePath)) {
                image = ImageIO.read(inputStream);
            }
        }

        if (image == null) {
            throw new IOException("Unable to read image file - may be corrupted or unsupported format");
        }

        return image;
    }

    private BufferedImage processImageByType(BufferedImage originalImage, String imageType) {
        return switch (imageType.toLowerCase()) {
            case "games" -> resizeImage(originalImage, GAME_WIDTH, GAME_HEIGHT);
            case "articles" -> resizeImage(originalImage, ARTICLE_WIDTH, ARTICLE_HEIGHT);
            case "avatars" -> resizeImage(originalImage, AVATAR_SIZE, AVATAR_SIZE);
            default -> throw new IllegalArgumentException("Invalid image type: " + imageType);
        };
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();

        // Enable high-quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();

        return resizedImage;
    }

    private boolean isValidImageFile(String filename) {
        for (String ext : ALLOWED_EXTENSIONS) {
            if (filename.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    private String getUploadDirectory(String imageType) {
        return switch (imageType.toLowerCase()) {
            case "games" -> GAMES_UPLOAD_DIR;
            case "articles" -> ARTICLES_UPLOAD_DIR;
            case "avatars" -> AVATARS_UPLOAD_DIR;
            default -> throw new IllegalArgumentException("Invalid image type: " + imageType);
        };
    }

    private String getUrlPrefix(String imageType) {
        return switch (imageType.toLowerCase()) {
            case "games" -> "/api/images/games/";
            case "articles" -> "/api/images/articles/";
            case "avatars" -> "/api/images/avatars/";
            default -> throw new IllegalArgumentException("Invalid image type: " + imageType);
        };
    }

    private void deleteOldImage(String existingUrl, String uploadDir) {
        if (existingUrl == null || existingUrl.trim().isEmpty() || existingUrl.equals(DEFAULT_COVER_URL)) {
            return;
        }

        try {
            String oldFilename;
            if (existingUrl.contains("/")) {
                oldFilename = existingUrl.substring(existingUrl.lastIndexOf('/') + 1);
            } else {
                oldFilename = existingUrl;
            }

            if (!oldFilename.trim().isEmpty()) {
                Path oldFilePath = Paths.get(uploadDir, oldFilename);
                Files.deleteIfExists(oldFilePath);
            }
        } catch (Exception e) {
            // Log error but don't fail the upload
            System.err.println("Failed to delete old image: " + e.getMessage());
        }
    }

    public String getDefaultCoverUrl() {
        return DEFAULT_COVER_URL;
    }
}