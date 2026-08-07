package com.project.codinviec_core_service.service.file;

import com.project.codinviec_core_service.enums.ResourceErrorCode;
import com.project.codinviec_core_service.exception.AppException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class FileServiceImp implements FileService {
    @Value("${upload.image}")
    private String root;

    @Override
    public String saveFiles(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                throw new AppException(ResourceErrorCode.FILE_ERROR, "Save file services lỗi");
            }

            Path rootPath = Paths.get(root);
            if(Files.notExists(rootPath)){
                Files.createDirectories(rootPath);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                originalFilename = originalFilename.substring(0, originalFilename.lastIndexOf("."));
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);
            String uniqueFilename = timestamp + "_" + uniqueId + "_" + originalFilename + extension;

            Path filePath = rootPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return uniqueFilename;
        }
        catch (AppException e) {
            throw e;
        }
        catch (Exception e) {
            throw new AppException(ResourceErrorCode.FILE_ERROR);
        }
    }

    @Override
    public Resource getFile(String fileName) {
        try {
            Path rootPath = Paths.get(root).resolve(fileName);
            Resource resource = new UrlResource(rootPath.toUri());
            if(resource.exists() || resource.isReadable()){
                return resource;
            }
            else {
                throw new AppException(ResourceErrorCode.FILE_ERROR);
            }
        }
        catch (AppException e) {
            throw e;
        }
        catch (Exception e) {
            throw new AppException(ResourceErrorCode.FILE_ERROR);
        }
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            if (fileName == null || fileName.isBlank()) return;

            Path filePath = Paths.get(root).resolve(fileName).normalize();

            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (Exception e) {
            throw new AppException(ResourceErrorCode.FILE_ERROR);
        }
    }

}
