package util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class ImageUploadUtil {
    private static final String UPLOAD_DIRECTORY = "uploads/images";
    
    public static void initUploadDirectory() {
        File uploadDir = new File(UPLOAD_DIRECTORY);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
    }
    
    public static String saveImage(File sourceFile, String originalFilename) {
        try {
            initUploadDirectory();
            
            String extension = "";
            int i = originalFilename.lastIndexOf('.');
            if (i > 0) {
                extension = originalFilename.substring(i);
            }
            
            String newFilename = UUID.randomUUID().toString() + extension;
            Path targetPath = Paths.get(UPLOAD_DIRECTORY, newFilename);
            
            Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            return newFilename;
        } catch (IOException e) {
            System.err.println("Error saving image: " + e.getMessage());
            return null;
        }
    }
    
    public static boolean deleteImage(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        
        try {
            Path filePath = Paths.get(UPLOAD_DIRECTORY, filename);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Error deleting image: " + e.getMessage());
            return false;
        }
    }
    
    public static String getImagePath(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        
        Path filePath = Paths.get(UPLOAD_DIRECTORY, filename);
        return filePath.toAbsolutePath().toString();
    }
    
    public static boolean imageExists(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        
        Path filePath = Paths.get(UPLOAD_DIRECTORY, filename);
        return Files.exists(filePath);
    }
} 